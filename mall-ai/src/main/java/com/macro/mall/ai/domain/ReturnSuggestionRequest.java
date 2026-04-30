package com.macro.mall.ai.domain;

public class ReturnSuggestionRequest {
    private String issue;
    private String productName;
    private String productAttr;
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
