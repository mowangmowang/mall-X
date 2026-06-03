package com.macro.mall.portal.config;

import com.macro.mall.portal.domain.QueueEnum;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 消息队列配置类 (RabbitMQ Message Queue Configuration)
 * 用于配置订单取消的延迟消息机制，通过死信队列实现订单超时自动取消功能
 */
@Configuration
public class RabbitMqConfig {

    /**
     * 创建订单实际消费队列的直连交换机 (Direct Exchange)
     * 用于接收从延迟队列转发过来的超时订单消息
     */
    @Bean
    DirectExchange orderDirect() {
        return ExchangeBuilder
                .directExchange(QueueEnum.QUEUE_ORDER_CANCEL.getExchange())
                .durable(true) // 持久化交换机，重启后仍然存在
                .build();
    }

    /**
     * 创建订单延迟队列的直连交换机 (Direct Exchange)
     * 用于接收新创建的订单消息，并设置过期时间
     */
    @Bean
    DirectExchange orderTtlDirect() {
        return ExchangeBuilder
                .directExchange(QueueEnum.QUEUE_TTL_ORDER_CANCEL.getExchange())
                .durable(true) // 持久化交换机
                .build();
    }

    /**
     * 创建订单实际消费队列
     * 该队列接收从延迟队列转发过来的超时订单，由消费者处理取消逻辑
     */
    @Bean
    public Queue orderQueue() {
        return new Queue(QueueEnum.QUEUE_ORDER_CANCEL.getName());
    }

    /**
     * 创建订单延迟队列（死信队列）
     * 消息在此队列中等待指定时间后，会自动转发到实际消费队列
     */
    @Bean
    public Queue orderTtlQueue() {
        return QueueBuilder
                .durable(QueueEnum.QUEUE_TTL_ORDER_CANCEL.getName())
                .withArgument("x-dead-letter-exchange", QueueEnum.QUEUE_ORDER_CANCEL.getExchange())//到期后转发的交换机
                .withArgument("x-dead-letter-routing-key", QueueEnum.QUEUE_ORDER_CANCEL.getRouteKey())//到期后转发的路由键
                .build();
    }

    /**
     * 将订单实际消费队列绑定到交换机
     * 指定路由键，确保消息能正确路由到该队列
     */
    @Bean
    Binding orderBinding(DirectExchange orderDirect,Queue orderQueue){
        return BindingBuilder
                .bind(orderQueue)
                .to(orderDirect)
                .with(QueueEnum.QUEUE_ORDER_CANCEL.getRouteKey());
    }

    /**
     * 将订单延迟队列绑定到交换机
     * 新订单消息发送到此队列，等待过期后自动转发
     */
    @Bean
    Binding orderTtlBinding(DirectExchange orderTtlDirect,Queue orderTtlQueue){
        return BindingBuilder
                .bind(orderTtlQueue)
                .to(orderTtlDirect)
                .with(QueueEnum.QUEUE_TTL_ORDER_CANCEL.getRouteKey());
    }

    /**
     * 配置 JSON 消息转换器
     * 使 RabbitMQ 能够序列化和反序列化 Java 对象为 JSON 格式
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
