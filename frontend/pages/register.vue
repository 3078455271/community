<template>
  <div class="register-page">
    <el-card class="register-card">
      <template #header>
        <h2>注册</h2>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>

        <el-form-item>
          <el-button class="register-btn" type="primary" @click="handleRegister" :loading="loading">
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <span>已有账号？</span>
        <NuxtLink to="/login">立即登录</NuxtLink>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import type { ApiResponse } from '~/types'

const formRef = ref()
const loading = ref(false)
const api = useApi()

const form = reactive({
  username: '',
  nickname: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
  ],
}

const handleRegister = async () => {
  await formRef.value?.validate()
  if (form.password !== form.confirmPassword) {
    ElMessage.error('两次密码不一致')
    return
  }
  loading.value = true
  try {
    const res = await api.post<ApiResponse<string>>('/auth/register', {
      username: form.username,
      nickname: form.nickname,
      email: form.email,
      password: form.password,
    })
    if (res.code === 200) {
      ElMessage.success('注册成功')
      navigateTo('/login')
    } else {
      ElMessage.error(res.message || '注册失败')
    }
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : '注册失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}

.register-card {
  width: 450px;
}

.register-card h2 {
  text-align: center;
  margin: 0;
}

.register-btn {
  width: 100%;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  color: #666;
}

.register-footer a {
  color: var(--primary-color);
  margin-left: 5px;
}
</style>