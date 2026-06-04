# AGENTS.md

## Build & Run

### Backend (Java / Spring Boot 2.7.5, JDK 1.8)

```bash
# Full build (tests are skipped by default in pom.xml)
mvn clean install -DskipTests

# Build single module (e.g. mall-admin)
mvn clean install -pl mall-admin -am -DskipTests

# Run a service
cd mall-admin && mvn spring-boot:run
cd mall-portal && mvn spring-boot:run
cd mall-search && mvn spring-boot:run
cd mall-ai && mvn spring-boot:run
```

Services: mall-admin `:8080`, mall-portal `:8085`, mall-search `:8081`, mall-ai `:8086`

### Frontend (mall-admin-web)

Requires Node.js `^20.19.0 || >=22.12.0`.

```bash
cd mall-admin-web
npm install
npm run dev          # Vite dev server → http://localhost:5173
npm run lint         # eslint . --fix --cache
npm run type-check   # vue-tsc --build
npm run build        # type-check + vite build
```

## Architecture

Multi-module Maven project. Base package: `com.macro.mall`.

| Module | Role | Depends on |
|---|---|---|
| mall-common | Shared utils, Redis service, exception handling, API response wrapper | — |
| mall-mbg | **Auto-generated** MyBatis models, mappers, XML | mall-common |
| mall-security | JWT filters, Spring Security config | mall-common |
| mall-admin | Back-office API service (:8080) | mall-common, mall-mbg, mall-security |
| mall-portal | Customer-facing API service (:8085) | mall-common, mall-mbg, mall-security |
| mall-search | Elasticsearch search service (:8081) | mall-common, mall-mbg |
| mall-ai | AI assistant service (:8086) | mall-common |
| mall-admin-web | Vue 3 admin panel (Vite) | — |
| mall-app-web | UniApp mobile (HBuilderX) | — |

## Gotchas

- **mall-mbg is generated code.** Do not manually edit files under `mall-mbg/src/main/java`. Regenerate via `generatorConfig.xml` instead.
- **Redis dev address** is `172.23.31.76` (WSL2 port-forward), not `localhost`. See `mall-admin/src/main/resources/application-dev.yml`.
- **Sensitive configs are gitignored**: `application-local.yml`, `application-secret.yml`, `*.env`, `.env.local`.
- **JWT secrets are hardcoded** in `application.yml` per service — each service has a different secret.
- **Docker builds** use a remote Docker host at `192.168.3.101:2375` (configured in root pom.xml `<docker.host>`).
- **MyBatis mapper locations** are split: `classpath:dao/*.xml` + `classpath*:com/**/mapper/*.xml`.
- **Maven repositories** use Aliyun mirror for faster downloads in China.
- **Swagger UI** at `http://localhost:8080/swagger-ui/`.
- **RabbitMQ vhost** is `/mall` (not default `/`). Management UI: `http://localhost:15672`, user `mall`/`mall`.
- **MinIO**: endpoint must be port `9000` (API), not `9001` (console).
- **ES Java client version** is managed by Spring Boot 3.5.14 BOM (`elasticsearch-client.version` = `8.18.8`). Do NOT manually pin `co.elastic.clients:elasticsearch-java` or `org.elasticsearch.client:elasticsearch-rest-client` in `mall-search/pom.xml` — let the BOM handle it. Manual pinning was an old 2.x-era workaround and is now wrong (locks to a stale version).
- **ES 8.x local dev requires HTTP (no SSL)**: The default `xpack.security.http.ssl.enabled: true` (set by ES 8.x auto-config) makes ES reject plain HTTP connections with `plaintext http traffic on an https channel`. For local dev, set `xpack.security.enabled`, `xpack.security.http.ssl.enabled`, `xpack.security.transport.ssl.enabled` all to `false` in `E:\ElasticSearch\elasticsearch-8.19.16\config\elasticsearch.yml`. Production must keep SSL on and configure HTTPS + auth in `application-*.yml`.
- **Chinese analyzer MUST be IK (infinilabs/analysis-ik), not smartcn**:
  - IK 8.19.16 release ships at `https://get.infini.cloud/elasticsearch/analysis-ik/8.19.16` (the project moved from `medcl/elasticsearch-analysis-ik` to `infinilabs/analysis-ik` and is actively maintained).
  - Install: `E:\ElasticSearch\elasticsearch-8.19.16\bin\elasticsearch-plugin install -b https://get.infini.cloud/elasticsearch/analysis-ik/8.19.16`
  - Use `analyzer = "ik_max_word"` (index) + `searchAnalyzer = "ik_smart"` (search) on `Text` fields.
  - **Do NOT use `smartcn`**: it is HMM-based and context-dependent — for "小米 8 全面屏手机" smartcn tokenizes as `[小, 米, 8, 全面, 屏, 手机]`, splitting brand names into single characters and breaking product search.
- **Tests are disabled globally** (`<skipTests>true</skipTests>` in root pom.xml). To run a specific test class: `mvn test -pl <module> -DskipTests=false -Dtest=ClassName`. Maven Surefire also fails on `docker:build` during `install` if remote Docker host is unreachable — pass `-Ddocker.skip=true` to skip the Docker plugin when not building images.
- **Spring Security 6 CORS 必须在 SecurityFilterChain 中显式启用**：仅注册 `CorsFilter` Bean 是不够的，必须在 `SecurityConfig.filterChain` 中调用 `.cors(c -> c.configurationSource(...))`，并提供 `CorsConfigurationSource` Bean。这是 Spring Security 5 → 6 升级时的最大变化之一，**Customizer.withDefaults() 在某些 6.x 版本下对 CORS 的处理不稳定，建议显式注入 CorsConfigurationSource**。
- **CORS 策略统一在 `mall-common-cors` 独立模块**（不在 mall-security/mall-common）：
  4 个服务（admin/portal/search/ai）都依赖 mall-common-cors，通过各服务 application.yml
  的 `mall.security.cors.*` 实现差异化配置：
  - admin/portal：通过 `SecurityConfig.filterChain().cors(c -> c.configurationSource(...))` 接入 Security 链
  - search/ai：通过 `FilterRegistrationBean<HIGHEST_PRECEDENCE>` 包装为 servlet filter
  （避免 Spring Boot 自动注册 CorsFilter Bean 时的 `LOWEST_PRECEDENCE` 顺序问题，
  注意 `@Bean` 方法名不能与 `@Configuration` 类名同名，否则会触发 BeanDefinitionOverrideException）
  - 注意 `setAllowCredentials(true)` 不能配 `setAllowedOrigins(["*"])`，需用 `addAllowedOriginPattern("*")` 或具体域名列表
  - 注入 `CorsConfigurationSource` 时需用 `@Qualifier("corsConfigurationSource")`，因为 `MvcHandlerMappingIntrospector` 也实现了该接口
- **OSS 图片跨域加载必 403（OSS 不放行非白名单 Origin）**：
  `curl` 直连 200 / 浏览器 `<img>` 必 403 的根因是 OSS CORS 策略不包含 dev 域名。
  修复方案：走 `mall-common-pic` 模块的 `PicProxyController`（`GET /pic/proxy?url=...`），
  后端 fetch 透传字节流；前端不需要改代码，Service 层用 `ImageUrlRewriter` 透明改写
  数据库里的 OSS URL 为代理 URL。
- **本地开发时 mall-admin 不会自动 reload `mall-security` / `mall-common-cors` / `mall-common-pic` 的 class 变更**：
  修改后必须先 `mvn install -pl mall-common-cors,mall-common-pic,mall-security -am -DskipTests -Ddocker.skip=true`，
  再 `mvn install -pl mall-admin,mall-portal -am -DskipTests -Ddocker.skip=true`。
  这是模块多层级联依赖导致的"暗坑"。

## External Dependencies

MySQL `5.7+`, Redis `5.0+`, RabbitMQ `3.8+`, Elasticsearch `7.x` (optional), MongoDB `4.0+` (optional), MinIO (optional).

SQL schema: `document/sql/mall.sql` (referenced in README, not verified present).

## Conventions

- Java code follows Alibaba Java coding guidelines.
- Lombok is used extensively — no manual getters/setters.
- Unified API response via `mall-common`'s `CommonResult` / `PageHelper`.
- Chinese comments are standard (bilingual where documented).
- Git commits: conventional commits style (`feat:`, `fix:`, `docs:`, `refactor:`, etc.).
- Frontend: Vue 3 + TypeScript + Element Plus + Pinia state management.
