import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 注意：这个版本故意不配置 proxy，让前端直接请求后端
// 这样可以演示 CORS 跨域问题
export default defineConfig({
  plugins: [vue()],
  server: {
    allowedHosts: ['local.dev'],
    // 没有 proxy 配置，前端会直接请求 http://localhost:8080/api
    // 浏览器会因为跨域问题而阻止请求
  },
})