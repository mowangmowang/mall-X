package com.macro.mall.dto;

import com.macro.mall.model.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 创建和修改商品的请求参数 DTO
 * 继承自 PmsProduct，扩展了商品相关的关联数据
 * 用于商品的新增和编辑操作
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PmsProductParam extends PmsProduct {
    /**
     * 商品阶梯价格设置（如：满2件打9折）
     */
    @ApiModelProperty("商品阶梯价格设置")
    private List<PmsProductLadder> productLadderList;
    
    /**
     * 商品满减价格设置（如：满100减20）
     */
    @ApiModelProperty("商品满减价格设置")
    private List<PmsProductFullReduction> productFullReductionList;
    
    /**
     * 商品会员价格设置（不同会员等级的价格）
     */
    @ApiModelProperty("商品会员价格设置")
    private List<PmsMemberPrice> memberPriceList;
    
    /**
     * 商品的 SKU 库存信息（规格-库存组合）
     */
    @ApiModelProperty("商品的sku库存信息")
    private List<PmsSkuStock> skuStockList;
    
    /**
     * 商品参数及自定义规格属性值
     */
    @ApiModelProperty("商品参数及自定义规格属性")
    private List<PmsProductAttributeValue> productAttributeValueList;
    
    /**
     * 专题和商品的关联关系
     */
    @ApiModelProperty("专题和商品关系")
    private List<CmsSubjectProductRelation> subjectProductRelationList;
    
    /**
     * 优选专区和商品的关联关系
     */
    @ApiModelProperty("优选专区和商品的关系")
    private List<CmsPrefrenceAreaProductRelation> prefrenceAreaProductRelationList;
}
