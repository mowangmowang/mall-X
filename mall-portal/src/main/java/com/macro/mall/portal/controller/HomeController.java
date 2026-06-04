package com.macro.mall.portal.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.CmsSubject;
import com.macro.mall.model.CmsTopic;
import com.macro.mall.model.PmsProduct;
import com.macro.mall.model.PmsProductCategory;
import com.macro.mall.portal.domain.HomeContentResult;
import com.macro.mall.portal.domain.PrefrenceAreaResult;
import com.macro.mall.portal.domain.SubjectDetail;
import com.macro.mall.portal.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首页内容管理控制器 (Home Content Controller)
 * 提供首页展示所需的各种数据，包括轮播广告、推荐商品、商品分类、专题等
 */
@RestController
@Tag(name = "HomeController", description = "首页内容管理")
@RequestMapping("/home")
public class HomeController {
    @Autowired
    private HomeService homeService;

    @Operation(summary = "首页内容信息展示")
    @RequestMapping(value = "/content", method = RequestMethod.GET)
    public CommonResult<HomeContentResult> content() {
        // 获取首页所有数据：轮播广告、推荐品牌、新品、热销商品等
        HomeContentResult contentResult = homeService.content();
        return CommonResult.success(contentResult);
    }

    @Operation(summary = "分页获取推荐商品")
    @RequestMapping(value = "/recommendProductList", method = RequestMethod.GET)
    public CommonResult<List<PmsProduct>> recommendProductList(@RequestParam(value = "pageSize", defaultValue = "4") Integer pageSize,
                                                               @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        // 查询标记为“推荐”的商品列表
        List<PmsProduct> productList = homeService.recommendProductList(pageSize, pageNum);
        return CommonResult.success(productList);
    }

    @Operation(summary = "获取首页商品分类")
    @RequestMapping(value = "/productCateList/{parentId}", method = RequestMethod.GET)
    public CommonResult<List<PmsProductCategory>> getProductCateList(@PathVariable Long parentId) {
        // 根据父分类ID查询子分类列表，用于构建分类导航
        List<PmsProductCategory> productCategoryList = homeService.getProductCateList(parentId);
        return CommonResult.success(productCategoryList);
    }

    @Operation(summary = "根据分类分页获取专题")
    @RequestMapping(value = "/subjectList", method = RequestMethod.GET)
    public CommonResult<List<CmsSubject>> getSubjectList(@RequestParam(required = false) Long cateId,
                                                         @RequestParam(value = "pageSize", defaultValue = "4") Integer pageSize,
                                                         @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        // 查询专题列表，可按分类筛选，用于展示主题活动
        List<CmsSubject> subjectList = homeService.getSubjectList(cateId,pageSize,pageNum);
        return CommonResult.success(subjectList);
    }

    @Operation(summary = "分页获取人气推荐商品")
    @RequestMapping(value = "/hotProductList", method = RequestMethod.GET)
    public CommonResult<List<PmsProduct>> hotProductList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                         @RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize) {
        // 查询标记为“热销”的商品列表
        List<PmsProduct> productList = homeService.hotProductList(pageNum,pageSize);
        return CommonResult.success(productList);
    }

    @Operation(summary = "分页获取新品推荐商品")
    @RequestMapping(value = "/newProductList", method = RequestMethod.GET)
    public CommonResult<List<PmsProduct>> newProductList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                         @RequestParam(value = "pageSize", defaultValue = "6") Integer pageSize) {
        // 查询标记为“新品”的商品列表
        List<PmsProduct> productList = homeService.newProductList(pageNum,pageSize);
        return CommonResult.success(productList);
    }

    @Operation(summary = "获取专题详情及关联商品")
    @RequestMapping(value = "/subject/{id}", method = RequestMethod.GET)
    public CommonResult<SubjectDetail> getSubjectDetail(@PathVariable Long id) {
        // 查询专题详细信息及其关联的商品列表
        SubjectDetail detail = homeService.getSubjectDetail(id);
        return CommonResult.success(detail);
    }

    @Operation(summary = "获取优选专区列表")
    @RequestMapping(value = "/prefrenceAreaList", method = RequestMethod.GET)
    public CommonResult<List<PrefrenceAreaResult>> getPrefrenceAreaList() {
        // 查询优选专区列表，用于展示精选商品区域
        List<PrefrenceAreaResult> list = homeService.getPrefrenceAreaList();
        return CommonResult.success(list);
    }

    @Operation(summary = "获取话题详情")
    @RequestMapping(value = "/topic/{id}", method = RequestMethod.GET)
    public CommonResult<CmsTopic> getTopicDetail(@PathVariable Long id) {
        CmsTopic topic = homeService.getTopicDetail(id);
        return CommonResult.success(topic);
    }

    @Operation(summary = "分页获取进行中的热门话题")
    @RequestMapping(value = "/topicList", method = RequestMethod.GET)
    public CommonResult<List<CmsTopic>> getTopicList(@RequestParam(value = "pageSize", defaultValue = "4") Integer pageSize,
                                                     @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        List<CmsTopic> topicList = homeService.getTopicList(pageSize, pageNum);
        return CommonResult.success(topicList);
    }
}
