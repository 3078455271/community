# Community Zhuge - 社区论坛

基于 Vue 3 + Spring Boot 的社区论坛系统。

## 技术栈

- **前端**: Nuxt 3 + Vue 3 + TypeScript + Element Plus
- **后端**: Spring Boot 3 + JDK 21 + MyBatis-Plus
- **数据库**: MySQL 8.0 + Redis 7
- **部署**: Docker + Docker Compose

## 快速开始

### 本地开发

1. 克隆项目
```bash
git clone https://github.com/your-username/community-zhuge.git
cd community-zhuge
```

2. 启动后端
```bash
cd backend
mvn spring-boot:run
```

3. 启动前端
```bash
cd frontend
npm install
npm run dev
```

### Docker 部署

1. 配置环境变量
```bash
cp .env.example .env
# 编辑 .env 文件，修改数据库密码等配置
```

2. 启动服务
```bash
docker-compose up -d
```

3. 访问应用
- 前端: http://localhost
- 后端 API: http://localhost/api

## 项目结构

```
community-zhuge/
├── frontend/          # Nuxt 3 前端
├── backend/           # Spring Boot 后端
├── docker/            # Docker 配置
├── docker-compose.yml # 开发环境编排
└── docker-compose.prod.yml # 生产环境编排
```

## API 文档

启动后端后访问: http://localhost:8080/swagger-ui.html

## License

MIT