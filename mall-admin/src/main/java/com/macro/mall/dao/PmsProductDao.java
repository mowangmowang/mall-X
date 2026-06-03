package com.macro.mall.dao;

import com.macro.mall.dto.PmsProductResult;
import org.apache.ibatis.annotations.Param;


/**
 * 商品管理自定义 DAO 接口
 * 用于处理复杂的商品查询和编辑操作
 * 对应的 SQL 实现在 resources/dao/PmsProductDao.xml 中
 */
public interface PmsProductDao {
    /**
     * 获取商品编辑信息
     * 包括商品基本信息、SKU、属性值等完整数据
     * @param id 商品 ID
     * @return 商品编辑信息对象
     */
    PmsProductResult getUpdateInfo(@Param("id") Long id);
}
