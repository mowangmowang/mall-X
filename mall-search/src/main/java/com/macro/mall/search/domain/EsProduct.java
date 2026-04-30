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
 * Elasticsearch 商品文档实体 (Document Entity)
 * 映射到 Elasticsearch 索引 "pms"，用于全文搜索和聚合分析
 * 使用 IK 分词器对中文文本字段进行分词处理
 */
@Data
@EqualsAndHashCode
@Document(indexName = "pms")
@Setting(shards = 1,replicas = 0)
public class EsProduct implements Serializable {
    private static final long serialVersionUID = -1L;
    @Id  // Elasticsearch 文档 ID
    private Long id;
    @Field(type = FieldType.Keyword)  // 精确匹配，不分词
    private String productSn;  // 商品编码
    private Long brandId;  // 品牌 ID
    @Field(type = FieldType.Keyword)  // 精确匹配，不分词
    private String brandName;  // 品牌名称
    private Long productCategoryId;  // 商品分类 ID
    @Field(type = FieldType.Keyword)  // 精确匹配，不分词
    private String productCategoryName;  // 分类名称
    private String pic;  // 商品主图 URL
    @Field(analyzer = "ik_max_word",type = FieldType.Text)  // 使用 IK 分词器，支持全文搜索
    private String name;  // 商品名称
    @Field(analyzer = "ik_max_word",type = FieldType.Text)  // 使用 IK 分词器，支持全文搜索
    private String subTitle;  // 副标题
    @Field(analyzer = "ik_max_word",type = FieldType.Text)  // 使用 IK 分词器，支持全文搜索
    private String keywords;  // 关键词
    private BigDecimal price;  // 销售价格
    private Integer sale;  // 销量
    private Integer newStatus;  // 是否新品：0->否；1->是
    private Integer recommandStatus;  // 是否推荐：0->否；1->是
    private Integer stock;  // 库存
    private Integer promotionType;  // 促销类型
    private Integer sort;  // 排序字段
    @Field(type = FieldType.Nested, fielddata = true)  // 嵌套类型，用于属性筛选和聚合
    private List<EsProductAttributeValue> attrValueList;  // 商品属性值列表
}
