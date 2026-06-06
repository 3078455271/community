export default defineNuxtRouteMiddleware(async (to) => {
  const userStore = useUserStore()
  if (!userStore.initialized) {
    await userStore.fetchCurrentUser()
  }

  if (!userStore.isLoggedIn) {
    return navigateTo({ path: '/login', query: { redirect: to.fullPath } })
  }
})