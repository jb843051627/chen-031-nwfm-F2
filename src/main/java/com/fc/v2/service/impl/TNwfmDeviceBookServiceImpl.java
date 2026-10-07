package com.fc.v2.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.mapper.auto.TNwfmDeviceBookMapper;
import com.fc.v2.mapper.auto.TNwfmDeviceBookReceiptMapper;
import com.fc.v2.mapper.auto.TNwfmDeviceBookRowMapper;
import com.fc.v2.model.auto.TNwfmDeviceBook;
import com.fc.v2.model.auto.TNwfmDeviceBookReceipt;
import com.fc.v2.model.auto.TNwfmDeviceBookRow;
import com.fc.v2.model.custom.nwfm.DeviceBookForm;
import com.fc.v2.model.custom.nwfm.DeviceBookLine;
import com.fc.v2.model.custom.nwfm.DeviceBookReceiptView;
import com.fc.v2.model.custom.nwfm.DeviceBookRowView;
import com.fc.v2.model.custom.nwfm.DeviceBookSplitView;
import com.fc.v2.service.ITNwfmDeviceBookService;

/**
 * 器具册整串 Service 实现（intake-receipt 形状）。
 *
 * <p>一趟收串走两道：进门解析只跑一遍（名目照册头核，空名目按没写，不合当场退回、不落册、
 * 不占行次），随后「逐行定去向」一趟出三笔（进门/收下/退回），回执只认这一趟的返回。
 * 册头那格定分串上限（缺省 150），溢出另立下一串；行次系统在本串内排定、落定不变；
 * 串一经收下不许再加行，续办只补没办的行，办好的不收惊；同一来手第二回、另一来手走
 * 第二遍，都只照面不翻账；空串只落「空册」不出「收讫」。</p>
 *
 * @author fuce
 * @date 2026-10-07
 */
@Service
public class TNwfmDeviceBookServiceImpl implements ITNwfmDeviceBookService {

    /** 册头没写按一百五十行 */
    private static final int DEFAULT_DECLARED_ROWS = 150;

    /** 逐行去向 */
    private static final int DIR_PENDING = 0;
    private static final int DIR_ACCEPTED = 1;
    private static final int DIR_REJECTED = 2;

    /** 串状态 */
    private static final int ST_OPEN = 0;
    private static final int ST_CLOSED = 1;
    private static final int ST_EMPTY = 2;

    /** 来手 0市场自录 1区里代收 */
    private static final int SOURCE_MARKET = 0;

    @javax.annotation.Resource
    private TNwfmDeviceBookMapper nwfmDeviceBookMapper;

    @javax.annotation.Resource
    private TNwfmDeviceBookRowMapper nwfmDeviceBookRowMapper;

    @javax.annotation.Resource
    private TNwfmDeviceBookReceiptMapper nwfmDeviceBookReceiptMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceBookReceiptView receiveBook(DeviceBookForm form) {
        if (form == null || form.getBookNo() == null || form.getBookNo().trim().isEmpty()) {
            throw new IllegalArgumentException("册头号不能为空：一次提交只进器具册整串这一个口子");
        }
        String bookNo = form.getBookNo().trim();
        int sourceType = form.getSourceType() == null ? SOURCE_MARKET : form.getSourceType();
        int declared = resolveDeclared(form.getDeclaredRows());
        String headerKind = form.getHeaderKind() == null ? null : form.getHeaderKind().trim();
        List<DeviceBookLine> lines = form.getLines() == null
                ? new ArrayList<DeviceBookLine>() : form.getLines();

        // 空串是正经输入：零行的串只落「空册」，不出「收讫」，也不从别串拉一行填数；
        // 已经有行的串收到空提交，只照面，不把老行抹成空册
        if (lines.isEmpty()) {
            return receiveEmpty(bookNo, form.getMarketCode(), headerKind, declared, sourceType);
        }

        // 正本先立住（申报行数/名目只在立串那一刻落定，隔年再查还是这个数）
        boolean[] existed = new boolean[1];
        TNwfmDeviceBook head0 = ensureSplit(bookNo, form.getMarketCode(), headerKind, 0, declared, existed);
        // 再交/续办认册上回来的数：名目规则与分串上限以册头当初落定的为准，中间不许改、不许加行
        headerKind = head0.getHeaderKind();
        declared = head0.getDeclaredRows() == null ? declared : head0.getDeclaredRows().intValue();

        // 第一道：进门解析，整串只跑这一遍。不合的行连门都进不去——当场退回、不落册，
        // 也不进回执三笔（三笔只数「逐行定去向」那一趟有去向的行），门口退回另列
        RoundResult gate = new RoundResult();
        List<DeviceBookLine> admitted = new ArrayList<DeviceBookLine>();
        parseOnce(head0, lines, headerKind, sourceType, gate, admitted);

        // 第二道：解析过的行逐串定去向。本串锁定后不再加行——陌生行溢出到下一串
        DeviceBookReceiptView main = null;
        List<DeviceBookLine> remaining = admitted;
        int seq = 0;
        while (remaining != null && !remaining.isEmpty()) {
            TNwfmDeviceBook head = seq == 0 ? head0
                    : ensureSplit(bookNo, form.getMarketCode(), headerKind, seq, declared, existed);
            List<DeviceBookLine> overflow = new ArrayList<DeviceBookLine>();
            RoundResult rr;
            if (seq == 0) {
                // 正本：解析退回也归在本串门口三笔里
                rr = gate;
                settleChunk(head, remaining, sourceType, existed[0], overflow, rr);
            } else {
                rr = new RoundResult();
                settleChunk(head, remaining, sourceType, existed[0], overflow, rr);
            }

            refreshHeadCounts(head);
            boolean hasPending = nz(head.getReceivedRows())
                    > nz(head.getAcceptedRows()) + nz(head.getRejectedRows());
            // 收讫：没有挂起的行、册上确实落了行（空册/全在门口退回不出）；
            // 本回是否亮「收讫」字样另看 touched——照面回不亮，但册头状态保持已收讫
            boolean closed = !hasPending && nz(head.getReceivedRows()) > 0;
            head.setStatus(Integer.valueOf(hasPending ? ST_OPEN : (closed ? ST_CLOSED : ST_EMPTY)));
            head.setClosedFlag(Integer.valueOf(closed ? 1 : 0));
            this.nwfmDeviceBookMapper.updateById(head);

            TNwfmDeviceBookReceipt receipt = writeReceipt(head, sourceType,
                    rr.inRows, rr.acceptedRows, rr.rejectedRows, false, closed && rr.touched, rr.touched);
            DeviceBookReceiptView view = buildReceiptView(head, sourceType, receipt);
            view.setTouched(rr.touched);
            if (main == null) {
                main = view;
            } else if (rr.touched) {
                // 中间串的「照面」回执仍各留各的；本回真正落了行的新串才挂进分串清单
                main.getSplits().add(toSplitView(view));
            }
            remaining = overflow;
            seq++;
        }

        if (main == null) {
            // 一行都没进门（全在解析门口退回）：正本仍出一张回执，三笔全 0、不出收讫；
            // 门口退回的行挂在回执 rows 里，退在哪一条都问得出
            refreshHeadCounts(head0);
            head0.setStatus(Integer.valueOf(ST_OPEN));
            head0.setClosedFlag(Integer.valueOf(0));
            this.nwfmDeviceBookMapper.updateById(head0);
            TNwfmDeviceBookReceipt receipt = writeReceipt(head0, sourceType,
                    gate.inRows, gate.acceptedRows, gate.rejectedRows, false, false, gate.touched);
            main = buildReceiptView(head0, sourceType, receipt);
            main.setTouched(gate.touched);
        }
        return main;
    }

    @Override
    public List<DeviceBookReceiptView> listReceipts(String bookNo) {
        List<TNwfmDeviceBook> heads = listHeads(bookNo);
        List<TNwfmDeviceBookReceipt> receipts = this.nwfmDeviceBookReceiptMapper.selectList(
                new QueryWrapper<TNwfmDeviceBookReceipt>()
                        .eq("book_no", bookNo).eq("del_flag", 0)
                        .orderByAsc("seq_no", "source_type", "round_no"));
        List<DeviceBookReceiptView> views = new ArrayList<DeviceBookReceiptView>();
        for (TNwfmDeviceBookReceipt r : receipts) {
            views.add(buildReceiptView(findHead(heads, r.getSeqNo()),
                    r.getSourceType() == null ? SOURCE_MARKET : r.getSourceType().intValue(), r));
        }
        return views;
    }

    @Override
    public List<DeviceBookRowView> listRows(String bookNo, Integer seqNo) {
        if (bookNo == null || seqNo == null) {
            return new ArrayList<DeviceBookRowView>();
        }
        TNwfmDeviceBook head = findHead(listHeads(bookNo), seqNo);
        List<DeviceBookRowView> views = new ArrayList<DeviceBookRowView>();
        // 读法定死：往年入册的存量行一条不减，照旧数得着
        for (TNwfmDeviceBookRow r : landedRows(bookNo, seqNo.intValue())) {
            views.add(toRowView(r, head));
        }
        return views;
    }

    @Override
    public DeviceBookRowView traceRow(String bookNo, Integer seqNo, Integer rowNo) {
        if (bookNo == null || seqNo == null || rowNo == null) {
            return null;
        }
        TNwfmDeviceBookRow row = this.nwfmDeviceBookRowMapper.selectOne(
                new QueryWrapper<TNwfmDeviceBookRow>()
                        .eq("book_no", bookNo).eq("seq_no", seqNo).eq("row_no", rowNo)
                        .eq("del_flag", 0).last("limit 1"));
        // 解析没过的行不落到册上（没有行次）；册上压根没有这一行就不认
        if (row == null || row.getParsePass() == null || row.getParsePass().intValue() != 1) {
            return null;
        }
        return toRowView(row, findHead(listHeads(bookNo), seqNo));
    }

    // ---------------------------------------------------------------------
    // 第一道：进门解析（整串只跑一遍）
    // ---------------------------------------------------------------------
    private void parseOnce(TNwfmDeviceBook head, List<DeviceBookLine> lines, String headerKind,
                           int sourceType, RoundResult gate, List<DeviceBookLine> admitted) {
        Map<String, TNwfmDeviceBookRow> parseLedger = new HashMap<String, TNwfmDeviceBookRow>();
        for (TNwfmDeviceBookRow r : parseRejectedRows(head.getBookNo(), head.getSeqNo().intValue())) {
            parseLedger.put(identityKey(r.getStallName(), r.getDeviceKind()), r);
        }
        int pos = 0;
        for (DeviceBookLine line : lines) {
            pos++;
            String kind = line.getDeviceKind() == null ? null : line.getDeviceKind().trim();
            if (kindMatches(kind, headerKind)) {
                admitted.add(line);
                continue;
            }
            // 名目合不上（含空名目、空册头）：当场退回，不落册、不占行次；
            // 同一错行第二回到只照面，不重复落退回
            String key = identityKey(line.getStallName(), kind);
            if (parseLedger.containsKey(key)) {
                continue;
            }
            TNwfmDeviceBookRow bad = new TNwfmDeviceBookRow();
            bad.setBookNo(head.getBookNo());
            bad.setSeqNo(head.getSeqNo());
            bad.setRowNo(null);
            bad.setStallName(trim(line.getStallName()));
            bad.setDeviceKind(kind);
            bad.setParsePass(Integer.valueOf(0));
            bad.setHandleFlag(Integer.valueOf(1));
            bad.setDirection(Integer.valueOf(DIR_REJECTED));
            bad.setSourceType(Integer.valueOf(sourceType));
            bad.setRejectReason(parseReason(pos, kind, headerKind));
            bad.setDelFlag(Integer.valueOf(0));
            this.nwfmDeviceBookRowMapper.insert(bad);
            parseLedger.put(key, bad);
            // 门口退回不进三笔；本回确实办过事（落了一条门口退回），touched 留下痕迹
            gate.touched = true;
        }
    }

    // ---------------------------------------------------------------------
    // 第二道：逐行定去向（三笔同出这一趟）
    // ---------------------------------------------------------------------
    private void settleChunk(TNwfmDeviceBook head, List<DeviceBookLine> chunk, int sourceType,
                             boolean headExisted, List<DeviceBookLine> overflow, RoundResult rr) {
        Map<String, TNwfmDeviceBookRow> index = new HashMap<String, TNwfmDeviceBookRow>();
        int maxRowNo = 0;
        for (TNwfmDeviceBookRow r : landedRows(head.getBookNo(), head.getSeqNo().intValue())) {
            index.put(identityKey(r.getStallName(), r.getDeviceKind()), r);
            if (r.getRowNo() != null && r.getRowNo().intValue() > maxRowNo) {
                maxRowNo = r.getRowNo().intValue();
            }
        }

        int newTaken = 0;
        int cap = head.getDeclaredRows() == null ? DEFAULT_DECLARED_ROWS : head.getDeclaredRows();
        for (DeviceBookLine line : chunk) {
            String kind = line.getDeviceKind() == null ? null : line.getDeviceKind().trim();
            String key = identityKey(line.getStallName(), kind);
            TNwfmDeviceBookRow row = index.get(key);

            if (row == null) {
                // 陌生行：本串一经收下不许再加行；新串也只收册头那格定的数。
                // 多出来的不就地续在后头，开成下一串
                if (headExisted || newTaken >= cap) {
                    overflow.add(line);
                    continue;
                }
                maxRowNo++;
                row = new TNwfmDeviceBookRow();
                row.setBookNo(head.getBookNo());
                row.setSeqNo(head.getSeqNo());
                row.setRowNo(Integer.valueOf(maxRowNo));
                row.setStallName(trim(line.getStallName()));
                row.setDeviceKind(kind);
                row.setParsePass(Integer.valueOf(1));
                row.setHandleFlag(Integer.valueOf(0));
                row.setDirection(Integer.valueOf(DIR_PENDING));
                row.setDelFlag(Integer.valueOf(0));
                this.nwfmDeviceBookRowMapper.insert(row);
                index.put(key, row);
                newTaken++;
            }

            // 头一遍办好的行不回未核：
            //  · 已收下的，第二遍（另一来手）/重报都只照面，永不翻账；
            //  · 已退回的，另一来手走第二遍只照面；原主照原数再报也是照面；
            //    过几天改好了再报，认的还是当初排定的行次，落到下面在原行上重判，不另起一行。
            if (row.getHandleFlag() != null && row.getHandleFlag().intValue() == 1) {
                int dir = row.getDirection() == null ? DIR_PENDING : row.getDirection().intValue();
                boolean otherSource = row.getSourceType() != null
                        && row.getSourceType().intValue() != sourceType;
                if (dir == DIR_ACCEPTED || otherSource || (dir == DIR_REJECTED && sameNumbers(line, row))) {
                    continue;
                }
            }

            Integer declaredQty = line.getDeclaredQty();
            Integer matchedQty = line.getMatchedQty();
            // 两样少一样：这一行不办，系统不代填、不按零认下，留在本串等续办
            if (declaredQty == null || matchedQty == null) {
                row.setDirection(Integer.valueOf(DIR_PENDING));
                row.setHandleFlag(Integer.valueOf(0));
                row.setRejectReason(null);
                this.nwfmDeviceBookRowMapper.updateById(row);
                continue;
            }
            // 行次认当初排定的号；实收几具认册上回来的数（matchedQty）
            row.setDeclaredQty(declaredQty);
            row.setMatchedQty(matchedQty);
            if (declaredQty.intValue() != matchedQty.intValue()) {
                // 退就退在「差几具」那一句上，逐行问得出答得上；
                // 退了过几天再报，还是这一个行次，不重新起步
                row.setDirection(Integer.valueOf(DIR_REJECTED));
                row.setHandleFlag(Integer.valueOf(1));
                row.setRejectReason(mismatchReason(row.getRowNo().intValue(), declaredQty, matchedQty));
            } else {
                row.setDirection(Integer.valueOf(DIR_ACCEPTED));
                row.setHandleFlag(Integer.valueOf(1));
                row.setRejectReason(null);
            }
            if (row.getSourceType() == null) {
                row.setSourceType(Integer.valueOf(sourceType));
            }
            this.nwfmDeviceBookRowMapper.updateById(row);
            if (row.getDirection().intValue() == DIR_ACCEPTED) {
                rr.acceptedRows++;
            } else {
                rr.rejectedRows++;
            }
            rr.inRows++;
            rr.touched = true;
        }
    }

    // ---------------------------------------------------------------------
    // 空串
    // ---------------------------------------------------------------------
    private DeviceBookReceiptView receiveEmpty(String bookNo, String marketCode, String headerKind,
                                               int declared, int sourceType) {
        boolean[] existed = new boolean[1];
        TNwfmDeviceBook head = ensureSplit(bookNo, marketCode, headerKind, 0, declared, existed);
        int landed = landedRows(bookNo, 0).size();
        if (landed == 0) {
            // 零行：只落「空册」两个字，收讫那一句不出
            refreshHeadCounts(head);
            head.setStatus(Integer.valueOf(ST_EMPTY));
            head.setClosedFlag(Integer.valueOf(0));
            this.nwfmDeviceBookMapper.updateById(head);
            TNwfmDeviceBookReceipt receipt = writeReceipt(head, sourceType, 0, 0, 0, true, false, false);
            DeviceBookReceiptView view = buildReceiptView(head, sourceType, receipt);
            view.setEmpty(true);
            view.setTouched(false);
            return view;
        }
        // 有行的串收到空提交：只照面，三笔全 0，老行不受惊动
        TNwfmDeviceBookReceipt receipt = writeReceipt(head, sourceType, 0, 0, 0, false, false, false);
        DeviceBookReceiptView view = buildReceiptView(head, sourceType, receipt);
        view.setTouched(false);
        return view;
    }

    // ---------------------------------------------------------------------
    // 串/册头
    // ---------------------------------------------------------------------

    /** 取本分串册头；没有就立。申报行数/名目只在立串时落定，以后不改、不许加行 */
    private TNwfmDeviceBook ensureSplit(String bookNo, String marketCode, String headerKind,
                                        int seqNo, int declared, boolean[] existed) {
        TNwfmDeviceBook head = this.nwfmDeviceBookMapper.selectOne(new QueryWrapper<TNwfmDeviceBook>()
                .eq("book_no", bookNo).eq("seq_no", seqNo).eq("del_flag", 0).last("limit 1"));
        if (head != null) {
            existed[0] = true;
            return head;
        }
        existed[0] = false;
        head = new TNwfmDeviceBook();
        head.setBookNo(bookNo);
        head.setMarketCode(trim(marketCode));
        head.setHeaderKind(headerKind);
        head.setSeqNo(Integer.valueOf(seqNo));
        head.setDeclaredRows(Integer.valueOf(declared));
        head.setReceivedRows(Integer.valueOf(0));
        head.setAcceptedRows(Integer.valueOf(0));
        head.setRejectedRows(Integer.valueOf(0));
        head.setStatus(Integer.valueOf(ST_OPEN));
        head.setClosedFlag(Integer.valueOf(0));
        head.setDelFlag(Integer.valueOf(0));
        this.nwfmDeviceBookMapper.insert(head);
        return head;
    }

    /** 册头三笔按在册行现算（不另跑核验，页面抢先亮的数不算依据） */
    private void refreshHeadCounts(TNwfmDeviceBook head) {
        List<TNwfmDeviceBookRow> rows = landedRows(head.getBookNo(), head.getSeqNo().intValue());
        int acc = 0;
        int rej = 0;
        for (TNwfmDeviceBookRow r : rows) {
            int d = r.getDirection() == null ? DIR_PENDING : r.getDirection().intValue();
            if (d == DIR_ACCEPTED) {
                acc++;
            } else if (d == DIR_REJECTED) {
                rej++;
            }
        }
        head.setReceivedRows(Integer.valueOf(rows.size()));
        head.setAcceptedRows(Integer.valueOf(acc));
        head.setRejectedRows(Integer.valueOf(rej));
    }

    private List<TNwfmDeviceBook> listHeads(String bookNo) {
        if (bookNo == null) {
            return new ArrayList<TNwfmDeviceBook>();
        }
        return this.nwfmDeviceBookMapper.selectList(new QueryWrapper<TNwfmDeviceBook>()
                .eq("book_no", bookNo).eq("del_flag", 0).orderByAsc("seq_no"));
    }

    private TNwfmDeviceBook findHead(List<TNwfmDeviceBook> heads, Integer seqNo) {
        if (seqNo == null) {
            return null;
        }
        for (TNwfmDeviceBook h : heads) {
            if (seqNo.equals(h.getSeqNo())) {
                return h;
            }
        }
        return null;
    }

    /** 落到册上的行（解析过、占行次），按行次升序，一条不减 */
    private List<TNwfmDeviceBookRow> landedRows(String bookNo, int seqNo) {
        return this.nwfmDeviceBookRowMapper.selectList(new QueryWrapper<TNwfmDeviceBookRow>()
                .eq("book_no", bookNo).eq("seq_no", seqNo)
                .eq("parse_pass", 1).eq("del_flag", 0).orderByAsc("row_no"));
    }

    /** 解析门口退回的行（不落册、无行次） */
    private List<TNwfmDeviceBookRow> parseRejectedRows(String bookNo, int seqNo) {
        return this.nwfmDeviceBookRowMapper.selectList(new QueryWrapper<TNwfmDeviceBookRow>()
                .eq("book_no", bookNo).eq("seq_no", seqNo)
                .eq("parse_pass", 0).eq("del_flag", 0));
    }

    // ---------------------------------------------------------------------
    // 回执
    // ---------------------------------------------------------------------

    /** 两个来手各走各的账：同一来手每提交一回留一张，回次往上加 */
    private TNwfmDeviceBookReceipt writeReceipt(TNwfmDeviceBook head, int sourceType,
                                                int in, int acc, int rej,
                                                boolean empty, boolean acceptedFlag,
                                                boolean touched) {
        int round = this.nwfmDeviceBookReceiptMapper.selectCount(
                new QueryWrapper<TNwfmDeviceBookReceipt>()
                        .eq("book_no", head.getBookNo()).eq("seq_no", head.getSeqNo())
                        .eq("source_type", sourceType).eq("del_flag", 0)).intValue() + 1;
        TNwfmDeviceBookReceipt receipt = new TNwfmDeviceBookReceipt();
        receipt.setBookNo(head.getBookNo());
        receipt.setSeqNo(head.getSeqNo());
        receipt.setSourceType(Integer.valueOf(sourceType));
        receipt.setRoundNo(Integer.valueOf(round));
        receipt.setInRows(Integer.valueOf(in));
        receipt.setAcceptedRows(Integer.valueOf(acc));
        receipt.setRejectedRows(Integer.valueOf(rej));
        receipt.setEmptyFlag(Integer.valueOf(empty ? 1 : 0));
        receipt.setAcceptedFlag(Integer.valueOf(acceptedFlag && !empty ? 1 : 0));
        receipt.setTouchedFlag(Integer.valueOf(touched ? 1 : 0));
        receipt.setDelFlag(Integer.valueOf(0));
        this.nwfmDeviceBookReceiptMapper.insert(receipt);

        head.setLastSourceType(Integer.valueOf(sourceType));
        return receipt;
    }

    private DeviceBookReceiptView buildReceiptView(TNwfmDeviceBook head, int sourceType,
                                                   TNwfmDeviceBookReceipt receipt) {
        DeviceBookReceiptView v = new DeviceBookReceiptView();
        v.setBookNo(receipt.getBookNo());
        v.setSeqNo(receipt.getSeqNo());
        v.setSourceType(receipt.getSourceType() == null ? sourceType : receipt.getSourceType());
        v.setRoundNo(receipt.getRoundNo());
        v.setInRows(nz(receipt.getInRows()));
        v.setAcceptedRows(nz(receipt.getAcceptedRows()));
        v.setRejectedRows(nz(receipt.getRejectedRows()));
        // 后两笔加起来等于头一笔——三个数同出逐行定去向一趟，天然守恒
        v.setBalanced(v.getAcceptedRows() + v.getRejectedRows() == v.getInRows());
        v.setEmpty(receipt.getEmptyFlag() != null && receipt.getEmptyFlag().intValue() == 1);
        v.setAccepted(receipt.getAcceptedFlag() != null && receipt.getAcceptedFlag().intValue() == 1);
        v.setTouched(receipt.getTouchedFlag() == null || receipt.getTouchedFlag().intValue() == 1);
        if (head != null) {
            v.setMarketCode(head.getMarketCode());
            for (TNwfmDeviceBookRow r : landedRows(head.getBookNo(), head.getSeqNo().intValue())) {
                v.getRows().add(toRowView(r, head));
            }
            // 解析门口当场退回的行也列上（没有行次、parsePass=0），退在每一条上都问得出
            for (TNwfmDeviceBookRow r : parseRejectedRows(head.getBookNo(), head.getSeqNo().intValue())) {
                v.getRows().add(toRowView(r, head));
            }
        }
        return v;
    }

    private DeviceBookSplitView toSplitView(DeviceBookReceiptView v) {
        DeviceBookSplitView s = new DeviceBookSplitView();
        s.setBookNo(v.getBookNo());
        s.setSeqNo(v.getSeqNo());
        s.setInRows(v.getInRows());
        s.setAcceptedRows(v.getAcceptedRows());
        s.setRejectedRows(v.getRejectedRows());
        return s;
    }

    private DeviceBookRowView toRowView(TNwfmDeviceBookRow r, TNwfmDeviceBook head) {
        DeviceBookRowView v = new DeviceBookRowView();
        v.setBookNo(r.getBookNo());
        v.setSeqNo(r.getSeqNo());
        v.setRowNo(r.getRowNo());
        v.setStallName(r.getStallName());
        v.setDeviceKind(r.getDeviceKind());
        v.setDeclaredQty(r.getDeclaredQty());
        v.setMatchedQty(r.getMatchedQty());
        v.setDirection(r.getDirection());
        v.setDirectionText(directionText(r.getDirection()));
        v.setParsePass(r.getParsePass());
        v.setHandleFlag(r.getHandleFlag());
        v.setSourceType(r.getSourceType());
        v.setRejectReason(r.getRejectReason());
        if (head != null) {
            // 倒查：这一行落在哪一档，跟册头三笔算出来的数对不对得上；合不上按行点名
            List<TNwfmDeviceBookRow> all = landedRows(head.getBookNo(), head.getSeqNo().intValue());
            int aggAcc = 0;
            int aggRej = 0;
            for (TNwfmDeviceBookRow x : all) {
                int d = x.getDirection() == null ? DIR_PENDING : x.getDirection().intValue();
                if (d == DIR_ACCEPTED) {
                    aggAcc++;
                } else if (d == DIR_REJECTED) {
                    aggRej++;
                }
            }
            v.setHeaderInRows(head.getReceivedRows());
            v.setHeaderAcceptedRows(head.getAcceptedRows());
            v.setHeaderRejectedRows(head.getRejectedRows());
            boolean match = nz(head.getReceivedRows()) == all.size()
                    && nz(head.getAcceptedRows()) == aggAcc
                    && nz(head.getRejectedRows()) == aggRej;
            v.setHeaderBalanced(Boolean.valueOf(match));
        }
        return v;
    }

    // ---------------------------------------------------------------------
    // 小工具
    // ---------------------------------------------------------------------

    private int resolveDeclared(Integer declaredRows) {
        if (declaredRows == null || declaredRows.intValue() <= 0) {
            return DEFAULT_DECLARED_ROWS;
        }
        return declaredRows.intValue();
    }

    /**
     * 名目照册头定下的校验规则核：册头空着按没写——没有「什么都行」那一格；
     * 行名目空着同样按没写。两边都写了才逐字比对。
     */
    private boolean kindMatches(String lineKind, String headerKind) {
        if (headerKind == null || headerKind.isEmpty()) {
            return false;
        }
        if (lineKind == null || lineKind.isEmpty()) {
            return false;
        }
        return headerKind.equals(lineKind);
    }

    private String identityKey(String stallName, String kind) {
        return trim(stallName) + "||" + (kind == null ? "" : kind);
    }

    /** 再报的两样数跟册上原行一致即视为照面；任一样不同才算修正、在原行次重判 */
    private boolean sameNumbers(DeviceBookLine line, TNwfmDeviceBookRow row) {
        Integer d = line.getDeclaredQty();
        Integer m = line.getMatchedQty();
        if (d == null || m == null) {
            return false;
        }
        return d.equals(row.getDeclaredQty()) && m.equals(row.getMatchedQty());
    }

    private String parseReason(int pos, String kind, String headerKind) {
        if (kind == null || kind.isEmpty()) {
            return "第" + pos + "行未写器具名目，按没写核，当场退回不进门";
        }
        return "第" + pos + "行器具名目「" + kind + "」与册头「"
                + (headerKind == null ? "" : headerKind) + "」对不上，当场退回不进门";
    }

    private String mismatchReason(int rowNo, int declared, int matched) {
        int diff = declared - matched;
        return "第" + rowNo + "行报" + declared + "具、对上" + matched + "具，差" + diff + "具，退回";
    }

    private String directionText(Integer direction) {
        if (direction == null) {
            return "待定";
        }
        switch (direction.intValue()) {
            case DIR_ACCEPTED:
                return "收下";
            case DIR_REJECTED:
                return "退回";
            default:
                return "待定";
        }
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }

    private int nz(Integer i) {
        return i == null ? 0 : i.intValue();
    }

    /** 本回三笔：三个数同出一趟，inRows 恒等于收下＋退回（缺一样不办的行不进这笔账） */
    private static class RoundResult {
        private int inRows;
        private int acceptedRows;
        private int rejectedRows;
        private boolean touched;
    }
}
