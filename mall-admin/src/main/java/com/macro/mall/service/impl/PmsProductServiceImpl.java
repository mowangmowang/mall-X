package com.macro.mall.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.macro.mall.bo.AdminUserDetails;
import com.macro.mall.dao.*;
import com.macro.mall.dto.PmsProductParam;
import com.macro.mall.dto.PmsProductQueryParam;
import com.macro.mall.dto.PmsProductResult;
import com.macro.mall.mapper.*;
import com.macro.mall.model.*;
import com.macro.mall.service.PmsProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品管理 Service 实现类
 * 实现商品的增删改查、上下架、审核、推荐等核心业务逻辑
 * 处理商品与 SKU、属性、促销价格等关联数据的一致性
 */
@Service
public class PmsProductServiceImpl implements PmsProductService {
    /**
     * 日志记录器
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PmsProductServiceImpl.class);
    
    /**
     * 商品 Mapper
     */
    @Autowired
    private PmsProductMapper productMapper;
    
    /**
     * 会员价格 DAO
     */
    @Autowired
    private PmsMemberPriceDao memberPriceDao;
    
    /**
     * 会员价格 Mapper
     */
    @Autowired
    private PmsMemberPriceMapper memberPriceMapper;
    
    /**
     * 阶梯价格 DAO
     */
    @Autowired
    private PmsProductLadderDao productLadderDao;
    
    /**
     * 阶梯价格 Mapper
     */
    @Autowired
    private PmsProductLadderMapper productLadderMapper;
    
    /**
     * 满减价格 DAO
     */
    @Autowired
    private PmsProductFullReductionDao productFullReductionDao;
    
    /**
     * 满减价格 Mapper
     */
    @Autowired
    private PmsProductFullReductionMapper productFullReductionMapper;
    
    /**
     * SKU 库存 DAO
     */
    @Autowired
    private PmsSkuStockDao skuStockDao;
    
    /**
     * SKU 库存 Mapper
     */
    @Autowired
    private PmsSkuStockMapper skuStockMapper;
    
    /**
     * 商品属性值 DAO
     */
    @Autowired
    private PmsProductAttributeValueDao productAttributeValueDao;
    
    /**
     * 商品属性值 Mapper
     */
    @Autowired
    private PmsProductAttributeValueMapper productAttributeValueMapper;
    
    /**
     * 专题-商品关系 DAO
     */
    @Autowired
    private CmsSubjectProductRelationDao subjectProductRelationDao;
    
    /**
     * 专题-商品关系 Mapper
     */
    @Autowired
    private CmsSubjectProductRelationMapper subjectProductRelationMapper;
    
    /**
     * 优选专区-商品关系 DAO
     */
    @Autowired
    private CmsPrefrenceAreaProductRelationDao prefrenceAreaProductRelationDao;
    
    /**
     * 优选专区-商品关系 Mapper
     */
    @Autowired
    private CmsPrefrenceAreaProductRelationMapper prefrenceAreaProductRelationMapper;
    
    /**
     * 商品自定义 DAO（复杂查询）
     */
    @Autowired
    private PmsProductDao productDao;
    
    /**
     * 商品审核记录 DAO
     */
    @Autowired
    private PmsProductVerifyRecordDao productVerifyRecordDao;

    /**
     * 创建商品
     * 包括商品基本信息、SKU、属性值、促销价格等完整信息
     *
     * @param productParam 商品参数对象
     * @return 新创建的商品 ID
     */
    @Override
    public Long create(PmsProductParam productParam) {
        // 创建商品基本信息
        PmsProduct product = productParam;
        product.setId(null);
        productMapper.insertSelective(product);
        Long productId = product.getId();
        
        // 根据促销类型设置价格：会员价格、阶梯价格、满减价格
        // 会员价格
        relateAndInsertList(memberPriceDao, productParam.getMemberPriceList(), productId);
        // 阶梯价格
        relateAndInsertList(productLadderDao, productParam.getProductLadderList(), productId);
        // 满减价格
        relateAndInsertList(productFullReductionDao, productParam.getProductFullReductionList(), productId);
        
        // 处理 SKU 的编码（如果为空则自动生成）
        handleSkuStockCode(productParam.getSkuStockList(), productId);
        // 添加 SKU 库存信息
        relateAndInsertList(skuStockDao, productParam.getSkuStockList(), productId);
        
        // 添加商品参数、自定义商品规格
        relateAndInsertList(productAttributeValueDao, productParam.getProductAttributeValueList(), productId);
        
        // 关联专题
        relateAndInsertList(subjectProductRelationDao, productParam.getSubjectProductRelationList(), productId);
        
        // 关联优选专区
        relateAndInsertList(prefrenceAreaProductRelationDao, productParam.getPrefrenceAreaProductRelationList(), productId);
        
        return productId;
    }

    /**
     * 处理 SKU 编码
     * 如果 SKU 编码为空，则自动生成：日期(8位) + 商品ID(4位) + 索引(3位)
     *
     * @param skuStockList SKU 列表
     * @param productId 商品 ID
     */
    private void handleSkuStockCode(List<PmsSkuStock> skuStockList, Long productId) {
        if (CollectionUtils.isEmpty(skuStockList)) return;
        for (int i = 0; i < skuStockList.size(); i++) {
            PmsSkuStock skuStock = skuStockList.get(i);
            if (StrUtil.isEmpty(skuStock.getSkuCode())) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                StringBuilder sb = new StringBuilder();
                // 日期（8位）
                sb.append(sdf.format(new Date()));
                // 四位商品 ID
                sb.append(String.format("%04d", productId));
                // 三位索引 ID
                sb.append(String.format("%03d", i + 1));
                skuStock.setSkuCode(sb.toString());
            }
        }
    }

    @Override
    public PmsProductResult getUpdateInfo(Long id) {
        return productDao.getUpdateInfo(id);
    }

    @Override
    public int update(Long id, PmsProductParam productParam) {
        int count;
        //更新商品信息
        PmsProduct product = productParam;
        product.setId(id);
        productMapper.updateByPrimaryKeySelective(product);
        //会员价格
        PmsMemberPriceExample pmsMemberPriceExample = new PmsMemberPriceExample();
        pmsMemberPriceExample.createCriteria().andProductIdEqualTo(id);
        memberPriceMapper.deleteByExample(pmsMemberPriceExample);
        relateAndInsertList(memberPriceDao, productParam.getMemberPriceList(), id);
        //阶梯价格
        PmsProductLadderExample ladderExample = new PmsProductLadderExample();
        ladderExample.createCriteria().andProductIdEqualTo(id);
        productLadderMapper.deleteByExample(ladderExample);
        relateAndInsertList(productLadderDao, productParam.getProductLadderList(), id);
        //满减价格
        PmsProductFullReductionExample fullReductionExample = new PmsProductFullReductionExample();
        fullReductionExample.createCriteria().andProductIdEqualTo(id);
        productFullReductionMapper.deleteByExample(fullReductionExample);
        relateAndInsertList(productFullReductionDao, productParam.getProductFullReductionList(), id);
        //修改sku库存信息
        handleUpdateSkuStockList(id, productParam);
        //修改商品参数,添加自定义商品规格
        PmsProductAttributeValueExample productAttributeValueExample = new PmsProductAttributeValueExample();
        productAttributeValueExample.createCriteria().andProductIdEqualTo(id);
        productAttributeValueMapper.deleteByExample(productAttributeValueExample);
        relateAndInsertList(productAttributeValueDao, productParam.getProductAttributeValueList(), id);
        //关联专题
        CmsSubjectProductRelationExample subjectProductRelationExample = new CmsSubjectProductRelationExample();
        subjectProductRelationExample.createCriteria().andProductIdEqualTo(id);
        subjectProductRelationMapper.deleteByExample(subjectProductRelationExample);
        relateAndInsertList(subjectProductRelationDao, productParam.getSubjectProductRelationList(), id);
        //关联优选
        CmsPrefrenceAreaProductRelationExample prefrenceAreaExample = new CmsPrefrenceAreaProductRelationExample();
        prefrenceAreaExample.createCriteria().andProductIdEqualTo(id);
        prefrenceAreaProductRelationMapper.deleteByExample(prefrenceAreaExample);
        relateAndInsertList(prefrenceAreaProductRelationDao, productParam.getPrefrenceAreaProductRelationList(), id);
        count = 1;
        return count;
    }

    private void handleUpdateSkuStockList(Long id, PmsProductParam productParam) {
        //当前的sku信息
        List<PmsSkuStock> currSkuList = productParam.getSkuStockList();
        //当前没有sku直接删除
        if(CollUtil.isEmpty(currSkuList)){
            PmsSkuStockExample skuStockExample = new PmsSkuStockExample();
            skuStockExample.createCriteria().andProductIdEqualTo(id);
            skuStockMapper.deleteByExample(skuStockExample);
            return;
        }
        //获取初始sku信息
        PmsSkuStockExample skuStockExample = new PmsSkuStockExample();
        skuStockExample.createCriteria().andProductIdEqualTo(id);
        List<PmsSkuStock> oriStuList = skuStockMapper.selectByExample(skuStockExample);
        //获取新增sku信息
        List<PmsSkuStock> insertSkuList = currSkuList.stream().filter(item->item.getId()==null).collect(Collectors.toList());
        //获取需要更新的sku信息
        List<PmsSkuStock> updateSkuList = currSkuList.stream().filter(item->item.getId()!=null).collect(Collectors.toList());
        List<Long> updateSkuIds = updateSkuList.stream().map(PmsSkuStock::getId).collect(Collectors.toList());
        //获取需要删除的sku信息
        List<PmsSkuStock> removeSkuList = oriStuList.stream().filter(item-> !updateSkuIds.contains(item.getId())).collect(Collectors.toList());
        handleSkuStockCode(insertSkuList,id);
        handleSkuStockCode(updateSkuList,id);
        //新增sku
        if(CollUtil.isNotEmpty(insertSkuList)){
            relateAndInsertList(skuStockDao, insertSkuList, id);
        }
        //删除sku
        if(CollUtil.isNotEmpty(removeSkuList)){
            List<Long> removeSkuIds = removeSkuList.stream().map(PmsSkuStock::getId).collect(Collectors.toList());
            PmsSkuStockExample removeExample = new PmsSkuStockExample();
            removeExample.createCriteria().andIdIn(removeSkuIds);
            skuStockMapper.deleteByExample(removeExample);
        }
        //修改sku
        if(CollUtil.isNotEmpty(updateSkuList)){
            for (PmsSkuStock pmsSkuStock : updateSkuList) {
                skuStockMapper.updateByPrimaryKeySelective(pmsSkuStock);
            }
        }

    }

    @Override
    public List<PmsProduct> list(PmsProductQueryParam productQueryParam, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        PmsProductExample productExample = new PmsProductExample();
        PmsProductExample.Criteria criteria = productExample.createCriteria();
        criteria.andDeleteStatusEqualTo(0);
        if (productQueryParam.getPublishStatus() != null) {
            criteria.andPublishStatusEqualTo(productQueryParam.getPublishStatus());
        }
        if (productQueryParam.getVerifyStatus() != null) {
            criteria.andVerifyStatusEqualTo(productQueryParam.getVerifyStatus());
        }
        if (!StrUtil.isEmpty(productQueryParam.getKeyword())) {
            criteria.andNameLike("%" + productQueryParam.getKeyword() + "%");
        }
        if (!StrUtil.isEmpty(productQueryParam.getProductSn())) {
            criteria.andProductSnEqualTo(productQueryParam.getProductSn());
        }
        if (productQueryParam.getBrandId() != null) {
            criteria.andBrandIdEqualTo(productQueryParam.getBrandId());
        }
        if (productQueryParam.getProductCategoryId() != null) {
            criteria.andProductCategoryIdEqualTo(productQueryParam.getProductCategoryId());
        }
        return productMapper.selectByExample(productExample);
    }

    @Override
    public int updateVerifyStatus(List<Long> ids, Integer verifyStatus, String detail) {
        PmsProduct product = new PmsProduct();
        product.setVerifyStatus(verifyStatus);
        PmsProductExample example = new PmsProductExample();
        example.createCriteria().andIdIn(ids);
        List<PmsProductVerifyRecord> list = new ArrayList<>();
        int count = productMapper.updateByExampleSelective(product, example);
        
        // 获取当前登录用户
        String username = "system"; // 默认值
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof AdminUserDetails) {
                    username = ((AdminUserDetails) principal).getUsername();
                } else if (principal instanceof String) {
                    username = (String) principal;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("获取当前用户失败，使用默认值: {}", e.getMessage());
        }
        
        //修改完审核状态后插入审核记录
        for (Long id : ids) {
            PmsProductVerifyRecord record = new PmsProductVerifyRecord();
            record.setProductId(id);
            record.setCreateTime(new Date());
            record.setDetail(detail);
            record.setStatus(verifyStatus);
            record.setVertifyMan(username);
            list.add(record);
        }
        productVerifyRecordDao.insertList(list);
        return count;
    }

    @Override
    public int updatePublishStatus(List<Long> ids, Integer publishStatus) {
        PmsProduct record = new PmsProduct();
        record.setPublishStatus(publishStatus);
        PmsProductExample example = new PmsProductExample();
        example.createCriteria().andIdIn(ids);
        return productMapper.updateByExampleSelective(record, example);
    }

    @Override
    public int updateRecommendStatus(List<Long> ids, Integer recommendStatus) {
        PmsProduct record = new PmsProduct();
        record.setRecommandStatus(recommendStatus);
        PmsProductExample example = new PmsProductExample();
        example.createCriteria().andIdIn(ids);
        return productMapper.updateByExampleSelective(record, example);
    }

    @Override
    public int updateNewStatus(List<Long> ids, Integer newStatus) {
        PmsProduct record = new PmsProduct();
        record.setNewStatus(newStatus);
        PmsProductExample example = new PmsProductExample();
        example.createCriteria().andIdIn(ids);
        return productMapper.updateByExampleSelective(record, example);
    }

    @Override
    public int updateDeleteStatus(List<Long> ids, Integer deleteStatus) {
        PmsProduct record = new PmsProduct();
        record.setDeleteStatus(deleteStatus);
        PmsProductExample example = new PmsProductExample();
        example.createCriteria().andIdIn(ids);
        return productMapper.updateByExampleSelective(record, example);
    }

    @Override
    public List<PmsProduct> list(String keyword) {
        PmsProductExample productExample = new PmsProductExample();
        PmsProductExample.Criteria criteria = productExample.createCriteria();
        criteria.andDeleteStatusEqualTo(0);
        if(!StrUtil.isEmpty(keyword)){
            criteria.andNameLike("%" + keyword + "%");
            productExample.or().andDeleteStatusEqualTo(0).andProductSnLike("%" + keyword + "%");
        }
        return productMapper.selectByExample(productExample);
    }

    @Override
    public List<PmsProductVerifyRecord> getVerifyRecordList(Long productId) {
        return productVerifyRecordDao.getListByProductId(productId);
    }

    /**
     * 建立和插入关系表操作
     *
     * @param dao       可以操作的dao
     * @param dataList  要插入的数据
     * @param productId 建立关系的id
     */
    private void relateAndInsertList(Object dao, List dataList, Long productId) {
        try {
            if (CollectionUtils.isEmpty(dataList)) return;
            for (Object item : dataList) {
                Method setId = item.getClass().getMethod("setId", Long.class);
                setId.invoke(item, (Long) null);
                Method setProductId = item.getClass().getMethod("setProductId", Long.class);
                setProductId.invoke(item, productId);
            }
            Method insertList = dao.getClass().getMethod("insertList", List.class);
            insertList.invoke(dao, dataList);
        } catch (Exception e) {
            LOGGER.warn("创建商品出错:{}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
