<script setup>
import { ref } from 'vue'

const notifications = ref([
  {
    id: 1,
    title: '关于期中考试安排的通知',
    content:
      '各位同学请注意，本学期期中考试将于下周五（5月23日）进行，请提前做好复习准备。具体考试安排将另行通知。',
    type: 'important',
    status: 'published',
    createTime: '2026-05-15 10:30',
    readCount: 128
  },
  {
    id: 2,
    title: '课程作业延期提交说明',
    content:
      '由于近期系统维护，本周的课程作业提交截止时间将延长至下周一晚上10点，请同学们合理安排时间完成作业。',
    type: 'info',
    status: 'published',
    createTime: '2026-05-14 16:20',
    readCount: 96
  },
  {
    id: 3,
    title: '学术讲座预告',
    content:
      '下周三下午2点，将邀请校外专家来我校进行学术讲座，主题为"人工智能前沿技术发展"，欢迎各位同学参加。',
    type: 'event',
    status: 'draft',
    createTime: '2026-05-13 09:15',
    readCount: 0
  },
  {
    id: 4,
    title: '实验室安全培训通知',
    content:
      '请所有研究生同学于本周四下午1点到实验楼B栋301室参加实验室安全培训，培训完成后方可进入实验室开展研究工作。',
    type: 'warning',
    status: 'published',
    createTime: '2026-05-12 14:45',
    readCount: 72
  }
])

const showModal = ref(false)
const editingNotification = ref(null)
const searchQuery = ref('')

const filteredNotifications = ref(notifications.value)

function filterNotifications() {
  if (!searchQuery.value.trim()) {
    filteredNotifications.value = notifications.value
    return
  }
  const query = searchQuery.value.toLowerCase()
  filteredNotifications.value = notifications.value.filter(
    (n) => n.title.toLowerCase().includes(query) || n.content.toLowerCase().includes(query)
  )
}

function openCreateModal() {
  editingNotification.value = {
    id: null,
    title: '',
    content: '',
    type: 'info',
    status: 'draft'
  }
  showModal.value = true
}

function openEditModal(notification) {
  editingNotification.value = { ...notification }
  showModal.value = true
}

function saveNotification() {
  if (editingNotification.value.id) {
    const index = notifications.value.findIndex((n) => n.id === editingNotification.value.id)
    if (index !== -1) {
      notifications.value[index] = { ...editingNotification.value }
    }
  } else {
    editingNotification.value.id = Date.now()
    editingNotification.value.createTime = new Date()
      .toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
      .replace(/\//g, '-')
    notifications.value.unshift({ ...editingNotification.value })
  }
  showModal.value = false
  editingNotification.value = null
  filterNotifications()
}

function deleteNotification(id) {
  if (confirm('确定要删除这条通知吗？')) {
    notifications.value = notifications.value.filter((n) => n.id !== id)
    filterNotifications()
  }
}

function publishNotification(id) {
  const notification = notifications.value.find((n) => n.id === id)
  if (notification) {
    notification.status = 'published'
    notification.readCount = 0
  }
}

function getTypeConfig(type) {
  const configs = {
    important: { label: '重要', color: 'var(--danger)', bg: '#fef2f2' },
    info: { label: '通知', color: 'var(--primary)', bg: 'var(--primary-light)' },
    event: { label: '活动', color: 'var(--success)', bg: '#f0fdf4' },
    warning: { label: '警告', color: 'var(--warning)', bg: '#fffbeb' }
  }
  return configs[type] || configs.info
}
</script>

<template>
  <div class="teaching-notifications">
    <div class="page-header">
      <div>
        <h2>教学通知管理</h2>
        <p>发布和管理课程通知，及时传达重要信息</p>
      </div>
      <button class="btn btn--primary" @click="openCreateModal">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
          <path
            d="M12 5v14M5 12h14"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
          />
        </svg>
        新增通知
      </button>
    </div>

    <div class="search-bar">
      <div class="search-input-wrapper">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
          <path
            d="M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16M21 21l-4.35-4.35"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="搜索通知标题或内容..."
          class="search-input"
          @input="filterNotifications"
        />
      </div>
    </div>

    <div class="notifications-list">
      <article
        v-for="notification in filteredNotifications"
        :key="notification.id"
        class="card notification-card"
        :class="{ 'card--draft': notification.status === 'draft' }"
      >
        <div class="card-header">
          <div
            class="type-tag"
            :style="{
              color: getTypeConfig(notification.type).color,
              background: getTypeConfig(notification.type).bg
            }"
          >
            {{ getTypeConfig(notification.type).label }}
          </div>
          <div
            class="status-badge"
            :class="{ 'status--published': notification.status === 'published' }"
          >
            {{ notification.status === 'published' ? '已发布' : '草稿' }}
          </div>
        </div>

        <h3 class="notification-title">{{ notification.title }}</h3>
        <p class="notification-content">{{ notification.content }}</p>

        <div class="card-footer">
          <div class="meta-info">
            <span class="meta-item">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                <path
                  d="M12 8v4l3 3"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              {{ notification.createTime }}
            </span>
            <span class="meta-item">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                <path
                  d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              {{ notification.readCount }} 已读
            </span>
          </div>
          <div class="actions">
            <button
              v-if="notification.status === 'draft'"
              class="btn btn--ghost btn--small"
              @click="publishNotification(notification.id)"
            >
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                <path
                  d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              发布
            </button>
            <button class="btn btn--ghost btn--small" @click="openEditModal(notification)">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                <path
                  d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7M14.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              编辑
            </button>
            <button
              class="btn btn--ghost btn--danger btn--small"
              @click="deleteNotification(notification.id)"
            >
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                <path
                  d="M3 6h18M19 6v14c0 1.1-.9 2-2 2H7c-1.1 0-2-.9-2-2V6m3 0V4c0-1.1.9-2 2-2h4c1.1 0 2 .9 2 2v2"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              删除
            </button>
          </div>
        </div>
      </article>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ editingNotification?.id ? '编辑通知' : '新增通知' }}</h3>
          <button class="modal-close" @click="showModal = false">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path
                d="M18 6L6 18M6 6l12 12"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
              />
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">通知标题</label>
            <input
              v-model="editingNotification.title"
              type="text"
              class="form-input"
              placeholder="请输入通知标题"
            />
          </div>

          <div class="form-group">
            <label class="form-label">通知类型</label>
            <select v-model="editingNotification.type" class="form-select">
              <option value="important">重要</option>
              <option value="info">通知</option>
              <option value="event">活动</option>
              <option value="warning">警告</option>
            </select>
          </div>

          <div class="form-group">
            <label class="form-label">通知内容</label>
            <textarea
              v-model="editingNotification.content"
              class="form-textarea"
              placeholder="请输入通知内容"
              rows="4"
            ></textarea>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn--ghost" @click="showModal = false">取消</button>
          <button class="btn btn--primary" @click="saveNotification">
            {{ editingNotification?.id ? '保存修改' : '发布通知' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.teaching-notifications {
  max-width: 900px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 6px;
}

.page-header p {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
}

/* ========== Button Styles ========== */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
  border: none;
}

.btn:active {
  transform: scale(0.98);
}

.btn--primary {
  background: var(--primary);
  color: #fff;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.25);
}

.btn--primary:hover:not(:disabled) {
  background: var(--primary-hover);
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.35);
  transform: translateY(-0.5px);
}

.btn--ghost {
  background: transparent;
  color: var(--text-muted);
  border: 1px solid var(--border);
  padding: 8px 14px;
  font-size: 13px;
  font-weight: 500;
}

.btn--ghost:hover {
  background: var(--bg-hover);
  color: var(--text-secondary);
}

.btn--small {
  padding: 6px 10px;
  font-size: 12px;
  gap: 5px;
}

.btn--danger {
  border-color: var(--danger);
  color: var(--danger);
}

.btn--danger:hover {
  background: #fef2f2;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.2);
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
  box-shadow: none !important;
}

.search-bar {
  margin-bottom: 20px;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  color: var(--text-muted);
}

.search-input-wrapper:focus-within {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 14px;
  color: var(--text-primary);
  outline: none;
  font-family: inherit;
}

.search-input::placeholder {
  color: var(--text-muted);
}

.notifications-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ========== Card Styles ========== */
.card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border);
  padding: 20px;
  box-shadow: var(--shadow-sm);
  transition: all 0.25s ease;
}

.card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--border-light);
}

.card--draft {
  border-color: var(--border-light);
  opacity: 0.85;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.type-tag {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.status-badge {
  font-size: 12px;
  color: var(--text-muted);
  padding: 4px 10px;
  border-radius: 4px;
  background: var(--bg-hover);
}

.status--published {
  color: var(--success);
  background: #f0fdf4;
}

.notification-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.notification-content {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0 0 16px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
}

.meta-info {
  display: flex;
  gap: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-muted);
}

.actions {
  display: flex;
  gap: 8px;
}

/* ========== Modal Styles ========== */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-content {
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  width: 90%;
  max-width: 500px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  animation: slideUp 0.25s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-light);
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.modal-close {
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 6px;
  border-radius: var(--radius-sm);
  color: var(--text-muted);
  transition: all 0.15s;
}

.modal-close:hover {
  background: var(--bg-hover);
  color: var(--text-secondary);
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.form-input,
.form-select {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  font-size: 14px;
  color: var(--text-primary);
  background: var(--bg-page);
  outline: none;
  transition: all 0.2s;
  font-family: inherit;
}

.form-input:focus,
.form-select:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.form-textarea {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  font-size: 14px;
  color: var(--text-primary);
  background: var(--bg-page);
  outline: none;
  transition: all 0.2s;
  font-family: inherit;
  resize: vertical;
}

.form-textarea:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 18px 24px;
  border-top: 1px solid var(--border-light);
}
</style>
