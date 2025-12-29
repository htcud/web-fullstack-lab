import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    allowedHosts: ['local.dev'],
    // 开发时代理 /api 到后端，避免浏览器 CORS 限制。
    // 前端请求仍然使用相对路径 /api/... 即可。
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        // 保持 /api 路径不变（后端期望 /api/...）
        rewrite: (path) => path,
      },
    },
  },
})