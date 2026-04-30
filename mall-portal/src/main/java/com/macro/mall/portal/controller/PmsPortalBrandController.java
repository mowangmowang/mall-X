package com.macro.mall.portal.controller;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.PmsBrand;
import com.macro.mall.model.PmsProduct;
import com.macro.mall.portal.service.PmsPortalBrandService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台品牌管理控制器
 * 提供品牌推荐、品牌详情查询及品牌关联商品查询功能
 */
@RestController
@Api(tags = "PmsPortalBrandController")
@Tag(name = "PmsPortalBrandController", description = "前台品牌管理")
@RequestMapping("/brand")
public class PmsPortalBrandController {

    @Autowired
    private PmsPortalBrandService portalBrandService;

    @ApiOperation("分页获取推荐品牌")
    @RequestMapping(value = "/recommendList", method = RequestMethod.GET)
    public CommonResult<List<PmsBrand>> recommendList(@RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize,
                                                      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        // 查询标记为“推荐”的品牌列表，用于首页品牌展示
        List<PmsBrand> brandList = portalBrandService.recommendList(pageNum, pageSize);
        return CommonResult.success(brandList);
    }

    @ApiOperation("获取品牌详情")
    @RequestMapping(value = "/detail/{brandId}", method = RequestMethod.GET)
    public CommonResult<PmsBrand> detail(@PathVariable Long brandId) {
        // 查询指定品牌的详细信息
        PmsBrand brand = portalBrandService.detail(brandId);
        return CommonResult.success(brand);
    }

    @ApiOperation("分页获取品牌相关商品")
    @RequestMapping(value = "/productList", method = RequestMethod.GET)
    public CommonResult<CommonPage<PmsProduct>> productList(@RequestParam Long brandId,
                                                            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                            @RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize) {
        // 查询指定品牌下的商品列表，支持分页
        CommonPage<PmsProduct> result = portalBrandService.productList(brandId,pageNum, pageSize);
        return CommonResult.success(result);
    }
}
