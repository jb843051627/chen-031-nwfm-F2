package com.fc.v2.model.custom.nwfm;

import java.io.Serializable;

/**
 * 器具册整串里的一行：咬定一个摊户的一批秤。
 *
 * <p>没有行次可填——行次由系统排定。declaredQty/matchedQty 缺一样这一行不办，
 * 系统不代填、不按零认下。</p>
 *
 * @author fuce
 * @date 2026-10-07
 */
public class DeviceBookLine implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 摊户名称（续办时据此认出当初那一行，保住原行次） */
    private String stallName;

    /** 器具名目（空着按没写核，解析当场退回） */
    private String deviceKind;

    /** 这批秤报来多少具 */
    private Integer declaredQty;

    /** 册上对得上多少具（认册上回来的数） */
    private Integer matchedQty;

    public DeviceBookLine() {
    }

    public DeviceBookLine(String stallName, String deviceKind, Integer declaredQty, Integer matchedQty) {
        this.stallName = stallName;
        this.deviceKind = deviceKind;
        this.declaredQty = declaredQty;
        this.matchedQty = matchedQty;
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
}
