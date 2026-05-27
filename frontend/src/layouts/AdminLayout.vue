<script setup>
import { computed } from 'vue'
import { useRoute, useRouter, RouterView } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const menuItems = [
  { path: '/admin/portal', label: '网站门户系统', icon: '🌐' },
  { path: '/admin/classes', label: '班级管理', icon: '📚' },
  { path: '/admin/homework-comments', label: '作业评语', icon: '✏️' },
  { path: '/admin/teaching-notifications', label: '教学通知', icon: '📢' },
  { path: '/admin/dashboard', label: '工作台', icon: '📋' }
]

const activePath = computed(() => route.path)

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <div class="admin-shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-icon">🎓</span>
        <div>
          <h1>教师中心</h1>
          <p>智能教学支持平台</p>
        </div>
      </div>
      <nav class="menu">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="menu-item"
          :class="{ active: activePath.startsWith(item.path) }"
        >
          <span class="menu-icon">{{ item.icon }}</span>
          {{ item.label }}
        </router-link>
      </nav>
      <div class="user-box">
        <p class="user-name">{{ auth.name || '教师' }}</p>
        <p class="user-meta">@{{ auth.username }}</p>
        <button type="button" class="logout-btn" @click="logout">退出登录</button>
      </div>
    </aside>
    <main class="main-area">
      <header class="top-bar">
        <h2>{{ route.meta.title || '后台管理' }}</h2>
      </header>
      <section class="content">
        <RouterView />
      </section>
    </main>
  </div>
</template>

<style scoped>
.admin-shell {
  display: flex;
  min-height: 100vh;
  background: var(--color-bg-page);
}

.sidebar {
  width: 256px;
  background: var(--color-bg-card);
  color: var(--color-text-secondary);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  border-radius: 0 var(--radius-xl) var(--radius-xl) 0;
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.04);
}

.brand {
  display: flex;
  gap: 12px;
  padding: 24px;
  border-bottom: 1px solid var(--color-border-light);
}

.brand-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  border-radius: var(--radius-md);
  color: #ffffff;
  font-size: 20px;
}

.brand h1 {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
  color: var(--color-text-primary);
  letter-spacing: 0.3px;
}

.brand p {
  margin: 3px 0 0;
  font-size: 12px;
  color: var(--color-text-muted);
}

.menu {
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  position: relative;
}

.menu-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 0;
  background: var(--color-primary);
  border-radius: 0 3px 3px 0;
  transition: height 0.25s ease;
}

.menu-item:hover {
  background: var(--color-bg-hover);
  color: var(--color-text-primary);
}

.menu-item.active {
  background: rgba(22, 93, 255, 0.08);
  color: var(--color-primary);
  font-weight: 600;
}

.menu-item.active::before {
  height: 60%;
}

.menu-icon {
  font-size: 18px;
  opacity: 0.8;
}

.user-box {
  padding: 20px 24px 24px;
  border-top: 1px solid var(--color-border-light);
  background: var(--color-bg-hover);
  border-radius: 0 0 var(--radius-xl) 0;
}

.user-name {
  margin: 0;
  font-weight: 600;
  color: var(--color-text-primary);
  font-size: 15px;
}

.user-meta {
  margin: 4px 0 12px;
  font-size: 12px;
  color: var(--color-text-muted);
}

.logout-btn {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-bg-card);
  color: var(--color-text-secondary);
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.logout-btn:hover {
  background: rgba(245, 63, 63, 0.08);
  border-color: rgba(245, 63, 63, 0.3);
  color: var(--color-danger);
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.top-bar {
  padding: 20px 32px;
  background: var(--color-bg-card);
  border-bottom: 1px solid var(--color-border);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.top-bar h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.content {
  flex: 1;
  padding: 28px 32px;
  overflow: auto;
}
</style>
