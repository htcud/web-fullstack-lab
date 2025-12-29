<template>
  <div>
    <h2>Users</h2>
    <form @submit.prevent="createUser">
      <input v-model="form.name" placeholder="Name" required />
      <input v-model="form.email" placeholder="Email" required />
      <select v-model="form.role">
        <option>user</option>
        <option>admin</option>
      </select>
      <button type="submit">Add</button>
      <button v-if="editing" type="button" @click="cancelEdit">Cancel</button>
    </form>

    <table border="1" cellpadding="6" cellspacing="0">
      <thead>
        <tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Actions</th></tr>
      </thead>
      <tbody>
        <tr v-for="u in users" :key="u.id">
          <td>{{ u.id }}</td>
          <td>{{ u.name }}</td>
          <td>{{ u.email }}</td>
          <td>{{ u.role }}</td>
          <td>
            <button @click="startEdit(u)">Edit</button>
            <button @click="deleteUser(u.id)">Delete</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

const users = ref([])
const form = ref({ name: '', email: '', role: 'user' })
const editing = ref(false)
const editingId = ref(null)

async function load() {
  const res = await api.get('/users')
  users.value = res.data
}

onMounted(load)

async function createUser() {
  if (editing.value) {
    await api.put(`/users/${editingId.value}`, form.value)
    editing.value = false
    editingId.value = null
  } else {
    await api.post('/users', form.value)
  }
  form.value = { name: '', email: '', role: 'user' }
  await load()
}

function startEdit(u) {
  editing.value = true
  editingId.value = u.id
  form.value = { name: u.name, email: u.email, role: u.role }
}

function cancelEdit() {
  editing.value = false
  editingId.value = null
  form.value = { name: '', email: '', role: 'user' }
}

async function deleteUser(id) {
  if (!confirm('Delete user?')) return
  await api.delete(`/users/${id}`)
  await load()
}
</script>

<style scoped>
form { margin-bottom: 12px; }
input, select { margin-right: 8px; }
table { width: 100%; border-collapse: collapse; }
</style>
