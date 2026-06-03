package com.macro.mall.search.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;

/**
 * Elasticsearch 商品属性值实体 (Product Attribute Value Entity)
 * <p>
 * 作为 EsProduct 的嵌套类型 (Nested Type)，用于支持属性筛选和聚合分析。
 * 嵌套类型允许对数组中的对象进行独立查询，避免对象之间的字段混淆。
 * </p>
 * <p>
 * 使用场景：
 * <ul>
 *   <li>属性筛选：如按颜色、尺码等规格筛选商品</li>
 *   <li>聚合分析：统计某个属性下的可选值分布</li>
 * </ul>
 * </p>
 *
 * @author alan
 * @since 1.0
 */
@Data
@EqualsAndHashCode
public class EsProductAttributeValue implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;  // 属性值 ID (Attribute Value ID)
    private Long productAttributeId;  // 商品属性 ID (Product Attribute ID)
    @Field(type = FieldType.Keyword)  // 精确匹配，不分词 (Exact Match, No Tokenization)
    private String value;  // 属性值（如：红色、XL） (Attribute Value, e.g., Red, XL)
    private Integer type;  // 属性类型：0->规格；1->参数 (Attribute Type: 0->Specification; 1->Parameter)
    @Field(type=FieldType.Keyword)  // 精确匹配，不分词
    private String name;  // 属性名称（如：颜色、尺码） (Attribute Name, e.g., Color, Size)
}
