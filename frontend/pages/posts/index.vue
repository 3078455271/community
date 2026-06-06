<template>
  <section class="posts-page">
    <header class="posts-header">
      <div>
        <p class="eyebrow">DISCOVER</p>
        <h1>发现板块</h1>
      </div>
      <el-button type="primary" @click="handleCreate">发布帖子</el-button>
    </header>

    <div class="filters">
      <el-segmented v-model="feedType" :options="feedOptions" />
      <el-select v-model="categoryId" placeholder="全部分类" clearable>
        <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
      </el-select>
      <el-select v-model="tagId" placeholder="热门标签" clearable>
        <el-option v-for="tag in hotTags" :key="tag.id" :label="`#${tag.name}`" :value="tag.id" />
      </el-select>
    </div>

    <div class="post-list" v-loading="loading">
      <PostCard v-for="post in posts" :key="post.id" :post="post" />
      <el-empty v-if="!loading && posts.length === 0" description="暂无帖子" />
    </div>

    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchPosts"
        @size-change="fetchPosts"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import type { ApiResponse, CategoryInfo, PageData, PostInfo, TagInfo } from '~/types'

const api = useApi()
const userStore = useUserStore()
const route = useRoute()

const posts = ref<PostInfo[]>([])
const categories = ref<CategoryInfo[]>([])
const hotTags = ref<TagInfo[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const categoryId = ref<number | null>(null)
const tagId = ref<number | null>(route.query.tagId ? Number(route.query.tagId) : null)
const feedType = ref<'all' | 'following'>('all')
const feedOptions = [
  { label: '全部', value: 'all' },
  { label: '关注', value: 'following' },
]

const fetchCategories = async () => {
  try {
    const res = await api.get<ApiResponse<CategoryInfo[]>>('/categories')
    if (res.code === 200) categories.value = res.data
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const fetchHotTags = async () => {
  try {
    const res = await api.get<ApiResponse<TagInfo[]>>('/tags/hot', { limit: 20 })
    if (res.code === 200) hotTags.value = res.data
  } catch (error) {
    console.error('获取热门标签失败:', error)
  }
}

const fetchPosts = async () => {
  if (feedType.value === 'following' && !userStore.isLoggedIn) {
    posts.value = []
    total.value = 0
    ElMessage.warning('请先登录后查看关注流')
    navigateTo('/login')
    return
  }

  loading.value = true
  try {
    const params: Record<string, number> = { page: page.value, size: size.value }
    if (categoryId.value && feedType.value === 'all') params.categoryId = categoryId.value
    if (tagId.value && feedType.value === 'all') params.tagId = tagId.value
    const url = feedType.value === 'following' ? '/posts/following' : '/posts'
    const res = await api.get<ApiResponse<PageData<PostInfo>>>(url, params)
    if (res.code === 200) {
      posts.value = res.data.records
      total.value = res.data.total
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

watch([categoryId, tagId, feedType], () => {
  page.value = 1
  fetchPosts()
})

onMounted(() => {
  userStore.fetchCurrentUser()
  fetchCategories()
  fetchHotTags()
  fetchPosts()
})
</script>

<style scoped>
.posts-page {
  display: grid;
  gap: 22px;
}

.posts-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
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

.filters {
  display: grid;
  grid-template-columns: auto minmax(160px, 1fr) minmax(160px, 1fr);
  gap: 12px;
  padding: 16px;
  border: 1px solid #edf0f5;
  border-radius: 8px;
  background: #fff;
}

.post-list {
  display: grid;
  gap: 16px;
  min-height: 240px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 720px) {
  .posts-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .filters {
    grid-template-columns: 1fr;
  }

  .pagination {
    justify-content: center;
  }
}
</style>
