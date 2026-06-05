# Stage 1: DTO 全面 Record 化 + 构造器注入 + Lombok

## 🎯 目标
- 5 个 DTO/POJO（`AiResponse` / `ProductQaRequest` / `ReturnSuggestionRequest` / `ReturnSuggestionResult` / `ChatMessage`）从 POJO 迁移为 Java 17 record
- 业务类（`Controller` / `Service` / `Config`）从 `@Autowired` 字段注入改为构造器注入
- 行数：~700 → ~55

## 📂 涉及文件

### 修改
- `mall-ai/src/main/java/com/macro/mall/ai/domain/AiResponse.java`
- `mall-ai/src/main/java/com/macro/mall/ai/domain/ProductQaRequest.java`
- `mall-ai/src/main/java/com/macro/mall/ai/domain/ReturnSuggestionRequest.java`
- `mall-ai/src/main/java/com/macro/mall/ai/domain/ReturnSuggestionResult.java`
- `mall-ai/src/main/java/com/macro/mall/ai/client/ChatMessage.java`
- `mall-ai/src/main/java/com/macro/mall/ai/config/AiClientConfig.java`
- `mall-ai/src/main/java/com/macro/mall/ai/config/RestTemplateConfig.java`
- `mall-ai/src/main/java/com/macro/mall/ai/controller/AiAssistantController.java`
- `mall-ai/src/main/java/com/macro/mall/ai/service/impl/AiAssistantServiceImpl.java`

### 新增
- 无（纯改造）

## 🔨 实施步骤

### Step 1.1: DTO 改 record
```java
// 范例：ProductQaRequest.java
public record ProductQaRequest(
    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID", example = "1", requiredMode = RequiredMode.REQUIRED)
    Long productId,

    @NotBlank(message = "问题不能为空")
    @Size(max = 500, message = "问题长度不能超过500字符")
    @Schema(description = "用户问题", example = "这个商品的材质是什么？")
    String question,

    @Schema(description = "商品名称", example = "iPhone 15 Pro")
    String productName,

    @Schema(description = "商品品牌", example = "Apple")
    String productBrand,

    @Schema(description = "商品价格", example = "7999")
    String productPrice,

    @Schema(description = "商品副标题/描述", example = "钛金属设计，A17 Pro芯片")
    String productSubTitle,

    @Size(max = 2000, message = "对话历史长度不能超过2000字符")
    @Schema(description = "对话历史上下文，用于多轮对话")
    String conversationHistory
) {}
```

**⚠️ 关键点**：
- `@Schema` 注解在 record 组件上**与字段并列**，springdoc-openapi 2.6 原生支持
- 不再需要 `default constructor` / `getter` / `setter`
- Jackson 2.15+ 通过 `parameter-names` 模块反序列化 record（Spring Boot 3.5 BOM 自带 Jackson 2.18）
- 调用方代码需改：`request.getQuestion()` → `request.question()`

### Step 1.2: 业务类改构造器注入
```java
// AiAssistantController.java
@RestController
@RequestMapping("/ai")
@Tag(name = "AiAssistantController", description = "AI购物助手")
@RequiredArgsConstructor
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    @PostMapping("/product/qa")
    public CommonResult<AiResponse> productQa(@Valid @RequestBody ProductQaRequest request) {
        return CommonResult.success(aiAssistantService.chatAboutProduct(request));
    }
}
```

### Step 1.3: 同步更新 `AiAssistantServiceImpl`
```java
// 把：
@Autowired private AiClient aiClient;
@Autowired private ReturnReasonService returnReasonService;
// 改为：
private final AiClient aiClient;
private final ReturnReasonService returnReasonService;
// + 类级别 @RequiredArgsConstructor
```

### Step 1.4: 调用方代码适配
- `request.getQuestion()` → `request.question()`
- `request.getProductId()` → `request.productId()`
- `result.setSuggestedReason(...)` → record 不可变，**改用 `with` 方法**（在 Stage 4 BeanOutputConverter 时统一处理）

## 🧪 测试细节

### 新增测试
- `src/test/java/com/macro/mall/ai/domain/ProductQaRequestValidationTest.java`
  - `@NotNull` 触发（`productId = null` → 400）
  - `@NotBlank` 触发（`question = ""` / `null` → 400）
  - `@Size(max=500)` 边界（500 字符 OK，501 字符 400）
  - `@Size(max=2000)` 边界在 `conversationHistory`

```java
@WebMvcTest(AiAssistantController.class)
class ProductQaRequestValidationTest {
    @Autowired MockMvc mvc;
    @MockBean AiAssistantService service;

    @Test
    void blankQuestion_returns400() throws Exception {
        mvc.perform(post("/ai/product/qa")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":1,\"question\":\"\"}"))
           .andExpect(status().isBadRequest());
    }

    @Test
    void nullProductId_returns400() throws Exception {
        mvc.perform(post("/ai/product/qa")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":null,\"question\":\"hi\"}"))
           .andExpect(status().isBadRequest());
    }

    @Test
    void oversizedQuestion_returns400() throws Exception {
        String big = "a".repeat(501);
        mvc.perform(post("/ai/product/qa")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":1,\"question\":\"" + big + "\"}"))
           .andExpect(status().isBadRequest());
    }
}
```

### 改写测试
- `src/test/java/com/macro/mall/ai/service/AiAssistantServiceTest.java`
  - `getXxx()` → record 字段访问
  - 断言 record 不可变性（修改后生成新实例）
- `src/test/java/com/macro/mall/ai/controller/AiAssistantControllerTest.java`
  - setter → record 构造（`new ProductQaRequest(1L, "...", ...)`）
  - 校验 400 响应

### 回归测试（必须全绿）
- ✅ `mvn test -pl mall-ai -Dtest=AiAssistantServiceTest`
- ✅ `mvn test -pl mall-ai -Dtest=AiAssistantControllerTest`
- ✅ `mvn test -pl mall-ai -Dtest=CorsPreflightTest`
- ✅ `mvn test -pl mall-ai -Dtest=ProductQaRequestValidationTest`（新增）

### 端到端 curl
```bash
mvn spring-boot:run -pl mall-ai &

# 正常请求
curl -s -X POST http://localhost:8086/ai/product/qa \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"question":"测试","productName":"X"}' | jq .

# 验证失败（缺 productId）应返 400
curl -s -X POST http://localhost:8086/ai/product/qa \
  -H "Content-Type: application/json" \
  -d '{"question":"测试"}' | jq .code
# 预期: 400
```

## 📊 验收标准
- [ ] `mvn test -pl mall-ai` 全绿（**0 failure**, 0 error）
- [ ] `git diff --stat` 净减少 ≥ 600 行
- [ ] JaCoCo 覆盖率：DTO 100%，Service 维持原水平
- [ ] curl 端到端 2 个接口行为 100% 等价于重构前
- [ ] 0 个 `@Autowired` 字段注入
- [ ] 0 个 `setXxx()` / `getXxx()` 在业务代码中（DTO 字段访问用 record accessor）

## 🌿 Git 操作

```bash
# 1. 切分支
git checkout -b refactor/mall-ai-stage-1-dto-record

# 2. Commit 1: 任务文档
git add mall-ai-fix-task/stage-1-dto-record.md
git commit -m "chore(mall-ai): Stage 1 任务文档 (DTO Record 化)"

# 3. Commit 2: 测试先行
git add src/test/java
git commit -m "test(mall-ai): Stage 1 DTO Record 验证 + 不可变性测试 (red)"

# 4. Commit 3: 业务代码改造
git add src/main/java
git commit -m "refactor(mall-ai): Stage 1 DTO 全面 Record 化 + 构造器注入

- 5 个 DTO/POJO 改为 Java 17 record
- @Schema 注解调整至 record 组件级别
- 业务类构造器注入替换 @Autowired 字段注入
- 行数：~700 → ~55 (-92%)

Refs: mall-ai-fix-task/stage-1-dto-record.md"

# 5. Commit 4: 测试转绿
git add src/test/java
git commit -m "test(mall-ai): Stage 1 测试全绿 (green)"

# 6. 同步文档
git add mall-ai/README.md mall-ai-fix-task/CHANGELOG.md
git commit -m "docs(mall-ai): Stage 1 README + CHANGELOG 同步"

# 7. 推送 + PR
git push -u origin refactor/mall-ai-stage-1-dto-record
gh pr create --title "[mall-ai] Stage 1: DTO 全面 Record 化" \
             --body-file mall-ai-fix-task/stage-1-dto-record.md
```

## 🔙 回滚方案
```bash
git revert <merge-commit-of-stage-1>
# 或
git checkout main && git branch -D refactor/mall-ai-stage-1-dto-record
```

## ⚠️ 风险
- **风险 1**：`@Schema` 注解位置在 record 上的 springdoc 行为 → 通过 `/swagger-ui/` 实际访问验证
- **风险 2**：Jackson 反序列化 record 需要 `parameter-names` 编译选项 → 验证 Maven 编译参数包含 `-parameters`
- **风险 3**：下游 Java 客户端若用 `req.getQuestion()` 会编译失败 → 确认 mall-portal / mall-admin 不依赖 mall-ai 的 DTO
