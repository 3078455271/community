<template>
  <div class="create-post">
    <el-card>
      <template #header>
        <h2>发布帖子</h2>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入帖子标题" />
        </el-form-item>

        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="10"
            placeholder="请输入帖子内容"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">发布</el-button>
          <el-button @click="navigateTo('/posts')">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import type { FormInstance } from 'element-plus'
import type { CategoryInfo, ApiResponse } from '~/types'

const formRef = ref<FormInstance>()
const loading = ref(false)
const api = useApi()

const form = reactive({
  title: '',
  content: '',
  categoryId: null as number | null,
})

const categories = ref<CategoryInfo[]>([])

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
}

const fetchCategories = async () => {
  try {
    const res = await api.get<ApiResponse<CategoryInfo[]>>('/categories')
    if (res.code === 200) {
      categories.value = res.data
    }
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  loading.value = true
  try {
    const res = await api.post<ApiResponse<number>>('/posts', form)
    if (res.code === 200) {
      ElMessage.success('发布成功')
      navigateTo(`/posts/${res.data}`)
    } else {
      ElMessage.error(res.message || '发布失败')
    }
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : '发布失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.create-post {
  max-width: 800px;
  margin: 0 auto;
}

h2 {
  margin: 0;
}
</style>