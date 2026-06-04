<template>
  <section class="feed-page">
    <div class="feed-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        :class="{ active: feedType === tab.value }"
        @click="feedType = tab.value"
      >
        {{ tab.label }}
      </button>
    </div>

    <div class="feed-list" v-loading="loading">
      <PostCard v-for="post in visiblePosts" :key="post.id" :post="post" />
      <el-empty v-if="!loading && visiblePosts.length === 0" description="暂无话题" />
    </div>
  </section>
</template>

<script setup lang="ts">
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

const visiblePosts = computed(() => {
  if (feedType.value !== 'hot') return posts.value
  return [...posts.value].sort((left, right) => right.viewCount + right.likeCount - left.viewCount - left.likeCount)
})

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

watch(feedType, fetchPosts)

onMounted(() => {
  userStore.loadFromStorage()
  fetchPosts()
})
</script>

<style scoped>
.feed-page {
  display: grid;
  gap: 24px;
}

.feed-tabs {
  display: flex;
  gap: 22px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eef0f4;
}

.feed-tabs button {
  border: 0;
  padding: 8px 16px;
  border-radius: 999px;
  background: transparent;
  color: #7a8290;
  font-weight: 700;
  cursor: pointer;
}

.feed-tabs button.active {
  background: #18191f;
  color: #fff;
}

.feed-list {
  display: grid;
  gap: 18px;
  min-height: 240px;
}
</style>
