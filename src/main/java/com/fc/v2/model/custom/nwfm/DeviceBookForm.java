package com.fc.v2.model.custom.nwfm;

import java.io.Serializable;
import java.util.List;

/**
 * 器具册整串提交入参——唯一入口的入参形状。
 *
 * <p>入参里没有总数、没有行次：总数由「逐行定去向」那一趟现算，行次由系统在本串里排。
 * 册头那一格的申报行数就是分串上限，不写按 150。</p>
 *
 * @author fuce
 * @date 2026-10-07
 */
public class DeviceBookForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 册头号（同一市场同一季的一串；溢出分串由系统另立串号） */
    private String bookNo;

    /** 市场代号 */
    private String marketCode;

    /** 册头那一处写着的器具名目（解析照它逐行核；空着不按「什么都行」放行） */
    private String headerKind;

    /** 册头申报行数＝分串上限；null 按 150，落定后中间不许加行 */
    private Integer declaredRows;

    /** 来手 0市场自录 1区里代收 */
    private Integer sourceType;

    /** 一串里的若干行（顺序即册面顺序） */
    private List<DeviceBookLine> lines;

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

    public Integer getDeclaredRows() {
        return declaredRows;
    }

    public void setDeclaredRows(Integer declaredRows) {
        this.declaredRows = declaredRows;
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public void setSourceType(Integer sourceType) {
        this.sourceType = sourceType;
    }

    public List<DeviceBookLine> getLines() {
        return lines;
    }

    public void setLines(List<DeviceBookLine> lines) {
        this.lines = lines;
    }
}
