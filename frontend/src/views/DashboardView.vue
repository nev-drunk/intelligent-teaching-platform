<script setup>
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
</script>

<template>
  <div class="dashboard">
    <div class="welcome-card">
      <div class="welcome-icon">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
          <path
            d="M12 2L2 7l10 5 10-5-10-5z"
            stroke="currentColor"
            stroke-width="1.5"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
          <path
            d="M2 17l10 5 10-5"
            stroke="currentColor"
            stroke-width="1.5"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
          <path
            d="M2 12l10 5 10-5"
            stroke="currentColor"
            stroke-width="1.5"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </div>
      <h3>欢迎回来，{{ auth.name }}</h3>
      <p>选择左侧菜单进入相应功能模块</p>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.welcome-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-xl);
  padding: 48px 64px;
  box-shadow: var(--shadow-card);
  text-align: center;
  max-width: 480px;
}

.welcome-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto 20px;
  color: var(--color-tech-blue);
}

.welcome-card h3 {
  margin: 0 0 12px;
  color: var(--color-text-primary);
  font-size: 22px;
  font-weight: 600;
}

.welcome-card p {
  color: var(--color-text-secondary);
  margin: 0;
  font-size: 15px;
  line-height: 1.6;
}
</style>
