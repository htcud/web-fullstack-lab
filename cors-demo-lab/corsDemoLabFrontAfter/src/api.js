import axios from 'axios'

// 注意：这个版本直接请求后端 URL，真正测试跨域
// 前端运行在 http://localhost:5173
// 后端运行在 http://localhost:8080
// 因为测试CORS，所以不使用代理
// 后端配置了全局 CORS 来支持跨域
const api = axios.create({
  baseURL: 'http://localhost:8080/api',  // 直接请求后端，体验真正的CORS配置
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,  // 允许携带 Cookie
})

// 添加响应拦截器用于调试
api.interceptors.response.use(
  response => response,
  error => {
    console.error('API Error:', {
      message: error.message,
      status: error.response?.status,
      statusText: error.response?.statusText,
      headers: error.response?.headers,
      data: error.response?.data,
    })
    return Promise.reject(error)
  }
)

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
