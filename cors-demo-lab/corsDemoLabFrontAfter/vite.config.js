import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 注意：这个版本没有配置 proxy，前端直接请求后端
// 这样可以真正测试跨域（并验证@CrossOrigin配置是否生效）
export default defineConfig({
  plugins: [vue()],
  server: {
    allowedHosts: ['local.dev'],
    // 没有 proxy 配置，前端会直接请求 http://localhost:8080/api
    // 浏览器会处理 CORS 预检请求
  },
})