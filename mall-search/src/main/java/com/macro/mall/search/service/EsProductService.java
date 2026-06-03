package com.macro.mall.search.service;

import com.macro.mall.search.domain.EsProduct;
import com.macro.mall.search.domain.EsProductRelatedInfo;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

/**
 * 搜索商品管理服务接口 (Search Product Management Service Interface)
 * <p>
 * 定义基于 Elasticsearch 的商品搜索、索引管理等核心业务逻辑。
 * 提供以下功能：
 * <ul>
 *   <li>索引管理：导入、创建、更新、删除商品索引</li>
 *   <li>全文搜索：简单搜索、综合搜索（支持多维度筛选与排序）</li>
 *   <li>商品推荐：基于相似度的相关商品推荐</li>
 *   <li>聚合分析：获取品牌、分类、属性等筛选条件</li>
 * </ul>
 * </p>
 *
 * @author alan
 * @since 1.0
 */
public interface EsProductService {
    /**
     * 从 MySQL 数据库批量导入所有商品到 Elasticsearch 索引 (Import All Products from MySQL)
     * <p>
     * 采用分页查询避免内存溢出，每批处理 500 条记录。
     * 适用于系统初始化或数据修复场景。
     * </p>
     *
     * @return 成功导入的商品数量 (Number of Imported Products)
     */
    int importAll();

    /**
     * 根据商品 ID 删除 Elasticsearch 索引文档 (Delete Product Index by ID)
     *
     * @param id 商品唯一标识符 (Product ID)
     */
    void delete(Long id);

    /**
     * 根据商品 ID 从数据库查询并创建/更新 Elasticsearch 索引 (Create or Update Product Index)
     * <p>
     * 若索引已存在则更新，否则创建新索引。
     * Spring Data Elasticsearch 的 save 方法会根据 ID 自动判断是新增还是更新。
     * </p>
     *
     * @param id 商品唯一标识符 (Product ID)
     * @return 创建成功的 EsProduct 对象，若不存在则返回 null
     */
    EsProduct create(Long id);

    /**
     * 批量删除多个商品的 Elasticsearch 索引 (Batch Delete Product Indexes)
     *
     * @param ids 商品 ID 列表 (List of Product IDs)
     */
    void delete(List<Long> ids);

    /**
     * 简单搜索：根据关键字匹配商品名称、副标题或关键词 (Simple Search by Keyword)
     * <p>
     * 使用 Function Score Query 对不同字段进行加权评分，
     * 权重设置：名称(10) > 关键词(5) > 副标题(3)。
     * </p>
     *
     * @param keyword 搜索关键字 (Search Keyword)
     * @param pageNum 页码（从 0 开始） (Page Number, starting from 0)
     * @param pageSize 每页大小 (Page Size)
     * @return 分页的商品搜索结果 (Paginated Search Results)
     */
    Page<EsProduct> search(String keyword, Integer pageNum, Integer pageSize);

    /**
     * 综合搜索：支持关键字、品牌、分类筛选及多种排序策略 (Advanced Search with Filters and Sorting)
     * <p>
     * 支持以下筛选条件：
     * <ul>
     *   <li>关键字：全文搜索商品名称、副标题、关键词</li>
     *   <li>品牌 ID：精确匹配品牌</li>
     *   <li>分类 ID：精确匹配分类</li>
     *   <li>价格区间：范围过滤</li>
     * </ul>
     * 支持以下排序方式：
     * <ul>
     *   <li>0 -> 相关度（默认）</li>
     *   <li>1 -> 新品（ID 降序）</li>
     *   <li>2 -> 销量（降序）</li>
     *   <li>3 -> 价格升序</li>
     *   <li>4 -> 价格降序</li>
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
    Page<EsProduct> search(String keyword, Long brandId, Long productCategoryId, Integer pageNum, Integer pageSize,Integer sort,
                           BigDecimal startPrice, BigDecimal endPrice);

    /**
     * 基于商品 ID 推荐相似商品（根据名称、品牌、分类的加权匹配） (Recommend Similar Products)
     * <p>
     * 根据参考商品的名称、品牌、分类进行加权匹配，排除自身。
     * 权重设置：名称(8) > 关键词(5) > 同品牌(5) > 副标题(3) > 同分类(3)。
     * </p>
     *
     * @param id 参考商品 ID (Reference Product ID)
     * @param pageNum 页码（从 0 开始） (Page Number, starting from 0)
     * @param pageSize 每页大小 (Page Size)
     * @return 分页的推荐商品列表 (Paginated Recommended Products)
     */
    Page<EsProduct> recommend(Long id, Integer pageNum, Integer pageSize);

    /**
     * 获取搜索关键字相关的聚合信息：品牌列表、分类列表、属性筛选条件 (Get Search Related Aggregation Info)
     * <p>
     * 使用 Elasticsearch Aggregation 功能统计搜索结果中的品牌、分类、属性分布，
     * 用于前端动态生成筛选器，帮助用户快速缩小搜索范围。
     * </p>
     *
     * @param keyword 搜索关键字 (Search Keyword)
     * @return 包含品牌、分类、属性的关联信息对象 (Related Information with Brands, Categories, Attributes)
     */
    EsProductRelatedInfo searchRelatedInfo(String keyword);
}
