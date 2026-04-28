package com.macro.mall.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ消息队列配置
 * 用于商品同步到Elasticsearch
 */
@Configuration
public class RabbitMqConfig {

    /**
     * 商品同步交换机
     */
    @Bean
    public DirectExchange productDirect() {
        return new DirectExchange("mall.product.direct", true, false);
    }

    /**
     * 配置JSON消息转换器，替代默认的Java序列化方式
     * 避免InvalidClassException等序列化问题
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
