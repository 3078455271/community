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

      <!-- 右侧：我的帖子 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>我的帖子</span>
              <el-button type="primary" size="small" @click="navigateTo('/posts/create')">
                发布帖子
              </el-button>
            </div>
          </template>

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
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import type { FormInstance, FormRules } from 'element-plus'
import type { PostInfo, UserInfo, ApiResponse, PageData } from '~/types'

const api = useApi()
const userStore = useUserStore()

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

onMounted(() => {
  fetchUserInfo()
  fetchMyPosts()
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
</style>