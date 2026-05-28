<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { fetchCarousel, fetchNotices } from '@/api/portal'
import { speakNotice, speakText } from '@/utils/tts'

const slides = ref([])
const notices = ref([])
const currentSlide = ref(0)
const loading = ref(true)
const speakingId = ref(null)
const isSpeaking = ref(false)

let slideTimer = null

const blueColors = ['#1e40af', '#1e40af', '#1e40af']

function normalizeColor(color) {
  const colorMap = {
    '#047857': '#2563eb',
    '#7c3aed': '#3b82f6',
    '#1a56db': '#1e40af',
    'rgb(4, 120, 87)': '#2563eb',
    'rgb(124, 58, 237)': '#3b82f6',
    'rgb(26, 86, 219)': '#1e40af'
  }
  return colorMap[color] || color
}

async function loadData() {
  loading.value = true
  try {
    const [carouselRes, noticesRes] = await Promise.all([fetchCarousel(), fetchNotices()])
    slides.value = (carouselRes.data || []).map((slide, index) => ({
      ...slide,
      color: blueColors[index % blueColors.length]
    }))
    notices.value = noticesRes.data || []
  } finally {
    loading.value = false
  }
}

function startCarousel() {
  if (slides.value.length <= 1) return
  slideTimer = setInterval(() => {
    currentSlide.value = (currentSlide.value + 1) % slides.value.length
  }, 4000)
}

function playNotice(notice) {
  if (isSpeaking.value && speakingId.value === notice.id) {
    window.speechSynthesis.cancel()
    isSpeaking.value = false
    speakingId.value = null
    return
  }
  window.speechSynthesis.cancel()
  speakingId.value = notice.id
  isSpeaking.value = true
  const text = `${notice.title}。${notice.content}`
  const utterance = new SpeechSynthesisUtterance(text)
  const voices = window.speechSynthesis.getVoices()
  if (voices.length > 0) {
    utterance.voice = voices.find((v) => v.lang.includes('zh')) || voices[0]
  }
  utterance.onend = () => {
    isSpeaking.value = false
    speakingId.value = null
  }
  utterance.onerror = () => {
    isSpeaking.value = false
    speakingId.value = null
  }
  window.speechSynthesis.speak(utterance)
}

function playSlide(slide) {
  speakText(`${slide.title}，${slide.subtitle}`)
}

onMounted(async () => {
  await loadData()
  startCarousel()
})

onUnmounted(() => {
  if (slideTimer) clearInterval(slideTimer)
})
</script>

<template>
  <div class="portal-public">
    <header class="portal-header">
      <h1>网站门户系统</h1>
      <p>通知公告 · 轮播展示 · TTS 语音播报</p>
      <router-link to="/login" class="admin-entry">教师后台登录</router-link>
    </header>

    <section v-if="loading" class="loading">加载中…</section>

    <template v-else>
      <section class="carousel" v-if="slides.length">
        <div
          v-for="(slide, index) in slides"
          :key="index"
          class="slide"
          :class="{ active: index === currentSlide }"
          :style="{ background: slide.color }"
          @click="playSlide(slide)"
        >
          <h2>{{ slide.title }}</h2>
          <p>{{ slide.subtitle }}</p>
        </div>
        <div class="dots">
          <button
            v-for="(_, index) in slides"
            :key="index"
            type="button"
            :class="{ active: index === currentSlide }"
            @click="currentSlide = index"
          />
        </div>
      </section>

      <section class="notices">
        <h3>通知公告</h3>
        <article v-for="n in notices" :key="n.id" class="notice-card">
          <h4>{{ n.title }}</h4>
          <p class="content">{{ n.content }}</p>
          <div class="notice-footer">
            <span class="meta">{{ n.createTime }}</span>
            <button
              type="button"
              class="speak-btn"
              :class="{ speaking: speakingId === n.id && isSpeaking }"
              @click="playNotice(n)"
            >
              <span v-if="speakingId === n.id && isSpeaking">⏹ 停止</span>
              <span v-else>🔊 朗读</span>
            </button>
          </div>
        </article>
        <p v-if="!notices.length" class="empty">暂无公告</p>
      </section>
    </template>
  </div>
</template>

<style scoped>
.portal-public {
  min-height: 100vh;
  background: var(--color-bg-page);
}

.portal-header {
  background: linear-gradient(
    135deg,
    #003a8c 0%,
    var(--color-primary) 50%,
    var(--color-primary-light) 100%
  );
  color: #ffffff;
  padding: 56px 24px;
  text-align: center;
  box-shadow: 0 8px 32px rgba(22, 93, 255, 0.15);
}

.portal-header h1 {
  margin: 0 0 12px;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #ffffff 0%, #3b82f6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.portal-header p {
  margin: 0;
  color: #60a5fa;
  font-size: 15px;
  letter-spacing: 1px;
  font-weight: 500;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
}

.admin-entry {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-top: 24px;
  padding: 12px 32px;
  background: #ffffff;
  color: #003a8c;
  border-radius: 24px;
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.admin-entry:hover {
  background: #f0f4ff;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 58, 140, 0.35);
}

.carousel {
  position: relative;
  max-width: 960px;
  margin: 40px auto;
  height: 260px;
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.slide {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  opacity: 0;
  transition: opacity 0.6s ease;
  cursor: pointer;
  padding: 32px;
  text-align: center;
}

.slide.active {
  opacity: 1;
  z-index: 1;
}

.slide h2 {
  margin: 0 0 12px;
  font-size: 26px;
  font-weight: 700;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.slide p {
  margin: 0;
  color: #e0e7ff;
  font-size: 15px;
  font-weight: 500;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
}

.dots {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 10px;
  z-index: 2;
}

.dots button {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.45);
  cursor: pointer;
  padding: 0;
  transition: all 0.25s ease;
}

.dots button:hover {
  background: rgba(255, 255, 255, 0.7);
  transform: scale(1.1);
}

.dots button.active {
  background: #ffffff;
  transform: scale(1.2);
}

.notices {
  max-width: 960px;
  margin: 0 auto 48px;
  padding: 0 24px;
}

.notices h3 {
  margin: 0 0 20px;
  color: var(--color-text-primary);
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.notices h3::before {
  content: '📢';
}

.notice-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: var(--shadow-card);
  cursor: pointer;
  transition: all 0.25s ease;
  border-left: 4px solid transparent;
}

.notice-card:hover {
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-2px);
  border-left-color: var(--color-primary);
}

.notice-card h4 {
  margin: 0 0 12px;
  color: var(--color-primary);
  font-size: 16px;
  font-weight: 600;
}

.content {
  margin: 0;
  color: var(--color-text-secondary);
  line-height: 1.7;
  font-size: 14px;
}

.notice-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14px;
}

.meta {
  font-size: 13px;
  color: var(--color-text-muted);
}

.speak-btn {
  padding: 8px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-bg-card);
  color: var(--color-text-secondary);
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
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
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

.empty,
.loading {
  text-align: center;
  padding: 60px;
  color: var(--color-text-muted);
}
</style>
