package com.macro.mall.search.repository;

import com.macro.mall.search.domain.EsProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Elasticsearch 商品操作仓储接口 (Elasticsearch Product Repository Interface)
 * <p>
 * 继承 Spring Data Elasticsearch 的 ElasticsearchRepository，提供基础的 CRUD 功能。
 * Spring Data 会根据方法名自动生成查询逻辑（Derived Query），无需编写实现代码。
 * </p>
 * <p>
 * 核心功能：
 * <ul>
 *   <li>基础 CRUD：save、delete、findById、findAll 等</li>
 *   <li>自定义查询：根据方法名自动推导查询条件</li>
 * </ul>
 * </p>
 *
 * @author alan
 * @since 1.0
 */
public interface EsProductRepository extends ElasticsearchRepository<EsProduct, Long> {
    /**
     * 根据商品名称、副标题或关键词进行全文搜索 (Full-text Search by Name, SubTitle or Keywords)
     * <p>
     * Spring Data 方法名自动推导查询（Derived Query），
     * 等价于 SQL: WHERE name LIKE ? OR subTitle LIKE ? OR keywords LIKE ?
     * </p>
     *
     * @param name      商品名称 (Product Name)
     * @param subTitle  商品副标题 (Product SubTitle)
     * @param keywords  商品关键词 (Product Keywords)
     * @param page      分页参数 (Pageable Parameter)
     * @return 分页的商品搜索结果 (Paginated Search Results)
     */
    Page<EsProduct> findByNameOrSubTitleOrKeywords(String name, String subTitle, String keywords,Pageable page);

}
