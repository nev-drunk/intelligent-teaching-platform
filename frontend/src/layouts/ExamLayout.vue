<template>
  <div class="exam-layout">
    <main class="exam-layout__main">
      <RouterView />
    </main>
    <AiChatPanel />
    <AddQuestionModal v-model="exam.state.addModalVisible" />
    <ExamToast />
  </div>
</template>

<script setup>
import { provide, ref, onMounted } from 'vue'
import { RouterView } from 'vue-router'
import { useExamState } from '@/composables/useExamState'
import AiChatPanel from '@/components/exam/AiChatPanel.vue'
import AddQuestionModal from '@/components/exam/AddQuestionModal.vue'
import ExamToast from '@/components/exam/ExamToast.vue'
import '@/styles/exam.css'

const exam = useExamState()
const aiPanelOpen = ref(false)

provide('global', exam)
provide('aiPanelOpen', aiPanelOpen)

const { loadQuestions, loadPapers, loadClasses } = exam

onMounted(() => {
  loadQuestions()
  loadPapers()
  loadClasses()
})
</script>

<style scoped>
.exam-layout {
  display: flex;
  min-height: 100vh;
  background: var(--bg-page, #f8fafc);
}

.exam-layout__main {
  flex: 1;
  min-width: 0;
  overflow: auto;
  padding: var(--spacing-lg, 24px);
  background: var(--bg-page, #f8fafc);
}
</style>
