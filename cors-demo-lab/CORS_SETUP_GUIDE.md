# CORS 配置调试指南

## ✅ 后端 (corsDemoLabBackendAfter) 的正确配置

### 1. WebConfig.java - 全局 CORS 配置
```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:5173")  // 允许前端域名
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)  // 关键：允许携带 Cookie
            .maxAge(3600);  // 预检请求缓存时间
}
```

### 2. 拦截器中放行 OPTIONS
拦截器中必须放行 OPTIONS 预检请求，否则跨域会失败：
```java
if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
    return true;
}
```

### 3. 不要设置 CSP 头
在拦截器中避免设置 CSP 头，因为它可能会干扰 CORS 响应头。

## ✅ 前端 (corsDemoLabFrontAfter) 的正确配置

### 1. api.js - 直接请求后端 URL
```javascript
const api = axios.create({
  baseURL: 'http://localhost:8080/api',  // 直接请求，不使用代理
  withCredentials: true,  // 允许携带 Cookie
})
```

### 2. vite.config.js - 不使用代理
在测试真正的 CORS 时，不要使用 Vite 代理。

## 🔧 调试步骤

1. **后端启动日志**：查看是否有错误
   ```bash
   cd corsDemoLabBackendAfter
   mvn spring-boot:run
   ```

2. **前端启动**：
   ```bash
   cd corsDemoLabFrontAfter
   npm run dev
   ```

3. **打开浏览器开发者工具** (F12) → Network 标签
   - 查看登陆请求 (POST /api/login)
   - 响应头中应该看到：
     - `Access-Control-Allow-Origin: http://localhost:5173`
     - `Access-Control-Allow-Credentials: true`

4. **检查控制台错误** (F12) → Console 标签
   - 任何 CORS 错误都会显示在这里

## 常见问题

### 问题：请求被 CORS 阻止
**解决方案**：
1. 确认后端已正确配置 `addCorsMappings`
2. 确认前端 `baseURL` 指向正确的后端地址
3. 确认 OPTIONS 请求被正确放行
4. 检查浏览器控制台的详细错误信息

### 问题：预检请求 (OPTIONS) 返回 401
**解决方案**：
在拦截器中，放行所有 OPTIONS 请求（不需要验证 token）

### 问题：Cookie 无法携带
**解决方案**：
1. 前端：`withCredentials: true`
2. 后端：`.allowCredentials(true)`
两者都需要配置

## 📊 验证步骤

打开浏览器控制台，运行：
```javascript
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  withCredentials: true,
})

api.post('/login', { 
  email: 'alice@example.com', 
  password: '123456' 
}).then(res => {
  console.log('登陆成功！', res.data)
}).catch(err => {
  console.error('登陆失败', err)
})
```

如果看到登陆成功信息，则 CORS 配置正确！
