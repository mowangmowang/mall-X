# Mall-AI 现代化重构变更日志

> 每个 Stage 完成后追加一条。格式：`<日期> - <类型> - <Stage 编号>: <一句话变更摘要>`

---

## 2026-06-05 - refactor - Stage 1: DTO 全面 Record 化 + 构造器注入

**PR**: 待创建
**分支**: `refactor/mall-ai-stage-1-dto-record`
**Commit 数**: 3 (chore / test red / refactor green)
**行数变化**: +385 / -853 (净减 468 行, -57%)
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
