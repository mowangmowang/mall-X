package com.macro.mall.search.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;

/**
 * Elasticsearch 商品属性值实体 (Attribute Value Entity)
 * 作为 EsProduct 的嵌套类型 (Nested Type)，用于支持属性筛选和聚合分析
 */
@Data
@EqualsAndHashCode
public class EsProductAttributeValue implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;  // 属性值 ID
    private Long productAttributeId;  // 商品属性 ID
    @Field(type = FieldType.Keyword)  // 精确匹配，不分词
    private String value;  // 属性值（如：红色、XL）
    private Integer type;  // 属性类型：0->规格；1->参数
    @Field(type=FieldType.Keyword)  // 精确匹配，不分词
    private String name;  // 属性名称（如：颜色、尺码）
}
