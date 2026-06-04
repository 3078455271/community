<template>
  <section class="create-post-page">
    <header class="create-header">
      <el-button class="back-button" :icon="ArrowLeft" circle @click="navigateTo('/posts')" />
      <div>
        <h1>{{ draftId ? '编辑草稿' : '发布新话题' }}</h1>
        <p>分享你的见解、代码或新鲜事</p>
      </div>
      <el-button class="refresh-button" :icon="Refresh" circle @click="openDraftDrawer" />
    </header>

    <el-form ref="formRef" :model="form" :rules="rules" class="publish-card">
      <div class="editor-pane">
        <div class="section-label">选择发布板块</div>
        <div class="category-chips">
          <button
            v-for="cat in categories"
            :key="cat.id"
            type="button"
            :class="{ active: form.categoryId === cat.id }"
            @click="form.categoryId = cat.id"
          >
            # {{ cat.name }}
          </button>
        </div>
        <el-form-item prop="categoryId" class="hidden-error">
          <span class="hidden-input">{{ form.categoryId }}</span>
        </el-form-item>

        <el-form-item prop="title">
          <el-input
            v-model="form.title"
            class="title-input"
            maxlength="200"
            placeholder="输入一个吸引人的标题..."
            show-word-limit
          />
        </el-form-item>

        <div class="editor-toolbar">
          <div class="tool-icons">
            <el-button text :icon="EditPen" @click="appendMarkdown('**', '**')" />
            <el-button text :icon="Connection" @click="appendMarkdown('[链接](', ')')" />
            <el-button text :icon="Picture" @click="appendMarkdown('![图片](', ')')" />
            <el-button text @click="appendMarkdown('```\\n', '\\n```')">Code</el-button>
          </div>
          <el-segmented v-model="editorMode" :options="editorOptions" size="small" />
        </div>

        <el-form-item prop="content">
          <el-input
            v-if="editorMode === 'edit'"
            v-model="form.content"
            class="content-input"
            type="textarea"
            :rows="13"
            placeholder="暂无正文内容... 支持 Markdown、代码块、表情和图片粘贴上传，使用 @用户名 可以提醒对方"
            @paste="handlePasteImage"
          />
          <div v-else class="markdown-preview" v-html="previewHtml"></div>
        </el-form-item>

        <el-progress
          v-if="uploading"
          :percentage="uploadProgress"
          :show-text="false"
          class="upload-progress"
        />

        <div class="tag-block">
          <div class="section-label">添加标签（按回车确认）</div>
          <el-select
            v-model="form.tags"
            multiple
            filterable
            allow-create
            default-first-option
            :multiple-limit="5"
            placeholder="+ 添加标签..."
          >
            <el-option v-for="tag in hotTags" :key="tag.id" :label="tag.name" :value="tag.name" />
          </el-select>
          <span class="autosave-text" v-if="lastSavedAt">已自动保存 {{ formatDate(lastSavedAt) }}</span>
        </div>
      </div>

      <aside class="settings-pane">
        <div>
          <h2>发布设置</h2>
          <div class="setting-row">
            <div>
              <strong>开启评论</strong>
              <span>允许其他用户回复</span>
            </div>
            <el-switch v-model="form.commentEnabled" />
          </div>
          <div class="setting-row">
            <div>
              <strong>仅粉丝可见</strong>
              <span>对游客及非粉丝隐藏</span>
            </div>
            <el-switch v-model="followersOnly" />
          </div>
        </div>

        <div class="publish-actions">
          <el-button class="draft-button" :loading="savingDraft" @click="saveDraft()">保存为草稿</el-button>
          <el-button class="submit-button" type="primary" :loading="loading" @click="handleSubmit">
            <el-icon><Promotion /></el-icon>
            发布帖子
          </el-button>
        </div>
      </aside>
    </el-form>

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
  </section>
</template>

<script setup lang="ts">
import { ArrowLeft, Connection, EditPen, Picture, Promotion, Refresh } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import type { ApiResponse, CategoryInfo, PageData, PostInfo, TagInfo } from '~/types'
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
  commentEnabled: true,
  visibility: 'PUBLIC' as 'PUBLIC' | 'FOLLOWERS',
})

const categories = ref<CategoryInfo[]>([])
const hotTags = ref<TagInfo[]>([])
const drafts = ref<PostInfo[]>([])
const draftId = ref<number | null>(route.query.draftId ? Number(route.query.draftId) : null)
const lastSavedAt = ref<Date | null>(null)
const editorMode = ref<'edit' | 'preview'>('edit')
const editorOptions = [
  { label: '编辑', value: 'edit' },
  { label: '预览', value: 'preview' },
]
let autosaveTimer: ReturnType<typeof setTimeout> | null = null

const followersOnly = computed({
  get: () => form.visibility === 'FOLLOWERS',
  set: (value: boolean) => {
    form.visibility = value ? 'FOLLOWERS' : 'PUBLIC'
  },
})
const previewHtml = computed(() => renderMarkdown(form.content || '暂无正文内容...'))

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
      if (!form.categoryId && res.data.length > 0) {
        form.categoryId = res.data[0].id
      }
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
      form.commentEnabled = res.data.commentEnabled ?? true
      form.visibility = res.data.visibility || 'PUBLIC'
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

const postPayload = () => ({
  title: form.title,
  content: form.content,
  categoryId: form.categoryId,
  tags: form.tags,
  commentEnabled: form.commentEnabled,
  visibility: form.visibility,
})

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
    const res = draftId.value
      ? await api.put<ApiResponse<string>>(`/posts/drafts/${draftId.value}`, postPayload())
      : await api.post<ApiResponse<number>>('/posts/drafts', postPayload())
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
      ? await api.post<ApiResponse<number>>(`/posts/drafts/${draftId.value}/publish`, postPayload())
      : await api.post<ApiResponse<number>>('/posts', postPayload())
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
      if (draftId.value === id) draftId.value = null
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

const appendMarkdown = (prefix: string, suffix: string) => {
  form.content = `${form.content}${prefix}${suffix}`
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
  if (autosaveTimer) clearTimeout(autosaveTimer)
  autosaveTimer = setTimeout(() => {
    saveDraft(true)
  }, 15000)
}

watch(form, () => {
  if (hasDraftContent()) scheduleAutosave()
}, { deep: true })

onMounted(async () => {
  userStore.loadFromStorage()
  fetchCategories()
  fetchHotTags()
  if (draftId.value) {
    await fetchDraftDetail(draftId.value)
  }
})

onBeforeUnmount(() => {
  if (autosaveTimer) clearTimeout(autosaveTimer)
})
</script>

<style scoped>
.create-post-page {
  max-width: 980px;
  margin: 0 auto;
}

.create-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 26px;
}

.create-header h1 {
  margin: 0;
  font-size: 24px;
}

.create-header p {
  margin: 4px 0 0;
  color: #98a0ae;
}

.back-button,
.refresh-button {
  border-color: #e5e8ef;
}

.refresh-button {
  margin-left: auto;
}

.publish-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 260px;
  border: 1px solid #e2e5eb;
  border-radius: 16px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 24px 48px rgba(18, 24, 38, 0.12);
}

.editor-pane {
  padding: 28px 30px;
}

.settings-pane {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 28px;
  border-left: 1px solid #edf0f5;
  background: #fff;
}

.section-label {
  margin-bottom: 10px;
  color: #7a8290;
  font-size: 13px;
  font-weight: 700;
}

.category-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 18px;
}

.category-chips button {
  border: 0;
  padding: 9px 14px;
  border-radius: 999px;
  background: #f1f3f7;
  color: #121826;
  cursor: pointer;
}

.category-chips button.active {
  background: #2f6df6;
  color: #fff;
  font-weight: 700;
}

.hidden-error {
  margin: -14px 0 0;
}

.hidden-input {
  display: none;
}

.title-input :deep(.el-input__wrapper) {
  box-shadow: none;
  border-bottom: 1px solid #dfe3ea;
  border-radius: 0;
  padding: 0;
}

.title-input :deep(.el-input__inner) {
  height: 54px;
  font-size: 22px;
  font-weight: 800;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #edf0f5;
}

.tool-icons {
  display: flex;
  align-items: center;
  gap: 4px;
}

.content-input :deep(.el-textarea__inner) {
  min-height: 320px !important;
  border: 0;
  box-shadow: none;
  resize: vertical;
  padding: 20px 0;
  font-size: 15px;
  line-height: 1.8;
}

.markdown-preview {
  min-height: 320px;
  padding: 20px 0;
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
  margin-bottom: 14px;
}

.tag-block {
  padding-top: 18px;
  border-top: 1px solid #edf0f5;
}

.tag-block :deep(.el-select) {
  width: 100%;
}

.tag-block :deep(.el-select__wrapper) {
  min-height: 42px;
  border-radius: 12px;
}

.autosave-text {
  display: inline-block;
  margin-top: 10px;
  color: #98a0ae;
  font-size: 13px;
}

.settings-pane h2 {
  margin: 0 0 22px;
  color: #7a8290;
  font-size: 14px;
}

.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
}

.setting-row div {
  display: grid;
  gap: 4px;
}

.setting-row strong {
  font-size: 15px;
}

.setting-row span {
  color: #98a0ae;
  font-size: 12px;
}

.publish-actions {
  display: grid;
  gap: 12px;
}

.draft-button,
.submit-button {
  width: 100%;
  height: 44px;
  border-radius: 10px;
  font-weight: 800;
}

@media (max-width: 900px) {
  .publish-card {
    grid-template-columns: 1fr;
  }

  .settings-pane {
    border-top: 1px solid #edf0f5;
    border-left: 0;
  }
}

@media (max-width: 640px) {
  .editor-pane,
  .settings-pane {
    padding: 20px 16px;
  }

  .create-header h1 {
    font-size: 20px;
  }

  .editor-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
