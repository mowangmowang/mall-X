package com.macro.mall.search.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 商品同步消息队列配置 */
@Configuration
public class EsProductMqConfig {

    /**
     * 商品变更交换机
     */
    @Bean
    DirectExchange productDirect() {
        return ExchangeBuilder
                .directExchange("mall.product.direct")
                .durable(true)
                .build();
    }

    /**
     * 商品同步队列
     */
    @Bean
    public Queue productQueue() {
        return new Queue("mall.product.update");
    }

    /**
     * 绑定队列到交换机
     */
    @Bean
    Binding productBinding(DirectExchange productDirect, Queue productQueue) {
        return BindingBuilder
                .bind(productQueue)
                .to(productDirect)
                .with("mall.product.update");
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
