import type { UserInfo } from '~/types'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>('')
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)

  const setToken = (newToken: string) => {
    token.value = newToken
    if (import.meta.client) {
      localStorage.setItem('token', newToken)
    }
  }

  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
    if (import.meta.client) {
      localStorage.setItem('userInfo', JSON.stringify(info))
    }
  }

  const loadFromStorage = () => {
    if (import.meta.client) {
      token.value = localStorage.getItem('token') || ''
      const stored = localStorage.getItem('userInfo')
      if (stored) {
        try {
          userInfo.value = JSON.parse(stored) as UserInfo
        } catch (e) {
          userInfo.value = null
        }
      }
    }
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    if (import.meta.client) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    setToken,
    setUserInfo,
    loadFromStorage,
    logout,
  }
})