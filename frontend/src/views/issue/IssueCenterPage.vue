<template>
  <div class="app-container">
    <div class="stat-container">
      <div class="stat-card">
        <el-icon class="stat-icon total"><ChatLineSquare /></el-icon>
        <div class="stat-info">
          <span class="stat-value">{{ totalCount }}</span>
          <span class="stat-label">问题总数</span>
        </div>
      </div>
      <div class="stat-card">
        <el-icon class="stat-icon pending"><Clock /></el-icon>
        <div class="stat-info">
          <span class="stat-value text-orange">{{ pendingCount }}</span>
          <span class="stat-label">待解答</span>
        </div>
      </div>
      <div class="stat-card">
        <el-icon class="stat-icon resolved"><CircleCheck /></el-icon>
        <div class="stat-info">
          <span class="stat-value text-green">{{ resolvedCount }}</span>
          <span class="stat-label">已解答</span>
        </div>
      </div>
    </div>

    <div class="page-card">
      <div class="card-header">
        <div class="header-left">
          <h2>🔍 问题中心答疑</h2>
          <p class="header-desc">查看学生提问，进行教师回复与人工智能辅助解答</p>
        </div>
        <el-button type="primary" :icon="Plus" @click="openPublishDialog">发布问题</el-button>
      </div>

      <div class="filter-bar">
        <el-input
          v-model="searchText"
          placeholder="搜索问题内容或学生姓名..."
          :prefix-icon="Search"
          clearable
          style="width: 300px"
        />
        <el-select
          v-model="statusFilter"
          placeholder="全部状态"
          clearable
          style="width: 140px; margin-left: 12px"
        >
          <el-option label="全部状态" value="" />
          <el-option label="待解答" value="0" />
          <el-option label="已解答" value="1" />
        </el-select>
      </div>

      <el-table :data="filteredList" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" align="center" />

        <el-table-column label="提问学生" width="130">
          <template #default="{ row }">
            <div class="student-cell">
              <el-avatar :size="24" :icon="UserFilled" class="student-avatar" />
              <span class="student-name">{{ row.studentName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="问题内容 (点击查看详情)" min-width="300">
          <template #default="{ row }">
            <div class="question-cell">
              <el-tag size="small" type="info" class="course-tag">{{
                row.courseName || '通用课程'
              }}</el-tag>
              <span class="question-text-link" @click="openDetailDialog(row)">{{
                row.questionText
              }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="{ row }">
            <span :class="['status-tag', row.status === 1 ? 'resolved' : 'pending']">
              {{ row.status === 1 ? '● 已解答' : '● 待解答' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="提问时间" width="170" align="center" />

        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="ChatLineSquare" @click="openReplyDialog(row)">
              {{ row.status === 1 ? '修改回复' : '去解答' }}
            </el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && !filteredList.length" description="暂无相关问题数据" />
    </div>

    <el-dialog v-model="publishDialogVisible" title="发布问题" width="520px" destroy-on-close>
      <el-form ref="publishFormRef" :model="publishForm" :rules="publishRules" label-width="90px">
        <el-form-item label="学生姓名" prop="studentName">
          <el-input v-model="publishForm.studentName" placeholder="请输入学生姓名" />
        </el-form-item>
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="publishForm.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option
              v-for="course in courseOptions"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="问题内容" prop="questionText">
          <el-input
            v-model="publishForm.questionText"
            type="textarea"
            :rows="4"
            placeholder="请详细描述学生提出的技术问题..."
            @input="onQuestionInput"
          />
        </el-form-item>

        <!-- AI 相似问题推荐 -->
        <div v-if="similarQuestions.length > 0" class="similar-questions-panel">
          <div class="similar-header">
            <span class="similar-badge">🤖 AI 相似问题推荐</span>
          </div>
          <!-- 高相似度警告 -->
          <div v-if="highSimilarityItem" class="high-similarity-warning">
            <el-icon class="warning-icon"><WarningFilled /></el-icon>
            <span>已有高度相似问题（{{ Math.round(highSimilarityItem.score * 100) }}%），是否直接查看？</span>
            <el-button type="warning" size="small" @click="viewExistingIssue(highSimilarityItem)">
              查看已有问题
            </el-button>
          </div>
          <div
            v-for="(item, idx) in similarQuestions"
            :key="idx"
            class="similar-item"
          >
            <div class="similar-item-header">
              <span class="similar-rank">#{{ idx + 1 }}</span>
              <el-progress
                :percentage="Math.round(item.score * 100)"
                :stroke-width="6"
                :color="item.score > 0.8 ? '#f56c6c' : item.score > 0.5 ? '#e6a23c' : '#67c23a'"
                style="flex: 1; margin: 0 10px"
              />
              <span class="similar-percent">{{ Math.round(item.score * 100) }}%</span>
            </div>
            <p class="similar-text">{{ item.text }}</p>
            <div v-if="item.id" class="similar-actions">
              <el-button type="primary" link size="small" @click="viewExistingIssueById(item.id)">
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="publishDialogVisible = false">取 消</el-button>
          <el-button type="primary" :loading="submitting" @click="handlePublish">确 定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="replyDialogVisible" title="教师回复答疑" width="560px" destroy-on-close>
      <div class="reply-context" v-if="replyTarget">
        <div class="reply-question-box">
          <span class="box-label">学生提问：</span>
          <p class="box-text">{{ replyTarget.questionText }}</p>
        </div>

        <div v-if="replyTarget.aiSuggestedAnswer" class="ai-suggestion-box">
          <div class="ai-box-header">
            <span class="ai-badge">🤖 AI 助教建议答案</span>
            <el-button type="primary" link size="small" @click="applyAiDraft">使用此拟稿</el-button>
          </div>
          <p class="ai-box-text">{{ replyTarget.aiSuggestedAnswer }}</p>
        </div>
      </div>

      <div class="reply-input-section">
        <span class="box-label">教师回复：</span>
        <el-input
          v-model="replyText"
          type="textarea"
          :rows="5"
          placeholder="请输入针对该问题的教师专业解答内容..."
          style="margin-top: 8px"
        />
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="replyDialogVisible = false">取 消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleReply">提交回复</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="问题详情档案" width="580px" destroy-on-close>
      <div v-if="detailIssue" class="detail-content">
        <div class="detail-grid">
          <div class="detail-row">
            <span class="detail-label">提问学生：</span>
            <span class="detail-val font-bold">{{ detailIssue.studentName }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">提问时间：</span>
            <span class="detail-val">{{ detailIssue.createTime }}</span>
          </div>
        </div>

        <div class="detail-block question-block">
          <span class="detail-label-block">❓ 问题内容</span>
          <p>{{ detailIssue.questionText }}</p>
        </div>

        <div v-if="detailIssue.aiSuggestedAnswer" class="detail-block ai-block">
          <span class="detail-label-block">🤖 AI 核心建议</span>
          <p>{{ detailIssue.aiSuggestedAnswer }}</p>
        </div>

        <div class="detail-block reply-block">
          <span class="detail-label-block">✍️ 教师最终回复</span>
          <p v-if="detailIssue.teacherReply">{{ detailIssue.teacherReply }}</p>
          <div v-else class="no-reply-hint">
            <span>暂无教师回复</span>
            <el-button
              type="primary"
              size="small"
              style="margin-left: 12px"
              @click="switchToReplyFromDetail"
              >立即回复</el-button
            >
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  ChatLineSquare,
  Delete,
  Search,
  UserFilled,
  Clock,
  CircleCheck,
  WarningFilled
} from '@element-plus/icons-vue'
import issueApi from '@/api/issue'
import courseApi from '@/api/course'
import request from '@/api/request'

// ========== 核心状态响应式变量 ==========
const loading = ref(false)
const submitting = ref(false)
const searchText = ref('')
const statusFilter = ref('')
const issueList = ref([]) // 真实的后端数据容器

// ========== 级联过滤处理 (纯后端数据驱动) ==========
const filteredList = computed(() => {
  let result = issueList.value

  // 1. 文本搜索过滤
  if (searchText.value) {
    const keyword = searchText.value.toLowerCase()
    result = result.filter(
      (item) =>
        (item.questionText && item.questionText.toLowerCase().includes(keyword)) ||
        (item.studentName && item.studentName.includes(searchText.value))
    )
  }

  // 2. 状态下拉过滤
  if (statusFilter.value !== '') {
    result = result.filter((item) => item.status === Number(statusFilter.value))
  }

  return result
})

// ========== 统计看板核心算法 (基于真实总数据集) ==========
const totalCount = computed(() => issueList.value.length)
const pendingCount = computed(() => issueList.value.filter((i) => i.status === 0).length)
const resolvedCount = computed(() => issueList.value.filter((i) => i.status === 1).length)

// ========== 核心 API 异步交互数据读取 ==========
async function fetchList() {
  loading.value = true
  try {
    const res = await issueApi.list()
    issueList.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error('获取真实问题列表失败:', e)
    ElMessage.error('无法获取答疑列表数据')
  } finally {
    loading.value = false
  }
}

async function fetchCourses() {
  try {
    const res = await courseApi.list()
    courseOptions.value = Array.isArray(res)
      ? res.map((c) => ({
          id: c.id,
          name: c.courseName
        }))
      : []
  } catch (e) {
    console.error('获取课程列表失败', e)
  }
}

onMounted(async () => {
  await Promise.all([fetchList(), fetchCourses()])
})

// ========== 新增提问模块 ==========
const publishDialogVisible = ref(false)
const publishFormRef = ref(null)
const publishForm = ref({
  studentName: '',
  courseId: null,
  questionText: ''
})

const courseOptions = ref([])

// AI 相似问题推荐
const similarQuestions = ref([])
let similarityDebounceTimer = null

// 最高相似度项（>0.8时触发警告）
const highSimilarityItem = computed(() => {
  if (similarQuestions.value.length > 0 && similarQuestions.value[0].score > 0.8) {
    return similarQuestions.value[0]
  }
  return null
})

/**
 * 查看已有问题详情
 */
function viewExistingIssue(item) {
  // 在列表中查找对应的问题
  const found = issueList.value.find((i) => i.id === item.id)
  if (found) {
    openDetailDialog(found)
  }
}

function viewExistingIssueById(id) {
  const found = issueList.value.find((i) => i.id === id)
  if (found) {
    openDetailDialog(found)
  } else {
    ElMessage.info('该问题可能已被删除')
  }
}

/**
 * 【只读】输入问题内容时实时调用相似度检索接口
 * 仅查询历史问题库，绝对不保存任何数据
 */
function onQuestionInput() {
  const text = publishForm.value.questionText?.trim()
  if (!text || text.length < 3) {
    similarQuestions.value = []
    return
  }

  // 防抖 500ms
  if (similarityDebounceTimer) clearTimeout(similarityDebounceTimer)
  similarityDebounceTimer = setTimeout(async () => {
    try {
      // 调用只读接口 — 仅检索，不保存
      const res = await request.post('/api/issue/check-similar', {
        questionText: text,
        courseId: publishForm.value.courseId || 1
      })
      if (res.code === 200) {
        // 只读接口直接返回数组
        const results = Array.isArray(res.data) ? res.data : []
        similarQuestions.value = results.slice(0, 3)
      }
    } catch (e) {
      console.warn('相似度检测暂不可用', e)
      similarQuestions.value = []
    }
  }, 500)
}

const publishRules = {
  studentName: [{ required: true, message: '请输入学生姓名', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  questionText: [{ required: true, message: '请输入问题内容', trigger: 'blur' }]
}

function openPublishDialog() {
  publishForm.value = { studentName: '', courseId: null, questionText: '' }
  similarQuestions.value = []
  publishDialogVisible.value = true
}

async function handlePublish() {
  const valid = await publishFormRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    // 使用带相似度检测的保存接口（此时才真正保存到数据库）
    const res = await request.post('/api/issue/create-with-similarity', {
      studentName: publishForm.value.studentName,
      courseId: publishForm.value.courseId,
      questionText: publishForm.value.questionText
    })
    if (res.code === 200) {
      // 如果有相似问题结果，一并展示
      if (res.data?.similarQuestions) {
        similarQuestions.value = res.data.similarQuestions.slice(0, 3)
      }
      ElMessage.success('问题成功发布至业务中心')
      publishDialogVisible.value = false
      fetchList()
    } else {
      ElMessage.error(res.msg || '问题发布失败')
    }
  } catch (e) {
    console.error('发布失败:', e)
    ElMessage.error('问题发布失败，请检查网络')
  } finally {
    submitting.value = false
  }
}

// ========== 教师回复模块 ==========
const replyDialogVisible = ref(false)
const replyTarget = ref(null)
const replyText = ref('')

function openReplyDialog(row) {
  replyTarget.value = row
  replyText.value = row.teacherReply || ''
  replyDialogVisible.value = true
}

function applyAiDraft() {
  if (replyTarget.value?.aiSuggestedAnswer) {
    replyText.value = replyTarget.value.aiSuggestedAnswer
    ElMessage.success('已成功应用 AI 拟稿内容')
  }
}

async function handleReply() {
  if (!replyText.value.trim()) {
    ElMessage.warning('回复内容不能为空，请输入具体解答意见')
    return
  }

  submitting.value = true
  try {
    await issueApi.reply(replyTarget.value.id, replyText.value)
    ElMessage.success('教师审阅回复成功')
    replyDialogVisible.value = false
    fetchList()
  } catch (e) {
    console.error('回复提交失败:', e)
    ElMessage.error('回复提交失败')
  } finally {
    submitting.value = false
  }
}

// ========== 详情探查模块 ==========
const detailDialogVisible = ref(false)
const detailIssue = ref(null)

function openDetailDialog(row) {
  detailIssue.value = row
  detailDialogVisible.value = true
}

function switchToReplyFromDetail() {
  const target = detailIssue.value
  detailDialogVisible.value = false
  openReplyDialog(target)
}

// ========== 移除删除逻辑 ==========
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确认为学生 [${row.studentName}] 永久删除该提问吗？此操作无法撤销。`,
      '安全危险确认',
      {
        confirmButtonText: '确定持久化删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await issueApi.delete(row.id)
    ElMessage.success('真实数据单元已从云端抹除')
    fetchList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('删除操作失败:', e)
      ElMessage.error('数据擦除失败')
    }
  }
}
</script>

<style scoped>
/* ========== 企业级现代后台高级样式布局 ========== */
.app-container {
  padding: 24px;
  background-color: #f6f8fa;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 1. 顶部高端统计看板 */
.stat-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}
.stat-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
  transition: transform 0.2s, box-shadow 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}
.stat-icon {
  font-size: 28px;
  padding: 12px;
  border-radius: 10px;
  margin-right: 18px;
}
.stat-icon.total {
  background: #eef2ff;
  color: #4f46e5;
}
.stat-icon.pending {
  background: #fff7ed;
  color: #ea580c;
}
.stat-icon.resolved {
  background: #f0fdf4;
  color: #16a34a;
}

.stat-info {
  display: flex;
  flex-direction: column;
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
}
.text-orange {
  color: #ea580c;
}
.text-green {
  color: #16a34a;
}
.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-top: 4px;
}

/* 2. 主页面数据流卡片 */
.page-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f1f5f9;
  padding-bottom: 16px;
  margin-bottom: 20px;
}
.card-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}
.header-desc {
  font-size: 13px;
  color: #94a3b8;
  margin: 4px 0 0 0;
}
.filter-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

/* 3. 优化过的表格内部组件 */
.student-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.student-avatar {
  background-color: #e2e8f0;
  color: #64748b;
}
.student-name {
  font-weight: 500;
  color: #334155;
}
.question-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.course-tag {
  flex-shrink: 0;
}
.question-text-link {
  color: #2563eb;
  cursor: pointer;
  font-weight: 400;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.question-text-link:hover {
  color: #1d4ed8;
  text-decoration: underline;
}

/* 优雅的状态圆点标签 */
.status-tag {
  display: inline-block;
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 500;
  border-radius: 20px;
}
.status-tag.pending {
  background-color: #fef2f2;
  color: #ef4444;
}
.status-tag.resolved {
  background-color: #f0fdf4;
  color: #22c55e;
}

/* 4. 精致弹窗气泡盒 */
.reply-context {
  background-color: #f8fafc;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  border: 1px solid #e2e8f0;
}
.box-label {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  display: block;
  margin-bottom: 6px;
}
.box-text {
  font-size: 14px;
  color: #1e293b;
  margin: 0;
  line-height: 1.6;
}

/* AI 拟稿盒子 */
.ai-suggestion-box {
  margin-top: 14px;
  background: linear-gradient(135deg, #f5f3ff, #edd9ff);
  border: 1px solid #d8b4fe;
  border-radius: 8px;
  padding: 12px 14px;
}
.ai-box-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.ai-badge {
  font-size: 12px;
  font-weight: 600;
  color: #6b21a8;
}
.ai-box-text {
  font-size: 13px;
  color: #581c87;
  margin: 0;
  line-height: 1.5;
}

/* 5. 详情查看视图 */
.detail-content {
  padding: 4px;
}
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  border-bottom: 1px dashed #e2e8f0;
  padding-bottom: 14px;
  margin-bottom: 16px;
}
.detail-row {
  font-size: 14px;
}
.detail-label {
  color: #64748b;
}
.detail-val {
  color: #1e293b;
}
.font-bold {
  font-weight: 600;
}

.detail-block {
  padding: 14px 16px;
  border-radius: 8px;
  margin-bottom: 14px;
}
.detail-label-block {
  font-size: 13px;
  font-weight: 600;
  display: block;
  margin-bottom: 6px;
}
.detail-block p {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
}

.question-block {
  background-color: #f8fafc;
  color: #1e293b;
  border-left: 4px solid #64748b;
}
.ai-block {
  background-color: #f5f3ff;
  color: #581c87;
  border-left: 4px solid #a855f7;
}
.ai-block .detail-label-block {
  color: #7c3aed;
}

.reply-block {
  background-color: #f0fdf4;
  color: #14532d;
  border-left: 4px solid #22c55e;
}
.reply-block .detail-label-block {
  color: #16a34a;
}

.no-reply-hint {
  display: flex;
  align-items: center;
  color: #94a3b8;
  font-size: 13px;
  padding: 6px 0;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* ── AI 相似问题推荐面板 ── */
.similar-questions-panel {
  background: linear-gradient(135deg, #f5f3ff, #ede9fe);
  border: 1px solid #d8b4fe;
  border-radius: 10px;
  padding: 14px 16px;
  margin-top: 16px;
}
.similar-header {
  margin-bottom: 12px;
}
.similar-badge {
  font-size: 13px;
  font-weight: 600;
  color: #6b21a8;
}
.similar-item {
  padding: 10px 12px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 8px;
  border: 1px solid #e9d5ff;
}
.similar-item:last-child {
  margin-bottom: 0;
}
.similar-item-header {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
}
.similar-rank {
  font-size: 12px;
  font-weight: 700;
  color: #7c3aed;
  width: 24px;
}
.similar-percent {
  font-size: 13px;
  font-weight: 600;
  color: #4a5568;
  width: 42px;
  text-align: right;
}
.similar-text {
  font-size: 13px;
  color: #334155;
  line-height: 1.5;
  margin: 0;
  padding-left: 28px;
}
.similar-actions {
  text-align: right;
  padding-top: 6px;
  padding-left: 28px;
}

/* 高相似度警告 */
.high-similarity-warning {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #dc2626;
}
.high-similarity-warning .warning-icon {
  font-size: 18px;
  flex-shrink: 0;
}
</style>
