package com.macro.mall.portal.component;

import com.macro.mall.portal.domain.QueueEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 取消订单消息发送者 (Cancel Order Message Sender)
 * 负责向 RabbitMQ 延迟队列发送订单取消消息，实现订单超时自动取消功能
 */
@Component
public class CancelOrderSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(CancelOrderSender.class);
    
    /**
     * RabbitMQ 消息操作模板 (RabbitMQ Message Operation Template)
     * Spring AMQP 框架提供的核心接口，用于简化与 RabbitMQ 的交互
     * 类似于 JdbcTemplate（数据库操作）或 RestTemplate（HTTP 请求）
     * 由 Spring Boot 根据 application.yml 中的 rabbitmq 配置自动创建并注入
     */
    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 发送订单取消延迟消息 (Send Order Cancellation Delayed Message)
     * <p>
     * 工作流程：
     * 1. 用户创建订单后，调用此方法发送延迟消息到 RabbitMQ
     * 2. 消息先进入 TTL 延迟队列（设置过期时间，如 30 分钟）
     * 3. 消息过期后，自动转发到死信交换机
     * 4. 死信交换机将消息路由到实际消费队列
     * 5. CancelOrderReceiver 监听到消息后，执行订单取消逻辑
     * </p>
     *
     * @param orderId    订单ID (Order ID)，用于标识需要取消的订单
     * @param delayTimes 延迟时间（毫秒），例如 30*60*1000 表示 30 分钟后触发取消
     */
    public void sendMessage(Long orderId, final long delayTimes) {
        // 向 RabbitMQ 延迟队列发送消息
        // convertAndSend 方法参数说明：
        // 参数1: exchange - 交换机名称，决定消息发送到哪个交换机
        // 参数2: routingKey - 路由键，决定消息在交换机中如何路由到队列
        // 参数3: message - 消息内容，这里是订单ID（Long类型）
        // 参数4: messagePostProcessor - 消息后处理器，在发送前对消息进行额外处理
        amqpTemplate.convertAndSend(
                QueueEnum.QUEUE_TTL_ORDER_CANCEL.getExchange(),   // 获取延迟队列的交换机名称
                QueueEnum.QUEUE_TTL_ORDER_CANCEL.getRouteKey(),   // 获取延迟队列的路由键
                orderId,                                           // 消息体：订单ID
                new MessagePostProcessor() {                      // 匿名内部类：消息后处理器
                    /**
                     * 在消息发送前进行处理
                     * @param message 待发送的消息对象
                     * @return 处理后的消息对象
                     * @throws AmqpException 消息处理异常
                     */
                    @Override
                    public Message postProcessMessage(Message message) throws AmqpException {
                        // 设置消息的过期时间（TTL: Time To Live）
                        // 消息在队列中存活的时间，超过此时间后消息会变成"死信"
                        // 死信会被转发到配置的死信交换机，实现延迟效果
                        message.getMessageProperties().setExpiration(String.valueOf(delayTimes));
                        return message;
                    }
                }
        );
        
        // 记录日志，便于追踪消息发送情况
        LOGGER.info("send orderId:{}", orderId);
    }
}
