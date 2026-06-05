package com.macro.mall.ai.client;

/**
 * 聊天消息 (Chat Message)
 *
 * <p>Stage 1 已迁移为 Java 17 record。Stage 3 会被 Spring AI 的
 * {@code org.springframework.ai.chat.messages.Message} 体系替换。</p>
 *
 * <p><b>角色类型：</b></p>
 * <ul>
 *   <li>system - 系统提示词</li>
 *   <li>user - 用户消息</li>
 *   <li>assistant - AI 回复</li>
 * </ul>
 *
 * @author alan
 * @since 1.0
 */
public record ChatMessage(String role, String content) {

    public static ChatMessage system(String content) {
        return new ChatMessage("system", content);
    }

    public static ChatMessage user(String content) {
        return new ChatMessage("user", content);
    }

    public static ChatMessage assistant(String content) {
        return new ChatMessage("assistant", content);
    }
}
