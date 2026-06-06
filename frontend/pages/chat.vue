<template>
  <div class="chat-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>私信</span>
          <el-button @click="fetchSessions">刷新</el-button>
        </div>
      </template>

      <div class="chat-layout">
        <aside class="session-list">
          <div
            v-for="session in sessions"
            :key="session.userId"
            class="session-item"
            :class="{ active: currentTargetId === session.userId }"
            @click="openSession(session)"
          >
            <el-avatar :size="36" :src="session.avatar">
              {{ (session.nickname || session.username)?.charAt(0) }}
            </el-avatar>
            <div class="session-main">
              <div class="session-name">
                <span>{{ session.nickname || session.username }}</span>
                <el-badge v-if="session.unreadCount > 0" :value="session.unreadCount" />
              </div>
              <div class="session-message">{{ session.lastMessage }}</div>
            </div>
          </div>
          <el-empty v-if="sessions.length === 0" description="暂无私信" />
        </aside>

        <section class="message-panel">
          <template v-if="currentTargetId">
            <div class="message-list" v-loading="loadingMessages">
              <div
                v-for="message in messages"
                :key="message.id"
                class="message-item"
                :class="{ mine: message.senderId === userStore.userInfo?.id }"
              >
                <div class="message-bubble">{{ message.content }}</div>
                <div class="message-time">{{ formatDate(message.createdAt) }}</div>
              </div>
            </div>
            <div class="message-input">
              <el-input
                v-model="messageContent"
                type="textarea"
                :rows="3"
                placeholder="输入私信内容"
              />
              <el-button type="primary" :loading="sending" @click="sendMessage">发送</el-button>
            </div>
          </template>
          <el-empty v-else description="选择一个会话开始聊天" />
        </section>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

import type { ApiResponse, ChatMessageInfo, ChatSessionInfo, PageData } from '~/types'

const api = useApi()
const userStore = useUserStore()
const webSocket = useWebSocket()
const route = useRoute()

const sessions = ref<ChatSessionInfo[]>([])
const messages = ref<ChatMessageInfo[]>([])
const currentTargetId = ref<number | null>(null)
const messageContent = ref('')
const loadingMessages = ref(false)
const sending = ref(false)

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const fetchSessions = async () => {
  const res = await api.get<ApiResponse<ChatSessionInfo[]>>('/chat/sessions')
  if (res.code === 200) {
    sessions.value = res.data
  }
}

const openTargetFromQuery = async () => {
  const targetUserId = route.query.targetUserId ? Number(route.query.targetUserId) : null
  if (!targetUserId) return

  currentTargetId.value = targetUserId
  if (!sessions.value.some(session => session.userId === targetUserId)) {
    try {
      const res = await api.get<ApiResponse<{ id: number; username: string; nickname?: string; avatar?: string }>>(`/user/${targetUserId}`)
      if (res.code === 200) {
        sessions.value.unshift({
          userId: res.data.id,
          username: res.data.username,
          nickname: res.data.nickname,
          avatar: res.data.avatar,
          lastMessage: '',
          unreadCount: 0,
          lastMessageAt: ''
        })
      }
    } catch (error) {
      console.error('获取私信对象失败:', error)
    }
  }
  await fetchMessages()
}

const fetchMessages = async () => {
  if (!currentTargetId.value) return
  loadingMessages.value = true
  try {
    const res = await api.get<ApiResponse<PageData<ChatMessageInfo>>>(`/chat/messages/${currentTargetId.value}`, {
      page: 1,
      size: 50
    })
    if (res.code === 200) {
      messages.value = res.data.records
      await api.put<ApiResponse<string>>(`/chat/messages/${currentTargetId.value}/read`)
      fetchSessions()
    }
  } finally {
    loadingMessages.value = false
  }
}

const openSession = async (session: ChatSessionInfo) => {
  currentTargetId.value = session.userId
  await fetchMessages()
}

const upsertSession = (session: ChatSessionInfo) => {
  const index = sessions.value.findIndex(item => item.userId === session.userId)
  if (index >= 0) {
    sessions.value.splice(index, 1)
  }
  sessions.value.unshift(session)
}

const appendMessage = (message: ChatMessageInfo) => {
  if (!currentTargetId.value) return
  const belongsToCurrentSession = message.senderId === currentTargetId.value || message.receiverId === currentTargetId.value
  if (!belongsToCurrentSession || messages.value.some(item => item.id === message.id)) return
  messages.value.push(message)
}

const sendMessage = async () => {
  if (!currentTargetId.value || !messageContent.value.trim()) {
    ElMessage.warning('请输入私信内容')
    return
  }
  sending.value = true
  try {
    const res = await api.post<ApiResponse<number>>('/chat/messages', {
      receiverId: currentTargetId.value,
      content: messageContent.value
    })
    if (res.code === 200) {
      messageContent.value = ''
      await fetchMessages()
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } finally {
    sending.value = false
  }
}

onMounted(() => {
  fetchSessions().then(openTargetFromQuery)
  webSocket.connect()

  const unsubscribeMessage = webSocket.subscribe<ChatMessageInfo>('chat.message.created', appendMessage)
  const unsubscribeSession = webSocket.subscribe<ChatSessionInfo>('chat.session.updated', upsertSession)
  const unsubscribeRead = webSocket.subscribe<{ targetUserId: number }>('chat.message.read', (payload) => {
    if (currentTargetId.value !== payload.targetUserId) return
    messages.value.forEach(message => {
      if (message.receiverId === userStore.userInfo?.id) {
        message.isRead = true
      }
    })
  })
  const unsubscribeConnected = webSocket.subscribe<{ reconnected: boolean }>('websocket.connected', async (payload) => {
    if (!payload.reconnected) return
    await fetchSessions()
    if (currentTargetId.value) {
      await fetchMessages()
    }
  })

  onBeforeUnmount(() => {
    unsubscribeMessage()
    unsubscribeSession()
    unsubscribeRead()
    unsubscribeConnected()
  })
})
</script>

<style scoped>
.chat-page {
  max-width: 1100px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  min-height: 560px;
}

.session-list {
  border-right: 1px solid #eee;
  padding-right: 12px;
}

.session-item {
  display: flex;
  gap: 10px;
  padding: 10px;
  border-radius: 6px;
  cursor: pointer;
}

.session-item.active,
.session-item:hover {
  background: #f5f7fa;
}

.session-main {
  flex: 1;
  min-width: 0;
}

.session-name {
  display: flex;
  justify-content: space-between;
  font-weight: 500;
}

.session-message {
  color: #909399;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-panel {
  padding-left: 16px;
  display: flex;
  flex-direction: column;
}

.message-list {
  flex: 1;
  min-height: 420px;
  overflow-y: auto;
}

.message-item {
  margin-bottom: 12px;
}

.message-item.mine {
  text-align: right;
}

.message-bubble {
  display: inline-block;
  max-width: 70%;
  padding: 8px 12px;
  border-radius: 6px;
  background: #f5f7fa;
  text-align: left;
}

.message-item.mine .message-bubble {
  background: #ecf5ff;
  color: #1f5fbf;
}

.message-time {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

.message-input {
  display: flex;
  gap: 10px;
  align-items: flex-end;
  border-top: 1px solid #eee;
  padding-top: 12px;
}
</style>
