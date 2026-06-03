package com.macro.mall.search.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 商品同步消息队列配置类 (Product Synchronization Message Queue Configuration)
 * <p>
 * 配置 RabbitMQ 的交换机 (Exchange)、队列 (Queue) 及绑定关系 (Binding)，
 * 实现商品数据变更的异步同步机制，确保 MySQL 与 Elasticsearch 的数据一致性。
 * </p>
 * <p>
 * 架构说明：
 * <ul>
 *   <li>交换机：mall.product.direct (直连交换机 Direct Exchange)</li>
 *   <li>队列：mall.product.update (商品更新队列)</li>
 *   <li>路由键：mall.product.update (Routing Key)</li>
 *   <li>消息格式：JSON (使用 Jackson2JsonMessageConverter 序列化)</li>
 * </ul>
 * </p>
 *
 * @author alan
 * @since 1.0
 */
@Configuration
public class EsProductMqConfig {

    /**
     * 创建直连交换机 (Create Direct Exchange)
     * <p>
     * 用于根据路由键精确路由商品变更消息到指定队列。
     * 直连交换机 (Direct Exchange) 适用于点对点的消息传递场景。
     * </p>
     *
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
     * 创建商品同步队列 (Create Product Sync Queue)
     * <p>
     * 存储待处理的商品索引更新消息，消费者从该队列中获取消息并执行同步操作。
     * 队列名称：mall.product.update
     * </p>
     *
     * @return Queue 队列实例
     */
    @Bean
    public Queue productQueue() {
        return new Queue("mall.product.update");  // 队列名称
    }

    /**
     * 绑定队列到交换机 (Bind Queue to Exchange)
     * <p>
     * 使用路由键 "mall.product.update" 将商品同步队列绑定到直连交换机，
     * 确保发送到交换机的消息能够正确路由到目标队列。
     * </p>
     *
     * @param productDirect 直连交换机实例
     * @param productQueue 商品同步队列实例
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
     * 配置 JSON 消息转换器 (Configure JSON Message Converter)
     * <p>
     * 替代默认的 Java 原生序列化方式，使用 Jackson2JsonMessageConverter 进行 JSON 序列化。
     * 优势：
     * <ul>
     *   <li>避免 InvalidClassException 等序列化版本兼容性问题</li>
     *   <li>提高消息的可读性和跨语言兼容性</li>
     *   <li>减小消息体积，提升传输效率</li>
     * </ul>
     * </p>
     *
     * @return Jackson2JsonMessageConverter 消息转换器实例
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
