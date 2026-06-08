# Tech Stack

**Analyzed:** 2026-06-08

## Core

- Project type: 前后端分离论坛项目。
- Backend language/runtime: Java 21.
- Backend framework: Spring Boot 3.2.5.
- Frontend language/runtime: TypeScript, Node 20 in Docker and CI.
- Frontend framework: Nuxt 3, Vue 3.
- Database: MySQL 8.0.
- Cache/session adjacent store: Redis 7.
- Package managers: Maven, npm.

## Backend

- API style: REST controllers under `/api`.
- Persistence: MyBatis-Plus 3.5.6 with mapper interfaces and entity classes.
- Database migrations: Flyway 9.22.3 via Spring Boot dependency management.
- Security: Spring Security, JWT via JJWT 0.12.5, HttpOnly auth cookie, Redis token blacklist, Redis auth rate limiter.
- Validation: Spring Boot Validation with DTO annotations.
- Realtime: Spring WebSocket with authenticated handshake.
- Object storage: Aliyun OSS SDK 3.17.4.

## Frontend

- UI framework: Element Plus, `@element-plus/nuxt`.
- State management: Pinia via `@pinia/nuxt`.
- API client: `frontend/composables/useApi.ts` wraps `$fetch` with `credentials: include`.
- Routing: Nuxt file routes in `frontend/pages`.
- Styling: global CSS variables plus scoped component styles.

## Testing

- Backend: Spring Boot Test, JUnit 5, MockMvc.
- Current tests: `SecurityAccessTest` only.
- Frontend: no dedicated test framework found.
- E2E: no Playwright/Cypress setup found.

## Delivery

- Dockerfiles exist for backend and frontend.
- Docker Compose includes MySQL, Redis, backend, frontend and Nginx.
- GitHub Actions has CI and tag-triggered deployment workflows.

## Not Found

- Springdoc/OpenAPI dependency despite README mentioning Swagger UI.
- Actuator/Micrometer dependency.
- ESLint, Vitest, Playwright or Cypress.
