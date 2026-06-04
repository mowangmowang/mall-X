# Changelog — Mall-X

> **本项目 Fork 自 [macrozheng/mall](https://github.com/macrozheng/mall)**，基于 Apache 2.0 协议。
> 本文档记录本项目与上游的**所有差异点**及对应的 git commit。

---

## v1.0.0 — 2026-06 · Spring Boot 3.5 重制版首次发布

### 重大变更

| # | 变更 | 类型 | 涉及模块 | 参考 commit |
|---|---|---|---|---|
| 1 | 升级 Spring Boot 2.7.5 → 3.5.14 | refactor | 全部 9 模块 | `feature/upgrade-stage0~7` (7 个分支保留完整升级路径) |
| 2 | 升级 Java 8 → 17 | chore | 全部 9 模块 | 同上 |
| 3 | Jakarta EE 9 命名空间迁移 (`javax.*` → `jakarta.*`) | refactor | 全部 9 模块 | 同上 |
| 4 | 提取 `mall-common-cors` 独立模块（4 服务共享 CORS 策略） | feat | 新模块 | `feat(common-cors): 新建独立模块，统一提供 CorsConfigurationSource Bean` |
| 5 | 新增 `mall-common-pic` 图片代理模块 | feat | 新模块 | `feat(common-pic): 新建 mall-common-pic 模块 + PicProxyController 后端图片代理` |
| 6 | 新增 `ImageUrlRewriter` 工具类 | feat | mall-common | `feat(common): 新增 ImageUrlRewriter 工具类` |
| 7 | JJWT 0.12 HS512 合规（secret 强制 ≥ 64 字符） | fix | mall-security | `fix(security): 延长 mall-admin/portal 的 JWT secret 至 64 字符以满足 JJWT 0.12 HS512` |
| 8 | Spring Security 6 CORS 显式链 | refactor | mall-security | `refactor(security): 移除 CORS 改依赖 mall-common-cors` |
| 9 | Elasticsearch 8.x HTTPS + IK 分析器 | fix | mall-search | `fix/search-es-https-and-analyzer` 分支 |
| 10 | 新增 `mall-ai` AI 助手服务（DeepSeek 集成） | feat | mall-ai | `feature/ai-assistant` 分支 |
| 11 | portal `secure.ignored.urls` 加入 `/pic/**` | fix | mall-portal | `fix(portal): 把 /pic/** 加入 Security 白名单（图片代理放行）` |

### 详细 commit 列表（从旧到新）

```
# 升级系列（保留为独立分支，可用于复盘 Spring Boot 3 迁移路径）
feature/upgrade-stage0-root-pom          # 根 pom 升级
feature/upgrade-stage1-mall-common      # common 升级
feature/upgrade-stage2-mall-mbg         # mbg 升级
feature/upgrade-stage3-mall-security    # security 升级
feature/upgrade-stage4-mall-admin       # admin 升级
feature/upgrade-stage5-mall-portal      # portal 升级
feature/upgrade-stage6-mall-search      # search 升级
feature/upgrade-stage7-mall-ai          # ai 升级

# 安全与 CORS 重构
2359d06 refactor(search,ai): 将 MallCorsConfig 从 CorsFilter 模式迁移到 CorsConfigurationSource
13aebed refactor(security): 清理异常处理器中手工设置的 CORS 响应头
2397eda docs(security): 在 AGENTS.md 补充 CORS 架构说明
c20af0b fix(security): 延长 mall-admin/portal 的 JWT secret 至 64 字符以满足 JJWT 0.12 HS512
d4ed16b refactor(security): JwtTokenUtil 全面清理（重命名/Javadoc/安全日志修复）

# CORS 统一 + 图片代理
9fb5266 feat(common-cors): 新建独立模块，统一提供 CorsConfigurationSource Bean
889a0f5 refactor(security): 移除 CORS 改依赖 mall-common-cors
8c35d78 chore(admin,portal): pom 显式声明 mall-common-cors 依赖
644e5c8 refactor(search,ai): 引入 mall-common-cors + FilterRegistrationBean
ce61a8a feat(common-pic): 新建 mall-common-pic 模块 + PicProxyController 后端图片代理
8b52a23 feat(common): 新增 ImageUrlRewriter 工具类
a6d2249 refactor(portal): HomeServiceImpl 在返回前用 ImageUrlRewriter 改写 pic 字段
75498d4 refactor(admin): Admin 端 Service 层调用 ImageUrlRewriter 改写 pic 字段
c56b693 test(security): CorsPreflightTest 跨服务回归（admin/portal/search/ai）
2e904d7 test(common-pic): PicProxyController 集成测试
96dafee test(common): ImageUrlRewriter 单元测试
2d44c00 docs: AGENTS.md 补充 mall-common-cors / mall-common-pic 架构说明

# ES 8.x HTTPS 修复 + 图片代理白名单修复
abffb64 fix(portal): 把 /pic/** 加入 Security 白名单（图片代理放行）
58e2007 docs(common-pic): 新增模块 README - 原理 / 架构 / 使用 / 暗坑解析
```

### 文档

- `AGENTS.md` — AI 代理 / 开发者必读的开发规范、Gotchas
- `mall-common-pic/README.md` — 图片代理模块的完整原理与使用说明
- `mall-security/README.md` — 继承自上游
- `mall-portal/README.md` / `mall-admin/README.md` / `mall-search/README.md` / `mall-ai/README.md` — 继承自上游

### 致谢

本项目基于 [macrozheng/mall](https://github.com/macrozheng/mall) 的优秀架构与代码，
由 **mowangmowang** 于 2026 年完成 Spring Boot 3.5 升级与多个增强特性。
感谢原作者 **macrozheng** 与社区贡献者。
