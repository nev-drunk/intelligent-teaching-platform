<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Search, RefreshRight, View } from '@element-plus/icons-vue'
import questionnaireApi from '@/api/questionnaire'
// 💥 引入课程 API
import courseApi from '@/api/course'

// ========== 数据 ==========
const questionnaireList = ref([])
const loading = ref(false)
// 💥 存储动态课程列表的响应式变量
const courseList = ref([])

// ========== 搜索 ==========
const searchText = ref('')
const filteredList = computed(() => {
  if (!searchText.value) return questionnaireList.value
  return questionnaireList.value.filter((q) =>
    q.title.toLowerCase().includes(searchText.value.toLowerCase())
  )
})

// ========== 统计 ==========
const totalCount = computed(() => questionnaireList.value.length)
const activeCount = computed(() => questionnaireList.value.filter((q) => q.status === 1).length)
const closedCount = computed(() => questionnaireList.value.filter((q) => q.status === 0).length)

// ========== 加载列表 ==========
async function fetchList() {
  loading.value = true
  try {
    const res = await questionnaireApi.list()
    questionnaireList.value = res.data || []
  } catch (e) {
    console.error('获取问卷列表失败', e)
  } finally {
    loading.value = false
  }
}

// 💥 精准对接你的 courseApi.list() 返回值结构
async function fetchCourseList() {
  try {
    const res = await courseApi.list(1) // 传入教师ID 1
    // 适配：如果你的 request.js 没剥离业务 data 字段，真实数组在 res.data 中
    // 如果全局已经剥离了最外层直接返回了数组，则直接使用 res
    if (res && res.code === 200) {
      courseList.value = res.data || []
    } else {
      courseList.value = res || []
    }
    console.log('课程列表动态加载成功：', courseList.value)
  } catch (e) {
    console.error('动态获取课程列表失败', e)
  }
}

// 初始化时，问卷列表和课程下拉框数据一起拉取
onMounted(() => {
  fetchList()
  fetchCourseList()
})

// ========== 标准化问卷模板（固定 10 道题） ==========
const standardQuestions = [
  { id: 'q1', question: '老师授课内容是否清晰易懂？', type: 'RATING', maxScore: 10 },
  { id: 'q2', question: '课程重点难点是否讲解透彻？', type: 'RATING', maxScore: 10 },
  { id: 'q3', question: '课堂互动是否充分？', type: 'RATING', maxScore: 10 },
  { id: 'q4', question: '作业量是否合理？', type: 'RATING', maxScore: 10 },
  { id: 'q5', question: '作业批改是否及时？', type: 'RATING', maxScore: 10 },
  { id: 'q6', question: '老师教学态度是否认真负责？', type: 'RATING', maxScore: 10 },
  { id: 'q7', question: '课程难度是否适中？', type: 'RATING', maxScore: 10 },
  { id: 'q8', question: '课程内容是否实用？', type: 'RATING', maxScore: 10 },
  { id: 'q9', question: '你对本课程的整体满意度？', type: 'RATING', maxScore: 10 },
  { id: 'q10', question: '你是否愿意向其他同学推荐本课程？', type: 'RATING', maxScore: 10 }
]

// ========== 弹窗 ==========
const dialogVisible = ref(false)
const dialogTitle = ref('创建问卷')
const isEdit = ref(false)
const editId = ref(null)
const submitting = ref(false)
const title = ref('')
const selectedClassId = ref(null)
const selectedCourseId = ref(null) // 选中的课程ID

// ========== 预览弹窗 ==========
const previewVisible = ref(false)
const previewTarget = ref(null)

function getPreviewQuestions(row) {
  return standardQuestions
}

function openPreview(row) {
  previewTarget.value = row
  previewVisible.value = true
}

// ========== 打开创建弹窗 ==========
function openCreateDialog() {
  isEdit.value = false
  dialogTitle.value = '创建问卷'
  editId.value = null
  title.value = ''
  selectedClassId.value = null
  selectedCourseId.value = null // 清空历史选择
  dialogVisible.value = true
}

// ========== 打开编辑弹窗 ==========
function openEditDialog(row) {
  isEdit.value = true
  dialogTitle.value = '编辑问卷'
  editId.value = row.id
  title.value = row.title
  selectedClassId.value = row.classId
  selectedCourseId.value = row.courseId // 回显关联的课程ID
  dialogVisible.value = true
}

// ========== 构建标准 JSON ==========
function buildContentJson() {
  return JSON.stringify(standardQuestions)
}

// ========== 提交保存 ==========
async function handleSubmit() {
  if (!title.value.trim()) {
    ElMessage.warning('请输入问卷标题')
    return
  }
  if (!selectedClassId.value) {
    ElMessage.warning('请选择发布班级')
    return
  }
  if (!selectedCourseId.value) {
    ElMessage.warning('请选择关联课程')
    return
  }

  submitting.value = true
  try {
    const contentJson = buildContentJson()
    const submitData = {
      title: title.value,
      contentJson,
      classId: selectedClassId.value,
      courseId: selectedCourseId.value
    }

    if (isEdit.value) {
      await questionnaireApi.update(editId.value, submitData)
      ElMessage.success('问卷更新成功')
    } else {
      await questionnaireApi.create({
        teacherId: 1,
        status: 1,
        ...submitData
      })
      ElMessage.success('问卷创建成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch (e) {
    console.error('提交失败', e)
  } finally {
    submitting.value = false
  }
}

// ========== 状态切换 ==========
async function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    if (newStatus === 0) {
      await ElMessageBox.confirm(
        '关闭问卷后将自动统计学生评分并生成教学效果评价报告，确定关闭吗？',
        '关闭问卷',
        { confirmButtonText: '确定关闭', cancelButtonText: '取消', type: 'warning' }
      )
      await questionnaireApi.closeAndGenerate(row.id)
      row.status = 0
      ElMessage.success('问卷已关闭并生成评价报告')
      fetchList()
    } else {
      await questionnaireApi.toggleStatus(row.id, newStatus)
      row.status = newStatus
      ElMessage.success('问卷已发布')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('状态切换失败', e)
    }
  }
}

// ========== 删除 ==========
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除问卷「${row.title}」吗？`, '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await questionnaireApi.delete(row.id)
    ElMessage.success('问卷删除成功')
    fetchList()
  } catch (e) {}
}

function getQuestionCount(row) {
  return 10
}

function typeName(type) {
  const map = { RATING: '评分', SINGLE: '单选', MULTI: '多选', TEXT: '简答' }
  return map[type] || type
}
</script>

<template>
  <div class="questionnaire-page">
    <div class="stat-row">
      <div class="stat-card">
        <span class="stat-value">{{ totalCount }}</span>
        <span class="stat-label">问卷总数</span>
      </div>
      <div class="stat-card">
        <span class="stat-value text-green">{{ activeCount }}</span>
        <span class="stat-label">进行中</span>
      </div>
      <div class="stat-card">
        <span class="stat-value text-muted">{{ closedCount }}</span>
        <span class="stat-label">已关闭</span>
      </div>
    </div>

    <div class="page-card">
      <div class="page-header">
        <div>
          <h2>问卷调查管理</h2>
          <p class="subtitle">创建、发布和管理教学满意度问卷</p>
        </div>
        <el-button type="primary" :icon="Plus" @click="openCreateDialog">新建问卷</el-button>
      </div>

      <div class="toolbar">
        <el-input
          v-model="searchText"
          placeholder="搜索问卷标题..."
          :prefix-icon="Search"
          clearable
          style="width: 280px"
        />
      </div>

      <el-table :data="filteredList" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="title" label="问卷标题" min-width="220">
          <template #default="{ row }">
            <span class="font-bold">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column label="题目数" width="80" align="center">
          <template #default="{ row }">
            <span class="text-blue font-bold">{{ getQuestionCount(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="['status-tag', row.status === 1 ? 'active' : 'inactive']">
              {{ row.status === 1 ? '● 进行中' : '○ 已关闭' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="260" align="center">
          <template #default="{ row }">
            <el-button size="small" :icon="View" @click="openPreview(row)">预览</el-button>
            <el-button
              size="small"
              :icon="RefreshRight"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '关闭' : '发布' }}
            </el-button>
            <el-button size="small" :icon="Delete" type="danger" @click="handleDelete(row)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="580px" destroy-on-close>
      <div class="editor-container">
        <div class="editor-section">
          <label class="editor-label">问卷标题 <span class="required">*</span></label>
          <el-input
            v-model="title"
            placeholder="例如：2026春季《大模型应用》课程评价"
            maxlength="50"
            show-word-limit
            size="large"
          />
        </div>

        <div class="editor-section">
          <label class="editor-label">发布班级 <span class="required">*</span></label>
          <el-select
            v-model="selectedClassId"
            placeholder="请选择班级"
            style="width: 100%"
            size="large"
          >
            <el-option label="2024级软件工程1班" :value="1" />
          </el-select>
          <p class="hint-text">问卷将发布到该班级，学生填写后自动汇总生成评价报告</p>
        </div>

        <div class="editor-section">
          <label class="editor-label">关联课程 <span class="required">*</span></label>
          <el-select
            v-model="selectedCourseId"
            placeholder="请选择关联课程"
            style="width: 100%"
            size="large"
          >
            <el-option
              v-for="course in courseList"
              :key="course.id"
              :label="course.courseName"
              :value="course.id"
            />
          </el-select>
          <p class="hint-text">问卷将与选中的课程绑定，以便系统大模型分析对应的课程指标</p>
        </div>

        <div class="editor-section">
          <label class="editor-label">问卷内容（标准 10 题）</label>
          <div class="standard-questions">
            <div v-for="q in standardQuestions" :key="q.id" class="standard-q-item">
              <span class="q-num">{{ q.id.replace('q', '') }}.</span>
              <span class="q-text">{{ q.question }}</span>
              <el-tag size="small" type="info">评分题</el-tag>
            </div>
          </div>
          <p class="hint-text">
            * 题目为标准化模板，不支持修改。每题最高 10 分（五星制，每星 2 分）
          </p>
        </div>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">发 布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="问卷预览" width="580px" destroy-on-close>
      <div v-if="previewTarget" class="preview-container">
        <h3 class="preview-title">{{ previewTarget.title }}</h3>
        <p class="preview-meta">
          {{ getQuestionCount(previewTarget) }} 道题 ·
          {{ previewTarget.status === 1 ? '进行中' : '已关闭' }}
        </p>
        <div
          v-for="(q, qi) in getPreviewQuestions(previewTarget)"
          :key="qi"
          class="preview-question"
        >
          <div class="preview-q-header">
            <span class="preview-q-num">{{ qi + 1 }}.</span>
            <span class="preview-q-text">{{ q.question }}</span>
            <el-tag size="small" type="info">{{ typeName(q.type) }}</el-tag>
          </div>
          <div v-if="q.options && q.options.length" class="preview-options">
            <div v-for="(opt, oi) in q.options" :key="oi" class="preview-opt">
              {{ String.fromCharCode(65 + oi) }}. {{ opt }}
            </div>
          </div>
          <div v-else-if="q.type === 'RATING'" class="preview-rating">
            <span v-for="s in 5" :key="s" class="star">★</span>
          </div>
          <div v-else-if="q.type === 'TEXT'" class="preview-textarea">
            <el-input type="textarea" :rows="2" disabled placeholder="（学生在此填写）" />
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.questionnaire-page {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}
.stat-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-lg);
}
.toolbar {
  margin-bottom: var(--space-md);
}

/* ========== 编辑器 ========== */
.editor-container {
  max-height: 65vh;
  overflow-y: auto;
  padding-right: 4px;
}
.editor-section {
  margin-bottom: 20px;
}
.editor-label {
  display: block;
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 8px;
}
.required {
  color: var(--danger-red);
}
.hint-text {
  font-size: 0.78rem;
  color: var(--text-muted);
  margin-top: 6px;
  line-height: 1.5;
}

/* 标准问卷展示 */
.standard-questions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 320px;
  overflow-y: auto;
  padding: 12px;
  background: var(--bg-page);
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
}
.standard-q-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  background: white;
  border-radius: var(--radius-xs);
  font-size: 0.88rem;
}
.q-num {
  font-weight: 700;
  color: var(--primary-blue);
  min-width: 20px;
}
.q-text {
  flex: 1;
  color: var(--text-primary);
  line-height: 1.5;
}

/* ========== 预览 ========== */
.preview-container {
  max-height: 60vh;
  overflow-y: auto;
}
.preview-title {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}
.preview-meta {
  font-size: 0.82rem;
  color: var(--text-muted);
  margin-bottom: 18px;
}
.preview-question {
  padding: 14px;
  background: var(--bg-page);
  border-radius: var(--radius-sm);
  margin-bottom: 10px;
}
.preview-q-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}
.preview-q-num {
  font-weight: 700;
  color: var(--primary-blue);
}
.preview-q-text {
  flex: 1;
  font-weight: 500;
  color: var(--text-primary);
}
.preview-options {
  padding-left: 20px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.preview-opt {
  font-size: 0.88rem;
  color: var(--text-secondary);
}
.preview-rating {
  padding-left: 20px;
}
.preview-rating .star {
  font-size: 1.3rem;
  color: var(--warning-orange);
  margin-right: 4px;
}
.preview-textarea {
  padding-left: 20px;
}
</style>
