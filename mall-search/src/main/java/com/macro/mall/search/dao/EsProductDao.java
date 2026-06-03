package com.macro.mall.search.dao;

import com.macro.mall.search.domain.EsProduct;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 搜索商品自定义 MyBatis DAO 接口 (Custom MyBatis DAO Interface)
 * <p>
 * 用于从 MySQL 数据库查询商品数据并转换为 Elasticsearch 文档格式。
 * 该接口通过 XML 映射文件实现复杂的 SQL 查询，包括多表关联、子查询等。
 * </p>
 *
 * @author alan
 * @since 1.0
 */
public interface EsProductDao {
    /**
     * 获取指定 ID 的商品列表（用于导入 Elasticsearch） (Get Products for ES Import)
     * <p>
     * 根据商品 ID 查询商品信息，若 ID 为 null 则查询所有上架商品。
     * 查询结果包含商品基本信息、品牌信息、分类信息及属性值列表。
     * </p>
     *
     * @param id 商品 ID，若为 null 则查询所有商品
     * @return EsProduct 列表，包含商品的完整信息
     */
    List<EsProduct> getAllEsProductList(@Param("id") Long id);
}
