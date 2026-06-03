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
 * 退货申请管理控制器 (Order Return Apply Controller)
 * 提供退货申请创建、查询、取消等功能，处理用户售后请求
 */
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
        // 创建退货申请记录，等待后台审核
        int count = returnApplyService.create(returnApply);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @ApiOperation("查询退货申请")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<java.util.List<com.macro.mall.model.OmsOrderReturnApply>> list() {
        // 查询当前用户的所有退货申请列表
        java.util.List<com.macro.mall.model.OmsOrderReturnApply> list = returnApplyService.list();
        return CommonResult.success(list);
    }

    @ApiOperation("获取退货申请详情")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public CommonResult<com.macro.mall.model.OmsOrderReturnApply> getDetail(@PathVariable Long id) {
        // 获取指定退货申请的详情
        com.macro.mall.model.OmsOrderReturnApply apply = returnApplyService.getDetail(id);
        if (apply != null) {
            return CommonResult.success(apply);
        }
        return CommonResult.failed("售后申请不存在");
    }

    @ApiOperation("取消退货申请")
    @RequestMapping(value = "/cancel/{id}", method = RequestMethod.POST)
    public CommonResult cancel(@PathVariable Long id) {
        // 取消待审核的退货申请
        int count = returnApplyService.cancel(id);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
}
