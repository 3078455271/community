<template>
  <div class="home-page">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="welcome-card">
          <template #header>
            <div class="card-header">
              <span>欢迎来到 Community Zhuge</span>
            </div>
          </template>
          <p>这是一个技术交流社区，欢迎分享你的知识和经验。</p>
          <el-button type="primary" @click="navigateTo('/posts')">浏览帖子</el-button>
        </el-card>

        <el-card class="latest-posts" style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>最新帖子</span>
            </div>
          </template>
          <div v-if="posts.length === 0" class="empty-state">
            <el-empty description="暂无帖子" />
          </div>
          <div v-else>
            <div v-for="post in posts" :key="post.id" class="post-item">
              <h3>
                <NuxtLink :to="`/posts/${post.id}`">{{ post.title }}</NuxtLink>
              </h3>
              <p class="post-meta">
                <span>{{ post.createdAt }}</span>
                <span>浏览: {{ post.viewCount }}</span>
              </p>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>热门分类</span>
            </div>
          </template>
          <div class="categories">
            <el-tag v-for="cat in categories" :key="cat.id" class="category-tag">
              {{ cat.name }}
            </el-tag>
          </div>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>社区统计</span>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="注册用户">0</el-descriptions-item>
            <el-descriptions-item label="帖子总数">0</el-descriptions-item>
            <el-descriptions-item label="今日发帖">0</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
const posts = ref([])
const categories = ref([
  { id: 1, name: '技术交流' },
  { id: 2, name: '问答求助' },
  { id: 3, name: '项目展示' },
  { id: 4, name: '资源分享' },
])
</script>

<style scoped>
.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.welcome-card :deep(.el-card__header) {
  border-bottom: none;
}

.welcome-card .card-header span {
  color: #fff;
  font-size: 18px;
}

.post-item {
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.post-item:last-child {
  border-bottom: none;
}

.post-item h3 {
  margin: 0 0 8px 0;
}

.post-item h3 a {
  color: #333;
}

.post-item h3 a:hover {
  color: var(--primary-color);
}

.post-meta {
  color: #999;
  font-size: 14px;
  margin: 0;
}

.post-meta span {
  margin-right: 15px;
}

.categories {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.category-tag {
  cursor: pointer;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>