# Mall-AI 现代化重构变更日志

> 每个 Stage 完成后追加一条。格式：`<日期> - <类型> - <Stage 编号>: <一句话变更摘要>`

---

## 2026-06-05 - refactor - Stage 2: 配置 Record 化 + Prompt 外置

**PR**: 待创建
**分支**: `refactor/mall-ai-stage-2-config-record-prompts`
**Commit 数**: 3 (test red / refactor green / docs)
**行数变化**: +228 / -129 (净增 99 行 — yml 配置占大头)
**验证**:
- ✅ `mvn test -pl mall-ai -DskipTests=false` 全绿 28/28 (新增 4 个 PromptPropertiesTest)
- ✅ curl 端到端 2 个接口 HTTP 400 校验 + HTTP 200 真实 DeepSeek 调用
- ✅ App 启动 11.9s，无配置加载错误

**变更摘要**:
- 新增 `AiClientProperties` record（@ConfigurationProperties + @Validated）
- 新增 `PromptProperties` record
- `AiClientConfig`: 61 → 47 行，删除 12 个手写 getter/setter
- 删除 `@PostConstruct` 手动校验
- application.yml: 100+ 行硬编码 Prompt 迁到 `ai.prompts.*`
- `AiAssistantServiceImpl` 注入 `PromptProperties`
- 硬编码 fallback（"质量问题" / "硬件故障"）改读配置
- 删除 `RestTemplateConfig.java`（合并到 `AiClientConfig`）
- `buildReturnSystemPrompt` 用 `String.replace("{reasons}", ...)` 占位符渲染

**风险与回滚**:
- 风险：yml 多行字符串（`|`）缩进敏感，IDE 格式化可能破坏 Prompt
- 风险：`{reasons}` 占位符在 yml 模板中若与其他字段冲突需 escape
- 回滚：`git revert <merge-commit-of-stage-2>`

---

## 2026-06-05 - refactor - Stage 1: DTO 全面 Record 化 + 构造器注入

**PR**: 待创建
**分支**: `refactor/mall-ai-stage-1-dto-record`
**Commit 数**: 4 (chore / test red / refactor green / docs)
**行数变化**: +430 / -869 (净减 439 行, -57%)
**验证**:
- ✅ `mvn test -pl mall-ai -DskipTests=false` 全绿 24/24
- ✅ curl 端到端 2 个接口 400 校验生效
- ✅ 端到端 app 启动 10.5s，DB + AI 配置均正确加载

**变更摘要**:
- 5 个 DTO/POJO 改为 Java 17 record（AiResponse / ProductQaRequest / ReturnSuggestionRequest / ReturnSuggestionResult / ChatMessage）
- 3 个业务类改 `@RequiredArgsConstructor` 构造器注入（Controller / ServiceImpl / ReturnReasonService）
- JSON 解析改 record 构造（替代 setter 原地修改）
- 新增 2 个测试类（ProductQaRequestValidationTest + RecordImmutabilityTest）
- 改写 2 个测试类（AiAssistantServiceTest + AiAssistantControllerTest）

**风险与回滚**:
- 风险：项目 GlobalExceptionHandler 把校验异常映射为 `code=404`（HTTP 200），与 Spring 标准 400 不一致。Stage 1 不修改。
- 回滚：`git revert <merge-commit-of-stage-1>`

---

```markdown
## YYYY-MM-DD - <refactor|feat|fix> - Stage X: <阶段名>

**PR**: #<PR号>
**分支**: `<分支名>`
**Commit 数**: N
**行数变化**: +A / -B
**验证**:
- ✅ `mvn test -pl mall-ai` 全绿
- ✅ curl 端到端通过
- ✅ 覆盖率 X%

**变更摘要**:
- 改动了什么
- 删除了什么
- 新增了什么

**风险与回滚**:
- 风险点
- 回滚命令
```
