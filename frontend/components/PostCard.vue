<template>
  <div class="post-card" @click="navigateTo(`/posts/${post.id}`)">
    <div class="post-card-header">
      <UserAvatar :avatar="post.avatar" :nickname="post.nickname" :username="post.username" :size="36" />
      <div class="post-meta">
        <span class="author">{{ post.nickname || post.username }}</span>
        <span class="time">{{ formatDate(post.createdAt) }}</span>
      </div>
      <el-tag size="small" class="category-tag">{{ post.categoryName }}</el-tag>
    </div>

    <h3 class="post-title">{{ post.title }}</h3>

    <div class="post-stats">
      <span class="stat-item">
        <el-icon><View /></el-icon>
        {{ post.viewCount }}
      </span>
      <span class="stat-item">
        <el-icon><Star /></el-icon>
        {{ post.likeCount }}
      </span>
      <span class="stat-item">
        <el-icon><ChatDotRound /></el-icon>
        {{ post.commentCount }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { View, Star, ChatDotRound } from '@element-plus/icons-vue'
import type { PostInfo } from '~/types'

const props = defineProps<{
  post: PostInfo
}>()

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}
</script>

<style scoped>
.post-card {
  padding: 16px;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.post-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border-color: var(--primary-color);
}

.post-card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.post-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.author {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.time {
  font-size: 12px;
  color: #999;
}

.category-tag {
  margin-left: auto;
}

.post-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin: 0 0 12px 0;
  line-height: 1.5;
}

.post-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #666;
}
</style>