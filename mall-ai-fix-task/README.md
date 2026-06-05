# Mall-AI 现代化重构任务总览

> **背景**：mall-ai 当前实现业务成熟、Prompt 工程质量高，但技术栈仍停留在 Spring Boot 2.x 时代。本项目目标是用 Spring Boot 3.5 / Spring 6 / Java 17 原生能力重构，把 1500+ 行代码瘦身到 250 行。
>
> **节奏**：分 8 个 PR 独立提交，每个 Stage 一个分支 + 一个 task.md。

## 阶段依赖图

```
Stage 1 (Record 化) ──┐
                       ├─> Stage 3 (Spring AI) ──> Stage 4 (BeanOutputConverter) ──┐
Stage 2 (配置 yml)  ───┘                       ─> Stage 5 (Advisor)                  │
                                                                                      ├─> Stage 7 (SSE, 可选)
                                                                          Stage 6 ──> ┤
                                                                       (去 MyBatis)   └─> Stage 8 (Function Calling, 可选)
```

| Stage | 分支 | 类型 | 行数变化 | 状态 |
|---|---|---|---|---|
| 1 | `refactor/mall-ai-stage-1-dto-record` | refactor | ~1500 → ~1100 (-468) | ✅ 完成 |
| 2 | `refactor/mall-ai-stage-2-config-record-prompts` | refactor | ~150 → ~30 | ⏳ |
| 3 | `feat/mall-ai-stage-3-spring-ai` | **feat** | ~270 → ~30 | ⏳ |
| 4 | `refactor/mall-ai-stage-4-bean-output-converter` | refactor | ~90 → ~5 | ⏳ |
| 5 | `refactor/mall-ai-stage-5-advisor-sanitizer` | refactor | ~146 → ~30 | ⏳ |
| 6 | `refactor/mall-ai-stage-6-remove-mybatis` | refactor | 减 4 个依赖 | ⏳ |
| 7 | `feat/mall-ai-stage-7-streaming-sse` | **feat** | +SSE 端点 | ⏳ |
| 8 | `feat/mall-ai-stage-8-function-calling` | **feat** | 协议升级 | ⏳ |

## 全局测试规范

### 层级
| 层级 | 位置 | 工具 | 命名 | 耗时 |
|---|---|---|---|---|
| Unit | `src/test/java/.../*Test.java` | JUnit 5 + Mockito + AssertJ | `*Test` | < 100ms |
| Service | `src/test/java/.../service/*Test.java` | JUnit 5 + Mockito | `*ServiceTest` | < 500ms |
| Integration | `src/test/java/.../*IT.java` | `@SpringBootTest` + WireMock | `*IT` | < 5s |

### 覆盖率门槛
- **新代码行覆盖 ≥ 85%**（JaCoCo 强制）
- **核心路径分支 100%**（success / 4xx / 5xx / fallback）
- **Bug 修复必须有失败回归测试**

### 命令
```bash
# 单 stage
mvn test -pl mall-ai -am -DskipTests=false -Dtest='*Test'

# 集成测试
mvn verify -pl mall-ai -am -DskipTests=false -Dtest='*IT'
```

## 全局 Git 规范

### 分支命名
`<type>/mall-ai-stage-X-<kebab-name>`，type ∈ `{refactor, feat, fix, chore, docs}`

### 单 Stage 内 Commit 拆分（5 个原子 commit）
1. `chore(mall-ai): Stage X 任务文档`
2. `test(mall-ai): Stage X 测试先行 (red)`
3. `<type>(mall-ai): Stage X 核心改动` ← 主 commit
4. `test(mall-ai): Stage X 测试全绿 (green)`
5. `docs(mall-ai): Stage X README + CHANGELOG`

### Commit Message 模板
```
<type>(<scope>): <subject>      ← 中文 ≤ 50 字符

<body>                          ← 中文，解释 why

Refs: mall-ai-fix-task/stage-X-*.md
```

### PR 标题格式
`[mall-ai] Stage X: <阶段名>`

## 任务清单
- [x] Stage 1: DTO 全面 Record 化 → [stage-1-dto-record.md](stage-1-dto-record.md) ✅
- [ ] Stage 2: 配置 Record 化 + Prompt 外置 → [stage-2-config-record-prompts.md](stage-2-config-record-prompts.md)
- [ ] Stage 3: 引入 Spring AI → [stage-3-spring-ai.md](stage-3-spring-ai.md)
- [ ] Stage 4: BeanOutputConverter 替换手写 JSON → [stage-4-bean-output-converter.md](stage-4-bean-output-converter.md)
- [ ] Stage 5: InputSanitizer 收编为 Advisor → [stage-5-advisor-sanitizer.md](stage-5-advisor-sanitizer.md)
- [ ] Stage 6: 移除 MyBatis 依赖 → [stage-6-remove-mybatis.md](stage-6-remove-mybatis.md)
- [ ] Stage 7: 流式输出 SSE（可选）→ [stage-7-streaming-sse.md](stage-7-streaming-sse.md)
- [ ] Stage 8: 对话记忆 + Function Calling（可选）→ [stage-8-function-calling.md](stage-8-function-calling.md)
