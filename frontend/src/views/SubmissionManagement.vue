<template>
  <div class="submission-management">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="学生姓名">
          <el-input
            v-model="searchForm.studentName"
            placeholder="请输入学生姓名"
            class="search-input"
          />
        </el-form-item>
        <el-form-item label="任务名称">
          <el-input
            v-model="searchForm.taskTitle"
            placeholder="请输入任务名称"
            class="search-input"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" class="search-select">
            <el-option label="全部状态" value="" />
            <el-option label="已提交" :value="SUBMITTED" />
            <el-option label="AI批改中" :value="AI_PROCESSED" />
            <el-option label="已批改" :value="GRADED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <el-card class="stat-card">
        <div class="stat-icon bg-primary">📝</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">总提交数</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-icon bg-warning">⚡</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.submitted }}</div>
          <div class="stat-label">待批改</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-icon bg-info">🤖</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.aiProcessed }}</div>
          <div class="stat-label">AI已批改</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-icon bg-success">✅</div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.graded }}</div>
          <div class="stat-label">教师已复核</div>
        </div>
      </el-card>
    </div>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <div class="table-header">
        <h3>作业提交列表</h3>
        <div class="header-actions">
          <el-button
            type="primary"
            plain
            @click="batchAiGrade"
            :disabled="selectedIds.length === 0"
          >
            <el-icon><Cpu /></el-icon>
            批量AI批改
          </el-button>
        </div>
      </div>

      <el-table
        :data="submissionList"
        stripe
        border
        :loading="loading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="taskTitle" label="任务名称" width="200" />
        <el-table-column prop="courseName" label="课程名称" width="180" />
        <el-table-column prop="submitTime" label="提交时间" width="200" :formatter="formatDate" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="aiScore" label="AI得分" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.aiScore !== null">{{ row.aiScore }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="teacherScore" label="教师评分" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.teacherScore !== null">{{ row.teacherScore }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="viewDetail(row)">
              <el-icon><Monitor /></el-icon>
              查看详情
            </el-button>
            <el-button
              v-if="row.status === SUBMITTED"
              size="small"
              type="success"
              plain
              @click="aiGrade(row.id)"
            >
              <el-icon><Cpu /></el-icon>
              AI批改
            </el-button>
            <el-button
              v-if="row.status === AI_PROCESSED"
              size="small"
              type="warning"
              plain
              @click="teacherGrade(row)"
            >
              <el-icon><Edit /></el-icon>
              教师复核
            </el-button>
            <el-button size="small" type="danger" plain @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="作业详情" width="600px" destroy-on-close>
      <div v-if="detailData" class="detail-content">
        <div class="detail-row">
          <span class="detail-label">学生姓名：</span>
          <span class="detail-value">{{ detailData.studentName }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">任务名称：</span>
          <span class="detail-value">{{ detailData.taskTitle }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">课程名称：</span>
          <span class="detail-value">{{ detailData.courseName }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">提交时间：</span>
          <span class="detail-value">{{ formatDate(detailData, 'submitTime') }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">提交文本：</span>
          <span class="detail-value">{{ detailData.submitText || '-' }}</span>
        </div>
        <div v-if="detailData.ocrRawText" class="detail-row">
          <span class="detail-label">OCR识别结果：</span>
          <span class="detail-value">{{ detailData.ocrRawText }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">AI得分：</span>
          <span class="detail-value">{{
            detailData.aiScore !== null ? detailData.aiScore : '-'
          }}</span>
        </div>
        <div v-if="detailData.aiComment" class="detail-row">
          <span class="detail-label">AI评语：</span>
          <span class="detail-value">{{ detailData.aiComment }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">教师评分：</span>
          <span class="detail-value">{{
            detailData.teacherScore !== null ? detailData.teacherScore : '-'
          }}</span>
        </div>
        <div v-if="detailData.teacherComment" class="detail-row">
          <span class="detail-label">教师评语：</span>
          <span class="detail-value">{{ detailData.teacherComment }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">状态：</span>
          <el-tag :type="getStatusType(detailData.status)">{{
            getStatusLabel(detailData.status)
          }}</el-tag>
        </div>
      </div>
    </el-dialog>

    <!-- 教师复核弹窗 -->
    <el-dialog
      v-model="gradeVisible"
      :title="isEditGrade ? '修改评分' : '教师复核'"
      width="480px"
      destroy-on-close
    >
      <el-form
        ref="gradeFormRef"
        :model="gradeForm"
        :rules="gradeRules"
        label-width="100px"
        class="grade-form"
      >
        <el-form-item label="AI得分" prop="aiScore">
          <el-input v-model="gradeForm.aiScore" disabled />
        </el-form-item>
        <el-form-item label="教师评分" prop="teacherScore">
          <el-input-number v-model="gradeForm.teacherScore" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="教师评语" prop="teacherComment">
          <el-input
            v-model="gradeForm.teacherComment"
            type="textarea"
            :rows="3"
            placeholder="请输入评语"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="gradeVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleTeacherGrade">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Cpu, Monitor, Edit, Delete } from '@element-plus/icons-vue'
import {
  fetchSubmissionList,
  aiGradeSubmission,
  teacherGradeSubmission,
  deleteSubmission
} from '@/api/submission'

// 状态常量
const SUBMITTED = 'SUBMITTED'
const AI_PROCESSED = 'AI_PROCESSED'
const GRADED = 'GRADED'

// 搜索表单
const searchForm = reactive({
  studentName: '',
  taskTitle: '',
  status: ''
})

// 分页配置
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 状态
const loading = ref(false)
const submissionList = ref([])
const selectedIds = ref([])

// 弹窗相关
const detailVisible = ref(false)
const gradeVisible = ref(false)
const isEditGrade = ref(false)
const gradeFormRef = ref(null)
const detailData = ref(null)
const gradeForm = reactive({
  submissionId: null,
  aiScore: null,
  teacherScore: null,
  teacherComment: ''
})

// 统计数据
const stats = computed(() => {
  const total = submissionList.value.length
  const submitted = submissionList.value.filter((s) => s.status === SUBMITTED).length
  const aiProcessed = submissionList.value.filter((s) => s.status === AI_PROCESSED).length
  const graded = submissionList.value.filter((s) => s.status === GRADED).length
  return { total, submitted, aiProcessed, graded }
})

// 表单校验规则
const gradeRules = {
  teacherScore: [
    { required: true, message: '请输入教师评分', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '评分范围为0-100', trigger: 'blur' }
  ],
  teacherComment: [{ max: 500, message: '评语长度不能超过500个字符', trigger: 'blur' }]
}

// 初始化
onMounted(() => {
  loadSubmissionList()
})

// 加载提交列表
async function loadSubmissionList() {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize
    }
    const res = await fetchSubmissionList(params)
    if (res.code === 200) {
      submissionList.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载提交列表失败:', error)
    ElMessage.error('加载提交列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.currentPage = 1
  loadSubmissionList()
}

// 重置
function handleReset() {
  searchForm.studentName = ''
  searchForm.taskTitle = ''
  searchForm.status = ''
  pagination.currentPage = 1
  loadSubmissionList()
}

// 分页大小改变
function handleSizeChange(val) {
  pagination.pageSize = val
  pagination.currentPage = 1
  loadSubmissionList()
}

// 当前页改变
function handleCurrentChange(val) {
  pagination.currentPage = val
  loadSubmissionList()
}

// 选择项改变
function handleSelectionChange(val) {
  selectedIds.value = val.map((item) => item.id)
}

// 获取状态标签类型
function getStatusType(status) {
  switch (status) {
    case SUBMITTED:
      return 'warning'
    case AI_PROCESSED:
      return 'info'
    case GRADED:
      return 'success'
    default:
      return 'default'
  }
}

// 获取状态标签文字
function getStatusLabel(status) {
  switch (status) {
    case SUBMITTED:
      return '已提交'
    case AI_PROCESSED:
      return 'AI已批改'
    case GRADED:
      return '教师已复核'
    default:
      return status
  }
}

// 日期格式化
function formatDate(row, column) {
  const dateValue = typeof column === 'string' ? row[column] : row.submitTime
  if (!dateValue) return ''

  let date
  if (Array.isArray(dateValue)) {
    const [year, month, day, hour, minute, second] = dateValue
    date = new Date(year, month - 1, day, hour || 0, minute || 0, second || 0)
  } else if (typeof dateValue === 'string') {
    date = new Date(dateValue)
  } else {
    return ''
  }

  if (isNaN(date.getTime())) {
    return '无效日期'
  }

  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 查看详情
function viewDetail(row) {
  detailData.value = row
  detailVisible.value = true
}

// AI批改
async function aiGrade(id) {
  try {
    ElMessage.info('正在进行AI批改，请稍候...')
    const res = await aiGradeSubmission(id)
    if (res.code === 200) {
      ElMessage.success('AI批改完成')
      loadSubmissionList()
    } else {
      ElMessage.error(res.msg || 'AI批改失败')
    }
  } catch (error) {
    console.error('AI批改失败:', error)
    // 显示更具体的错误信息
    const errorMsg = error.response?.data?.msg || error.message || 'AI批改失败，请稍后重试'
    ElMessage.error(errorMsg)
  }
}

// 批量AI批改
async function batchAiGrade() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要批改的提交记录')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要对选中的 ${selectedIds.value.length} 条记录进行AI批改吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    ElMessage.info('正在批量进行AI批改，请稍候...')

    // 逐个批改（避免并发过大）
    let successCount = 0
    for (const id of selectedIds.value) {
      try {
        const res = await aiGradeSubmission(id)
        if (res.code === 200) {
          successCount++
        }
      } catch (error) {
        console.error(`AI批改失败，ID: ${id}`, error)
      }
    }

    ElMessage.success(
      `批量批改完成，成功 ${successCount} 条，失败 ${selectedIds.value.length - successCount} 条`
    )
    selectedIds.value = []
    loadSubmissionList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量AI批改失败:', error)
      ElMessage.error('批量AI批改失败')
    }
  }
}

// 教师复核
function teacherGrade(row) {
  isEditGrade.value = row.status === GRADED
  gradeForm.submissionId = row.id
  gradeForm.aiScore = row.aiScore
  gradeForm.teacherScore = row.teacherScore || ''
  gradeForm.teacherComment = row.teacherComment || ''
  gradeVisible.value = true
}

// 提交教师评分
async function handleTeacherGrade() {
  if (!gradeFormRef.value) return

  try {
    await gradeFormRef.value.validate()

    const data = {
      submissionId: gradeForm.submissionId,
      teacherScore: gradeForm.teacherScore,
      teacherComment: gradeForm.teacherComment
    }

    const res = await teacherGradeSubmission(data)
    if (res.code === 200) {
      ElMessage.success('批改完成')
      gradeVisible.value = false
      loadSubmissionList()
    } else {
      ElMessage.error(res.msg || '批改失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败')
  }
}

// 删除提交
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除学生「${row.studentName}」的提交记录吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteSubmission(row.id)
    ElMessage.success('删除成功')
    loadSubmissionList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped>
.submission-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 80px);
}

.search-bar {
  background: #fff;
  padding: 16px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
}

.search-input {
  width: 200px;
}

.search-select {
  width: 180px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-icon.bg-primary {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
}

.stat-icon.bg-warning {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.stat-icon.bg-info {
  background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%);
}

.stat-icon.bg-success {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
}

.table-card {
  background: #fff;
  border-radius: 8px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
}

.table-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.text-muted {
  color: #94a3b8;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid #e2e8f0;
}

.detail-content {
  padding: 10px 0;
}

.detail-row {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px dashed #e2e8f0;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  width: 120px;
  font-weight: 600;
  color: #64748b;
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  color: #1e293b;
}

.grade-form {
  padding: 10px 0;
}
</style>
