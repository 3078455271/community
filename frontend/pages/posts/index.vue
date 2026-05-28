<template>
  <div class="posts-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span>帖子列表</span>
            <el-select v-model="categoryId" placeholder="全部分类" clearable style="margin-left: 15px; width: 150px;">
              <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
            </el-select>
          </div>
          <el-button type="primary" @click="handleCreate">发布帖子</el-button>
        </div>
      </template>

      <el-table :data="posts" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="300">
          <template #default="{ row }">
            <NuxtLink :to="`/posts/${row.id}`" class="post-title">{{ row.title }}</NuxtLink>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="nickname" label="作者" width="120">
          <template #default="{ row }">
            {{ row.nickname || row.username }}
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="80" />
        <el-table-column prop="commentCount" label="评论" width="80" />
        <el-table-column prop="createdAt" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>

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
const api = useApi()
const userStore = useUserStore()

const posts = ref([])
const categories = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const categoryId = ref<number | null>(null)

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const fetchCategories = async () => {
  try {
    const res = await api.get('/categories') as any
    if (res.code === 200) {
      categories.value = res.data
    }
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const fetchPosts = async () => {
  loading.value = true
  try {
    const params: any = { page: page.value, size: size.value }
    if (categoryId.value) {
      params.categoryId = categoryId.value
    }
    const res = await api.get('/posts', params) as any
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

watch(categoryId, () => {
  page.value = 1
  fetchPosts()
})

onMounted(() => {
  fetchCategories()
  fetchPosts()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
}

.post-title {
  color: #333;
  font-weight: 500;
}

.post-title:hover {
  color: var(--primary-color);
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>