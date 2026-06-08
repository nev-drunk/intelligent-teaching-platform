<template>
  <div class="submission-management">
    <el-tabs v-model="activeTab" class="main-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="作业工作台" name="task">
        <template #label>
          <span class="tab-label">
            <el-icon><Collection /></el-icon>
            作业工作台
          </span>
        </template>

        <div class="task-workspace">
          <div class="top-bar">
            <div class="top-bar-left">
              <div class="page-title">🎯 作业工作台</div>
              <div class="page-subtitle">管理并布置课程作业，支持创建、查看和删除作业任务</div>
            </div>
            <div class="top-bar-actions">
              <el-button type="primary" @click="openTaskDialog" class="btn-primary-custom">
                <el-icon><Plus /></el-icon>
                布置新作业
              </el-button>
            </div>
          </div>

          <el-card class="task-card" v-loading="taskLoading" shadow="hover">
            <template #header class="card-header-custom">
              <div style="display: flex; align-items: center; gap: 8px">
                <div class="card-header-bar"></div>
                <span style="font-weight: 600">作业列表</span>
              </div>
            </template>
            <el-table :data="taskList" stripe border style="width: 100%" class="custom-table">
              <el-table-column prop="id" label="ID" width="70" align="center" />
              <el-table-column
                prop="title"
                label="作业名称"
                min-width="220"
                show-overflow-tooltip
              />
              <el-table-column prop="type" label="类型" width="120" align="center">
                <template #default="{ row }">
                  <el-tag :type="getTaskTypeTag(row.type)" effect="light">{{
                    getTaskTypeLabel(row.type)
                  }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column
                prop="contentText"
                label="作业描述"
                min-width="280"
                show-overflow-tooltip
              />
              <el-table-column
                prop="deadline"
                label="截止时间"
                width="200"
                :formatter="formatDeadlineColumn"
              />
              <el-table-column label="操作" width="140" align="center" fixed="right">
                <template #default="{ row }">
                  <el-popconfirm
                    title="确定要删除此作业吗？"
                    :confirm-button-text="'确定删除'"
                    :cancel-button-text="'取消'"
                    @confirm="handleDeleteTask(row)"
                  >
                    <template #reference>
                      <el-button size="small" type="danger" plain link>
                        <el-icon><Delete /></el-icon>
                        删除
                      </el-button>
                    </template>
                  </el-popconfirm>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="已提交作业批改" name="submission">
        <template #label>
          <span class="tab-label">
            <el-icon><DocumentChecked /></el-icon>
            已提交作业批改
          </span>
        </template>

        <div class="submission-workspace">
          <div class="top-bar">
            <div class="top-bar-left">
              <div class="page-title">📋 作业批改</div>
              <div class="page-subtitle">查看学生作业提交记录，进行AI批改和教师复核</div>
            </div>
          </div>

          <div class="search-bar">
            <el-form :model="searchForm" class="search-form">
              <el-form-item label="学生姓名" class="search-item">
                <el-input
                  v-model="searchForm.studentName"
                  placeholder="请输入学生姓名"
                  clearable
                  class="search-input"
                />
              </el-form-item>
              <el-form-item label="任务名称" class="search-item">
                <el-input
                  v-model="searchForm.taskTitle"
                  placeholder="请输入任务名称"
                  clearable
                  class="search-input"
                />
              </el-form-item>
              <el-form-item label="状态" class="search-item">
                <el-select
                  v-model="searchForm.status"
                  placeholder="全部状态"
                  clearable
                  class="search-select"
                >
                  <el-option label="已提交" value="SUBMITTED" />
                  <el-option label="AI批改中" value="AI_PROCESSED" />
                  <el-option label="已批改" value="GRADED" />
                </el-select>
              </el-form-item>
              <div class="search-buttons">
                <el-button type="primary" @click="handleSearch">
                  <el-icon><Search /></el-icon>
                  搜索
                </el-button>
                <el-button @click="handleReset">
                  <el-icon><Refresh /></el-icon>
                  重置
                </el-button>
              </div>
            </el-form>
          </div>

          <div class="stats-row">
            <div class="stat-card total">
              <div class="stat-number">{{ stats.total }}</div>
              <div class="stat-title">总提交数</div>
              <div class="stat-icon">📊</div>
            </div>
            <div class="stat-card pending">
              <div class="stat-number">{{ stats.submitted }}</div>
              <div class="stat-title">待批改</div>
              <div class="stat-icon">⏳</div>
            </div>
            <div class="stat-card processing">
              <div class="stat-number">{{ stats.aiProcessed }}</div>
              <div class="stat-title">AI已批改</div>
              <div class="stat-icon">🤖</div>
            </div>
            <div class="stat-card completed">
              <div class="stat-number">{{ stats.graded }}</div>
              <div class="stat-title">教师已复核</div>
              <div class="stat-icon">✅</div>
            </div>
          </div>

          <el-card class="table-card" shadow="hover">
            <template #header class="card-header-custom">
              <div style="display: flex; justify-content: space-between; align-items: center">
                <div style="display: flex; align-items: center; gap: 8px">
                  <div class="card-header-bar"></div>
                  <span style="font-weight: 600">作业提交列表</span>
                </div>
                <el-button
                  type="primary"
                  plain
                  size="small"
                  @click="batchAiGrade"
                  :disabled="selectedIds.length === 0"
                >
                  <el-icon><Cpu /></el-icon>
                  批量AI批改 ({{ selectedIds.length }})
                </el-button>
              </div>
            </template>

            <el-table
              :data="submissionList"
              stripe
              border
              :loading="submissionLoading"
              style="width: 100%"
              @selection-change="handleSelectionChange"
              class="custom-table"
            >
              <el-table-column type="selection" width="50" />
              <el-table-column prop="id" label="ID" width="70" align="center" />
              <el-table-column prop="studentName" label="学生姓名" width="120" />
              <el-table-column
                prop="taskTitle"
                label="任务名称"
                width="200"
                show-overflow-tooltip
              />
              <el-table-column prop="courseName" label="课程名称" width="180" />
              <el-table-column
                prop="submitTime"
                label="提交时间"
                width="200"
                :formatter="formatDateColumn"
              />
              <el-table-column prop="status" label="状态" width="130" align="center">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)" effect="light">{{
                    getStatusLabel(row.status)
                  }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="aiScore" label="AI得分" width="100" align="center">
                <template #default="{ row }">
                  <span v-if="row.aiScore !== null" class="score-text">{{ row.aiScore }}</span>
                  <span v-else class="text-muted">-</span>
                </template>
              </el-table-column>
              <el-table-column prop="teacherScore" label="教师评分" width="100" align="center">
                <template #default="{ row }">
                  <span v-if="row.teacherScore !== null" class="score-text">{{
                    row.teacherScore
                  }}</span>
                  <span v-else class="text-muted">-</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="320" align="center" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" type="primary" plain link @click="viewDetail(row)">
                    <el-icon><Monitor /></el-icon>
                    查看
                  </el-button>
                  <el-button
                    v-if="row.status === 'SUBMITTED'"
                    size="small"
                    type="success"
                    plain
                    link
                    @click="aiGrade(row.id)"
                  >
                    <el-icon><Cpu /></el-icon>
                    AI批改
                  </el-button>
                  <el-button
                    v-if="row.status === 'AI_PROCESSED'"
                    size="small"
                    type="warning"
                    plain
                    link
                    @click="teacherGrade(row)"
                  >
                    <el-icon><Edit /></el-icon>
                    教师复核
                  </el-button>
                  <el-popconfirm
                    title="确定要删除吗？"
                    confirm-button-text="确定"
                    cancel-button-text="取消"
                    @confirm="handleDelete(row)"
                  >
                    <template #reference>
                      <el-button size="small" type="danger" plain link>
                        <el-icon><Delete /></el-icon>
                        删除
                      </el-button>
                    </template>
                  </el-popconfirm>
                </template>
              </el-table-column>
            </el-table>

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
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="detailVisible" title="📌 作业详情" width="650px" destroy-on-close>
      <div v-if="detailData" class="detail-content">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">学生姓名</span>
            <span class="detail-value">{{ detailData.studentName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">任务名称</span>
            <span class="detail-value">{{ detailData.taskTitle }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">课程名称</span>
            <span class="detail-value">{{ detailData.courseName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">提交时间</span>
            <span class="detail-value">{{ formatDateValue(detailData.submitTime) }}</span>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">提交内容</div>
          <div class="detail-content-box">
            {{ detailData.submitText || '(无)' }}
          </div>
        </div>

        <div v-if="detailData.ocrRawText" class="detail-section">
          <div class="section-title">OCR识别结果</div>
          <div class="detail-content-box">
            {{ detailData.ocrRawText }}
          </div>
        </div>

        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">AI得分</span>
            <span class="detail-value">{{
              detailData.aiScore !== null ? detailData.aiScore : '-'
            }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">教师评分</span>
            <span class="detail-value">{{
              detailData.teacherScore !== null ? detailData.teacherScore : '-'
            }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">状态</span>
            <el-tag :type="getStatusType(detailData.status)">{{
              getStatusLabel(detailData.status)
            }}</el-tag>
          </div>
        </div>

        <div v-if="detailData.aiComment" class="detail-section">
          <div class="section-title">AI评语</div>
          <div class="detail-content-box">
            {{ detailData.aiComment }}
          </div>
        </div>

        <div v-if="detailData.teacherComment" class="detail-section">
          <div class="section-title">教师评语</div>
          <div class="detail-content-box">
            {{ detailData.teacherComment }}
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="gradeVisible" title="✍️ 教师复核评分" width="500px" destroy-on-close>
      <el-form
        ref="gradeFormRef"
        :model="gradeForm"
        :rules="gradeRules"
        label-width="100px"
        class="grade-form"
      >
        <el-form-item label="AI得分">
          <el-input v-model.number="gradeForm.aiScore" disabled />
        </el-form-item>
        <el-form-item label="教师评分" prop="teacherScore">
          <el-input-number v-model="gradeForm.teacherScore" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="评语" prop="teacherComment">
          <el-input
            v-model="gradeForm.teacherComment"
            type="textarea"
            :rows="4"
            placeholder="请输入教师评语"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="gradeVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTeacherGrade">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="taskDialogVisible" title="➕ 布置新作业" width="580px" destroy-on-close>
      <el-form
        ref="taskFormRef"
        :model="taskForm"
        :rules="taskRules"
        label-width="100px"
        class="task-form"
      >
        <el-form-item label="作业名称" prop="title">
          <el-input
            v-model="taskForm.title"
            placeholder="请输入作业名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="任务类型" prop="type">
          <el-select v-model="taskForm.type" placeholder="请选择任务类型" style="width: 100%">
            <el-option label="普通作业" value="HOMEWORK" />
            <el-option label="在线测评" value="EXAM" />
            <el-option label="实训项目" value="PRACTICE" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程选择" prop="courseId">
          <el-select v-model="taskForm.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option
              v-for="course in courseList"
              :key="course.id"
              :label="course.courseName"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="班级选择" prop="classId">
          <el-select v-model="taskForm.classId" placeholder="请选择班级" style="width: 100%">
            <el-option
              v-for="cls in classList"
              :key="cls.id"
              :label="cls.className"
              :value="cls.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="截止时间" prop="deadline">
          <el-date-picker
            v-model="taskForm.deadline"
            type="datetime"
            placeholder="请选择截止时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="作业描述" prop="contentText">
          <el-input
            v-model="taskForm.contentText"
            type="textarea"
            :rows="4"
            placeholder="请输入作业描述内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateTask" :loading="taskSubmitLoading"
          >发布</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Cpu,
  Monitor,
  Edit,
  Delete,
  Plus,
  Collection,
  DocumentChecked
} from '@element-plus/icons-vue'
import request from '@/api/request'
import { fetchSubmissionList, deleteSubmission } from '@/api/submission'
import { getTasksByTeacher, createTask, deleteTask as removeTask } from '@/api/task'
import { fetchTasks } from '@/api/task'

const SUBMITTED = 'SUBMITTED'
const AI_PROCESSED = 'AI_PROCESSED'
const GRADED = 'GRADED'

// 状态管理
const activeTab = ref('task')
const taskLoading = ref(false)
const submissionLoading = ref(false)
const taskSubmitLoading = ref(false)

// 数据
const taskList = ref([])
const submissionList = ref([])
const selectedIds = ref([])

// 弹窗状态
const taskDialogVisible = ref(false)
const detailVisible = ref(false)
const gradeVisible = ref(false)
const isEditGrade = ref(false)

// 表单引用
const taskFormRef = ref(null)
const gradeFormRef = ref(null)

// 详情数据
const detailData = ref(null)

// 评分表单
const gradeForm = reactive({
  submissionId: null,
  aiScore: null,
  teacherScore: null,
  teacherComment: ''
})

// 作业表单
const taskForm = reactive({
  title: '',
  type: 'HOMEWORK',
  courseId: null,
  classId: null,
  contentText: '',
  deadline: ''
})

// 搜索表单
const searchForm = reactive({
  studentName: '',
  taskTitle: '',
  status: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 模拟数据
const courseList = ref([
  { id: 1, courseName: '大模型应用与微调技术' },
  { id: 2, courseName: '软件工程' }
])

const classList = ref([
  { id: 1, className: '2024级软件工程1班' },
  { id: 2, className: '2024级软件工程2班' }
])

// 验证规则
const gradeRules = {
  teacherScore: [
    { required: true, message: '请输入教师评分', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '评分范围为0-100', trigger: 'blur' }
  ],
  teacherComment: [{ max: 500, message: '评语长度不能超过500个字符', trigger: 'blur' }]
}

const taskRules = {
  title: [{ required: true, message: '请输入作业名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  classId: [{ required: true, message: '请选择班级', trigger: 'change' }],
  deadline: [{ required: true, message: '请选择截止时间', trigger: 'change' }],
  contentText: [{ required: true, message: '请输入作业描述', trigger: 'blur' }]
}

// 计算统计数据
const stats = computed(() => {
  const total = submissionList.value.length
  const submitted = submissionList.value.filter((s) => s.status === SUBMITTED).length
  const aiProcessed = submissionList.value.filter((s) => s.status === AI_PROCESSED).length
  const graded = submissionList.value.filter((s) => s.status === GRADED).length
  return { total, submitted, aiProcessed, graded }
})

// ==================== 日期处理函数 ====================
/**
 * 解析日期值 - 处理各种格式
 * @param {*} dateValue - 日期值（字符串、时间戳、数组或 Proxy Array）
 * @returns {Date | null}
 */
function parseDate(dateValue) {
  if (!dateValue) return null

  try {
    // 情况1: 字符串格式
    if (typeof dateValue === 'string') {
      const date = new Date(dateValue)
      return isNaN(date.getTime()) ? null : date
    }

    // 情况2: 数字（时间戳）
    if (typeof dateValue === 'number') {
      const date = new Date(dateValue)
      return isNaN(date.getTime()) ? null : date
    }

    // 情况3: Array 或 Proxy(Array) - [year, month, day, hour, minute, second]
    if (Array.isArray(dateValue) || (typeof dateValue === 'object' && dateValue !== null)) {
      try {
        // 将 Proxy 转换为数组
        const arr = Array.isArray(dateValue) ? dateValue : [...dateValue]
        if (arr.length >= 3) {
          const [year, month, day, hour = 0, minute = 0, second = 0] = arr
          const date = new Date(year, month - 1, day, hour, minute, second)
          return isNaN(date.getTime()) ? null : date
        }
      } catch (e) {
        console.error('Array 日期解析失败:', e)
        return null
      }
    }

    // 情况4: Date 对象
    if (dateValue instanceof Date) {
      return isNaN(dateValue.getTime()) ? null : dateValue
    }

    return null
  } catch (error) {
    console.error('日期解析错误:', error, dateValue)
    return null
  }
}

/**
 * 格式化日期显示 - 用于 Formatter
 * @param {Object} row - 表格行数据
 * @param {Object} column - 列配置
 * @param {*} cellValue - 单元格值
 * @returns {string}
 */
function formatDateColumn(row, column, cellValue) {
  return formatDateValue(cellValue)
}

/**
 * 格式化日期值 - 用于具体字段
 * @param {*} dateValue - 日期值
 * @returns {string}
 */
function formatDateValue(dateValue) {
  const date = parseDate(dateValue)
  if (!date) {
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

/**
 * 格式化截止时间
 */
function formatDeadlineColumn(row, column, cellValue) {
  return formatDateValue(cellValue)
}

// ==================== Tab 切换 ====================
function handleTabChange(tabName) {
  if (tabName === 'task') {
    loadTaskList()
  } else if (tabName === 'submission') {
    loadSubmissionList()
  }
}

// ==================== 作业管理 ====================
async function loadTaskList() {
  taskLoading.value = true
  try {
    const res = await fetchTasks()
    if (res.code === 200) {
      taskList.value = res.data || []
    }
  } catch (error) {
    console.error('加载作业列表失败:', error)
    ElMessage.error('加载作业列表失败')
  } finally {
    taskLoading.value = false
  }
}

function openTaskDialog() {
  taskForm.title = ''
  taskForm.type = 'HOMEWORK'
  taskForm.courseId = null
  taskForm.classId = null
  taskForm.contentText = ''
  taskForm.deadline = ''
  taskDialogVisible.value = true
}

async function handleCreateTask() {
  if (!taskFormRef.value) return

  await taskFormRef.value.validate(async (valid) => {
    if (valid) {
      taskSubmitLoading.value = true
      try {
        const data = {
          title: taskForm.title,
          type: taskForm.type,
          courseId: taskForm.courseId,
          classId: taskForm.classId,
          contentText: taskForm.contentText,
          deadline: taskForm.deadline
        }
        const res = await createTask(data)
        if (res.code === 200) {
          ElMessage.success('作业布置成功')
          taskDialogVisible.value = false
          loadTaskList()
        } else {
          ElMessage.error(res.msg || '布置作业失败')
        }
      } catch (error) {
        console.error('布置作业失败:', error)
        ElMessage.error('布置作业失败，请稍后重试')
      } finally {
        taskSubmitLoading.value = false
      }
    }
  })
}

async function handleDeleteTask(row) {
  try {
    const res = await removeTask(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadTaskList()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    console.error('删除作业失败:', error)
    ElMessage.error('删除失败')
  }
}

function getTaskTypeTag(type) {
  switch (type) {
    case 'HOMEWORK':
      return ''
    case 'EXAM':
      return 'warning'
    case 'PRACTICE':
      return 'success'
    default:
      return 'info'
  }
}

function getTaskTypeLabel(type) {
  switch (type) {
    case 'HOMEWORK':
      return '普通作业'
    case 'EXAM':
      return '在线测评'
    case 'PRACTICE':
      return '实训项目'
    default:
      return type
  }
}

// ==================== 作业提交管理 ====================
async function loadSubmissionList() {
  submissionLoading.value = true
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
    submissionLoading.value = false
  }
}

function handleSearch() {
  pagination.currentPage = 1
  loadSubmissionList()
}

function handleReset() {
  searchForm.studentName = ''
  searchForm.taskTitle = ''
  searchForm.status = ''
  pagination.currentPage = 1
  loadSubmissionList()
}

function handleSizeChange(val) {
  pagination.pageSize = val
  pagination.currentPage = 1
  loadSubmissionList()
}

function handleCurrentChange(val) {
  pagination.currentPage = val
  loadSubmissionList()
}

function handleSelectionChange(val) {
  selectedIds.value = val.map((item) => item.id)
}

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

function viewDetail(row) {
  detailData.value = row
  detailVisible.value = true
}

async function aiGrade(id) {
  try {
    ElMessage.info('正在进行AI批改，请稍候...')
    const res = await request.post(`/api/submission/ai-grade/${id}`)
    if (res.code === 200) {
      ElMessage.success('AI批改完成')
      loadSubmissionList()
    } else {
      ElMessage.error(res.msg || 'AI批改失败')
    }
  } catch (error) {
    console.error('AI批改失败:', error)
    const errorMsg = error.response?.data?.msg || error.message || 'AI批改失败，请稍后重试'
    ElMessage.error(errorMsg)
  }
}

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

    let successCount = 0
    for (const id of selectedIds.value) {
      try {
        const res = await request.post(`/api/submission/ai-grade/${id}`)
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

function teacherGrade(row) {
  gradeForm.submissionId = row.id
  gradeForm.aiScore = row.aiScore
  gradeForm.teacherScore = null
  gradeForm.teacherComment = ''
  isEditGrade.value = false
  gradeVisible.value = true
}

async function handleTeacherGrade() {
  if (!gradeFormRef.value) return

  await gradeFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const data = {
          submissionId: gradeForm.submissionId,
          teacherComment: gradeForm.teacherComment,
          teacherScore: gradeForm.teacherScore
        }
        const res = await request.put('/api/submission/teacher-grade', data)
        if (res.code === 200) {
          ElMessage.success('批改完成')
          gradeVisible.value = false
          loadSubmissionList()
        } else {
          ElMessage.error(res.msg || '批改失败')
        }
      } catch (error) {
        console.error('批改失败:', error)
        ElMessage.error('批改失败，请稍后重试')
      }
    }
  })
}

async function handleDelete(row) {
  try {
    const res = await deleteSubmission(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadSubmissionList()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

// 初始化
onMounted(() => {
  loadTaskList()
})
</script>

<style scoped>
/* ==================== 通用变量与基础 ==================== */
:root {
  --color-primary: #3b82f6;
  --color-success: #10b981;
  --color-warning: #f59e0b;
  --color-danger: #ef4444;
  --color-info: #0ea5e9;
  --color-bg: #f8fafc;
  --color-border: #e2e8f0;
  --color-text: #1e293b;
  --color-text-secondary: #64748b;
  --border-radius: 12px;
  --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  --shadow-hover: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

* {
  box-sizing: border-box;
}

.submission-management {
  padding: 24px;
  background: var(--color-bg);
  min-height: 100vh;
}

/* ==================== 标签页 ==================== */
:deep(.main-tabs .el-tabs__header) {
  margin-bottom: 24px;
  background: white;
  border-radius: var(--border-radius);
  padding: 0 16px;
  box-shadow: var(--shadow-sm);
}

:deep(.main-tabs .el-tabs__nav) {
  border-bottom: none; /* 移除默认底边框，让界面更清爽 */
}

:deep(.main-tabs .el-tabs__item) {
  height: 48px;
  line-height: 48px;
  font-size: 15px;
}

:deep(.main-tabs .el-tabs__active-bar) {
  background: var(--color-primary) !important;
  height: 3px;
  border-radius: 3px 3px 0 0;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

/* ==================== 顶部信息栏 ==================== */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  gap: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-subtitle {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.btn-primary-custom {
  background: linear-gradient(135deg, var(--color-primary) 0%, #2563eb 100%);
  border: none;
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 600;
  color: #000000;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
  transition: all 0.3s ease;
}

.btn-primary-custom:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
}

.btn-primary-custom span {
  color: #ffffff;
  font-weight: 600;
}

/* ==================== 搜索区域 ==================== */
.search-bar {
  background: white;
  padding: 20px 20px 0 20px;
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-sm);
  margin-bottom: 20px;
  border: 1px solid var(--color-border);
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: flex-end;
}

.search-item {
  margin-bottom: 20px !important;
}

.search-input,
.search-select {
  width: 220px;
}

.search-buttons {
  margin-bottom: 20px;
  display: flex;
  gap: 12px;
}

/* ==================== 统计数据卡片 ==================== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  padding: 24px;
  border-radius: var(--border-radius);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  position: relative;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-hover);
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: var(--color-text);
  line-height: 1.2;
}

.stat-title {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}

.stat-icon {
  position: absolute;
  right: 24px;
  bottom: 24px;
  font-size: 40px;
  opacity: 0.15;
  transition: opacity 0.3s;
}

.stat-card:hover .stat-icon {
  opacity: 0.3;
}

/* 各卡片顶部标识线 */
.stat-card.total {
  border-top: 4px solid var(--color-primary);
}
.stat-card.pending {
  border-top: 4px solid var(--color-warning);
}
.stat-card.processing {
  border-top: 4px solid var(--color-info);
}
.stat-card.completed {
  border-top: 4px solid var(--color-success);
}

/* ==================== 表格卡片 ==================== */
:deep(.el-card) {
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-sm);
}

.card-header-bar {
  width: 4px;
  height: 18px;
  background: linear-gradient(180deg, var(--color-primary) 0%, #2563eb 100%);
  border-radius: 2px;
}

.custom-table {
  border-radius: 8px;
  overflow: hidden;
}

.score-text {
  font-weight: 600;
  color: var(--color-primary);
}

.text-muted {
  color: var(--color-text-secondary);
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* ==================== 弹窗详情排版 ==================== */
.detail-content {
  padding: 10px 0;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
  background: var(--color-bg);
  padding: 16px;
  border-radius: 8px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-label {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.detail-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
}

.detail-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}

.section-title::before {
  content: '';
  display: inline-block;
  width: 3px;
  height: 14px;
  background: var(--color-primary);
  border-radius: 2px;
  margin-right: 8px;
}

.detail-content-box {
  background: #fcfcfc;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 16px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--color-text);
  white-space: pre-wrap;
  min-height: 60px;
}

/* ==================== 表单排版 ==================== */
.task-form,
.grade-form {
  padding-right: 20px;
}
</style>
