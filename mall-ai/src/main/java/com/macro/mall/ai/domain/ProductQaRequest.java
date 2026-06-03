package com.macro.mall.ai.domain;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 商品问答请求参数 (Product Q&A Request)
 * 
 * <p>封装用户对商品的提问及相关商品信息，用于调用 AI 模型生成回答。</p>
 * 
 * <p><b>使用场景：</b></p>
 * <ul>
 *   <li>用户在商品详情页点击“AI 助手”按钮提问</li>
 *   <li>多轮对话中追问商品细节（通过 conversationHistory 传递历史）</li>
 * </ul>
 * 
 * <p><b>必填字段：</b></p>
 * <ul>
 *   <li>productId - 商品ID，用于日志记录和数据分析</li>
 *   <li>question - 用户问题，AI 将根据此问题生成回答</li>
 * </ul>
 * 
 * <p><b>可选字段：</b></p>
 * <ul>
 *   <li>productName、productBrand、productPrice、productSubTitle - 商品信息，越完整 AI 回答越准确</li>
 *   <li>conversationHistory - 对话历史，用于支持多轮对话</li>
 * </ul>
 * 
 * @author alan
 * @since 1.0
 */
public class ProductQaRequest {
    
    /**
     * 商品ID (Product ID)
     * <p>用于标识具体商品，便于日志追踪和数据分析</p>
     */
    @NotNull(message = "商品ID不能为空")
    @ApiModelProperty(value = "商品ID", required = true, example = "1")
    private Long productId;
    
    /**
     * 用户问题 (User Question)
     * <p>用户对商品的提问，如“这款手机拍照效果怎么样？”</p>
     * <p>最大长度：500 字符</p>
     */
    @NotBlank(message = "问题不能为空")
    @Size(max = 500, message = "问题长度不能超过500字符")
    @ApiModelProperty(value = "用户问题", required = true, example = "这个商品的材质是什么？")
    private String question;
    
    /**
     * 商品名称 (Product Name)
     * <p>如“Redmi Note 13 Pro”</p>
     */
    @ApiModelProperty(value = "商品名称", example = "iPhone 15 Pro")
    private String productName;
    
    /**
     * 商品品牌 (Product Brand)
     * <p>如“小米”、“Apple”、“华为”</p>
     */
    @ApiModelProperty(value = "商品品牌", example = "Apple")
    private String productBrand;
    
    /**
     * 商品价格 (Product Price)
     * <p>单位：元，如“1999”、“7999.00”</p>
     */
    @ApiModelProperty(value = "商品价格", example = "7999")
    private String productPrice;
    
    /**
     * 商品副标题/描述 (Product Subtitle/Description)
     * <p>商品的简短描述或卖点，如“性能小钢炮 5G 手机”</p>
     */
    @ApiModelProperty(value = "商品副标题/描述", example = "钛金属设计，A17 Pro芯片")
    private String productSubTitle;
    
    /**
     * 对话历史上下文 (Conversation History)
     * <p>用于多轮对话场景，记录之前的对话内容</p>
     * <p>格式示例："用户: 这个手机拍照怎么样？\nAI: 该手机配备4800万像素摄像头..."</p>
     * <p>最大长度：2000 字符</p>
     */
    @Size(max = 2000, message = "对话历史长度不能超过2000字符")
    @ApiModelProperty(value = "对话历史上下文，用于多轮对话", example = "用户: 这个手机拍照怎么样？\nAI: 该手机配备4800万像素摄像头...")
    private String conversationHistory;

    // Getter and Setter methods with detailed comments
    
    /**
     * 获取商品ID (Get Product ID)
     * @return 商品ID
     */
    public Long getProductId() { return productId; }
    
    /**
     * 设置商品ID (Set Product ID)
     * @param productId 商品ID
     */
    public void setProductId(Long productId) { this.productId = productId; }
    
    /**
     * 获取用户问题 (Get User Question)
     * @return 用户问题文本
     */
    public String getQuestion() { return question; }
    
    /**
     * 设置用户问题 (Set User Question)
     * @param question 用户问题文本
     */
    public void setQuestion(String question) { this.question = question; }
    
    /**
     * 获取商品名称 (Get Product Name)
     * @return 商品名称
     */
    public String getProductName() { return productName; }
    
    /**
     * 设置商品名称 (Set Product Name)
     * @param productName 商品名称
     */
    public void setProductName(String productName) { this.productName = productName; }
    
    /**
     * 获取商品品牌 (Get Product Brand)
     * @return 商品品牌
     */
    public String getProductBrand() { return productBrand; }
    
    /**
     * 设置商品品牌 (Set Product Brand)
     * @param productBrand 商品品牌
     */
    public void setProductBrand(String productBrand) { this.productBrand = productBrand; }
    
    /**
     * 获取商品价格 (Get Product Price)
     * @return 商品价格字符串
     */
    public String getProductPrice() { return productPrice; }
    
    /**
     * 设置商品价格 (Set Product Price)
     * @param productPrice 商品价格字符串
     */
    public void setProductPrice(String productPrice) { this.productPrice = productPrice; }
    
    /**
     * 获取商品副标题/描述 (Get Product Subtitle)
     * @return 商品副标题或描述文本
     */
    public String getProductSubTitle() { return productSubTitle; }
    
    /**
     * 设置商品副标题/描述 (Set Product Subtitle)
     * @param productSubTitle 商品副标题或描述文本
     */
    public void setProductSubTitle(String productSubTitle) { this.productSubTitle = productSubTitle; }
    
    /**
     * 获取对话历史 (Get Conversation History)
     * @return 对话历史文本
     */
    public String getConversationHistory() { return conversationHistory; }
    
    /**
     * 设置对话历史 (Set Conversation History)
     * @param conversationHistory 对话历史文本
     */
    public void setConversationHistory(String conversationHistory) { this.conversationHistory = conversationHistory; }
}
