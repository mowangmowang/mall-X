package com.macro.mall.dto;

import com.macro.mall.model.*;
import io.swagger.v3.oas.annotations.media.Schema;
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
     * 商品阶梯价格设置（例如：购买 2-5 件打 9 折）
     */
    @Schema(description = "商品阶梯价格列表")
    private List<PmsProductLadder> productLadderList;
    
    /**
     * 商品满减优惠设置（例如：满 100 元减 20 元）
     */
    @Schema(description = "商品满减优惠列表")
    private List<PmsProductFullReduction> productFullReductionList;
    
    /**
     * 不同会员等级的专属价格设置
     */
    @Schema(description = "会员价格列表")
    private List<PmsMemberPrice> memberPriceList;
    
    /**
     * 商品的 SKU (Stock Keeping Unit) 库存及规格信息
     */
    @Schema(description = "SKU 库存列表")
    private List<PmsSkuStock> skuStockList;
    
    /**
     * 商品参数值及自定义规格属性（如颜色、尺寸等具体取值）
     */
    @Schema(description = "商品属性值列表")
    private List<PmsProductAttributeValue> productAttributeValueList;
    
    /**
     * 商品与专题 (Subject) 的关联关系
     */
    @Schema(description = "专题关联列表")
    private List<CmsSubjectProductRelation> subjectProductRelationList;
    
    /**
     * 商品与优选专区 (Prefrence Area) 的关联关系
     */
    @Schema(description = "优选专区关联列表")
    private List<CmsPrefrenceAreaProductRelation> prefrenceAreaProductRelationList;
}