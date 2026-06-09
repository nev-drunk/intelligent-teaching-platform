<script setup>
import { onMounted, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { fetchCarousel, fetchNotices, publishNotice, deleteNotice } from '@/api/portal'
import { speakNotice } from '@/utils/tts'

const auth = useAuthStore()
const slides = ref([])
const notices = ref([])
const form = ref({ title: '', content: '' })
const publishing = ref(false)
const msg = ref('')
const speakingId = ref(null)

const blueColors = ['#2563eb', '#2563eb', '#2563eb']

async function load() {
  const [c, n] = await Promise.all([fetchCarousel(), fetchNotices()])
  slides.value = (c.data || []).map((slide, index) => ({
    ...slide,
    color: blueColors[index % blueColors.length]
  }))
  notices.value = n.data || []
}

async function handlePublish() {
  if (!form.value.title.trim() || !form.value.content.trim()) {
    msg.value = '请填写标题和正文'
    return
  }
  publishing.value = true
  msg.value = ''
  try {
    const res = await publishNotice({
      teacherId: Number(auth.teacherId) || 1,
      title: form.value.title,
      content: form.value.content
    })
    // 播放服务器生成的 TTS 音频
    const ttsUrl = res?.ttsAudioUrl
    if (ttsUrl && !ttsUrl.startsWith('client://')) {
      const audio = new Audio(import.meta.env.VITE_API_BASE_URL + '/' + ttsUrl)
      audio.play().catch(() => {})
    }
    form.value = { title: '', content: '' }
    msg.value = '发布成功（DashScope TTS 语音已生成）'
    await load()
  } catch (e) {
    msg.value = e.message
  } finally {
    publishing.value = false
  }
}

const audioRef = ref(null)

function replay(notice) {
  // 如果正在播放同一条，则停止
  if (speakingId.value === notice.id) {
    if (audioRef.value) {
      audioRef.value.pause()
      audioRef.value = null
    }
    window.speechSynthesis.cancel()
    speakingId.value = null
    return
  }

  // 停止之前的播放
  if (audioRef.value) {
    audioRef.value.pause()
    audioRef.value = null
  }
  window.speechSynthesis.cancel()

  // 优先使用服务器生成的 TTS 音频文件
  const ttsUrl = notice.ttsAudioUrl
  if (ttsUrl && ttsUrl !== 'client://speechSynthesis' && !ttsUrl.startsWith('client://')) {
    const audio = new Audio(import.meta.env.VITE_API_BASE_URL + '/' + ttsUrl)
    speakingId.value = notice.id
    audio.onended = () => {
      speakingId.value = null
      audioRef.value = null
    }
    audio.onerror = () => {
      console.warn('服务器TTS音频播放失败，回退到浏览器语音')
      fallbackBrowserSpeech(notice)
    }
    audio.play()
    audioRef.value = audio
    return
  }

  // 回退到浏览器内置语音合成
  fallbackBrowserSpeech(notice)
}

function fallbackBrowserSpeech(notice) {
  speakingId.value = notice.id
  const text = `${notice.title}。${notice.content}`
  const utterance = new SpeechSynthesisUtterance(text)
  const voices = window.speechSynthesis.getVoices()
  if (voices.length > 0) {
    utterance.voice = voices.find(v => v.lang.includes('zh')) || voices[0]
  }
  utterance.onend = () => {
    speakingId.value = null
  }
  utterance.onerror = () => {
    speakingId.value = null
  }
  window.speechSynthesis.speak(utterance)
}

async function handleDelete(notice) {
  if (!confirm(`确定删除公告「${notice.title}」？`)) return
  try {
    await deleteNotice(notice.id)
    msg.value = '删除成功'
    await load()
  } catch (e) {
    msg.value = e.message
  }
}

onMounted(load)
</script>

<template>
  <div class="portal-manage">
    <div class="preview-carousel">
      <div
        v-for="(slide, i) in slides"
        :key="i"
        class="mini-slide"
        :style="{ '--accent-color': slide.color }"
      >
        <strong>{{ slide.title }}</strong>
        <span>{{ slide.subtitle }}</span>
      </div>
    </div>

    <div class="grid">
      <section class="panel publish">
        <h3>发布门户通知</h3>
        <p class="tts-tip">💥 TTS：发布后将自动朗读标题与正文（speechSynthesis）</p>
        <input v-model="form.title" type="text" placeholder="通知标题" />
        <textarea v-model="form.content" rows="5" placeholder="通知正文公告" />
        <button type="button" class="btn-publish" :disabled="publishing" @click="handlePublish">
          {{ publishing ? '发布中…' : '发布公告并语音播报' }}
        </button>
        <p v-if="msg" class="msg">{{ msg }}</p>
      </section>

      <section class="panel list">
        <h3>已发布公告</h3>
        <ul>
          <li v-for="n in notices" :key="n.id">
            <div>
              <strong>{{ n.title }}</strong>
              <p>{{ n.content }}</p>
              <small>{{ n.createTime }}</small>
            </div>
            <div class="actions">
              <button type="button" class="btn-outline btn-replay" :class="{ speaking: speakingId === n.id }" @click="replay(n)">
                <span v-if="speakingId === n.id">⏹ 停止</span>
                <span v-else>🔊 朗读</span>
              </button>
              <button type="button" class="btn-outline btn-delete" @click="handleDelete(n)">🗑️ 删除</button>
            </div>
          </li>
        </ul>
        <p v-if="!notices.length" class="empty">暂无公告，请在左侧发布</p>
      </section>
    </div>
  </div>
</template>

<style scoped>
.preview-carousel {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
  flex-wrap: wrap;
}

.mini-slide {
  flex: 1;
  min-width: 220px;
  padding: 20px;
  border-radius: var(--radius-lg);
  background: #ffffff;
  border-left: 4px solid var(--accent-color, #2563eb);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.03);
}

.mini-slide strong {
  display: block;
  margin-bottom: 8px;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-inverse, #1f2937);
}

.mini-slide span {
  font-size: 14px;
  color: var(--text-tertiary, #9ca3af);
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 28px;
}

@media (max-width: 960px) {
  .grid {
    grid-template-columns: 1fr;
  }
}

.panel {
  background: var(--color-bg-card);
  border-radius: var(--radius-xl);
  padding: 28px;
  box-shadow: var(--shadow-card);
}

.panel h3 {
  margin: 0 0 16px;
  color: var(--color-text-primary);
  font-size: 17px;
  font-weight: 600;
}

.tts-tip {
  font-size: 13px;
  color: var(--color-primary);
  margin: 0 0 20px;
  padding: 12px 14px;
  background: rgba(22, 93, 255, 0.08);
  border-radius: var(--radius-sm);
  border-left: 3px solid var(--color-primary);
}

.panel input,
.panel textarea {
  width: 100%;
  box-sizing: border-box;
  margin-bottom: 16px;
  padding: 13px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 14px;
  transition: all 0.25s ease;
}

.panel input:focus,
.panel textarea:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(22, 93, 255, 0.1);
}

.panel input::placeholder,
.panel textarea::placeholder {
  color: var(--color-text-muted);
}

.btn-publish {
  width: 100%;
  padding: 13px 24px;
  background: var(--blue-primary, #2563eb);
  color: #ffffff;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.25s ease;
}

.btn-publish:hover:not(:disabled) {
  background: var(--blue-dark, #1d4ed8);
}

.btn-publish:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.msg {
  margin-top: 16px;
  font-size: 13px;
  color: var(--color-success);
  padding: 12px 14px;
  background: rgba(0, 180, 42, 0.08);
  border-radius: var(--radius-sm);
  border-left: 3px solid var(--color-success);
}

.list ul {
  list-style: none;
  margin: 0;
  padding: 0;
}

.list li {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding: 18px 0;
  border-bottom: 1px solid var(--color-border-light);
  transition: background-color 0.15s ease;
}

.list li:hover {
  background: var(--color-bg-hover);
  margin: 0 -12px;
  padding-left: 12px;
  padding-right: 12px;
  border-radius: var(--radius-sm);
}

.list li:last-child {
  border-bottom: none;
}

.list li strong {
  color: var(--color-primary);
  font-size: 15px;
}

.list li p {
  margin: 8px 0;
  color: var(--color-text-secondary);
  font-size: 14px;
  line-height: 1.6;
}

.list li small {
  color: var(--color-text-muted);
  font-size: 12px;
}

.actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.btn-outline {
  padding: 8px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-text-secondary);
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.25s ease;
}

.btn-replay:hover {
  background: var(--color-bg-hover);
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.btn-replay.speaking {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: #ffffff;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.btn-delete:hover {
  background: rgba(239, 68, 68, 0.08);
  border-color: var(--color-danger);
  color: var(--color-danger);
}

.empty {
  color: var(--color-text-muted);
  font-size: 14px;
  padding: 32px;
  text-align: center;
}
</style>
