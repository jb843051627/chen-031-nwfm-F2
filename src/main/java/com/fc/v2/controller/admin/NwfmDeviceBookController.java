package com.fc.v2.controller.admin;

import java.util.List;

import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.custom.nwfm.DeviceBookForm;
import com.fc.v2.model.custom.nwfm.DeviceBookReceiptView;
import com.fc.v2.model.custom.nwfm.DeviceBookRowView;
import com.fc.v2.service.ITNwfmDeviceBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 器具册整串 Controller——交一串只进这一个口子。
 *
 * <p>几行几具这一趟走服务层；行次与实收几具认册上回来的数；分串上限写在册头那一格。
 * 查回执、逐行倒查都是只读旁口，不收件。</p>
 *
 * @author fuce
 * @date 2026-10-07
 */
@Api(value = "器具册整串")
@Controller
@RequestMapping("/NwfmDeviceBookController")
public class NwfmDeviceBookController extends BaseController {

    @Autowired
    private ITNwfmDeviceBookService nwfmDeviceBookService;

    @Log(title = "器具册整串收件", action = "receive")
    @ApiOperation(value = "收一串", notes = "器具册整串唯一收件口")
    @PostMapping("/submit")
    @RequiresPermissions("nwfm:deviceBook:submit")
    @ResponseBody
    public AjaxResult submit(@RequestBody DeviceBookForm form) {
        try {
            DeviceBookReceiptView receipt = nwfmDeviceBookService.receiveBook(form);
            AjaxResult ok = AjaxResult.success("收串完成");
            ok.put("data", receipt);
            return ok;
        } catch (IllegalArgumentException e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @ApiOperation(value = "回执查询", notes = "某册头号下所有串、两个来手的全部回执")
    @GetMapping("/receipts")
    @RequiresPermissions("nwfm:deviceBook:view")
    @ResponseBody
    public AjaxResult receipts(@RequestParam("bookNo") String bookNo) {
        List<DeviceBookReceiptView> list = nwfmDeviceBookService.listReceipts(bookNo);
        AjaxResult ok = AjaxResult.success();
        ok.put("data", list);
        return ok;
    }

    @ApiOperation(value = "逐行查询", notes = "本串逐行定去向，按行次升序，存量行一条不减")
    @GetMapping("/rows")
    @RequiresPermissions("nwfm:deviceBook:view")
    @ResponseBody
    public AjaxResult rows(@RequestParam("bookNo") String bookNo,
                           @RequestParam("seqNo") Integer seqNo) {
        List<DeviceBookRowView> rows = nwfmDeviceBookService.listRows(bookNo, seqNo);
        AjaxResult ok = AjaxResult.success();
        ok.put("data", rows);
        return ok;
    }

    @ApiOperation(value = "单行倒查", notes = "点一行倒着查落在哪一档；册上没有这一行不认")
    @GetMapping("/trace")
    @RequiresPermissions("nwfm:deviceBook:view")
    @ResponseBody
    public AjaxResult trace(@RequestParam("bookNo") String bookNo,
                            @RequestParam("seqNo") Integer seqNo,
                            @RequestParam("rowNo") Integer rowNo) {
        DeviceBookRowView row = nwfmDeviceBookService.traceRow(bookNo, seqNo, rowNo);
        AjaxResult ok = AjaxResult.success();
        ok.put("data", row);
        return ok;
    }
}
