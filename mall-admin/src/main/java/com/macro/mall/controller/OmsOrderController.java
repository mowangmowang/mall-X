package com.macro.mall.controller;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.dto.*;
import com.macro.mall.model.OmsOrder;
import com.macro.mall.service.OmsOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单管理 Controller
 * 提供订单查询、发货、关闭、删除、修改及备注等核心业务功能。
 */
@Controller
@Tag(name = "OmsOrderController", description = "订单管理")
@RequestMapping("/order")
public class OmsOrderController {
    /**
     * 订单服务接口实例
     */
    @Autowired
    private OmsOrderService orderService;

    /**
     * 分页查询订单列表
     * 支持按订单号、状态、时间范围等多种条件进行动态筛选。
     *
     * @param queryParam 查询参数封装对象
     * @param pageSize 每页显示条数，默认 5 条
     * @param pageNum 当前页码，默认第 1 页
     * @return 分页后的订单列表数据
     */
    @Operation(summary = "查询订单列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<OmsOrder>> list(OmsOrderQueryParam queryParam,
                                                   @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                   @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        List<OmsOrder> orderList = orderService.list(queryParam, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(orderList));
    }

    /**
     * 批量处理订单发货
     * 更新订单物流信息（物流公司、单号）并将状态流转为"已发货"。
     *
     * @param deliveryParamList 发货参数集合，包含订单 ID 及物流详情
     * @return 操作结果
     */
    @Operation(summary = "批量发货")
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
     * 批量关闭交易订单
     * 通常用于处理超时未支付或异常状态的订单。
     *
     * @param ids 待关闭的订单 ID 集合
     * @param note 关闭原因或管理员备注
     * @return 操作结果
     */
    @Operation(summary = "批量关闭订单")
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
     * 批量逻辑删除订单
     * 仅允许删除已完成或已关闭状态的订单记录。
     *
     * @param ids 待删除的订单 ID 集合
     * @return 操作结果
     */
    @Operation(summary = "批量删除订单")
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
     * 获取订单完整详细信息
     * 包含订单基础信息、购买商品清单、历史操作记录及收货人详情。
     *
     * @param id 目标订单 ID
     * @return 订单详情对象
     */
    @Operation(summary = "获取订单详情")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<OmsOrderDetail> detail(@PathVariable Long id) {
        OmsOrderDetail orderDetailResult = orderService.detail(id);
        return CommonResult.success(orderDetailResult);
    }

    /**
     * 修改订单收货人信息
     * 支持在发货前修改地址、联系人等信息。
     *
     * @param receiverInfoParam 收货人更新参数
     * @return 操作结果
     */
    @Operation(summary = "修改收货人信息")
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
     * 调整订单费用信息
     * 用于修改运费、优惠金额或应付总额等财务相关字段。
     *
     * @param moneyInfoParam 费用调整参数
     * @return 操作结果
     */
    @Operation(summary = "修改订单费用信息")
    @RequestMapping(value = "/update/moneyInfo", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateMoneyInfo(@RequestBody OmsMoneyInfoParam moneyInfoParam) {
        int count = orderService.updateMoneyInfo(moneyInfoParam);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    /**
     * 为订单添加或更新管理员备注
     *
     * @param id 订单 ID
     * @param note 备注内容
     * @param status 当前订单状态
     * @return 操作结果
     */
    @Operation(summary = "备注订单")
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
     * 手动取消订单
     * 由管理员介入取消处于特定状态的订单。
     *
     * @param id 订单 ID
     * @param note 取消原因说明
     * @return 操作结果
     */
    @Operation(summary = "取消订单")
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
