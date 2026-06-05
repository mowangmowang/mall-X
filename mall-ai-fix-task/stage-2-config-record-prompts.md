# Stage 2: 配置 Record 化 + Prompt 外置到 application.yml

## 🎯 目标
- `AiClientConfig` 改为 record 风格的 `@ConfigurationProperties`（带 `@Validated` 自动校验）
- 100+ 行硬编码 Prompt 从 Java 代码迁到 `application.yml` 的 `ai.prompts.*`
- `AiAssistantServiceImpl` 通过 `PromptProperties` 注入
- `@PostConstruct` 手动校验改为 Bean Validation 自动校验

## 📂 涉及文件

### 新增
- `mall-ai/src/main/java/com/macro/mall/ai/config/AiClientProperties.java`
- `mall-ai/src/main/java/com/macro/mall/ai/config/PromptProperties.java`
- `mall-ai/src/test/java/com/macro/mall/ai/config/PromptPropertiesTest.java`
- `mall-ai/src/test/java/com/macro/mall/ai/config/AiClientPropertiesTest.java`

### 修改
- `mall-ai/src/main/java/com/macro/mall/ai/config/AiClientConfig.java`（大幅简化）
- `mall-ai/src/main/java/com/macro/mall/ai/service/impl/AiAssistantServiceImpl.java`
- `mall-ai/src/main/resources/application.yml`（新增 `ai.prompts.*`）
- `mall-ai/src/main/resources/application-dev.yml`（dev profile prompt 可选覆盖）

### 删除
- 无（保持向后兼容）

## 🔨 实施步骤

### Step 2.1: 创建 `AiClientProperties`
```java
@ConfigurationProperties(prefix = "ai.client")
@Validated
public record AiClientProperties(
    @NotBlank(message = "AI base URL 不能为空")
    String baseUrl,

    @NotBlank(message = "AI API Key 未配置，请设置环境变量 AI_API_KEY 或在配置文件中设置 ai.client.api-key")
    String apiKey,

    @NotBlank String model,
    @DecimalMin("0.0") @DecimalMax("2.0") Double temperature,
    @Min(1) @Max(8192) Integer maxTokens
) {}
```

### Step 2.2: 创建 `PromptProperties`
```java
@ConfigurationProperties(prefix = "ai.prompts")
@Validated
public record PromptProperties(
    @NotBlank String productQaSystem,
    @NotBlank String returnSuggestionSystem,
    @NotBlank String productQaFallback,
    @NotBlank String returnReasonDefault,
    @NotBlank String categoryDefault
) {}
```

### Step 2.3: 简化 `AiClientConfig`
```java
@Configuration
@EnableConfigurationProperties({AiClientProperties.class, PromptProperties.class})
public class AiClientConfig {
    // 类体可空，所有逻辑由 Spring Boot 自动接管
}
```

### Step 2.4: `application.yml` 新增 prompts
```yaml
ai:
  client:
    api-key: ${AI_API_KEY:your-api-key-here}
    base-url: https://api.deepseek.com/v1
    model: deepseek-chat
    temperature: 0.7
    max-tokens: 1024
  prompts:
    product-qa-system: |
      你是专业电商购物助手，帮助顾客了解商品信息。请严格遵循以下规范：
      【回答原则】...（原 QA_SYSTEM_PROMPT 全部内容，约 25 行）
    return-suggestion-system: |
      你是专业电商售后客服助手...
      【退货原因选项】（必须从以下选项中选择）：
      {reasons}
      【3轮引导流程】...
    product-qa-fallback: "该信息暂未提供，建议咨询客服"
    return-reason-default: "质量问题"
    category-default: "硬件故障"
```

### Step 2.5: `AiAssistantServiceImpl` 注入 `PromptProperties`
```java
@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private final AiClient aiClient;
    private final ReturnReasonService returnReasonService;
    private final PromptProperties prompts;

    @Override
    public AiResponse chatAboutProduct(ProductQaRequest request) {
        String content = ...;
        String reply = aiClient.chat(prompts.productQaSystem(), content);
        return new AiResponse(reply);
    }

    @Override
    public ReturnSuggestionResult suggestReturn(ReturnSuggestionRequest req) {
        Map<String, Object> vars = Map.of("reasons",
            String.join("、", returnReasonService.getEnabledReturnReasons()));
        String systemPrompt = new PromptTemplate(prompts.returnSuggestionSystem())
            .render(vars);
        // ...
    }
}
```

## 🧪 测试细节

### 新增单元测试
- `src/test/java/com/macro/mall/ai/config/PromptPropertiesTest.java`
  - 缺字段启动失败（用 `ApplicationContextRunner`）
  - YAML 正确绑定到 record
  - 默认值

```java
class PromptPropertiesTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
        .withUserConfiguration(AiClientConfig.class);

    @Test
    void missingPrompts_shouldFailToStart() {
        runner.withPropertyValues(
                "ai.client.base-url=https://api.test.com",
                "ai.client.api-key=sk-test",
                "ai.client.model=test"
                // 故意缺 ai.prompts.*
            )
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure())
                    .hasMessageContaining("ai.prompts");
            });
    }

    @Test
    void allPresent_bindsCorrectly() {
        runner.withPropertyValues(
                "ai.client.base-url=https://api.test.com",
                "ai.client.api-key=sk-test",
                "ai.client.model=test",
                "ai.prompts.product-qa-system=QA prompt",
                "ai.prompts.return-suggestion-system=RETURN prompt {reasons}",
                "ai.prompts.product-qa-fallback=fallback",
                "ai.prompts.return-reason-default=质量问题",
                "ai.prompts.category-default=硬件故障"
            )
            .run(context -> {
                PromptProperties p = context.getBean(PromptProperties.class);
                assertThat(p.productQaSystem()).isEqualTo("QA prompt");
                assertThat(p.returnSuggestionSystem()).contains("{reasons}");
            });
    }
}
```

### 新增：`AiClientPropertiesTest.java`
- 缺 `api-key` 启动失败
- `temperature` 越界（3.0）启动失败
- `max-tokens` 越界（10000）启动失败

### 回归测试
- ✅ `AiAssistantServiceTest`（关键：prompt 内容比对）
- ✅ `AiAssistantControllerTest`
- ✅ `CorsPreflightTest`

### 端到端 curl
```bash
# 启动（应在缺 AI_API_KEY 时启动失败）
unset AI_API_KEY
mvn spring-boot:run -pl mall-ai 2>&1 | grep "Validation failed"
# 预期: BindValidationException 启动失败 ✓

# 正常启动后接口行为不变
export AI_API_KEY=sk-test
mvn spring-boot:run -pl mall-ai &
sleep 10

curl -s -X POST http://localhost:8086/ai/product/qa \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"question":"测试"}' | jq .data.reply
# 预期: 100 字以内的回答（与 Stage 1 输出内容风格一致）
```

## 📊 验收标准
- [ ] `AiClientConfig` 行数 ≤ 10
- [ ] `AiClientProperties` / `PromptProperties` 启动校验生效（缺字段即启动失败）
- [ ] `AiAssistantServiceImpl` 内 0 行硬编码 Prompt
- [ ] 端到端接口输出**风格一致**（prompt 内容等价）
- [ ] `mvn test -pl mall-ai` 全绿

## 🌿 Git 操作
```bash
git checkout -b refactor/mall-ai-stage-2-config-record-prompts
git add mall-ai-fix-task/stage-2-config-record-prompts.md
git commit -m "chore(mall-ai): Stage 2 任务文档"

git add src/test/java/com/macro/mall/ai/config/
git commit -m "test(mall-ai): Stage 2 Properties 校验测试 (red)"

git add src/main/
git commit -m "refactor(mall-ai): Stage 2 配置 Record 化 + Prompt 外置

- AiClientProperties/PromptProperties 用 record + @Validated
- @PostConstruct 校验改为 Spring Boot 自动 Bind 校验
- 100+ 行硬编码 Prompt 迁到 application.yml ai.prompts.*
- @RequiredArgsConstructor 注入 PromptProperties

Refs: mall-ai-fix-task/stage-2-config-record-prompts.md"

git add src/test/java/
git commit -m "test(mall-ai): Stage 2 测试全绿"

git add mall-ai/README.md mall-ai-fix-task/CHANGELOG.md
git commit -m "docs(mall-ai): Stage 2 README + CHANGELOG"

git push -u origin refactor/mall-ai-stage-2-config-record-prompts
gh pr create --title "[mall-ai] Stage 2: 配置 Record 化 + Prompt 外置"
```

## ⚠️ 风险
- **风险 1**：YAML 里的多行字符串（`|`）缩进敏感，IDE 自动格式化可能破坏
- **风险 2**：record + `@Validated` 需要 `spring-boot-starter-validation` 已在 classpath（已通过 mall-common 传递依赖引入）
- **风险 3**：Prompt 改成 yml 后，特殊字符（`{` `}`）若出现在内容里会被 PromptTemplate 当占位符 → 需对真实 prompt 做 escape 测试
