package com.macro.mall.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商品同步消息 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EsProductMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 操作类型：ADD-新增，UPDATE-修改，DELETE-删除
     */
    private String actionType;
    
    /**
     * 时间戳
     */
    private Long timestamp;
}
