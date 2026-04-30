package com.macro.mall.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商品同步消息 (Elasticsearch Product Message)
 * 用于在 RabbitMQ 中传递商品索引的增删改操作指令
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EsProductMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 商品 ID
     */
    private Long productId;
    
    /**
     * 操作类型：ADD-新增，UPDATE-修改，DELETE-删除
     */
    private String actionType;
    
    /**
     * 时间戳（用于消息顺序控制）
     */
    private Long timestamp;
}
