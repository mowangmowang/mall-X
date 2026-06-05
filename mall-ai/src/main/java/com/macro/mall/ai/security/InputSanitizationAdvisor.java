package com.macro.mall.ai.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 输入清理 Advisor (Input Sanitization Advisor) - Stage 5
 *
 * <p>Spring AI 的 {@link CallAdvisor} 实现，拦截所有 ChatClient 调用，
 * 自动清洗 {@link UserMessage} 内容：</p>
 * <ul>
 *   <li>剥离控制字符（保留换行/制表）</li>
 *   <li>检测并 warn Prompt Injection 模式</li>
 *   <li>截断超长输入</li>
 *   <li>trim 首尾空白</li>
 * </ul>
 *
 * <p>业务层无需关心输入清理，{@code AiChatService} 注入本 Advisor 后
 * 所有 {@code chat()} / {@code chatEntity()} 调用都会自动清洗。</p>
 *
 * @author alan
 * @since 2026-06
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class InputSanitizationAdvisor implements CallAdvisor {

    private static final Logger log = LoggerFactory.getLogger(InputSanitizationAdvisor.class);

    /** 常见 Prompt Injection 攻击模式（中英文双语） */
    private static final Pattern DANGEROUS_PATTERN = Pattern.compile(
        "忽略.*指令|ignore.*instruction|忘记.*之前|forget.*previous|" +
        "覆盖.*规则|override.*rule|你是一个.*助手|you are a.*assistant|" +
        "现在你是|now you are|扮演|act as|系统提示|system prompt|" +
        "系统消息|system message|初始指令|initial instruction",
        Pattern.CASE_INSENSITIVE);

    /** 控制字符正则（保留 \n=\x0A, \r=\x0D, \t=\x09） */
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]");

    private final SanitizationProperties props;

    public InputSanitizationAdvisor(SanitizationProperties props) {
        this.props = props;
    }

    @Override
    public String getName() {
        return "input-sanitization-advisor";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        Prompt sanitizedPrompt = sanitizePrompt(request.prompt());
        ChatClientRequest sanitizedRequest = ChatClientRequest.builder()
            .prompt(sanitizedPrompt)
            .context(request.context())
            .build();
        return chain.nextCall(sanitizedRequest);
    }

    private Prompt sanitizePrompt(Prompt prompt) {
        List<Message> original = prompt.getInstructions();
        List<Message> sanitized = new ArrayList<>(original.size());
        boolean changed = false;
        for (Message m : original) {
            if (m instanceof UserMessage um) {
                String cleaned = sanitize(um.getText());
                if (!cleaned.equals(um.getText())) {
                    sanitized.add(new UserMessage(cleaned));
                    changed = true;
                } else {
                    sanitized.add(um);
                }
            } else {
                sanitized.add(m);
            }
        }
        return changed ? new Prompt(sanitized) : prompt;
    }

    private String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String result = input;

        if (Boolean.TRUE.equals(props.stripControlChars())) {
            result = CONTROL_CHARS.matcher(result).replaceAll("");
        }

        if (Boolean.TRUE.equals(props.detectPromptInjection())
            && DANGEROUS_PATTERN.matcher(result).find()) {
            log.warn("检测到潜在 Prompt Injection 攻击，输入: {}",
                result.substring(0, Math.min(100, result.length())));
        }

        int max = props.maxLength() != null ? props.maxLength() : 5000;
        if (result.length() > max) {
            log.warn("输入内容过长，截断至 {} 字符", max);
            result = result.substring(0, max);
        }

        return result.trim();
    }
}
