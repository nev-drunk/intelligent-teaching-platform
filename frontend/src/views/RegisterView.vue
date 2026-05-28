<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  phone: ''
})

const loading = ref(false)
const error = ref('')

const validateForm = () => {
  if (!form.value.username.trim()) {
    error.value = '请输入用户名'
    return false
  }
  if (form.value.username.length < 3) {
    error.value = '用户名长度至少3个字符'
    return false
  }
  if (!form.value.password) {
    error.value = '请输入密码'
  }
  if (form.value.password.length < 6) {
    error.value = '密码长度至少6个字符'
    return false
  }
  if (form.value.password !== form.value.confirmPassword) {
    error.value = '两次输入的密码不一致'
    return false
  }
  if (!form.value.name.trim()) {
    error.value = '请输入姓名'
    return false
  }
  if (form.value.phone && !/^1[3-9]\d{9}$/.test(form.value.phone)) {
    error.value = '手机号格式不正确'
    return false
  }
  return true
}

const handleRegister = async () => {
  error.value = ''
  if (!validateForm()) return

  loading.value = true
  try {
    await auth.register({
      username: form.value.username,
      password: form.value.password,
      name: form.value.name,
      phone: form.value.phone
    })
    router.push('/admin/dashboard')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<template>
  <div class="login-page">
    <div class="left-section">
      <div class="brand-area">
        <h1>全国教师管理信息系统</h1>
        <div class="logo-area">
          <img src="@/assets/teacher_logo.png" alt="logo" class="logo-icon" />
          <span class="logo-text">中国教师</span>
        </div>
      </div>
    </div>
    <div class="right-section">
      <div class="login-card">
        <h2>用户注册</h2>
        <form class="login-form" @submit.prevent="handleRegister">
          <div class="input-group">
            <span class="input-icon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
            </span>
            <input
              v-model="form.username"
              type="text"
              placeholder="请输入用户名"
              autocomplete="username"
            />
          </div>

          <div class="input-group">
            <span class="input-icon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
            </span>
            <input
              v-model="form.password"
              type="password"
              placeholder="请输入密码（至少6位）"
              autocomplete="new-password"
            />
          </div>

          <div class="input-group">
            <span class="input-icon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path
                  d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"
                ></path>
              </svg>
            </span>
            <input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              autocomplete="new-password"
            />
          </div>

          <div class="input-group">
            <span class="input-icon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                <polyline points="14 2 14 8 20 8"></polyline>
                <line x1="16" y1="13" x2="8" y2="13"></line>
                <line x1="16" y1="17" x2="8" y2="17"></line>
                <polyline points="10 9 9 9 8 9"></polyline>
              </svg>
            </span>
            <input v-model="form.name" type="text" placeholder="请输入真实姓名" />
          </div>

          <div class="input-group">
            <span class="input-icon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <rect x="5" y="2" width="14" height="20" rx="2" ry="2"></rect>
                <line x1="12" y1="18" x2="12.01" y2="18"></line>
              </svg>
            </span>
            <input v-model="form.phone" type="tel" placeholder="请输入手机号（可选）" />
          </div>

          <p v-if="error" class="error-msg">{{ error }}</p>

          <button type="submit" class="submit-btn" :disabled="loading">
            <span v-if="loading" class="spinner"></span>
            {{ loading ? '正在注册...' : '注 册' }}
          </button>
        </form>
        <div class="footer-area">
          <span>已有账号？</span>
          <a href="#" class="login-link" @click.prevent="goToLogin">立即登录</a>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  background: radial-gradient(circle at 10% 20%, #06152d 0%, #0e2b5c 40%, #164bb5 90%);
  position: relative;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

.login-page::before {
  content: '';
  position: absolute;
  top: -10%;
  right: -5%;
  width: 700px;
  height: 700px;
  background: radial-gradient(circle, rgba(24, 144, 255, 0.15) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
}

.left-section {
  flex: 1.4;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  z-index: 1;
}

.brand-area {
  color: #ffffff;
  text-align: left;
}

.brand-area h1 {
  font-size: 42px;
  font-weight: 600;
  margin: 0 0 24px;
  letter-spacing: 4px;
  background: linear-gradient(to right, #ffffff, #e6f7ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 16px;
  opacity: 0.9;
}

.logo-icon {
  width: 56px;
  height: 56px;
  object-fit: contain;
}

.logo-text {
  font-size: 32px;
  font-weight: 500;
  letter-spacing: 4px;
  color: #ffffff;
}

.right-section {
  width: 40%;
  min-width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  position: relative;
  z-index: 1;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(25px);
  -webkit-backdrop-filter: blur(25px);
  border-radius: 20px;
  padding: 44px 40px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.login-card h2 {
  text-align: center;
  margin: 0 0 32px;
  font-size: 24px;
  font-weight: 500;
  color: #ffffff;
  letter-spacing: 6px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.5);
  transition: color 0.3s ease;
}

.input-icon svg {
  width: 18px;
  height: 18px;
}

.input-group input {
  width: 100%;
  padding: 14px 16px 14px 48px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 10px;
  font-size: 14px;
  color: #ffffff;
  box-sizing: border-box;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  background: rgba(255, 255, 255, 0.05);
}

.input-group input:focus {
  outline: none;
  border-color: #165dff;
  background: rgba(255, 255, 255, 0.1);
  box-shadow: 0 0 0 4px rgba(22, 93, 255, 0.2);
}

.input-group input:focus + .input-icon {
  color: #165dff;
}

.input-group input::placeholder {
  color: rgba(255, 255, 255, 0.35);
}

.error-msg {
  color: #ff7875;
  font-size: 13px;
  margin: -4px 0 0;
  padding: 8px 12px;
  background: rgba(255, 120, 117, 0.1);
  border-radius: 6px;
  border: 1px solid rgba(255, 120, 117, 0.2);
}

.submit-btn {
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #165dff 0%, #0041c4 100%);
  color: #ffffff;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 18px rgba(22, 93, 255, 0.3);
  letter-spacing: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.submit-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #2e73ff 0%, #165dff 100%);
  box-shadow: 0 8px 24px rgba(22, 93, 255, 0.45);
  transform: translateY(-1px);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(1px);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  box-shadow: none;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #ffffff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.footer-area {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 28px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}

.login-link {
  color: #3875ff;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.login-link:hover {
  color: #5389ff;
}

@media (max-width: 1024px) {
  .login-page {
    flex-direction: column;
  }
  .left-section {
    flex: none;
    padding: 60px 40px 30px;
    justify-content: center;
  }
  .brand-area {
    text-align: center;
  }
  .logo-area {
    justify-content: center;
  }
  .right-section {
    width: 100%;
    min-width: auto;
    padding: 20px;
  }
  .login-card {
    padding: 36px 28px;
  }
}
</style>
