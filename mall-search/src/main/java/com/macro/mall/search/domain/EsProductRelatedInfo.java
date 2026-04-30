package com.macro.mall.search.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 搜索商品关联信息实体 (Related Information Entity)
 * 用于存储搜索结果的品牌、分类、属性聚合数据，支持前端筛选条件展示
 */
@Data
@EqualsAndHashCode
public class EsProductRelatedInfo {
    private List<String> brandNames;  // 品牌名称列表（聚合结果）
    private List<String> productCategoryNames;  // 商品分类名称列表（聚合结果）
    private List<ProductAttr> productAttrs;  // 商品属性列表（嵌套聚合结果）

    /**
     * 商品属性内部类 (Inner Class)
     * 封装单个属性的 ID、名称和可选值列表
     */

    @Data
    @EqualsAndHashCode
    public static class ProductAttr {
        private Long attrId;  // 属性 ID
        private String attrName;  // 属性名称（如：颜色、尺码）
        private List<String> attrValues;  // 属性可选值列表（如：[红色, 蓝色, 绿色]）
    }
}
