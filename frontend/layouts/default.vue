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

        <!-- 侧边栏底部：登录/退出区域 -->
        <div class="sidebar-auth" v-if="!userStore.isLoggedIn">
          <el-button type="primary" class="sidebar-auth-btn" @click="navigateTo('/login')">登录</el-button>
          <el-button class="sidebar-auth-btn" @click="navigateTo('/register')">注册</el-button>
        </div>
        <div class="sidebar-auth" v-else>
          <div class="sidebar-user-info">
            <el-icon><User /></el-icon>
            <span>{{ displayName }}</span>
          </div>
          <el-button text type="danger" size="small" @click="handleLogout">退出登录</el-button>
        </div>

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
      <div class="mobile-header-actions">
        <el-button size="small" @click="handleCreate">发布</el-button>
        <template v-if="userStore.isLoggedIn">
          <el-button size="small" text type="danger" @click="handleLogout">退出</el-button>
        </template>
        <template v-else>
          <el-button size="small" type="primary" @click="navigateTo('/login')">登录</el-button>
        </template>
      </div>
    </header>

    <main class="forum-main">
      <slot />
    </main>

    <aside class="forum-rightbar">
      <div class="user-card" v-if="userStore.isLoggedIn">
        <p class="spark">✨ 独立开发基地</p>
        <p class="welcome">欢迎回来，{{ displayName }}，今天社区有 12 个新话题，快去打卡吧！</p>
        <el-button
          class="checkin-button"
          type="primary"
          :disabled="signedInToday"
          :loading="checkingIn"
          @click="handleCheckIn"
        >
          {{ signedInToday ? '今日已签到 ✓' : '每日签到领积分' }}
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
        <div class="user-actions">
          <NotificationBell />
          <el-button text type="danger" size="small" @click="handleLogout">退出登录</el-button>
        </div>
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
const darkTheme = ref(
  import.meta.client ? localStorage.getItem('theme') === 'dark' : false
)
const signedInToday = ref(false)
const checkingIn = ref(false)
const api = useApi()
const webSocket = useWebSocket()

// 切换主题时应用到 DOM
watch(darkTheme, (isDark) => {
  if (import.meta.client) {
    document.documentElement.setAttribute('data-theme', isDark ? 'dark' : 'light')
    localStorage.setItem('theme', isDark ? 'dark' : 'light')
  }
}, { immediate: true })
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

const handleCheckIn = async () => {
  if (signedInToday.value) return
  checkingIn.value = true
  try {
    const res = await api.post<{ code: number; data: { signedInToday: boolean } }>('/points/sign-in')
    if (res.code === 200) {
      signedInToday.value = true
      ElMessage.success('签到成功，积分 +5')
    } else {
      ElMessage.error(res.message || '签到失败')
    }
  } catch {
    ElMessage.error('签到失败，请稍后重试')
  } finally {
    checkingIn.value = false
  }
}

const fetchSignInStatus = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const res = await api.get<{ code: number; data: { signedInToday: boolean } }>('/points/me')
    if (res.code === 200) {
      signedInToday.value = res.data.signedInToday
    }
  } catch {
    // 静默失败
  }
}

const handleLogout = async () => {
  try {
    await api.post('/auth/logout')
  } finally {
    webSocket.disconnect()
    userStore.logout()
    ElMessage.success('已退出登录')
    navigateTo('/')
  }
}

onMounted(async () => {
  await userStore.fetchCurrentUser()
  if (userStore.isLoggedIn) {
    webSocket.connect()
  }
  fetchSignInStatus()
})
</script>

<style scoped>
/* 主题切换过渡效果 */
.forum-shell,
.forum-sidebar,
.forum-main,
.forum-rightbar,
.mobile-header,
.user-card,
.trend-card,
.auth-card {
  transition: background-color 0.3s, color 0.3s, border-color 0.3s;
}

.forum-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 280px minmax(0, 760px) 280px;
  justify-content: center;
  background: var(--bg-color);
  color: var(--text-color);
}

.forum-sidebar {
  position: sticky;
  top: 0;
  height: 100vh;
  padding: 26px 24px;
  border-right: 1px solid var(--border-color);
  background: var(--surface-color);
  display: flex;
  flex-direction: column;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-size: var(--text-xl);
  font-weight: 800;
}

.brand-mark {
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: var(--primary-color);
  color: var(--inverse-text-color);
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
  color: var(--muted-text-color);
  font-weight: 600;
}

.nav-item.active {
  background: var(--active-bg-color);
  color: var(--active-text-color);
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
  background: var(--divider-color);
}

.theme-row,
.theme-row span {
  display: flex;
  align-items: center;
}

.theme-row {
  justify-content: space-between;
  color: var(--muted-text-color);
  font-size: var(--text-sm);
}

.theme-row span {
  gap: 8px;
}

.forum-main {
  min-height: 100vh;
  padding: 28px 24px 60px;
  background: var(--card-color);
}

.forum-rightbar {
  position: sticky;
  top: 0;
  height: 100vh;
  padding: 22px 24px;
  border-left: 1px solid var(--border-color);
  background: var(--surface-color);
}

.user-card,
.trend-card,
.auth-card {
  border: 1px solid var(--soft-border-color);
  border-radius: 8px;
  background: var(--card-color);
}

.user-card {
  padding: 18px;
  text-align: center;
  background: var(--muted-card-color);
}

.spark {
  margin: 0 0 12px;
  font-weight: 800;
}

.welcome {
  margin: 0 0 12px;
  color: var(--subtle-text-color);
  font-size: var(--text-xs);
  line-height: 1.6;
}

.checkin-button {
  width: 100%;
  border-radius: var(--radius-md);
  background: var(--primary-color) !important;
  border-color: var(--primary-color) !important;
  color: var(--inverse-text-color) !important;
  transition: background-color 0.3s, border-color 0.3s, color 0.3s, opacity 0.2s;
}

.checkin-button:hover {
  opacity: 0.9;
}

.checkin-button.is-disabled {
  background: var(--muted-card-color) !important;
  border-color: var(--border-color) !important;
  color: var(--subtle-text-color) !important;
  opacity: 1;
}

.trend-card {
  margin-top: 18px;
  padding: 16px;
}

.trend-card h3 {
  margin: 0 0 12px;
  font-size: var(--text-sm);
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
  font-size: var(--text-xs);
}

.trend-card li span {
  width: 18px;
  height: 18px;
  flex: 0 0 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  background: var(--danger-color);
  color: #fff;
  font-size: var(--text-xs);
}

.trend-card li:nth-child(n + 3) span {
  background: var(--subtle-text-color);
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

/* 侧边栏底部登录/退出区域 */
.sidebar-auth {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}

.sidebar-auth-btn {
  flex: 1;
}

.sidebar-user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  min-width: 0;
  color: var(--muted-text-color);
  font-size: var(--text-sm);
}

.sidebar-user-info span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 右侧栏用户操作区 */
.user-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 18px;
}

/* 移动端头部操作区 */
.mobile-header-actions {
  display: flex;
  align-items: center;
  gap: 6px;
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
    border-bottom: 1px solid var(--border-color);
    background: var(--surface-color);
  }

  .forum-main {
    min-height: calc(100vh - 57px);
    padding: 18px 14px 40px;
  }
}
</style>
