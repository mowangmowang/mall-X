package com.macro.mall.ai.domain;

public class ProductQaRequest {
    private Long productId;
    private String question;
    private String productName;
    private String productBrand;
    private String productPrice;
    private String productSubTitle;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductBrand() { return productBrand; }
    public void setProductBrand(String productBrand) { this.productBrand = productBrand; }
    public String getProductPrice() { return productPrice; }
    public void setProductPrice(String productPrice) { this.productPrice = productPrice; }
    public String getProductSubTitle() { return productSubTitle; }
    public void setProductSubTitle(String productSubTitle) { this.productSubTitle = productSubTitle; }
}
