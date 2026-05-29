<template>
  <div class="comment-item">
    <div class="comment-main">
      <UserAvatar
        :avatar="comment.avatar"
        :nickname="comment.nickname"
        :username="comment.username"
        :size="36"
      />
      <div class="comment-body">
        <div class="comment-header">
          <span class="comment-author">{{ comment.nickname || comment.username }}</span>
          <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
        </div>
        <div class="comment-content">{{ comment.content }}</div>
        <div class="comment-actions">
          <slot name="actions" :comment="comment" />
        </div>
      </div>
    </div>

    <!-- 子评论 -->
    <div v-if="comment.children?.length" class="sub-comments">
      <div v-for="child in comment.children" :key="child.id" class="sub-comment-item">
        <div class="comment-main">
          <UserAvatar
            :avatar="child.avatar"
            :nickname="child.nickname"
            :username="child.username"
            :size="32"
          />
          <div class="comment-body">
            <div class="comment-header">
              <span class="comment-author">{{ child.nickname || child.username }}</span>
              <span class="comment-time">{{ formatDate(child.createdAt) }}</span>
            </div>
            <div class="comment-content">{{ child.content }}</div>
            <div class="comment-actions">
              <slot name="actions" :comment="child" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { CommentInfo } from '~/types'

defineProps<{
  comment: CommentInfo
}>()

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}
</script>

<style scoped>
.comment-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-main {
  display: flex;
  gap: 12px;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.comment-time {
  color: #999;
  font-size: 12px;
}

.comment-content {
  color: #555;
  line-height: 1.6;
  font-size: 14px;
}

.comment-actions {
  margin-top: 8px;
}

.sub-comments {
  margin-left: 48px;
  background: #f9f9f9;
  border-radius: 4px;
  padding: 0 12px;
}

.sub-comment-item {
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.sub-comment-item:last-child {
  border-bottom: none;
}
</style>