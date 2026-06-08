# Project Structure

**Root:** `D:\java\communiy\Community-zhuge`

## Directory Tree

```text
Community-zhuge/
├── backend/
│   ├── src/main/java/xyz/haimianxiaozi/
│   ├── src/main/resources/
│   ├── src/test/java/
│   ├── pom.xml
│   └── Dockerfile
├── frontend/
│   ├── pages/
│   ├── components/
│   ├── composables/
│   ├── stores/
│   ├── types/
│   ├── assets/
│   ├── package.json
│   └── Dockerfile
├── docker/
│   ├── mysql/
│   └── nginx/
├── .github/workflows/
├── docker-compose.yml
├── docker-compose.prod.yml
└── README.md
```

## Module Organization

### Backend API

**Purpose:** HTTP API, security, business services and persistence.
**Location:** `backend/src/main/java/xyz/haimianxiaozi`.
**Key areas:** `controller`, `service`, `service/impl`, `mapper`, `entity`, `dto`, `vo`, `security`, `websocket`, `config`.

### Frontend Web

**Purpose:** Nuxt SSR web app.
**Location:** `frontend`.
**Key areas:** `pages`, `components`, `composables`, `stores`, `types`, `assets/css`.

### Database Bootstrap

**Purpose:** MySQL schema and seed data.
**Location:** `backend/src/main/resources/db/migration`.
**Key files:** `V1__init_schema.sql`, `V2__ensure_post_publish_settings.sql`.
**Legacy reference:** `docker/mysql/init.sql`.

### Delivery

**Purpose:** Containers, proxy and CI/CD.
**Location:** root Docker Compose files, `backend/Dockerfile`, `frontend/Dockerfile`, `docker/nginx/nginx.conf`, `.github/workflows`.

## Where Things Live

**Authentication:**

- UI: `frontend/pages/login.vue`, `frontend/pages/register.vue`
- State: `frontend/stores/user.ts`
- Backend: `AuthController`, `JwtAuthenticationFilter`, `AuthCookieManager`, `TokenBlacklistService`

**Posts and comments:**

- UI: `frontend/pages/posts`, `PostCard.vue`, `CommentItem.vue`
- Backend: `PostController`, `CommentController`, `PostServiceExtImpl`, `CommentServiceImpl`
- Data: `post`, `comment`, `post_tag`, `tag`

**Admin and moderation:**

- UI: `frontend/pages/admin.vue`, `frontend/middleware/admin.ts`
- Backend: `AdminController`, `AdminServiceImpl`, `ContentModerationServiceImpl`
- Data: `report`, `sensitive_word`, `audit_log`, user role/status fields

**Notifications and chat:**

- UI: `NotificationBell.vue`, `pages/notifications.vue`, `pages/chat.vue`
- Backend: `NotificationController`, `ChatController`, websocket package
- Data: `notification`, `chat_message`

**File upload:**

- UI: `FileUpload.vue`, `useUpload.ts`
- Backend: `FileController`, `OssServiceImpl`
- External: local upload directory and Aliyun OSS
