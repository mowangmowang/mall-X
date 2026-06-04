# Mall-X

<div align="center">

**🌐 Languages: [🇺🇸 English](README.en.md) | [🇨🇳 简体中文](README.md)**

</div>

> **⚠️ This repository is a fork of [macrozheng/mall](https://github.com/macrozheng/mall)**, licensed under Apache 2.0.
> See [NOTICE](./NOTICE) and [CHANGELOG.md](./CHANGELOG.md) for attribution and the complete list of modifications.
> For full Chinese documentation, please switch to [简体中文](README.md).

---

## 🛒 What is Mall-X?

**Mall-X** is an open-source e-commerce platform built with **Spring Boot 3.5 + Vue 3**, derived from the popular [macrozheng/mall](https://github.com/macrozheng/mall). It is a production-grade reference implementation featuring microservices, a modern frontend, and an AI shopping assistant.

### ✨ Highlights vs. upstream

| Area | What changed | Why |
|---|---|---|
| **Framework** | Spring Boot **2.7.5 → 3.5.14**, Java **8 → 17** | Jakarta EE 9 namespace, modern Spring Security 6, performance improvements |
| **CORS** | Extracted to dedicated `mall-common-cors` module | Single source of truth, shared by 4 services (admin/portal/search/ai) |
| **Image loading** | New `mall-common-pic` image proxy | Solves OSS CORS 403 on `<img>` cross-origin loads |
| **Image URL rewriting** | New `ImageUrlRewriter` utility | Transparent OSS URL → proxy URL at Service layer (zero frontend changes) |
| **JWT** | JJWT 0.12 with HS512, 64-char minimum secret | RFC 7518 §3.2 compliance |
| **Elasticsearch** | 8.x with HTTPS + IK analyzer (`infinilabs/analysis-ik`) | Modern stack; IK is required for Chinese brand-name tokenization |
| **AI assistant** | New `mall-ai` service with DeepSeek integration | Smart shopping Q&A |

### 🏗️ Tech Stack

| Layer | Technologies |
|---|---|
| **Backend** | Spring Boot 3.5.14, Spring Security 6, Spring Data JPA / Elasticsearch, MyBatis |
| **Microservices** | mall-admin (8080), mall-portal (8085), mall-search (8081), mall-ai (8086) |
| **Data** | MySQL 5.7+, Redis 5.0+, Elasticsearch 8.x, MongoDB 4.0+, MinIO |
| **Messaging** | RabbitMQ 3.8+ |
| **Frontend** | Vue 3 + TypeScript + Element Plus + Pinia (mall-admin-web), UniApp (mall-app-web) |
| **AI** | DeepSeek API |
| **Build** | Maven (multi-module), Node.js 20+ / pnpm |

### 🧩 Module Layout

```
mall-X/
├── mall-common/          # Shared utilities, Redis service, exception handling
├── mall-common-cors/     # CORS unified configuration (NEW in Mall-X)
├── mall-common-pic/      # Image proxy for OSS CORS workaround (NEW in Mall-X)
├── mall-mbg/             # MyBatis Generator output (auto-generated models/mappers)
├── mall-security/        # JWT filters, Spring Security configuration
├── mall-admin/           # Back-office API (port 8080)
├── mall-portal/          # Customer-facing API (port 8085, also hosts /pic/proxy)
├── mall-search/          # Elasticsearch search service (port 8081)
├── mall-ai/              # AI shopping assistant (port 8086)
├── mall-admin-web/       # Vue 3 admin panel (Vite, port 5173 in dev)
└── mall-app-web/         # UniApp mobile app (HBuilderX)
```

### 🚀 Quick Start

> **Prerequisites**: JDK 17, Maven 3.8+, Node.js 20+, MySQL 5.7+, Redis 5.0+, RabbitMQ 3.8+, Elasticsearch 8.x (optional), MongoDB 4.0+ (optional), MinIO (optional).

```bash
# 1. Clone
git clone https://github.com/mowangmowang/mall-X.git
cd mall-X

# 2. Initialize database (import SQL schema)
mysql -u root -p < document/sql/mall.sql

# 3. Start backend services (in 4 separate terminals)
cd mall-admin  && mvn spring-boot:run    # :8080
cd mall-portal && mvn spring-boot:run    # :8085
cd mall-search && mvn spring-boot:run    # :8081
cd mall-ai     && mvn spring-boot:run    # :8086

# 4. Start admin frontend
cd mall-admin-web
npm install
npm run dev                                # http://localhost:5173

# 5. Open Swagger UI to explore APIs
open http://localhost:8080/swagger-ui/
```

Default admin account: `admin` / `macro123` (change immediately in production).

### 🔧 Key Configuration

All services share `mall.security.cors.*` via `mall-common-cors`:

```yaml
mall:
  security:
    cors:
      allowed-origins: "*"            # dev only; specify domains in prod
      allowed-methods: "*"
      allowed-headers: "*"
      allow-credentials: true
      max-age: 3600
```

Image proxy base URL (where `/pic/proxy` is hosted):

```yaml
mall:
  pic:
    proxy-base-url: http://localhost:8085
```

> **Why `localhost:8085`?** Only `mall-portal` exposes `/pic/proxy`. The admin/portal Services transparently rewrite OSS URLs in `pic` fields to point at it via `ImageUrlRewriter`.

### 🧪 Tests

```bash
# All skipTests by default; opt-in per class
mvn test -pl mall-admin -Dtest=CorsPreflightTest -DskipTests=false -Ddocker.skip=true
mvn test -pl mall-portal -Dtest=CorsPreflightTest -DskipTests=false -Ddocker.skip=true
mvn test -pl mall-search -Dtest=CorsPreflightTest -DskipTests=false -Ddocker.skip=true
mvn test -pl mall-ai -Dtest=CorsPreflightTest -DskipTests=false -Ddocker.skip=true
mvn test -pl mall-common-pic -Dtest=PicProxyControllerTest -DskipTests=false -Ddocker.skip=true
mvn test -pl mall-common -Dtest=ImageUrlRewriterTest -DskipTests=false -Ddocker.skip=true
```

### 🤝 Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss.

```bash
# 1. Fork this repo
# 2. Create your feature branch
git checkout -b feat/your-feature
# 3. Commit (use conventional commits)
git commit -m "feat: your feature"
# 4. Push and open a PR
git push origin feat/your-feature
```

### 📄 License

Copyright 2026 mowangmowang. Licensed under the **Apache License, Version 2.0**.

This product includes software developed at [macrozheng/mall](https://github.com/macrozheng/mall) (Copyright 2018-2024 macrozheng). See [NOTICE](./NOTICE) for details.

### 🙏 Acknowledgments

- **[macrozheng/mall](https://github.com/macrozheng/mall)** — the original project and architecture
- **[macrozheng](https://github.com/macrozheng)** — original author and community contributors
- Spring Boot, Spring Security, MyBatis, Elasticsearch, RabbitMQ, Redis, MinIO communities
- [infinilabs/analysis-ik](https://github.com/infinilabs/analysis-ik) — Chinese analyzer for Elasticsearch

---

<div align="center">

**🌐 Languages: [🇺🇸 English](README.en.md) | [🇨🇳 简体中文](README.md)**

Maintained by [@mowangmowang](https://github.com/mowangmowang)

</div>
