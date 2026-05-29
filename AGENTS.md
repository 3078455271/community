# AGENTS.md - 项目开发规范

> 本文件用于指导 AI 助手在本项目中的编码行为
> 基于阿里巴巴Java开发手册（黄山版）和 Vue 3 前端开发规范整理

---

## 项目概述

- **项目名称**: Community Zhuge - 社区论坛
- **后端**: Spring Boot 3 + JDK 21 + MyBatis-Plus + MySQL + Redis
- **前端**: Nuxt 3 + Vue 3 + TypeScript + Element Plus
- **部署**: Docker + Docker Compose

---

## 后端开发规范（Java / Spring Boot）

### 命名规范

| 类型 | 规则 | 示例 |
|------|------|------|
| 类名 | UpperCamelCase | `UserService`, `PostController` |
| 方法名/变量名 | lowerCamelCase | `getById`, `userName` |
| 常量 | 全大写下划线 | `MAX_PAGE_SIZE` |
| 包名 | 全小写 | `xyz.haimianxiaozi.service` |
| 数据库字段 | 小写下划线 | `user_id`, `created_at` |

### 代码格式

- 缩进：4 个空格，禁止 Tab
- 单行不超过 120 字符
- 大括号前不换行，后换行
- 运算符两侧加空格

### OOP 规约

- 覆写方法必须加 `@Override`
- POJO 类属性使用包装类型（Long, Integer 等）
- POJO 类必须有 `toString()` 方法
- 构造方法禁止业务逻辑

### 异常处理

- 禁止用异常做流程控制
- catch 后必须处理，不能吞掉异常
- finally 必须关闭资源
- 使用 SLF4J 日志框架，禁止 `System.out.println`

### 集合处理

- 使用 `isEmpty()` 判断空，不要用 `size() == 0`
- 不要在 foreach 中 remove/add
- 集合初始化指定容量大小
- 使用 entrySet 遍历 Map

### 并发处理

- 使用线程池，禁止自行创建线程
- SimpleDateFormat 线程不安全，使用 DateTimeFormatter
- ThreadLocal 必须回收

### 数据库规范

- 表名小写下划线，不使用复数
- 主键索引：`pk_字段名`，唯一索引：`uk_字段名`，普通索引：`idx_字段名`
- 必备字段：`id`, `created_at`, `updated_at`, `deleted`
- 使用逻辑删除，禁止物理删除
- 使用参数绑定，禁止 SQL 拼接

### API 规范

- RESTful 风格设计
- JSON key 使用 lowerCamelCase
- 返回空集合用 `[]` 或 `{}`
- 时间格式：`yyyy-MM-dd HH:mm:ss`

### Spring Boot 特定规范

- Controller 只做参数校验和返回，业务逻辑放 Service
- 使用 `@RequiredArgsConstructor` 注入依赖
- 使用 `@Valid` 校验请求参数
- 统一响应格式：`R<T>` 包装

### 包结构

```
xyz.haimianxiaozi/
├── controller/     # 控制器
├── service/        # 接口
│   └── impl/       # 实现
├── mapper/         # MyBatis Mapper
├── entity/         # 数据库实体
├── dto/            # 请求参数
├── vo/             # 响应数据
├── enums/          # 公共枚举（CommonEnums 等）
├── config/         # 配置类
├── common/         # 公共类
└── util/           # 工具类
```

**枚举规范**：公共枚举统一放在 `enums` 包下，命名为 `XxxEnums` 或 `XxxEnum`，用于定义状态码、类型等常量集合。

---

## 前端开发规范（Vue 3 / Nuxt 3 / TypeScript）

### 命名规范

| 类型 | 规则 | 示例 |
|------|------|------|
| 组件名 | PascalCase | `UserCard.vue`, `PostList.vue` |
| 文件名 | kebab-case | `user-card.vue`, `post-list.vue` |
| 变量名 | lowerCamelCase | `userList`, `isLoading` |
| 方法名 | lowerCamelCase + 动宾 | `fetchUserList`, `handleSubmit` |
| 类型名 | PascalCase | `UserInfo`, `PostData` |

### 组件规范

- 必须使用 `<script setup lang="ts">` 语法
- 组件结构顺序：script -> template -> style
- 单个组件不超过 300 行，超过需拆分
- 使用 scoped 样式隔离

```vue
<script setup lang="ts">
// 逻辑代码
</script>

<template>
  <!-- 模板 -->
</template>

<style scoped>
/* 样式 */
</style>
```

### TypeScript 规范

- 必须使用 TypeScript
- 禁止使用 `any`，除非必要
- 所有 props 必须显式声明类型
- API 返回数据定义类型接口

```typescript
// 好的写法
const props = defineProps<{
  userId: string
  title?: string
}>()

// 坏的写法
const props = defineProps(['userId', 'title'])
```

### 响应式规范

- 简单类型用 `ref`，复杂类型用 `reactive`
- ref 必须声明默认值或类型

```typescript
// 好
const count = ref(0)
const user = reactive({ name: '', age: 0 })

// 坏
const count = ref()
const user = ref({ name: '', age: 0 })
```

### 代码风格

- 使用 ES6+ 语法
- 使用 async/await，避免 Promise 回调
- 使用可选链 `?.` 和空值合并 `??`
- 禁止直接操作 DOM，使用 Vue Ref

```typescript
// 好
const data = await fetchData()

// 坏
fetchData().then(res => {
  data = res
})
```

### 样式规范

- 禁止内联样式
- 使用 UnoCSS / Tailwind CSS 原子类
- 组件样式使用 scoped

### 路由规范（Nuxt 3）

- 使用文件系统路由：`pages/posts/[id].vue` -> `/posts/:id`
- 动态参数使用方括号：`[id].vue`, `[slug].vue`
- 嵌套路由使用目录结构

### API 请求规范

- 使用 `composables/useApi.ts` 封装请求
- 统一错误处理
- 请求/响应拦截器

```typescript
export const useApi = () => {
  const config = useRuntimeConfig()
  
  return {
    get: <T>(url: string, params?: any) => request<T>(url, { method: 'GET', params }),
    post: <T>(url: string, body?: any) => request<T>(url, { method: 'POST', body }),
  }
}
```

### 状态管理（Pinia）

- 使用 `defineStore` + Composition API
- 持久化数据使用 localStorage

```typescript
export const useUserStore = defineStore('user', () => {
  const token = ref('')
  const setToken = (t: string) => { token.value = t }
  return { token, setToken }
})
```

### 目录结构

```
frontend/
├── pages/          # 页面路由
├── layouts/        # 布局组件
├── components/     # 公共组件
├── composables/    # 组合式函数
├── stores/         # Pinia 状态
├── assets/         # 静态资源
├── types/          # 类型定义
└── utils/          # 工具函数
```

### 注释规范

- 使用 JSDoc 格式注释函数和组件
- TODO 标记待完成事项

```typescript
/** 获取用户列表 */
const fetchUsers = async () => { ... }

/**
 * TODO: 添加分页参数支持
 * NOTE: 当前返回全量数据
 */
```

---

## 通用规范

### Git 提交规范

- feat: 新功能
- fix: 修复
- docs: 文档
- style: 格式
- refactor: 重构
- test: 测试
- chore: 构建/工具

### 安全规范

- 所有外部输入必须校验
- 使用参数绑定防止 SQL 注入
- 输出前转义防止 XSS
- 敏感信息脱敏处理
- 密码等敏感数据加密存储

### 性能规范

- 避免 N+1 查询
- 合理使用缓存
- 大列表使用分页
- 图片懒加载
- 组件按需加载

---

## 版本信息

- Java 开发规范：基于阿里巴巴 Java 开发手册（黄山版 1.7.1，2022.02.03）
- 前端开发规范：基于 Vue 3 + TypeScript 项目适用规范
- 最后更新：2024