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
- **Chinese analyzer is `smartcn`, not IK**: Use `analyzer = "smartcn"` in `@Field` annotations. IK (`medcl/elasticsearch-analysis-ik`) has not released an ES 8.19 compatible version. The `analysis-smartcn` plugin ships with the local ES 8.19.16 install and is maintained by Elastic.
- **Tests are disabled globally** (`<skipTests>true</skipTests>` in root pom.xml). To run a specific test class: `mvn test -pl <module> -DskipTests=false -Dtest=ClassName`. Maven Surefire also fails on `docker:build` during `install` if remote Docker host is unreachable — pass `-Ddocker.skip=true` to skip the Docker plugin when not building images.

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
