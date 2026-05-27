<script setup>
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
</script>

<template>
  <div class="dashboard">
    <div class="welcome-card">
      <h3>欢迎，{{ auth.name }}</h3>
      <p>您已成功登录教师中心后台。请从左侧导航进入各功能模块。</p>
      <ul>
        <li>网站门户系统：轮播图、通知公告、TTS 语音播报</li>
        <li>班级管理：查看 tb_class 数据</li>
        <li>其他模块由组员继续开发对接</li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.welcome-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-xl);
  padding: 32px;
  box-shadow: var(--shadow-card);
}

.welcome-card h3 {
  margin: 0 0 14px;
  color: var(--color-text-primary);
  font-size: 20px;
  font-weight: 600;
}

.welcome-card p {
  color: var(--color-text-secondary);
  margin: 0 0 20px;
  font-size: 15px;
  line-height: 1.6;
}

.welcome-card ul {
  margin: 0;
  padding-left: 24px;
  color: var(--color-text-secondary);
  line-height: 2;
}

.welcome-card li {
  position: relative;
  padding-left: 8px;
}

.welcome-card li::marker {
  color: var(--color-tech-blue);
  font-size: 12px;
}
</style>
