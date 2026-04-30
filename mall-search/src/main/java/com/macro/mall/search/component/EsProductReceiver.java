package com.macro.mall.search.component;

import com.macro.mall.common.domain.EsProductMessage;
import com.macro.mall.search.service.EsProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 商品同步消息接收器 (Message Receiver)
 * 监听 RabbitMQ 队列，接收商品变更消息并同步到 Elasticsearch
 * 同时提供定期全量校对任务，确保数据一致性
 */
@Component
@RabbitListener(queues = "mall.product.update")  // 监听商品更新队列
public class EsProductReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsProductReceiver.class);
    
    @Autowired
    private EsProductService esProductService;  // 注入商品搜索服务
    
    /**
     * 处理商品同步消息
     * @param message 商品消息对象，包含商品 ID 和操作类型（ADD/UPDATE/DELETE）
     */
    @RabbitHandler
    public void handle(EsProductMessage message) {
        LOGGER.info("接收到商品同步消息：productId={}, actionType={}",
            message.getProductId(), message.getActionType());

        if ("ADD".equals(message.getActionType()) || "UPDATE".equals(message.getActionType())) {
            // 新增或更新：从 MySQL 查询并创建/更新 ES 索引
            esProductService.create(message.getProductId());
            LOGGER.info("商品索引更新成功：productId={}", message.getProductId());
        } else if ("DELETE".equals(message.getActionType())) {
            // 删除：移除 ES 索引
            esProductService.delete(message.getProductId());
            LOGGER.info("商品索引删除成功：productId={}", message.getProductId());
        }
    }

    /**
     * 定期全量校对任务：每天凌晨 3:00 执行
     * 从 MySQL 导入所有商品到 Elasticsearch，确保数据最终一致性
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void syncAllProducts() {
        LOGGER.info("开始执行 Elasticsearch 全量校对任务...");
        try {
            int count = esProductService.importAll();
            LOGGER.info("Elasticsearch 全量校对任务完成，共同步 {} 个商品", count);
        } catch (Exception e) {
            LOGGER.error("Elasticsearch 全量校对任务执行失败: {}", e.getMessage(), e);
        }
    }
}
