package com.macro.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.macro.mall.dao.OmsOrderDao;
import com.macro.mall.dao.OmsOrderOperateHistoryDao;
import com.macro.mall.dto.*;
import com.macro.mall.mapper.OmsOrderMapper;
import com.macro.mall.mapper.OmsOrderOperateHistoryMapper;
import com.macro.mall.model.OmsOrder;
import com.macro.mall.model.OmsOrderExample;
import com.macro.mall.model.OmsOrderOperateHistory;
import com.macro.mall.service.OmsOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单管理 Service 实现类
 * 实现订单查询、发货、关闭、删除、修改等核心业务逻辑
 * 所有操作都会记录到订单操作历史表中
 */
@Service
public class OmsOrderServiceImpl implements OmsOrderService {
    /**
     * 订单 Mapper
     */
    @Autowired
    private OmsOrderMapper orderMapper;
    
    /**
     * 订单自定义 DAO（复杂查询）
     */
    @Autowired
    private OmsOrderDao orderDao;
    
    /**
     * 订单操作历史 DAO
     */
    @Autowired
    private OmsOrderOperateHistoryDao orderOperateHistoryDao;
    
    /**
     * 订单操作历史 Mapper
     */
    @Autowired
    private OmsOrderOperateHistoryMapper orderOperateHistoryMapper;

    /**
     * 分页查询订单列表
     *
     * @param queryParam 查询参数
     * @param pageSize 每页条数
     * @param pageNum 页码
     * @return 订单列表
     */
    @Override
    public List<OmsOrder> list(OmsOrderQueryParam queryParam, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        return orderDao.getList(queryParam);
    }

    /**
     * 批量发货
     * 更新订单物流信息并记录操作历史
     *
     * @param deliveryParamList 发货参数列表
     * @return 影响行数
     */
    @Override
    public int delivery(List<OmsOrderDeliveryParam> deliveryParamList) {
        // 批量发货
        int count = orderDao.delivery(deliveryParamList);
        
        // 添加操作记录
        List<OmsOrderOperateHistory> operateHistoryList = deliveryParamList.stream()
                .map(omsOrderDeliveryParam -> {
                    OmsOrderOperateHistory history = new OmsOrderOperateHistory();
                    history.setOrderId(omsOrderDeliveryParam.getOrderId());
                    history.setCreateTime(new Date());
                    history.setOperateMan("后台管理员");
                    history.setOrderStatus(2);  // 2-已发货
                    history.setNote("完成发货");
                    return history;
                }).collect(Collectors.toList());
        orderOperateHistoryDao.insertList(operateHistoryList);
        return count;
    }

    /**
     * 批量关闭订单
     * 通常用于超时未支付的订单
     *
     * @param ids 订单 ID 列表
     * @param note 关闭原因
     * @return 影响行数
     */
    @Override
    public int close(List<Long> ids, String note) {
        OmsOrder record = new OmsOrder();
        record.setStatus(4);  // 4-已关闭
        OmsOrderExample example = new OmsOrderExample();
        example.createCriteria().andDeleteStatusEqualTo(0).andIdIn(ids);
        int count = orderMapper.updateByExampleSelective(record, example);
        
        // 记录操作历史
        List<OmsOrderOperateHistory> historyList = ids.stream().map(orderId -> {
            OmsOrderOperateHistory history = new OmsOrderOperateHistory();
            history.setOrderId(orderId);
            history.setCreateTime(new Date());
            history.setOperateMan("后台管理员");
            history.setOrderStatus(4);
            history.setNote("订单关闭:" + note);
            return history;
        }).collect(Collectors.toList());
        orderOperateHistoryDao.insertList(historyList);
        return count;
    }

    /**
     * 批量删除订单（逻辑删除）
     * 仅标记删除状态，不物理删除
     *
     * @param ids 订单 ID 列表
     * @return 影响行数
     */
    @Override
    public int delete(List<Long> ids) {
        OmsOrder record = new OmsOrder();
        record.setDeleteStatus(1);  // 1-已删除
        OmsOrderExample example = new OmsOrderExample();
        example.createCriteria().andDeleteStatusEqualTo(0).andIdIn(ids);
        return orderMapper.updateByExampleSelective(record, example);
    }

    /**
     * 获取订单详细信息
     * 包括订单基本信息、商品列表、操作记录等
     *
     * @param id 订单 ID
     * @return 订单详情对象
     */
    @Override
    public OmsOrderDetail detail(Long id) {
        return orderDao.getDetail(id);
    }

    /**
     * 修改订单收货人信息
     * 同时记录操作历史
     *
     * @param receiverInfoParam 收货人信息参数
     * @return 影响行数
     */
    @Override
    public int updateReceiverInfo(OmsReceiverInfoParam receiverInfoParam) {
        OmsOrder order = new OmsOrder();
        order.setId(receiverInfoParam.getOrderId());
        order.setReceiverName(receiverInfoParam.getReceiverName());
        order.setReceiverPhone(receiverInfoParam.getReceiverPhone());
        order.setReceiverPostCode(receiverInfoParam.getReceiverPostCode());
        order.setReceiverDetailAddress(receiverInfoParam.getReceiverDetailAddress());
        order.setReceiverProvince(receiverInfoParam.getReceiverProvince());
        order.setReceiverCity(receiverInfoParam.getReceiverCity());
        order.setReceiverRegion(receiverInfoParam.getReceiverRegion());
        order.setModifyTime(new Date());
        int count = orderMapper.updateByPrimaryKeySelective(order);
        
        // 插入操作记录
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(receiverInfoParam.getOrderId());
        history.setCreateTime(new Date());
        history.setOperateMan("后台管理员");
        history.setOrderStatus(receiverInfoParam.getStatus());
        history.setNote("修改收货人信息");
        orderOperateHistoryMapper.insert(history);
        return count;
    }

    /**
     * 修改订单费用信息
     * 如运费、优惠金额等
     *
     * @param moneyInfoParam 费用信息参数
     * @return 影响行数
     */
    @Override
    public int updateMoneyInfo(OmsMoneyInfoParam moneyInfoParam) {
        OmsOrder order = new OmsOrder();
        order.setId(moneyInfoParam.getOrderId());
        order.setFreightAmount(moneyInfoParam.getFreightAmount());
        order.setDiscountAmount(moneyInfoParam.getDiscountAmount());
        order.setModifyTime(new Date());
        int count = orderMapper.updateByPrimaryKeySelective(order);
        
        // 插入操作记录
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(moneyInfoParam.getOrderId());
        history.setCreateTime(new Date());
        history.setOperateMan("后台管理员");
        history.setOrderStatus(moneyInfoParam.getStatus());
        history.setNote("修改费用信息");
        orderOperateHistoryMapper.insert(history);
        return count;
    }

    /**
     * 修改订单备注
     *
     * @param id 订单 ID
     * @param note 备注内容
     * @param status 当前订单状态
     * @return 影响行数
     */
    @Override
    public int updateNote(Long id, String note, Integer status) {
        OmsOrder order = new OmsOrder();
        order.setId(id);
        order.setNote(note);
        order.setModifyTime(new Date());
        int count = orderMapper.updateByPrimaryKeySelective(order);
        
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(id);
        history.setCreateTime(new Date());
        history.setOperateMan("后台管理员");
        history.setOrderStatus(status);
        history.setNote("修改备注信息：" + note);
        orderOperateHistoryMapper.insert(history);
        return count;
    }

    /**
     * 取消订单
     * 由管理员手动取消，将订单状态改为已关闭
     *
     * @param id 订单 ID
     * @param note 取消原因
     * @return 影响行数
     */
    @Override
    public int cancel(Long id, String note) {
        OmsOrder order = new OmsOrder();
        order.setId(id);
        order.setStatus(4); // 4-已关闭
        order.setModifyTime(new Date());
        int count = orderMapper.updateByPrimaryKeySelective(order);
        
        // 插入操作记录
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(id);
        history.setCreateTime(new Date());
        history.setOperateMan("后台管理员");
        history.setOrderStatus(4);
        history.setNote("取消订单：" + note);
        orderOperateHistoryMapper.insert(history);
        
        return count;
    }
}
