# Stage 4: BeanOutputConverter 替换手写 JSON 解析

## 🎯 目标
- `parseReturnSuggestion` 90+ 行手写 JSON 解析代码 → 5 行 `BeanOutputConverter` 调用
- AI 自动注入 JSON schema 到 prompt（不再需要手写"严格只返回 JSON"指令）
- 类型安全（编译期发现字段拼写错误）

## 📂 涉及文件

### 修改
- `mall-ai/src/main/java/com/macro/mall/ai/service/impl/AiAssistantServiceImpl.java`（核心改造）
- `mall-ai/src/main/java/com/macro/mall/ai/chat/AiChatService.java`（加 `entity(Class<T>)` 重载）
- `mall-ai/src/main/resources/application.yml`（`ai.prompts.return-suggestion-system` 微调）

### 新增
- `mall-ai/src/test/java/com/macro/mall/ai/service/ReturnSuggestionBeanOutputTest.java`

## 🔨 实施步骤

### Step 4.1: AiChatService 加 entity 重载
```java
public <T> T chatEntity(String systemPromptTemplate, Map<String, Object> vars,
                        String userContent, Class<T> responseType) {
    String rendered = new PromptTemplate(systemPromptTemplate).render(vars);
    return chatClient.prompt()
        .system(rendered)
        .user(userContent)
        .call()
        .entity(responseType);
}
```

### Step 4.2: AiAssistantServiceImpl 用 BeanOutputConverter
```java
@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private final AiChatService aiChat;
    private final ReturnReasonService returnReasonService;
    private final PromptProperties prompts;

    private BeanOutputConverter<ReturnSuggestionResult> returnConverter;

    @PostConstruct
    void init() {
        this.returnConverter = new BeanOutputConverter<>(ReturnSuggestionResult.class);
    }

    @Override
    public ReturnSuggestionResult suggestReturn(ReturnSuggestionRequest req) {
        String sanitized = InputSanitizer.sanitize(req.issue());
        Map<String, Object> vars = Map.of(
            "reasons", String.join("、", returnReasonService.getEnabledReturnReasons()),
            "format", returnConverter.getFormat()
        );
        String systemPrompt = new PromptTemplate(prompts.returnSuggestionSystem())
            .render(vars);

        String content = String.format("当前引导步骤：%d/3\n用户描述的问题：%s\n...",
            req.step() == null ? 1 : req.step(), sanitized);

        try {
            ReturnSuggestionResult result = aiChat.chatEntity(
                systemPrompt, Map.of(), content, ReturnSuggestionResult.class);

            int step = req.step() == null ? 1 : req.step();
            if (step >= 3) {
                return enforceStep3(result, req.issue());
            }
            return result;
        } catch (Exception e) {
            log.warn("AI 退货建议解析失败，使用 fallback. err={}", e.getMessage());
            return fallbackResult(req);
        }
    }

    private ReturnSuggestionResult enforceStep3(ReturnSuggestionResult r, String issue) {
        return new ReturnSuggestionResult(
            r.suggestedReason().isEmpty() ? prompts.returnReasonDefault() : r.suggestedReason(),
            r.suggestedDescription().isEmpty() ? issue : r.suggestedDescription(),
            r.category().isEmpty() ? prompts.categoryDefault() : r.category(),
            r.confidence() == null ? "medium" : r.confidence(),
            r.guideQuestion() == null ? "" : r.guideQuestion(),
            true,
            "已根据对话历史生成建议"
        );
    }

    private ReturnSuggestionResult fallbackResult(ReturnSuggestionRequest req) {
        int step = req.step() == null ? 1 : req.step();
        if (step >= 3) {
            return new ReturnSuggestionResult(
                prompts.returnReasonDefault(),
                req.issue(),
                prompts.categoryDefault(),
                "low", "已为您生成建议，请确认。", true,
                "解析失败，已生成默认建议"
            );
        }
        return new ReturnSuggestionResult(
            "", "", "", "low", "抱歉我没听清，请具体描述问题。",
            false, "解析失败，请重试"
        );
    }
}
```

### Step 4.3: application.yml 的 prompt 微调
```yaml
ai:
  prompts:
    return-suggestion-system: |
      你是专业电商售后客服助手...
      {format}    ← BeanOutputConverter 自动填充 JSON schema 描述
      ...
```

## 🧪 测试细节

### 新增：`ReturnSuggestionBeanOutputTest.java`
```java
@ExtendWith(MockitoExtension.class)
class ReturnSuggestionBeanOutputTest {

    @Mock AiChatService aiChat;
    @Mock ReturnReasonService returnReasonService;
    @Mock PromptProperties prompts;
    @InjectMocks AiAssistantServiceImpl service;  // ★ 触发 @PostConstruct init()

    @BeforeEach
    void setUp() {
        when(prompts.returnReasonDefault()).thenReturn("质量问题");
        when(prompts.categoryDefault()).thenReturn("硬件故障");
        when(returnReasonService.getEnabledReturnReasons())
            .thenReturn(List.of("质量问题", "商品损坏"));
    }

    @Test
    void suggestReturn_step3_aiReturnsValidJson_returnsAsIs() {
        ReturnSuggestionResult aiResult = new ReturnSuggestionResult(
            "商品损坏", "屏幕有裂痕", "硬件故障", "high", "明白了", true, "分析");
        when(aiChat.chatEntity(anyString(), anyMap(), anyString(), eq(ReturnSuggestionResult.class)))
            .thenReturn(aiResult);

        ReturnSuggestionRequest req = new ReturnSuggestionRequest(
            "屏幕有裂痕", null, null, null, null, 3);

        ReturnSuggestionResult result = service.suggestReturn(req);

        assertThat(result.suggestedReason()).isEqualTo("商品损坏");
        assertThat(result.finished()).isTrue();
    }

    @Test
    void suggestReturn_step3_aiReturnsEmptyReason_usesDefault() {
        ReturnSuggestionResult aiResult = new ReturnSuggestionResult(
            "", "", "", "medium", "请问还有其他问题吗", true, "分析");
        when(aiChat.chatEntity(anyString(), anyMap(), anyString(), eq(ReturnSuggestionResult.class)))
            .thenReturn(aiResult);

        ReturnSuggestionRequest req = new ReturnSuggestionRequest(
            "屏幕有裂痕", null, null, null, null, 3);

        ReturnSuggestionResult result = service.suggestReturn(req);

        assertThat(result.suggestedReason()).isEqualTo("质量问题");
        assertThat(result.suggestedDescription()).isEqualTo("屏幕有裂痕");
        assertThat(result.category()).isEqualTo("硬件故障");
        assertThat(result.finished()).isTrue();
    }

    @Test
    void suggestReturn_step3_throws_returnsFallback() {
        when(aiChat.chatEntity(anyString(), anyMap(), anyString(), eq(ReturnSuggestionResult.class)))
            .thenThrow(new RuntimeException("AI service down"));

        ReturnSuggestionRequest req = new ReturnSuggestionRequest(
            "屏幕有裂痕", null, null, null, null, 3);

        ReturnSuggestionResult result = service.suggestReturn(req);

        assertThat(result.suggestedReason()).isEqualTo("质量问题");
        assertThat(result.confidence()).isEqualTo("low");
        assertThat(result.finished()).isTrue();
        assertThat(result.analysisNote()).contains("解析失败");
    }

    @Test
    void suggestReturn_step1_returnsGuideOnly() {
        ReturnSuggestionResult aiResult = new ReturnSuggestionResult(
            "", "", "", "medium", "请问具体什么问题？", false, "引导中");
        when(aiChat.chatEntity(anyString(), anyMap(), anyString(), eq(ReturnSuggestionResult.class)))
            .thenReturn(aiResult);

        ReturnSuggestionRequest req = new ReturnSuggestionRequest(
            "手机有问题", null, null, null, null, 1);

        ReturnSuggestionResult result = service.suggestReturn(req);

        assertThat(result.guideQuestion()).isEqualTo("请问具体什么问题？");
        assertThat(result.finished()).isFalse();
    }

    @Test
    void suggestReturn_stepNull_defaultsTo1() {
        ReturnSuggestionResult aiResult = new ReturnSuggestionResult(
            "", "", "", "medium", "请问什么坏了？", false, "");
        when(aiChat.chatEntity(anyString(), anyMap(), anyString(), eq(ReturnSuggestionResult.class)))
            .thenReturn(aiResult);

        ReturnSuggestionRequest req = new ReturnSuggestionRequest(
            "有问题", null, null, null, null, null);  // step=null

        ReturnSuggestionResult result = service.suggestReturn(req);

        assertThat(result.finished()).isFalse();
    }
}
```

### 改写：`AiAssistantServiceTest.java`
- 删除 `parseReturnSuggestion` 直接测试（已无该方法）
- 现有用例改用 mock `AiChatService` 替代 mock `AiClient`

### 回归测试
- ✅ `mvn test -pl mall-ai -Dtest=AiAssistantServiceTest`
- ✅ `mvn test -pl mall-ai -Dtest=ReturnSuggestionBeanOutputTest`（新增）
- ✅ `mvn test -pl mall-ai -Dtest=AiAssistantControllerTest`
- ✅ `mvn test -pl mall-ai -Dtest=CorsPreflightTest`

### 端到端 curl
```bash
mvn spring-boot:run -pl mall-ai &

# 验证正常 JSON 输出仍能正确解析
curl -s -X POST http://localhost:8086/ai/return/suggest \
  -H "Content-Type: application/json" \
  -d '{"issue":"屏幕有裂痕","productName":"iPhone","step":3}' | jq .data
# 预期: finished=true, suggestedReason, suggestedDescription 都非空
```

## 📊 验收标准
- [ ] `parseReturnSuggestion` 方法**已删除**
- [ ] `suggestReturn` 主体 ≤ 25 行
- [ ] 5 个核心测试用例全绿
- [ ] 端到端 JSON 解析行为 100% 等价（用相同输入比对 Stage 3 输出）
- [ ] `mvn test -pl mall-ai` 全绿
- [ ] `git diff --stat` 净减少 ≥ 60 行

## 🌿 Git 操作
```bash
git checkout -b refactor/mall-ai-stage-4-bean-output-converter
git add mall-ai-fix-task/stage-4-bean-output-converter.md
git commit -m "chore(mall-ai): Stage 4 任务文档"

git add src/test/java/com/macro/mall/ai/service/ReturnSuggestionBeanOutputTest.java
git commit -m "test(mall-ai): Stage 4 BeanOutputConverter 场景测试 (red)"

git add src/main/
git commit -m "refactor(mall-ai): Stage 4 BeanOutputConverter 替换手写 JSON 解析

- parseReturnSuggestion 删除（90+ 行 → 0）
- suggestReturn 主体精简到 25 行
- BeanOutputConverter 自动注入 JSON schema 到 prompt
- 强制校验逻辑改用 record 不可变 with 模式
- Fallback 逻辑改用 record 构造

Refs: mall-ai-fix-task/stage-4-bean-output-converter.md"

git add src/test/java/
git commit -m "test(mall-ai): Stage 4 全测试通过 (green)"

git add mall-ai/README.md mall-ai-fix-task/CHANGELOG.md
git commit -m "docs(mall-ai): Stage 4 README + CHANGELOG"

git push -u origin refactor/mall-ai-stage-4-bean-output-converter
gh pr create --title "[mall-ai] Stage 4: BeanOutputConverter 替换手写 JSON"
```

## ⚠️ 风险
- **风险 1**：`BeanOutputConverter` 对 record 的支持需要 Jackson 2.15+（Spring Boot 3.5 BOM 自带 2.18，已 OK）
- **风险 2**：AI 返回的 JSON 字段名若与 record 字段名不一致（驼峰 vs 下划线）→ 显式 `@JsonProperty` 注解
- **风险 3**：record 字段顺序在反序列化时**不影响** Jackson 行为，但调试时易混淆
