<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="header-content">
        <NuxtLink to="/" class="logo">
          <h1>Community Zhuge</h1>
        </NuxtLink>
        <el-menu mode="horizontal" :router="true" class="nav-menu" :default-active="route.path">
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/posts">帖子</el-menu-item>
        </el-menu>
        <SearchBox />
        <div class="user-actions">
          <template v-if="userStore.isLoggedIn">
            <NotificationBell />
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                  {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
                </el-avatar>
                <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" @click="navigateTo('/login')">登录</el-button>
            <el-button @click="navigateTo('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </el-header>

    <el-main class="main-content">
      <slot />
    </el-main>

    <el-footer class="footer">
      <p>&copy; 2024 Community Zhuge. All rights reserved.</p>
    </el-footer>
  </el-container>
</template>

<script setup lang="ts">
const route = useRoute()
const userStore = useUserStore()

onMounted(() => {
  userStore.loadFromStorage()
})

const handleCommand = (command: string) => {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    navigateTo('/')
  } else if (command === 'profile') {
    navigateTo('/user/profile')
  }
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 20px;
  gap: 20px;
}

.logo {
  margin-right: 20px;
}

.logo h1 {
  font-size: 20px;
  color: var(--primary-color);
  margin: 0;
}

.nav-menu {
  flex: 1;
  border-bottom: none;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
  color: #333;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  width: 100%;
}

.footer {
  text-align: center;
  color: #666;
  background: #fff;
  border-top: 1px solid #eee;
}
</style>