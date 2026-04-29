package com.macro.mall.dao;

import com.macro.mall.model.PmsProductVerifyRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品审核日志管理自定义Dao */
public interface PmsProductVerifyRecordDao {
    /**
     * 批量创建
     */
    int insertList(@Param("list") List<PmsProductVerifyRecord> list);
    
    /**
     * 根据商品ID查询审核记录
     */
    List<PmsProductVerifyRecord> getListByProductId(@Param("productId") Long productId);
}
