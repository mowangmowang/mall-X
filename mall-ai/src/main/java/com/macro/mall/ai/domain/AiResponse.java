package com.macro.mall.ai.domain;

import io.swagger.annotations.ApiModelProperty;

public class AiResponse {
    
    @ApiModelProperty(value = "AI 回复内容", example = "根据商品信息，这款手机采用钛金属设计...")
    private String reply;

    public AiResponse() {}

    public AiResponse(String reply) { this.reply = reply; }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
}
