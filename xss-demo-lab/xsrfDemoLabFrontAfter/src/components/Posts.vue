<template>
  <div>
    <h2>Posts</h2>
    <form @submit.prevent="createPost">
      <input v-model="form.title" placeholder="Title" required />
      <input v-model="form.body" placeholder="Body" required />
      <select v-model.number="form.userId">
        <option v-for="u in users" :key="u.id" :value="u.id">{{ u.name }}</option>
      </select>
      <label><input type="checkbox" v-model="form.published" /> Published</label>
      <button type="submit">Add</button>
      <button v-if="editing" type="button" @click="cancelEdit">Cancel</button>
    </form>

    <ul>
      <li v-for="p in posts" :key="p.id">
        <strong>{{ p.title }}</strong> — {{ p.body }} (by {{ findUser(p.userId) }})
        <button @click="startEdit(p)">Edit</button>
        <button @click="deletePost(p.id)">Delete</button>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

const posts = ref([])
const users = ref([])
const form = ref({ title: '', body: '', userId: null, published: false })
const editing = ref(false)
const editingId = ref(null)

async function load() {
  const [pRes, uRes] = await Promise.all([api.get('/posts'), api.get('/users')])
  posts.value = pRes.data
  users.value = uRes.data
}

onMounted(load)

async function createPost() {
  if (editing.value) {
    await api.put(`/posts/${editingId.value}`, form.value)
    editing.value = false
    editingId.value = null
  } else {
    await api.post('/posts', form.value)
  }
  form.value = { title: '', body: '', userId: null, published: false }
  await load()
}

function startEdit(p) {
  editing.value = true
  editingId.value = p.id
  form.value = { title: p.title, body: p.body, userId: p.userId, published: p.published }
}

function cancelEdit() {
  editing.value = false
  editingId.value = null
  form.value = { title: '', body: '', userId: null, published: false }
}

function findUser(id) {
  const u = users.value.find(x => x.id === id)
  return u ? u.name : 'Unknown'
}

async function deletePost(id) {
  if (!confirm('Delete post?')) return
  await api.delete(`/posts/${id}`)
  await load()
}
</script>

<style scoped>
form { margin-bottom: 12px; }
input, select { margin-right: 8px; }
</style>
