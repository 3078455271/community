<template>
  <div class="create-post">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>{{ draftId ? '编辑草稿' : '发布帖子' }}</h2>
          <el-button @click="openDraftDrawer">草稿箱</el-button>
        </div>
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

        <el-form-item label="标签">
          <el-select
            v-model="form.tags"
            multiple
            filterable
            allow-create
            default-first-option
            :multiple-limit="5"
            placeholder="输入标签后回车，最多 5 个"
            style="width: 100%;"
          >
            <el-option v-for="tag in hotTags" :key="tag.id" :label="tag.name" :value="tag.name" />
          </el-select>
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <div class="editor-box">
            <div class="editor-toolbar">
              <el-radio-group v-model="editorMode" size="small">
                <el-radio-button label="edit">编辑</el-radio-button>
                <el-radio-button label="preview">预览</el-radio-button>
              </el-radio-group>
              <div class="emoji-list">
                <el-button v-for="emoji in emojis" :key="emoji" text size="small" @click="appendEmoji(emoji)">
                  {{ emoji }}
                </el-button>
              </div>
            </div>
            <el-input
              v-if="editorMode === 'edit'"
              v-model="form.content"
              type="textarea"
              :rows="12"
              placeholder="支持 Markdown、代码块、表情和图片粘贴上传，使用 @用户名 可以提醒对方"
              @paste="handlePasteImage"
            />
            <div v-else class="markdown-preview" v-html="renderMarkdown(form.content)"></div>
            <el-progress
              v-if="uploading"
              :percentage="uploadProgress"
              :show-text="false"
              class="upload-progress"
            />
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">发布</el-button>
          <el-button @click="saveDraft" :loading="savingDraft">保存草稿</el-button>
          <el-button @click="navigateTo('/posts')">取消</el-button>
          <span class="autosave-text" v-if="lastSavedAt">已自动保存 {{ formatDate(lastSavedAt) }}</span>
        </el-form-item>
      </el-form>
    </el-card>

    <el-drawer v-model="draftDrawerVisible" title="草稿箱" size="520px">
      <el-table :data="drafts" v-loading="loadingDrafts" style="width: 100%">
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column label="更新时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.updatedAt || row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button text type="primary" size="small" @click="loadDraft(row.id)">继续编辑</el-button>
            <el-button text type="danger" size="small" @click="deleteDraft(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loadingDrafts && drafts.length === 0" description="暂无草稿" />
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import type { FormInstance } from 'element-plus'
import type { CategoryInfo, ApiResponse, PageData, PostInfo, TagInfo } from '~/types'
import { renderMarkdown } from '~/utils/markdown'

const formRef = ref<FormInstance>()
const loading = ref(false)
const savingDraft = ref(false)
const loadingDrafts = ref(false)
const draftDrawerVisible = ref(false)
const api = useApi()
const route = useRoute()
const userStore = useUserStore()
const { smartUpload, uploading, uploadProgress } = useUpload()

const form = reactive({
  title: '',
  content: '',
  categoryId: null as number | null,
  tags: [] as string[],
})

const categories = ref<CategoryInfo[]>([])
const hotTags = ref<TagInfo[]>([])
const drafts = ref<PostInfo[]>([])
const draftId = ref<number | null>(route.query.draftId ? Number(route.query.draftId) : null)
const lastSavedAt = ref<Date | null>(null)
const editorMode = ref<'edit' | 'preview'>('edit')
const emojis = ['😀', '👍', '🎉', '❤️', '🔥', '😂', '👏', '💡']
let autosaveTimer: ReturnType<typeof setTimeout> | null = null

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

const fetchHotTags = async () => {
  try {
    const res = await api.get<ApiResponse<TagInfo[]>>('/tags/hot', { limit: 20 })
    if (res.code === 200) {
      hotTags.value = res.data
    }
  } catch (error) {
    console.error('获取热门标签失败:', error)
  }
}

const fetchDrafts = async () => {
  if (!userStore.isLoggedIn) return

  loadingDrafts.value = true
  try {
    const res = await api.get<ApiResponse<PageData<PostInfo>>>('/posts/drafts', { page: 1, size: 50 })
    if (res.code === 200) {
      drafts.value = res.data.records
    }
  } catch (error) {
    console.error('获取草稿失败:', error)
  } finally {
    loadingDrafts.value = false
  }
}

const fetchDraftDetail = async (id: number) => {
  try {
    const res = await api.get<ApiResponse<PostInfo>>(`/posts/drafts/${id}`)
    if (res.code === 200) {
      form.title = res.data.title
      form.content = res.data.content
      form.categoryId = res.data.categoryId || null
      form.tags = res.data.tags?.map(tag => tag.name) || []
      draftId.value = id
    } else {
      ElMessage.error(res.message || '草稿不存在')
    }
  } catch (error) {
    ElMessage.error('获取草稿失败')
  }
}

const openDraftDrawer = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    navigateTo('/login')
    return
  }
  draftDrawerVisible.value = true
  fetchDrafts()
}

const loadDraft = async (id: number) => {
  await fetchDraftDetail(id)
  draftDrawerVisible.value = false
}

const hasDraftContent = () => {
  return Boolean(form.title.trim() || form.content.trim() || form.categoryId)
}

const saveDraft = async (silent = false) => {
  if (!userStore.isLoggedIn) {
    if (!silent) {
      ElMessage.warning('请先登录')
      navigateTo('/login')
    }
    return
  }
  if (!hasDraftContent()) {
    if (!silent) ElMessage.warning('请输入草稿内容')
    return
  }

  savingDraft.value = true
  try {
    const data = {
      title: form.title,
      content: form.content,
      categoryId: form.categoryId,
      tags: form.tags
    }
    const res = draftId.value
      ? await api.put<ApiResponse<string>>(`/posts/drafts/${draftId.value}`, data)
      : await api.post<ApiResponse<number>>('/posts/drafts', data)
    if (res.code === 200) {
      if (!draftId.value && typeof res.data === 'number') {
        draftId.value = res.data
      }
      lastSavedAt.value = new Date()
      if (!silent) ElMessage.success('草稿已保存')
    } else if (!silent) {
      ElMessage.error(res.message || '保存草稿失败')
    }
  } catch (error) {
    if (!silent) ElMessage.error('保存草稿失败')
  } finally {
    savingDraft.value = false
  }
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  loading.value = true
  try {
    const res = draftId.value
      ? await api.post<ApiResponse<number>>(`/posts/drafts/${draftId.value}/publish`, form)
      : await api.post<ApiResponse<number>>('/posts', form)
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

const deleteDraft = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定删除这篇草稿吗？', '提示', { type: 'warning' })
    const res = await api.delete<ApiResponse<string>>(`/posts/${id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      if (draftId.value === id) {
        draftId.value = null
      }
      fetchDrafts()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch {
    // 用户取消
  }
}

const formatDate = (date: string | Date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const appendEmoji = (emoji: string) => {
  form.content = `${form.content}${emoji}`
}

const handlePasteImage = async (event: ClipboardEvent) => {
  const file = Array.from(event.clipboardData?.files || []).find(item => item.type.startsWith('image/'))
  if (!file) return

  event.preventDefault()
  const url = await smartUpload(file)
  if (url) {
    form.content = `${form.content}\n![图片](${url})\n`
    ElMessage.success('图片已上传')
  }
}

const scheduleAutosave = () => {
  if (autosaveTimer) {
    clearTimeout(autosaveTimer)
  }
  autosaveTimer = setTimeout(() => {
    saveDraft(true)
  }, 15000)
}

watch(form, () => {
  if (hasDraftContent()) {
    scheduleAutosave()
  }
}, { deep: true })

onMounted(async () => {
  fetchCategories()
  fetchHotTags()
  if (draftId.value) {
    await fetchDraftDetail(draftId.value)
  }
})

onBeforeUnmount(() => {
  if (autosaveTimer) {
    clearTimeout(autosaveTimer)
  }
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

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.autosave-text {
  margin-left: 12px;
  color: #909399;
  font-size: 13px;
}

.editor-box {
  width: 100%;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.emoji-list {
  display: flex;
  flex-wrap: wrap;
  gap: 2px;
}

.markdown-preview {
  min-height: 280px;
  padding: 12px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  line-height: 1.8;
}

.markdown-preview :deep(pre) {
  padding: 12px;
  overflow: auto;
  background: #1f2937;
  border-radius: 6px;
}

.markdown-preview :deep(code) {
  padding: 2px 5px;
  background: #f3f4f6;
  border-radius: 4px;
}

.markdown-preview :deep(pre code) {
  padding: 0;
  color: #f9fafb;
  background: transparent;
}

.markdown-preview :deep(img) {
  max-width: 100%;
  border-radius: 6px;
}

.upload-progress {
  margin-top: 8px;
}
</style>
