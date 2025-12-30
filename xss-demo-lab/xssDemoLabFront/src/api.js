import axios from 'axios'

const api = axios.create({
  // 使用相对路径，让 Vite 的 dev proxy (/api -> http://localhost:8080) 生效，避免 CORS
  baseURL: '/api',
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json',
  },
  // 重要：后端现在使用 HttpOnly Cookie 存储认证 token，需让浏览器发送 cookie
  withCredentials: true,
})

// 不再在前端管理 token（HttpOnly cookie 不可被 JS 读取）。
// 提供一个小 helper 用于判断登录状态（会触发一次请求并依赖后端返回 200/401）。
export async function isAuthenticated() {
  try {
    // 任选一个不会被公共访问的受保护接口，例如 /users
    await api.get('/users')
    return true
  } catch (e) {
    return false
  }
}

export default api
