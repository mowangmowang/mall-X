package com.macro.mall.portal.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.macro.mall.mapper.*;
import com.macro.mall.model.*;
import com.macro.mall.portal.dao.PortalProductDao;
import com.macro.mall.portal.domain.PmsPortalProductDetail;
import com.macro.mall.portal.domain.PmsProductCategoryNode;
import com.macro.mall.portal.service.PmsPortalProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 前台商品管理Service实现类 (Portal Product Service Implementation)
 * <p>
 * 负责前台用户端的商品相关查询操作，包括商品搜索、分类树、商品详情等。
 */
@Service
public class PmsPortalProductServiceImpl implements PmsPortalProductService {
    /** 商品Mapper，用于商品数据的持久化操作 */
    @Autowired
    private PmsProductMapper productMapper;
    
    /** 商品分类Mapper，用于分类数据的持久化操作 */
    @Autowired
    private PmsProductCategoryMapper productCategoryMapper;
    
    /** 品牌Mapper，用于品牌数据的持久化操作 */
    @Autowired
    private PmsBrandMapper brandMapper;
    
    /** 商品属性Mapper，用于属性数据的持久化操作 */
    @Autowired
    private PmsProductAttributeMapper productAttributeMapper;
    
    /** 商品属性值Mapper，用于属性值数据的持久化操作 */
    @Autowired
    private PmsProductAttributeValueMapper productAttributeValueMapper;
    
    /** SKU库存Mapper，用于SKU库存数据的持久化操作 */
    @Autowired
    private PmsSkuStockMapper skuStockMapper;
    
    /** 商品阶梯价格Mapper，用于阶梯价格数据的持久化操作 */
    @Autowired
    private PmsProductLadderMapper productLadderMapper;
    
    /** 商品满减Mapper，用于满减数据的持久化操作 */
    @Autowired
    private PmsProductFullReductionMapper productFullReductionMapper;
    
    /** 商品DAO，用于复杂商品查询操作 */
    @Autowired
    private PortalProductDao portalProductDao;

    /**
     * 搜索商品列表
     * <p>
     * 支持关键字、品牌、分类筛选，以及多种排序方式
     *
     * @param keyword 搜索关键字（模糊匹配商品名称）
     * @param brandId 品牌ID（可选）
     * @param productCategoryId 商品分类ID（可选）
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param sort 排序方式：1->按新品；2->按销量；3->价格从低到高；4->价格从高到低
     * @return 商品列表
     */
    @Override
    public List<PmsProduct> search(String keyword, Long brandId, Long productCategoryId, Integer pageNum, Integer pageSize, Integer sort) {
        PageHelper.startPage(pageNum, pageSize);
        PmsProductExample example = new PmsProductExample();
        PmsProductExample.Criteria criteria = example.createCriteria();
        criteria.andDeleteStatusEqualTo(0);
        criteria.andPublishStatusEqualTo(1);
        if (StrUtil.isNotEmpty(keyword)) {
            criteria.andNameLike("%" + keyword + "%");
        }
        if (brandId != null) {
            criteria.andBrandIdEqualTo(brandId);
        }
        if (productCategoryId != null) {
            criteria.andProductCategoryIdEqualTo(productCategoryId);
        }
        //1->按新品；2->按销量；3->价格从低到高；4->价格从高到低
        if (sort == 1) {
            example.setOrderByClause("id desc");
        } else if (sort == 2) {
            example.setOrderByClause("sale desc");
        } else if (sort == 3) {
            example.setOrderByClause("price asc");
        } else if (sort == 4) {
            example.setOrderByClause("price desc");
        }
        return productMapper.selectByExample(example);
    }

    /**
     * 查询商品分类树形结构
     * <p>
     * 递归构建商品分类的树形结构，用于前端展示分类导航
     *
     * @return 分类树形列表
     */
    @Override
    public List<PmsProductCategoryNode> categoryTreeList() {
        PmsProductCategoryExample example = new PmsProductCategoryExample();
        List<PmsProductCategory> allList = productCategoryMapper.selectByExample(example);
        List<PmsProductCategoryNode> result = allList.stream()
                .filter(item -> item.getParentId().equals(0L))
                .map(item -> covert(item, allList))
                .collect(Collectors.toList());
        return result;
    }

    /**
     * 查询商品详情
     * <p>
     * 获取商品的完整信息，包括基本信息、品牌、属性、SKU、促销信息等
     *
     * @param id 商品唯一标识符 (Product ID)
     * @return 商品详情对象
     */
    @Override
    public PmsPortalProductDetail detail(Long id) {
        PmsPortalProductDetail result = new PmsPortalProductDetail();
        //获取商品信息
        PmsProduct product = productMapper.selectByPrimaryKey(id);
        result.setProduct(product);
        //获取品牌信息
        PmsBrand brand = brandMapper.selectByPrimaryKey(product.getBrandId());
        result.setBrand(brand);
        //获取商品属性信息
        PmsProductAttributeExample attributeExample = new PmsProductAttributeExample();
        attributeExample.createCriteria().andProductAttributeCategoryIdEqualTo(product.getProductAttributeCategoryId());
        List<PmsProductAttribute> productAttributeList = productAttributeMapper.selectByExample(attributeExample);
        result.setProductAttributeList(productAttributeList);
        //获取商品属性值信息
        if(CollUtil.isNotEmpty(productAttributeList)){
            List<Long> attributeIds = productAttributeList.stream().map(PmsProductAttribute::getId).collect(Collectors.toList());
            PmsProductAttributeValueExample attributeValueExample = new PmsProductAttributeValueExample();
            attributeValueExample.createCriteria().andProductIdEqualTo(product.getId())
                    .andProductAttributeIdIn(attributeIds);
            List<PmsProductAttributeValue> productAttributeValueList = productAttributeValueMapper.selectByExample(attributeValueExample);
            result.setProductAttributeValueList(productAttributeValueList);
        }
        //获取商品SKU库存信息
        PmsSkuStockExample skuExample = new PmsSkuStockExample();
        skuExample.createCriteria().andProductIdEqualTo(product.getId());
        List<PmsSkuStock> skuStockList = skuStockMapper.selectByExample(skuExample);
        result.setSkuStockList(skuStockList);
        //商品阶梯价格设置
        if(product.getPromotionType()==3){
            PmsProductLadderExample ladderExample = new PmsProductLadderExample();
            ladderExample.createCriteria().andProductIdEqualTo(product.getId());
            List<PmsProductLadder> productLadderList = productLadderMapper.selectByExample(ladderExample);
            result.setProductLadderList(productLadderList);
        }
        //商品满减价格设置
        if(product.getPromotionType()==4){
            PmsProductFullReductionExample fullReductionExample = new PmsProductFullReductionExample();
            fullReductionExample.createCriteria().andProductIdEqualTo(product.getId());
            List<PmsProductFullReduction> productFullReductionList = productFullReductionMapper.selectByExample(fullReductionExample);
            result.setProductFullReductionList(productFullReductionList);
        }
        //商品可用优惠券
        result.setCouponList(portalProductDao.getAvailableCouponList(product.getId(),product.getProductCategoryId()));
        return result;
    }


    /**
     * 初始对象转化为节点对象
     */
    private PmsProductCategoryNode covert(PmsProductCategory item, List<PmsProductCategory> allList) {
        PmsProductCategoryNode node = new PmsProductCategoryNode();
        BeanUtils.copyProperties(item, node);
        List<PmsProductCategoryNode> children = allList.stream()
                .filter(subItem -> subItem.getParentId().equals(item.getId()))
                .map(subItem -> covert(subItem, allList)).collect(Collectors.toList());
        node.setChildren(children);
        return node;
    }
}
