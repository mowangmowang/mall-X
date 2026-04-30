package com.macro.mall.search.service.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.macro.mall.search.dao.EsProductDao;
import com.macro.mall.search.domain.EsProduct;
import com.macro.mall.search.domain.EsProductRelatedInfo;
import com.macro.mall.search.repository.EsProductRepository;
import com.macro.mall.search.service.EsProductService;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.filter.ParsedFilter;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 搜索商品管理服务实现类 (Service Implementation)
 * 实现基于 Elasticsearch 的商品索引管理、全文搜索、聚合分析等核心功能
 */
@Service
public class EsProductServiceImpl implements EsProductService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsProductServiceImpl.class);

    // Elasticsearch 字段常量定义，用于构建查询和聚合
    private static final String FIELD_NAME = "name";  // 商品名称
    private static final String FIELD_SUB_TITLE = "subTitle";  // 副标题
    private static final String FIELD_KEYWORDS = "keywords";  // 关键词
    private static final String FIELD_BRAND_ID = "brandId";  // 品牌 ID
    private static final String FIELD_PRODUCT_CATEGORY_ID = "productCategoryId";  // 分类 ID
    private static final String FIELD_BRAND_NAME = "brandName";  // 品牌名称
    private static final String FIELD_PRODUCT_CATEGORY_NAME = "productCategoryName";  // 分类名称
    private static final String FIELD_ID = "id";  // 商品 ID
    private static final String FIELD_PRICE = "price";  // 价格
    private static final String FIELD_SALE = "sale";  // 销量
    private static final String FIELD_ATTR_VALUE_LIST = "attrValueList";  // 属性值列表（嵌套类型）
    private static final String FIELD_ATTR_VALUES = "attrValues";  // 属性值
    private static final String FIELD_ATTR_NAMES = "attrNames";  // 属性名称
    private static final String FIELD_ATTR_IDS = "attrIds";  // 属性 ID
    
    // Elasticsearch 聚合名称常量
    private static final String AGG_BRAND_NAMES = "brandNames";  // 品牌名称聚合
    private static final String AGG_PRODUCT_CATEGORY_NAMES = "productCategoryNames";  // 分类名称聚合
    private static final String AGG_ALL_ATTR_VALUES = "allAttrValues";  // 所有属性值聚合
    private static final String AGG_PRODUCT_ATTRS = "productAttrs";  // 商品属性聚合

    @Autowired
    private EsProductDao productDao;  // MyBatis DAO，用于从 MySQL 查询商品数据
    @Autowired
    private EsProductRepository productRepository;  // Spring Data Elasticsearch Repository，提供基础 CRUD 操作
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;  // Elasticsearch 模板类，用于执行复杂查询和聚合

    /**
     * 从 MySQL 数据库批量导入商品到 Elasticsearch
     * 采用分页查询避免内存溢出，每批处理 500 条记录
     * @return 成功导入的商品总数
     */
    @Override
    public int importAll() {
        int pageNum = 1;
        int pageSize = 500;
        int totalImported = 0;
        while (true) {
            // 使用 PageHelper 进行分页查询
            PageHelper.startPage(pageNum, pageSize);
            List<EsProduct> esProductList = productDao.getAllEsProductList(null);
            if (CollectionUtils.isEmpty(esProductList)) {
                break;  // 无更多数据，退出循环
            }
            // 批量保存到 Elasticsearch
            productRepository.saveAll(esProductList);
            totalImported += esProductList.size();
            pageNum++;
            LOGGER.info("已导入 {} 条商品数据", totalImported);
        }
        return totalImported;
    }

    /**
     * 根据商品 ID 删除 Elasticsearch 索引文档
     * @param id 商品唯一标识符
     */
    @Override
    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    /**
     * 根据商品 ID 从 MySQL 查询并创建/更新 Elasticsearch 索引
     * @param id 商品唯一标识符
     * @return 保存后的 EsProduct 对象，若商品不存在则返回 null
     */
    @Override
    public EsProduct create(Long id) {
        EsProduct result = null;
        List<EsProduct> esProductList = productDao.getAllEsProductList(id);
        if (esProductList.size() > 0) {
            EsProduct esProduct = esProductList.get(0);
            result = productRepository.save(esProduct);  // save 方法会根据 ID 自动判断是新增还是更新
        }
        return result;
    }

    /**
     * 批量删除多个商品的 Elasticsearch 索引
     * @param ids 商品 ID 列表
     */
    @Override
    public void delete(List<Long> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            List<EsProduct> esProductList = new ArrayList<>();
            for (Long id : ids) {
                EsProduct esProduct = new EsProduct();
                esProduct.setId(id);
                esProductList.add(esProduct);
            }
            productRepository.deleteAll(esProductList);  // 批量删除
        }
    }

    /**
     * 简单搜索：根据关键字匹配商品名称、副标题或关键词
     * 使用 Function Score Query 对不同字段进行加权评分
     * @param keyword 搜索关键字
     * @param pageNum 页码（从 0 开始）
     * @param pageSize 每页大小
     * @return 按相关度排序的分页搜索结果
     */
    @Override
    public Page<EsProduct> search(String keyword, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.withPageable(pageable);
        if (StrUtil.isEmpty(keyword)) {
            nativeSearchQueryBuilder.withQuery(QueryBuilders.matchAllQuery());  // 无关键字时返回所有商品
        } else {
            nativeSearchQueryBuilder.withQuery(buildFunctionScoreQuery(keyword));  // 使用加权查询
        }
        // 按相关度分数降序排序
        nativeSearchQueryBuilder.withSorts(SortBuilders.scoreSort().order(SortOrder.DESC));
        NativeSearchQuery searchQuery = nativeSearchQueryBuilder.build();
        LOGGER.info("DSL:{}", searchQuery.getQuery().toString());
        SearchHits<EsProduct> searchHits = elasticsearchRestTemplate.search(searchQuery, EsProduct.class);
        if (searchHits.getTotalHits() <= 0) {
            return new PageImpl<>(ListUtil.empty(), pageable, 0);
        }
        List<EsProduct> searchProductList = searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
        return new PageImpl<>(searchProductList, pageable, searchHits.getTotalHits());
    }

    /**
     * 综合搜索：支持关键字、品牌、分类筛选、价格区间过滤及多种排序策略
     * @param keyword 搜索关键字
     * @param brandId 品牌 ID（可选筛选条件）
     * @param productCategoryId 商品分类 ID（可选筛选条件）
     * @param pageNum 页码（从 0 开始）
     * @param pageSize 每页大小
     * @param sort 排序方式：0->相关度；1->新品；2->销量；3->价格升序；4->价格降序
     * @param startPrice 价格区间下限（可选）
     * @param endPrice 价格区间上限（可选）
     * @return 分页的商品搜索结果
     */
    @Override
    public Page<EsProduct> search(String keyword, Long brandId, Long productCategoryId, Integer pageNum, Integer pageSize, Integer sort,
                                  BigDecimal startPrice, BigDecimal endPrice) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.withPageable(pageable);
        // 构建布尔查询：品牌、分类、价格区间作为过滤条件（不影响评分）
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (brandId != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery(FIELD_BRAND_ID, brandId));  // 精确匹配品牌
        }
        if (productCategoryId != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery(FIELD_PRODUCT_CATEGORY_ID, productCategoryId));  // 精确匹配分类
        }
        if (startPrice != null || endPrice != null) {
            boolQueryBuilder.must(QueryBuilders.rangeQuery(FIELD_PRICE)
                    .gte(startPrice != null ? startPrice : 0)
                    .lte(endPrice != null ? endPrice : Double.MAX_VALUE));  // 价格区间过滤
        }
        if (boolQueryBuilder.must().size() > 0) {
            nativeSearchQueryBuilder.withFilter(boolQueryBuilder);  // 使用 filter 上下文，性能更优
        }
        // 处理关键字查询
        if (StrUtil.isEmpty(keyword)) {
            nativeSearchQueryBuilder.withQuery(QueryBuilders.matchAllQuery());
        } else {
            nativeSearchQueryBuilder.withQuery(buildFunctionScoreQuery(keyword));
        }
        // 根据 sort 参数应用不同的排序策略
        if (sort == 1) {
            nativeSearchQueryBuilder.withSorts(SortBuilders.fieldSort(FIELD_ID).order(SortOrder.DESC));  // 按新品（ID 降序）
        } else if (sort == 2) {
            nativeSearchQueryBuilder.withSorts(SortBuilders.fieldSort(FIELD_SALE).order(SortOrder.DESC));  // 按销量降序
        } else if (sort == 3) {
            nativeSearchQueryBuilder.withSorts(SortBuilders.fieldSort(FIELD_PRICE).order(SortOrder.ASC));  // 价格升序
        } else if (sort == 4) {
            nativeSearchQueryBuilder.withSorts(SortBuilders.fieldSort(FIELD_PRICE).order(SortOrder.DESC));  // 价格降序
        } else {
            nativeSearchQueryBuilder.withSorts(SortBuilders.scoreSort().order(SortOrder.DESC));  // 默认按相关度
        }
        NativeSearchQuery searchQuery = nativeSearchQueryBuilder.build();
        LOGGER.info("DSL:{}", searchQuery.getQuery().toString());
        SearchHits<EsProduct> searchHits = elasticsearchRestTemplate.search(searchQuery, EsProduct.class);
        if (searchHits.getTotalHits() <= 0) {
            return new PageImpl<>(ListUtil.empty(), pageable, 0);
        }
        List<EsProduct> searchProductList = searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
        return new PageImpl<>(searchProductList, pageable, searchHits.getTotalHits());
    }

    /**
     * 基于商品 ID 推荐相似商品
     * 根据参考商品的名称、品牌、分类进行加权匹配，排除自身
     * @param id 参考商品 ID
     * @param pageNum 页码（从 0 开始）
     * @param pageSize 每页大小
     * @return 分页的推荐商品列表
     */
    @Override
    public Page<EsProduct> recommend(Long id, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        List<EsProduct> esProductList = productDao.getAllEsProductList(id);
        if (esProductList.size() > 0) {
            EsProduct esProduct = esProductList.get(0);
            String keyword = esProduct.getName();
            Long brandId = esProduct.getBrandId();
            Long productCategoryId = esProduct.getProductCategoryId();
            // 构建 Function Score Query：对不同字段设置不同权重
            List<FunctionScoreQueryBuilder.FilterFunctionBuilder> filterFunctionBuilders = new ArrayList<>();
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery(FIELD_NAME, keyword),
                    ScoreFunctionBuilders.weightFactorFunction(8)));  // 名称匹配权重 8
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery(FIELD_SUB_TITLE, keyword),
                    ScoreFunctionBuilders.weightFactorFunction(3)));  // 副标题权重 3
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.matchQuery(FIELD_KEYWORDS, keyword),
                    ScoreFunctionBuilders.weightFactorFunction(5)));  // 关键词权重 5
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.termQuery(FIELD_BRAND_ID, brandId),
                    ScoreFunctionBuilders.weightFactorFunction(5)));  // 同品牌权重 5
            filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.termQuery(FIELD_PRODUCT_CATEGORY_ID, productCategoryId),
                    ScoreFunctionBuilders.weightFactorFunction(3)));  // 同分类权重 3
            FunctionScoreQueryBuilder.FilterFunctionBuilder[] builders = new FunctionScoreQueryBuilder.FilterFunctionBuilder[filterFunctionBuilders.size()];
            filterFunctionBuilders.toArray(builders);
            FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(builders)
                    .scoreMode(FunctionScoreQuery.ScoreMode.SUM)  // 分数累加模式
                    .setMinScore(2);  // 最低分数阈值，过滤不相关结果
            // 排除当前商品本身
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            boolQueryBuilder.mustNot(QueryBuilders.termQuery(FIELD_ID, id));
            NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
            builder.withQuery(functionScoreQueryBuilder);
            builder.withFilter(boolQueryBuilder);
            builder.withPageable(pageable);
            NativeSearchQuery searchQuery = builder.build();
            LOGGER.info("DSL:{}", searchQuery.getQuery().toString());
            SearchHits<EsProduct> searchHits = elasticsearchRestTemplate.search(searchQuery, EsProduct.class);
            if (searchHits.getTotalHits() <= 0) {
                return new PageImpl<>(ListUtil.empty(), pageable, 0);
            }
            List<EsProduct> searchProductList = searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
            return new PageImpl<>(searchProductList, pageable, searchHits.getTotalHits());
        }
        return new PageImpl<>(ListUtil.empty());
    }

    /**
     * 获取搜索关键字相关的聚合信息：品牌列表、分类列表、属性筛选条件
     * 使用 Elasticsearch Aggregation 进行数据统计分析
     * @param keyword 搜索关键字
     * @return 包含品牌、分类、属性的关联信息对象
     */
    @Override
    public EsProductRelatedInfo searchRelatedInfo(String keyword) {
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        // 构建查询条件
        if (StrUtil.isEmpty(keyword)) {
            builder.withQuery(QueryBuilders.matchAllQuery());
        } else {
            builder.withQuery(QueryBuilders.multiMatchQuery(keyword, FIELD_NAME, FIELD_SUB_TITLE, FIELD_KEYWORDS));
        }
        // 添加品牌名称聚合
        builder.withAggregations(AggregationBuilders.terms(AGG_BRAND_NAMES).field(FIELD_BRAND_NAME));
        // 添加分类名称聚合
        builder.withAggregations(AggregationBuilders.terms(AGG_PRODUCT_CATEGORY_NAMES).field(FIELD_PRODUCT_CATEGORY_NAME));
        // 添加嵌套属性聚合：先过滤出参数类型（type=1），再按属性 ID、值、名称分组
        AbstractAggregationBuilder aggregationBuilder = AggregationBuilders.nested(AGG_ALL_ATTR_VALUES, FIELD_ATTR_VALUE_LIST)
                .subAggregation(AggregationBuilders.filter(AGG_PRODUCT_ATTRS, QueryBuilders.termQuery("attrValueList.type", 1))
                        .subAggregation(AggregationBuilders.terms(FIELD_ATTR_IDS)
                                .field("attrValueList.productAttributeId")
                                .subAggregation(AggregationBuilders.terms(FIELD_ATTR_VALUES)
                                        .field("attrValueList.value"))
                                .subAggregation(AggregationBuilders.terms(FIELD_ATTR_NAMES)
                                        .field("attrValueList.name"))));
        builder.withAggregations(aggregationBuilder);
        NativeSearchQuery searchQuery = builder.build();
        SearchHits<EsProduct> searchHits = elasticsearchRestTemplate.search(searchQuery, EsProduct.class);
        return convertProductRelatedInfo(searchHits);  // 转换聚合结果
    }

    /**
     * 构建 Function Score Query，对商品名称、副标题、关键词进行加权搜索
     * 不同字段的权重设置：名称(10) > 关键词(5) > 副标题(3)
     * @param keyword 搜索关键字
     * @return FunctionScoreQueryBuilder 查询构建器
     */
    private FunctionScoreQueryBuilder buildFunctionScoreQuery(String keyword) {
        List<FunctionScoreQueryBuilder.FilterFunctionBuilder> filterFunctionBuilders = new ArrayList<>();
        // 商品名称匹配，权重最高（10）
        filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                QueryBuilders.matchQuery(FIELD_NAME, keyword),
                ScoreFunctionBuilders.weightFactorFunction(10)));
        // 副标题匹配，权重较低（3）
        filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                QueryBuilders.matchQuery(FIELD_SUB_TITLE, keyword),
                ScoreFunctionBuilders.weightFactorFunction(3)));
        // 关键词匹配，权重中等（5）
        filterFunctionBuilders.add(new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                QueryBuilders.matchQuery(FIELD_KEYWORDS, keyword),
                ScoreFunctionBuilders.weightFactorFunction(5)));
        FunctionScoreQueryBuilder.FilterFunctionBuilder[] builders =
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[filterFunctionBuilders.size()];
        filterFunctionBuilders.toArray(builders);
        return QueryBuilders.functionScoreQuery(builders)
                .scoreMode(FunctionScoreQuery.ScoreMode.SUM)  // 分数累加模式
                .setMinScore(0.5f);  // 最低分数阈值，过滤低相关度结果
    }

    /**
     * 将 Elasticsearch 聚合结果转换为 EsProductRelatedInfo 对象
     * 解析品牌、分类、属性的聚合数据
     * @param response Elasticsearch 搜索结果（包含聚合信息）
     * @return 转换后的关联信息对象
     */
    private EsProductRelatedInfo convertProductRelatedInfo(SearchHits<EsProduct> response) {
        EsProductRelatedInfo productRelatedInfo = new EsProductRelatedInfo();
        Map<String, Aggregation> aggregationMap = ((Aggregations) response.getAggregations().aggregations()).asMap();
        // 提取品牌名称列表
        Aggregation brandNames = aggregationMap.get(AGG_BRAND_NAMES);
        List<String> brandNameList = new ArrayList<>();
        for (int i = 0; i < ((Terms) brandNames).getBuckets().size(); i++) {
            brandNameList.add(((Terms) brandNames).getBuckets().get(i).getKeyAsString());
        }
        productRelatedInfo.setBrandNames(brandNameList);
        // 提取分类名称列表
        Aggregation productCategoryNames = aggregationMap.get(AGG_PRODUCT_CATEGORY_NAMES);
        List<String> productCategoryNameList = new ArrayList<>();
        for (int i = 0; i < ((Terms) productCategoryNames).getBuckets().size(); i++) {
            productCategoryNameList.add(((Terms) productCategoryNames).getBuckets().get(i).getKeyAsString());
        }
        productRelatedInfo.setProductCategoryNames(productCategoryNameList);
        // 提取嵌套属性聚合结果：attrId -> attrValues + attrName
        Aggregation productAttrs = aggregationMap.get(AGG_ALL_ATTR_VALUES);
        List<? extends Terms.Bucket> attrIds = ((ParsedLongTerms) ((ParsedFilter) ((ParsedNested) productAttrs)
                .getAggregations().get(AGG_PRODUCT_ATTRS)).getAggregations().get(FIELD_ATTR_IDS)).getBuckets();
        List<EsProductRelatedInfo.ProductAttr> attrList = new ArrayList<>();
        for (Terms.Bucket attrId : attrIds) {
            EsProductRelatedInfo.ProductAttr attr = new EsProductRelatedInfo.ProductAttr();
            attr.setAttrId((Long) attrId.getKey());  // 设置属性 ID
            // 提取属性值列表
            List<String> attrValueList = new ArrayList<>();
            List<? extends Terms.Bucket> attrValues = ((ParsedStringTerms) attrId.getAggregations().get(FIELD_ATTR_VALUES)).getBuckets();
            List<? extends Terms.Bucket> attrNames = ((ParsedStringTerms) attrId.getAggregations().get(FIELD_ATTR_NAMES)).getBuckets();
            for (Terms.Bucket attrValue : attrValues) {
                attrValueList.add(attrValue.getKeyAsString());
            }
            attr.setAttrValues(attrValueList);
            // 提取属性名称（取第一个桶的值）
            if (!CollectionUtils.isEmpty(attrNames)) {
                String attrName = attrNames.get(0).getKeyAsString();
                attr.setAttrName(attrName);
            }
            attrList.add(attr);
        }
        productRelatedInfo.setProductAttrs(attrList);
        return productRelatedInfo;
    }
}
