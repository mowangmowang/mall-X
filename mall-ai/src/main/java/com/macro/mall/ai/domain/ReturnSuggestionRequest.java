package com.macro.mall.ai.domain;

import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 退货建议请求参数 (Return Suggestion Request)
 * 
 * <p>封装用户描述的售后问题及相关商品信息，用于 AI 多轮引导对话。</p>
 * 
 * <p><b>业务流程：</b></p>
 * <ol>
 *   <li>前端发起第1次请求（step=1），用户提供初步问题描述</li>
 *   <li>AI 返回引导问题，前端显示给用户</li>
 *   <li>用户回答后，前端发起第2次请求（step=2），传递用户回答</li>
 *   <li>AI 再次返回引导问题</li>
 *   <li>前端发起第3次请求（step=3），整合所有对话历史</li>
 *   <li>AI 返回最终建议（退货原因、描述等）</li>
 * </ol>
 * 
 * <p><b>必填字段：</b></p>
 * <ul>
 *   <li>issue - 用户问题描述（每次调用时传递当前步骤的用户输入）</li>
 * </ul>
 * 
 * <p><b>状态管理字段：</b></p>
 * <ul>
 *   <li>step - 当前引导步骤（1-3），由前端维护并递增</li>
 *   <li>sessionId - 会话ID，用于关联同一用户的多次请求（前端生成 UUID）</li>
 * </ul>
 * 
 * @author alan
 * @since 1.0
 */
public class ReturnSuggestionRequest {
    
    /**
     * 用户问题描述 (User Issue Description)
     * <p>用户对售后问题的描述，如“手机有问题”、“屏幕有裂痕”等</p>
     * <p>在第3步时，前端会将多轮对话历史用分号拼接后传递</p>
     * <p>最大长度：1000 字符</p>
     */
    @NotBlank(message = "问题描述不能为空")
    @Size(max = 1000, message = "问题描述长度不能超过1000字符")
    @ApiModelProperty(value = "用户问题描述", required = true, example = "商品收到后发现屏幕有裂痕")
    private String issue;
    
    /**
     * 商品名称 (Product Name)
     * <p>可选字段，帮助 AI 更准确地理解问题背景</p>
     */
    @ApiModelProperty(value = "商品名称", example = "iPhone 15 Pro")
    private String productName;
    
    /**
     * 商品属性 (Product Attributes)
     * <p>如“颜色:黑色,容量:256GB”，帮助 AI 了解具体配置</p>
     */
    @ApiModelProperty(value = "商品属性", example = "颜色:黑色,容量:256GB")
    private String productAttr;
    
    /**
     * 订单编号 (Order Serial Number)
     * <p>用于日志记录和数据分析，可选字段</p>
     */
    @ApiModelProperty(value = "订单编号", example = "202401010001")
    private String orderSn;

    /**
     * 会话ID (Session ID)
     * <p>用于多轮对话状态管理，关联同一用户的多次请求</p>
     * <p>由前端生成 UUID，首次调用时创建，后续调用保持不变</p>
     */
    @ApiModelProperty(value = "会话ID，用于多轮对话状态管理", example = "session_123456")
    private String sessionId;

    /**
     * 当前引导步骤 (Current Step)
     * <p>取值范围：1-3</p>
     * <ul>
     *   <li>1 - 询问故障现象</li>
     *   <li>2 - 追问细节</li>
     *   <li>3 - 确认并给出建议</li>
     * </ul>
     * <p>由前端维护，每次调用后自增</p>
     */
    @ApiModelProperty(value = "当前引导步骤 (1-3)", example = "1")
    private Integer step;

    // Getter and Setter methods with detailed comments
    
    /**
     * 获取问题描述 (Get Issue Description)
     * @return 用户问题描述文本
     */
    public String getIssue() { return issue; }
    
    /**
     * 设置问题描述 (Set Issue Description)
     * @param issue 用户问题描述文本
     */
    public void setIssue(String issue) { this.issue = issue; }
    
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
     * 获取商品属性 (Get Product Attributes)
     * @return 商品属性字符串
     */
    public String getProductAttr() { return productAttr; }
    
    /**
     * 设置商品属性 (Set Product Attributes)
     * @param productAttr 商品属性字符串
     */
    public void setProductAttr(String productAttr) { this.productAttr = productAttr; }
    
    /**
     * 获取订单编号 (Get Order Serial Number)
     * @return 订单编号
     */
    public String getOrderSn() { return orderSn; }
    
    /**
     * 设置订单编号 (Set Order Serial Number)
     * @param orderSn 订单编号
     */
    public void setOrderSn(String orderSn) { this.orderSn = orderSn; }
    
    /**
     * 获取会话ID (Get Session ID)
     * @return 会话ID字符串
     */
    public String getSessionId() { return sessionId; }
    
    /**
     * 设置会话ID (Set Session ID)
     * @param sessionId 会话ID字符串
     */
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    /**
     * 获取当前步骤 (Get Current Step)
     * @return 当前引导步骤（1-3）
     */
    public Integer getStep() { return step; }
    
    /**
     * 设置当前步骤 (Set Current Step)
     * @param step 当前引导步骤（1-3）
     */
    public void setStep(Integer step) { this.step = step; }
}
