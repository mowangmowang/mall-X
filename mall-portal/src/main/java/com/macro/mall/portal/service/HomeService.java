package com.macro.mall.portal.service;

import com.macro.mall.model.CmsSubject;
import com.macro.mall.model.CmsTopic;
import com.macro.mall.model.PmsProduct;
import com.macro.mall.model.PmsProductCategory;
import com.macro.mall.portal.domain.HomeContentResult;
import com.macro.mall.portal.domain.PrefrenceAreaResult;
import com.macro.mall.portal.domain.SubjectDetail;

import java.util.List;

/**
 * 首页内容管理服务接口 (Home Content Service Interface)
 */
public interface HomeService {

    /**
     * 获取首页内容
     */
    HomeContentResult content();

    /**
     * 首页商品推荐
     */
    List<PmsProduct> recommendProductList(Integer pageSize, Integer pageNum);

    /**
     * 获取商品分类
     * @param parentId 0:获取一级分类；其他：获取指定二级分类
     */
    List<PmsProductCategory> getProductCateList(Long parentId);

    /**
     * 根据专题分类id分页获取专题
     * @param cateId 专题分类id
     */
    List<CmsSubject> getSubjectList(Long cateId, Integer pageSize, Integer pageNum);

    /**
     * 分页获取人气推荐商品
     */
    List<PmsProduct> hotProductList(Integer pageNum, Integer pageSize);

    /**
     * 分页获取新品推荐商品
     */
    List<PmsProduct> newProductList(Integer pageNum, Integer pageSize);

    /**
     * 获取专题详情及关联商品
     */
    SubjectDetail getSubjectDetail(Long subjectId);

    /**
     * 获取优选专区列表（含商品）
     */
    List<PrefrenceAreaResult> getPrefrenceAreaList();

    /**
     * 获取话题列表（进行中的热门话题）
     */
    List<CmsTopic> getTopicList(Integer pageSize, Integer pageNum);

    /**
     * 获取话题详情
     */
    CmsTopic getTopicDetail(Long topicId);
}
