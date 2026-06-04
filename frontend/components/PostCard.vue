<template>
  <article class="post-card" @click="navigateTo(`/posts/${post.id}`)">
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

    <h2 class="post-title">
      <span v-if="post.status === 2">📌</span>
      <span v-if="post.essence">🔥</span>
      {{ post.title }}
    </h2>

    <p class="post-excerpt">{{ excerpt }}</p>

    <div class="post-tags" v-if="post.tags?.length">
      <span v-for="tag in post.tags" :key="tag.id">#{{ tag.name }}</span>
    </div>

    <footer class="post-stats">
      <span>
        <el-icon><Star /></el-icon>
        {{ post.likeCount }}
      </span>
      <span>
        <el-icon><ChatDotRound /></el-icon>
        {{ post.commentCount }}
      </span>
      <span>
        <el-icon><View /></el-icon>
        {{ formatCount(post.viewCount) }}
      </span>
      <span v-if="post.visibility === 'FOLLOWERS'" class="visibility">仅粉丝</span>
      <span v-if="post.commentEnabled === false" class="visibility">评论关闭</span>
    </footer>
  </article>
</template>

<script setup lang="ts">
import { ChatDotRound, Star, View } from '@element-plus/icons-vue'
import type { PostInfo } from '~/types'

const props = defineProps<{
  post: PostInfo
}>()

const excerpt = computed(() => {
  const text = (props.post.content || '')
    .replace(/!\[[^\]]*]\([^)]*\)/g, '')
    .replace(/[#*_`>~\-[\]()]/g, '')
    .replace(/\s+/g, ' ')
    .trim()
  return text || '这个话题还没有摘要，点进去看看完整内容。'
})

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
</script>

<style scoped>
.post-card {
  padding: 22px;
  border: 1px solid #e7eaf0;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
}

.post-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 28px rgba(18, 24, 38, 0.08);
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
  color: #98a0ae;
  font-size: 13px;
}

.author-line {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #121826;
}

.owner-badge,
.category-chip,
.visibility {
  border-radius: 6px;
  font-size: 12px;
  font-weight: 700;
}

.owner-badge {
  color: #98a0ae;
}

.category-chip {
  padding: 6px 10px;
  background: #eff5ff;
  color: #2563eb;
}

.post-title {
  margin: 18px 0 10px;
  font-size: 20px;
  line-height: 1.35;
  font-weight: 800;
}

.post-excerpt {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 0;
  color: #5d6472;
  line-height: 1.7;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
  color: #2563eb;
  font-size: 13px;
}

.post-stats {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px solid #f0f2f5;
  color: #98a0ae;
  font-size: 13px;
}

.post-stats span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.visibility {
  padding: 3px 8px;
  background: #f4f6fa;
  color: #5d6472;
}

@media (max-width: 640px) {
  .post-card {
    padding: 16px;
  }

  .post-title {
    font-size: 17px;
  }

  .category-chip {
    display: none;
  }
}
</style>
