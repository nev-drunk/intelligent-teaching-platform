<script setup>
import { ref, onMounted } from 'vue'
import { speakText } from '@/utils/tts'

const comments = ref([
  {
    id: 1,
    studentName: '李明',
    courseName: '大模型应用与微调技术',
    taskTitle: '第一次编程作业',
    comment: '本次作业完成情况良好，对Transformer架构的理解比较深入，代码实现规范，希望继续保持对前沿技术的学习热情。',
    score: 92,
    createTime: '2026-05-20 14:30'
  },
  {
    id: 2,
    studentName: '王小红',
    courseName: '大模型应用与微调技术',
    taskTitle: '第二次编程作业',
    comment: '作业质量有明显提升，对提示词工程有了更深的理解。建议加强对上下文窗口机制的学习，注意作业提交的规范性。',
    score: 88,
    createTime: '2026-05-18 09:15'
  },
  {
    id: 3,
    studentName: '张伟',
    courseName: '大模型应用与微调技术',
    taskTitle: '第三次编程作业',
    comment: '本次作业完成认真，对微调技术的实验设计合理，超参数选择恰当。继续加油，期待看到更多创新性的实验结果。',
    score: 95,
    createTime: '2026-05-15 16:45'
  }
])

const selectedComment = ref(null)
const isSpeaking = ref(false)

function speakComment(comment) {
  if (isSpeaking.value && selectedComment.value === comment.id) {
    window.speechSynthesis.cancel()
    isSpeaking.value = false
    selectedComment.value = null
    return
  }
  if (isSpeaking.value) {
    window.speechSynthesis.cancel()
  }
  selectedComment.value = comment.id
  const text = `学生姓名：${comment.studentName}。课程：${comment.courseName}。作业标题：${comment.taskTitle}。评语：${comment.comment}。得分：${comment.score}分。`
  isSpeaking.value = true
  const utterance = new SpeechSynthesisUtterance(text)
  const voices = window.speechSynthesis.getVoices()
  if (voices.length > 0) {
    utterance.voice = voices.find(v => v.lang.includes('zh')) || voices[0]
  }
  utterance.onend = () => {
    isSpeaking.value = false
    selectedComment.value = null
  }
  utterance.onerror = () => {
    isSpeaking.value = false
    selectedComment.value = null
  }
  window.speechSynthesis.speak(utterance)
}

function speakAllComments() {
  if (isSpeaking.value) {
    window.speechSynthesis.cancel()
    isSpeaking.value = false
    selectedComment.value = null
    return
  }
  isSpeaking.value = true
  let index = 0
  const voices = window.speechSynthesis.getVoices()
  
  function speakNext() {
    if (index >= comments.value.length || !isSpeaking.value) {
      isSpeaking.value = false
      selectedComment.value = null
      return
    }
    const comment = comments.value[index]
    selectedComment.value = comment.id
    const text = `学生姓名：${comment.studentName}。课程：${comment.courseName}。作业标题：${comment.taskTitle}。评语：${comment.comment}。得分：${comment.score}分。`
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
      selectedComment.value = null
    }
    window.speechSynthesis.speak(utterance)
  }
  speakNext()
}

onMounted(() => {
})
</script>

<template>
  <div class="homework-comments">
    <div class="page-header">
      <div>
        <h2>学生作业评语</h2>
        <p>将评语转换为语音，方便学生收听学习反馈</p>
      </div>
      <button class="speak-all-btn" @click="speakAllComments" :disabled="isSpeaking">
        <span v-if="!isSpeaking">🔊 朗读全部评语</span>
        <span v-else>⏹ 停止朗读</span>
      </button>
    </div>

    <div class="comments-grid">
      <article
        v-for="comment in comments"
        :key="comment.id"
        class="comment-card"
        :class="{ active: selectedComment === comment.id }"
      >
        <div class="card-header">
          <div class="student-info">
            <span class="avatar">👨‍🎓</span>
            <div>
              <h3>{{ comment.studentName }}</h3>
              <p>{{ comment.courseName }}</p>
            </div>
          </div>
          <div class="score-badge">
            <span class="score-value">{{ comment.score }}</span>
            <span class="score-label">分</span>
          </div>
        </div>

        <div class="task-title">
          <span>📝 {{ comment.taskTitle }}</span>
        </div>

        <div class="comment-content">
          <p>{{ comment.comment }}</p>
        </div>

        <div class="card-footer">
          <span class="time">📅 {{ comment.createTime }}</span>
          <button
            class="speak-btn"
            :class="{ speaking: selectedComment === comment.id && isSpeaking }"
            @click="speakComment(comment)"
          >
            <span v-if="selectedComment === comment.id && isSpeaking">⏹ 停止</span>
            <span v-else>🔊 语音播报</span>
          </button>
        </div>
      </article>
    </div>

    <div class="tts-info">
      <p>💡 <strong>提示：</strong>点击"语音播报"按钮，系统将使用 TTS 语音合成技术将评语转换为自然流畅的语音，方便学生随时收听学习反馈。</p>
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
  margin-bottom: 28px;
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
  opacity: 0.7;
  cursor: not-allowed;
}

.comments-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.comment-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-xl);
  padding: 24px;
  box-shadow: var(--shadow-card);
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.comment-card:hover {
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-2px);
}

.comment-card.active {
  border-color: var(--color-tech-blue);
  box-shadow: 0 4px 20px rgba(37, 99, 235, 0.2);
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
  margin: 0 0 4px;
  color: var(--color-text-primary);
  font-size: 16px;
  font-weight: 600;
}

.student-info p {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 13px;
}

.score-badge {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  color: #ffffff;
  padding: 8px 16px;
  border-radius: 24px;
  font-weight: 700;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 60px;
  box-shadow: 0 4px 12px rgba(22, 93, 255, 0.3);
}

.score-value {
  font-size: 24px;
  line-height: 1;
}

.score-label {
  font-size: 11px;
  opacity: 0.9;
  margin-top: 2px;
}

.task-title {
  margin-bottom: 16px;
  padding: 12px 14px;
  background: var(--color-tech-blue-subtle);
  border-radius: var(--radius-md);
}

.task-title span {
  color: var(--color-tech-blue);
  font-size: 14px;
  font-weight: 500;
}

.comment-content {
  margin-bottom: 20px;
}

.comment-content p {
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