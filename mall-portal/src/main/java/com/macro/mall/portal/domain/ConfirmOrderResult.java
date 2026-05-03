package com.macro.mall.portal.domain;

import com.macro.mall.model.UmsIntegrationConsumeSetting;
import com.macro.mall.model.UmsMemberReceiveAddress;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 确认单结果封装类 (Confirm Order Result DTO)
 * 包含生成订单前需要展示给用户的所有信息，如商品列表、收货地址、优惠券、积分规则和金额计算
 */
@Getter
@Setter
public class ConfirmOrderResult {
    @ApiModelProperty("包含优惠信息的购物车商品列表")
    private List<CartPromotionItem> cartPromotionItemList;
    @ApiModelProperty("用户收货地址列表")
    private List<UmsMemberReceiveAddress> memberReceiveAddressList;
    @ApiModelProperty("用户可用优惠券列表")
    private List<SmsCouponHistoryDetail> couponHistoryDetailList;
    @ApiModelProperty("积分使用规则配置")
    private UmsIntegrationConsumeSetting integrationConsumeSetting;
    @ApiModelProperty("会员当前持有的积分")
    private Integer memberIntegration;
    @ApiModelProperty("订单金额计算结果")
    private CalcAmount calcAmount;

    /**
     * 订单金额计算内部类
     * 封装订单的总金额、运费、优惠金额和应付金额
     */
    @Getter
    @Setter
    public static class CalcAmount{
        @ApiModelProperty("订单商品总金额")
        private BigDecimal totalAmount;
        @ApiModelProperty("运费")
        private BigDecimal freightAmount;
        @ApiModelProperty("活动优惠金额")
        private BigDecimal promotionAmount;
        @ApiModelProperty("应付金额（扣除所有优惠后）")
        private BigDecimal payAmount;
    }
}
