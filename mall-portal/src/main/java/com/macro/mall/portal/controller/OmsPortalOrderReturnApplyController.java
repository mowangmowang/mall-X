package com.macro.mall.portal.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.portal.domain.OmsOrderReturnApplyParam;
import com.macro.mall.portal.service.OmsPortalOrderReturnApplyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 退货申请管理Controller */
@RestController
@Api(tags = "OmsPortalOrderReturnApplyController")
@Tag(name = "OmsPortalOrderReturnApplyController",description = "退货申请管理")
@RequestMapping("/returnApply")
public class OmsPortalOrderReturnApplyController {
    @Autowired
    private OmsPortalOrderReturnApplyService returnApplyService;

    @ApiOperation("申请退货")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public CommonResult create(@RequestBody OmsOrderReturnApplyParam returnApply) {
        int count = returnApplyService.create(returnApply);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @ApiOperation("查询退货申请")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<java.util.List<com.macro.mall.model.OmsOrderReturnApply>> list() {
        java.util.List<com.macro.mall.model.OmsOrderReturnApply> list = returnApplyService.list();
        return CommonResult.success(list);
    }

    @ApiOperation("取消退货申请")
    @RequestMapping(value = "/cancel/{id}", method = RequestMethod.POST)
    public CommonResult cancel(@PathVariable Long id) {
        int count = returnApplyService.cancel(id);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
}
