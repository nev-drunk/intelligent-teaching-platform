<template>
  <aside class="exam-sidebar">
    <div class="sidebar-logo">
      <div class="logo-icon">
        <svg viewBox="0 0 24 24">
          <rect
            x="3"
            y="3"
            width="18"
            height="18"
            rx="4"
            fill="none"
            stroke="#fff"
            stroke-width="2.5"
          />
          <line
            x1="8"
            y1="10"
            x2="16"
            y2="10"
            stroke="#fff"
            stroke-width="2"
            stroke-linecap="round"
          />
          <line
            x1="8"
            y1="14"
            x2="14"
            y2="14"
            stroke="#fff"
            stroke-width="2"
            stroke-linecap="round"
          />
        </svg>
      </div>
      <div>
        <div class="logo-text">题库与组卷</div>
        <div class="logo-sub">智能教学系统</div>
      </div>
    </div>

    <nav class="sidebar-nav">
      <div class="nav-section">智能工具</div>
      <button
        type="button"
        class="nav-item"
        :class="{ active: aiPanelOpen?.value }"
        @click="toggleAi"
      >
        <span class="nav-icon">
          <svg viewBox="0 0 24 24">
            <circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" stroke-width="2" />
            <polygon points="10,8 16,12 10,16" fill="currentColor" />
          </svg>
        </span>
        <span>AI 智能出题</span>
        <span class="nav-badge">AI</span>
      </button>
    </nav>

    <div class="sidebar-footer">
      <div class="user-info">
        <div class="user-avatar">{{ avatarChar }}</div>
        <div>
          <div class="user-name">{{ auth?.name || '教师' }}</div>
          <div class="user-role">@{{ auth?.username || 'teacher' }}</div>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { inject, computed } from 'vue'

const global = inject('global')
const aiPanelOpen = inject('aiPanelOpen')
const auth = global?.auth

const avatarChar = computed(() => (auth?.name || '师').charAt(0))

const navItems = [
  {
    name: 'exam-questions',
    label: '题库管理',
    icon: '<svg viewBox="0 0 24 24"><rect x="4" y="3" width="16" height="18" rx="2"/><line x1="8" y1="8" x2="16" y2="8"/><line x1="8" y1="12" x2="16" y2="12"/></svg>'
  },
  {
    name: 'exam-assemble',
    label: '试卷组装',
    icon: '<svg viewBox="0 0 24 24"><rect x="5" y="2" width="14" height="20" rx="2"/><line x1="9" y1="7" x2="15" y2="7"/><line x1="9" y1="11" x2="15" y2="11"/></svg>'
  },
  {
    name: 'exam-publish',
    label: '发布管理',
    icon: '<svg viewBox="0 0 24 24"><polygon points="12,2 22,10 18,10 18,22 6,22 6,10 2,10"/></svg>'
  }
]

function toggleAi() {
  if (aiPanelOpen) {
    aiPanelOpen.value = !aiPanelOpen.value
  }
}
</script>

<style scoped>
.exam-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: var(--bg-sidebar, #ffffff);
  color: var(--text-primary, #4b5563);
  display: flex;
  flex-direction: column;
  border-radius: var(--radius-lg, 16px);
  box-shadow: var(--shadow-md, 0 4px 6px rgba(0, 0, 0, 0.05));
  overflow: hidden;
  margin: var(--spacing-md, 16px);
}

.sidebar-logo {
  display: flex;
  gap: 10px;
  padding: 20px 16px;
  border-bottom: 1px solid var(--border-light, #f3f4f6);
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, var(--blue-primary, #2563eb) 0%, #3b82f6 100%);
  border-radius: var(--radius-md, 12px);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 14px var(--blue-glow, rgba(37, 99, 235, 0.15));
}

.logo-icon svg {
  width: 20px;
  height: 20px;
}

.logo-text {
  font-weight: 700;
  font-size: 16px;
  color: var(--text-inverse, #1f2937);
}

.logo-sub {
  font-size: 11px;
  color: var(--text-tertiary, #9ca3af);
  margin-top: 2px;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 10px;
}

.nav-section {
  font-size: 11px;
  text-transform: uppercase;
  color: var(--text-tertiary, #9ca3af);
  padding: 8px 10px 6px;
  letter-spacing: 0.5px;
  font-weight: 600;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: var(--radius-md, 12px);
  color: var(--text-secondary, #6b7280);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  cursor: pointer;
  border: none;
  background: transparent;
  width: 100%;
  text-align: left;
  transition: all 0.15s ease-in-out;
}

.nav-item:hover {
  background: var(--bg-sidebar-hover, #f1f5f9);
  color: var(--text-primary, #4b5563);
}

.nav-item.active {
  background: var(--bg-sidebar-active, #eff6ff);
  color: var(--blue-primary, #2563eb);
  font-weight: 600;
  box-shadow: inset 3px 0 0 var(--blue-primary, #2563eb);
}

.nav-icon :deep(svg) {
  width: 18px;
  height: 18px;
  stroke: currentColor;
  fill: none;
}

.nav-badge {
  margin-left: auto;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 20px;
  background: var(--orange-vital, #f97316);
  color: #fff;
  font-weight: 700;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid var(--border-light, #f3f4f6);
}

.user-info {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 8px 10px;
  border-radius: var(--radius-md, 12px);
  cursor: pointer;
  transition: background 0.15s ease-in-out;
}

.user-info:hover {
  background: var(--bg-sidebar-hover, #f1f5f9);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--blue-primary, #2563eb), #3b82f6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

.user-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary, #4b5563);
}

.user-role {
  font-size: 11px;
  color: var(--text-tertiary, #9ca3af);
}
</style>
