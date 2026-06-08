# Community Zhuge Enterprise Forum

**Vision:** 将 Community Zhuge 从功能型论坛原型演进为可运营、可审计、可扩展、可交付的企业级社区论坛平台。
**For:** 社区运营团队、内容创作者、普通用户、审核员、管理员和运维人员。
**Solves:** 提供稳定的内容发布、互动、治理、通知、私信、积分和后台运营能力，并让安全、数据、测试、部署和运维达到可持续迭代标准。

## Goals

- 核心论坛闭环可用：注册登录、发帖、评论、点赞、收藏、关注、搜索、通知、私信、举报、审核、积分在 Web 和 API 层稳定运行。
- 企业级工程基线可验证：后端测试覆盖核心写链路和权限链路，前端具备构建、类型检查、关键页面验证，CI 能阻止明显回归。
- 安全与治理可落地：认证、授权、限流、审计、敏感词、禁言、举报处理、文件上传均有明确边界和测试。
- 数据演进可控：数据库结构使用版本化迁移管理，生产部署不依赖一次性初始化脚本或手动补丁。
- 运维可观察：健康检查、日志、指标、错误定位、部署回滚和配置说明可供生产使用。

## Tech Stack

**Core:**

- Backend: Spring Boot 3.2.5, Java 21, MyBatis-Plus 3.5.6
- Frontend: Nuxt 3, Vue 3, TypeScript, Element Plus, Pinia
- Database: MySQL 8.0, Redis 7
- Delivery: Docker, Docker Compose, GitHub Actions

**Key dependencies:**

- Spring Security, Spring Validation, Spring WebSocket
- JJWT 0.12.5
- Aliyun OSS SDK 3.17.4
- Nuxt, Element Plus, Pinia

## Scope

**v1 includes:**

- 用户账号、安全登录、个人资料、角色和后台权限。
- 帖子、草稿、分类、标签、评论、点赞、收藏、关注、浏览历史和搜索。
- 通知、WebSocket 推送、私信、@提及。
- 举报、敏感词、禁言、封禁、后台审计。
- 积分、签到、等级等社区成长能力。
- Docker 化部署、基础 CI、数据库初始化。

**Enterprise hardening includes:**

- 数据库迁移体系、测试体系、API 文档、运维手册、配置治理、日志与指标。
- 幂等和并发保护，尤其是点赞、评论计数、收藏、关注、积分奖励和通知投递。
- 安全基线，尤其是 Cookie、CSRF、CORS、上传校验、权限测试和敏感配置管理。

**Explicitly out of scope for the first enterprise milestone:**

- 多租户商业化。
- 大规模推荐算法和复杂内容风控模型。
- 原生移动端。
- 微服务拆分。

## Constraints

- 先保持现有 Spring Boot + Nuxt 架构，不为“企业级”过早拆服务。
- 变更必须可测试、可回滚，优先补齐地基再扩展大功能。
- 遵守项目 AGENTS.md 的 Java、Vue、数据库、安全和 Git 规范。
- `.codegraph/` 仅为本地索引，不提交。

