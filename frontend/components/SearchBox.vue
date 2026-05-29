<template>
  <div class="search-box">
    <el-input
      v-model="keyword"
      placeholder="搜索帖子..."
      clearable
      @keyup.enter="handleSearch"
    >
      <template #append>
        <el-button :icon="Search" @click="handleSearch" />
      </template>
    </el-input>
  </div>
</template>

<script setup lang="ts">
import { Search } from '@element-plus/icons-vue'

const keyword = ref('')
const router = useRouter()

const handleSearch = () => {
  if (keyword.value.trim()) {
    router.push({ path: '/search', query: { q: keyword.value.trim() } })
  }
}
</script>

<style scoped>
.search-box {
  width: 300px;
}
</style>