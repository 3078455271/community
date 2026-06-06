<template>
  <div class="admin-page">
    <div class="admin-header">
      <h2>运营管理</h2>
      <span>用户、内容审核、分类、敏感词与审计日志</span>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane v-if="isAdmin" label="用户管理" name="users">
        <div class="toolbar">
          <el-input v-model="userKeyword" placeholder="搜索用户名/昵称" clearable @keyup.enter="fetchUsers" />
          <el-button type="primary" @click="fetchUsers">搜索</el-button>
        </div>
        <el-table :data="users" v-loading="loadingUsers">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="username" label="用户名" width="140" />
          <el-table-column prop="nickname" label="昵称" width="140" />
          <el-table-column prop="role" label="角色" width="130">
            <template #default="{ row }">
              <el-select v-model="row.role" size="small" @change="updateRole(row)">
                <el-option label="普通用户" value="USER" />
                <el-option label="版主" value="MODERATOR" />
                <el-option label="管理员" value="ADMIN" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                {{ row.status === 1 ? '正常' : '封禁' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="禁言至" min-width="180">
            <template #default="{ row }">{{ formatDate(row.mutedUntil) || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="260">
            <template #default="{ row }">
              <el-button text type="danger" @click="toggleBan(row)">
                {{ row.status === 1 ? '封禁' : '解封' }}
              </el-button>
              <el-button text type="warning" @click="muteUser(row.id)">禁言</el-button>
              <el-button text @click="unmuteUser(row.id)">解禁言</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="举报处理" name="reports">
        <div class="toolbar">
          <el-select v-model="reportStatus" clearable placeholder="全部状态" @change="fetchReports">
            <el-option label="待处理" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已驳回" :value="2" />
          </el-select>
          <el-button @click="fetchReports">刷新</el-button>
        </div>
        <el-table :data="reports" v-loading="loadingReports">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="reporterName" label="举报人" width="120" />
          <el-table-column prop="targetType" label="对象" width="100" />
          <el-table-column prop="targetId" label="对象ID" width="90" />
          <el-table-column prop="reason" label="原因" min-width="220" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="reportTagType(row.status)">{{ reportStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button v-if="row.status === 0" text type="primary" @click="handleReport(row.id, 1)">通过</el-button>
              <el-button v-if="row.status === 0" text type="danger" @click="handleReport(row.id, 2)">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="分类管理" name="categories">
        <div class="toolbar">
          <el-input v-model="categoryForm.name" placeholder="分类名称" />
          <el-input v-model="categoryForm.description" placeholder="描述" />
          <el-input-number v-model="categoryForm.sort" :min="0" />
          <el-button type="primary" @click="saveCategory">{{ categoryForm.id ? '保存' : '新增' }}</el-button>
        </div>
        <el-table :data="categories" v-loading="loadingCategories">
          <el-table-column prop="name" label="名称" width="160" />
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="sort" label="排序" width="90" />
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-button text type="primary" @click="editCategory(row)">编辑</el-button>
              <el-button text type="danger" @click="deleteCategory(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="敏感词" name="words">
        <div class="toolbar">
          <el-input v-model="wordInput" placeholder="新增敏感词" @keyup.enter="createWord" />
          <el-button type="primary" @click="createWord">新增</el-button>
        </div>
        <el-table :data="words" v-loading="loadingWords">
          <el-table-column prop="word" label="敏感词" />
          <el-table-column label="创建时间" width="180">
            <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button text type="danger" @click="deleteWord(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane v-if="isAdmin" label="审计日志" name="logs">
        <el-table :data="logs" v-loading="loadingLogs">
          <el-table-column prop="operatorName" label="操作人" width="130" />
          <el-table-column prop="action" label="动作" width="180" />
          <el-table-column prop="targetType" label="对象" width="120" />
          <el-table-column prop="targetId" label="对象ID" width="90" />
          <el-table-column prop="detail" label="详情" />
          <el-table-column label="时间" width="180">
            <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'admin' })

import type {
  ApiResponse,
  AuditLogInfo,
  CategoryInfo,
  PageData,
  ReportInfo,
  SensitiveWordInfo,
  UserInfo
} from '~/types'

type AdminUser = UserInfo & { status: number; role: 'USER' | 'MODERATOR' | 'ADMIN'; mutedUntil?: string }

const api = useApi()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.userInfo?.role === 'ADMIN')
const isManager = computed(() => ['ADMIN', 'MODERATOR'].includes(userStore.userInfo?.role || ''))
const activeTab = ref(isAdmin.value ? 'users' : 'reports')

const users = ref<AdminUser[]>([])
const reports = ref<ReportInfo[]>([])
const categories = ref<CategoryInfo[]>([])
const words = ref<SensitiveWordInfo[]>([])
const logs = ref<AuditLogInfo[]>([])
const userKeyword = ref('')
const reportStatus = ref<number | null>(0)
const wordInput = ref('')
const categoryForm = reactive<{ id?: number; name: string; description: string; sort: number }>({
  name: '',
  description: '',
  sort: 0
})
const loadingUsers = ref(false)
const loadingReports = ref(false)
const loadingCategories = ref(false)
const loadingWords = ref(false)
const loadingLogs = ref(false)

const formatDate = (date?: string) => date ? new Date(date).toLocaleString('zh-CN') : ''
const reportStatusText = (status: number) => ['待处理', '已通过', '已驳回'][status] || '未知'
const reportTagType = (status: number) => status === 0 ? 'warning' : status === 1 ? 'success' : 'info'

const fetchUsers = async () => {
  if (!isAdmin.value) return
  loadingUsers.value = true
  try {
    const res = await api.get<ApiResponse<PageData<AdminUser>>>('/admin/users', {
      page: 1,
      size: 100,
      keyword: userKeyword.value || undefined
    })
    if (res.code === 200) users.value = res.data.records
  } finally {
    loadingUsers.value = false
  }
}

const fetchReports = async () => {
  loadingReports.value = true
  try {
    const params: Record<string, number> = { page: 1, size: 100 }
    if (reportStatus.value !== null) params.status = reportStatus.value
    const res = await api.get<ApiResponse<PageData<ReportInfo>>>('/admin/reports', params)
    if (res.code === 200) reports.value = res.data.records
  } finally {
    loadingReports.value = false
  }
}

const fetchCategories = async () => {
  loadingCategories.value = true
  try {
    const res = await api.get<ApiResponse<PageData<CategoryInfo>>>('/admin/categories', { page: 1, size: 100 })
    if (res.code === 200) categories.value = res.data.records
  } finally {
    loadingCategories.value = false
  }
}

const fetchWords = async () => {
  loadingWords.value = true
  try {
    const res = await api.get<ApiResponse<PageData<SensitiveWordInfo>>>('/admin/sensitive-words', { page: 1, size: 100 })
    if (res.code === 200) words.value = res.data.records
  } finally {
    loadingWords.value = false
  }
}

const fetchLogs = async () => {
  if (!isAdmin.value) return
  loadingLogs.value = true
  try {
    const res = await api.get<ApiResponse<PageData<AuditLogInfo>>>('/admin/audit-logs', { page: 1, size: 100 })
    if (res.code === 200) logs.value = res.data.records
  } finally {
    loadingLogs.value = false
  }
}

const updateRole = async (row: AdminUser) => {
  const res = await api.put<ApiResponse<string>>(`/admin/users/${row.id}/role`, { role: row.role })
  res.code === 200 ? ElMessage.success('角色已更新') : ElMessage.error(res.message)
}

const toggleBan = async (row: AdminUser) => {
  const res = row.status === 1
    ? await api.put<ApiResponse<string>>(`/admin/users/${row.id}/ban`)
    : await api.delete<ApiResponse<string>>(`/admin/users/${row.id}/ban`)
  if (res.code === 200) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.success('操作成功')
  } else {
    ElMessage.error(res.message)
  }
}

const muteUser = async (id: number) => {
  const { value } = await ElMessageBox.prompt('请输入禁言天数', '禁言用户', {
    inputPattern: /^[1-9]\d{0,2}$/,
    inputErrorMessage: '请输入 1-365 天'
  })
  const res = await api.put<ApiResponse<string>>(`/admin/users/${id}/mute`, { days: Number(value) })
  if (res.code === 200) {
    ElMessage.success('禁言成功')
    fetchUsers()
  } else {
    ElMessage.error(res.message)
  }
}

const unmuteUser = async (id: number) => {
  const res = await api.delete<ApiResponse<string>>(`/admin/users/${id}/mute`)
  if (res.code === 200) {
    ElMessage.success('已解除禁言')
    fetchUsers()
  } else {
    ElMessage.error(res.message)
  }
}

const handleReport = async (id: number, status: number) => {
  const { value } = await ElMessageBox.prompt('处理备注', '举报处理', { inputValue: status === 1 ? '举报成立' : '证据不足' })
  const res = await api.put<ApiResponse<string>>(`/admin/reports/${id}`, { status, remark: value })
  if (res.code === 200) {
    ElMessage.success('处理成功')
    fetchReports()
  } else {
    ElMessage.error(res.message)
  }
}

const saveCategory = async () => {
  const payload = { name: categoryForm.name, description: categoryForm.description, sort: categoryForm.sort }
  const res = categoryForm.id
    ? await api.put<ApiResponse<string>>(`/admin/categories/${categoryForm.id}`, payload)
    : await api.post<ApiResponse<number>>('/admin/categories', payload)
  if (res.code === 200) {
    ElMessage.success('保存成功')
    Object.assign(categoryForm, { id: undefined, name: '', description: '', sort: 0 })
    fetchCategories()
  } else {
    ElMessage.error(res.message)
  }
}

const editCategory = (row: CategoryInfo) => {
  Object.assign(categoryForm, { id: row.id, name: row.name, description: row.description || '', sort: row.sort || 0 })
}

const deleteCategory = async (id: number) => {
  await ElMessageBox.confirm('确定删除该分类吗？', '提示', { type: 'warning' })
  const res = await api.delete<ApiResponse<string>>(`/admin/categories/${id}`)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    fetchCategories()
  } else {
    ElMessage.error(res.message)
  }
}

const createWord = async () => {
  if (!wordInput.value.trim()) return
  const res = await api.post<ApiResponse<number>>('/admin/sensitive-words', { word: wordInput.value.trim() })
  if (res.code === 200) {
    wordInput.value = ''
    ElMessage.success('新增成功')
    fetchWords()
  } else {
    ElMessage.error(res.message)
  }
}

const deleteWord = async (id: number) => {
  const res = await api.delete<ApiResponse<string>>(`/admin/sensitive-words/${id}`)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    fetchWords()
  } else {
    ElMessage.error(res.message)
  }
}

const handleTabChange = () => {
  if (activeTab.value === 'users') fetchUsers()
  if (activeTab.value === 'reports') fetchReports()
  if (activeTab.value === 'categories') fetchCategories()
  if (activeTab.value === 'words') fetchWords()
  if (activeTab.value === 'logs') fetchLogs()
}

onMounted(() => {
  handleTabChange()
})
</script>

<style scoped>
.admin-page {
  max-width: 1200px;
  margin: 0 auto;
}

.admin-header {
  display: flex;
  align-items: baseline;
  gap: 16px;
  margin-bottom: 16px;
}

.admin-header h2 {
  margin: 0;
}

.admin-header span {
  color: #909399;
}

.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.toolbar .el-input {
  max-width: 260px;
}
</style>
