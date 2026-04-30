package com.macro.mall.portal.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 订单生成参数
 * 封装用户提交订单时所需的信息，包括收货地址、优惠券、积分、支付方式和购物车商品
 */
@Data
@EqualsAndHashCode
public class OrderParam {
    @ApiModelProperty("收货地址ID")
    private Long memberReceiveAddressId;
    @ApiModelProperty("优惠券ID，不使用则为null")
    private Long couponId;
    @ApiModelProperty("使用的积分数，0表示不使用积分")
    private Integer useIntegration;
    @ApiModelProperty("支付方式：0->未支付；1->支付宝；2->微信")
    private Integer payType;
    @ApiModelProperty("被选中的购物车商品ID列表")
    private List<Long> cartIds;
}
