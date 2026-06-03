package com.macro.mall.search.service.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.macro.mall.search.dao.EsProductDao;
import com.macro.mall.search.domain.EsProduct;
import com.macro.mall.search.domain.EsProductRelatedInfo;
import com.macro.mall.search.repository.EsProductRepository;
import com.macro.mall.search.service.EsProductService;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.AggregationsContainer;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 搜索商品管理服务实现类 (Search Product Management Service Implementation)
 * <p>
 * 实现基于 Elasticsearch 的商品索引管理、全文搜索、聚合分析等核心功能。
 * 使用 Spring Data Elasticsearch 5.x (ES 8.x 客户端) 的新 API。
 * </p>
 *
 * @author alan
 * @since 1.0
 */
@Service
public class EsProductServiceImpl implements EsProductService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsProductServiceImpl.class);

    // Elasticsearch 字段常量定义，用于构建查询和聚合
    private static final String FIELD_NAME = "name";
    private static final String FIELD_SUB_TITLE = "subTitle";
    private static final String FIELD_KEYWORDS = "keywords";
    private static final String FIELD_BRAND_ID = "brandId";
    private static final String FIELD_PRODUCT_CATEGORY_ID = "productCategoryId";
    private static final String FIELD_BRAND_NAME = "brandName";
    private static final String FIELD_PRODUCT_CATEGORY_NAME = "productCategoryName";
    private static final String FIELD_ID = "id";
    private static final String FIELD_PRICE = "price";
    private static final String FIELD_SALE = "sale";
    private static final String FIELD_ATTR_VALUE_LIST = "attrValueList";
    private static final String FIELD_ATTR_VALUES = "attrValues";
    private static final String FIELD_ATTR_NAMES = "attrNames";
    private static final String FIELD_ATTR_IDS = "attrIds";

    // Elasticsearch 聚合名称常量
    private static final String AGG_BRAND_NAMES = "brandNames";
    private static final String AGG_PRODUCT_CATEGORY_NAMES = "productCategoryNames";
    private static final String AGG_ALL_ATTR_VALUES = "allAttrValues";
    private static final String AGG_PRODUCT_ATTRS = "productAttrs";

    @Autowired
    private EsProductDao productDao;
    @Autowired
    private EsProductRepository productRepository;
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Override
    public int importAll() {
        int pageNum = 1;
        int pageSize = 500;
        int totalImported = 0;
        while (true) {
            PageHelper.startPage(pageNum, pageSize);
            List<EsProduct> esProductList = productDao.getAllEsProductList(null);
            if (CollectionUtils.isEmpty(esProductList)) {
                break;
            }
            productRepository.saveAll(esProductList);
            totalImported += esProductList.size();
            pageNum++;
            LOGGER.info("已导入 {} 条商品数据", totalImported);
        }
        return totalImported;
    }

    @Override
    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public EsProduct create(Long id) {
        EsProduct result = null;
        List<EsProduct> esProductList = productDao.getAllEsProductList(id);
        if (esProductList.size() > 0) {
            EsProduct esProduct = esProductList.get(0);
            result = productRepository.save(esProduct);
        }
        return result;
    }

    @Override
    public void delete(List<Long> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            List<EsProduct> esProductList = new ArrayList<>();
            for (Long id : ids) {
                EsProduct esProduct = new EsProduct();
                esProduct.setId(id);
                esProductList.add(esProduct);
            }
            productRepository.deleteAll(esProductList);
        }
    }

    @Override
    public Page<EsProduct> search(String keyword, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        Query query;
        if (StrUtil.isEmpty(keyword)) {
            query = Query.of(q -> q.matchAll(m -> m));
        } else {
            query = buildFunctionScoreQuery(keyword);
        }
        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(query)
                .withPageable(pageable)
                .withSort(s -> s.score(sc -> sc.order(SortOrder.Desc)))
                .build();
        LOGGER.info("DSL:{}", searchQuery.getQuery().toString());
        SearchHits<EsProduct> searchHits = elasticsearchOperations.search(searchQuery, EsProduct.class);
        if (searchHits.getTotalHits() <= 0) {
            return new PageImpl<>(ListUtil.empty(), pageable, 0);
        }
        List<EsProduct> searchProductList = searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
        return new PageImpl<>(searchProductList, pageable, searchHits.getTotalHits());
    }

    @Override
    public Page<EsProduct> search(String keyword, Long brandId, Long productCategoryId, Integer pageNum, Integer pageSize, Integer sort,
                                  BigDecimal startPrice, BigDecimal endPrice) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        // 构建过滤条件（不影响评分）
        List<Query> filterQueries = new ArrayList<>();
        if (brandId != null) {
            filterQueries.add(Query.of(q -> q.term(t -> t.field(FIELD_BRAND_ID).value(FieldValue.of(brandId)))));
        }
        if (productCategoryId != null) {
            filterQueries.add(Query.of(q -> q.term(t -> t.field(FIELD_PRODUCT_CATEGORY_ID).value(FieldValue.of(productCategoryId)))));
        }
        if (startPrice != null || endPrice != null) {
            Double gteValue = startPrice != null ? startPrice.doubleValue() : 0d;
            Double lteValue = endPrice != null ? endPrice.doubleValue() : Double.MAX_VALUE;
            Query rangeQuery = Query.of(q -> q.range(r -> r.number(n -> n
                    .field(FIELD_PRICE)
                    .gte(gteValue)
                    .lte(lteValue))));
            filterQueries.add(rangeQuery);
        }
        // 构建主查询
        Query mainQuery;
        if (StrUtil.isEmpty(keyword)) {
            mainQuery = Query.of(q -> q.matchAll(m -> m));
        } else {
            mainQuery = buildFunctionScoreQuery(keyword);
        }
        NativeQueryBuilder builder = NativeQuery.builder()
                .withQuery(mainQuery)
                .withPageable(pageable);
        // 添加 filter
        if (!filterQueries.isEmpty()) {
            BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
            for (Query filterQuery : filterQueries) {
                boolBuilder.filter(filterQuery);
            }
            builder.withFilter(Query.of(q -> q.bool(boolBuilder.build())));
        }
        if (sort != null) {
            switch (sort) {
                case 1:
                    builder.withSort(s -> s.field(f -> f.field(FIELD_ID).order(SortOrder.Desc)));
                    break;
                case 2:
                    builder.withSort(s -> s.field(f -> f.field(FIELD_SALE).order(SortOrder.Desc)));
                    break;
                case 3:
                    builder.withSort(s -> s.field(f -> f.field(FIELD_PRICE).order(SortOrder.Asc)));
                    break;
                case 4:
                    builder.withSort(s -> s.field(f -> f.field(FIELD_PRICE).order(SortOrder.Desc)));
                    break;
                default:
                    builder.withSort(s -> s.score(sc -> sc.order(SortOrder.Desc)));
            }
        } else {
            builder.withSort(s -> s.score(sc -> sc.order(SortOrder.Desc)));
        }
        NativeQuery searchQuery = builder.build();
        LOGGER.info("DSL:{}", searchQuery.getQuery().toString());
        SearchHits<EsProduct> searchHits = elasticsearchOperations.search(searchQuery, EsProduct.class);
        if (searchHits.getTotalHits() <= 0) {
            return new PageImpl<>(ListUtil.empty(), pageable, 0);
        }
        List<EsProduct> searchProductList = searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
        return new PageImpl<>(searchProductList, pageable, searchHits.getTotalHits());
    }

    @Override
    public Page<EsProduct> recommend(Long id, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        List<EsProduct> esProductList = productDao.getAllEsProductList(id);
        if (esProductList.size() > 0) {
            EsProduct esProduct = esProductList.get(0);
            String keyword = esProduct.getName();
            Long brandId = esProduct.getBrandId();
            Long productCategoryId = esProduct.getProductCategoryId();
            // 构建 Function Score Query
            Query functionScoreQuery = Query.of(q -> q
                    .functionScore(fs -> fs
                            .functions(f -> f
                                    .filter(Query.of(qq -> qq.match(m -> m.field(FIELD_NAME).query(keyword))))
                                    .weight(8.0))
                            .functions(f -> f
                                    .filter(Query.of(qq -> qq.match(m -> m.field(FIELD_SUB_TITLE).query(keyword))))
                                    .weight(3.0))
                            .functions(f -> f
                                    .filter(Query.of(qq -> qq.match(m -> m.field(FIELD_KEYWORDS).query(keyword))))
                                    .weight(5.0))
                            .functions(f -> f
                                    .filter(Query.of(qq -> qq.term(t -> t.field(FIELD_BRAND_ID).value(FieldValue.of(brandId)))))
                                    .weight(5.0))
                            .functions(f -> f
                                    .filter(Query.of(qq -> qq.term(t -> t.field(FIELD_PRODUCT_CATEGORY_ID).value(FieldValue.of(productCategoryId)))))
                                    .weight(3.0))
                            .scoreMode(FunctionScoreMode.Sum)
                            .boostMode(FunctionBoostMode.Multiply)
                            .minScore(2.0)
                    ));
            // 排除当前商品本身
            Query boolFilter = Query.of(q -> q.bool(b -> b
                    .mustNot(mn -> mn.term(t -> t.field(FIELD_ID).value(FieldValue.of(id))))
            ));
            NativeQuery searchQuery = NativeQuery.builder()
                    .withQuery(functionScoreQuery)
                    .withFilter(boolFilter)
                    .withPageable(pageable)
                    .build();
            LOGGER.info("DSL:{}", searchQuery.getQuery().toString());
            SearchHits<EsProduct> searchHits = elasticsearchOperations.search(searchQuery, EsProduct.class);
            if (searchHits.getTotalHits() <= 0) {
                return new PageImpl<>(ListUtil.empty(), pageable, 0);
            }
            List<EsProduct> searchProductList = searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
            return new PageImpl<>(searchProductList, pageable, searchHits.getTotalHits());
        }
        return new PageImpl<>(ListUtil.empty());
    }

    @Override
    public EsProductRelatedInfo searchRelatedInfo(String keyword) {
        Query mainQuery;
        if (StrUtil.isEmpty(keyword)) {
            mainQuery = Query.of(q -> q.matchAll(m -> m));
        } else {
            mainQuery = Query.of(q -> q.multiMatch(mm -> mm
                    .query(keyword)
                    .fields(FIELD_NAME, FIELD_SUB_TITLE, FIELD_KEYWORDS)));
        }
        // 构建品牌名称聚合
        Aggregation brandNamesAgg = Aggregation.of(a -> a
                .terms(t -> t.field(FIELD_BRAND_NAME)));
        // 构建分类名称聚合
        Aggregation productCategoryNamesAgg = Aggregation.of(a -> a
                .terms(t -> t.field(FIELD_PRODUCT_CATEGORY_NAME)));
        // 构建嵌套属性聚合
        Aggregation productAttrsAgg = Aggregation.of(a -> a
                .filter(f -> f.term(t -> t.field("attrValueList.type").value(FieldValue.of(1))))
                .aggregations(FIELD_ATTR_IDS, Aggregation.of(aa -> aa
                        .terms(t -> t.field("attrValueList.productAttributeId"))
                        .aggregations(FIELD_ATTR_VALUES, Aggregation.of(aaa -> aaa
                                .terms(t -> t.field("attrValueList.value"))))
                        .aggregations(FIELD_ATTR_NAMES, Aggregation.of(aaa -> aaa
                                .terms(t -> t.field("attrValueList.name"))))))
        );
        Aggregation allAttrValuesAgg = Aggregation.of(a -> a
                .nested(n -> n.path(FIELD_ATTR_VALUE_LIST))
                .aggregations(AGG_PRODUCT_ATTRS, productAttrsAgg));
        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(mainQuery)
                .withAggregation(AGG_BRAND_NAMES, brandNamesAgg)
                .withAggregation(AGG_PRODUCT_CATEGORY_NAMES, productCategoryNamesAgg)
                .withAggregation(AGG_ALL_ATTR_VALUES, allAttrValuesAgg)
                .withMaxResults(0)  // 只关心聚合
                .build();
        SearchHits<EsProduct> searchHits = elasticsearchOperations.search(searchQuery, EsProduct.class);
        return convertProductRelatedInfo(searchHits);
    }

    /**
     * 构建 Function Score Query，对商品名称、副标题、关键词进行加权搜索
     */
    private Query buildFunctionScoreQuery(String keyword) {
        return Query.of(q -> q
                .functionScore(fs -> fs
                        .functions(f -> f
                                .filter(Query.of(qq -> qq.match(m -> m.field(FIELD_NAME).query(keyword))))
                                .weight(10.0))
                        .functions(f -> f
                                .filter(Query.of(qq -> qq.match(m -> m.field(FIELD_SUB_TITLE).query(keyword))))
                                .weight(3.0))
                        .functions(f -> f
                                .filter(Query.of(qq -> qq.match(m -> m.field(FIELD_KEYWORDS).query(keyword))))
                                .weight(5.0))
                        .scoreMode(FunctionScoreMode.Sum)
                        .boostMode(FunctionBoostMode.Multiply)
                        .minScore(0.5)
                ));
    }

    /**
     * 将 Elasticsearch 聚合结果转换为 EsProductRelatedInfo 对象
     */
    private EsProductRelatedInfo convertProductRelatedInfo(SearchHits<EsProduct> response) {
        EsProductRelatedInfo productRelatedInfo = new EsProductRelatedInfo();
        AggregationsContainer<?> aggregationsContainer = response.getAggregations();
        if (aggregationsContainer == null) {
            return productRelatedInfo;
        }
        // 获取聚合 Map: 顶层聚合名 -> 聚合对象（Spring Data 包装）
        Map<String, org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation> aggregationMap =
                ((org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations) aggregationsContainer).aggregationsAsMap();
        // 提取品牌名称列表
        List<String> brandNameList = new ArrayList<>();
        org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation brandNamesAgg = aggregationMap.get(AGG_BRAND_NAMES);
        if (brandNamesAgg != null) {
            Aggregate aggregate = brandNamesAgg.aggregation().getAggregate();
            if (aggregate.isSterms()) {
                List<StringTermsBucket> buckets = aggregate.sterms().buckets().array();
                for (StringTermsBucket bucket : buckets) {
                    brandNameList.add(bucket.key().stringValue());
                }
            }
        }
        productRelatedInfo.setBrandNames(brandNameList);
        // 提取分类名称列表
        List<String> productCategoryNameList = new ArrayList<>();
        org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation productCategoryNamesAgg = aggregationMap.get(AGG_PRODUCT_CATEGORY_NAMES);
        if (productCategoryNamesAgg != null) {
            Aggregate aggregate = productCategoryNamesAgg.aggregation().getAggregate();
            if (aggregate.isSterms()) {
                List<StringTermsBucket> buckets = aggregate.sterms().buckets().array();
                for (StringTermsBucket bucket : buckets) {
                    productCategoryNameList.add(bucket.key().stringValue());
                }
            }
        }
        productRelatedInfo.setProductCategoryNames(productCategoryNameList);
        // 提取嵌套属性聚合结果
        // allAttrValues (nested) -> productAttrs (filter) -> attrIds (terms) -> attrValues + attrNames
        List<EsProductRelatedInfo.ProductAttr> attrList = new ArrayList<>();
        org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation allAttrValuesAgg = aggregationMap.get(AGG_ALL_ATTR_VALUES);
        if (allAttrValuesAgg != null && allAttrValuesAgg.aggregation().getAggregate().isNested()) {
            // 从 nested 聚合的子聚合中获取 productAttrs (filter)
            Map<String, Aggregate> nestedSubAggs = allAttrValuesAgg.aggregation().getAggregate().nested().aggregations();
            Aggregate productAttrsAgg = nestedSubAggs.get(AGG_PRODUCT_ATTRS);
            if (productAttrsAgg != null && productAttrsAgg.isFilter()) {
                // 从 filter 聚合的子聚合中获取 attrIds (terms)
                Map<String, Aggregate> filterSubAggs = productAttrsAgg.filter().aggregations();
                Aggregate attrIdsAgg = filterSubAggs.get(FIELD_ATTR_IDS);
                if (attrIdsAgg != null && attrIdsAgg.isLterms()) {
                    List<LongTermsBucket> buckets = attrIdsAgg.lterms().buckets().array();
                    for (LongTermsBucket bucket : buckets) {
                        EsProductRelatedInfo.ProductAttr attr = new EsProductRelatedInfo.ProductAttr();
                        attr.setAttrId((Long) bucket.key());
                        // 提取属性值列表
                        List<String> attrValueList = new ArrayList<>();
                        Map<String, Aggregate> subAggs = bucket.aggregations();
                        Aggregate attrValAgg = subAggs.get(FIELD_ATTR_VALUES);
                        if (attrValAgg != null && attrValAgg.isSterms()) {
                            for (StringTermsBucket attrValueBucket : attrValAgg.sterms().buckets().array()) {
                                attrValueList.add(attrValueBucket.key().stringValue());
                            }
                        }
                        attr.setAttrValues(attrValueList);
                        // 提取属性名称（取第一个桶的值）
                        Aggregate attrNameAgg = subAggs.get(FIELD_ATTR_NAMES);
                        if (attrNameAgg != null && attrNameAgg.isSterms()) {
                            List<StringTermsBucket> nameBuckets = attrNameAgg.sterms().buckets().array();
                            if (!nameBuckets.isEmpty()) {
                                attr.setAttrName(nameBuckets.get(0).key().stringValue());
                            }
                        }
                        attrList.add(attr);
                    }
                }
            }
        }
        productRelatedInfo.setProductAttrs(attrList);
        return productRelatedInfo;
    }
}