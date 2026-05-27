<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <div class="app-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo">
          <span class="logo-icon">&#127979;</span>
          <span class="logo-text">智慧教学平台</span>
        </div>
      </div>
      <nav class="sidebar-nav">
        <router-link to="/" class="nav-item" exact-active-class="active">
          <span class="nav-icon">&#127968;</span>
          <span>首页</span>
        </router-link>
        <router-link to="/course" class="nav-item" exact-active-class="active">
          <span class="nav-icon">&#128218;</span>
          <span>课程管理</span>
        </router-link>
      </nav>
    </aside>
    <main class="main-content">
      <RouterView />
    </main>
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
width: 100% !important;
  max-width: none !important; /* 彻底解除 1280px 的魔咒 */
  margin: 0 !important;
  padding: 0 !important;
  height: 100%;
}


body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background: #f0f2f5;
  color: #4a5568;
}

.app-layout {
display: flex;
display: flex;
  width: 100% !important; /* 改为 100% 确保跟随父级 */
  height: 100vh;
}

/* 侧边栏 */
.sidebar {
 width: 240px;
  min-width: 240px;
  background-color: #fff; /* 确保侧边栏整体背景是白色 */
  height: 100vh;          /* 强制高度为视口高度 */
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e4e7ed;
  /* 移除任何可能存在的 margin-left 或 padding */
  margin: 0;
}

.sidebar-header {
  padding: 20px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  font-size: 22px;
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: #2c3e50;
  letter-spacing: 0.5px;
}

.sidebar-nav {
flex: 1;               /* 占据侧边栏剩余的所有垂直空间 */
  background-color: #fff; 
  padding: 12px 10px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 8px;
  color: #606266;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.nav-item:hover {
  background: #f5f7fa;
  color: #409eff;
}

.nav-item.active {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: #fff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.35);
}

.nav-icon {
  font-size: 16px;
}

/* 主内容区 */
.main-content {
flex: 1;
  display: flex;
  flex-direction: column;
  align-items: stretch; /* 确保子页面拉伸 */
  width: 0; /* 配合 flex: 1 使用，防止内容撑破 */
}
</style>
