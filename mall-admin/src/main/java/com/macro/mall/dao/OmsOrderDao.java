package com.macro.mall.dao;

import com.macro.mall.dto.OmsOrderDeliveryParam;
import com.macro.mall.dto.OmsOrderDetail;
import com.macro.mall.dto.OmsOrderQueryParam;
import com.macro.mall.model.OmsOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单查询自定义 DAO 接口
 * 用于处理复杂的订单查询和批量操作
 * 对应的 SQL 实现在 resources/dao/OmsOrderDao.xml 中
 */
public interface OmsOrderDao {
    /**
     * 条件查询订单列表
     * 支持按订单号、状态、时间等多种条件筛选
     * @param queryParam 查询参数对象
     * @return 订单列表
     */
    List<OmsOrder> getList(@Param("queryParam") OmsOrderQueryParam queryParam);

    /**
     * 批量发货
     * 更新订单的物流信息并修改状态为已发货
     * @param deliveryParamList 发货参数列表
     * @return 影响行数
     */
    int delivery(@Param("list") List<OmsOrderDeliveryParam> deliveryParamList);

    /**
     * 获取订单详细信息
     * 包括订单基本信息、商品列表、操作记录等
     * @param id 订单 ID
     * @return 订单详情对象
     */
    OmsOrderDetail getDetail(@Param("id") Long id);
}
