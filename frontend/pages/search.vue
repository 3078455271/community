<template>
  <div class="search-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>搜索结果：{{ keyword }}</span>
          <span class="result-count">共 {{ total }} 条结果</span>
        </div>
      </template>

      <div v-loading="loading">
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
    </el-card>
  </div>
</template>

<script setup lang="ts">
import type { PostInfo, ApiResponse, PageData } from '~/types'

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
      params: {
        page: page.value,
        size: size.value,
        keyword: keyword.value
      }
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

onMounted(() => {
  fetchPosts()
})
</script>

<style scoped>
.search-page {
  max-width: 900px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-count {
  font-size: 14px;
  color: #999;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>