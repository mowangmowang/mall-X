package com.macro.mall.search.component;

import com.macro.mall.common.domain.EsProductMessage;
import com.macro.mall.search.service.EsProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 商品同步消息接收器 */
@Component
@RabbitListener(queues = "mall.product.update")
public class EsProductReceiver {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsProductReceiver.class);
    
    @Autowired
    private EsProductService esProductService;
    
    @RabbitHandler
    public void handle(EsProductMessage message) {
        LOGGER.info("接收到商品同步消息：productId={}, actionType={}",
            message.getProductId(), message.getActionType());

        if ("ADD".equals(message.getActionType()) || "UPDATE".equals(message.getActionType())) {
            esProductService.create(message.getProductId());
            LOGGER.info("商品索引更新成功：productId={}", message.getProductId());
        } else if ("DELETE".equals(message.getActionType())) {
            esProductService.delete(message.getProductId());
            LOGGER.info("商品索引删除成功：productId={}", message.getProductId());
        }
    }
}
