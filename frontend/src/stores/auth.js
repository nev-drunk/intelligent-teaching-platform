import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const teacherId = ref(localStorage.getItem('teacherId') || '')
  const name = ref(localStorage.getItem('name') || '')
  const username = ref(localStorage.getItem('username') || '')

  const isLoggedIn = computed(() => !!token.value)

  function setSession(data) {
    token.value = data.token
    teacherId.value = String(data.teacherId)
    name.value = data.name
    username.value = data.username
    localStorage.setItem('token', data.token)
    localStorage.setItem('teacherId', String(data.teacherId))
    localStorage.setItem('name', data.name)
    localStorage.setItem('username', data.username)
  }

  async function login(usernameInput, password) {
    const res = await loginApi(usernameInput, password)
    setSession(res.data)
    return res
  }

  function logout() {
    token.value = ''
    teacherId.value = ''
    name.value = ''
    username.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('teacherId')
    localStorage.removeItem('name')
    localStorage.removeItem('username')
  }

  return { token, teacherId, name, username, isLoggedIn, login, logout, setSession }
})
