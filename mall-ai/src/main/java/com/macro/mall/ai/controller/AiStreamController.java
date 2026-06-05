package com.macro.mall.ai.controller;

import com.macro.mall.ai.domain.ProductQaRequest;
import com.macro.mall.ai.service.AiAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * AI 流式输出控制器 (AI Stream Controller) - Stage 7
 *
 * <p>提供 SSE (Server-Sent Events) 端点，实现"打字机"效果。</p>
 *
 * <p><b>端点：</b></p>
 * <ul>
 *   <li>{@code POST /ai/product/qa/stream} - 商品问答流式</li>
 * </ul>
 *
 * <p><b>响应格式：</b>{@code text/event-stream}，每行 {@code data: <chunk>}</p>
 *
 * <p><b>前端用法：</b></p>
 * <pre>{@code
 * const response = await fetch('/ai/product/qa/stream', {
 *   method: 'POST',
 *   headers: { 'Content-Type': 'application/json', 'Accept': 'text/event-stream' },
 *   body: JSON.stringify(request)
 * });
 * const reader = response.body.getReader();
 * const decoder = new TextDecoder();
 * while (true) {
 *   const { done, value } = await reader.read();
 *   if (done) break;
 *   const chunk = decoder.decode(value);  // "data: 你\ndata: 好\n\n"
 *   // 解析每个 data: 行
 * }
 * }</pre>
 *
 * @author alan
 * @since 2026-06
 */
@RestController
@RequestMapping("/ai")
@Tag(name = "AiStreamController", description = "AI 流式输出 (SSE)")
@RequiredArgsConstructor
public class AiStreamController {

    private final AiAssistantService aiAssistantService;

    @Operation(summary = "商品问答（流式）",
               description = "通过 SSE 逐 token 返回 AI 回复，实现打字机效果")
    @PostMapping(value = "/product/qa/stream",
                 produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> productQaStream(@Valid @RequestBody ProductQaRequest request) {
        return aiAssistantService.streamChatAboutProduct(request);
    }
}
