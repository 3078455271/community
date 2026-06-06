<template>
  <section class="notifications-page">
    <header class="page-header">
      <div>
        <p class="eyebrow">NOTIFICATIONS</p>
        <h1>通知中心</h1>
      </div>
      <div class="header-actions">
        <el-button @click="fetchNotifications">刷新</el-button>
        <el-button type="primary" :disabled="unreadCount === 0" @click="markAllAsRead">
          全部已读
        </el-button>
      </div>
    </header>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>全部通知</span>
          <el-tag v-if="unreadCount > 0" type="danger">{{ unreadCount }} 条未读</el-tag>
          <el-tag v-else type="success">暂无未读</el-tag>
        </div>
      </template>

      <div class="notification-list" v-loading="loading">
        <article
          v-for="item in notifications"
          :key="item.id"
          class="notification-item"
          :class="{ unread: !item.isRead, clickable: canNavigate(item) }"
          @click="handleClick(item)"
        >
          <div class="notification-main">
            <div class="notification-title-row">
              <el-tag size="small" :type="typeMeta(item.type).tagType">
                {{ typeMeta(item.type).label }}
              </el-tag>
              <span v-if="!item.isRead" class="unread-dot"></span>
            </div>
            <p class="notification-content">{{ item.content }}</p>
            <p class="notification-time">{{ formatTime(item.createdAt) }}</p>
          </div>
          <el-button v-if="!item.isRead" text size="small" @click.stop="markAsRead(item)">
            标为已读
          </el-button>
        </article>

        <el-empty v-if="!loading && notifications.length === 0" description="暂无通知" />
      </div>

      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchNotifications"
          @size-change="fetchNotifications"
        />
      </div>
    </el-card>
  </section>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

import type { ApiResponse, NotificationInfo, PageData } from '~/types'

type NotificationTagType = 'primary' | 'success' | 'warning' | 'danger' | 'info'

const api = useApi()
const webSocket = useWebSocket()

const notifications = ref<NotificationInfo[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const unreadCount = ref(0)

const typeMeta = (type: string): { label: string; tagType: NotificationTagType } => {
  const meta: Record<string, { label: string; tagType: NotificationTagType }> = {
    LIKE: { label: '点赞', tagType: 'danger' },
    COMMENT: { label: '评论', tagType: 'primary' },
    FOLLOW: { label: '关注', tagType: 'success' },
    MENTION: { label: '@提及', tagType: 'warning' },
    SYSTEM: { label: '系统', tagType: 'info' },
  }
  return meta[type] ?? { label: '通知', tagType: 'info' }
}

const formatTime = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const canNavigate = (item: NotificationInfo) => {
  return Boolean(item.targetId && ['LIKE', 'COMMENT', 'MENTION'].includes(item.type))
}

const fetchUnreadCount = async () => {
  const res = await api.get<ApiResponse<number>>('/notifications/unread-count')
  if (res.code === 200) {
    unreadCount.value = res.data
  }
}

const fetchNotifications = async () => {
  loading.value = true
  try {
    const res = await api.get<ApiResponse<PageData<NotificationInfo>>>('/notifications', {
      page: page.value,
      size: size.value,
    })
    if (res.code === 200) {
      notifications.value = res.data.records
      total.value = res.data.total
    }
    await fetchUnreadCount()
  } finally {
    loading.value = false
  }
}

const markAsRead = async (item: NotificationInfo) => {
  if (item.isRead) return
  const res = await api.put<ApiResponse<string>>(`/notifications/${item.id}/read`)
  if (res.code === 200) {
    item.isRead = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
}

const markAllAsRead = async () => {
  const res = await api.put<ApiResponse<string>>('/notifications/read-all')
  if (res.code === 200) {
    notifications.value.forEach(item => item.isRead = true)
    unreadCount.value = 0
    ElMessage.success('已全部标为已读')
  }
}

const handleClick = async (item: NotificationInfo) => {
  await markAsRead(item)
  if (canNavigate(item)) {
    navigateTo(`/posts/${item.targetId}`)
  }
}

const prependNotification = (notification: NotificationInfo) => {
  if (notifications.value.some(item => item.id === notification.id)) return
  notifications.value.unshift(notification)
  total.value += 1
}

onMounted(() => {
  fetchNotifications()
  webSocket.connect()

  const unsubscribeCreated = webSocket.subscribe<NotificationInfo>('notification.created', (notification) => {
    prependNotification(notification)
  })
  const unsubscribeUnreadCount = webSocket.subscribe<{ count: number }>('notification.unreadCount', (payload) => {
    unreadCount.value = payload.count
  })
  const unsubscribeRead = webSocket.subscribe<number>('notification.read', (id) => {
    const notification = notifications.value.find(item => item.id === id)
    if (notification) notification.isRead = true
  })
  const unsubscribeReadAll = webSocket.subscribe('notification.readAll', () => {
    notifications.value.forEach(item => item.isRead = true)
    unreadCount.value = 0
  })
  const unsubscribeConnected = webSocket.subscribe<{ reconnected: boolean }>('websocket.connected', async (payload) => {
    if (payload.reconnected) await fetchNotifications()
  })

  onBeforeUnmount(() => {
    unsubscribeCreated()
    unsubscribeUnreadCount()
    unsubscribeRead()
    unsubscribeReadAll()
    unsubscribeConnected()
  })
})
</script>

<style scoped>
.notifications-page {
  display: grid;
  gap: 22px;
}

.page-header,
.card-header,
.header-actions,
.notification-item,
.notification-title-row,
.pagination {
  display: flex;
  align-items: center;
}

.page-header,
.card-header,
.notification-item {
  justify-content: space-between;
}

.header-actions {
  gap: 10px;
}

.eyebrow {
  margin: 0 0 6px;
  color: #98a0ae;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
}

h1 {
  margin: 0;
  font-size: 26px;
}

.notification-list {
  min-height: 260px;
}

.notification-item {
  gap: 16px;
  padding: 16px;
  border-bottom: 1px solid #edf0f5;
}

.notification-item:last-child {
  border-bottom: 0;
}

.notification-item.unread {
  background: #f6faff;
}

.notification-item.clickable {
  cursor: pointer;
}

.notification-main {
  flex: 1;
  min-width: 0;
}

.notification-title-row {
  gap: 8px;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f56c6c;
}

.notification-content {
  margin: 10px 0 6px;
  color: #303133;
  line-height: 1.6;
}

.notification-time {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.pagination {
  justify-content: flex-end;
  margin-top: 18px;
}

@media (max-width: 720px) {
  .page-header {
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
  }
}
</style>