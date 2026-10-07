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
 * 器具册整串册行对象 t_nwfm_device_book_row
 *
 * @author fuce
 * @date 2026-10-07
 */
@TableName("t_nwfm_device_book_row")
@ApiModel(value = "TNwfmDeviceBookRow", description = "器具册整串册行")
public class TNwfmDeviceBookRow implements Serializable {
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

    /** 本串系统排定行次（1起，落定后不再变） */
    @TableField("row_no")
    @ApiModelProperty(value = "本串系统排定行次")
    private Integer rowNo;

    /** 摊户名称 */
    @TableField("stall_name")
    @ApiModelProperty(value = "摊户名称")
    private String stallName;

    /** 器具名目（空名目按未写核，不放行） */
    @TableField("device_kind")
    @ApiModelProperty(value = "器具名目")
    private String deviceKind;

    /** 这批秤报来多少具 */
    @TableField("declared_qty")
    @ApiModelProperty(value = "报来多少具")
    private Integer declaredQty;

    /** 册上对得上多少具 */
    @TableField("matched_qty")
    @ApiModelProperty(value = "对得上多少具")
    private Integer matchedQty;

    /** 逐行去向 1收下 2退回 0进门待定 */
    @TableField("direction")
    @ApiModelProperty(value = "逐行去向 1收下 2退回 0进门待定")
    private Integer direction;

    /** 退回原因（点到行，如「差两具」） */
    @TableField("reject_reason")
    @ApiModelProperty(value = "退回原因")
    private String rejectReason;

    /** 解析是否过 1过 0当场退回不进门 */
    @TableField("parse_pass")
    @ApiModelProperty(value = "解析是否过")
    private Integer parsePass;

    /** 本回是否已办 0未办 1已办（续办只补未办，办好不惊动） */
    @TableField("handle_flag")
    @ApiModelProperty(value = "本回是否已办")
    private Integer handleFlag;

    /** 最初收下这一行的来手（单行退回不飘串） */
    @TableField("source_type")
    @ApiModelProperty(value = "最初收下这一行的来手")
    private Integer sourceType;

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

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
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
