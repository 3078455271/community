import type { ApiResponse } from '~/types'

export const useApi = () => {
  const config = useRuntimeConfig()
  const apiBase = config.public.apiBase?.trim() || ''
  const baseURL = apiBase.startsWith('http://') || apiBase.startsWith('https://')
    ? `${apiBase}/api`
    : '/api'

  const request = async <T>(url: string, options: Record<string, unknown> = {}): Promise<T> => {
    const response = await $fetch<T>(url, {
      baseURL,
      ...options,
    })
    return response
  }

  return {
    get: <T>(url: string, params?: Record<string, unknown>) => request<T>(url, { method: 'GET', params }),
    post: <T>(url: string, body?: unknown) => request<T>(url, { method: 'POST', body }),
    put: <T>(url: string, body?: unknown) => request<T>(url, { method: 'PUT', body }),
    delete: <T>(url: string) => request<T>(url, { method: 'DELETE' }),
  }
}
