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
 * 器具册整串回执对象 t_nwfm_device_book_receipt
 *
 * @author fuce
 * @date 2026-10-07
 */
@TableName("t_nwfm_device_book_receipt")
@ApiModel(value = "TNwfmDeviceBookReceipt", description = "器具册整串回执")
public class TNwfmDeviceBookReceipt implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 册头号 */
    @TableField("book_no")
    @ApiModelProperty(value = "册头号")
    private String bookNo;

    /** 分串序号 */
    @TableField("seq_no")
    @ApiModelProperty(value = "分串序号")
    private Integer seqNo;

    /** 本来手（两个来手各留一张回执） */
    @TableField("source_type")
    @ApiModelProperty(value = "本来手")
    private Integer sourceType;

    /** 同一来手第几回（同一回重交只照面，仍回头一张） */
    @TableField("round_no")
    @ApiModelProperty(value = "同一来手第几回")
    private Integer roundNo;

    /** 进门笔数 */
    @TableField("in_rows")
    @ApiModelProperty(value = "进门笔数")
    private Integer inRows;

    /** 收下笔数 */
    @TableField("accepted_rows")
    @ApiModelProperty(value = "收下笔数")
    private Integer acceptedRows;

    /** 退回笔数 */
    @TableField("rejected_rows")
    @ApiModelProperty(value = "退回笔数")
    private Integer rejectedRows;

    /** 空册字样 0否 1是（空册只落两个字，不出收讫） */
    @TableField("empty_flag")
    @ApiModelProperty(value = "空册字样")
    private Integer emptyFlag;

    /** 收讫字样 0不出 1出 */
    @TableField("accepted_flag")
    @ApiModelProperty(value = "收讫字样")
    private Integer acceptedFlag;

    /** 本回是否翻账 0只照面 1新办（第二回不回未核、不再落一遍） */
    @TableField("touched_flag")
    @ApiModelProperty(value = "本回是否翻账")
    private Integer touchedFlag;

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

    public Integer getInRows() {
        return inRows;
    }

    public void setInRows(Integer inRows) {
        this.inRows = inRows;
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

    public Integer getEmptyFlag() {
        return emptyFlag;
    }

    public void setEmptyFlag(Integer emptyFlag) {
        this.emptyFlag = emptyFlag;
    }

    public Integer getAcceptedFlag() {
        return acceptedFlag;
    }

    public void setAcceptedFlag(Integer acceptedFlag) {
        this.acceptedFlag = acceptedFlag;
    }

    public Integer getTouchedFlag() {
        return touchedFlag;
    }

    public void setTouchedFlag(Integer touchedFlag) {
        this.touchedFlag = touchedFlag;
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
