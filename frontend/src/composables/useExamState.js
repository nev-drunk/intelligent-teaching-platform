import { reactive, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import * as questionApi from '@/api/question'
import * as paperApi from '@/api/paper'
import * as classApi from '@/api/class'

const state = reactive({
  currentView: 'question-bank',
  selectedQuestionIds: new Set(),
  paperName: '',
  paperScore: 100,
  paperCourseId: 1,
  paperClassId: null,
  aiMessages: [
    {
      sender: 'ai',
      text: '👋 你好！我是 AI 出题助手。\n请描述你需要的题目，例如：\n「帮我出 3 道高一数学函数选择题」'
    }
  ],
  toastMessage: '',
  toastVisible: false,
  questions: [],
  papers: [],
  classes: [],
  loading: false,
  editingQuestion: null,
  addModalVisible: false
})

export function useExamState() {
  const auth = useAuthStore()

  const loadQuestions = async (courseId = state.paperCourseId) => {
    state.loading = true
    try {
      state.questions = await questionApi.listQuestions(courseId)
    } catch (error) {
      console.error('加载题目失败:', error)
      showToast('❌ 加载题目失败')
    } finally {
      state.loading = false
    }
  }

  const loadPapers = async () => {
    state.loading = true
    try {
      state.papers = await paperApi.listPapers()
    } catch (error) {
      console.error('加载试卷失败:', error)
      showToast('❌ 加载试卷失败')
    } finally {
      state.loading = false
    }
  }

  const loadClasses = async () => {
    try {
      const teacherId = auth.teacherId ? Number(auth.teacherId) : null
      state.classes = await classApi.listClasses(teacherId)
    } catch (error) {
      console.error('加载班级失败:', error)
      state.classes = []
    }
  }

  const saveQuestion = async (question) => {
    const saved = await questionApi.saveQuestion(question)
    const index = state.questions.findIndex((q) => q.id === saved.id)
    if (index !== -1) {
      state.questions[index] = saved
    } else {
      state.questions.unshift(saved)
    }
    return saved
  }

  const deleteQuestion = async (id) => {
    if (!confirm('确定要删除这道题目吗？')) return
    await questionApi.deleteQuestion(id)
    state.questions = state.questions.filter((q) => q.id !== id)
    showToast('✅ 题目已删除')
  }

  const generateQuestionsByAi = async (prompt, count = 3) => {
    state.loading = true
    try {
      const result = await questionApi.aiGenerate({
        prompt,
        count,
        courseId: state.paperCourseId
      })
      const list = Array.isArray(result) ? result : [result]
      state.questions.unshift(...list)
      return list
    } finally {
      state.loading = false
    }
  }

  const publishPaper = async (paperData) => {
    const teacherId = auth.teacherId ? Number(auth.teacherId) : 1
    const classId = paperData.classId || state.paperClassId

    if (classId && paperData.items?.length) {
      const res = await paperApi.createAndPublishPaper({
        courseId: paperData.courseId || state.paperCourseId,
        title: paperData.title,
        classId,
        teacherId,
        items: paperData.items.map((it) => ({
          questionId: it.questionId,
          score: it.score
        }))
      })
      await loadPapers()
      return res.data
    }

    const saved = await paperApi.savePaper({
      courseId: paperData.courseId || state.paperCourseId,
      title: paperData.title,
      totalScore: paperData.totalScore,
      teacherId,
      classId,
      questions: paperData.questions
    })
    await loadPapers()
    return saved
  }

  const deletePaperFromState = async (id) => {
    await paperApi.deletePaper(id)
    state.papers = state.papers.filter((p) => p.id !== id)
  }

  const toggleQuestionSelection = (id) => {
    const newSet = new Set(state.selectedQuestionIds)
    if (newSet.has(id)) newSet.delete(id)
    else newSet.add(id)
    state.selectedQuestionIds = newSet
  }

  const removeQuestionFromSelection = (id) => {
    const newSet = new Set(state.selectedQuestionIds)
    newSet.delete(id)
    state.selectedQuestionIds = newSet
  }

  const clearSelections = () => {
    state.selectedQuestionIds = new Set()
    state.paperName = ''
    state.paperScore = 100
    state.paperClassId = null
  }

  const showToast = (msg, duration = 2500) => {
    state.toastMessage = msg
    state.toastVisible = true
    clearTimeout(window.__examToastTimer)
    window.__examToastTimer = setTimeout(() => {
      state.toastVisible = false
    }, duration)
  }

  const canPublish = computed(() => {
    return state.selectedQuestionIds.size > 0 && state.paperName.trim().length > 0
  })

  return {
    state,
    auth,
    toggleQuestionSelection,
    removeQuestionFromSelection,
    clearSelections,
    showToast,
    canPublish,
    loadQuestions,
    loadPapers,
    loadClasses,
    saveQuestion,
    deleteQuestion,
    generateQuestionsByAi,
    publishPaper,
    deletePaperFromState
  }
}
