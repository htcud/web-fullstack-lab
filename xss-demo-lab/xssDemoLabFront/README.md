# Vue + Axios + json-server Demo

快速启动步骤：

1. 安装依赖：

```bash
npm install
```

2. 启动 json-server（或使用 `npm run start` 同时启动前端与后端）：

```bash
npm run json-server
# 在另一个终端
npm run dev
```

或者同时运行（需要 `concurrently` 已安装）：

```bash
npm run start
```

前端默认通过 Vite 在 http://localhost:5173
json-server 在 http://localhost:3000

示例 API：
- http://localhost:3000/users
- http://localhost:3000/posts
