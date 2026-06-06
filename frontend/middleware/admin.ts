export default defineNuxtRouteMiddleware(async () => {
  const userStore = useUserStore()
  if (!userStore.initialized) {
    await userStore.fetchCurrentUser()
  }

  if (!userStore.isManager) {
    return navigateTo('/')
  }
})