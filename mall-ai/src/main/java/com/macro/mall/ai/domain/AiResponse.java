package com.macro.mall.ai.domain;

import io.swagger.annotations.ApiModelProperty;

/**
 * AI 响应数据结构 (AI Response)
 * 
 * <p>封装 AI 模型生成的回复内容。</p>
 * 
 * <p><b>使用场景：</b></p>
 * <ul>
 *   <li>商品问答接口返回 AI 对商品的回答</li>
 *   <li>未来可扩展其他 AI 功能的统一响应格式</li>
 * </ul>
 * 
 * <p><b>响应示例：</b></p>
 * <pre>{@code
 * {
 *   "reply": "该手机配备4800万像素摄像头，支持光学防抖和夜景模式。根据商品描述，拍照效果满足日常使用需求。"
 * }
 * }</pre>
 * 
 * @author alan
 * @since 1.0
 */
public class AiResponse {
    
    /**
     * AI 回复内容 (AI Reply Content)
     * <p>AI 模型生成的文本回复，经过安全过滤和格式校验</p>
     */
    @ApiModelProperty(value = "AI 回复内容", example = "根据商品信息，这款手机采用钛金属设计...")
    private String reply;

    /**
     * 默认构造函数 (Default Constructor)
     * <p>用于 JSON 反序列化</p>
     */
    public AiResponse() {}

    /**
     * 带参构造函数 (Parameterized Constructor)
     * 
     * @param reply AI 回复内容
     */
    public AiResponse(String reply) { 
        this.reply = reply; 
    }

    /**
     * 获取AI回复内容 (Get AI Reply)
     * 
     * @return AI 生成的回复文本
     */
    public String getReply() { 
        return reply; 
    }
    
    /**
     * 设置AI回复内容 (Set AI Reply)
     * 
     * @param reply AI 生成的回复文本
     */
    public void setReply(String reply) { 
        this.reply = reply; 
    }
}
