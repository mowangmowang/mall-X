package com.macro.mall.ai.chat;

import com.macro.mall.ai.config.PromptProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * AI 聊天服务 (AI Chat Service) - Stage 7
 *
 * <p>封装 Spring AI {@link ChatClient}，为业务层提供简洁的 API。</p>
 *
 * <p><b>Stage 7 升级：</b>新增 {@code streamChat(...)} 返回 servlet 原生
 * {@link SseEmitter}，避免引入 webflux 栈，Spring MVC 异步任务调度直接推流。</p>
 *
 * @author alan
 * @since 2026-06
 */
@Service
public class AiChatService {

    /** SSE 连接默认 60s 超时，AI 长文输出也不会触发 */
    private static final long SSE_TIMEOUT_MS = 60_000L;

    /** 异步订阅调度器：避免在 Tomcat worker 线程上跑 reactive 回调 */
    private static final ScheduledExecutorService SSE_SCHEDULER =
        Executors.newScheduledThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
            r -> {
                Thread t = new Thread(r, "mall-ai-sse");
                t.setDaemon(true);
                return t;
            });

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
     * Stage 7: 流式输出 - 返回 servlet 原生 SseEmitter
     *
     * <p>内部订阅 Spring AI {@code chatClient.stream().content()} 返回的
     * {@link Flux}，逐 chunk 通过 {@link SseEmitter#send} 推给客户端，
     * 实现"打字机"效果。</p>
     *
     * <p><b>为什么选 SseEmitter 而不是直接返回 Flux：</b>mvc 栈返回 {@code Flux}
     * 会强制引入 webflux 容器（servlet 栈让步），而本服务其他端点都是同步
     * servlet 风格。SseEmitter 走 {@code spring-boot-starter-web} 的异步
     * 请求处理（{@code AsyncContext}），不切换容器。</p>
     */
    public SseEmitter streamChat(String systemPrompt, String userContent) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        Flux<String> flux = chatClient.prompt()
            .system(systemPrompt)
            .user(userContent)
            .stream()
            .content();
        flux.subscribeOn(reactor.core.scheduler.Schedulers.fromExecutor(SSE_SCHEDULER))
            .subscribe(
                chunk -> {
                    try {
                        emitter.send(SseEmitter.event().data(chunk));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError,
                emitter::complete);
        return emitter;
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
