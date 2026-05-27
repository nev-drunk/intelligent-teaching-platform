<script setup>
import { ref, onMounted } from 'vue'
import { speakText } from '@/utils/tts'

const notifications = ref([
  {
    id: 1,
    title: '关于期中考试安排的通知',
    type: '考试安排',
    content: '各位同学，期中考试将于6月15日上午9点在教学楼A座进行，考试科目为《大模型应用与微调技术》。请同学们提前做好复习，认真备考。考试形式为在线机考，请确保网络环境稳定。',
    createTime: '2026-05-25 10:00',
    urgent: true
  },
  {
    id: 2,
    title: '课程作业提交提醒',
    type: '作业通知',
    content: '第三次编程作业提交截止时间为6月1日23:59。请同学们尽快完成作业并按时提交。作业要求已在课程平台发布，如有疑问可在问题中心留言。',
    createTime: '2026-05-24 14:30',
    urgent: false
  },
  {
    id: 3,
    title: '实训项目开课通知',
    type: '实训通知',
    content: '下周开始我们将进入实训环节，实训项目为基于大模型的智能问答系统开发。请同学们提前准备好开发环境，熟悉相关工具链的使用。实训地点为计算机实验中心302室。',
    createTime: '2026-05-23 09:00',
    urgent: false
  },
  {
    id: 4,
    title: '端午节放假调课通知',
    type: '假期通知',
    content: '端午节假期为5月28日至5月30日，期间课程顺延。5月27日（周六）正常上课，请同学们注意调整作息时间，按时参加课程。',
    createTime: '2026-05-22 16:00',
    urgent: false
  }
])

const selectedNotification = ref(null)
const isSpeaking = ref(false)
const filterType = ref('全部')
const showAddModal = ref(false)
const newNotification = ref({
  title: '',
  content: '',
  type: '考试安排',
  urgent: false
})

const typeOptions = ['全部', '考试安排', '作业通知', '实训通知', '假期通知']
const notificationTypes = ['考试安排', '作业通知', '实训通知', '假期通知']

const filteredNotifications = ref([])

function updateFiltered() {
  if (filterType.value === '全部') {
    filteredNotifications.value = notifications.value
  } else {
    filteredNotifications.value = notifications.value.filter(n => n.type === filterType.value)
  }
}

function speakNotification(notification) {
  if (isSpeaking.value && selectedNotification.value === notification.id) {
    window.speechSynthesis.cancel()
    isSpeaking.value = false
    selectedNotification.value = null
    return
  }
  if (isSpeaking.value) {
    window.speechSynthesis.cancel()
  }
  selectedNotification.value = notification.id
  const text = `通知标题：${notification.title}。类型：${notification.type}。内容：${notification.content}。发布时间：${notification.createTime}。`
  isSpeaking.value = true
  const utterance = new SpeechSynthesisUtterance(text)
  const voices = window.speechSynthesis.getVoices()
  if (voices.length > 0) {
    utterance.voice = voices.find(v => v.lang.includes('zh')) || voices[0]
  }
  utterance.onend = () => {
    isSpeaking.value = false
    selectedNotification.value = null
  }
  utterance.onerror = () => {
    isSpeaking.value = false
    selectedNotification.value = null
  }
  window.speechSynthesis.speak(utterance)
}

function speakAll() {
  if (isSpeaking.value) {
    window.speechSynthesis.cancel()
    isSpeaking.value = false
    return
  }
  isSpeaking.value = true
  let index = 0
  const voices = window.speechSynthesis.getVoices()
  
  function speakNext() {
    if (index >= filteredNotifications.value.length || !isSpeaking.value) {
      isSpeaking.value = false
      return
    }
    const notification = filteredNotifications.value[index]
    const text = `通知标题：${notification.title}。类型：${notification.type}。内容：${notification.content}。发布时间：${notification.createTime}。`
    const utterance = new SpeechSynthesisUtterance(text)
    if (voices.length > 0) {
      utterance.voice = voices.find(v => v.lang.includes('zh')) || voices[0]
    }
    utterance.onend = () => {
      if (isSpeaking.value) {
        index++
        speakNext()
      }
    }
    utterance.onerror = () => {
      isSpeaking.value = false
    }
    window.speechSynthesis.speak(utterance)
  }
  speakNext()
}

onMounted(() => {
  updateFiltered()
})

function setFilter(type) {
  filterType.value = type
  updateFiltered()
}

function deleteNotification(id) {
  if (!confirm('确定删除这条通知吗？')) return
  const index = notifications.value.findIndex(n => n.id === id)
  if (index !== -1) {
    notifications.value.splice(index, 1)
    updateFiltered()
    if (selectedNotification.value === id) {
      window.speechSynthesis.cancel()
      selectedNotification.value = null
      isSpeaking.value = false
    }
  }
}

function openAddModal() {
  newNotification.value = {
    title: '',
    content: '',
    type: '考试安排',
    urgent: false
  }
  showAddModal.value = true
}

function closeAddModal() {
  showAddModal.value = false
}

function addNotification() {
  if (!newNotification.value.title.trim() || !newNotification.value.content.trim()) {
    alert('请填写标题和内容')
    return
  }
  const notification = {
    id: Date.now(),
    title: newNotification.value.title,
    content: newNotification.value.content,
    type: newNotification.value.type,
    urgent: newNotification.value.urgent,
    createTime: new Date().toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    }).replace(/\//g, '-')
  }
  notifications.value.unshift(notification)
  updateFiltered()
  closeAddModal()
}
</script>

<template>
  <div class="teaching-notifications">
    <div class="page-header">
      <div>
        <h2>教学通知</h2>
        <p>将通知内容转换为语音，方便学生收听</p>
      </div>
      <div class="header-actions">
        <button class="speak-all-btn" @click="speakAll" :disabled="isSpeaking || filteredNotifications.length === 0">
          <span v-if="!isSpeaking">🔊 朗读全部通知</span>
          <span v-else>⏹ 停止朗读</span>
        </button>
        <button class="add-btn" @click="openAddModal">
          ➕ 新增通知
        </button>
      </div>
    </div>

    <div class="filter-tabs">
      <button
        v-for="type in typeOptions"
        :key="type"
        class="filter-tab"
        :class="{ active: filterType === type }"
        @click="setFilter(type)"
      >
        {{ type }}
      </button>
    </div>

    <div class="notifications-list">
      <article
        v-for="notification in filteredNotifications"
        :key="notification.id"
        class="notification-card"
        :class="{ urgent: notification.urgent, active: selectedNotification === notification.id }"
      >
        <div class="card-header">
          <div class="title-row">
            <span v-if="notification.urgent" class="urgent-badge">紧急</span>
            <h3>{{ notification.title }}</h3>
          </div>
          <span class="type-tag">{{ notification.type }}</span>
        </div>

        <div class="notification-content">
          <p>{{ notification.content }}</p>
        </div>

        <div class="card-footer">
          <span class="time">📅 {{ notification.createTime }}</span>
          <div class="card-actions">
            <button
              class="speak-btn"
              :class="{ speaking: selectedNotification === notification.id && isSpeaking }"
              @click="speakNotification(notification)"
            >
              <span v-if="selectedNotification === notification.id && isSpeaking">⏹ 停止</span>
              <span v-else>🔊 语音播报</span>
            </button>
            <button
              class="delete-btn"
              @click="deleteNotification(notification.id)"
            >
              🗑️ 删除
            </button>
          </div>
        </div>
      </article>

      <div v-if="filteredNotifications.length === 0" class="empty-state">
        <span class="empty-icon">📭</span>
        <p>暂无相关通知</p>
      </div>
    </div>

    <div v-if="showAddModal" class="modal-overlay" @click.self="closeAddModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>新增通知</h3>
          <button class="close-btn" @click="closeAddModal">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>标题</label>
            <input
              v-model="newNotification.title"
              type="text"
              placeholder="请输入通知标题"
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label>类型</label>
            <select v-model="newNotification.type" class="form-select">
              <option v-for="type in notificationTypes" :key="type" :value="type">
                {{ type }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>内容</label>
            <textarea
              v-model="newNotification.content"
              placeholder="请输入通知内容"
              class="form-textarea"
              rows="4"
            ></textarea>
          </div>
          <div class="form-group">
            <label class="checkbox-label">
              <input v-model="newNotification.urgent" type="checkbox" />
              <span>紧急通知</span>
            </label>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeAddModal">取消</button>
          <button class="btn-submit" @click="addNotification">发布通知</button>
        </div>
      </div>
    </div>

    <div class="tts-info">
      <p>💡 <strong>提示：</strong>点击"语音播报"按钮，系统将使用 TTS 语音合成技术将通知内容转换为自然流畅的语音，方便学生随时收听重要的教学通知。</p>
    </div>
  </div>
</template>

<style scoped>
.teaching-notifications {
  max-width: 1000px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 6px;
  color: var(--color-text-primary);
  font-size: 20px;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 14px;
}

.speak-all-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, var(--color-tech-blue) 0%, var(--color-tech-blue-dark) 100%);
  color: #ffffff;
  border: none;
  border-radius: var(--radius-md);
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.speak-all-btn:hover:not(:disabled) {
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.4);
  transform: translateY(-1px);
}

.speak-all-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.filter-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.filter-tab {
  padding: 8px 18px;
  background: var(--color-bg-card);
  color: var(--color-text-secondary);
  border: 1px solid var(--color-border);
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-tab:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.filter-tab.active {
  background: var(--color-primary) !important;
  color: #ffffff !important;
  border-color: var(--color-primary) !important;
}

.notifications-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.notification-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-xl);
  padding: 24px;
  box-shadow: var(--shadow-card);
  transition: all 0.3s ease;
}

.notification-card:hover {
  box-shadow: var(--shadow-card-hover);
  transform: translateX(4px);
}

.notification-card.urgent {
  background: linear-gradient(90deg, var(--color-tech-blue-subtle) 0%, var(--color-bg-card) 100%);
}

.notification-card.active {
  border-color: var(--color-tech-blue);
  box-shadow: 0 4px 20px rgba(37, 99, 235, 0.2);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.urgent-badge {
  background: var(--color-tech-blue);
  color: #ffffff;
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
}

.card-header h3 {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 16px;
  font-weight: 600;
}

.type-tag {
  padding: 4px 12px;
  background: var(--color-tech-blue-subtle);
  color: var(--color-tech-blue);
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  flex-shrink: 0;
}

.notification-content {
  margin-bottom: 20px;
}

.notification-content p {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 14px;
  line-height: 1.7;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid var(--color-border-light);
}

.time {
  color: var(--color-text-muted);
  font-size: 13px;
}

.speak-btn {
  padding: 8px 16px;
  background: var(--color-bg-card);
  color: var(--color-text-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
}

.speak-btn:hover,
.speak-btn.speaking {
  background: var(--color-primary) !important;
  border-color: var(--color-primary) !important;
  color: #ffffff !important;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.add-btn {
  padding: 12px 20px;
  background: var(--color-bg-card);
  color: var(--color-text-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-weight: 500;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.add-btn:hover {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: #ffffff;
}

.card-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.delete-btn {
  padding: 8px 14px;
  background: var(--color-bg-card);
  color: var(--color-text-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
}

.delete-btn:hover {
  background: var(--color-danger);
  border-color: var(--color-danger);
  color: #ffffff;
}

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
}

.modal-content {
  background: var(--color-bg-card);
  border-radius: var(--radius-xl);
  width: 90%;
  max-width: 500px;
  box-shadow: var(--shadow-card-hover);
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--color-border);
}

.modal-header h3 {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 18px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  color: var(--color-text-muted);
  font-size: 20px;
  cursor: pointer;
  padding: 4px;
  border-radius: var(--radius-sm);
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: var(--color-bg-hover);
  color: var(--color-text-primary);
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: var(--color-text-primary);
  font-size: 14px;
  font-weight: 500;
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 14px;
  background: var(--color-bg-card);
  color: var(--color-text-primary);
  transition: all 0.2s ease;
  box-sizing: border-box;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(22, 93, 255, 0.1);
}

.form-textarea {
  resize: vertical;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.checkbox-label input {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid var(--color-border);
  background: var(--color-bg-page);
}

.btn-cancel {
  padding: 10px 20px;
  background: var(--color-bg-card);
  color: var(--color-text-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
}

.btn-cancel:hover {
  background: var(--color-bg-hover);
}

.btn-submit {
  padding: 10px 24px;
  background: var(--color-primary);
  color: #ffffff;
  border: none;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
}

.btn-submit:hover {
  background: var(--color-primary-dark);
  transform: translateY(-1px);
}

.empty-state {
  text-align: center;
  padding: 48px;
  background: var(--color-bg-card);
  border-radius: var(--radius-xl);
}

.empty-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 16px;
}

.empty-state p {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 15px;
}

.tts-info {
  background: var(--color-tech-blue-subtle);
  border-radius: var(--radius-lg);
  padding: 20px 24px;
  border-left: 4px solid var(--color-tech-blue);
}

.tts-info p {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 14px;
  line-height: 1.6;
}

.tts-info strong {
  color: var(--color-tech-blue);
}
</style>