package com.macro.mall.search.service;

import com.macro.mall.search.domain.EsProduct;
import com.macro.mall.search.domain.EsProductRelatedInfo;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

/**
 * 搜索商品管理服务接口 (Service Interface)
 * 定义基于 Elasticsearch 的商品搜索、索引管理等核心业务逻辑
 */
public interface EsProductService {
    /**
     * 从 MySQL 数据库批量导入所有商品到 Elasticsearch 索引
     * @return 成功导入的商品数量
     */
    int importAll();

    /**
     * 根据商品 ID 删除 Elasticsearch 索引文档
     * @param id 商品唯一标识符 (Product ID)
     */
    void delete(Long id);

    /**
     * 根据商品 ID 从数据库查询并创建/更新 Elasticsearch 索引
     * @param id 商品唯一标识符 (Product ID)
     * @return 创建成功的 EsProduct 对象，若不存在则返回 null
     */
    EsProduct create(Long id);

    /**
     * 批量删除多个商品的 Elasticsearch 索引
     * @param ids 商品 ID 列表
     */
    void delete(List<Long> ids);

    /**
     * 简单搜索：根据关键字匹配商品名称、副标题或关键词
     * @param keyword 搜索关键字
     * @param pageNum 页码（从 0 开始）
     * @param pageSize 每页大小
     * @return 分页的商品搜索结果
     */
    Page<EsProduct> search(String keyword, Integer pageNum, Integer pageSize);

    /**
     * 综合搜索：支持关键字、品牌、分类筛选及多种排序策略
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
    Page<EsProduct> search(String keyword, Long brandId, Long productCategoryId, Integer pageNum, Integer pageSize,Integer sort,
                           BigDecimal startPrice, BigDecimal endPrice);

    /**
     * 基于商品 ID 推荐相似商品（根据名称、品牌、分类的加权匹配）
     * @param id 参考商品 ID
     * @param pageNum 页码（从 0 开始）
     * @param pageSize 每页大小
     * @return 分页的推荐商品列表
     */
    Page<EsProduct> recommend(Long id, Integer pageNum, Integer pageSize);

    /**
     * 获取搜索关键字相关的聚合信息：品牌列表、分类列表、属性筛选条件
     * @param keyword 搜索关键字
     * @return 包含品牌、分类、属性的关联信息对象
     */
    EsProductRelatedInfo searchRelatedInfo(String keyword);
}
