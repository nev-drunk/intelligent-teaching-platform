<script setup>
import { computed } from 'vue'
import { useRoute, useRouter, RouterView } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const menuGroups = [
  {
    title: '教学管理',
    items: [
      {
        path: '/admin/courses',
        label: '课程管理',
        match: (p) => p.startsWith('/admin/courses')
      },
      {
        path: '/admin/classes',
        label: '班级管理',
        match: (p) => p.startsWith('/admin/classes')
      },
      {
        path: '/admin/student',
        label: '学生管理',
        match: (p) => p.startsWith('/admin/student')
      }
    ]
  },
  {
    title: '教学工作',
    items: [
      {
        path: '/admin/exam/questions',
        label: '题库与组卷',
        match: (p) => p.startsWith('/admin/exam')
      },
      {
        path: '/admin/homework-comments',
        label: '作业评语',
        match: (p) => p.startsWith('/admin/homework-comments')
      },
      {
        path: '/admin/submission',
        label: '作业批改',
        match: (p) => p.startsWith('/admin/submission')
      },
      {
        path: '/admin/teaching-notifications',
        label: '教学通知',
        match: (p) => p.startsWith('/admin/teaching-notifications')
      },
      {
        path: '/admin/questionnaire',
        label: '问卷调查',
        match: (p) => p.startsWith('/admin/questionnaire')
      },
      {
        path: '/admin/issue-center',
        label: '问题中心',
        match: (p) => p.startsWith('/admin/issue-center')
      },
      {
        path: '/admin/evaluation',
        label: '教学评价',
        match: (p) => p.startsWith('/admin/evaluation')
      }
    ]
  },
  {
    title: '系统功能',
    items: [
      {
        path: '/admin/portal',
        label: '网站门户',
        match: (p) => p.startsWith('/admin/portal')
      }
    ]
  }
]

const pageTitle = computed(() => {
  const matched = [...route.matched].reverse()
  const examChild = matched.find((r) => r.meta?.title && r.path.includes('exam'))
  const any = matched.find((r) => r.meta?.title)
  return examChild?.meta?.title || any?.meta?.title || '后台管理'
})

function isActive(item) {
  return item.match(route.path)
}

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <div class="admin-shell">
    <aside class="sidebar">
      <div class="brand">
        <div>
          <h1>教师中心</h1>
          <p>智能教学支持平台</p>
        </div>
      </div>
      <nav class="menu">
        <div v-for="group in menuGroups" :key="group.title" class="menu-group">
          <div class="group-title">{{ group.title }}</div>
          <router-link
            v-for="item in group.items"
            :key="item.path"
            :to="item.path"
            class="menu-item"
            :class="{ active: isActive(item) }"
          >
            {{ item.label }}
          </router-link>
        </div>
      </nav>
      <div class="user-box">
        <p class="user-name">{{ auth.name || '教师' }}</p>
        <p class="user-meta">@{{ auth.username }}</p>
        <button type="button" class="btn btn--ghost btn--danger" @click="logout">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
            <path
              d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M19 12H7"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          退出登录
        </button>
      </div>
    </aside>
    <main class="main-area">
      <header class="top-bar">
        <h2>{{ pageTitle }}</h2>
      </header>
      <section class="content" :class="{ 'content--exam': route.path.startsWith('/admin/exam') }">
        <RouterView />
      </section>
    </main>
  </div>
</template>

<style scoped>
.admin-shell {
  display: flex;
  min-height: 100vh;
  background: var(--bg-page);
}

.sidebar {
  width: 260px;
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-md);
  margin: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px;
  border-bottom: 1px solid var(--border-light);
}

.brand-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-hover) 100%);
  border-radius: var(--radius-lg);
  font-size: 22px;
  box-shadow: 0 4px 14px var(--primary-glow);
}

.brand h1 {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.brand p {
  font-size: 12px;
  color: var(--text-muted);
  margin: 2px 0 0;
}

.menu {
  flex: 1;
  padding: 16px 8px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
}

.menu-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.group-title {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-muted);
  padding: 8px 16px 6px;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: var(--radius-md);
  color: var(--text-primary);
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.15s ease;
}

.menu-item:hover {
  background: var(--bg-hover);
  color: var(--primary);
}

.menu-item.active {
  background: var(--primary-light);
  color: var(--primary);
  font-weight: 600;
  box-shadow: inset 3px 0 0 var(--primary);
}

.menu-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.user-box {
  padding: 20px 24px;
  border-top: 1px solid var(--border-light);
  margin-top: auto;
}

.user-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.user-meta {
  font-size: 12px;
  color: var(--text-muted);
  margin: 4px 0 12px;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
  font-family: inherit;
  border: none;
}

.btn--ghost {
  background: transparent;
  color: var(--text-muted);
  border: 1px solid var(--border);
}

.btn--ghost:hover {
  background: var(--bg-hover);
  color: var(--text-secondary);
}

.btn--danger {
  border-color: var(--danger);
  color: var(--danger);
}

.btn--danger:hover {
  background: #fef2f2;
  border-color: var(--danger);
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px;
  min-width: 0;
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  margin-bottom: 20px;
}

.top-bar h2 {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.content {
  flex: 1;
  overflow: auto;
}

.content--exam {
  padding: 0;
}
</style>
