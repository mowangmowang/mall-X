package com.macro.mall.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 消息队列配置类
 * 主要用于商品数据同步到 Elasticsearch 搜索引擎
 * 通过异步消息机制解耦商品管理与搜索功能
 */
@Configuration
public class RabbitMqConfig {

    /**
     * 配置商品同步专用的直连交换机 (Direct Exchange)
     * 交换机名称：mall.product.direct
     * @return DirectExchange 交换机实例
     * 参数说明：
     * - durable: true 表示持久化，重启后交换机仍然存在
     * - autoDelete: false 表示不自动删除
     */
    @Bean
    public DirectExchange productDirect() {
        return new DirectExchange("mall.product.direct", true, false);
    }

    /**
     * 配置 JSON 消息转换器
     * 替代默认的 Java 序列化方式，避免 InvalidClassException 等序列化问题
     * 使用 Jackson 库进行对象与 JSON 之间的转换
     * @return MessageConverter 消息转换器实例
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
