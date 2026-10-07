package com.fc.v2.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.mapper.auto.TNwfmCheckRowMapper;
import com.fc.v2.mapper.auto.TNwfmDeviceRuleMapper;
import com.fc.v2.model.auto.TNwfmCheckRow;
import com.fc.v2.model.auto.TNwfmDeviceRule;
import com.fc.v2.service.ITNwfmCheckRowService;

/**
 * 周期核收条目 Service业务层处理（batch-process 形状：器具册整串提交，逐行定去向）
 *
 * <p>一串册一个口子进：册头没写行数按一百五十行收，超出的部分不能就地续在后头；
 * 进门先过一遍解析，名目合不上的行连门都进不去、不落到册上；每一行要落两样数，
 * 缺一样不办、系统不代填也不按零认下，两样对不平的退回并写明差几具；行次由系统在
 * 本串里排，重复交、续办都认当初排定的行次，办好的行只照面不翻账。</p>
 *
 * @author fuce
 * @date 2026-09-14
 */
@Service
public class TNwfmCheckRowServiceImpl implements ITNwfmCheckRowService {

    /** 分串上限：册头那一格没写时按一百五十行收 */
    private static final int MAX_ROWS = 150;
    /** 条目状态：1已核（收下，落在册上） */
    private static final int STATUS_OK = 1;
    /** 条目状态：2已处理（退回，为哪一条退写在备注里） */
    private static final int STATUS_FAIL = 2;

    @javax.annotation.Resource
    private TNwfmCheckRowMapper nwfmCheckRowMapper;

    @javax.annotation.Resource
    private TNwfmDeviceRuleMapper nwfmDeviceRuleMapper;

    @Override
    public TNwfmCheckRow selectTNwfmCheckRowById(Long id) {
        return this.nwfmCheckRowMapper.selectById(id);
    }

    @Override
    public int submitBatch(String batchNo, List<TNwfmCheckRow> rows) {
        // 空串是正经输入：回执只落「空册」两个字样，收讫那一句不出
        if (rows == null || rows.isEmpty()) {
            return 0;
        }
        // 册头没写按一百五十行：多出来的部分不能就地续在后头，得开成下一串，本串一行不收
        if (rows.size() > MAX_ROWS) {
            return -1;
        }
        if (batchNo == null || batchNo.trim().isEmpty()) {
            return 0;
        }
        // 本串已在册上的行（行次 -> 册上行）：重复交、续办都照这本账走，行不会飘到别串去
        Map<Integer, TNwfmCheckRow> onBook = new HashMap<Integer, TNwfmCheckRow>();
        List<TNwfmCheckRow> booked = this.nwfmCheckRowMapper.selectList(
                new QueryWrapper<TNwfmCheckRow>().eq("batch_no", batchNo).eq("del_flag", 0));
        for (TNwfmCheckRow b : booked) {
            if (b.getRowNo() != null) {
                onBook.put(b.getRowNo(), b);
            }
        }
        // 解析只跑这一趟：器具名目照册上定下的校验规则逐行核（在用、未删、在生效窗口内）
        List<TNwfmDeviceRule> rules = this.nwfmDeviceRuleMapper.selectList(
                new QueryWrapper<TNwfmDeviceRule>().eq("del_flag", 0).eq("status", 0));
        Date now = new Date();

        int accepted = 0;
        int seq = 0;
        for (TNwfmCheckRow row : rows) {
            seq++;
            // 行次由系统在本串里排，没有敲键的地方；隔几天再报，认的还是当初排定的行次
            TNwfmCheckRow old = onBook.get(Integer.valueOf(seq));
            if (old != null && old.getStatus() != null && old.getStatus().intValue() == STATUS_OK) {
                // 头一遍办好的行只照面不翻账：不回未核，本串也不再落一遍另立一处
                accepted++;
                continue;
            }
            // 名目合不上连门都进不去，不落到册上；空着的按没写核，不按「什么都行」放过去
            if (row == null || !usableRule(row.getItemCode(), rules, now)) {
                continue;
            }
            // 每一行要落两样，缺一样这一行不办：系统不代填，也不按零认下
            if (row.getQty() == null) {
                saveRow(old, batchNo, seq, row, STATUS_FAIL, "缺器具数，系统不代填");
                continue;
            }
            if (row.getQty().compareTo(BigDecimal.ZERO) <= 0) {
                saveRow(old, batchNo, seq, row, STATUS_FAIL, "器具数不按零认下");
                continue;
            }
            // 两样对不平的行退回：报数跟册上存量对不上，退就退在「差几具」那一句上
            BigDecimal stocked = stockedQty(row.getItemCode(), row.getStallName());
            if (stocked != null && stocked.compareTo(row.getQty()) != 0) {
                BigDecimal diff = row.getQty().subtract(stocked).abs();
                saveRow(old, batchNo, seq, row, STATUS_FAIL,
                        "报" + fmt(row.getQty()) + "具、册上" + fmt(stocked) + "具，差" + fmt(diff) + "具");
                continue;
            }
            // 收下：新行落册；退过的行续办转已核，行次不动、整串不跟着抹回去
            saveRow(old, batchNo, seq, row, STATUS_OK, old == null ? row.getRemark() : "续办收下");
            accepted++;
        }
        // 回执三笔取自这一趟：进门 rows.size()，收下 accepted，退回为两者之差
        return accepted;
    }

    @Override
    public List<TNwfmCheckRow> listErrors(String batchNo) {
        return this.nwfmCheckRowMapper.selectList(new QueryWrapper<TNwfmCheckRow>()
                .eq("batch_no", batchNo).eq("status", STATUS_FAIL).eq("del_flag", 0)
                .orderByAsc("row_no"));
    }

    /** 名目核校：空着按没写核；须合上一条在用且在生效窗口内的校验规则（截止时刻不含） */
    private boolean usableRule(String itemCode, List<TNwfmDeviceRule> rules, Date now) {
        if (itemCode == null || itemCode.trim().isEmpty()) {
            return false;
        }
        for (TNwfmDeviceRule rule : rules) {
            if (!itemCode.equals(rule.getRuleCode())) {
                continue;
            }
            if (rule.getEffStart() != null && now.before(rule.getEffStart())) {
                continue;
            }
            if (rule.getEffEnd() != null && !now.before(rule.getEffEnd())) {
                continue;
            }
            return true;
        }
        return false;
    }

    /** 册上存量：同一摊户同一名目最近一条已核行的器具数；册上没有这一行就返回 null（新立档照申报数收） */
    private BigDecimal stockedQty(String itemCode, String stallName) {
        QueryWrapper<TNwfmCheckRow> q = new QueryWrapper<TNwfmCheckRow>()
                .eq("del_flag", 0).eq("status", STATUS_OK).eq("item_code", itemCode);
        if (stallName == null) {
            q.isNull("stall_name");
        } else {
            q.eq("stall_name", stallName);
        }
        q.orderByDesc("id").last("limit 1");
        TNwfmCheckRow latest = this.nwfmCheckRowMapper.selectOne(q);
        return latest == null ? null : latest.getQty();
    }

    /** 落一行：册上已有这一行次就原行更新（不另立一处），没有就新落一行 */
    private void saveRow(TNwfmCheckRow old, String batchNo, int rowNo, TNwfmCheckRow row,
                         int status, String remark) {
        TNwfmCheckRow r = old == null ? new TNwfmCheckRow() : old;
        r.setBatchNo(batchNo);
        r.setRowNo(Integer.valueOf(rowNo));
        r.setItemCode(row.getItemCode());
        r.setQty(row.getQty());
        r.setDeviceKind(row.getDeviceKind());
        r.setStallName(row.getStallName());
        r.setStatus(Integer.valueOf(status));
        r.setRemark(remark);
        r.setDelFlag(Integer.valueOf(0));
        if (r.getId() == null) {
            this.nwfmCheckRowMapper.insert(r);
        } else {
            this.nwfmCheckRowMapper.updateById(r);
        }
    }

    /** 数量落字：去掉小数尾巴，20.00 落作 20 */
    private static String fmt(BigDecimal v) {
        return v.stripTrailingZeros().toPlainString();
    }
}
