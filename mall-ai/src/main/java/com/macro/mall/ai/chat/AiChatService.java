package com.macro.mall.ai.chat;

import com.macro.mall.ai.config.PromptProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * AI 聊天服务 (AI Chat Service) - Stage 7
 *
 * <p>封装 Spring AI {@link ChatClient}，为业务层提供简洁的 API。</p>
 *
 * <p><b>Stage 7 升级：</b>新增 {@code streamChat(...)} 返回 {@link Flux}，
 * 用于 SSE 流式输出端点。</p>
 *
 * @author alan
 * @since 2026-06
 */
@Service
public class AiChatService {

    private final ChatClient chatClient;
    private final PromptProperties prompts;

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

    /**
     * Stage 4: 结构化输出 - 直接对话返回 record
     */
    public <T> T chatEntity(String systemPrompt, String userContent, Class<T> responseType) {
        BeanOutputConverter<T> converter = new BeanOutputConverter<>(responseType);
        String enhancedPrompt = systemPrompt + "\n" + converter.getFormat();
        return chatClient.prompt()
            .system(enhancedPrompt)
            .user(userContent)
            .call()
            .entity(responseType);
    }

    /**
     * Stage 4: 结构化输出 - 模板渲染后返回 record
     */
    public <T> T renderAndChatEntity(String template, Map<String, Object> vars,
                                     String userContent, Class<T> responseType) {
        String rendered = renderTemplate(template, vars);
        return chatEntity(rendered, userContent, responseType);
    }

    /**
     * Stage 7: 流式输出 - 返回逐 token 的 Flux
     *
     * <p>前端通过 SSE (text/event-stream) 消费 {@link Flux}，实现"打字机"效果。</p>
     */
    public Flux<String> streamChat(String systemPrompt, String userContent) {
        return chatClient.prompt()
            .system(systemPrompt)
            .user(userContent)
            .stream()
            .content();
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
