<template>
  <div>
    <h2>Login</h2>
    <form @submit.prevent="login">
      <input v-model="email" type="email" placeholder="Email" required />
      <input v-model="password" type="password" placeholder="Password" required />
      <button type="submit">Login</button>
    </form>
    <div v-if="error" style="color:#a00;margin-top:8px">{{ error }}</div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import api from '../api'

const email = ref('')
const password = ref('')
const error = ref('')

async function login() {
  error.value = ''
  try {
    const res = await api.post('/login', { email: email.value, password: password.value })
    // 后端现在使用 HttpOnly Cookie 存储认证信息，前端无法读取 token。
    // 当返回 200 时视为登录成功，通知父组件。
    if (res.status === 200) {
      // 通过自定义事件通知父组件登录成功
      const ev = new CustomEvent('login-success')
      window.dispatchEvent(ev)
    } else {
      error.value = 'Login failed'
    }
  } catch (e) {
    console.error('Login Error Details:', {
      message: e.message,
      response: e.response,
      status: e.response?.status,
      headers: e.response?.headers,
    })
    if (e.response && e.response.status === 401) error.value = 'Invalid credentials'
    else error.value = 'Login error: ' + (e.message || e)
  }
}
</script>

<style scoped>
input { display:block; margin:6px 0; }
</style>
