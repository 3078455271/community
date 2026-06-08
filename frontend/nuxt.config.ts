// https://nuxt.com/docs/api/configuration/nuxt-config
declare const process: {
  env: Record<string, string | undefined>
}

export default defineNuxtConfig({
  devtools: { enabled: true },

  experimental: {
    appManifest: false,
  },

  modules: [
    '@element-plus/nuxt',
    '@pinia/nuxt',
  ],

  runtimeConfig: {
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE || '',
    },
  },

  app: {
    pageTransition: { name: 'page', mode: 'out-in' },
    head: {
      title: 'Community Zhuge',
      meta: [
        { charset: 'utf-8' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
        { name: 'description', content: '社区论坛' },
      ],
    },
  },

  css: ['~/assets/css/main.css'],

  compatibilityDate: '2024-11-01',
})
