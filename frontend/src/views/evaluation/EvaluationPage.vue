<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Delete,
  MagicStick,
  Star,
  DataLine,
  View,
  ArrowRight,
  Check
} from '@element-plus/icons-vue'
import evaluationApi from '@/api/evaluation'
import courseApi from '@/api/course'
import * as echarts from 'echarts'

// ========== 数据 ==========
const reportList = ref([])
const loading = ref(false)
const courseList = ref([])

// ========== 搜索 ==========
const searchText = ref('')
const filteredList = computed(() => {
  if (!searchText.value) return reportList.value
  const kw = searchText.value.toLowerCase()
  return reportList.value.filter(
    (r) => r.courseName?.toLowerCase().includes(kw) || r.teacherName?.includes(searchText.value)
  )
})

// ========== 统计 ==========
const avgScore = computed(() => {
  if (!reportList.value.length) return '0.0'
  const sum = reportList.value.reduce((acc, r) => acc + (Number(r.avgSatisfaction) || 0), 0)
  return (sum / reportList.value.length / 10).toFixed(1)
})
const reportCount = computed(() => reportList.value.length)
const aiDiagnosedCount = computed(() => reportList.value.filter((r) => r.llmAnalysisReport).length)

// 最近已诊断的报告（供右侧摘要展示）
const latestDiagnosed = computed(() => reportList.value.find((r) => r.llmAnalysisReport) || null)

// ========== 图表 ==========
const lineChartRef = ref(null)
const areaChartRef = ref(null)
let lineChart = null
let areaChartInstance = null

function initCharts() {
  if (lineChartRef.value && !lineChart) {
    lineChart = echarts.init(lineChartRef.value, null, { renderer: 'canvas' })
  }
  if (areaChartRef.value && !areaChartInstance) {
    areaChartInstance = echarts.init(areaChartRef.value, null, { renderer: 'canvas' })
  }
}

function renderCharts() {
  const data = reportList.value
  const reversed = [...data].reverse()

  if (lineChart) {
    const labels = reversed.map((_, i) => `第${i + 1}周`)
    const scores = reversed.map((r) => Number(r.avgSatisfaction) || 0)

    lineChart.setOption({
      title: {
        text: '📈 满意度趋势',
        textStyle: { fontSize: 14, fontWeight: 600, color: '#334155' }
      },
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(255,255,255,0.95)',
        borderColor: '#e2e8f0',
        textStyle: { color: '#334155' },
        formatter: (params) => {
          const item = params[0]
          return `<strong>${item.name}</strong><br/>满意度: <span style="color:#3b82f6;font-weight:bold">${item.value}分</span>`
        }
      },
      grid: { top: 50, bottom: 35, left: 55, right: 25 },
      xAxis: {
        type: 'category',
        data: labels,
        axisLine: { lineStyle: { color: '#e2e8f0' } },
        axisLabel: { color: '#64748b', fontSize: 12 }
      },
      yAxis: {
        type: 'value',
        min: 60,
        max: 100,
        axisLine: { show: false },
        splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } },
        axisLabel: { color: '#94a3b8', formatter: '{value}分' }
      },
      series: [
        {
          type: 'line',
          data: scores,
          smooth: 0.4,
          symbol: 'circle',
          symbolSize: 8,
          lineStyle: { color: '#3b82f6', width: 3 },
          itemStyle: { color: '#3b82f6', borderColor: '#fff', borderWidth: 2 },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(59,130,246,0.4)' },
              { offset: 1, color: 'rgba(59,130,246,0.05)' }
            ])
          }
        }
      ]
    })
  }

  if (areaChartInstance) {
    const courses = reversed.map((r) => r.courseName?.substring(0, 4) || '')
    const scores = reversed.map((r) => Number(r.avgSatisfaction) || 0)

    areaChartInstance.setOption({
      title: {
        text: '💫 各科目评分',
        textStyle: { fontSize: 14, fontWeight: 600, color: '#334155' }
      },
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(255,255,255,0.95)',
        borderColor: '#e2e8f0',
        textStyle: { color: '#334155' }
      },
      grid: { top: 50, bottom: 35, left: 55, right: 25 },
      xAxis: {
        type: 'category',
        data: courses,
        axisLine: { lineStyle: { color: '#e2e8f0' } },
        axisLabel: { color: '#64748b', fontSize: 11, rotate: 15 }
      },
      yAxis: {
        type: 'value',
        min: 60,
        max: 100,
        axisLine: { show: false },
        splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } },
        axisLabel: { color: '#94a3b8' }
      },
      series: [
        {
          type: 'line',
          data: scores,
          smooth: 0.5,
          symbol: 'circle',
          symbolSize: 8,
          lineStyle: { color: '#8b5cf6', width: 3 },
          itemStyle: { color: '#8b5cf6', borderColor: '#fff', borderWidth: 2 },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(139,92,246,0.35)' },
              { offset: 1, color: 'rgba(139,92,246,0.05)' }
            ])
          }
        }
      ]
    })
  }
}

function handleResize() {
  lineChart?.resize()
  areaChartInstance?.resize()
}

// ========== 加载列表 ==========
async function fetchList() {
  loading.value = true
  try {
    const res = await evaluationApi.list()
    reportList.value = Array.isArray(res) ? res : []
    renderCharts()
  } catch (e) {
    console.error('获取评价报告列表失败', e)
    ElMessage.error('获取报告列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function fetchCourses() {
  try {
    const res = await courseApi.list()
    courseList.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error('获取课程列表失败', e)
  }
}

onMounted(async () => {
  await Promise.all([fetchList(), fetchCourses()])
  initCharts()
  renderCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  lineChart?.dispose()
  areaChartInstance?.dispose()
  window.removeEventListener('resize', handleResize)
})

// ========== AI 诊断 ==========
const aiLoading = ref(false)
const aiDialogVisible = ref(false)
const aiReportText = ref('')
const currentDiagnosingId = ref(null)

async function handleAiDiagnose(row) {
  aiLoading.value = true
  currentDiagnosingId.value = row.id
  aiDialogVisible.value = true
  aiReportText.value = ''

  try {
    const report = await evaluationApi.generateAiReport(row.id)
    aiReportText.value = report
    row.llmAnalysisReport = report
    ElMessage.success('AI诊断报告生成成功')
  } catch (e) {
    console.error('AI诊断失败', e)
    ElMessage.error('AI诊断报告生成失败，请稍后重试')
    aiDialogVisible.value = false
  } finally {
    aiLoading.value = false
    currentDiagnosingId.value = null
  }
}

function viewAiReport(row) {
  aiReportText.value = row.llmAnalysisReport
  aiDialogVisible.value = true
  aiLoading.value = false
}

// ========== 创建报告 ==========
const createDialogVisible = ref(false)
const createForm = reactive({
  courseId: null,
  teacherName: '',
  avgSatisfaction: null,
  responseCount: 0
})

function openCreateDialog() {
  createForm.courseId = null
  createForm.teacherName = ''
  createForm.avgSatisfaction = null
  createForm.responseCount = 0
  createDialogVisible.value = true
}

async function handleCreate() {
  if (!createForm.courseId || !createForm.teacherName) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    const course = courseList.value.find((c) => c.id === createForm.courseId)
    const data = {
      courseId: createForm.courseId,
      courseName: course?.courseName || '',
      teacherName: createForm.teacherName,
      avgSatisfaction: createForm.avgSatisfaction,
      responseCount: createForm.responseCount
    }
    await evaluationApi.create(data)
    createDialogVisible.value = false
    ElMessage.success('报告创建成功')
    fetchList()
  } catch (e) {
    console.error('创建失败', e)
    ElMessage.error('创建失败，请稍后重试')
  }
}

// ========== 删除 ==========
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该评价报告吗？', '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await evaluationApi.delete(row.id)
    ElMessage.success('报告删除成功')
    fetchList()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('删除失败', e)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

// ========== 批量AI诊断 ==========
async function handleBatchAiDiagnose() {
  const undiagnosedList = filteredList.value.filter((r) => !r.llmAnalysisReport)
  if (undiagnosedList.length === 0) {
    ElMessage.info('暂无需要诊断的报告')
    return
  }
  try {
    await ElMessageBox.confirm(
      `将对 ${undiagnosedList.length} 条未诊断报告进行AI批量诊断，是否继续？`,
      '批量AI诊断',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' }
    )
    aiLoading.value = true
    for (const row of undiagnosedList) {
      try {
        const report = await evaluationApi.generateAiReport(row.id)
        row.llmAnalysisReport = report
        await new Promise((resolve) => setTimeout(resolve, 300))
      } catch (e) {
        console.error(`诊断 ${row.courseName} 失败`, e)
      }
    }
    aiLoading.value = false
    ElMessage.success('批量AI诊断完成')
  } catch (e) {
    if (e !== 'cancel') {
      aiLoading.value = false
      ElMessage.error('批量诊断中断')
    }
  }
}
</script>

<template>
  <div class="evaluation-page">
    <!-- 统计卡片 -->
    <div class="stat-row">
      <div class="stat-card stat-card-primary">
        <div class="stat-icon">
          <el-icon><Star /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value-row">
            <span class="stat-value">{{ avgScore }}</span>
            <el-rate
              :model-value="Number(avgScore)"
              disabled
              show-score
              text-color="#ff9900"
              style="margin-left: 8px"
            />
          </div>
          <span class="stat-label">平均满意度得分</span>
        </div>
      </div>
      <div class="stat-card stat-card-success">
        <div class="stat-icon">
          <el-icon><DataLine /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-value">{{ reportCount }}</span>
          <span class="stat-label">评价报告总数</span>
        </div>
      </div>
      <div class="stat-card stat-card-purple">
        <div class="stat-icon">🤖</div>
        <div class="stat-content">
          <span class="stat-value">{{ aiDiagnosedCount }}</span>
          <span class="stat-label">AI 已诊断</span>
        </div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="chart-row">
      <div class="page-card chart-card">
        <div ref="lineChartRef" class="chart-container"></div>
      </div>

      <div class="page-card chart-card ai-card">
        <h3 class="card-title">🤖 最近 AI 诊断摘要</h3>

        <template v-if="latestDiagnosed">
          <div class="ai-highlight-box">
            <div class="ai-highlight-icon">💡</div>
            <p class="ai-highlight-text">
              {{ latestDiagnosed.llmAnalysisReport?.slice(0, 80) }}...
            </p>
          </div>
          <div class="ai-detail-card">
            <div class="ai-detail-header">
              <span class="ai-badge">🤖 AI</span>
              <span class="ai-time">{{
                latestDiagnosed.generateTime || latestDiagnosed.createTime
              }}</span>
            </div>
            <p class="ai-detail-preview">
              课程「{{ latestDiagnosed.courseName }}」满意度得分
              {{ latestDiagnosed.avgSatisfaction }} 分...
            </p>
            <el-button type="primary" class="ai-detail-btn" @click="viewAiReport(latestDiagnosed)">
              查看完整AI深度报告
              <el-icon class="el-icon--right"><ArrowRight /></el-icon>
            </el-button>
          </div>
        </template>
        <el-empty v-else description="暂无AI诊断数据" :image-size="80" />

        <div ref="areaChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 报告列表 -->
    <div class="page-card">
      <div class="page-header">
        <div>
          <h2>评价报告列表</h2>
          <p class="subtitle">查看历次教学效果评价，使用AI生成智能诊断报告</p>
        </div>
        <div class="header-actions">
          <el-input
            v-model="searchText"
            placeholder="搜索课程或教师..."
            clearable
            style="width: 220px"
          />
          <el-button type="primary" plain :icon="Plus" @click="openCreateDialog"
            >新建报告</el-button
          >
          <el-button
            type="primary"
            class="ai-generate-btn"
            :icon="MagicStick"
            @click="handleBatchAiDiagnose"
          >
            批量AI诊断
          </el-button>
        </div>
      </div>

      <el-table :data="filteredList" stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column label="课程名称" width="160">
          <template #default="{ row }">
            <span class="course-name">{{ row.courseName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="授课教师" width="100" align="center">
          <template #default="{ row }">
            <span class="teacher-name">{{ row.teacherName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="满意度得分" width="180" align="center">
          <template #default="{ row }">
            <div class="score-with-rate">
              <span
                class="score-badge"
                :class="{
                  high: Number(row.avgSatisfaction) >= 85,
                  mid: Number(row.avgSatisfaction) >= 70 && Number(row.avgSatisfaction) < 85,
                  low: Number(row.avgSatisfaction) < 70
                }"
              >
                {{ row.avgSatisfaction }}分
              </span>
              <el-rate
                :model-value="Number(row.avgSatisfaction) / 20"
                disabled
                score-template=""
                style="font-size: 12px"
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="参与人数" width="100" align="center">
          <template #default="{ row }">{{ row.responseCount || 0 }}人</template>
        </el-table-column>
        <el-table-column label="AI 诊断" width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.llmAnalysisReport" class="status-tag resolved">
              <el-icon><Check /></el-icon> 已生成
            </span>
            <span v-else class="status-tag inactive">未诊断</span>
          </template>
        </el-table-column>
        <el-table-column label="生成时间" width="170">
          <template #default="{ row }">
            {{ row.generateTime || row.createTime || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" :icon="MagicStick" type="primary" link :loading="aiLoading && currentDiagnosingId === row.id" @click="handleAiDiagnose(row)">
              AI诊断
            </el-button>
            <el-button v-if="row.llmAnalysisReport" size="small" :icon="View" link @click="viewAiReport(row)">
              查看
            </el-button>
            <el-button size="small" :icon="Delete" type="danger" link @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && !filteredList.length" description="暂无评价报告数据" />
    </div>

    <!-- 创建报告弹窗 -->
    <el-dialog v-model="createDialogVisible" title="新建评价报告" width="480px" destroy-on-close>
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="课程">
          <el-select v-model="createForm.courseId" placeholder="请选择课程" style="width: 100%">
            <el-option
              v-for="course in courseList"
              :key="course.id"
              :label="course.courseName"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="授课教师">
          <el-input v-model="createForm.teacherName" placeholder="请输入教师姓名" />
        </el-form-item>
        <el-form-item label="满意度得分">
          <el-input-number
            v-model="createForm.avgSatisfaction"
            :min="0"
            :max="100"
            :precision="1"
            :step="0.5"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="参与人数">
          <el-input-number
            v-model="createForm.responseCount"
            :min="0"
            :max="9999"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleCreate">创 建</el-button>
      </template>
    </el-dialog>

    <!-- AI 报告弹窗 -->
    <el-dialog
      v-model="aiDialogVisible"
      title="🤖 AI 教学效果诊断报告"
      width="650px"
      destroy-on-close
      class="ai-dialog"
    >
      <div class="ai-report-area">
        <div v-if="aiLoading" class="ai-loading">
          <div class="ai-spinner"></div>
          <p>AI 正在分析教学数据，请稍候...</p>
          <p class="ai-loading-hint">正在生成个性化诊断建议...</p>
        </div>
        <pre v-else class="ai-report-text">{{ aiReportText }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.evaluation-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 24px;
}

/* 统计卡片 */
.stat-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  flex-shrink: 0;
}

.stat-card-primary .stat-icon {
  background: linear-gradient(135deg, #3b82f6, #60a5fa);
  color: #fff;
}

.stat-card-success .stat-icon {
  background: linear-gradient(135deg, #10b981, #34d399);
  color: #fff;
}

.stat-card-purple .stat-icon {
  background: linear-gradient(135deg, #8b5cf6, #a78bfa);
  color: #fff;
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value-row {
  display: flex;
  align-items: center;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
}

/* 图表区 */
.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.page-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.chart-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chart-container {
  width: 100%;
  height: 240px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #334155;
  margin: 0;
}

.ai-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.ai-highlight-box {
  display: flex;
  gap: 12px;
  padding: 14px 16px;
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  border-radius: 10px;
  border-left: 4px solid #f59e0b;
  align-items: flex-start;
}

.ai-highlight-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.ai-highlight-text {
  margin: 0;
  font-size: 13px;
  color: #92400e;
  line-height: 1.6;
}

.ai-detail-card {
  padding: 14px 16px;
  background: #f8fafc;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}

.ai-detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.ai-badge {
  background: linear-gradient(135deg, #8b5cf6, #a78bfa);
  color: #fff;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.ai-time {
  font-size: 12px;
  color: #94a3b8;
}

.ai-detail-preview {
  margin: 0 0 10px;
  font-size: 13px;
  color: #475569;
  line-height: 1.5;
}

.ai-detail-btn {
  width: 100%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  color: #fff;
  font-weight: 500;
}

/* 列表区 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.subtitle {
  margin: 4px 0 0;
  font-size: 13px;
  color: #64748b;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.ai-generate-btn {
  background: linear-gradient(135deg, #8b5cf6, #a78bfa);
  border: none;
  color: #fff;
  font-weight: 500;
}

.course-name {
  font-weight: 500;
  color: #334155;
}

.teacher-name {
  color: #64748b;
}

.score-with-rate {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.score-badge {
  display: inline-block;
  padding: 3px 12px;
  border-radius: 16px;
  font-weight: 700;
  font-size: 14px;
}

.score-badge.high {
  background: #d1fae5;
  color: #059669;
}

.score-badge.mid {
  background: #fef3c7;
  color: #d97706;
}

.score-badge.low {
  background: #fee2e2;
  color: #dc2626;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 14px;
  font-size: 12px;
  font-weight: 500;
}

.status-tag.resolved {
  background: #d1fae5;
  color: #059669;
}

.status-tag.inactive {
  background: #f1f5f9;
  color: #94a3b8;
}

.ai-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  color: #fff;
}

/* AI弹窗 */
.ai-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 0;
  gap: 16px;
  color: #64748b;
}

.ai-loading p {
  margin: 0;
}

.ai-loading-hint {
  font-size: 13px;
  color: #94a3b8;
}

.ai-spinner {
  width: 42px;
  height: 42px;
  border: 4px solid #e2e8f0;
  border-top-color: #8b5cf6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.ai-report-text {
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.8;
  color: #334155;
  background: #f8fafc;
  padding: 24px;
  border-radius: 12px;
  margin: 0;
  max-height: 450px;
  overflow-y: auto;
  border: 1px solid #e2e8f0;
}

:deep(.ai-dialog .el-dialog__header) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 20px;
}

:deep(.ai-dialog .el-dialog__title) {
  color: #fff;
  font-weight: 600;
}

:deep(.ai-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: #fff;
}
</style>
