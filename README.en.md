# mall-X

<p>
  <a href="LICENSE"><img src="https://img.shields.io/github/license/mowangmowang/mall-X?style=flat-square" alt="License"></a>
  <a href="https://github.com/macrozheng/mall"><img src="https://img.shields.io/badge/fork%20of-macrozheng%2Fmall-blue?style=flat-square" alt="Fork of macrozheng/mall"></a>
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Spring%20Boot-3.5.14-6DB33F?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot"></a>
  <a href="https://www.oracle.com/java/"><img src="https://img.shields.io/badge/JDK-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white" alt="JDK 17"></a>
  <a href="https://vuejs.org/"><img src="https://img.shields.io/badge/Vue-3.3-4FC08D?style=flat-square&logo=vue.js&logoColor=white" alt="Vue 3"></a>
  <a href="https://maven.apache.org/"><img src="https://img.shields.io/badge/Maven-3.8+-C71A36?style=flat-square&logo=apachemaven&logoColor=white" alt="Maven"></a>
  <a href="https://github.com/macrozheng/mall/stargazers"><img src="https://img.shields.io/github/stars/macrozheng/mall?style=flat-square" alt="Upstream stars"></a>
</p>

<div align="center">

**🌐 Languages: [🇺🇸 English](README.en.md) | [🇨🇳 简体中文](README.md)**

</div>

> ⚠️ **This repository is a fork of [macrozheng/mall](https://github.com/macrozheng/mall)**, licensed under Apache 2.0.
> Upstream Copyright 2018-2024 macrozheng. Mall-X adds the Spring Boot 2.7 → 3.5 and Java 8 → 17 upgrade,
> new modules `mall-common-cors` / `mall-common-pic` / `mall-ai`, and a refactored CORS / image-proxy layer.
> Full diff and commit list: [CHANGELOG.md](./CHANGELOG.md) and [NOTICE](./NOTICE).

---

## Friendly Tips

> 1. **Live demo**: [macrozheng upstream demo](https://www.macrozheng.com/admin/index.html) (default `admin` / `macro123`).
> 2. **Full tutorial**: [《mall Learning Tutorial》](https://www.macrozheng.com) — Mall-X keeps the same business & architecture.
> 3. **Spring Cloud version**: [mall-swarm](https://github.com/macrozheng/mall-swarm) (upstream sister project).
> 4. **Mall-X-only enhancements**: Spring Boot 3.5 upgrade path · AI assistant · unified CORS · image proxy — see [CHANGELOG.md](./CHANGELOG.md).
> 5. **Dev guide / Gotchas**: [AGENTS.md](./AGENTS.md) (required reading).

## Introduction

`mall-X` is a full-featured e-commerce system built with **Spring Boot 3.5 + Vue 3**, frontend-backend separated, integrating Elasticsearch, RabbitMQ, Redis, MongoDB and MinIO.

## 🎯 Highlights

**🛠 Fixed & Upgraded** (vs upstream [macrozheng/mall](https://github.com/macrozheng/mall)):

- ⬆️ **Backend upgrade**: Spring Boot 2.7.5 → 3.5.14 · Java 8 → 17 · Jakarta EE 9 namespace
- ⬆️ **Frontend upgrade**: Vue 2 → Vue 3 + Vite + TypeScript + Element Plus + Pinia
- 🔧 **CORS refactor**: extracted `mall-common-cors` module, shared by 4 services
- 🔧 **Image proxy**: new `mall-common-pic` + `ImageUrlRewriter`, solves `<img>` CORS 403
- 🔐 **JWT compliance**: JJWT 0.12 · HS512 · enforced 64-char minimum secret
- 🛡️ **Spring Security 6**: explicit CORS Filter Chain, fixes SS 5→6 upgrade pitfall

**🆕 Brand-new modules**:

- 🤖 `mall-ai` — AI shopping assistant (product Q&A + return guidance, DeepSeek-powered)
- 🖼️ `mall-common-pic` — OSS image proxy
- ⚙️ `mall-common-cors` — unified CORS shared by 4 services

**✂️ Trimmed** (upstream unimplemented modules removed; **all retained features have complete frontend + backend API**):

- ❌ Back-office: operations · statistics · finance
- ❌ Customer-facing: customer service · help center
- ✅ Kept: product (PMS) · order (OMS) · marketing (SMS) · RBAC (UMS) · content (CMS) · file (OSS)

### Demo

#### Back-office system

Frontend project [mall-admin-web](https://github.com/macrozheng/mall-admin-web) (Mall-X upgrades to Vue 3 + Vite).

![Back-office demo](./document/resource/mall_admin_show.png)

#### Customer-facing portal

Frontend project [mall-app-web](https://github.com/macrozheng/mall-app-web) (UniApp; switch browser to mobile mode for the best look).

![Portal demo](./document/resource/re_mall_app_show.jpg)

## Module Layout

```
mall-X
├── mall-common          # Shared utilities, unified response, Redis service
├── mall-common-cors     # Unified CORS (shared by 4 services, ★ new in Mall-X)
├── mall-common-pic      # OSS image proxy (★ new in Mall-X)
├── mall-mbg             # MyBatis Generator output (auto-generated models/mappers)
├── mall-security        # Spring Security + JWT
├── mall-admin           # Back-office API (port 8080)
├── mall-portal          # Customer-facing API (port 8085, also hosts /pic/proxy)
├── mall-search          # Elasticsearch product search (port 8081)
└── mall-ai              # AI shopping assistant (port 8086, ★ new in Mall-X)
```

## Tech Stack

### Backend

| Technology | Version | Description |
| --- | --- | --- |
| Spring Boot | 3.5.14 | Web application framework |
| Spring Security | 6.x | AuthN / AuthZ framework |
| MyBatis | 3.5.10 | ORM framework |
| MyBatis Generator | 1.4.x | Data layer code generator |
| Elasticsearch Java Client | 8.18.8 | Distributed search engine |
| RabbitMQ | 3.8+ | Message queue (vhost `/mall`) |
| Redis | 5.0+ | Distributed cache |
| MongoDB | 4.0+ | NoSQL (user behavior) |
| MinIO / Aliyun OSS | 8.x / Latest | Object storage |
| JWT (jjwt) | 0.12 | Login token (HS512) |
| Druid | 1.2.14 | Database connection pool |
| Lombok | 1.18+ | Java enhancement library |
| Hutool | 5.8.9 | Java utility library |
| PageHelper | 1.4.5 | MyBatis physical paging |
| SpringDoc | 2.x | API documentation |
| DeepSeek API | deepseek-chat | AI model (used by mall-ai) |

### Frontend

| Technology | Version | Description |
| --- | --- | --- |
| Vue | 3.3.x | Progressive JavaScript framework |
| TypeScript | 5.x | Type system |
| Vite | 5.x | Next-generation build tool |
| Element Plus | 2.4.x | Vue 3 UI library |
| Pinia | 2.x | State management |
| Vue Router | 4.x | Routing |
| uni-app | Latest | Cross-platform mobile framework |

### Infrastructure

| Middleware | Version | Purpose |
| --- | --- | --- |
| MySQL | 5.7+ / 8.0 | Relational DB (core business) |
| Redis | 5.0+ | Cache / Session / Auth code |
| Elasticsearch | 8.x | Product search (IK analyzer) |
| RabbitMQ | 3.8+ | Async messaging / delay queue |
| MongoDB | 4.0+ | Member behavior log |
| MinIO | Latest | File storage |

### Dev Tools

| Tool | Description |
| --- | --- |
| IntelliJ IDEA | IDE |
| Maven 3.8+ | Multi-module build |
| Node.js 20.19+ / ≥22.12 | Required by mall-admin-web |
| Navicat | MySQL client |
| AnotherRedisDesktopManager | Redis client |
| Postman | API debugging |
| HBuilderX | uni-app IDE (for mall-app-web) |

## Architecture

![System Architecture](./document/resource/系统架构图.drawio.png)

> Detailed module dependency / order sequence diagrams: [document/architecture_diagram.md](./document/architecture_diagram.md).

## Modules

### 🏪 Back-office `mall-admin` (`:8080`)

| Subsystem | Features |
| --- | --- |
| **PMS** Product | Brand · SPU/SKU · Category · Attribute |
| **OMS** Order | Order · Order settings · Return apply · Return reason |
| **SMS** Marketing | Coupon · Flash sale · Home banner / brand / new / recommend |
| **UMS** RBAC | Admin · Role · Menu · Resource · Member level |
| **CMS** Content | Preference area · Subject |
| **OSS** File | MinIO / Aliyun OSS upload |

### 🛍️ Customer-facing `mall-portal` (`:8085`)

| Module | Features |
| --- | --- |
| Home portal | Banner · Recommend · Preference · Subject aggregation |
| Product | Category tree · Brand · Search · Detail · Review |
| Shopping cart | Add · Update · Delete · Quantity |
| Order | Place · Pay (Alipay) · Cancel · Refund |
| Member | Register/Login · Profile · Address |
| Member behavior | Follow brand · Favorite product · Browsing history |
| Coupon | Claim · List · Status |

### 🔍 Search `mall-search` (`:8081`)

- Full-text search (IK Chinese analyzer)
- Function Score weighted query (name 10 / keyword 5 / subtitle 3)
- Brand / category / attribute aggregation
- Similar product recommendation
- Async ES index sync via RabbitMQ listener

### 🤖 AI assistant `mall-ai` (`:8086`) ★ new in Mall-X

A standalone Spring Boot microservice exposing two endpoints with **zero business coupling** (depends only on `mall-common` + MyBatis).

**API Overview**

| Endpoint | Function | Auth |
| --- | --- | --- |
| `POST /ai/product/qa` | Product-context-aware Q&A (multi-turn optional) | Public (called by mall-portal) |
| `POST /ai/return/suggest` | 3-round guided return dialog with auto-generated reason + description | Public (called by mall-portal) |
| `GET  /v3/api-docs` | SpringDoc OpenAPI docs | — |

#### ① Product Q&A

The frontend fires a request on the product detail page with the product info + the user's question; the AI answers based on the **structured product context** (name / brand / price / subtitle).

**Request example**

```http
POST /ai/product/qa
Content-Type: application/json

{
  "productId": 1,
  "question": "How is the camera on this phone?",
  "productName": "Redmi Note 13",
  "productBrand": "Xiaomi",
  "productPrice": "1999",
  "productSubTitle": "Performance beast 5G phone",
  "conversationHistory": ""  // optional, multi-turn history
}
```

**Response example**

```json
{
  "code": 200,
  "data": { "reply": "This phone features a high-resolution main camera with night mode..." }
}
```

**Safety constraints** (hardcoded in the System Prompt): only answer based on provided product info · never promise discounts/freebies · no absolute words like "definitely" / "guaranteed" · reply in ≤100 chars.

#### ② Return Suggestion — 3-round Guided Dialog

**Core flow**

```
┌─────────┐   step=1   ┌──────────┐   step=2   ┌──────────┐   step=3   ┌──────────────┐
│ User    │ ─────────▶ │ Ask what │ ─────────▶ │ Follow-up│ ─────────▶ │ Finalize:    │
│ submits │            │ went     │            │ for      │            │ reason+desc  │
│ return  │            │ wrong    │            │ details  │            │              │
└─────────┘            └──────────┘            └──────────┘            └──────────────┘
```

Each round returns `guideQuestion` to prompt the user; round 3 returns `finished: true` + the full JSON.

**Round 3 response example**

```json
{
  "code": 200,
  "data": {
    "suggestedReason": "Damaged product",
    "suggestedDescription": "Phone screen has a crack — damage occurred in transit",
    "category": "Hardware failure",
    "confidence": "high",
    "guideQuestion": "Understood, this does affect your normal use...",
    "finished": true,
    "analysisNote": "Based on description 'phone screen has a crack...', classified as Hardware failure, matched 'Damaged product'"
  }
}
```

**Smart points**:
- **Return reasons read dynamically from DB** (`oms_order_return_reason` table, `status=1` rows) — always in sync with back-office config
- **Graceful degradation** to 5 default reasons (Quality issue / Wrong size / Wrong color / 7-day no-reason return / Other) if DB query fails
- **Auto-strip Markdown** wrappers when AI returns `​```json ... ```​` (extracts raw JSON)
- **JSON-parse fallback** by current step: rounds 1/2 give a guidance question, round 3 gives a full suggestion
- **Forced invariants** at step=3: `finished=true` and reason/description must be non-empty

#### Architecture & Safety

```
mall-portal (frontend trigger)
    │
    ▼
┌──────────────────────────────────────────────┐
│  mall-ai (:8086)                             │
│  ┌──────────┐  ┌──────────────┐  ┌────────┐  │
│  │Controller│─▶│InputSanitizer│─▶│Service │  │
│  └──────────┘  └──────────────┘  └────┬───┘  │
│  ┌──────────┐  ┌──────────────┐       │      │
│  │AiClient  │◀─┤ReturnReason  │◀──────┘      │
│  │(strategy)│  │Service(reads │              │
│  └────┬─────┘  │  DB)         │              │
│       │ HTTPS  └──────────────┘              │
│       │ (Bearer)                             │
└───────┼──────────────────────────────────────┘
        ▼
   OpenAI-compatible API
   (DeepSeek / OpenAI / SiliconFlow)
```

**🛡️ Three-layer safety** (`InputSanitizer` utility):

1. **Control-char filtering** — strips `\x00-\x1F` (keeps newline/tab)
2. **Prompt-injection detection** — 30+ attack patterns via regex (ignore instructions / role spoofing / system-prompt reveal / command exec / SQL injection / XSS)
3. **Length truncation** — user questions ≤ 5000 chars; product info ≤ 1000 chars

**🔌 Pluggable AI Provider** (Strategy pattern via `AiClient` interface):

| Implementation | Use case | Switch cost |
| --- | --- | --- |
| `OpenAiCompatibleClient` (default) | DeepSeek · OpenAI · SiliconFlow · any OpenAI-compatible API | Edit `application.yml` |
| Custom impl | On-prem / private model | Implement `AiClient` + inject Bean |

**⚙️ Typical config**

```yaml
ai:
  client:
    base-url: https://api.deepseek.com/v1
    api-key: ${AI_API_KEY:your-api-key-here}  # env var recommended
    model: deepseek-chat
    temperature: 0.7
    max-tokens: 1024
```

See `mall-ai/README.md` for full architecture & gotchas.

### 🖼️ Image proxy `mall-common-pic` ★ new in Mall-X

`GET /pic/proxy?url=<oss-url>` — backend fetch transparently streams the bytes, solving the OSS CORS 403 that browsers throw when loading `<img>` cross-origin.
Combined with `ImageUrlRewriter` at the Service layer, OSS URLs in the database are rewritten to proxy URLs transparently — zero frontend changes required.

## 🚀 Quick Start

**Prerequisites**: JDK 17 · Maven 3.8+ · Node.js 20+ · MySQL 5.7+ · Redis 5.0+ · RabbitMQ 3.8+

```bash
# 1. Initialize database
mysql -u root -p < document/sql/mall.sql

# 2. Start the 4 backend services (each in a separate terminal)
cd mall-admin  && mvn spring-boot:run   # :8080
cd mall-portal && mvn spring-boot:run   # :8085
cd mall-search && mvn spring-boot:run   # :8081
cd mall-ai     && mvn spring-boot:run   # :8086

# 3. Start the admin frontend
cd mall-admin-web && npm install && npm run dev   # http://localhost:5173
```

**Full build/run commands, port conventions, and key gotchas** (ES 8.x disable SSL, IK analyzer install, `mall-common-cors` wiring, multi-module reload steps) are in **[AGENTS.md](./AGENTS.md)**.

**Docker deployment**: [`document/docker/`](./document/docker/) + [`document/reference/docker.md`](./document/reference/docker.md)

## Documentation Index

| Document | Purpose |
| --- | --- |
| [AGENTS.md](./AGENTS.md) | Dev conventions, build/run commands, gotchas (required for devs / AI agents) |
| [CHANGELOG.md](./CHANGELOG.md) | All differences between Mall-X and upstream macrozheng/mall |
| [NOTICE](./NOTICE) | Apache 2.0 fork attribution |
| [document/architecture_diagram.md](./document/architecture_diagram.md) | Detailed Mermaid architecture |
| [document/postman/](./document/postman/) | Postman collections |
| [document/sql/mall.sql](./document/sql/mall.sql) | Database schema |
| [document/reference/](./document/reference/) | Windows deploy, shortcuts, references |
| [mall-common-pic/README.md](./mall-common-pic/README.md) | Image proxy module details |

## License

[Apache License 2.0](LICENSE)

Copyright 2018-2024 macrozheng (upstream) · Copyright 2026 mowangmowang (Mall-X derivative)

## Acknowledgments

This project is built on the excellent architecture and code of [macrozheng/mall](https://github.com/macrozheng/mall). Thanks to the original author [macrozheng](https://github.com/macrozheng) and all community contributors.
