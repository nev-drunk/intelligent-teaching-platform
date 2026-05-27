<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const username = ref('admin')
const password = ref('123456')
const loading = ref(false)
const error = ref('')

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    await auth.login(username.value, password.value)
    router.push('/admin/portal')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
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
        <h2>用户登录</h2>
        <form class="login-form" @submit.prevent="handleLogin">
          <div class="input-group">
            <span class="input-icon"></span>
            <input v-model="username" type="text" placeholder="请输入用户名/手机号" autocomplete="username" />
          </div>
          <div class="input-group">
            <span class="input-icon">🔒</span>
            <input v-model="password" type="password" placeholder="请输入密码" autocomplete="current-password" />
          </div>
          <div class="form-options">
            <label class="remember-me">
              <input type="checkbox" />
              <span>记住我</span>
            </label>
            <div class="links">
              <a href="#">用户中心登录</a>
              <a href="#">邀请码登录</a>
            </div>
          </div>
          <p v-if="error" class="error">{{ error }}</p>
          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? '登录中…' : '登 录' }}
          </button>
        </form>
        <router-link to="/portal" class="portal-link">→ 访问网站门户（免登录）</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  background: linear-gradient(135deg, #0A1628 0%, #165DFF 35%, #4080FF 70%, #80B3FF 100%);
  position: relative;
  overflow: hidden;
}

.login-page::before {
  content: '';
  position: absolute;
  top: -20%;
  right: -10%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.12) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
}

.login-page::after {
  content: '';
  position: absolute;
  bottom: -15%;
  left: -10%;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.08) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
}

.left-section {
  flex: 1.5;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  z-index: 1;
}

.brand-area {
  color: #ffffff;
  text-align: center;
}

.brand-area h1 {
  font-size: 48px;
  font-weight: 700;
  margin: 0 0 36px;
  letter-spacing: 8px;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.logo-area {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.logo-icon {
  width: 88px;
  height: 88px;
  object-fit: contain;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.2));
}

.logo-text {
  font-size: 40px;
  font-weight: 600;
  letter-spacing: 8px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.right-section {
  width: 45%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  position: relative;
  z-index: 1;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: var(--radius-xl);
  padding: 48px 36px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.login-card h2 {
  text-align: center;
  margin: 0 0 36px;
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text-primary);
  letter-spacing: 8px;
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
  font-size: 16px;
  color: var(--color-text-muted);
  pointer-events: none;
}

.input-group input {
  width: 100%;
  padding: 14px 16px 14px 48px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 14px;
  font-family: inherit;
  box-sizing: border-box;
  transition: all 0.25s ease;
  background: var(--color-bg-page);
}

.input-group input:focus {
  outline: none;
  border-color: var(--color-primary);
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(22, 93, 255, 0.1);
}

.input-group input::placeholder {
  color: var(--color-text-muted);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-secondary);
  cursor: pointer;
}

.remember-me input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: var(--color-primary);
  cursor: pointer;
}

.links {
  display: flex;
  gap: 20px;
}

.links a {
  color: var(--color-text-secondary);
  text-decoration: none;
  transition: all 0.2s;
}

.links a:hover {
  color: var(--color-primary);
  text-decoration: underline;
}

.error {
  color: var(--color-danger);
  font-size: 13px;
  margin: 0;
  padding: 12px 14px;
  background: rgba(245, 63, 63, 0.06);
  border-radius: var(--radius-sm);
  border-left: 3px solid var(--color-danger);
}

.submit-btn {
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  color: #ffffff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 4px 16px rgba(22, 93, 255, 0.28);
  letter-spacing: 3px;
}

.submit-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, var(--color-primary-dark) 0%, #003A8C 100%);
  box-shadow: 0 8px 24px rgba(22, 93, 255, 0.35);
  transform: translateY(-2px);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
}

.portal-link {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 28px;
  padding-top: 20px;
  font-size: 13px;
  color: var(--color-text-muted);
  text-decoration: none;
  border-top: 1px solid var(--color-border);
  transition: all 0.25s ease;
}

.portal-link:hover {
  color: var(--color-primary);
  gap: 10px;
}

@media (max-width: 960px) {
  .login-page {
    flex-direction: column;
  }
  
  .left-section {
    padding: 40px 24px;
  }
  
  .brand-area h1 {
    font-size: 28px;
    margin-bottom: 24px;
    letter-spacing: 4px;
  }
  
  .logo-icon {
    width: 64px;
    height: 64px;
  }
  
  .logo-text {
    font-size: 28px;
    letter-spacing: 4px;
  }
  
  .right-section {
    width: 100%;
    padding: 24px;
  }
  
  .login-card {
    padding: 32px 24px;
  }
}
</style>
