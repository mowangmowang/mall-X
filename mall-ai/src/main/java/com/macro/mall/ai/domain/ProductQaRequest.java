package com.macro.mall.ai.domain;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ProductQaRequest {
    
    @NotNull(message = "商品ID不能为空")
    @ApiModelProperty(value = "商品ID", required = true, example = "1")
    private Long productId;
    
    @NotBlank(message = "问题不能为空")
    @Size(max = 500, message = "问题长度不能超过500字符")
    @ApiModelProperty(value = "用户问题", required = true, example = "这个商品的材质是什么？")
    private String question;
    
    @ApiModelProperty(value = "商品名称", example = "iPhone 15 Pro")
    private String productName;
    
    @ApiModelProperty(value = "商品品牌", example = "Apple")
    private String productBrand;
    
    @ApiModelProperty(value = "商品价格", example = "7999")
    private String productPrice;
    
    @ApiModelProperty(value = "商品副标题/描述", example = "钛金属设计，A17 Pro芯片")
    private String productSubTitle;
    
    @Size(max = 2000, message = "对话历史长度不能超过2000字符")
    @ApiModelProperty(value = "对话历史上下文，用于多轮对话", example = "用户: 这个手机拍照怎么样？\nAI: 该手机配备4800万像素摄像头...")
    private String conversationHistory;

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
    public String getConversationHistory() { return conversationHistory; }
    public void setConversationHistory(String conversationHistory) { this.conversationHistory = conversationHistory; }
}
