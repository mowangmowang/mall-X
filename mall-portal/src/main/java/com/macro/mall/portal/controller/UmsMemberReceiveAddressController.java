package com.macro.mall.portal.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.UmsMemberReceiveAddress;
import com.macro.mall.portal.service.UmsMemberReceiveAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会员收货地址管理控制器 (Member Receive Address Controller)
 * 提供收货地址的增删改查功能，用于订单确认时选择收货地址
 */
@RestController
@Tag(name = "UmsMemberReceiveAddressController", description = "会员收货地址管理")
@RequestMapping("/member/address")
public class UmsMemberReceiveAddressController {
    @Autowired
    private UmsMemberReceiveAddressService memberReceiveAddressService;

    @Operation(summary = "添加收货地址")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public CommonResult add(@RequestBody UmsMemberReceiveAddress address) {
        // 新增收货地址，自动关联当前登录用户
        int count = memberReceiveAddressService.add(address);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "删除收货地址")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    public CommonResult delete(@PathVariable Long id) {
        // 根据ID删除收货地址
        int count = memberReceiveAddressService.delete(id);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "修改收货地址")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    public CommonResult update(@PathVariable Long id, @RequestBody UmsMemberReceiveAddress address) {
        // 更新指定收货地址的信息
        int count = memberReceiveAddressService.update(id, address);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @Operation(summary = "获取所有收货地址")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<List<UmsMemberReceiveAddress>> list() {
        // 查询当前用户的所有收货地址列表
        List<UmsMemberReceiveAddress> addressList = memberReceiveAddressService.list();
        return CommonResult.success(addressList);
    }

    @Operation(summary = "获取收货地址详情")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public CommonResult<UmsMemberReceiveAddress> getItem(@PathVariable Long id) {
        // 查询指定ID的收货地址详细信息
        UmsMemberReceiveAddress address = memberReceiveAddressService.getItem(id);
        return CommonResult.success(address);
    }
}
