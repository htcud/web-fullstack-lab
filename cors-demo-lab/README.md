# CORS 跨域资源共享演示项目

基于 Vue 3 前端 + Spring Boot 后端的 CORS（Cross-Origin Resource Sharing）跨域演示项目。

## 项目结构

```
cors-demo-lab/
├── corsDemoLabFront/          # 前端 - 不支持跨域（演示问题）
├── corsDemoLabBackend/        # 后端 - 不支持跨域（演示问题）
├── corsDemoLabFrontAfter/     # 前端 - 支持跨域（演示解决方案）
└── corsDemoLabBackendAfter/   # 后端 - 支持跨域（演示解决方案）
```

## 什么是 CORS？

CORS（跨域资源共享）是一种基于 HTTP 头的机制，允许服务器指示浏览器应该允许哪些源（origin）访问其资源。

**同源策略（Same-Origin Policy）**：浏览器的安全特性，限制一个源的文档或脚本如何与另一个源的资源进行交互。

**源（Origin）** = 协议（Protocol）+ 域名（Domain）+ 端口（Port）

例如：
- `http://localhost:5173`（前端）
- `http://localhost:8080`（后端）

这两个是**不同源**，因此会触发 CORS 限制。

## 演示场景

### 场景一：不支持跨域（会报错）

**目录**：`corsDemoLabFront` + `corsDemoLabBackend`

#### 问题描述
- 前端运行在：`http://localhost:5173`
- 后端运行在：`http://localhost:8080`
- 前端直接请求后端 API 时，浏览器会阻止请求，报 CORS 错误

#### 启动步骤

1. **启动后端**（不支持跨域）
   ```bash
   cd corsDemoLabBackend
   mvn spring-boot:run
   # 后端运行在 http://localhost:8080
   ```

2. **启动前端**（配置为直接请求后端）
   ```bash
   cd corsDemoLabFront
   npm install
   npm run dev
   # 前端运行在 http://localhost:5173
   ```

3. **测试**
   - 打开浏览器：`http://localhost:5173`
   - 打开开发者工具（F12）→ Console
   - 尝试登录或访问数据
   - **预期结果**：浏览器控制台会出现 CORS 错误，类似：
     ```
     Access to XMLHttpRequest at 'http://localhost:8080/api/users' 
     from origin 'http://localhost:5173' has been blocked by CORS policy: 
     No 'Access-Control-Allow-Origin' header is present on the requested resource.
     ```

### 场景二：支持跨域（正常工作）

**目录**：`corsDemoLabFrontAfter` + `corsDemoLabBackendAfter`

#### 解决方案
后端通过配置 CORS 允许跨域请求：
- 使用 `@CrossOrigin` 注解
- 配置允许的源、方法、头等

#### 启动步骤

1. **启动后端**（支持跨域）
   ```bash
   cd corsDemoLabBackendAfter
   mvn spring-boot:run
   # 后端运行在 http://localhost:8080
   ```

2. **启动前端**（配置为直接请求后端）
   ```bash
   cd corsDemoLabFrontAfter
   npm install
   npm run dev
   # 前端运行在 http://localhost:5173
   ```

3. **测试**
   - 打开浏览器：`http://localhost:5173`
   - 登录账号：
     - Email: `alice@example.com`
     - Password: `123456`
   - **预期结果**：正常登录，可以查看用户列表、帖子等数据

## CORS 配置详解

### 后端配置（Spring Boot）

#### 方式一：使用 @CrossOrigin 注解（简单快捷）
```java
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")  // 允许所有源，生产环境应指定具体域名
public class JsonApiController {
    // ...
}
```

#### 方式二：全局配置（推荐生产环境）
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")  // 指定允许的源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)  // 允许携带 Cookie
                .maxAge(3600);
    }
}
```

### 前端配置

#### 开发环境：使用 Vite Proxy（推荐）
```javascript
// vite.config.js
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})
```
**优点**：开发时前端请求 `/api/*` 会被代理到 `http://localhost:8080/api/*`，避免跨域问题

#### 生产环境：配置后端 CORS
生产环境中前端和后端通常部署在不同域名，需要后端正确配置 CORS。

## 常见 CORS 错误

### 1. No 'Access-Control-Allow-Origin' header
**原因**：后端未设置 CORS 头  
**解决**：添加 `@CrossOrigin` 或配置 CORS

### 2. CORS preflight request failed
**原因**：预检请求（OPTIONS）被拦截器拦截  
**解决**：在拦截器中放行 OPTIONS 请求
```java
if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
    return true;
}
```

### 3. Credentials flag is 'true', but 'Access-Control-Allow-Credentials' header is ''
**原因**：前端设置了 `withCredentials: true`，但后端未配置 `allowCredentials`  
**解决**：后端添加 `.allowCredentials(true)`

## 技术栈

- **前端**：Vue 3 + Vite + Axios
- **后端**：Spring Boot + Spring Web
- **认证**：Cookie-based (HttpOnly)

## 测试账号

- **Email**: `alice@example.com` / **Password**: `123456`
- **Email**: `bob@example.com` / **Password**: `123456`

## 学习要点

1. 理解同源策略和 CORS 的概念
2. 掌握 Spring Boot CORS 配置方法
3. 了解开发环境和生产环境的不同处理方式
4. 理解预检请求（Preflight Request）的作用
5. 掌握如何调试 CORS 问题
