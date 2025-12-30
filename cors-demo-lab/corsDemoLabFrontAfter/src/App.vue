<template>
  <div class="container">
    <h1>Vue + Axios CRUD Demo</h1>
    <div class="nav" v-if="authenticated">
      <button @click="view='users'">Users</button>
      <button @click="view='posts'">Posts</button>
      <button style="float:right" @click="logout">Logout</button>
    </div>

    <div v-if="!authenticated">
      <Login />
    </div>

    <div v-else>
      <component :is="currentComponent" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import Users from './components/Users.vue'
import Posts from './components/Posts.vue'
import Login from './components/Login.vue'
import api, { isAuthenticated } from './api'

const view = ref('users')
const currentComponent = computed(() => (view.value === 'users' ? Users : Posts))

const authenticated = ref(false)

function logout() {
  // 调用后端登出，后端会清除 cookie
  api.post('/logout').finally(() => {
    authenticated.value = false
  })
}

onMounted(() => {
  // 检查是否已登录（会向受保护接口发起请求，返回 200 则视为已登录）
  isAuthenticated().then(ok => {
    authenticated.value = ok
  })

  // 监听登录成功事件（Login.vue 会在登录成功后触发 window 的 login-success 事件）
  const onLogin = () => { authenticated.value = true }
  window.addEventListener('login-success', onLogin)

  // 清理监听器
  onBeforeUnmount(() => {
    window.removeEventListener('login-success', onLogin)
  })
})
</script>

<style>
body { font-family: Arial, sans-serif; }
.container { max-width: 900px; margin: 24px auto; }
.nav { margin-bottom: 16px; }
.nav button { margin-right: 8px; }
</style>
