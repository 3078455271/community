<template>
  <div class="forum-shell">
    <aside class="forum-sidebar">
      <NuxtLink to="/" class="brand">
        <span class="brand-mark">M</span>
        <span>HeidanForum</span>
      </NuxtLink>

      <nav class="side-nav">
        <NuxtLink
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </NuxtLink>
        <NuxtLink v-if="isManager" to="/admin" class="nav-item" :class="{ active: isActive('/admin') }">
          <el-icon><Setting /></el-icon>
          <span>后台管理</span>
        </NuxtLink>
      </nav>

      <el-button class="create-button" type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        发布新帖子
      </el-button>

      <div class="sidebar-footer">
        <div class="divider"></div>
        <div class="theme-row">
          <span>
            <el-icon><Moon /></el-icon>
            切换主题
          </span>
          <el-switch v-model="darkTheme" size="small" />
        </div>
      </div>
    </aside>

    <header class="mobile-header">
      <NuxtLink to="/" class="brand">
        <span class="brand-mark">M</span>
        <span>HeidanForum</span>
      </NuxtLink>
      <el-button type="primary" size="small" @click="handleCreate">发布</el-button>
    </header>

    <main class="forum-main">
      <slot />
    </main>

    <aside class="forum-rightbar">
      <div class="user-card">
        <p class="spark">✨ 独立开发基地</p>
        <p class="welcome">欢迎回来，{{ displayName }}，今天社区有 12 个新话题，快去打卡吧！</p>
        <el-button class="checkin-button" type="primary" @click="handleCheckIn">
          每日签到领积分
        </el-button>
      </div>

      <div class="trend-card">
        <h3>📈 今日热榜</h3>
        <ol>
          <li v-for="(item, index) in trends" :key="item">
            <span>{{ index + 1 }}</span>
            <p>{{ item }}</p>
          </li>
        </ol>
      </div>

      <template v-if="userStore.isLoggedIn">
        <NotificationBell />
      </template>
      <div v-else class="auth-card">
        <el-button type="primary" @click="navigateTo('/login')">登录</el-button>
        <el-button @click="navigateTo('/register')">注册</el-button>
      </div>
    </aside>
  </div>
</template>

<script setup lang="ts">
import { Bell, Compass, HomeFilled, Moon, Plus, Setting, User } from '@element-plus/icons-vue'

const route = useRoute()
const userStore = useUserStore()
const darkTheme = ref(false)
const isManager = computed(() => ['ADMIN', 'MODERATOR'].includes(userStore.userInfo?.role || ''))
const displayName = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.username || '开发者')

const navItems = [
  { label: '首页', path: '/', icon: HomeFilled },
  { label: '发现板块', path: '/posts', icon: Compass },
  { label: '通知中心', path: '/chat', icon: Bell },
  { label: '个人中心', path: '/user/profile', icon: User },
]

const trends = [
  '英寸开源全栈架构，前端狂喜？',
  '独立开发的第二年，我赚到了第一桶金',
  '如何评价今年的大模型应用落地潮？',
]

const isActive = (path: string) => {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

const handleCreate = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    navigateTo('/login')
    return
  }
  navigateTo('/posts/create')
}

const handleCheckIn = () => {
  ElMessage.success('今日签到成功')
}

onMounted(() => {
  userStore.loadFromStorage()
})
</script>

<style scoped>
.forum-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 280px minmax(0, 760px) 280px;
  justify-content: center;
  background: #f7f8fb;
  color: #121826;
}

.forum-sidebar {
  position: sticky;
  top: 0;
  height: 100vh;
  padding: 26px 24px;
  border-right: 1px solid #e9ebf0;
  background: #fff;
  display: flex;
  flex-direction: column;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 800;
}

.brand-mark {
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #2f6df6;
  color: #fff;
  font-size: 16px;
}

.side-nav {
  display: grid;
  gap: 12px;
  margin-top: 34px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 13px 14px;
  border-radius: 8px;
  color: #5d6472;
  font-weight: 600;
}

.nav-item.active {
  background: #eceef2;
  color: #2563eb;
}

.create-button {
  width: 100%;
  height: 46px;
  margin-top: 34px;
  border-radius: 8px;
  font-weight: 700;
  box-shadow: 0 10px 18px rgba(47, 109, 246, 0.24);
}

.sidebar-footer {
  margin-top: auto;
}

.divider {
  height: 1px;
  margin-bottom: 18px;
  background: #eceef2;
}

.theme-row,
.theme-row span {
  display: flex;
  align-items: center;
}

.theme-row {
  justify-content: space-between;
  color: #5d6472;
  font-size: 14px;
}

.theme-row span {
  gap: 8px;
}

.forum-main {
  min-height: 100vh;
  padding: 28px 24px 60px;
  background: #fff;
}

.forum-rightbar {
  position: sticky;
  top: 0;
  height: 100vh;
  padding: 22px 24px;
  border-left: 1px solid #e9ebf0;
  background: #fff;
}

.user-card,
.trend-card,
.auth-card {
  border: 1px solid #edf0f5;
  border-radius: 8px;
  background: #fff;
}

.user-card {
  padding: 18px;
  text-align: center;
  background: #f8f8fa;
}

.spark {
  margin: 0 0 12px;
  font-weight: 800;
}

.welcome {
  margin: 0 0 12px;
  color: #98a0ae;
  font-size: 13px;
  line-height: 1.6;
}

.checkin-button {
  width: 100%;
  border-radius: 8px;
  background: #18191f;
  border-color: #18191f;
}

.trend-card {
  margin-top: 18px;
  padding: 16px;
}

.trend-card h3 {
  margin: 0 0 12px;
  font-size: 15px;
}

.trend-card ol {
  display: grid;
  gap: 10px;
  padding: 0;
  margin: 0;
  list-style: none;
}

.trend-card li {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.trend-card li span {
  width: 18px;
  height: 18px;
  flex: 0 0 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  background: #f04040;
  color: #fff;
  font-size: 12px;
}

.trend-card li:nth-child(n + 3) span {
  background: #98a0ae;
}

.trend-card p {
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.auth-card {
  display: flex;
  gap: 10px;
  margin-top: 18px;
  padding: 14px;
}

.mobile-header {
  display: none;
}

@media (max-width: 1180px) {
  .forum-shell {
    grid-template-columns: 240px minmax(0, 1fr);
  }

  .forum-rightbar {
    display: none;
  }
}

@media (max-width: 760px) {
  .forum-shell {
    display: block;
  }

  .forum-sidebar {
    display: none;
  }

  .mobile-header {
    position: sticky;
    top: 0;
    z-index: 10;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    border-bottom: 1px solid #e9ebf0;
    background: #fff;
  }

  .forum-main {
    min-height: calc(100vh - 57px);
    padding: 18px 14px 40px;
  }
}
</style>
