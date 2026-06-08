<template>
  <section class="feed-page">
    <header class="home-hero">
      <div class="hero-copy">
        <p class="eyebrow">COMMUNITY WORKSPACE</p>
        <h1>把高质量讨论沉淀成社区资产</h1>
        <p class="hero-text">追踪最新话题、热门反馈和关注流，让内容发现更稳定、更高效。</p>
        <div class="hero-actions">
          <SearchBox />
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            发布帖子
          </el-button>
        </div>
      </div>

      <div class="hero-panel">
        <div v-for="item in heroStats" :key="item.label" class="metric-item">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <small>{{ item.caption }}</small>
        </div>
      </div>
    </header>

    <div class="feed-toolbar">
      <div>
        <h2>社区信息流</h2>
        <p>{{ feedSubtitle }}</p>
      </div>
      <el-segmented v-model="feedType" :options="tabs" />
    </div>

    <div class="feed-list" v-loading="loading">
      <template v-if="loading && visiblePosts.length === 0">
        <div v-for="item in 3" :key="item" class="post-skeleton">
          <span></span>
          <strong></strong>
          <p></p>
          <p></p>
        </div>
      </template>
      <PostCard v-for="post in visiblePosts" :key="post.id" :post="post" />
      <el-empty v-if="!loading && visiblePosts.length === 0" description="暂无话题" />
    </div>
  </section>
</template>

<script setup lang="ts">
import { Plus } from '@element-plus/icons-vue'
import type { ApiResponse, PageData, PostInfo } from '~/types'

const api = useApi()
const userStore = useUserStore()
const posts = ref<PostInfo[]>([])
const loading = ref(false)
const feedType = ref<'latest' | 'hot' | 'following'>('latest')

const tabs = [
  { label: '最新', value: 'latest' },
  { label: '热门', value: 'hot' },
  { label: '关注', value: 'following' },
] as const

const currentTab = computed(() => tabs.find(tab => tab.value === feedType.value)?.label || '最新')
const totalViews = computed(() => posts.value.reduce((sum, post) => sum + post.viewCount, 0))
const totalInteractions = computed(() => posts.value.reduce((sum, post) => sum + post.likeCount + post.commentCount, 0))
const heroStats = computed(() => [
  { label: '内容曝光', value: formatCount(totalViews.value), caption: '当前信息流' },
  { label: '互动总量', value: formatCount(totalInteractions.value), caption: '点赞与评论' },
  { label: '精选内容', value: posts.value.filter(post => post.essence).length.toString(), caption: currentTab.value },
])

const visiblePosts = computed(() => {
  if (feedType.value !== 'hot') return posts.value
  return [...posts.value].sort((left, right) => right.viewCount + right.likeCount - left.viewCount - left.likeCount)
})
const feedSubtitle = computed(() => `${currentTab.value}视图 · ${visiblePosts.value.length} 条讨论`)

const formatCount = (count: number) => {
  if (count >= 10000) {
    return `${(count / 10000).toFixed(1)}w`
  }
  if (count >= 1000) {
    return `${(count / 1000).toFixed(1)}k`
  }
  return count.toString()
}

const fetchPosts = async () => {
  if (feedType.value === 'following' && !userStore.isLoggedIn) {
    posts.value = []
    ElMessage.warning('请先登录后查看关注流')
    navigateTo('/login')
    return
  }

  loading.value = true
  try {
    const url = feedType.value === 'following' ? '/posts/following' : '/posts'
    const res = await api.get<ApiResponse<PageData<PostInfo>>>(url, { page: 1, size: 12 })
    if (res.code === 200) {
      posts.value = res.data.records
    }
  } catch (error) {
    console.error('获取帖子失败:', error)
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    navigateTo('/login')
    return
  }
  navigateTo('/posts/create')
}

watch(feedType, fetchPosts)

onMounted(() => {
  userStore.fetchCurrentUser()
  fetchPosts()
})
</script>

<style scoped>
.feed-page {
  display: grid;
  gap: 22px;
}

.home-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 260px;
  gap: 18px;
  min-width: 0;
  padding: 24px;
  border: 1px solid var(--soft-border-color);
  border-radius: 8px;
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--primary-soft-color) 72%, transparent), transparent 56%),
    var(--card-color);
  box-shadow: var(--shadow-sm);
  animation: hero-enter var(--motion-slow) var(--ease-out) both;
}

.hero-copy {
  min-width: 0;
}

.eyebrow {
  margin: 0 0 8px;
  color: var(--primary-color);
  font-size: var(--text-xs);
  font-weight: 900;
  letter-spacing: 0.08em;
}

.hero-copy h1 {
  max-width: min(520px, 100%);
  margin: 0;
  color: var(--text-color);
  font-size: var(--text-3xl);
  line-height: 1.16;
  overflow-wrap: anywhere;
  word-break: break-all;
}

.hero-text {
  max-width: 540px;
  margin: 12px 0 0;
  color: var(--muted-text-color);
  line-height: 1.8;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.hero-actions {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) auto;
  gap: 12px;
  max-width: 560px;
  margin-top: 20px;
}

.hero-actions :deep(.search-box) {
  width: 100%;
}

.hero-panel {
  display: grid;
  gap: 10px;
}

.metric-item {
  display: grid;
  gap: 2px;
  padding: 14px;
  border: 1px solid var(--soft-border-color);
  border-radius: 8px;
  background: color-mix(in srgb, var(--card-color) 88%, transparent);
}

.metric-item span,
.metric-item small {
  color: var(--subtle-text-color);
  font-size: var(--text-xs);
}

.metric-item strong {
  color: var(--text-color);
  font-size: var(--text-2xl);
  line-height: 1.05;
}

.feed-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-width: 0;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--soft-border-color);
}

.feed-toolbar h2 {
  margin: 0;
  font-size: var(--text-xl);
}

.feed-toolbar p {
  margin: 4px 0 0;
  color: var(--subtle-text-color);
  font-size: var(--text-sm);
}

.feed-list {
  display: grid;
  gap: 14px;
  min-height: 240px;
}

.feed-list :deep(.post-card) {
  animation: card-enter var(--motion-slow) var(--ease-out) both;
}

.feed-list :deep(.post-card:nth-child(2)) {
  animation-delay: 45ms;
}

.feed-list :deep(.post-card:nth-child(3)) {
  animation-delay: 90ms;
}

.feed-list :deep(.post-card:nth-child(n + 4)) {
  animation-delay: 120ms;
}

.post-skeleton {
  display: grid;
  gap: 12px;
  padding: 22px;
  border: 1px solid var(--soft-border-color);
  border-radius: 8px;
  background: var(--card-color);
}

.post-skeleton span,
.post-skeleton strong,
.post-skeleton p {
  display: block;
  margin: 0;
  border-radius: var(--radius-full);
  background: linear-gradient(
    90deg,
    var(--muted-card-color),
    color-mix(in srgb, var(--primary-soft-color) 55%, var(--muted-card-color)),
    var(--muted-card-color)
  );
  background-size: 220% 100%;
  animation: skeleton-wave 1.2s var(--ease-standard) infinite;
}

.post-skeleton span {
  width: 180px;
  height: 16px;
}

.post-skeleton strong {
  width: 72%;
  height: 24px;
}

.post-skeleton p {
  width: 100%;
  height: 12px;
}

.post-skeleton p:last-child {
  width: 58%;
}

@keyframes hero-enter {
  from {
    opacity: 0;
    transform: translateY(14px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes card-enter {
  from {
    opacity: 0;
    transform: translateY(12px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes skeleton-wave {
  from {
    background-position: 120% 0;
  }

  to {
    background-position: -120% 0;
  }
}

@media (max-width: 760px) {
  .home-hero {
    grid-template-columns: 1fr;
    padding: 18px;
  }

  .hero-copy h1 {
    font-size: var(--text-2xl);
  }

  .hero-actions,
  .feed-toolbar {
    grid-template-columns: 1fr;
  }

  .hero-actions {
    display: grid;
  }

  .feed-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .feed-toolbar :deep(.el-segmented),
  .feed-toolbar :deep(.el-segmented__group) {
    width: 100%;
  }

  .feed-toolbar :deep(.el-segmented__item) {
    flex: 1;
    min-width: 0;
  }

  .feed-toolbar :deep(.el-segmented__item-label) {
    overflow: hidden;
    text-overflow: ellipsis;
  }
}
</style>
