<template>
  <el-popover placement="bottom" :width="360" trigger="click">
    <template #reference>
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
        <el-icon :size="20" class="notification-icon"><Bell /></el-icon>
      </el-badge>
    </template>

    <div class="notification-panel">
      <div class="notification-header">
        <span>通知</span>
        <el-button text size="small" @click="markAllAsRead" v-if="unreadCount > 0">
          全部已读
        </el-button>
      </div>

      <div class="notification-list" v-loading="loading">
        <div
          v-for="item in notifications"
          :key="item.id"
          class="notification-item"
          :class="{ unread: !item.isRead }"
          @click="handleClick(item)"
        >
          <div class="notification-content">{{ item.content }}</div>
          <div class="notification-time">{{ formatTime(item.createdAt) }}</div>
        </div>

        <el-empty v-if="!loading && notifications.length === 0" description="暂无通知" :image-size="60" />
      </div>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { Bell } from '@element-plus/icons-vue'
import type { NotificationInfo, ApiResponse, PageData } from '~/types'

const api = useApi()
const userStore = useUserStore()

const notifications = ref<NotificationInfo[]>([])
const unreadCount = ref(0)
const loading = ref(false)

const formatTime = (date: string) => {
  if (!date) return ''
  const now = new Date()
  const target = new Date(date)
  const diff = now.getTime() - target.getTime()

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  return target.toLocaleDateString('zh-CN')
}

const fetchNotifications = async () => {
  if (!userStore.isLoggedIn) return

  loading.value = true
  try {
    const res = await api.get<ApiResponse<PageData<NotificationInfo>>>('/notifications', {
      params: { page: 1, size: 20 }
    })
    if (res.code === 200) {
      notifications.value = res.data.records
    }
  } catch (error) {
    console.error('获取通知失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchUnreadCount = async () => {
  if (!userStore.isLoggedIn) return

  try {
    const res = await api.get<ApiResponse<number>>('/notifications/unread-count')
    if (res.code === 200) {
      unreadCount.value = res.data
    }
  } catch (error) {
    console.error('获取未读数量失败:', error)
  }
}

const markAllAsRead = async () => {
  try {
    const res = await api.put<ApiResponse<string>>('/notifications/read-all')
    if (res.code === 200) {
      unreadCount.value = 0
      notifications.value.forEach(n => n.isRead = true)
    }
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

const handleClick = async (item: NotificationInfo) => {
  if (!item.isRead) {
    try {
      await api.put<ApiResponse<string>>(`/notifications/${item.id}/read`)
      item.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }

  // 跳转到相关帖子
  if (item.targetId) {
    navigateTo(`/posts/${item.targetId}`)
  }
}

onMounted(() => {
  fetchNotifications()
  fetchUnreadCount()
})

// 暴露刷新方法
defineExpose({
  refresh: fetchUnreadCount
})
</script>

<style scoped>
.notification-badge {
  cursor: pointer;
}

.notification-icon {
  color: #666;
}

.notification-panel {
  max-height: 400px;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
  margin-bottom: 10px;
  font-weight: 500;
}

.notification-list {
  max-height: 320px;
  overflow-y: auto;
}

.notification-item {
  padding: 10px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.notification-item:hover {
  background-color: #f5f5f5;
}

.notification-item.unread {
  background-color: #f0f7ff;
}

.notification-content {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
}

.notification-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>