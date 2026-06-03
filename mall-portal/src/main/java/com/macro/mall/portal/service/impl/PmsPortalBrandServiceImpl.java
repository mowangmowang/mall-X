package com.macro.mall.portal.service.impl;

import com.github.pagehelper.PageHelper;
import com.macro.mall.common.api.CommonPage;
import com.macro.mall.mapper.PmsBrandMapper;
import com.macro.mall.mapper.PmsProductMapper;
import com.macro.mall.model.PmsBrand;
import com.macro.mall.model.PmsProduct;
import com.macro.mall.model.PmsProductExample;
import com.macro.mall.portal.dao.HomeDao;
import com.macro.mall.portal.service.PmsPortalBrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 前台品牌管理Service实现类 (Portal Brand Service Implementation)
 * <p>
 * 负责前台用户端的品牌相关查询操作，包括推荐品牌、品牌详情和品牌商品列表。
 */
@Service
public class PmsPortalBrandServiceImpl implements PmsPortalBrandService {
    /** 首页DAO，用于查询推荐品牌等首页数据 */
    @Autowired
    private HomeDao homeDao;
    
    /** 品牌Mapper，用于品牌数据的持久化操作 */
    @Autowired
    private PmsBrandMapper brandMapper;
    
    /** 商品Mapper，用于查询品牌下的商品列表 */
    @Autowired
    private PmsProductMapper productMapper;

    /**
     * 查询推荐品牌列表
     * <p>
     * 获取首页展示的品牌推荐列表，按推荐排序
     *
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 推荐品牌列表
     */
    @Override
    public List<PmsBrand> recommendList(Integer pageNum, Integer pageSize) {
        int offset = (pageNum - 1) * pageSize;
        return homeDao.getRecommendBrandList(offset, pageSize);
    }

    /**
     * 查询品牌详情
     * <p>
     * 根据品牌ID获取品牌的详细信息
     *
     * @param brandId 品牌唯一标识符 (Brand ID)
     * @return 品牌详情对象
     */
    @Override
    public PmsBrand detail(Long brandId) {
        return brandMapper.selectByPrimaryKey(brandId);
    }

    /**
     * 分页查询品牌下的商品列表
     * <p>
     * 获取指定品牌的所有已发布且未删除的商品
     *
     * @param brandId 品牌唯一标识符 (Brand ID)
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页后的商品列表
     */
    @Override
    public CommonPage<PmsProduct> productList(Long brandId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        PmsProductExample example = new PmsProductExample();
        example.createCriteria().andDeleteStatusEqualTo(0)
                .andPublishStatusEqualTo(1)
                .andBrandIdEqualTo(brandId);
        List<PmsProduct> productList = productMapper.selectByExample(example);
        return CommonPage.restPage(productList);
    }
}
