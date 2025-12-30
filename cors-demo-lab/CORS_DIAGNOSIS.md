# CORS 问题诊断清单

如果 corsDemoLabBackendAfter 仍然无法登陆，请按以下步骤诊断：

## 1️⃣ 验证后端配置

### 检查 JsonApiController.java
```bash
grep -n "@CrossOrigin\|@RestController" corsDemoLabBackendAfter/src/main/java/com/httpcode/test/JsonApiController.java
```

应该看到：
- ✅ `@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")` 在类级别
- ✅ `@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true", maxAge = 3600)` 在登陆方法上

### 检查 WebConfig.java
```bash
grep -n "addCorsMappings\|allowCredentials" corsDemoLabBackendAfter/src/main/java/com/httpcode/test/WebConfig.java
```

应该看到：
- ✅ `public void addCorsMappings(CorsRegistry registry)` 方法存在
- ✅ `.allowCredentials(true)` 配置存在
- ✅ `.allowedOrigins("http://localhost:5173")` 配置存在

## 2️⃣ 验证前端配置

### 检查 api.js
```bash
grep -n "baseURL\|withCredentials" corsDemoLabFrontAfter/src/api.js
```

应该看到：
- ✅ `baseURL: 'http://localhost:8080/api'` （直接请求，不是代理）
- ✅ `withCredentials: true`

### 检查 vite.config.js
```bash
grep -n "proxy" corsDemoLabFrontAfter/vite.config.js
```

应该看到：
- ✅ 没有配置 proxy（或者 proxy 配置被注释了）

## 3️⃣ 启动并测试

### 启动后端
```bash
cd corsDemoLabBackendAfter
mvn clean spring-boot:run
```

等待看到：
- ✅ `Tomcat started on port(s): 8080`
- ✅ 没有任何 CORS 相关的错误

### 启动前端
```bash
cd corsDemoLabFrontAfter
npm run dev
```

等待看到：
- ✅ `VITE v... ready in ... ms`
- ✅ `Local: http://localhost:5173`

### 在浏览器中打开测试
打开这个文件：`test-cors.html`（在 cors-demo-lab 目录中）

## 4️⃣ 使用 Network 标签调试

打开浏览器 F12 → Network 标签：

1. 在前端输入邮箱和密码
2. 点击登陆
3. 观察 Network 标签中的请求：

**预检请求 (OPTIONS /api/login)**：
- 状态码应该是 200 或 204（不是 404 或 401）
- 响应头应该包含：
  ```
  Access-Control-Allow-Origin: http://localhost:5173
  Access-Control-Allow-Credentials: true
  Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
  ```

**实际登陆请求 (POST /api/login)**：
- 状态码应该是 200（登陆成功）或 401（凭证错误，但至少没有被 CORS 阻止）
- 响应头同样应该包含上述 CORS 头

## 5️⃣ 使用 Console 标签调试

打开浏览器 F12 → Console 标签，在控制台运行：

```javascript
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  withCredentials: true,
})

api.post('/login', { 
  email: 'alice@example.com', 
  password: '123456' 
}).then(res => {
  console.log('✓ 登陆成功！', res.data)
}).catch(err => {
  console.error('✗ 登陆失败', {
    message: err.message,
    status: err.response?.status,
    headers: err.response?.headers,
  })
})
```

## 常见错误排查

### ❌ 错误：CORS policy has been blocked
- 原因：后端未配置 CORS 或配置不正确
- 解决：检查 JsonApiController 和 WebConfig 中的 CORS 配置

### ❌ 错误：OPTIONS 返回 404
- 原因：Spring 没有正确处理 CORS 预检请求
- 解决：确保 addCorsMappings 方法被正确实现

### ❌ 错误：OPTIONS 返回 401
- 原因：拦截器阻止了 OPTIONS 请求
- 解决：在拦截器中确保放行 OPTIONS 请求

### ❌ 错误：跨域请求成功但 Cookie 未携带
- 原因：前后端中任一方未设置 credentials
- 解决：
  - 前端：`withCredentials: true`
  - 后端：`.allowCredentials(true)`

## 📞 获取帮助

如果仍有问题，请提供：
1. 后端启动日志（完整输出）
2. 浏览器 Network 标签中 OPTIONS 请求的完整信息
3. 浏览器 Console 中的错误信息

