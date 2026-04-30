package com.macro.mall.controller;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.dto.*;
import com.macro.mall.model.OmsOrder;
import com.macro.mall.service.OmsOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单管理 Controller
 * 提供订单查询、发货、关闭、删除、修改等功能
 */
@Controller
@Api(tags = "OmsOrderController")
@Tag(name = "OmsOrderController", description = "订单管理")
@RequestMapping("/order")
public class OmsOrderController {
    /**
     * 订单服务
     */
    @Autowired
    private OmsOrderService orderService;

    /**
     * 分页查询订单列表
     * 支持按多种条件筛选（订单号、状态、时间等）
     * @param queryParam 查询参数
     * @param pageSize 每页条数，默认5条
     * @param pageNum 页码，默认第1页
     * @return 分页订单列表
     */
    @ApiOperation("查询订单")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<OmsOrder>> list(OmsOrderQueryParam queryParam,
                                                   @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                   @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        List<OmsOrder> orderList = orderService.list(queryParam, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(orderList));
    }

    /**
     * 批量发货
     * 更新订单的物流信息并修改状态为已发货
     * @param deliveryParamList 发货参数列表（包含订单ID、物流公司、物流单号）
     * @return 操作结果
     */
    @ApiOperation("批量发货")
    @RequestMapping(value = "/update/delivery", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult delivery(@RequestBody List<OmsOrderDeliveryParam> deliveryParamList) {
        int count = orderService.delivery(deliveryParamList);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    /**
     * 批量关闭订单
     * 通常用于超时未支付的订单
     * @param ids 订单 ID 列表
     * @param note 关闭原因/备注
     * @return 操作结果
     */
    @ApiOperation("批量关闭订单")
    @RequestMapping(value = "/update/close", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult close(@RequestParam("ids") List<Long> ids, @RequestParam String note) {
        int count = orderService.close(ids, note);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    /**
     * 批量删除订单
     * 逻辑删除，仅删除已完成或已关闭的订单
     * @param ids 订单 ID 列表
     * @return 操作结果
     */
    @ApiOperation("批量删除订单")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult delete(@RequestParam("ids") List<Long> ids) {
        int count = orderService.delete(ids);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    /**
     * 获取订单详细信息
     * 包括订单基本信息、商品列表、操作记录等
     * @param id 订单 ID
     * @return 订单详情
     */
    @ApiOperation("获取订单详情：订单信息、商品信息、操作记录")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<OmsOrderDetail> detail(@PathVariable Long id) {
        OmsOrderDetail orderDetailResult = orderService.detail(id);
        return CommonResult.success(orderDetailResult);
    }

    /**
     * 修改订单收货人信息
     * @param receiverInfoParam 收货人信息参数
     * @return 操作结果
     */
    @ApiOperation("修改收货人信息")
    @RequestMapping(value = "/update/receiverInfo", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateReceiverInfo(@RequestBody OmsReceiverInfoParam receiverInfoParam) {
        int count = orderService.updateReceiverInfo(receiverInfoParam);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    /**
     * 修改订单费用信息
     * 如运费、优惠金额等
     * @param moneyInfoParam 费用信息参数
     * @return 操作结果
     */
    @ApiOperation("修改订单费用信息")
    @RequestMapping(value = "/update/moneyInfo", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateReceiverInfo(@RequestBody OmsMoneyInfoParam moneyInfoParam) {
        int count = orderService.updateMoneyInfo(moneyInfoParam);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    /**
     * 添加订单备注
     * @param id 订单 ID
     * @param note 备注内容
     * @param status 当前订单状态
     * @return 操作结果
     */
    @ApiOperation("备注订单")
    @RequestMapping(value = "/update/note", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateNote(@RequestParam("id") Long id,
                                   @RequestParam("note") String note,
                                   @RequestParam("status") Integer status) {
        int count = orderService.updateNote(id, note, status);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    /**
     * 取消订单
     * 通常由管理员手动取消
     * @param id 订单 ID
     * @param note 取消原因
     * @return 操作结果
     */
    @ApiOperation("取消订单")
    @RequestMapping(value = "/update/cancel", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult cancel(@RequestParam("id") Long id, @RequestParam("note") String note) {
        int count = orderService.cancel(id, note);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
}
