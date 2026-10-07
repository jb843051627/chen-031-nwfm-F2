package com.fc.v2.model.custom.nwfm;

import java.io.Serializable;

/**
 * 逐行倒查视图：一行落在哪一档，跟回执三笔算出来的数能不能对得上，点哪行答哪行。
 *
 * @author fuce
 * @date 2026-10-07
 */
public class DeviceBookRowView implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 册头号 */
    private String bookNo;

    /** 分串序号 */
    private Integer seqNo;

    /** 本串系统排定行次（退回再报还是这个号） */
    private Integer rowNo;

    /** 摊户名称 */
    private String stallName;

    /** 器具名目 */
    private String deviceKind;

    /** 报来多少具 */
    private Integer declaredQty;

    /** 对得上多少具（认册上回来的数） */
    private Integer matchedQty;

    /** 逐行去向 0进门待定 1收下 2退回 */
    private Integer direction;

    /** 去向字样 */
    private String directionText;

    /** 解析是否过（没过的行不落到册上） */
    private Integer parsePass;

    /** 本回是否已办 */
    private Integer handleFlag;

    /** 最初收下这一行的来手 */
    private Integer sourceType;

    /** 退回原因（点到行，如「差两具」） */
    private String rejectReason;

    /** 倒查时册头记的进门／收下／退回三笔（应与逐行加出来的数一致） */
    private Integer headerInRows;
    private Integer headerAcceptedRows;
    private Integer headerRejectedRows;

    /** 这一档逐行加总与册头三笔对不对得上（合不上按行点名） */
    private Boolean headerBalanced;

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

    public Integer getRowNo() {
        return rowNo;
    }

    public void setRowNo(Integer rowNo) {
        this.rowNo = rowNo;
    }

    public String getStallName() {
        return stallName;
    }

    public void setStallName(String stallName) {
        this.stallName = stallName;
    }

    public String getDeviceKind() {
        return deviceKind;
    }

    public void setDeviceKind(String deviceKind) {
        this.deviceKind = deviceKind;
    }

    public Integer getDeclaredQty() {
        return declaredQty;
    }

    public void setDeclaredQty(Integer declaredQty) {
        this.declaredQty = declaredQty;
    }

    public Integer getMatchedQty() {
        return matchedQty;
    }

    public void setMatchedQty(Integer matchedQty) {
        this.matchedQty = matchedQty;
    }

    public Integer getDirection() {
        return direction;
    }

    public void setDirection(Integer direction) {
        this.direction = direction;
    }

    public String getDirectionText() {
        return directionText;
    }

    public void setDirectionText(String directionText) {
        this.directionText = directionText;
    }

    public Integer getParsePass() {
        return parsePass;
    }

    public void setParsePass(Integer parsePass) {
        this.parsePass = parsePass;
    }

    public Integer getHandleFlag() {
        return handleFlag;
    }

    public void setHandleFlag(Integer handleFlag) {
        this.handleFlag = handleFlag;
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public Integer getHeaderInRows() {
        return headerInRows;
    }

    public void setHeaderInRows(Integer headerInRows) {
        this.headerInRows = headerInRows;
    }

    public Integer getHeaderAcceptedRows() {
        return headerAcceptedRows;
    }

    public void setHeaderAcceptedRows(Integer headerAcceptedRows) {
        this.headerAcceptedRows = headerAcceptedRows;
    }

    public Integer getHeaderRejectedRows() {
        return headerRejectedRows;
    }

    public void setHeaderRejectedRows(Integer headerRejectedRows) {
        this.headerRejectedRows = headerRejectedRows;
    }

    public Boolean getHeaderBalanced() {
        return headerBalanced;
    }

    public void setHeaderBalanced(Boolean headerBalanced) {
        this.headerBalanced = headerBalanced;
    }
}
