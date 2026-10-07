package com.fc.v2.model.auto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * 器具册整串册头对象 t_nwfm_device_book
 *
 * @author fuce
 * @date 2026-10-07
 */
@TableName("t_nwfm_device_book")
@ApiModel(value = "TNwfmDeviceBook", description = "器具册整串册头")
public class TNwfmDeviceBook implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 册头号（同一市场同一季的一串） */
    @TableField("book_no")
    @ApiModelProperty(value = "册头号")
    private String bookNo;

    /** 市场代号 */
    @TableField("market_code")
    @ApiModelProperty(value = "市场代号")
    private String marketCode;

    /** 册头写着的器具名目（续办仍照它核；空着按没写，不放行） */
    @TableField("header_kind")
    @ApiModelProperty(value = "册头写着的器具名目")
    private String headerKind;

    /** 来手 0市场自录 1区里代收（两个来手各走一回） */
    @TableField("source_type")
    @ApiModelProperty(value = "来手 0市场自录 1区里代收")
    private Integer sourceType;

    /** 本串分串序号（0正本 1起溢出新串） */
    @TableField("seq_no")
    @ApiModelProperty(value = "本串分串序号")
    private Integer seqNo;

    /** 册头申报行数（缺省150，落库后不再变） */
    @TableField("declared_rows")
    @ApiModelProperty(value = "册头申报行数")
    private Integer declaredRows;

    /** 本串实际进门行数 */
    @TableField("received_rows")
    @ApiModelProperty(value = "本串实际进门行数")
    private Integer receivedRows;

    /** 收下行数 */
    @TableField("accepted_rows")
    @ApiModelProperty(value = "收下行数")
    private Integer acceptedRows;

    /** 退回行数 */
    @TableField("rejected_rows")
    @ApiModelProperty(value = "退回行数")
    private Integer rejectedRows;

    /** 串状态 0收串中 1收讫 2空册 9异常 */
    @TableField("status")
    @ApiModelProperty(value = "串状态 0收串中 1收讫 2空册 9异常")
    private Integer status;

    /** 收讫标记 0未收讫 1已收讫（空册不出收讫） */
    @TableField("closed_flag")
    @ApiModelProperty(value = "收讫标记 0未收讫 1已收讫")
    private Integer closedFlag;

    /** 最近一回的来手（第二回只照面） */
    @TableField("last_source_type")
    @ApiModelProperty(value = "最近一回的来手")
    private Integer lastSourceType;

    /** 删除标记 0正常 1删除 */
    @TableField("del_flag")
    @ApiModelProperty(value = "删除标记 0正常 1删除")
    private Integer delFlag;

    /** 创建者 */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建者")
    private String createBy;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /** 更新者 */
    @TableField(value = "update_by", fill = FieldFill.UPDATE)
    @ApiModelProperty(value = "更新者")
    private String updateBy;

    /** 更新时间 */
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    /** 备注 */
    @TableField("remark")
    @ApiModelProperty(value = "备注")
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getHeaderKind() {
        return headerKind;
    }

    public void setHeaderKind(String headerKind) {
        this.headerKind = headerKind;
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
    }

    public Integer getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(Integer seqNo) {
        this.seqNo = seqNo;
    }

    public Integer getDeclaredRows() {
        return declaredRows;
    }

    public void setDeclaredRows(Integer declaredRows) {
        this.declaredRows = declaredRows;
    }

    public Integer getReceivedRows() {
        return receivedRows;
    }

    public void setReceivedRows(Integer receivedRows) {
        this.receivedRows = receivedRows;
    }

    public Integer getAcceptedRows() {
        return acceptedRows;
    }

    public void setAcceptedRows(Integer acceptedRows) {
        this.acceptedRows = acceptedRows;
    }

    public Integer getRejectedRows() {
        return rejectedRows;
    }

    public void setRejectedRows(Integer rejectedRows) {
        this.rejectedRows = rejectedRows;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getClosedFlag() {
        return closedFlag;
    }

    public void setClosedFlag(Integer closedFlag) {
        this.closedFlag = closedFlag;
    }

    public Integer getLastSourceType() {
        return lastSourceType;
    }

    public void setLastSourceType(Integer lastSourceType) {
        this.lastSourceType = lastSourceType;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
