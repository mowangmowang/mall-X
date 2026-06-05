package com.macro.mall.ai.chat;

import com.macro.mall.ai.config.PromptProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * AI 聊天服务 (AI Chat Service) - Stage 3
 *
 * <p>封装 Spring AI {@link ChatClient}，为业务层提供简洁的 API。
 * 替代 Stage 1-2 时期的 {@code AiClient} 接口 + {@code OpenAiCompatibleClient} 手写实现。</p>
 *
 * <p><b>职责：</b></p>
 * <ul>
 *   <li>管理 {@link ChatClient} 单例（线程安全）</li>
 *   <li>提供字符串返回的同步调用</li>
 *   <li>提供模板渲染（{@code {placeholder}}）后调用的方法</li>
 *   <li>注入 {@link PromptProperties}，业务代码可直接 {@code prompts.productQaSystem()}</li>
 * </ul>
 *
 * <p><b>Stage 4 计划：</b>新增 {@code <T> T chatEntity(...)} 重载，用
 * {@code BeanOutputConverter} 替代手写 JSON 解析。</p>
 *
 * @author alan
 * @since 2026-06
 */
@Service
public class AiChatService {

    private final ChatClient chatClient;
    private final PromptProperties prompts;

    /**
     * 直接注入 ChatClient（由 Spring AI 自动配置 + {@link ChatClient.Builder#build()} 创建）
     *
     * <p>设计为接受 ChatClient 而非 Builder，便于单元测试时直接 mock。</p>
     */
    public AiChatService(ChatClient chatClient, PromptProperties prompts) {
        this.chatClient = chatClient;
        this.prompts = prompts;
    }

    /**
     * 直接对话：传入已渲染的 system prompt 和用户内容
     */
    public String chat(String systemPrompt, String userContent) {
        return chatClient.prompt()
            .system(systemPrompt)
            .user(userContent)
            .call()
            .content();
    }

    /**
     * 模板对话：先用 {@code {placeholder}} 占位符渲染模板，再调用
     *
     * <p>Stage 3 用 {@code String.replace} 简单实现（Stage 4+ 可换 {@code PromptTemplate}）。</p>
     */
    public String chat(String template, Map<String, Object> vars, String userContent) {
        String rendered = renderTemplate(template, vars);
        return chat(rendered, userContent);
    }

    /**
     * 渲染 yml 模板 + 调用（Stage 3 业务层主要用此方法）
     */
    public String renderAndChat(String template, Map<String, Object> vars, String userContent) {
        return chat(template, vars, userContent);
    }

    private static String renderTemplate(String template, Map<String, Object> vars) {
        if (vars == null || vars.isEmpty()) return template;
        String result = template;
        for (Map.Entry<String, Object> e : vars.entrySet()) {
            result = result.replace("{" + e.getKey() + "}", String.valueOf(e.getValue()));
        }
        return result;
    }

    public PromptProperties getPrompts() {
        return prompts;
    }
}
