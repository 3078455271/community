<template>
  <el-avatar :size="size" :src="avatar" class="user-avatar">
    {{ displayText }}
  </el-avatar>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  avatar?: string
  nickname?: string
  username?: string
  size?: number
}>(), {
  size: 40
})

const displayText = computed(() => {
  if (props.nickname) {
    return props.nickname.charAt(0).toUpperCase()
  }
  if (props.username) {
    return props.username.charAt(0).toUpperCase()
  }
  return 'U'
})
</script>

<style scoped>
.user-avatar {
  background-color: var(--primary-color);
  color: #fff;
  font-weight: 500;
}
</style>