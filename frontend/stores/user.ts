import type { ApiResponse, UserInfo } from '~/types'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserInfo | null>(null)
  const initialized = ref(false)

  const isLoggedIn = computed(() => !!userInfo.value)
  const isManager = computed(() => ['ADMIN', 'MODERATOR'].includes(userInfo.value?.role || ''))
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

  const setUserInfo = (info: UserInfo | null) => {
    userInfo.value = info
    initialized.value = true
  }

  const fetchCurrentUser = async () => {
    const api = useApi()
    try {
      const res = await api.get<ApiResponse<UserInfo>>('/auth/me')
      setUserInfo(res.code === 200 ? res.data : null)
    } catch {
      setUserInfo(null)
    }
    return userInfo.value
  }

  const logout = () => {
    setUserInfo(null)
  }

  return {
    userInfo,
    initialized,
    isLoggedIn,
    isManager,
    isAdmin,
    setUserInfo,
    fetchCurrentUser,
    logout,
  }
})