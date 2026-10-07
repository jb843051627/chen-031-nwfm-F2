package com.fc.v2.model.custom.nwfm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 器具册整串回执：末尾三笔（进门／收下／退回），后两笔加起来等于头一笔。
 *
 * <p>三个数都取自「逐行定去向」那一趟的返回，不另跑一趟，也不看页面抢先亮出来的数。
 * 空串是正经输入：只落「空册」，不出「收讫」。同一来手第二回到只照面——
 * touchedFlag=0、三笔全 0、头一遍办好的行不回未核、本串不再落第二遍。</p>
 *
 * @author fuce
 * @date 2026-10-07
 */
public class DeviceBookReceiptView implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 册头号 */
    private String bookNo;

    /** 市场代号 */
    private String marketCode;

    /** 分串序号（0正本，溢出往后排） */
    private Integer seqNo;

    /** 本来手 0市场自录 1区里代收 */
    private Integer sourceType;

    /** 同一来手第几回 */
    private Integer roundNo;

    /** 进门笔数 */
    private int inRows;

    /** 收下笔数 */
    private int acceptedRows;

    /** 退回笔数 */
    private int rejectedRows;

    /** 三笔是否守恒（收下＋退回＝进门） */
    private boolean balanced;

    /** 空册字样 */
    private boolean empty;

    /** 收讫字样（空册不出） */
    private boolean accepted;

    /** 本回是否翻账（false＝第二回只照面） */
    private boolean touched;

    /** 本串逐行去向（点行倒查用，按行次升序） */
    private List<DeviceBookRowView> rows = new ArrayList<DeviceBookRowView>();

    /** 交一串溢出时，另立出去的下一串（不就地续在本串后头） */
    private List<DeviceBookSplitView> splits = new ArrayList<DeviceBookSplitView>();

    public String getBookNo() {
        return bookNo;
    }

    public void setBookNo(String bookNo) {
        this.bookNo = bookNo;
    }

    public String getMarketCode() {
        return marketCode;
    }

    public void setMarketCode(String marketCode) {
        this.marketCode = marketCode;
    }

    public Integer getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(Integer seqNo) {
        this.seqNo = seqNo;
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
    }

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
    }

    public int getInRows() {
        return inRows;
    }

    public void setInRows(int inRows) {
        this.inRows = inRows;
    }

    public int getAcceptedRows() {
        return acceptedRows;
    }

    public void setAcceptedRows(int acceptedRows) {
        this.acceptedRows = acceptedRows;
    }

    public int getRejectedRows() {
        return rejectedRows;
    }

    public void setRejectedRows(int rejectedRows) {
        this.rejectedRows = rejectedRows;
    }

    public boolean isBalanced() {
        return balanced;
    }

    public void setBalanced(boolean balanced) {
        this.balanced = balanced;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isTouched() {
        return touched;
    }

    public void setTouched(boolean touched) {
        this.touched = touched;
    }

    public List<DeviceBookRowView> getRows() {
        return rows;
    }

    public void setRows(List<DeviceBookRowView> rows) {
        this.rows = rows;
    }

    public List<DeviceBookSplitView> getSplits() {
        return splits;
    }

    public void setSplits(List<DeviceBookSplitView> splits) {
        this.splits = splits;
    }
}
