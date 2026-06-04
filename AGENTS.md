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
- **Spring Security 6 CORS 必须在 SecurityFilterChain 中显式启用**：仅注册 `CorsFilter` Bean 是不够的，必须在 `SecurityConfig.filterChain` 中调用 `.cors(c -> c.configurationSource(...))`，并提供 `CorsConfigurationSource` Bean（参考 `mall-security/.../config/CorsConfig.java`）。这是 Spring Security 5 → 6 升级时的最大变化之一，**Customizer.withDefaults() 在某些 6.x 版本下对 CORS 的处理不稳定，建议显式注入 CorsConfigurationSource**。
- **CORS 策略统一从 `application.yml` 的 `mall.security.cors.*` 读取**：
  - 仅 mall-admin / mall-portal 享受统一配置（依赖 mall-security 继承 CorsConfigurationSource + CorsProperties）
  - mall-search / mall-ai 因架构原因（不依赖 mall-security）保留本地 `CorsConfigurationSource` Bean，结构与 security 模块完全一致
  - 注意 `setAllowCredentials(true)` 不能配 `setAllowedOrigins(["*"])`，需用 `addAllowedOriginPattern("*")` 或具体域名列表
- **本地开发时 mall-admin 不会自动 reload `mall-security` 的 class 变更**：修改 mall-security 后必须 `mvn install -pl mall-security -am -DskipTests -Ddocker.skip=true`，否则 mall-admin 仍会从本地 .m2 仓库用旧 JAR。这是 MockMvc 集成测试看不到新配置的最常见原因。

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
