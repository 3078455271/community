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
            <span class="followers" v-if="showFollowButton">
              {{ followStatus.followerCount }} 粉丝
            </span>
            <el-button
              v-if="showFollowButton"
              size="small"
              :type="followStatus.following ? 'primary' : 'default'"
              :loading="followLoading"
              @click="handleFollow"
            >
              <el-icon><User /></el-icon>
              {{ followStatus.following ? '已关注' : '关注' }}
            </el-button>
          </div>
        </div>
      </template>

      <div class="post-content" v-html="post.content"></div>

      <div class="post-actions">
        <el-button :type="isLiked ? 'primary' : 'default'" @click="handleLike">
          <el-icon><Star /></el-icon>
          {{ isLiked ? '已点赞' : '点赞' }} ({{ post.likeCount }})
        </el-button>
        <el-button :type="favoriteStatus.favorited ? 'primary' : 'default'" :loading="favoriteLoading" @click="handleFavorite">
          <el-icon><Collection /></el-icon>
          {{ favoriteStatus.favorited ? '已收藏' : '收藏' }}
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
          placeholder="写下你的评论，使用 @用户名 可以提醒对方"
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
import { Collection, User, Timer, View, Star } from '@element-plus/icons-vue'
import type { PostInfo, CommentInfo, ApiResponse, FollowStatus, FavoriteStatus } from '~/types'

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
const followLoading = ref(false)
const favoriteLoading = ref(false)
const followStatus = reactive<FollowStatus>({
  following: false,
  followingCount: 0,
  followerCount: 0
})
const favoriteStatus = reactive<FavoriteStatus>({
  favorited: false
})

const postId = route.params.id

const showFollowButton = computed(() => {
  return Boolean(
    post.value
      && userStore.isLoggedIn
      && userStore.userInfo?.id
      && post.value.userId !== userStore.userInfo.id
  )
})

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const fetchPost = async () => {
  try {
    const res = await api.get<{ code: number; data: PostInfo }>(`/posts/${postId}`)
    if (res.code === 200) {
      post.value = res.data
      await Promise.all([fetchFollowStatus(), fetchFavoriteStatus()])
    }
  } catch (error) {
    console.error('获取帖子失败:', error)
  }
}

const fetchFavoriteStatus = async () => {
  if (!post.value || !userStore.isLoggedIn) return

  try {
    const res = await api.get<ApiResponse<FavoriteStatus>>(`/favorites/posts/${post.value.id}/status`)
    if (res.code === 200) {
      favoriteStatus.favorited = res.data.favorited
    }
  } catch (error) {
    console.error('获取收藏状态失败:', error)
  }
}

const fetchFollowStatus = async () => {
  if (!post.value) return

  try {
    const res = await api.get<ApiResponse<FollowStatus>>(`/users/${post.value.userId}/follow-status`)
    if (res.code === 200) {
      followStatus.following = res.data.following
      followStatus.followingCount = res.data.followingCount
      followStatus.followerCount = res.data.followerCount
    }
  } catch (error) {
    console.error('获取关注状态失败:', error)
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

const handleFollow = async () => {
  if (!post.value) return
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    navigateTo('/login')
    return
  }

  followLoading.value = true
  try {
    const res = followStatus.following
      ? await api.delete<ApiResponse<string>>(`/users/${post.value.userId}/follow`)
      : await api.post<ApiResponse<string>>(`/users/${post.value.userId}/follow`)
    if (res.code === 200) {
      followStatus.following = !followStatus.following
      followStatus.followerCount += followStatus.following ? 1 : -1
      ElMessage.success(res.data || res.message)
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    followLoading.value = false
  }
}

const handleFavorite = async () => {
  if (!post.value) return
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    navigateTo('/login')
    return
  }

  favoriteLoading.value = true
  try {
    const res = favoriteStatus.favorited
      ? await api.delete<ApiResponse<string>>(`/favorites/posts/${post.value.id}`)
      : await api.post<ApiResponse<string>>(`/favorites/posts/${post.value.id}`, {})
    if (res.code === 200) {
      favoriteStatus.favorited = !favoriteStatus.favorited
      ElMessage.success(res.data || res.message)
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    favoriteLoading.value = false
  }
}

const replyTo = (comment: CommentInfo) => {
  replyingTo.value = comment
  commentContent.value = `@${comment.username} `
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
