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
 * Created by macro on 2026/4/27.
 */
@Component
public class EsProductSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsProductSender.class);
    
    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 发送商品同步消息
     * @param productId 商品ID
     * @param actionType 操作类型：ADD/UPDATE/DELETE
     */
    public void send(Long productId, String actionType) {
        EsProductMessage message = new EsProductMessage();
        message.setProductId(productId);
        message.setActionType(actionType);
        message.setTimestamp(System.currentTimeMillis());
        
        amqpTemplate.convertAndSend("mall.product.direct", "mall.product.update", message);
        LOGGER.info("发送商品同步消息：productId={}, actionType={}", productId, actionType);
    }
}
