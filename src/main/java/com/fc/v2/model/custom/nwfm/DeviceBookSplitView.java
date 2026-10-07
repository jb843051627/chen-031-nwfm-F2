package com.fc.v2.model.custom.nwfm;

import java.io.Serializable;

/**
 * 多出来的行不能就地续在后头，另立的下一串回执（只带定位与三笔，行明细仍按册头号查）。
 *
 * @author fuce
 * @date 2026-10-07
 */
public class DeviceBookSplitView implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 新串册头号（系统另立） */
    private String bookNo;

    /** 新串分串序号 */
    private Integer seqNo;

    /** 新串进门笔数 */
    private int inRows;

    /** 新串收下笔数 */
    private int acceptedRows;

    /** 新串退回笔数 */
    private int rejectedRows;

    public String getBookNo() {
        return bookNo;
    }

    public void setBookNo(String bookNo) {
        this.bookNo = bookNo;
    }

    public Integer getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(Integer seqNo) {
        this.seqNo = seqNo;
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
}
