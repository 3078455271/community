<template>
  <div class="file-upload">
    <el-upload
      :show-file-list="false"
      :before-upload="handleBeforeUpload"
      :http-request="handleUpload"
      accept="image/*"
    >
      <slot>
        <el-button type="primary" :loading="uploading">
          <el-icon><Upload /></el-icon>
          {{ uploading ? '上传中...' : '选择文件' }}
        </el-button>
      </slot>
    </el-upload>

    <!-- 上传进度条 -->
    <div v-if="uploading && uploadProgress > 0" class="upload-progress">
      <el-progress
        :percentage="uploadProgress"
        :stroke-width="8"
        :format="progressFormat"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { Upload } from '@element-plus/icons-vue'
import type { UploadRequestOptions } from 'element-plus'

const emit = defineEmits<{
  success: [url: string]
  error: [message: string]
}>()

const { uploading, uploadProgress, smartUpload } = useUpload()

const handleBeforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }

  // 分片上传支持更大的文件（最大 100MB）
  const isLt100M = file.size / 1024 / 1024 < 100
  if (!isLt100M) {
    ElMessage.error('图片大小不能超过 100MB')
    return false
  }

  return true
}

const handleUpload = async (options: UploadRequestOptions) => {
  const url = await smartUpload(options.file as File, (progress) => {
    // 进度回调已通过 uploadProgress 响应式更新
  })

  if (url) {
    emit('success', url)
  } else {
    emit('error', '上传失败')
  }
}

const progressFormat = (percentage: number) => {
  return percentage === 100 ? '上传完成' : `${percentage}%`
}
</script>

<style scoped>
.file-upload {
  display: inline-block;
}

.upload-progress {
  margin-top: 8px;
  width: 200px;
}
</style>