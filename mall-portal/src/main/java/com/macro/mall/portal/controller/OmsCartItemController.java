package com.macro.mall.portal.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.OmsCartItem;
import com.macro.mall.portal.domain.CartProduct;
import com.macro.mall.portal.domain.CartPromotionItem;
import com.macro.mall.portal.service.OmsCartItemService;
import com.macro.mall.portal.service.UmsMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车管理控制器
 * 提供购物车的增删改查、数量调整、规格修改等功能
 */
@RestController
@Api(tags = "OmsCartItemController")
@Tag(name = "OmsCartItemController", description = "购物车管理")
@RequestMapping("/cart")
public class OmsCartItemController {
    @Autowired
    private OmsCartItemService cartItemService;
    @Autowired
    private UmsMemberService memberService;

    @ApiOperation("添加商品到购物车")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public CommonResult add(@RequestBody OmsCartItem cartItem) {
        // 如果购物车中已存在相同商品和规格，则累加数量；否则新增记录
        int count = cartItemService.add(cartItem);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @ApiOperation("获取当前会员的购物车列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<List<OmsCartItem>> list() {
        // 查询当前登录用户的所有购物车项
        List<OmsCartItem> cartItemList = cartItemService.list(memberService.getCurrentMember().getId());
        return CommonResult.success(cartItemList);
    }

    @ApiOperation("获取当前会员的购物车列表,包括促销信息")
    @RequestMapping(value = "/list/promotion", method = RequestMethod.GET)
    public CommonResult<List<CartPromotionItem>> listPromotion(@RequestParam(required = false) List<Long> cartIds) {
        // 查询购物车商品及其对应的促销活动信息（如满减、打折等）
        List<CartPromotionItem> cartPromotionItemList = cartItemService.listPromotion(memberService.getCurrentMember().getId(), cartIds);
        return CommonResult.success(cartPromotionItemList);
    }

    @ApiOperation("修改购物车中指定商品的数量")
    @RequestMapping(value = "/update/quantity", method = RequestMethod.GET)
    public CommonResult updateQuantity(@RequestParam Long id,
                                       @RequestParam Integer quantity) {
        // 更新购物车项数量，校验库存是否充足
        int count = cartItemService.updateQuantity(id, memberService.getCurrentMember().getId(), quantity);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @ApiOperation("获取购物车中指定商品的规格,用于重选规格")
    @RequestMapping(value = "/getProduct/{productId}", method = RequestMethod.GET)
    public CommonResult<CartProduct> getCartProduct(@PathVariable Long productId) {
        // 查询商品的 SKU 信息，供用户重新选择规格
        CartProduct cartProduct = cartItemService.getCartProduct(productId);
        return CommonResult.success(cartProduct);
    }

    @ApiOperation("修改购物车中商品的规格")
    @RequestMapping(value = "/update/attr", method = RequestMethod.POST)
    public CommonResult updateAttr(@RequestBody OmsCartItem cartItem) {
        // 更新购物车项的 SKU 规格（如颜色、尺寸等）
        int count = cartItemService.updateAttr(cartItem);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @ApiOperation("删除购物车中的指定商品")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public CommonResult delete(@RequestParam("ids") List<Long> ids) {
        // 批量删除购物车项
        int count = cartItemService.delete(memberService.getCurrentMember().getId(), ids);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @ApiOperation("清空当前会员的购物车")
    @RequestMapping(value = "/clear", method = RequestMethod.POST)
    public CommonResult clear() {
        // 删除当前用户的所有购物车项
        int count = cartItemService.clear(memberService.getCurrentMember().getId());
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
}
