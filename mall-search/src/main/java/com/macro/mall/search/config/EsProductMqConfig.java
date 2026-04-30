package com.macro.mall.search.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 商品同步消息队列配置类 (Message Queue Configuration)
 * 配置 RabbitMQ 交换机、队列及绑定关系，实现商品数据变更的异步同步
 */
@Configuration
public class EsProductMqConfig {

    /**
     * 创建直连交换机 (Direct Exchange)
     * 用于路由商品变更消息到指定队列
     * @return DirectExchange 交换机实例
     */
    @Bean
    DirectExchange productDirect() {
        return ExchangeBuilder
                .directExchange("mall.product.direct")  // 交换机名称
                .durable(true)  // 持久化，防止重启丢失
                .build();
    }

    /**
     * 创建商品同步队列 (Queue)
     * 存储待处理的商品索引更新消息
     * @return Queue 队列实例
     */
    @Bean
    public Queue productQueue() {
        return new Queue("mall.product.update");  // 队列名称
    }

    /**
     * 绑定队列到交换机 (Binding)
     * 使用路由键 "mall.product.update" 将消息路由到指定队列
     * @param productDirect 直连交换机
     * @param productQueue 商品同步队列
     * @return Binding 绑定关系实例
     */
    @Bean
    Binding productBinding(DirectExchange productDirect, Queue productQueue) {
        return BindingBuilder
                .bind(productQueue)
                .to(productDirect)
                .with("mall.product.update");  // 路由键
    }

    /**
     * 配置 JSON 消息转换器 (Message Converter)
     * 替代默认的 Java 序列化方式，避免 InvalidClassException 等序列化问题
     * @return Jackson2JsonMessageConverter 转换器实例
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
