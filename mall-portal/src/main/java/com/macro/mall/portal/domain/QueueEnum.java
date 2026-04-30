package com.macro.mall.portal.domain;

import lombok.Getter;

/**
 * 消息队列枚举配置
 * 定义订单取消相关的交换机、队列和路由键
 */
@Getter
public enum QueueEnum {
    /**
     * 订单实际取消队列
     * 接收从延迟队列转发过来的超时订单消息，由消费者处理取消逻辑
     */
    QUEUE_ORDER_CANCEL("mall.order.direct", "mall.order.cancel", "mall.order.cancel"),
    /**
     * 订单延迟队列（TTL队列）
     * 新订单消息先发送到此队列，设置过期时间后自动转发到实际取消队列
     */
    QUEUE_TTL_ORDER_CANCEL("mall.order.direct.ttl", "mall.order.cancel.ttl", "mall.order.cancel.ttl");

    /**
     * 交换名称 (Exchange)
     */
    private final String exchange;
    /**
     * 队列名称 (Queue)
     */
    private final String name;
    /**
     * 路由键 (Routing Key)
     */
    private final String routeKey;

    QueueEnum(String exchange, String name, String routeKey) {
        this.exchange = exchange;
        this.name = name;
        this.routeKey = routeKey;
    }
}
