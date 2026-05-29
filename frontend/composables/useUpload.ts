import type { ApiResponse } from '~/types'

/** 分片上传初始化响应 */
interface MultipartInitResponse {
  uploadId: string
  objectKey: string
  chunkSize: number
}

/** 分片信息 */
interface PartInfo {
  partNumber: number
  etag: string
}

/** 上传进度回调 */
export type UploadProgressCallback = (progress: number) => void

/**
 * 文件上传 composable，支持分片上传
 */
export const useUpload = () => {
  const uploading = ref(false)
  const uploadProgress = ref(0)

  /**
   * 单文件上传（小文件，5MB 以内）
   * @param file 要上传的文件
   * @returns 文件 URL
   */
  const upload = async (file: File): Promise<string | null> => {
    // 检查文件大小（5MB）
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.error('文件大小不能超过 5MB，请使用分片上传')
      return null
    }

    // 检查文件类型
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
    if (!allowedTypes.includes(file.type)) {
      ElMessage.error('只支持 JPG、PNG、GIF、WebP 格式')
      return null
    }

    uploading.value = true
    uploadProgress.value = 0

    try {
      const formData = new FormData()
      formData.append('file', file)

      const response = await $fetch<ApiResponse<string>>('/api/files/upload', {
        method: 'POST',
        body: formData
      })

      if (response.code === 200) {
        uploadProgress.value = 100
        return response.data
      } else {
        ElMessage.error(response.message || '上传失败')
        return null
      }
    } catch (error) {
      ElMessage.error('上传失败')
      return null
    } finally {
      uploading.value = false
    }
  }

  /**
   * 分片上传（大文件）
   * @param file 要上传的文件
   * @param onProgress 进度回调
   * @returns 文件 URL
   */
  const uploadChunked = async (
    file: File,
    onProgress?: UploadProgressCallback
  ): Promise<string | null> => {
    uploading.value = true
    uploadProgress.value = 0

    try {
      // 1. 初始化分片上传
      const initResponse = await $fetch<ApiResponse<MultipartInitResponse>>('/api/files/multipart/init', {
        method: 'POST',
        body: {
          filename: file.name,
          fileSize: file.size
        }
      })

      if (initResponse.code !== 200 || !initResponse.data) {
        ElMessage.error('初始化上传失败')
        return null
      }

      const { uploadId, objectKey, chunkSize } = initResponse.data
      const chunks = Math.ceil(file.size / chunkSize)
      const parts: PartInfo[] = []

      // 2. 逐个上传分片
      for (let i = 0; i < chunks; i++) {
        const start = i * chunkSize
        const end = Math.min(start + chunkSize, file.size)
        const chunk = file.slice(start, end)

        const formData = new FormData()
        formData.append('file', chunk, file.name)

        const partResponse = await $fetch<ApiResponse<PartInfo>>('/api/files/multipart/upload', {
          method: 'POST',
          params: {
            uploadId,
            objectKey,
            partNumber: i + 1
          },
          body: formData
        })

        if (partResponse.code !== 200 || !partResponse.data) {
          // 上传失败，取消分片上传
          await abortMultipartUpload(uploadId, objectKey)
          ElMessage.error(`分片 ${i + 1} 上传失败`)
          return null
        }

        parts.push(partResponse.data)

        // 更新进度
        const progress = Math.round(((i + 1) / chunks) * 100)
        uploadProgress.value = progress
        onProgress?.(progress)
      }

      // 3. 完成分片上传
      const completeResponse = await $fetch<ApiResponse<string>>('/api/files/multipart/complete', {
        method: 'POST',
        body: {
          uploadId,
          objectKey,
          parts
        }
      })

      if (completeResponse.code === 200) {
        uploadProgress.value = 100
        return completeResponse.data
      } else {
        ElMessage.error('完成上传失败')
        return null
      }
    } catch (error) {
      ElMessage.error('上传失败')
      return null
    } finally {
      uploading.value = false
    }
  }

  /**
   * 取消分片上传
   */
  const abortMultipartUpload = async (uploadId: string, objectKey: string) => {
    try {
      await $fetch<ApiResponse<void>>('/api/files/multipart/abort', {
        method: 'POST',
        body: { uploadId, objectKey }
      })
    } catch (error) {
      console.error('取消分片上传失败:', error)
    }
  }

  /**
   * 智能上传：根据文件大小自动选择单文件或分片上传
   * @param file 要上传的文件
   * @param onProgress 进度回调
   * @returns 文件 URL
   */
  const smartUpload = async (
    file: File,
    onProgress?: UploadProgressCallback
  ): Promise<string | null> => {
    // 检查文件类型
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
    if (!allowedTypes.includes(file.type)) {
      ElMessage.error('只支持 JPG、PNG、GIF、WebP 格式')
      return null
    }

    // 5MB 以下使用单文件上传，以上使用分片上传
    if (file.size <= 5 * 1024 * 1024) {
      return upload(file)
    } else {
      return uploadChunked(file, onProgress)
    }
  }

  return {
    uploading: readonly(uploading),
    uploadProgress: readonly(uploadProgress),
    upload,
    uploadChunked,
    smartUpload
  }
}