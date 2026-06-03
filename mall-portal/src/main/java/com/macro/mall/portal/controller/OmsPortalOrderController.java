package com.macro.mall.portal.controller;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.portal.domain.ConfirmOrderResult;
import com.macro.mall.portal.domain.OmsOrderDetail;
import com.macro.mall.portal.domain.OrderParam;
import com.macro.mall.portal.service.OmsPortalOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 前台订单管理控制器 (Portal Order Management Controller)
 * 提供订单生成、支付、查询、取消等接口，供用户端调用
 */
@RestController
@Api(tags = "OmsPortalOrderController")
@Tag(name = "OmsPortalOrderController", description = "订单管理")
@RequestMapping("/order")
public class OmsPortalOrderController {
    @Autowired
    private OmsPortalOrderService portalOrderService;

    @ApiOperation("根据购物车信息生成确认单")
    @RequestMapping(value = "/generateConfirmOrder", method = RequestMethod.POST)
    public CommonResult<ConfirmOrderResult> generateConfirmOrder(@RequestBody List<Long> cartIds) {
        // 获取购物车商品、收货地址、优惠券等信息，计算优惠金额和应付金额
        ConfirmOrderResult confirmOrderResult = portalOrderService.generateConfirmOrder(cartIds);
        return CommonResult.success(confirmOrderResult);
    }

    @ApiOperation("根据购物车信息生成订单")
    @RequestMapping(value = "/generateOrder", method = RequestMethod.POST)
    public CommonResult generateOrder(@RequestBody OrderParam orderParam) {
        // 校验库存、优惠券、积分，锁定库存并创建订单
        Map<String, Object> result = portalOrderService.generateOrder(orderParam);
        return CommonResult.success(result, "下单成功");
    }

    @ApiOperation("用户支付成功的回调")
    @RequestMapping(value = "/paySuccess", method = RequestMethod.POST)
    public CommonResult paySuccess(@RequestParam Long orderId,@RequestParam Integer payType) {
        // 更新订单状态为已支付，增加会员积分和成长值
        Integer count = portalOrderService.paySuccess(orderId,payType);
        return CommonResult.success(count, "支付成功");
    }

    @ApiOperation("自动取消超时订单")
    @RequestMapping(value = "/cancelTimeOutOrder", method = RequestMethod.POST)
    public CommonResult cancelTimeOutOrder() {
        // 定时任务调用，批量取消超过指定时间未支付的订单
        portalOrderService.cancelTimeOutOrder();
        return CommonResult.success(null);
    }

    @ApiOperation("取消单个超时订单")
    @RequestMapping(value = "/cancelOrder", method = RequestMethod.POST)
    public CommonResult cancelOrder(Long orderId) {
        // 发送延迟消息到 RabbitMQ，异步取消订单并释放库存
        portalOrderService.sendDelayMessageCancelOrder(orderId);
        return CommonResult.success(null);
    }

    @ApiOperation("按状态分页获取用户订单列表")
    @ApiImplicitParam(name = "status", value = "订单状态：-1->全部；0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭",
            defaultValue = "-1", allowableValues = "-1,0,1,2,3,4", paramType = "query", dataType = "int")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<OmsOrderDetail>> list(@RequestParam Integer status,
                                                   @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                                                   @RequestParam(required = false, defaultValue = "5") Integer pageSize) {
        // 根据订单状态和分页参数查询当前用户的订单列表
        CommonPage<OmsOrderDetail> orderPage = portalOrderService.list(status,pageNum,pageSize);
        return CommonResult.success(orderPage);
    }

    @ApiOperation("根据ID获取订单详情")
    @RequestMapping(value = "/detail/{orderId}", method = RequestMethod.GET)
    public CommonResult<OmsOrderDetail> detail(@PathVariable Long orderId) {
        // 查询订单及其订单项的详细信息
        OmsOrderDetail orderDetail = portalOrderService.detail(orderId);
        return CommonResult.success(orderDetail);
    }

    @ApiOperation("用户取消订单")
    @RequestMapping(value = "/cancelUserOrder", method = RequestMethod.POST)
    public CommonResult cancelUserOrder(Long orderId) {
        // 用户主动取消未支付的订单，释放锁定库存
        portalOrderService.cancelOrder(orderId);
        return CommonResult.success(null);
    }

    @ApiOperation("用户确认收货")
    @RequestMapping(value = "/confirmReceiveOrder", method = RequestMethod.POST)
    public CommonResult confirmReceiveOrder(Long orderId) {
        // 更新订单状态为已完成，增加会员积分和成长值
        portalOrderService.confirmReceiveOrder(orderId);
        return CommonResult.success(null);
    }

    @ApiOperation("用户删除订单")
    @RequestMapping(value = "/deleteOrder", method = RequestMethod.POST)
    public CommonResult deleteOrder(Long orderId) {
        // 逻辑删除订单（修改 deleteStatus 字段），仅对已关闭或已完成的订单有效
        portalOrderService.deleteOrder(orderId);
        return CommonResult.success(null);
    }
}
