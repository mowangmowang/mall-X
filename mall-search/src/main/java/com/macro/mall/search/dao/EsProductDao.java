package com.macro.mall.search.dao;

import com.macro.mall.search.domain.EsProduct;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 搜索商品自定义 MyBatis DAO 接口
 * 用于从 MySQL 数据库查询商品数据并转换为 Elasticsearch 文档格式
 */
public interface EsProductDao {
    /**
     * 获取指定 ID 的商品列表（用于导入 Elasticsearch）
     * @param id 商品 ID，若为 null 则查询所有商品
     * @return EsProduct 列表
     */
    List<EsProduct> getAllEsProductList(@Param("id") Long id);
}
