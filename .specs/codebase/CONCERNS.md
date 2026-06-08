# Codebase Concerns

**Analyzed:** 2026-06-08

## High Priority

### Flyway needs automated database regression coverage

**Evidence:** Flyway has been added with migrations under `backend/src/main/resources/db/migration`, and the old `alter_post_settings.sql` manual patch was replaced by `V2__ensure_post_publish_settings.sql`. The SQL was manually verified against a temporary MySQL 8.0.44 instance, but CI does not yet run migrations against a real database.

**Risk:** Future migrations could pass unit tests while failing on MySQL-specific DDL or data changes.

**Fix approach:** Add Testcontainers or a CI MySQL service and run Flyway migrations automatically in the backend test job.

### Core business writes have limited test coverage

**Evidence:** Only `SecurityAccessTest` exists under `backend/src/test`. CodeGraph reports no covering tests for `Post`, `Comment`, `Notification`, `NotificationService`, `AdminService.audit`.

**Risk:** Post, comment, notification, admin and counter regressions can ship unnoticed.

**Fix approach:** Add integration tests for authenticated users, admin/moderator users, post/comment/like flows, report handling and notification creation.

### Interaction counters are updated in application code

**Evidence:** `PostController` increments and decrements `likeCount`; `CommentController` increments `commentCount` and `likeCount`.

**Risk:** Concurrent likes/comments can lose updates. Failed downstream notification or point operations can leave inconsistent state if transactions are incomplete.

**Fix approach:** Move write workflows to services, use transactions, atomic SQL updates and idempotency tests.

### Controller layer contains business logic

**Evidence:** `PostController`, `CommentController`, `AdminController` and `FileController` perform permission checks, moderation checks, entity mutation, notification and point logic.

**Risk:** Rules duplicate across endpoints and are harder to test without HTTP setup.

**Fix approach:** Move cohesive workflows to service methods, keep controllers focused on validation, current user extraction and response mapping.

## Medium Priority

### README mentions Swagger UI but no OpenAPI dependency was found

**Evidence:** README points to `http://localhost:8080/swagger-ui.html`; dependency scan did not find Springdoc, Swagger or Knife4j.

**Risk:** New contributors and clients cannot rely on generated API docs.

**Fix approach:** Add Springdoc OpenAPI or update README if API docs are intentionally absent.

### Production observability is minimal

**Evidence:** No Actuator or Micrometer dependency found. `application.yml` configures MyBatis SQL stdout logging.

**Risk:** Health checks, metrics and production diagnostics are weak. SQL stdout can leak noisy or sensitive information.

**Fix approach:** Add Actuator health endpoint, environment-specific logging profiles and production-safe SQL logging defaults.

### Frontend lacks quality gates beyond build

**Evidence:** `frontend/package.json` only has `build`, `dev`, `generate`, `preview`, `postinstall`.

**Risk:** Type errors, lint issues and behavior regressions may only surface late.

**Fix approach:** Add typecheck and lint scripts first, then add focused component or E2E smoke tests.

### Upload security policy is split

**Evidence:** `FileController` supports local upload with extension checks and OSS multipart upload.

**Risk:** MIME spoofing, object key handling, local disk exposure and production storage policy are unclear.

**Fix approach:** Define one production upload policy, validate MIME and extension, constrain object keys, test file limits and document public/private URL behavior.

## Low Priority

### CI/CD branch naming may be inconsistent

**Evidence:** CI runs on `main` and `develop`; deploy script pulls `master`; current local branch is `master`.

**Risk:** Deploy automation may pull a different branch than CI validated.

**Fix approach:** Standardize branch strategy and update workflows.

### Frontend page components may grow too large

**Evidence:** Admin and profile pages hold many operations in a single component.

**Risk:** Maintenance and targeted testing become harder as admin workflows grow.

**Fix approach:** Split by workflow after test coverage exists, not as an isolated cosmetic refactor.
