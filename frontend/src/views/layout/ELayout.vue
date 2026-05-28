<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Document,
  ChatDotRound,
  DataAnalysis,
  ArrowLeft
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const menuItems = [
  { index: '/questionnaire', icon: Document, label: '问卷调查管理' },
  { index: '/issue-center', icon: ChatDotRound, label: '问题中心答疑' },
  { index: '/evaluation', icon: DataAnalysis, label: '教学效果评价' }
]

const activeMenu = computed(() => route.path)

function handleMenuSelect(index) {
  router.push(index)
}

function goBack() {
  router.push('/')
}
</script>

<template>
  <div class="layout-wrapper">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo-area">
          <div class="logo-icon">智</div>
          <span class="logo-text">教学平台</span>
        </div>
      </div>

      <div class="sidebar-user">
        <div class="user-avatar">张</div>
        <div class="user-info">
          <span class="user-name">张教授</span>
          <span class="user-role">教师端</span>
        </div>
      </div>

      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        background-color="transparent"
        text-color="#94a3b8"
        active-text-color="#ffffff"
        @select="handleMenuSelect"
      >
        <el-menu-item v-for="item in menuItems" :key="item.index" :index="item.index">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <button class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回首页</span>
        </button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.layout-wrapper {
  display: flex;
  min-height: 100vh;
}

/* ---------- 侧边栏 ---------- */
.sidebar {
  width: 220px;
  background: var(--bg-sidebar);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
}

.sidebar-header {
  padding: 20px 18px 12px;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: linear-gradient(135deg, var(--primary-blue), var(--primary-blue-light));
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 1rem;
  flex-shrink: 0;
}

.logo-text {
  color: var(--text-white);
  font-size: 1.05rem;
  font-weight: 700;
  letter-spacing: 1px;
}

/* ---------- 用户信息 ---------- */
.sidebar-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  margin: 4px 12px;
  background: var(--bg-sidebar-active);
  border-radius: var(--radius-sm);
}

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: var(--primary-blue);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.85rem;
  flex-shrink: 0;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  color: var(--text-white);
  font-size: 0.88rem;
  font-weight: 600;
}

.user-role {
  color: var(--text-muted);
  font-size: 0.75rem;
}

/* ---------- 菜单 ---------- */
.sidebar-menu {
  flex: 1;
  border: none !important;
  padding: 8px 0;
}

.sidebar-menu .el-menu-item {
  margin: 2px 10px;
  border-radius: var(--radius-sm);
  height: 44px;
  line-height: 44px;
  font-size: 0.9rem;
}

.sidebar-menu .el-menu-item:hover {
  background: var(--bg-sidebar-active) !important;
}

.sidebar-menu .el-menu-item.is-active {
  background: var(--primary-blue) !important;
  color: white !important;
}

/* ---------- 底部返回 ---------- */
.sidebar-footer {
  padding: 16px 18px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 8px 12px;
  border: none;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  border-radius: var(--radius-sm);
  font-size: 0.85rem;
  transition: all 0.2s;
}

.back-btn:hover {
  background: var(--bg-sidebar-active);
  color: var(--text-white);
}

/* ---------- 主内容区 ---------- */
.main-content {
  flex: 1;
  margin-left: 220px;
  padding: var(--space-xl);
  min-height: 100vh;
  background: var(--bg-page);
}
</style>
