package com.macro.mall.portal.controller;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.portal.domain.MemberBrandAttention;
import com.macro.mall.portal.service.MemberAttentionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 会员品牌关注管理控制器
 * 提供品牌关注的增删查功能，数据存储于 MongoDB
 */
@RestController
@Api(tags = "MemberAttentionController")
@Tag(name = "MemberAttentionController",description = "会员关注品牌管理")
@RequestMapping("/member/attention")
public class MemberAttentionController {
    @Autowired
    private MemberAttentionService memberAttentionService;
    
    @ApiOperation("添加品牌关注")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public CommonResult add(@RequestBody MemberBrandAttention memberBrandAttention) {
        // 将品牌关注记录保存到 MongoDB
        int count = memberAttentionService.add(memberBrandAttention);
        if(count>0){
            return CommonResult.success(count);
        }else{
            return CommonResult.failed();
        }
    }

    @ApiOperation("取消品牌关注")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public CommonResult delete(Long brandId) {
        // 根据品牌ID删除关注记录
        int count = memberAttentionService.delete(brandId);
        if(count>0){
            return CommonResult.success(count);
        }else{
            return CommonResult.failed();
        }
    }

    @ApiOperation("分页查询当前用户品牌关注列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<MemberBrandAttention>> list(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                               @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        // 查询当前用户的品牌关注列表，按时间倒序排列
        Page<MemberBrandAttention> page = memberAttentionService.list(pageNum,pageSize);
        return CommonResult.success(CommonPage.restPage(page));
    }

    @ApiOperation("根据品牌ID获取品牌关注详情")
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public CommonResult<MemberBrandAttention> detail(@RequestParam Long brandId) {
        // 查询指定品牌的关注详情
        MemberBrandAttention memberBrandAttention = memberAttentionService.detail(brandId);
        return CommonResult.success(memberBrandAttention);
    }

    @ApiOperation("清空当前用户品牌关注列表")
    @RequestMapping(value = "/clear", method = RequestMethod.POST)
    public CommonResult clear() {
        // 删除当前用户的所有品牌关注记录
        memberAttentionService.clear();
        return CommonResult.success(null);
    }
}
