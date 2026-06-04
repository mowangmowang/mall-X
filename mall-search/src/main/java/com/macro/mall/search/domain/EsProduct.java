package com.macro.mall.search.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Elasticsearch 商品文档实体 (Elasticsearch Product Document Entity)
 * <p>
 * 映射到 Elasticsearch 索引 "pms"，用于全文搜索和聚合分析。
 * 使用 IK 分词器对中文文本字段进行分词处理，支持高效的中文搜索。
 * </p>
 * <p>
 * 索引配置：
 * <ul>
 *   <li>分片数 (Shards)：1</li>
 *   <li>副本数 (Replicas)：0（单节点环境）</li>
 *   <li>分词器 (Analyzer)：ik_max_word（索引，IK 细粒度切分，高召回）+ ik_smart（搜索，IK 粗粒度切分，高精确）</li>
 * </ul>
 * </p>
 *
 * @author alan
 * @since 1.0
 */
@Data
@EqualsAndHashCode
@Document(indexName = "pms")
@Setting(shards = 1,replicas = 0)
public class EsProduct implements Serializable {
    private static final long serialVersionUID = -1L;
    @Id  // Elasticsearch 文档 ID (Document ID)
    private Long id;
    @Field(type = FieldType.Keyword)  // 精确匹配，不分词 (Exact Match, No Tokenization)
    private String productSn;  // 商品编码 (Product Serial Number)
    private Long brandId;  // 品牌 ID (Brand ID)
    @Field(type = FieldType.Keyword)  // 精确匹配，不分词
    private String brandName;  // 品牌名称 (Brand Name)
    private Long productCategoryId;  // 商品分类 ID (Product Category ID)
    @Field(type = FieldType.Keyword)  // 精确匹配，不分词
    private String productCategoryName;  // 分类名称 (Category Name)
    private String pic;  // 商品主图 URL (Main Image URL)
    @Field(analyzer = "ik_max_word", searchAnalyzer = "ik_smart", type = FieldType.Text)  // IK 索引时细粒度切分（高召回），搜索时粗粒度切分（高精确）
    private String name;  // 商品名称 (Product Name)
    @Field(analyzer = "ik_max_word", searchAnalyzer = "ik_smart", type = FieldType.Text)  // IK 分词器，支持全文搜索
    private String subTitle;  // 副标题 (Sub Title)
    @Field(analyzer = "ik_max_word", searchAnalyzer = "ik_smart", type = FieldType.Text)  // IK 分词器，支持全文搜索
    private String keywords;  // 关键词 (Keywords)
    private BigDecimal price;  // 销售价格 (Sale Price)
    private Integer sale;  // 销量 (Sales Volume)
    private Integer newStatus;  // 是否新品：0->否；1->是 (New Product Status: 0->No; 1->Yes)
    private Integer recommandStatus;  // 是否推荐：0->否；1->是 (Recommendation Status: 0->No; 1->Yes)
    private Integer stock;  // 库存 (Stock Quantity)
    private Integer promotionType;  // 促销类型 (Promotion Type)
    private Integer sort;  // 排序字段 (Sort Order)
    @Field(type = FieldType.Nested, fielddata = true)  // 嵌套类型，用于属性筛选和聚合 (Nested Type for Attribute Filtering & Aggregation)
    private List<EsProductAttributeValue> attrValueList;  // 商品属性值列表 (Product Attribute Values)
}
