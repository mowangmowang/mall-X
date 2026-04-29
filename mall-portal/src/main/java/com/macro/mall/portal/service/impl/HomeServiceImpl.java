package com.macro.mall.portal.service.impl;

import com.github.pagehelper.PageHelper;
import com.macro.mall.mapper.*;
import com.macro.mall.model.*;
import java.util.ArrayList;
import com.macro.mall.portal.dao.HomeDao;
import com.macro.mall.portal.domain.FlashPromotionProduct;
import com.macro.mall.portal.domain.HomeContentResult;
import com.macro.mall.portal.domain.HomeFlashPromotion;
import com.macro.mall.portal.domain.PrefrenceAreaResult;
import com.macro.mall.portal.domain.SubjectDetail;
import com.macro.mall.portal.service.HomeService;
import com.macro.mall.portal.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * 首页内容管理Service实现类 */
@Service
public class HomeServiceImpl implements HomeService {
    @Autowired
    private SmsHomeAdvertiseMapper advertiseMapper;
    @Autowired
    private HomeDao homeDao;
    @Autowired
    private SmsFlashPromotionMapper flashPromotionMapper;
    @Autowired
    private SmsFlashPromotionSessionMapper promotionSessionMapper;
    @Autowired
    private PmsProductMapper productMapper;
    @Autowired
    private PmsProductCategoryMapper productCategoryMapper;
    @Autowired
    private CmsSubjectMapper subjectMapper;
    @Autowired
    private CmsSubjectProductRelationMapper subjectProductRelationMapper;
    @Autowired
    private CmsPrefrenceAreaMapper prefrenceAreaMapper;
    @Autowired
    private CmsPrefrenceAreaProductRelationMapper prefrenceAreaProductRelationMapper;

    @Override
    public HomeContentResult content() {
        HomeContentResult result = new HomeContentResult();
        //获取首页广告
        result.setAdvertiseList(getHomeAdvertiseList());
        //获取推荐品牌
        result.setBrandList(homeDao.getRecommendBrandList(0,6));
        //获取新品推荐
        result.setNewProductList(homeDao.getNewProductList(0,4));
        //获取人气推荐
        result.setHotProductList(homeDao.getHotProductList(0,4));
        //获取推荐专题
        result.setSubjectList(homeDao.getRecommendSubjectList(0,4));
        return result;
    }

    @Override
    public List<PmsProduct> recommendProductList(Integer pageSize, Integer pageNum) {
        // TODO: 2019/1/29 暂时默认推荐所有商品
        PageHelper.startPage(pageNum,pageSize);
        PmsProductExample example = new PmsProductExample();
        example.createCriteria()
                .andDeleteStatusEqualTo(0)
                .andPublishStatusEqualTo(1);
        return productMapper.selectByExample(example);
    }

    @Override
    public List<PmsProductCategory> getProductCateList(Long parentId) {
        PmsProductCategoryExample example = new PmsProductCategoryExample();
        example.createCriteria()
                .andShowStatusEqualTo(1)
                .andParentIdEqualTo(parentId);
        example.setOrderByClause("sort desc");
        return productCategoryMapper.selectByExample(example);
    }

    @Override
    public List<CmsSubject> getSubjectList(Long cateId, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum,pageSize);
        CmsSubjectExample example = new CmsSubjectExample();
        CmsSubjectExample.Criteria criteria = example.createCriteria();
        if(cateId!=null){
            criteria.andCategoryIdEqualTo(cateId);
        }
        List<CmsSubject> subjectList = subjectMapper.selectByExample(example);
        // 过滤未显示专题(showStatus=0，兼容NULL视为显示)
        if (subjectList != null && !subjectList.isEmpty()) {
            subjectList.removeIf(s -> s.getShowStatus() != null && s.getShowStatus() == 0);
        }
        // 从关联表计算真实商品数量（仅统计上架未删商品），并补充封面图
        if (subjectList != null && !subjectList.isEmpty()) {
            List<Long> subjectIds = new ArrayList<>();
            for (CmsSubject subject : subjectList) {
                subjectIds.add(subject.getId());
            }
            CmsSubjectProductRelationExample relationExample = new CmsSubjectProductRelationExample();
            relationExample.createCriteria().andSubjectIdIn(subjectIds);
            List<CmsSubjectProductRelation> relations = subjectProductRelationMapper.selectByExample(relationExample);
            if (relations.isEmpty()) {
                for (CmsSubject subject : subjectList) {
                    subject.setProductCount(0);
                }
            } else {
                // 收集所有关联的商品ID
                java.util.Set<Long> allProductIds = new java.util.HashSet<>();
                for (CmsSubjectProductRelation relation : relations) {
                    allProductIds.add(relation.getProductId());
                }
                // 批量查询商品，过滤下架/删除的
                java.util.Set<Long> validProductIds = new java.util.HashSet<>();
                java.util.Map<Long, PmsProduct> productMap = new java.util.HashMap<>();
                if (!allProductIds.isEmpty()) {
                    PmsProductExample productExample = new PmsProductExample();
                    productExample.createCriteria().andIdIn(new ArrayList<>(allProductIds));
                    List<PmsProduct> products = productMapper.selectByExample(productExample);
                    for (PmsProduct product : products) {
                        productMap.put(product.getId(), product);
                        if (product.getPublishStatus() != null && product.getPublishStatus() == 1
                                && product.getDeleteStatus() != null && product.getDeleteStatus() == 0) {
                            validProductIds.add(product.getId());
                        }
                    }
                }
                // 按subjectId统计有效商品数，并记录第一个有效商品ID
                java.util.Map<Long, Integer> countMap = new java.util.HashMap<>();
                java.util.Map<Long, Long> firstProductMap = new java.util.HashMap<>();
                for (CmsSubjectProductRelation relation : relations) {
                    if (validProductIds.contains(relation.getProductId())) {
                        countMap.merge(relation.getSubjectId(), 1, Integer::sum);
                        firstProductMap.putIfAbsent(relation.getSubjectId(), relation.getProductId());
                    }
                }
                for (CmsSubject subject : subjectList) {
                    subject.setProductCount(countMap.getOrDefault(subject.getId(), 0));
                    // 当专题自身无封面图时，使用第一个有效关联商品图片
                    if (subject.getPic() == null) {
                        Long firstProductId = firstProductMap.get(subject.getId());
                        if (firstProductId != null) {
                            PmsProduct product = productMap.get(firstProductId);
                            if (product != null && product.getPic() != null) {
                                subject.setPic(product.getPic());
                            }
                        }
                    }
                }
            }
        }
        return subjectList;
    }

    @Override
    public List<PmsProduct> hotProductList(Integer pageNum, Integer pageSize) {
        int offset = pageSize * (pageNum - 1);
        return homeDao.getHotProductList(offset, pageSize);
    }

    @Override
    public List<PmsProduct> newProductList(Integer pageNum, Integer pageSize) {
        int offset = pageSize * (pageNum - 1);
        return homeDao.getNewProductList(offset, pageSize);
    }

    @Override
    public SubjectDetail getSubjectDetail(Long subjectId) {
        SubjectDetail detail = new SubjectDetail();
        CmsSubject subject = subjectMapper.selectByPrimaryKey(subjectId);
        detail.setSubject(subject);
        // 查询关联商品
        CmsSubjectProductRelationExample relationExample = new CmsSubjectProductRelationExample();
        relationExample.createCriteria().andSubjectIdEqualTo(subjectId);
        List<CmsSubjectProductRelation> relations = subjectProductRelationMapper.selectByExample(relationExample);
        List<PmsProduct> productList = new ArrayList<>();
        for (CmsSubjectProductRelation relation : relations) {
            PmsProduct product = productMapper.selectByPrimaryKey(relation.getProductId());
            if (product != null && product.getPublishStatus() == 1 && product.getDeleteStatus() == 0) {
                productList.add(product);
            }
        }
        detail.setProductList(productList);
        // 当专题自身无封面图时，使用第一个关联商品图片
        if (subject != null && subject.getPic() == null && !productList.isEmpty()) {
            PmsProduct firstProduct = productList.get(0);
            if (firstProduct.getPic() != null) {
                subject.setPic(firstProduct.getPic());
            }
        }
        return detail;
    }

    @Override
    public List<PrefrenceAreaResult> getPrefrenceAreaList() {
        List<PrefrenceAreaResult> resultList = new ArrayList<>();
        CmsPrefrenceAreaExample areaExample = new CmsPrefrenceAreaExample();
        areaExample.createCriteria().andShowStatusEqualTo(1);
        areaExample.setOrderByClause("sort desc");
        List<CmsPrefrenceArea> areaList = prefrenceAreaMapper.selectByExampleWithBLOBs(areaExample);
        for (CmsPrefrenceArea area : areaList) {
            PrefrenceAreaResult result = new PrefrenceAreaResult();
            result.setArea(area);
            CmsPrefrenceAreaProductRelationExample relationExample = new CmsPrefrenceAreaProductRelationExample();
            relationExample.createCriteria().andPrefrenceAreaIdEqualTo(area.getId());
            List<CmsPrefrenceAreaProductRelation> relations = prefrenceAreaProductRelationMapper.selectByExample(relationExample);
            List<PmsProduct> productList = new ArrayList<>();
            for (CmsPrefrenceAreaProductRelation relation : relations) {
                PmsProduct product = productMapper.selectByPrimaryKey(relation.getProductId());
                if (product != null && product.getPublishStatus() == 1 && product.getDeleteStatus() == 0) {
                    productList.add(product);
                }
            }
            result.setProductList(productList);
            resultList.add(result);
        }
        return resultList;
    }

    private HomeFlashPromotion getHomeFlashPromotion() {
        HomeFlashPromotion homeFlashPromotion = new HomeFlashPromotion();
        //获取当前秒杀活动
        Date now = new Date();
        SmsFlashPromotion flashPromotion = getFlashPromotion(now);
        if (flashPromotion != null) {
            //获取当前秒杀场次
            SmsFlashPromotionSession flashPromotionSession = getFlashPromotionSession(now);
            if (flashPromotionSession != null) {
                homeFlashPromotion.setStartTime(flashPromotionSession.getStartTime());
                homeFlashPromotion.setEndTime(flashPromotionSession.getEndTime());
                //获取下一个秒杀场次
                SmsFlashPromotionSession nextSession = getNextFlashPromotionSession(homeFlashPromotion.getStartTime());
                if(nextSession!=null){
                    homeFlashPromotion.setNextStartTime(nextSession.getStartTime());
                    homeFlashPromotion.setNextEndTime(nextSession.getEndTime());
                }
                //获取秒杀商品
                List<FlashPromotionProduct> flashProductList = homeDao.getFlashProductList(flashPromotion.getId(), flashPromotionSession.getId());
                homeFlashPromotion.setProductList(flashProductList);
            }
        }
        return homeFlashPromotion;
    }

    //获取下一个场次信息
    private SmsFlashPromotionSession getNextFlashPromotionSession(Date date) {
        SmsFlashPromotionSessionExample sessionExample = new SmsFlashPromotionSessionExample();
        sessionExample.createCriteria()
                .andStartTimeGreaterThan(date);
        sessionExample.setOrderByClause("start_time asc");
        List<SmsFlashPromotionSession> promotionSessionList = promotionSessionMapper.selectByExample(sessionExample);
        if (!CollectionUtils.isEmpty(promotionSessionList)) {
            return promotionSessionList.get(0);
        }
        return null;
    }

    private List<SmsHomeAdvertise> getHomeAdvertiseList() {
        SmsHomeAdvertiseExample example = new SmsHomeAdvertiseExample();
        example.createCriteria().andTypeEqualTo(1).andStatusEqualTo(1);
        example.setOrderByClause("sort desc");
        return advertiseMapper.selectByExample(example);
    }

    //根据时间获取秒杀活动
    private SmsFlashPromotion getFlashPromotion(Date date) {
        Date currDate = DateUtil.getDate(date);
        SmsFlashPromotionExample example = new SmsFlashPromotionExample();
        example.createCriteria()
                .andStatusEqualTo(1)
                .andStartDateLessThanOrEqualTo(currDate)
                .andEndDateGreaterThanOrEqualTo(currDate);
        List<SmsFlashPromotion> flashPromotionList = flashPromotionMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(flashPromotionList)) {
            return flashPromotionList.get(0);
        }
        return null;
    }

    //根据时间获取秒杀场次
    private SmsFlashPromotionSession getFlashPromotionSession(Date date) {
        Date currTime = DateUtil.getTime(date);
        SmsFlashPromotionSessionExample sessionExample = new SmsFlashPromotionSessionExample();
        sessionExample.createCriteria()
                .andStartTimeLessThanOrEqualTo(currTime)
                .andEndTimeGreaterThanOrEqualTo(currTime);
        List<SmsFlashPromotionSession> promotionSessionList = promotionSessionMapper.selectByExample(sessionExample);
        if (!CollectionUtils.isEmpty(promotionSessionList)) {
            return promotionSessionList.get(0);
        }
        return null;
    }
}
