package com.fc.v2.service;

import java.util.List;

import com.fc.v2.model.custom.nwfm.DeviceBookForm;
import com.fc.v2.model.custom.nwfm.DeviceBookReceiptView;
import com.fc.v2.model.custom.nwfm.DeviceBookRowView;

/**
 * 器具册整串 Service（intake-receipt 形状：一个市场一次交一整串，连着办不等人手）。
 *
 * <p>一次提交只进「器具册整串」这一个口子；几行几具这一趟走服务层；行次与实收几具
 * 认册上回来的数；分串上限写在册头那一格。老路子（按市场／按种类分串）另立并列
 * 入口时不改本接口的入参与返回。</p>
 *
 * @author fuce
 * @date 2026-10-07
 */
public interface ITNwfmDeviceBookService {

    /**
     * 收一串：册头定分串上限（缺省 150），溢出另立下一串；
     * 解析只跑一遍，名目不合当场退回不落册；逐行定去向（收下/退回），
     * 退回原因点到行；同一来手第二回只照面不翻账；空册只落「空册」不出「收讫」。
     * 回执三笔（进门/收下/退回）取自这一趟，后两笔之和等于头一笔。
     *
     * @return 本串（正本）回执；溢出新串挂在回执的 splits 上，各带各自三笔
     */
    DeviceBookReceiptView receiveBook(DeviceBookForm form);

    /** 某册头号下所有串的全部回执（两个来手各留各的，按分串序号、来手、回次升序） */
    List<DeviceBookReceiptView> listReceipts(String bookNo);

    /** 本串逐行去向（按行次升序；往年入册的存量行一条不减） */
    List<DeviceBookRowView> listRows(String bookNo, Integer seqNo);

    /** 点一行倒着查：这一行落在哪一档；册上压根没有这一行返回 null（不认） */
    DeviceBookRowView traceRow(String bookNo, Integer seqNo, Integer rowNo);
}
