import axios from 'axios'

// 注意：这个版本直接请求后端 URL，用于演示 CORS 跨域问题
// 当前端运行在 http://localhost:5173
// 后端运行在 http://localhost:8080
// 由于端口不同，浏览器会阻止请求并报 CORS 错误
const api = axios.create({  baseURL: 'http://localhost:8080/api',  // 直接请求后端，会触发 CORS
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,  // 允许携带 Cookie
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
