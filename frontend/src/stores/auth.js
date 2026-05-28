import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, logout as logoutApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const teacherId = ref(localStorage.getItem('teacherId') || '')
  const name = ref(localStorage.getItem('name') || '')
  const username = ref(localStorage.getItem('username') || '')
  const avatar = ref(localStorage.getItem('avatar') || '')

  const isLoggedIn = computed(() => !!token.value)

  function setSession(data) {
    token.value = data.token
    teacherId.value = String(data.teacherId)
    name.value = data.name
    username.value = data.username
    avatar.value = data.avatar || ''

    // 存储到 localStorage
    localStorage.setItem('token', data.token)
    localStorage.setItem('teacherId', String(data.teacherId))
    localStorage.setItem('name', data.name)
    localStorage.setItem('username', data.username)
    if (data.avatar) {
      localStorage.setItem('avatar', data.avatar)
    }
  }

  async function login(usernameInput, password, rememberMe = false) {
    const res = await loginApi(usernameInput, password, rememberMe)
    
    // 如果勾选了记住我，保存用户名到本地
    if (rememberMe) {
      localStorage.setItem('rememberedUsername', usernameInput)
    } else {
      localStorage.removeItem('rememberedUsername')
    }
    
    setSession(res.data)
    return res
  }

  /**
   * 注册新用户
   * @param {object} data - 注册数据
   */
  async function register(data) {
    const res = await registerApi(data)
    setSession(res.data)
    return res
  }

  async function logout() {
    try {
      await logoutApi()
    } catch (e) {
      console.warn('Logout API call failed:', e)
    }

    token.value = ''
    teacherId.value = ''
    name.value = ''
    username.value = ''
    avatar.value = ''

    localStorage.removeItem('token')
    localStorage.removeItem('teacherId')
    localStorage.removeItem('name')
    localStorage.removeItem('username')
    localStorage.removeItem('avatar')
  }

  /**
   * 获取记住的用户名
   */
  function getRememberedUsername() {
    return localStorage.getItem('rememberedUsername') || ''
  }

  return { 
    token, teacherId, name, username, avatar, isLoggedIn, 
    login, logout, register, setSession, getRememberedUsername 
  }
})
