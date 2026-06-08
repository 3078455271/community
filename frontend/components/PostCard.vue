<template>
  <article
    class="post-card"
    role="button"
    tabindex="0"
    @click="goPost"
    @keydown.enter="goPost"
    @keydown.space.prevent="goPost"
  >
    <span class="card-accent"></span>
    <header class="post-top">
      <UserAvatar :avatar="post.avatar" :nickname="post.nickname" :username="post.username" :size="42" />
      <div class="author-block">
        <div class="author-line">
          <strong>{{ post.nickname || post.username }}</strong>
          <span v-if="post.userId" class="owner-badge">楼主</span>
        </div>
        <span>{{ formatDate(post.createdAt) }}</span>
      </div>
      <span class="category-chip"># {{ post.categoryName || '技术交流' }}</span>
    </header>

    <div class="title-block">
      <div v-if="post.status === 2 || post.essence" class="status-pills">
        <span v-if="post.status === 2" class="is-pinned">置顶</span>
        <span v-if="post.essence" class="is-essence">精选</span>
      </div>
      <h2 class="post-title">{{ post.title }}</h2>
    </div>

    <p class="post-excerpt">{{ excerpt }}</p>

    <div class="post-tags" v-if="post.tags?.length">
      <span v-for="tag in post.tags" :key="tag.id">#{{ tag.name }}</span>
    </div>

    <footer class="post-stats">
      <div class="stats-group">
        <span class="stat-item">
          <el-icon><Star /></el-icon>
          {{ post.likeCount }}
        </span>
        <span class="stat-item">
          <el-icon><ChatDotRound /></el-icon>
          {{ post.commentCount }}
        </span>
        <span class="stat-item">
          <el-icon><View /></el-icon>
          {{ formatCount(post.viewCount) }}
        </span>
        <span v-if="post.visibility === 'FOLLOWERS'" class="visibility">仅粉丝</span>
        <span v-if="post.commentEnabled === false" class="visibility">评论关闭</span>
      </div>
      <div class="post-actions">
        <el-button
          size="small"
          :type="favorited ? 'primary' : 'default'"
          :loading="favoriteLoading"
          @click.stop="handleFavorite"
        >
          <el-icon><Star /></el-icon>
          {{ favorited ? '已收藏' : '收藏' }}
        </el-button>
        <el-button size="small" type="primary" @click.stop="goPost">
          <el-icon><View /></el-icon>
          查看帖子
        </el-button>
      </div>
    </footer>
  </article>
</template>

<script setup lang="ts">
import { ChatDotRound, Star, View } from '@element-plus/icons-vue'
import type { ApiResponse, FavoriteStatus, PostInfo } from '~/types'

const props = defineProps<{
  post: PostInfo
}>()

const api = useApi()
const userStore = useUserStore()
const favorited = ref(false)
const favoriteLoading = ref(false)

const excerpt = computed(() => {
  const text = (props.post.content || '')
    .replace(/!\[[^\]]*]\([^)]*\)/g, '')
    .replace(/[#*_`>~\-[\]()]/g, '')
    .replace(/\s+/g, ' ')
    .trim()
  return text || '这个话题还没有摘要，点进去看看完整内容。'
})

const goPost = () => {
  navigateTo(`/posts/${props.post.id}`)
}

const fetchFavoriteStatus = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const res = await api.get<ApiResponse<FavoriteStatus>>(`/favorites/posts/${props.post.id}/status`)
    if (res.code === 200) {
      favorited.value = res.data.favorited
    }
  } catch (error) {
    console.error('获取收藏状态失败:', error)
  }
}

const handleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后收藏帖子')
    navigateTo('/login')
    return
  }
  if (favoriteLoading.value) return

  favoriteLoading.value = true
  try {
    const res = favorited.value
      ? await api.delete<ApiResponse<string>>(`/favorites/posts/${props.post.id}`)
      : await api.post<ApiResponse<string>>(`/favorites/posts/${props.post.id}`, {})
    if (res.code === 200) {
      favorited.value = !favorited.value
      ElMessage.success(favorited.value ? '收藏成功' : '已取消收藏')
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    favoriteLoading.value = false
  }
}

const formatDate = (date: string) => {
  if (!date) return ''
  const target = new Date(date)
  const diff = Date.now() - target.getTime()
  if (diff > 0 && diff < 24 * 60 * 60 * 1000) {
    const hours = Math.max(1, Math.floor(diff / 1000 / 60 / 60))
    return `${hours}小时前`
  }
  return target.toLocaleDateString('zh-CN')
}

const formatCount = (count: number) => {
  if (count >= 1000) {
    return `${(count / 1000).toFixed(1)}k`
  }
  return count
}

onMounted(fetchFavoriteStatus)
</script>

<style scoped>
.post-card {
  position: relative;
  overflow: hidden;
  padding: 22px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: var(--card-color);
  cursor: pointer;
  box-shadow: 0 1px 0 rgba(18, 24, 38, 0.02);
  transition:
    box-shadow var(--motion-base) var(--ease-out),
    transform var(--motion-base) var(--ease-out),
    background-color var(--motion-base) var(--ease-standard),
    border-color var(--motion-base) var(--ease-standard);
}

.post-card:hover {
  transform: translateY(-3px);
  border-color: color-mix(in srgb, var(--primary-color) 38%, var(--border-color));
  box-shadow: var(--shadow-lg);
}

.post-card:active {
  transform: translateY(-1px);
}

.card-accent {
  position: absolute;
  inset: 0 auto 0 0;
  width: 3px;
  background: linear-gradient(180deg, var(--primary-color), var(--accent-color));
  opacity: 0;
  transform: scaleY(0.4);
  transform-origin: top;
  transition: opacity var(--motion-base) var(--ease-out), transform var(--motion-base) var(--ease-out);
}

.post-card:hover .card-accent,
.post-card:focus-visible .card-accent {
  opacity: 1;
  transform: scaleY(1);
}

.post-top {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-block {
  min-width: 0;
  flex: 1;
  display: grid;
  gap: 3px;
  color: var(--subtle-text-color);
  font-size: var(--text-xs);
}

.author-line {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-color);
  min-width: 0;
}

.author-line strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.owner-badge,
.category-chip,
.visibility,
.status-pills span {
  border-radius: var(--radius-sm);
  font-size: var(--text-xs);
  font-weight: 700;
}

.owner-badge {
  color: var(--subtle-text-color);
}

.category-chip {
  padding: 6px 10px;
  background: color-mix(in srgb, var(--primary-color) 12%, transparent);
  color: var(--primary-color);
}

.title-block {
  display: grid;
  gap: 8px;
  margin: 18px 0 10px;
}

.status-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.status-pills .is-pinned {
  padding: 3px 8px;
  background: var(--highlight-color);
  color: var(--warning-color);
}

.status-pills .is-essence {
  padding: 3px 8px;
  background: var(--accent-soft-color);
  color: var(--accent-color);
}

.post-title {
  margin: 0;
  font-size: var(--text-xl);
  line-height: 1.35;
  font-weight: 800;
  color: var(--text-color);
}

.post-excerpt {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 0;
  color: var(--muted-text-color);
  line-height: 1.7;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
  color: var(--active-text-color);
  font-size: var(--text-xs);
}

.post-tags span {
  padding: 4px 8px;
  border-radius: var(--radius-full);
  background: var(--muted-card-color);
  transition: background-color var(--motion-fast) var(--ease-standard), color var(--motion-fast) var(--ease-standard);
}

.post-card:hover .post-tags span {
  background: var(--primary-soft-color);
  color: var(--primary-color);
}

.post-stats {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px solid var(--soft-border-color);
  color: var(--subtle-text-color);
  font-size: var(--text-xs);
}

.stats-group,
.post-actions {
  display: flex;
  align-items: center;
}

.stats-group {
  flex-wrap: wrap;
  gap: 18px;
  min-width: 0;
}

.post-actions {
  flex: 0 0 auto;
  gap: 8px;
}

.stat-item,
.visibility {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.stat-item {
  min-width: 54px;
  padding: 4px 0;
  transition: color var(--motion-fast) var(--ease-standard), transform var(--motion-fast) var(--ease-out);
}

.post-card:hover .stat-item {
  color: var(--primary-color);
  transform: translateY(-1px);
}

.visibility {
  padding: 3px 8px;
  background: var(--muted-card-color);
  color: var(--muted-text-color);
}

.post-actions :deep(.el-button) {
  min-width: auto;
  border-radius: var(--radius-sm);
}

@media (max-width: 640px) {
  .post-card {
    padding: 16px;
  }

  .post-title {
    font-size: var(--text-lg);
  }

  .category-chip {
    display: none;
  }

  .post-stats {
    align-items: stretch;
    flex-direction: column;
  }

  .post-actions {
    display: grid;
    grid-template-columns: 1fr 1fr;
  }
}
</style>
