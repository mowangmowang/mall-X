package com.macro.mall.ai.domain;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ReturnSuggestionRequest {
    
    @NotBlank(message = "问题描述不能为空")
    @Size(max = 1000, message = "问题描述长度不能超过1000字符")
    @ApiModelProperty(value = "用户问题描述", required = true, example = "商品收到后发现屏幕有裂痕")
    private String issue;
    
    @ApiModelProperty(value = "商品名称", example = "iPhone 15 Pro")
    private String productName;
    
    @ApiModelProperty(value = "商品属性", example = "颜色:黑色,容量:256GB")
    private String productAttr;
    
    @ApiModelProperty(value = "订单编号", example = "202401010001")
    private String orderSn;

    public String getIssue() { return issue; }
    public void setIssue(String issue) { this.issue = issue; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductAttr() { return productAttr; }
    public void setProductAttr(String productAttr) { this.productAttr = productAttr; }
    public String getOrderSn() { return orderSn; }
    public void setOrderSn(String orderSn) { this.orderSn = orderSn; }
}
