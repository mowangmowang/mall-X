package com.macro.mall.ai.domain;

public class AiResponse {
    private String reply;

    public AiResponse() {}

    public AiResponse(String reply) { this.reply = reply; }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
}
