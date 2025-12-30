<template>
  <div>
    <h2>Posts</h2>
    <h3>DOM XSS Demo</h3>
    <input v-model="domInput" placeholder="Enter HTML for DOM XSS" />
    <button @click="injectDOM">Inject</button>
    <div id="dom-demo"></div>
    <div>
      <input v-model="searchQuery" placeholder="Search posts (for Reflected XSS demo)" />
      <button @click="performSearch">Search</button>
    </div>
    <div v-if="reflectedXSS">
      <h3>Search Result:</h3>
      <div>{{ reflectedXSS }}</div>
    </div>
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
        <strong>{{ p.title }}</strong> — <span>{{ p.body }}</span> (by {{ findUser(p.userId) }})
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
const searchQuery = ref('')
const reflectedXSS = ref('')
const domInput = ref('')

async function load() {
  const [pRes, uRes] = await Promise.all([api.get('/posts'), api.get('/users')])
  posts.value = pRes.data
  users.value = uRes.data
}

function performSearch() {
  const params = new URLSearchParams(window.location.search)
  params.set('q', searchQuery.value)
  window.location.search = params.toString()
}

function injectDOM() {
  document.getElementById('dom-demo').textContent = domInput.value
}

onMounted(() => {
  load()
  const params = new URLSearchParams(window.location.search)
  const q = params.get('q')
  if (q) {
    reflectedXSS.value = q
  }
})

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
