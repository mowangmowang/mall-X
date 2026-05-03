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
 * 商品同步消息接收器 (Product Synchronization Message Receiver)
 * <p>
 * 监听 RabbitMQ 队列，接收商品变更消息并实时同步到 Elasticsearch。
 * 同时提供定期全量校对任务，确保 MySQL 与 Elasticsearch 数据的最终一致性。
 * </p>
 * <p>
 * 核心功能：
 * <ul>
 *   <li>实时增量同步：监听 mall.product.update 队列，处理新增、更新、删除操作</li>
 *   <li>定时全量同步：每天凌晨 3:00 执行全量数据校对，修复可能的数据不一致问题</li>
 * </ul>
 * </p>
 *
 * @author macro
 * @since 1.0
 */
@Component
@RabbitListener(queues = "mall.product.update")  // 监听商品更新队列
public class EsProductReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsProductReceiver.class);
    
    @Autowired
    private EsProductService esProductService;  // 注入商品搜索服务
    
    /**
     * 处理商品同步消息 (Handle Product Sync Message)
     * <p>
     * 根据消息中的操作类型 (Action Type) 执行相应的索引操作：
     * <ul>
     *   <li>ADD/UPDATE：从 MySQL 查询商品数据并创建/更新 Elasticsearch 索引</li>
     *   <li>DELETE：从 Elasticsearch 中删除对应的索引文档</li>
     * </ul>
     * </p>
     *
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
     * 定期全量校对任务 (Scheduled Full Sync Task)
     * <p>
     * 每天凌晨 3:00 自动执行，从 MySQL 导入所有上架商品到 Elasticsearch，
     * 确保数据的最终一致性 (Eventual Consistency)，修复因消息丢失或处理失败导致的数据差异。
     * </p>
     * <p>
     * Cron 表达式说明："0 0 3 * * ?" 表示每天 3:00 AM 执行
     * </p>
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
