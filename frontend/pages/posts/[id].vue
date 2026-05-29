<template>
  <div class="post-detail" v-loading="loading">
    <el-card v-if="post">
      <template #header>
        <div class="post-header">
          <h1>{{ post.title }}</h1>
          <div class="post-meta">
            <span class="author">
              <el-icon><User /></el-icon>
              {{ post.nickname || post.username }}
            </span>
            <span class="category">
              <el-tag size="small">{{ post.categoryName }}</el-tag>
            </span>
            <span class="time">
              <el-icon><Timer /></el-icon>
              {{ formatDate(post.createdAt) }}
            </span>
            <span class="views">
              <el-icon><View /></el-icon>
              {{ post.viewCount }} 浏览
            </span>
          </div>
        </div>
      </template>

      <div class="post-content" v-html="post.content"></div>

      <div class="post-actions">
        <el-button :type="isLiked ? 'primary' : 'default'" @click="handleLike">
          <el-icon><Star /></el-icon>
          {{ isLiked ? '已点赞' : '点赞' }} ({{ post.likeCount }})
        </el-button>
      </div>
    </el-card>

    <!-- 评论区 -->
    <el-card class="comment-section" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>评论 ({{ comments.length }})</span>
        </div>
      </template>

      <!-- 发表评论 -->
      <div class="comment-form">
        <el-input
          v-model="commentContent"
          type="textarea"
          :rows="3"
          placeholder="写下你的评论..."
        />
        <el-button type="primary" @click="submitComment" :loading="submitting" style="margin-top: 10px;">
          发表评论
        </el-button>
      </div>

      <!-- 评论列表 -->
      <div class="comment-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-header">
            <span class="comment-author">{{ comment.nickname || comment.username }}</span>
            <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
          </div>
          <div class="comment-content">{{ comment.content }}</div>
          <div class="comment-actions">
            <el-button text size="small" @click="replyTo(comment)">回复</el-button>
          </div>

          <!-- 子评论 -->
          <div v-if="comment.children?.length" class="sub-comments">
            <div v-for="child in comment.children" :key="child.id" class="comment-item sub">
              <div class="comment-header">
                <span class="comment-author">{{ child.nickname || child.username }}</span>
                <span class="comment-time">{{ formatDate(child.createdAt) }}</span>
              </div>
              <div class="comment-content">{{ child.content }}</div>
            </div>
          </div>
        </div>

        <el-empty v-if="comments.length === 0" description="暂无评论" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { User, Timer, View, Star } from '@element-plus/icons-vue'
import type { PostInfo, CommentInfo } from '~/types'

const route = useRoute()
const api = useApi()
const userStore = useUserStore()

const post = ref<PostInfo | null>(null)
const comments = ref<CommentInfo[]>([])
const loading = ref(true)
const commentContent = ref('')
const submitting = ref(false)
const replyingTo = ref<CommentInfo | null>(null)
const isLiked = ref(false)

const postId = route.params.id

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const fetchPost = async () => {
  try {
    const res = await api.get<{ code: number; data: PostInfo }>(`/posts/${postId}`)
    if (res.code === 200) {
      post.value = res.data
    }
  } catch (error) {
    console.error('获取帖子失败:', error)
  }
}

const fetchComments = async () => {
  try {
    const res = await api.get<{ code: number; data: CommentInfo[] }>(`/posts/${postId}/comments`)
    if (res.code === 200) {
      comments.value = res.data
    }
  } catch (error) {
    console.error('获取评论失败:', error)
  }
}

const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    navigateTo('/login')
    return
  }

  try {
    if (isLiked.value) {
      const res = await api.delete<{ code: number; message: string }>(`/posts/${postId}/like`)
      if (res.code === 200) {
        isLiked.value = false
        if (post.value) {
          post.value.likeCount = Math.max(0, post.value.likeCount - 1)
        }
        ElMessage.success('取消点赞成功')
      }
    } else {
      const res = await api.post<{ code: number; message: string }>(`/posts/${postId}/like`)
      if (res.code === 200) {
        isLiked.value = true
        if (post.value) {
          post.value.likeCount++
        }
        ElMessage.success('点赞成功')
      }
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const replyTo = (comment: CommentInfo) => {
  replyingTo.value = comment
  commentContent.value = `@${comment.nickname || comment.username} `
}

const submitComment = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    navigateTo('/login')
    return
  }

  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  submitting.value = true
  try {
    const data: { content: string; parentId?: number } = { content: commentContent.value }
    if (replyingTo.value) {
      data.parentId = replyingTo.value.id
    }

    const res = await api.post<{ code: number; message: string }>(`/posts/${postId}/comments`, data)
    if (res.code === 200) {
      ElMessage.success('评论成功')
      commentContent.value = ''
      replyingTo.value = null
      fetchComments()
    } else {
      ElMessage.error(res.message || '评论失败')
    }
  } catch (error: unknown) {
    const err = error as Error
    ElMessage.error(err.message || '评论失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchPost(), fetchComments()])
  loading.value = false
})
</script>

<style scoped>
.post-header h1 {
  margin: 0 0 15px 0;
  font-size: 24px;
}

.post-meta {
  display: flex;
  gap: 20px;
  color: #666;
  font-size: 14px;
}

.post-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.post-content {
  line-height: 1.8;
  font-size: 16px;
  min-height: 200px;
}

.post-actions {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.comment-section {
  margin-top: 20px;
}

.comment-form {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.comment-item {
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-item.sub {
  padding-left: 40px;
  border-bottom: none;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 500;
  color: #333;
}

.comment-time {
  color: #999;
  font-size: 13px;
}

.comment-content {
  color: #555;
  line-height: 1.6;
}

.comment-actions {
  margin-top: 8px;
}

.sub-comments {
  background: #f9f9f9;
  border-radius: 4px;
  margin-top: 10px;
  padding: 0 10px;
}
</style>