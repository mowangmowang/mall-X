package com.macro.mall.search.repository;

import com.macro.mall.search.domain.EsProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Elasticsearch 商品操作仓储接口 (Repository Interface)
 * 继承 Spring Data Elasticsearch 的 ElasticsearchRepository，提供基础 CRUD 功能
 */
public interface EsProductRepository extends ElasticsearchRepository<EsProduct, Long> {
    /**
     * 根据商品名称、副标题或关键词进行全文搜索
     * Spring Data 方法名自动推导查询（Derived Query）
     *
     * @param name      商品名称
     * @param subTitle  商品副标题
     * @param keywords  商品关键词
     * @param page      分页参数
     * @return 分页的商品搜索结果
     */
    Page<EsProduct> findByNameOrSubTitleOrKeywords(String name, String subTitle, String keywords,Pageable page);

}
