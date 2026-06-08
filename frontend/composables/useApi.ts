import type { ApiResponse } from '~/types'

export const useApi = () => {
  const config = useRuntimeConfig()
  const apiBase = config.public.apiBase?.trim() || ''
  const baseURL = apiBase.startsWith('http://') || apiBase.startsWith('https://')
    ? `${apiBase}/api`
    : '/api'

  const request = async <T>(url: string, options: Record<string, unknown> = {}): Promise<T> => {
    const headers = { ...(options.headers as Record<string, string> | undefined) }

    try {
      return await $fetch<T>(url, {
        baseURL,
        ...options,
        credentials: 'include',
        headers,
        timeout: 10000,
      })
    } catch (error: unknown) {
      const responseStatus = (error as { response?: { status?: number } }).response?.status
      if (responseStatus === 401 && import.meta.client) {
        const userStore = useUserStore()
        userStore.logout()
        if (!['/login', '/register'].includes(useRoute().path)) {
          navigateTo('/login')
        }
      }
      throw error
    }
  }

  const normalizeParams = (params?: Record<string, unknown>) => {
    if (params && 'params' in params && Object.keys(params).length === 1) {
      return params.params as Record<string, unknown>
    }
    return params
  }

  return {
    get: <T>(url: string, params?: Record<string, unknown>) => request<T>(url, {
      method: 'GET',
      params: normalizeParams(params),
    }),
    post: <T>(url: string, body?: unknown) => request<T>(url, { method: 'POST', body }),
    put: <T>(url: string, body?: unknown) => request<T>(url, { method: 'PUT', body }),
    delete: <T>(url: string) => request<T>(url, { method: 'DELETE' }),
  }
}
