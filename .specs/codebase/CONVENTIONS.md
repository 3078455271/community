# Code Conventions

## Naming

**Backend classes:** UpperCamelCase, for example `AuthController`, `PostServiceImpl`, `JwtAuthenticationFilter`.

**Backend methods and fields:** lowerCamelCase, for example `getCurrentUserId`, `markAllAsRead`, `createdAt`.

**Backend packages:** lower-case under `xyz.haimianxiaozi`.

**Frontend files:** Mixed current style. Components use PascalCase such as `PostCard.vue`, pages use Nuxt route names such as `pages/posts/[id].vue`.

**Frontend variables and functions:** lowerCamelCase, for example `fetchUsers`, `activeTab`, `handleSearch`.

## Backend Organization

- Controllers live in `backend/src/main/java/xyz/haimianxiaozi/controller`.
- Services are interfaces in `service` and implementations in `service/impl`.
- MyBatis-Plus mappers live in `mapper`.
- Entities map directly to database tables with `@TableName`.
- DTOs and VOs are separate packages.
- Shared enums are in `enums`.

## Frontend Organization

- Nuxt pages live in `frontend/pages`.
- Shared components live in `frontend/components`.
- Request helpers live in `frontend/composables`.
- Pinia user state lives in `frontend/stores/user.ts`.
- Shared types live in `frontend/types/index.ts`.

## Error Handling

- Backend usually returns `R.fail(...)` from controllers for expected business failures.
- `GlobalExceptionHandler` exists for validation and exceptions.
- Some controller methods catch broad exceptions and return failure responses.
- Frontend API wrapper redirects on 401 and rethrows errors.

## Validation

- Backend DTOs use Jakarta Validation in auth, post, comment, report and upload flows.
- Some endpoints still accept raw request bodies without `@Valid`, especially draft operations.
- Frontend uses Element Plus form rules for login, register and profile forms.

## Comments

- Java code contains concise Chinese comments for feature intent.
- Vue and TypeScript comments are mostly type/interface descriptions.
- Future comments should explain rules or constraints, not restate the code.

## Observed Deviations From Project Rules

- Some Controller methods contain business logic that should eventually move to Service.
- POJO `toString()` is not explicitly implemented, relying on Lombok `@Data` for entities.
- `application.yml` uses MyBatis stdout SQL logging, which is not suitable for production.
- Frontend uses scoped CSS and TypeScript, but there is no lint or typecheck script in `package.json`.

