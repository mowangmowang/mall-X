package com.macro.mall.portal.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 订单生成参数类 (Order Parameter DTO)
 * 封装用户提交订单时所需的信息，包括收货地址、优惠券、积分、支付方式和购物车商品
 */
@Data
@EqualsAndHashCode
public class OrderParam {
    @Schema(description = "收货地址ID")
    private Long memberReceiveAddressId;
    @Schema(description = "优惠券ID，不使用则为null")
    private Long couponId;
    @Schema(description = "使用的积分数，0表示不使用积分")
    private Integer useIntegration;
    @Schema(description = "支付方式：0->未支付；1->支付宝；2->微信")
    private Integer payType;
    @Schema(description = "被选中的购物车商品ID列表")
    private List<Long> cartIds;
}
