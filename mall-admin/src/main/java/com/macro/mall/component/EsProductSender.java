package com.macro.mall.component;

import com.macro.mall.common.domain.EsProductMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 商品同步消息发送器
 * 用于将商品的增删改操作异步发送到 RabbitMQ
 * mall-search 服务会消费这些消息并同步到 Elasticsearch
 */
@Component
public class EsProductSender {
    /**
     * 日志记录器
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EsProductSender.class);
    
    /**
     * AMQP 模板，用于发送消息到 RabbitMQ
     */
    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 发送商品同步消息到 RabbitMQ
     * 消息会被 mall-search 服务消费，用于同步 ES 数据
     * @param productId 商品 ID
     * @param actionType 操作类型：ADD-新增，UPDATE-更新，DELETE-删除
     */
    public void send(Long productId, String actionType) {
        // 构建消息对象
        EsProductMessage message = new EsProductMessage();
        message.setProductId(productId);
        message.setActionType(actionType);
        message.setTimestamp(System.currentTimeMillis());
        
        // 发送消息到交换机
        // 交换机：mall.product.direct
        // 路由键：mall.product.update
        amqpTemplate.convertAndSend("mall.product.direct", "mall.product.update", message);
        LOGGER.info("发送商品同步消息：productId={}, actionType={}", productId, actionType);
    }
}
