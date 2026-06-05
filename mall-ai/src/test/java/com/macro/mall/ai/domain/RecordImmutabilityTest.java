package com.macro.mall.ai.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Record 不可变性 + 访问器测试 (Stage 1)
 *
 * <p>验证 DTO 改为 Java 17 record 后：</p>
 * <ul>
 *   <li>字段只能通过构造器设置</li>
 *   <li>访问器方法（无 get 前缀）正确返回</li>
 *   <li>字段不能重新赋值（编译期保证）</li>
 *   <li>equals/hashCode/toString 由 record 自动生成</li>
 * </ul>
 *
 * @author alan
 * @since 2026-06
 */
class RecordImmutabilityTest {

    @Test
    void aiResponse_recordAccessor() {
        AiResponse r = new AiResponse("hello");
        assertEquals("hello", r.reply());
    }

    @Test
    void aiResponse_equalsAndHashCode() {
        AiResponse a = new AiResponse("x");
        AiResponse b = new AiResponse("x");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new AiResponse("y"));
    }

    @Test
    void productQaRequest_recordAccessor() {
        ProductQaRequest r = new ProductQaRequest(1L, "q", "name", "brand", "99", "sub", null);
        assertEquals(1L, r.productId());
        assertEquals("q", r.question());
        assertEquals("name", r.productName());
        assertNull(r.conversationHistory());
    }

    @Test
    void returnSuggestionRequest_recordAccessor() {
        ReturnSuggestionRequest r = new ReturnSuggestionRequest(
            "issue", "name", "attr", "sn", "sid", 3);
        assertEquals("issue", r.issue());
        assertEquals(3, r.step());
    }

    @Test
    void returnSuggestionResult_recordAccessor() {
        ReturnSuggestionResult r = new ReturnSuggestionResult(
            "reason", "desc", "cat", "high", "guide", true, "note");
        assertEquals("reason", r.suggestedReason());
        assertTrue(r.finished());
    }

    @Test
    void recordToString_includesAllFields() {
        AiResponse r = new AiResponse("hello");
        String s = r.toString();
        assertTrue(s.contains("hello"));
        assertTrue(s.contains("reply"));
    }
}
