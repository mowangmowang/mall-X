package com.macro.mall.common.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Controller 层日志封装类 (Web Log)
 * 用于 AOP 切面记录请求详细信息，包括请求参数、响应结果、耗时等
 */
@Data
@EqualsAndHashCode
public class WebLog {
    /**
     * 操作描述
     */
    private String description;

    /**
     * 操作用户
     */
    private String username;

    /**
     * 操作时间
     */
    private Long startTime;

    /**
     * 消耗时间
     */
    private Integer spendTime;

    /**
     * 根路径
     */
    private String basePath;

    /**
     * URI
     */
    private String uri;

    /**
     * URL
     */
    private String url;

    /**
     * 请求类型
     */
    private String method;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 请求参数
     */
    private Object parameter;

    /**
     * 返回结果
     */
    private Object result;

}
