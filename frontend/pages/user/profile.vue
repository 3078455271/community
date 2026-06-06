<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <!-- 左侧：个人信息 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>个人信息</span>
          </template>

          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="80px"
          >
            <el-form-item label="用户名">
              <el-input :value="userStore.userInfo?.username" disabled />
            </el-form-item>

            <el-form-item label="头像">
              <div class="avatar-section">
                <el-avatar :size="64" :src="profileForm.avatar">
                  {{ profileForm.nickname?.charAt(0) || 'U' }}
                </el-avatar>
                <FileUpload @success="handleAvatarSuccess">
                  <el-button size="small" style="margin-top: 8px;">更换头像</el-button>
                </FileUpload>
              </div>
            </el-form-item>

            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="updateProfile" :loading="updating">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card style="margin-top: 20px;">
          <template #header>
            <span>积分等级</span>
          </template>
          <div class="point-panel" v-loading="loadingPoints">
            <div class="point-row">
              <span>等级</span>
              <strong>Lv.{{ pointInfo?.level || 1 }}</strong>
            </div>
            <div class="point-row">
              <span>积分</span>
              <strong>{{ pointInfo?.points || 0 }}</strong>
            </div>
            <div class="point-row">
              <span>签到天数</span>
              <strong>{{ pointInfo?.signInDays || 0 }}</strong>
            </div>
            <el-button
              type="primary"
              :disabled="pointInfo?.signedInToday"
              :loading="signingIn"
              @click="handleSignIn"
              style="width: 100%; margin-top: 12px;"
            >
              {{ pointInfo?.signedInToday ? '今日已签到' : '每日签到 +5' }}
            </el-button>
          </div>
        </el-card>

        <!-- 修改密码 -->
        <el-card style="margin-top: 20px;">
          <template #header>
            <span>修改密码</span>
          </template>

          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="80px"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                show-password
                placeholder="请输入原密码"
              />
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                show-password
                placeholder="请输入新密码"
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                show-password
                placeholder="请再次输入新密码"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="updatePassword" :loading="changingPassword">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：内容管理 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>内容管理</span>
              <el-button type="primary" size="small" @click="navigateTo('/posts/create')">
                发布帖子
              </el-button>
            </div>
          </template>

          <el-tabs v-model="activeTab">
            <el-tab-pane label="我的帖子" name="posts">
              <el-table :data="myPosts" v-loading="loadingPosts" style="width: 100%">
                <el-table-column prop="title" label="标题" min-width="200">
                  <template #default="{ row }">
                    <el-link type="primary" @click="navigateTo(`/posts/${row.id}`)">
                      {{ row.title }}
                    </el-link>
                  </template>
                </el-table-column>
                <el-table-column prop="categoryName" label="分类" width="100" />
                <el-table-column prop="viewCount" label="浏览" width="80" />
                <el-table-column prop="likeCount" label="点赞" width="80" />
                <el-table-column prop="commentCount" label="评论" width="80" />
                <el-table-column label="发布时间" width="160">
                  <template #default="{ row }">
                    {{ formatDate(row.createdAt) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120">
                  <template #default="{ row }">
                    <el-button text type="primary" size="small" @click="navigateTo(`/posts/${row.id}`)">
                      查看
                    </el-button>
                    <el-button text type="danger" size="small" @click="handleDelete(row.id)">
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>

              <el-empty v-if="!loadingPosts && myPosts.length === 0" description="暂无帖子" />
            </el-tab-pane>

            <el-tab-pane label="我的收藏" name="favorites">
              <div class="favorite-toolbar">
                <el-select v-model="favoriteFolderId" placeholder="全部收藏夹" clearable style="width: 180px;">
                  <el-option v-for="folder in favoriteFolders" :key="folder.id" :label="folder.name" :value="folder.id" />
                </el-select>
                <el-button @click="handleCreateFolder">新建收藏夹</el-button>
              </div>

              <el-table :data="favoritePosts" v-loading="loadingFavorites" style="width: 100%">
                <el-table-column prop="title" label="标题" min-width="220">
                  <template #default="{ row }">
                    <el-link type="primary" @click="navigateTo(`/posts/${row.id}`)">
                      {{ row.title }}
                    </el-link>
                  </template>
                </el-table-column>
                <el-table-column prop="categoryName" label="分类" width="100" />
                <el-table-column prop="nickname" label="作者" width="120">
                  <template #default="{ row }">
                    {{ row.nickname || row.username }}
                  </template>
                </el-table-column>
                <el-table-column label="发布时间" width="160">
                  <template #default="{ row }">
                    {{ formatDate(row.createdAt) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120">
                  <template #default="{ row }">
                    <el-button text type="primary" size="small" @click="navigateTo(`/posts/${row.id}`)">
                      查看
                    </el-button>
                    <el-button text type="danger" size="small" @click="handleUnfavorite(row.id)">
                      取消收藏
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>

              <el-empty v-if="!loadingFavorites && favoritePosts.length === 0" description="暂无收藏" />
            </el-tab-pane>

            <el-tab-pane label="浏览历史" name="history">
              <el-table :data="historyPosts" v-loading="loadingHistory" style="width: 100%">
                <el-table-column prop="title" label="标题" min-width="220">
                  <template #default="{ row }">
                    <el-link type="primary" @click="navigateTo(`/posts/${row.id}`)">
                      <el-tag v-if="row.status === 2" size="small" type="danger">置顶</el-tag>
                      <el-tag v-if="row.essence" size="small" type="warning">精华</el-tag>
                      {{ row.title }}
                    </el-link>
                  </template>
                </el-table-column>
                <el-table-column prop="categoryName" label="分类" width="100" />
                <el-table-column prop="nickname" label="作者" width="120">
                  <template #default="{ row }">
                    {{ row.nickname || row.username }}
                  </template>
                </el-table-column>
                <el-table-column prop="viewCount" label="浏览" width="80" />
                <el-table-column label="发布时间" width="160">
                  <template #default="{ row }">
                    {{ formatDate(row.createdAt) }}
                  </template>
                </el-table-column>
              </el-table>

              <el-empty v-if="!loadingHistory && historyPosts.length === 0" description="暂无浏览历史" />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

import type { FormInstance, FormRules } from 'element-plus'
import type { PostInfo, UserInfo, ApiResponse, PageData, FavoriteFolderInfo, UserPointInfo } from '~/types'

const api = useApi()
const userStore = useUserStore()
const activeTab = ref('posts')
const pointInfo = ref<UserPointInfo | null>(null)
const loadingPoints = ref(false)
const signingIn = ref(false)

// 个人信息表单
const profileFormRef = ref<FormInstance>()
const updating = ref(false)
const profileForm = reactive({
  nickname: '',
  email: '',
  avatar: ''
})

const profileRules: FormRules = {
  nickname: [
    { min: 2, max: 20, message: '昵称长度 2-20 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }
  ]
}

// 密码表单
const passwordFormRef = ref<FormInstance>()
const changingPassword = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6-20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 我的帖子
const myPosts = ref<PostInfo[]>([])
const loadingPosts = ref(true)

// 我的收藏
const favoriteFolders = ref<FavoriteFolderInfo[]>([])
const favoritePosts = ref<PostInfo[]>([])
const favoriteFolderId = ref<number | null>(null)
const loadingFavorites = ref(false)

// 浏览历史
const historyPosts = ref<PostInfo[]>([])
const loadingHistory = ref(false)

const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

// 头像上传成功
const handleAvatarSuccess = (url: string) => {
  profileForm.avatar = url
}

// 获取个人信息
const fetchUserInfo = async () => {
  try {
    const res = await api.get<ApiResponse<UserInfo>>('/user/me')
    if (res.code === 200) {
      const user = res.data
      profileForm.nickname = user.nickname || ''
      profileForm.email = user.email || ''
      profileForm.avatar = user.avatar || ''
      userStore.setUserInfo(user)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

const fetchPointInfo = async () => {
  loadingPoints.value = true
  try {
    const res = await api.get<ApiResponse<UserPointInfo>>('/points/me')
    if (res.code === 200) {
      pointInfo.value = res.data
    }
  } catch (error) {
    console.error('获取积分信息失败:', error)
  } finally {
    loadingPoints.value = false
  }
}

const handleSignIn = async () => {
  signingIn.value = true
  try {
    const res = await api.post<ApiResponse<UserPointInfo>>('/points/sign-in')
    if (res.code === 200) {
      pointInfo.value = res.data
      ElMessage.success(res.data.signedInToday ? '签到成功' : '今日已签到')
    } else {
      ElMessage.error(res.message || '签到失败')
    }
  } finally {
    signingIn.value = false
  }
}

// 获取我的帖子
const fetchMyPosts = async () => {
  try {
    const res = await api.get<ApiResponse<PageData<PostInfo>>>('/posts', {
      params: { page: 1, size: 100 }
    })
    if (res.code === 200) {
      myPosts.value = res.data.records.filter(
        (p) => p.userId === userStore.userInfo?.id
      )
    }
  } catch (error) {
    console.error('获取帖子列表失败:', error)
  } finally {
    loadingPosts.value = false
  }
}

// 获取收藏夹
const fetchFavoriteFolders = async () => {
  try {
    const res = await api.get<ApiResponse<FavoriteFolderInfo[]>>('/favorites/folders')
    if (res.code === 200) {
      favoriteFolders.value = res.data
    }
  } catch (error) {
    console.error('获取收藏夹失败:', error)
  }
}

// 获取收藏帖子
const fetchFavoritePosts = async () => {
  loadingFavorites.value = true
  try {
    const params: Record<string, number> = { page: 1, size: 100 }
    if (favoriteFolderId.value) {
      params.folderId = favoriteFolderId.value
    }
    const res = await api.get<ApiResponse<PageData<PostInfo>>>('/favorites/posts', params)
    if (res.code === 200) {
      favoritePosts.value = res.data.records
    }
  } catch (error) {
    console.error('获取收藏帖子失败:', error)
  } finally {
    loadingFavorites.value = false
  }
}

const fetchHistoryPosts = async () => {
  loadingHistory.value = true
  try {
    const res = await api.get<ApiResponse<PageData<PostInfo>>>('/posts/history', { page: 1, size: 100 })
    if (res.code === 200) {
      historyPosts.value = res.data.records
    }
  } catch (error) {
    console.error('获取浏览历史失败:', error)
  } finally {
    loadingHistory.value = false
  }
}

// 更新个人信息
const updateProfile = async () => {
  if (!profileFormRef.value) return

  await profileFormRef.value.validate(async (valid) => {
    if (!valid) return

    updating.value = true
    try {
      const res = await api.put<ApiResponse<string>>('/user/me', profileForm)
      if (res.code === 200) {
        ElMessage.success('更新成功')
        fetchUserInfo()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    } catch (error) {
      ElMessage.error('更新失败')
    } finally {
      updating.value = false
    }
  })
}

// 修改密码
const updatePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return

    changingPassword.value = true
    try {
      const res = await api.put<ApiResponse<string>>('/user/me/password', {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      if (res.code === 200) {
        ElMessage.success('密码修改成功')
        passwordFormRef.value?.resetFields()
      } else {
        ElMessage.error(res.message || '修改失败')
      }
    } catch (error) {
      ElMessage.error('修改失败')
    } finally {
      changingPassword.value = false
    }
  })
}

// 删除帖子
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇帖子吗？', '提示', {
      type: 'warning'
    })

    const res = await api.delete<ApiResponse<string>>(`/posts/${id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchMyPosts()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch {
    // 用户取消
  }
}

// 新建收藏夹
const handleCreateFolder = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入收藏夹名称', '新建收藏夹', {
      inputPattern: /^.{1,50}$/,
      inputErrorMessage: '收藏夹名称长度为 1-50 个字符'
    })
    const res = await api.post<ApiResponse<number>>('/favorites/folders', { name: value })
    if (res.code === 200) {
      ElMessage.success('创建成功')
      fetchFavoriteFolders()
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch {
    // 用户取消
  }
}

// 取消收藏
const handleUnfavorite = async (postId: number) => {
  try {
    const res = await api.delete<ApiResponse<string>>(`/favorites/posts/${postId}`)
    if (res.code === 200) {
      ElMessage.success('取消收藏成功')
      fetchFavoritePosts()
    } else {
      ElMessage.error(res.message || '取消收藏失败')
    }
  } catch {
    ElMessage.error('取消收藏失败')
  }
}

watch(favoriteFolderId, () => {
  fetchFavoritePosts()
})

watch(activeTab, (tab) => {
  if (tab === 'favorites') {
    fetchFavoriteFolders()
    fetchFavoritePosts()
  }
  if (tab === 'history') {
    fetchHistoryPosts()
  }
})

onMounted(() => {
  fetchUserInfo()
  fetchPointInfo()
  fetchMyPosts()
  fetchFavoriteFolders()
})
</script>

<style scoped>
.profile-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.favorite-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.point-panel {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.point-row {
  display: flex;
  justify-content: space-between;
  color: #606266;
}
</style>
