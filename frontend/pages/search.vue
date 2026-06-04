<template>
  <section class="search-page">
    <header class="search-header">
      <div>
        <p class="eyebrow">SEARCH</p>
        <h1>搜索结果：{{ keyword }}</h1>
      </div>
      <span>共 {{ total }} 条结果</span>
    </header>

    <div class="post-list" v-loading="loading">
      <PostCard v-for="post in posts" :key="post.id" :post="post" />
      <el-empty v-if="!loading && posts.length === 0" description="未找到相关帖子" />
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
import type { ApiResponse, PageData, PostInfo } from '~/types'

const route = useRoute()
const api = useApi()

const keyword = computed(() => (route.query.q as string) || '')
const posts = ref<PostInfo[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const fetchPosts = async () => {
  if (!keyword.value) return

  loading.value = true
  try {
    const res = await api.get<ApiResponse<PageData<PostInfo>>>('/posts/search', {
      page: page.value,
      size: size.value,
      keyword: keyword.value,
    })
    if (res.code === 200) {
      posts.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('搜索失败:', error)
  } finally {
    loading.value = false
  }
}

watch(keyword, () => {
  page.value = 1
  fetchPosts()
})

onMounted(fetchPosts)
</script>

<style scoped>
.search-page {
  display: grid;
  gap: 22px;
}

.search-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  padding-bottom: 18px;
  border-bottom: 1px solid #eef0f4;
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
  font-size: 24px;
}

.search-header span {
  color: #98a0ae;
  font-size: 14px;
}

.post-list {
  display: grid;
  gap: 16px;
  min-height: 220px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 640px) {
  .search-header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
