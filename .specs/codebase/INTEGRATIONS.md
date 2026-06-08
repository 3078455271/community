# External Integrations

## MySQL

**Purpose:** Primary relational storage.
**Implementation:** MyBatis-Plus entities, mappers and services.
**Configuration:** `spring.datasource` in `backend/src/main/resources/application.yml`.
**Bootstrap:** Flyway migrations in `backend/src/main/resources/db/migration`.
**Legacy reference:** `docker/mysql/init.sql`.

## Flyway

**Purpose:** Versioned database schema migration.
**Implementation:** `flyway-core` dependency managed by Spring Boot.
**Configuration:** `spring.flyway` in `backend/src/main/resources/application.yml`.
**Migrations:**

- `V1__init_schema.sql`
- `V2__ensure_post_publish_settings.sql`

## Redis

**Purpose:** Auth rate limiting and token blacklist; Redis may also support future caches.
**Implementation:**

- `AuthRateLimiter`
- `TokenBlacklistService`

**Configuration:** `spring.data.redis` in `application.yml`.

## Aliyun OSS

**Purpose:** Object storage for uploads, especially multipart upload flow.
**Implementation:** `OssService`, `OssServiceImpl`, `OssConfig`, `FileController`.
**Configuration:** `aliyun.oss.*` environment variables.
**Concern:** Local upload endpoint and OSS multipart endpoint coexist and need clearer production policy.

## WebSocket

**Purpose:** Realtime notifications and chat-related push.
**Implementation:** `WebSocketConfig`, `AuthenticatedHandshakeInterceptor`, `AuthenticatedWebSocketHandler`, `WebSocketSessionRegistry`, `WebSocketEventPublisher`.
**Authentication:** Token read through auth cookie during handshake.

## Nginx

**Purpose:** Reverse proxy to Nuxt frontend and Spring Boot backend.
**Implementation:** `docker/nginx/nginx.conf`.
**Routes:**

- `/` proxies to frontend SSR.
- `/api/` proxies to backend.
- static assets receive long cache headers.

## GitHub Actions

**Purpose:** CI and deployment.
**Implementation:** `.github/workflows/ci.yml`, `.github/workflows/deploy.yml`.
**Current behavior:**

- CI builds backend and frontend.
- Deploy builds and pushes images to GHCR, then deploys over SSH on tag push.

## Docker Compose

**Purpose:** Local and production orchestration.
**Implementation:** `docker-compose.yml`, `docker-compose.prod.yml`.
**Services:** MySQL, Redis, backend, frontend, Nginx.

## Missing or Not Yet Integrated

- API documentation service such as Springdoc OpenAPI.
- Health and metrics via Spring Boot Actuator.
- Dedicated logging or tracing backend.
- Message queue for asynchronous notification and audit processing.
- Search engine beyond MySQL fulltext index.
