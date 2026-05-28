<template>
  <div>
    <div class="ai-backdrop" :class="{ show: panelOpen }" @click="close"></div>
    <div class="ai-chat-panel" :class="{ open: panelOpen }">
      <div class="ai-chat-header">
        <span class="ai-badge">AI</span>
        <strong>智能出题助手</strong>
        <button type="button" class="modal-close" style="margin-left: auto" @click="close">✕</button>
      </div>
      <div ref="chatBody" class="ai-chat-body">
        <div v-for="(msg, idx) in state.aiMessages" :key="idx" class="chat-msg" :class="msg.sender">
          {{ msg.text }}
        </div>
      </div>
      <div class="ai-chat-footer">
        <input
          v-model="aiInput"
          type="text"
          placeholder="描述你需要的题目..."
          @keydown.enter="sendMessage"
        />
        <button type="button" class="btn btn-orange btn-sm" @click="sendMessage">发送</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { inject, ref, nextTick, computed } from 'vue'

const global = inject('global')
const aiPanelOpenRef = inject('aiPanelOpen')
const { state, generateQuestionsByAi, showToast, loadQuestions } = global

const aiInput = ref('')
const chatBody = ref(null)

const panelOpen = computed({
  get: () => aiPanelOpenRef?.value ?? false,
  set: (v) => {
    if (aiPanelOpenRef) aiPanelOpenRef.value = v
  }
})

function close() {
  panelOpen.value = false
}

async function sendMessage() {
  const text = aiInput.value.trim()
  if (!text) return

  state.aiMessages.push({ sender: 'user', text })
  aiInput.value = ''
  await scrollBottom()

  try {
    showToast('🤖 AI 正在出题中...')
    const questions = await generateQuestionsByAi(text, 3)
    const responseText =
      `✅ 已为你生成 ${questions.length} 道题目：\n\n` +
      questions.map((q, i) => `${i + 1}. [${q.type || 'SINGLE'}] ${q.content || ''}`).join('\n\n') +
      '\n\n题目已保存到题库。'
    state.aiMessages.push({ sender: 'ai', text: responseText })
    await loadQuestions()
    await scrollBottom()
  } catch {
    state.aiMessages.push({ sender: 'ai', text: '❌ AI 出题失败，请稍后重试。' })
    await scrollBottom()
  }
}

async function scrollBottom() {
  await nextTick()
  if (chatBody.value) chatBody.value.scrollTop = chatBody.value.scrollHeight
}
</script>
