<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { fetchSubmissionList, teacherGradeSubmission } from '@/api/submission'

const auth = useAuthStore()
const submissions = ref([])
const loading = ref(true)
const errorMessage = ref('')
const selectedSubmission = ref(null)
const isSpeaking = ref(false)
const showModal = ref(false)
const editingComment = ref(null)

const filteredSubmissions = computed(() => submissions.value)

onMounted(async () => {
  await loadSubmissions()
})

async function loadSubmissions() {
  loading.value = true
  errorMessage.value = ''
  try {
    const res = await fetchSubmissionList({ page: 1, size: 100 })
    submissions.value = res.data?.records || []
  } catch (e) {
    errorMessage.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

function getScoreClass(score) {
  if (!score) return ''
  if (score >= 90) return 'score-excellent'
  if (score >= 70) return 'score-good'
  if (score >= 60) return 'score-pass'
  return 'score-fail'
}

function openCommentModal(submission) {
  editingComment.value = { id: submission.id, comment: submission.teacherComment || '', score: submission.teacherScore || 0 }
  showModal.value = true
}

async function saveComment() {
  if (!editingComment.value.comment.trim()) {
    errorMessage.value = '评语不能为空'
    return
  }
  errorMessage.value = ''
  try {
    await teacherGradeSubmission({
      submissionId: editingComment.value.id,
      teacherComment: editingComment.value.comment,
      teacherScore: editingComment.value.score
    })
    showModal.value = false
    editingComment.value = null
    await loadSubmissions()
  } catch (e) {
    errorMessage.value = e.message || '保存失败'
  }
}

function speakSubmission(submission) {
  if (submission.aiReviewVoiceUrl) {
    playReviewVoice(submission)
  }
}

async function speakAllSubmissions() {
  if (isSpeaking.value) { isSpeaking.value = false; return }
  isSpeaking.value = true
  for (const sub of submissions.value) {
    if (!isSpeaking.value) break
    if (sub.aiReviewVoiceUrl) {
      selectedSubmission.value = sub.id
      await new Promise(r => {
        const a = new Audio(import.meta.env.VITE_API_BASE_URL + '/' + sub.aiReviewVoiceUrl)
        a.onended = r; a.onerror = r; a.play().catch(r); setTimeout(r, 15000)
      })
    }
  }
  isSpeaking.value = false
  selectedSubmission.value = null
}

function formatTime(time) {
  if (!time) return '未知时间'
  if (Array.isArray(time)) {
    const [y, m, d, hh, mm] = time
    return y + '/' + String(m).padStart(2,'0') + '/' + String(d).padStart(2,'0') + ' ' + String(hh||0).padStart(2,'0') + ':' + String(mm||0).padStart(2,'0')
  }
  const d = new Date(time)
  if (isNaN(d.getTime())) return String(time)
  return d.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function playReviewVoice(submission) {
  const url = submission.aiReviewVoiceUrl
  if (!url) return
  const audio = new Audio(import.meta.env.VITE_API_BASE_URL + '/' + url)
  audio.play().catch(() => {})
}
</script>

<template>
  <div class="homework-comments">
    <div class="page-header">
      <div>
        <h2>学生作业评语</h2>
        <p>语音播报评语，方便学生收听学习反馈</p>
      </div>
      <button
        class="btn btn--primary"
        @click="speakAllSubmissions"
        :disabled="isSpeaking || submissions.length === 0"
      >
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
          <path
            d="M11 5L6 9H2v6h4l5 4V5z"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
          <path
            d="M15.54 8.46a5 5 0 0 1 0 7.07"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
          />
          <path
            d="M19.07 4.93a10 10 0 0 1 0 14.14"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
          />
        </svg>
        {{ isSpeaking ? '停止朗读' : '朗读全部' }}
      </button>
    </div>

    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="submissions.length === 0" class="empty">
      <p>暂无学生提交的作业</p>
    </div>

    <div v-else class="comments-grid">
      <article
        v-for="submission in filteredSubmissions"
        :key="submission.id"
        class="card comment-card"
        :class="{ active: selectedSubmission === submission.id }"
      >
        <div class="card-header">
          <div class="student-info">
            <span class="avatar">👨‍🎓</span>
            <div>
              <h3>{{ submission.studentName }}</h3>
              <p>{{ submission.courseName || '未知课程' }}</p>
            </div>
          </div>
          <div class="score-badge" :class="getScoreClass(submission.teacherScore)">
            <span class="score-value">{{ submission.teacherScore || '--' }}</span>
            <span class="score-label">分</span>
          </div>
        </div>

        <div class="task-title">
          <span>{{ submission.taskTitle || '未知作业' }}</span>
        </div>

        <div class="submission-content">
          <p class="content-label">提交内容：</p>
          <p>{{ submission.submitText || '无' }}</p>
        </div>

        <div class="comment-content">
          <p class="content-label">教师评语：</p>
          <p>{{ submission.teacherComment || '暂无评语' }}</p>
        </div>

        <div class="card-footer">
          <span class="time">{{ formatTime(submission.submitTime) }}</span>
          <div class="actions">
            <button
              v-if="submission.aiReviewVoiceUrl"
              class="btn btn--ghost"
              @click="playReviewVoice(submission)"
            >
              🔊 播放评语
            </button>
            <button
              class="btn btn--ghost"
              @click="editComment(submission)"
            >
              ✏️ 编辑
            </button>
            <button class="btn btn--ghost btn--primary" @click="openCommentModal(submission)">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                <path
                  d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7M14.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              撰写评语
            </button>
          </div>
        </div>
      </article>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>撰写评语</h3>
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
          <div class="submission-info">
            <p><strong>学生：</strong>{{ editingComment?.studentName }}</p>
            <p><strong>作业：</strong>{{ editingComment?.taskTitle }}</p>
            <p><strong>课程：</strong>{{ editingComment?.courseName }}</p>
          </div>

          <div class="form-group">
            <label class="form-label">评分</label>
            <input
              v-model.number="editingComment.score"
              type="number"
              min="0"
              max="100"
              class="form-input"
              placeholder="请输入评分（0-100）"
            />
          </div>

          <div class="form-group">
            <label class="form-label">评语内容</label>
            <textarea
              v-model="editingComment.comment"
              class="form-textarea"
              placeholder="请输入评语内容"
              rows="4"
            ></textarea>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn--ghost" @click="showModal = false">取消</button>
          <button class="btn btn--primary" @click="saveComment">保存评语</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.homework-comments {
  max-width: 1200px;
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

.error-message {
  padding: 12px 16px;
  background: #fef2f2;
  border: 1px solid var(--danger);
  border-radius: var(--radius-md);
  color: var(--danger);
  font-size: 14px;
  margin-bottom: 20px;
}

.loading,
.empty {
  padding: 40px;
  text-align: center;
  color: var(--text-muted);
}

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

.btn--primary {
  background: var(--primary);
  color: #fff;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.25);
}

.btn--primary:hover:not(:disabled) {
  background: var(--primary-hover);
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.35);
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

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.comments-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
}

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

.comment-card.active {
  border-color: var(--primary);
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.student-info {
  display: flex;
  gap: 12px;
}

.avatar {
  font-size: 36px;
}

.student-info h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 4px;
}

.student-info p {
  font-size: 13px;
  color: var(--text-muted);
  margin: 0;
}

.score-badge {
  padding: 8px 14px;
  border-radius: 20px;
  font-weight: 700;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 65px;
}

.score--excellent {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: #fff;
}

.score--good {
  background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
  color: #fff;
}

.score--pass {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: #fff;
}

.score--fail {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: #fff;
}

.score--pending {
  background: var(--bg-hover);
  color: var(--text-muted);
}

.score-value {
  font-size: 22px;
  line-height: 1;
}

.score-label {
  font-size: 11px;
  opacity: 0.9;
  margin-top: 2px;
}

.task-title {
  padding: 12px 14px;
  background: var(--primary-light);
  border-radius: var(--radius-md);
  margin-bottom: 16px;
}

.task-title span {
  color: var(--primary);
  font-size: 14px;
  font-weight: 500;
}

.submission-content,
.comment-content {
  margin-bottom: 16px;
}

.content-label {
  font-size: 12px;
  color: var(--text-muted);
  margin: 0 0 4px;
  font-weight: 600;
}

.submission-content p,
.comment-content p {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.7;
  margin: 0;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
}

.time {
  font-size: 13px;
  color: var(--text-muted);
}

.actions {
  display: flex;
  gap: 8px;
}

/* Modal Styles */
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
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  width: 90%;
  max-width: 500px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
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

.submission-info {
  padding: 12px 14px;
  background: var(--primary-light);
  border-radius: var(--radius-md);
  margin-bottom: 20px;
}

.submission-info p {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 4px 0;
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
}

.form-textarea {
  resize: vertical;
}

.form-input:focus,
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
