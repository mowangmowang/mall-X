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
 * 搜索商品管理服务实现类 (Search Product Management Service Implementation)
 * <p>
 * 实现基于 Elasticsearch 的商品索引管理、全文搜索、聚合分析等核心功能。
 * 使用 Spring Data Elasticsearch 和原生 Elasticsearch API 相结合的方式，
 * 提供灵活高效的搜索能力。
 * </p>
 * <p>
 * 核心技术：
 * <ul>
 *   <li>Function Score Query：加权评分搜索</li>
 *   <li>Bool Query：多条件组合查询</li>
 *   <li>Aggregation：聚合分析（品牌、分类、属性统计）</li>
 *   <li>Nested Query：嵌套类型查询（属性筛选）</li>
 * </ul>
 * </p>
 *
 * @author macro
 * @since 1.0
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
     * 从 MySQL 数据库批量导入商品到 Elasticsearch (Import All Products from MySQL to Elasticsearch)
     * <p>
     * 采用分页查询避免内存溢出，每批处理 500 条记录。
     * 使用 PageHelper 进行物理分页，确保大数据量下的稳定性。
     * </p>
     *
     * @return 成功导入的商品总数 (Total Number of Imported Products)
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
     * 根据商品 ID 删除 Elasticsearch 索引文档 (Delete Product Index by ID)
     *
     * @param id 商品唯一标识符 (Product ID)
     */
    @Override
    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    /**
     * 根据商品 ID 从 MySQL 查询并创建/更新 Elasticsearch 索引 (Create or Update Product Index by ID)
     * <p>
     * Spring Data Elasticsearch 的 save 方法会根据 ID 自动判断是新增还是更新：
     * <ul>
     *   <li>若 ID 不存在：创建新索引</li>
     *   <li>若 ID 已存在：更新现有索引</li>
     * </ul>
     * </p>
     *
     * @param id 商品唯一标识符 (Product ID)
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
     * 批量删除多个商品的 Elasticsearch 索引 (Batch Delete Product Indexes)
     *
     * @param ids 商品 ID 列表 (List of Product IDs)
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
     * 简单搜索：根据关键字匹配商品名称、副标题或关键词 (Simple Search by Keyword)
     * <p>
     * 使用 Function Score Query 对不同字段进行加权评分，提升搜索结果的相关度。
     * 权重设置：名称(10) > 关键词(5) > 副标题(3)。
     * </p>
     *
     * @param keyword 搜索关键字 (Search Keyword)
     * @param pageNum 页码（从 0 开始） (Page Number, starting from 0)
     * @param pageSize 每页大小 (Page Size)
     * @return 按相关度排序的分页搜索结果 (Paginated Search Results Sorted by Relevance)
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
     * 综合搜索：支持关键字、品牌、分类筛选、价格区间过滤及多种排序策略 (Advanced Search with Filters and Sorting)
     * <p>
     * 查询架构：
     * <ul>
     *   <li>Filter 上下文：品牌、分类、价格区间（不影响评分，性能更优）</li>
     *   <li>Query 上下文：关键字全文搜索（影响评分）</li>
     * </ul>
     * 排序策略：
     * <ul>
     *   <li>sort=0：按相关度分数降序（默认）</li>
     *   <li>sort=1：按新品（ID 降序）</li>
     *   <li>sort=2：按销量降序</li>
     *   <li>sort=3：按价格升序</li>
     *   <li>sort=4：按价格降序</li>
     * </ul>
     * </p>
     *
     * @param keyword 搜索关键字 (Search Keyword)
     * @param brandId 品牌 ID（可选筛选条件） (Brand ID, optional)
     * @param productCategoryId 商品分类 ID（可选筛选条件） (Product Category ID, optional)
     * @param pageNum 页码（从 0 开始） (Page Number, starting from 0)
     * @param pageSize 每页大小 (Page Size)
     * @param sort 排序方式 (Sort Type: 0->Relevance; 1->New; 2->Sales; 3->Price ASC; 4->Price DESC)
     * @param startPrice 价格区间下限（可选） (Minimum Price, optional)
     * @param endPrice 价格区间上限（可选） (Maximum Price, optional)
     * @return 分页的商品搜索结果 (Paginated Search Results)
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
     * 基于商品 ID 推荐相似商品 (Recommend Similar Products by ID)
     * <p>
     * 根据参考商品的名称、品牌、分类进行加权匹配，排除自身。
     * 使用 Function Score Query 实现多维度相似度计算。
     * </p>
     * <p>
     * 权重设置：
     * <ul>
     *   <li>名称匹配：8（最高权重）</li>
     *   <li>关键词匹配：5</li>
     *   <li>同品牌：5</li>
     *   <li>副标题匹配：3</li>
     *   <li>同分类：3</li>
     * </ul>
     * 最低分数阈值：2分，过滤不相关结果
     * </p>
     *
     * @param id 参考商品 ID (Reference Product ID)
     * @param pageNum 页码（从 0 开始） (Page Number, starting from 0)
     * @param pageSize 每页大小 (Page Size)
     * @return 分页的推荐商品列表 (Paginated Recommended Products)
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
     * 获取搜索关键字相关的聚合信息：品牌列表、分类列表、属性筛选条件 (Get Search Related Aggregation Info)
     * <p>
     * 使用 Elasticsearch Aggregation 功能统计搜索结果中的品牌、分类、属性分布，
     * 用于前端动态生成筛选器，帮助用户快速缩小搜索范围。
     * </p>
     * <p>
     * 聚合类型：
     * <ul>
     *   <li>品牌名称聚合 (Terms Aggregation)</li>
     *   <li>分类名称聚合 (Terms Aggregation)</li>
     *   <li>嵌套属性聚合 (Nested Aggregation)：先过滤参数类型 (type=1)，再按属性 ID、值、名称分组</li>
     * </ul>
     * </p>
     *
     * @param keyword 搜索关键字 (Search Keyword)
     * @return 包含品牌、分类、属性的关联信息对象 (Related Information with Brands, Categories, Attributes)
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
     * 构建 Function Score Query，对商品名称、副标题、关键词进行加权搜索 (Build Function Score Query)
     * <p>
     * Function Score Query 允许对不同字段设置不同的权重，提升搜索结果的相关度。
     * 分数计算模式：SUM（累加各字段的加权分数）。
     * </p>
     * <p>
     * 权重设置：
     * <ul>
     *   <li>商品名称 (name)：10（最高权重，名称匹配最相关）</li>
     *   <li>关键词 (keywords)：5（中等权重）</li>
     *   <li>副标题 (subTitle)：3（较低权重）</li>
     * </ul>
     * 最低分数阈值：0.5分，过滤低相关度结果
     * </p>
     *
     * @param keyword 搜索关键字 (Search Keyword)
     * @return FunctionScoreQueryBuilder 查询构建器 (Query Builder)
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
     * 将 Elasticsearch 聚合结果转换为 EsProductRelatedInfo 对象 (Convert Aggregation Results)
     * <p>
     * 解析 Elasticsearch 返回的聚合数据，提取品牌、分类、属性信息。
     * 嵌套属性聚合的解析路径：
     * allAttrValues (Nested) -> productAttrs (Filter) -> attrIds (Terms) -> attrValues + attrNames
     * </p>
     *
     * @param response Elasticsearch 搜索结果（包含聚合信息） (Search Response with Aggregations)
     * @return 转换后的关联信息对象 (Converted Related Information Object)
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
