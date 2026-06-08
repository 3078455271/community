# Project State

**Last updated:** 2026-06-08

## Current Focus

- 将项目从功能型论坛提升为企业级可交付论坛。
- 当前优先级是 M1 工程基线与生产可交付性。

## Verified Baseline

- `mvn test` in `backend`: passed, 3 tests.
- `npm run build` in `frontend`: passed, with third-party dependency warnings from Nuxt/Vite build.
- CodeGraph index exists and reports 166 indexed files, 2514 nodes, 4529 edges.
- Flyway dependency and migration resources compile in `mvn test`; test context disables Flyway to avoid requiring MySQL for security-only tests.
- Flyway SQL was executed against a temporary MySQL 8.0.44 instance on 2026-06-08. `V1` created 18 business tables and 4 seed categories; `V2` restored `post.comment_enabled`, `post.visibility` and `idx_visibility` after a simulated legacy schema.

## Decisions

- 保持现有 Spring Boot + Nuxt 单体前后端分离架构。
- 优先补数据库迁移、测试、API 文档、配置、观测和部署说明。
- 不做微服务拆分，除非后续有明确规模和团队边界需求。
- 所有后续重构必须绑定具体风险或测试缺口。
- 数据库迁移工具选择 Flyway，并由 Spring Boot 在应用启动时默认执行。

## Known Constraints

- `.codegraph/` 是本地索引，不提交。
- 项目 shell 命令按 RTK 约束执行。
- AGENTS.md 要求后端统一 `R<T>`、Service 放业务、Controller 做参数和返回。

## Open Questions

- 企业级目标的第一批上线对象是内部演示、真实生产社区，还是企业内网知识社区。
- 是否允许引入 Springdoc、Actuator、Testcontainers、ESLint/Vitest/Playwright 等新依赖。
- 是否要求兼容现有线上数据库数据。
- Docker daemon was unavailable during verification, so MySQL validation used an isolated temporary local MySQL instance instead.

## Deferred Ideas

- 搜索服务外置。
- 消息队列异步化通知、积分和审计。
- 多租户空间、组织成员和企业 SSO。
