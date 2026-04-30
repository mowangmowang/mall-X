package com.macro.mall.ai.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 输入清理工具类
 * 用于防止 Prompt Injection 攻击和过滤危险输入
 */
public class InputSanitizer {

    private static final Logger log = LoggerFactory.getLogger(InputSanitizer.class);

    // 常见的 Prompt Injection 攻击模式
    private static final List<String> DANGEROUS_PATTERNS = Arrays.asList(
            // 忽略指令类
            "忽略.*指令",
            "ignore.*instruction",
            "忘记.*之前",
            "forget.*previous",
            "覆盖.*规则",
            "override.*rule",
            
            // 角色伪装类
            "你是一个.*助手",
            "you are a.*assistant",
            "现在你是",
            "now you are",
            "扮演",
            "act as",
            
            // 系统提示类
            "系统提示",
            "system prompt",
            "系统消息",
            "system message",
            "初始指令",
            "initial instruction",
            
            // 命令执行类
            "执行命令",
            "execute command",
            "运行代码",
            "run code",
            "eval",
            "javascript:",
            "data:",
            
            // SQL 注入类
            "DROP TABLE",
            "INSERT INTO",
            "DELETE FROM",
            "UNION SELECT",
            
            // XSS 攻击类
            "<script",
            "javascript:",
            "onerror=",
            "onclick="
    );

    private static final Pattern DANGEROUS_PATTERN_REGEX;

    static {
        // 构建正则表达式，不区分大小写
        String pattern = String.join("|", DANGEROUS_PATTERNS);
        DANGEROUS_PATTERN_REGEX = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }

    /**
     * 清理用户输入，移除潜在的危险内容
     *
     * @param input 原始输入
     * @return 清理后的输入
     */
    public static String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String sanitized = input;

        // 1. 移除控制字符（保留换行和制表符）
        sanitized = sanitized.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");

        // 2. 检测并警告潜在的 Prompt Injection 攻击
        if (DANGEROUS_PATTERN_REGEX.matcher(sanitized).find()) {
            log.warn("检测到潜在的 Prompt Injection 攻击，输入内容: {}", input.substring(0, Math.min(100, input.length())));
            // 这里可以选择拒绝请求或继续清理
            // throw new IllegalArgumentException("输入包含不安全的内容");
        }

        // 3. 限制输入长度（防止超长输入）
        if (sanitized.length() > 5000) {
            log.warn("输入内容过长，截断至5000字符");
            sanitized = sanitized.substring(0, 5000);
        }

        // 4. 去除首尾空白
        sanitized = sanitized.trim();

        return sanitized;
    }

    /**
     * 清理商品相关信息（相对安全，但仍需基本清理）
     *
     * @param input 原始输入
     * @return 清理后的输入
     */
    public static String sanitizeProductInfo(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // 商品信息的清理可以相对宽松，主要移除控制字符
        String sanitized = input.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");
        
        // 限制长度
        if (sanitized.length() > 1000) {
            sanitized = sanitized.substring(0, 1000);
        }

        return sanitized.trim();
    }

    /**
     * 验证输入是否为纯文本（不包含 HTML/XML 标签）
     *
     * @param input 待验证的输入
     * @return 是否安全
     */
    public static boolean isPlainText(String input) {
        if (input == null || input.isEmpty()) {
            return true;
        }

        // 检测是否包含 HTML/XML 标签
        Pattern tagPattern = Pattern.compile("<[^>]*>");
        return !tagPattern.matcher(input).find();
    }
}
