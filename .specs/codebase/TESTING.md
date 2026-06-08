# Testing Infrastructure

## Current Frameworks

**Backend:** Spring Boot Test, JUnit 5, MockMvc.

**Frontend:** No dedicated test framework found.

**E2E:** No Playwright or Cypress setup found.

## Current Tests

**Location:** `backend/src/test/java/xyz/haimianxiaozi/security/SecurityAccessTest.java`

**Covered behavior:**

- Anonymous user cannot access `/api/chat/sessions`.
- Anonymous user can call auth login endpoint and receives validation failure.
- Anonymous user cannot access `/api/admin/users`.

## Verified Commands

**Backend:**

```bash
mvn test
```

Result on 2026-06-08: passed, 3 tests.

Flyway integration note: security-only tests set `spring.flyway.enabled=false` so they do not require a running MySQL instance.

Migration SQL verification on 2026-06-08:

- Temporary MySQL 8.0.44 instance started with an isolated workspace data directory.
- `V1__init_schema.sql` created 18 business tables and 4 seed categories.
- `V2__ensure_post_publish_settings.sql` no-oped on the fresh schema.
- After simulating a legacy `post` table by dropping `comment_enabled`, `visibility` and `idx_visibility`, `V2` restored both columns and the index.
- Temporary MySQL instance was shut down and the data directory removed.

**Frontend:**

```bash
npm run build
```

Result on 2026-06-08: passed. Nuxt/Vite emitted third-party dependency warnings.

## CI

**Location:** `.github/workflows/ci.yml`

Current CI jobs:

- Backend Maven package with skipped tests.
- Backend Maven test.
- Frontend npm ci and Nuxt build.

## Gaps

- No backend tests for successful login, logout, token blacklist, rate limiter, role gates with authenticated users, post/comment operations, admin operations, uploads, notifications or WebSocket handshake.
- No test database strategy found. Existing tests avoid persistence-heavy paths.
- No frontend typecheck script.
- No frontend unit/component tests.
- No E2E smoke test for login, posting, admin access and notification flows.
- No coverage measurement or minimum threshold.

## Recommended Test Phases

1. Add backend integration tests for security and role access.
2. Add service/controller tests for post, comment, like and admin workflows.
3. Add Testcontainers or a dedicated test database profile.
4. Add `typecheck` and lint scripts to frontend.
5. Add Playwright smoke tests once local full-stack startup is stable.
