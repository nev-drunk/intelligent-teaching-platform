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

          <!-- 作业类型 Tab -->
          <div class="type-tabs">
            <button :class="['type-tab', { active: typeFilter === '' }]" @click="typeFilter = ''">
              全部
            </button>
            <button
              :class="['type-tab', { active: typeFilter === 'HOMEWORK' }]"
              @click="typeFilter = 'HOMEWORK'"
            >
              📝 普通作业
            </button>
            <button
              :class="['type-tab', { active: typeFilter === 'CHOICE' }]"
              @click="typeFilter = 'CHOICE'"
            >
              📋 选择题
            </button>
            <button
              :class="['type-tab', { active: typeFilter === 'EXAM' }]"
              @click="typeFilter = 'EXAM'"
            >
              📄 卷子
            </button>
          </div>

          <div class="search-bar">
            <el-form :model="searchForm" class="search-form">
              <el-form-item label="任务" class="search-item">
                <el-select
                  v-model="searchForm.taskId"
                  placeholder="选择任务"
                  clearable
                  class="search-select"
                  @change="handleSearch"
                >
                  <el-option
                    v-for="t in taskList"
                    :key="t.id"
                    :label="`[${getTaskTypeLabel(t.type)}] ${t.title}`"
                    :value="t.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="学生姓名" class="search-item">
                <el-input
                  v-model="searchForm.studentName"
                  placeholder="学生姓名"
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
                  <el-option label="AI已批改" value="AI_PROCESSED" />
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
              :data="filteredSubmissionList"
              stripe
              border
              :loading="submissionLoading"
              style="width: 100%"
              @selection-change="handleSelectionChange"
              class="custom-table"
            >
              <el-table-column type="selection" width="50" />
              <el-table-column prop="id" label="ID" width="70" align="center" />
              <el-table-column label="类型" width="90" align="center">
                <template #default="{ row }">
                  <span class="type-tag" :class="'type-' + getTaskType(row.taskId)">
                    {{ getTaskTypeLabel(getTaskType(row.taskId)) }}
                  </span>
                </template>
              </el-table-column>
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
              <el-table-column label="图片" width="70" align="center">
                <template #default="{ row }">
                  <el-tooltip :content="row.fileUrl ? '有上传图片' : '纯文本提交，无图片'">
                    <span :style="{ fontSize: '18px' }">{{ row.fileUrl ? '🖼️' : '📝' }}</span>
                  </el-tooltip>
                </template>
              </el-table-column>
              <el-table-column label="查重" width="100" align="center">
                <template #default="{ row }">
                  <span v-if="row.plagiarismRate === null || row.plagiarismRate === undefined || row.plagiarismRate === 0" class="text-muted">-</span>
                  <span v-else-if="row.plagiarismRate > 80" style="color:#ef4444;font-weight:700">{{ row.plagiarismRate }}%</span>
                  <span v-else-if="row.plagiarismRate > 50" style="color:#e6a23c;font-weight:600">{{ row.plagiarismRate }}%</span>
                  <span v-else style="color:#67c23a;font-weight:600">{{ row.plagiarismRate }}%</span>
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
              <el-table-column label="操作" width="380" align="center" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" type="primary" plain link @click="viewDetail(row)">
                    <el-icon><Monitor /></el-icon>
                    查看
                  </el-button>
                  <el-button size="small" type="primary" plain link @click="autoGrade(row.id)">
                    <el-icon><MagicStick /></el-icon>
                    AI 批改
                  </el-button>
                  <el-button size="small" type="warning" plain link @click="teacherGrade(row)">
                    <el-icon><Edit /></el-icon>
                    教师复核
                  </el-button>
                  <el-button
                    size="small"
                    type="danger"
                    plain
                    link
                    @click="checkStudentPlagiarism(row)"
                  >
                    <el-icon><Warning /></el-icon>查重
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

        <!-- 上传图片预览 -->
        <div v-if="detailData.fileUrl" class="detail-section">
          <div class="section-title">
            📷 上传图片
            <span v-if="layoutData" style="font-size: 12px; color: #8b5cf6; margin-left: 10px">
              (纸面检测: {{ layoutData.layout_boxes?.length || 0 }} 区域,
              {{ layoutData.ocr_regions?.length || 0 }} OCR)
            </span>
          </div>
          <div class="detail-image-wrapper">
            <img
              :src="'http://localhost:8081/' + detailData.fileUrl.replace(/\\/g, '/')"
              class="detail-preview-image"
              ref="detailImgRef"
              @load="onDetailImgLoad"
              @error="(e) => (e.target.style.display = 'none')"
            />
            <canvas
              ref="detailCanvasRef"
              class="detail-canvas-overlay"
              v-show="layoutData"
            ></canvas>
            <div v-if="layoutData" class="layout-legend">
              <span
                v-for="cat in detailLegend"
                :key="cat.label"
                class="legend-tag"
                :style="{ background: cat.bg, color: cat.color, borderColor: cat.color }"
              >
                {{ cat.name }}: {{ cat.count }}
              </span>
            </div>
          </div>
        </div>

        <div v-if="detailData.ocrRawText" class="detail-section">
          <div class="section-title">OCR识别结果</div>
          <div v-if="layoutData && layoutData.ocr_regions" class="ocr-regions-grid">
            <div v-for="(region, ri) in layoutData.ocr_regions" :key="ri" class="ocr-region-card">
              <div class="ocr-region-label">{{ region.box.label }} #{{ ri + 1 }}</div>
              <canvas :ref="(el) => setRegionCanvas(ri, el)" class="ocr-region-canvas"></canvas>
              <div class="ocr-region-text">{{ region.ocr_text || '(空)' }}</div>
              <div class="ocr-region-conf">{{ (region.ocr_confidence * 100).toFixed(0) }}%</div>
            </div>
          </div>
          <div v-else class="detail-content-box">
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

        <div
          class="detail-grid"
          v-if="detailData.plagiarismRate !== null || detailData.isCheated === 1"
        >
          <div class="detail-item">
            <span class="detail-label">抄袭检测率</span>
            <span
              class="detail-value"
              :style="{ color: detailData.plagiarismRate > 50 ? '#f56c6c' : '#67c23a' }"
            >
              {{ detailData.plagiarismRate }}%
            </span>
          </div>
          <div class="detail-item">
            <span class="detail-label">作弊判定</span>
            <el-tag :type="detailData.isCheated === 1 ? 'danger' : 'success'" size="small">
              {{ detailData.isCheated === 1 ? '⚠ 疑似作弊' : '✓ 正常' }}
            </el-tag>
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
          <el-select
            v-model="taskForm.type"
            placeholder="请选择任务类型"
            style="width: 100%"
            @change="onTaskTypeChange"
          >
            <el-option label="📝 普通作业（文本/DeepSeek判分）" value="HOMEWORK" />
            <el-option label="📋 选择题作业（答题卡+OCR批改）" value="CHOICE" />
            <el-option label="📄 在线测评（关联试卷）" value="EXAM" />
            <el-option label="🔬 实训项目" value="PRACTICE" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="taskForm.type === 'CHOICE'" label="答案模板" prop="answerTemplate">
          <el-input
            v-model="taskForm.answerTemplate"
            placeholder="[ANSWERS:B,D,A,C,B,A,D,C] 每道题的标准答案"
            maxlength="200"
          />
          <span style="font-size: 11px; color: #909399; margin-top: 4px"
            >格式: [ANSWERS:B,D,A,C] 每个字母对应一道选择题的正确答案</span
          >
        </el-form-item>
        <el-form-item v-if="taskForm.type === 'EXAM'" label="关联试卷" prop="paperId">
          <el-select
            v-model="taskForm.paperId"
            placeholder="选择试卷（可选）"
            style="width: 100%"
            clearable
          >
            <el-option v-for="p in paperList" :key="p.id" :label="p.title" :value="p.id" />
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

    <!-- 查重结果行内面板 -->
    <div v-if="plagiarismReport" class="plagiarism-panel">
      <div class="plagiarism-panel-header">
        <span>🔍 抄袭检测 — 任务 {{ plagiarismReport.taskId }}</span>
        <span>{{ plagiarismReport.totalStudents }}人 {{ plagiarismReport.totalPairs }}对</span>
        <span :style="{ color: plagiarismReport.anySuspicious ? '#ef4444' : '#10b981' }">
          {{
            plagiarismReport.anySuspicious
              ? '⚠ ' + plagiarismReport.comparisons.filter((p) => p.alert).length + '对疑似抄袭'
              : '✅ 未发现抄袭'
          }}
        </span>
        <el-button size="small" text @click="plagiarismReport = null">✕ 关闭</el-button>
      </div>
      <div class="plagiarism-pairs">
        <div
          v-for="(p, i) in plagiarismReport.comparisons"
          :key="i"
          class="plagiarism-pair"
          :class="{ alert: p.alert }"
          @click="togglePlagiarismDetail(i)"
        >
          <span class="pair-a">{{ p.studentA }}</span>
          <div class="pair-bar">
            <div
              class="pair-fill"
              :style="{
                width: p.similarity + '%',
                background: p.alert ? '#ef4444' : p.similarity > 50 ? '#e6a23c' : '#67c23a'
              }"
            ></div>
          </div>
          <span class="pair-b">{{ p.studentB }}</span>
          <span class="pair-pct">{{ Math.round(p.similarity) }}%</span>
          <span v-if="p.alert" class="pair-badge">⚠</span>
        </div>
      </div>
    </div>
  </div>

  <!-- 查重结果弹窗 -->
  <el-dialog v-model="plagiarismVisible" :title="'查重结果：' + plagiarismStudentName" width="720px" destroy-on-close>
    <!-- 提示 -->
    <div style="font-size:12px;color:#94a3b8;margin-bottom:12px;padding:6px 10px;background:#f8fafc;border-radius:6px">
      基于 版面检测 + 手写识别 → Jaccard Bigram 文本相似度比对
    </div>
    <!-- Loading -->
    <div v-if="plagiarismLoading" style="text-align:center;padding:40px">
      <el-icon class="is-loading" style="font-size:28px;color:#3b82f6"><Loading /></el-icon>
      <p style="color:#94a3b8;margin-top:12px">正在识别并比对...</p>
    </div>
    <!-- 空 -->
    <div v-else-if="!plagiarismResult || (!plagiarismResult.results || plagiarismResult.results.length === 0)" style="text-align:center;padding:30px;color:#94a3b8">
      ✅ 未发现相似提交
    </div>
    <!-- 结果 -->
    <div v-else>
      <div style="display:flex;gap:20px;margin-bottom:10px;font-size:13px;color:#475569">
        <span>已检测 <strong>{{ plagiarismResult.checkedCount }}</strong> 份提交</span>
        <span>最高相似度 <strong :style="{color: plagiarismResult.maxSimilarity > 80 ? '#ef4444' : '#10b981'}">{{ plagiarismResult.maxSimilarity }}%</strong></span>
        <el-tag v-if="plagiarismResult.currentCheated" type="danger" size="small">⚠ 疑似抄袭</el-tag>
      </div>
      <div v-for="(p, i) in plagiarismResult.results" :key="i" class="plagiarism-match" style="padding:10px;margin-bottom:8px;background:#f8fafc;border-radius:8px">
        <div style="display:flex;align-items:center;gap:10px">
          <span style="font-weight:600;color:#334155;min-width:100px">{{ p.studentName }}</span>
          <el-progress :percentage="p.similarity" :stroke-width="8"
            :color="p.similarity > 80 ? '#ef4444' : p.similarity > 50 ? '#e6a23c' : '#67c23a'"
            style="flex:1" />
          <span style="font-weight:700;font-size:13px">{{ p.similarity }}%</span>
          <el-tag v-if="p.similarity > 80" type="danger" size="small">疑似抄袭</el-tag>
          <el-tag v-else-if="p.similarity > 50" type="warning" size="small">可疑</el-tag>
          <el-button size="small" text type="primary" @click="showPlagiarismCompare(p)">对比</el-button>
        </div>
      </div>
    </div>
  </el-dialog>

  <!-- 文本对比弹窗 -->
  <el-dialog v-model="plagiarismCompareVisible" title="文本对比" width="800px" destroy-on-close>
    <div v-if="plagiarismCompareData" style="display:flex;gap:16px">
      <div style="flex:1;background:#f8fafc;border-radius:8px;padding:12px">
        <div style="font-weight:600;color:#2563eb;margin-bottom:8px">{{ plagiarismStudentName }}（当前）</div>
        <div style="font-size:13px;color:#334155;white-space:pre-wrap;max-height:400px;overflow-y:auto;line-height:1.6">{{ plagiarismCompareData.sourceText }}</div>
      </div>
      <div style="display:flex;align-items:center;font-weight:700;color:#94a3b8;flex-shrink:0">VS</div>
      <div style="flex:1;background:#f8fafc;border-radius:8px;padding:12px">
        <div style="font-weight:600;color:#dc2626;margin-bottom:8px">{{ plagiarismCompareData.studentName }}</div>
        <div style="font-size:13px;color:#334155;white-space:pre-wrap;max-height:400px;overflow-y:auto;line-height:1.6">{{ plagiarismCompareData.compareText }}</div>
      </div>
    </div>
  </el-dialog>
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
  DocumentChecked,
  MagicStick,
  Warning,
  Loading
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
  deadline: '',
  answerTemplate: '',
  paperId: null
})

const paperList = ref([])

async function loadPaperList() {
  try {
    const res = await request.get('/api/papers')
    paperList.value = Array.isArray(res.data) ? res.data : []
  } catch {}
}

function onTaskTypeChange(type) {
  if (type === 'CHOICE') {
    taskForm.answerTemplate = '[ANSWERS:B,D,A,C,B,A,D,C]'
  }
  if (type !== 'EXAM') {
    taskForm.paperId = null
  }
}

// 构建提交数据
function buildTaskData() {
  const data = {
    title: taskForm.title,
    type: taskForm.type,
    courseId: taskForm.courseId,
    classId: taskForm.classId,
    contentText: taskForm.contentText,
    deadline: taskForm.deadline
  }
  if (taskForm.type === 'CHOICE') {
    data.contentText = taskForm.answerTemplate + '\n' + taskForm.contentText
  }
  if (taskForm.type === 'EXAM' && taskForm.paperId) {
    data.paperId = taskForm.paperId
  }
  return data
}

// 搜索表单
const searchForm = reactive({
  studentName: '',
  taskId: null,
  status: ''
})
const typeFilter = ref('')

// 根据类型筛选任务列表
const filteredTaskList = computed(() => {
  if (!typeFilter.value) return taskList.value
  return taskList.value.filter((t) => t.type === typeFilter.value)
})

// 根据任务类型过滤提交列表
const filteredSubmissionList = computed(() => {
  if (!typeFilter.value) return submissionList.value
  return submissionList.value.filter((s) => {
    const t = taskList.value.find((t) => t.id === s.taskId)
    return t?.type === typeFilter.value
  })
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
  const list = filteredSubmissionList.value
  const total = list.length
  const submitted = list.filter((s) => s.status === SUBMITTED).length
  const aiProcessed = list.filter((s) => s.status === AI_PROCESSED).length
  const graded = list.filter((s) => s.status === GRADED).length
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
        const data = buildTaskData()
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
  const map = { HOMEWORK: '普通作业', CHOICE: '选择题', EXAM: '卷子', PRACTICE: '实训' }
  return map[type] || type || '未知'
}
function getTaskType(taskId) {
  const t = taskList.value.find((t) => t.id === taskId)
  return t?.type || ''
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

// 版面检测可视化
const detailCanvasRef = ref(null)
const detailImgRef = ref(null)
const layoutData = ref(null)
const detailLegend = ref([])

const LAYOUT_COLORS = {
  Text: { bg: '#dbeafe', color: '#2563eb' },
  Title: { bg: '#ede9fe', color: '#7c3aed' },
  Header: { bg: '#fef3c7', color: '#d97706' },
  Footer: { bg: '#e0e7ff', color: '#4f46e5' },
  Figure: { bg: '#fce7f3', color: '#db2777' },
  Table: { bg: '#d1fae5', color: '#059669' },
  'Table caption': { bg: '#ecfdf5', color: '#10b981' },
  Equation: { bg: '#fef9c3', color: '#ca8a04' },
  Reference: { bg: '#f1f5f9', color: '#64748b' }
}

function parseLayoutData() {
  try {
    const raw = detailData.value?.ocrRawText
    if (raw && raw.startsWith('{') && raw.includes('layout_boxes')) {
      return JSON.parse(raw)
    }
  } catch {}
  return null
}

function onDetailImgLoad() {
  // 图片加载完成后，如果有版面数据，渲染 Canvas 叠加层
  if (!layoutData.value || !detailImgRef.value || !detailCanvasRef.value) return

  const img = detailImgRef.value
  const canvas = detailCanvasRef.value
  const data = layoutData.value

  // Canvas 覆盖在图片上，匹配图片显示尺寸
  const rect = img.getBoundingClientRect()
  const wrapper = img.parentElement
  const wrapperRect = wrapper.getBoundingClientRect()

  canvas.style.position = 'absolute'
  canvas.style.left = rect.left - wrapperRect.left + 'px'
  canvas.style.top = rect.top - wrapperRect.top + 'px'
  canvas.width = img.naturalWidth
  canvas.height = img.naturalHeight
  canvas.style.width = rect.width + 'px'
  canvas.style.height = rect.height + 'px'

  const ctx = canvas.getContext('2d')

  data.layout_boxes.forEach((box) => {
    const x = box.x1,
      y = box.y1,
      w = box.x2 - box.x1,
      h = box.y2 - box.y1
    const colors = LAYOUT_COLORS[box.label] || { color: '#94a3b8', bg: '#f1f5f9' }
    ctx.fillStyle = colors.bg + '60'
    ctx.fillRect(x, y, w, h)
    ctx.strokeStyle = colors.color
    ctx.lineWidth = 2
    ctx.strokeRect(x, y, w, h)
    // 标签
    const label = `${box.label} ${Math.round((box.confidence || 0) * 100)}%`
    ctx.font = '11px "PingFang SC","Microsoft YaHei",sans-serif'
    const tm = ctx.measureText(label)
    ctx.fillStyle = colors.color
    ctx.fillRect(x, Math.max(0, y - 18), tm.width + 8, 18)
    ctx.fillStyle = '#fff'
    ctx.fillText(label, x + 4, Math.max(12, y - 5))
  })

  // 统计图例
  const counts = {}
  data.layout_boxes.forEach((b) => {
    counts[b.label] = (counts[b.label] || 0) + 1
  })
  detailLegend.value = Object.entries(counts).map(([label, count]) => ({
    label,
    name: label,
    count,
    bg: (LAYOUT_COLORS[label] || { bg: '#f1f5f9' }).bg,
    color: (LAYOUT_COLORS[label] || { color: '#94a3b8' }).color
  }))

  // 渲染裁剪区域缩略图
  setTimeout(() => renderRegionThumbnails(), 100)
}

// 裁剪区域缩略图
const regionCanvases = ref({})
function setRegionCanvas(idx, el) {
  if (el) regionCanvases.value[idx] = el
}

function renderRegionThumbnails() {
  if (!layoutData.value?.ocr_regions || !detailImgRef.value) return
  const img = detailImgRef.value

  layoutData.value.ocr_regions.forEach((region, idx) => {
    const canvas = regionCanvases.value[idx]
    if (!canvas) return
    const box = region.box
    const x = box.x1,
      y = box.y1,
      w = box.x2 - box.x1,
      h = box.y2 - box.y1
    if (w <= 0 || h <= 0) return

    // 计算缩放
    const scaleX = img.naturalWidth / img.clientWidth
    const scaleY = img.naturalHeight / img.clientHeight
    const sx = x,
      sy = y,
      sw = w,
      sh = h

    canvas.width = 120
    canvas.height = Math.min(80, sh * (120 / sw))
    const ctx = canvas.getContext('2d')
    ctx.drawImage(img, sx, sy, sw, sh, 0, 0, canvas.width, canvas.height)
    // 边框
    ctx.strokeStyle = '#3b82f6'
    ctx.lineWidth = 2
    ctx.strokeRect(0, 0, canvas.width, canvas.height)
  })
}

function viewDetail(row) {
  detailData.value = row
  layoutData.value = parseLayoutData()
  detailLegend.value = []
  regionCanvases.value = {}
  detailVisible.value = true
  if (layoutData.value) {
    // 等 DOM 渲染完成后渲染 Canvas
    setTimeout(() => {
      const img = detailImgRef.value
      if (img) {
        if (img.complete) {
          onDetailImgLoad()
          renderRegionThumbnails()
        } else {
          img.addEventListener(
            'load',
            () => {
              onDetailImgLoad()
              renderRegionThumbnails()
            },
            { once: true }
          )
        }
      }
    }, 400)
  }
}

/**
 * AI 批改
 * OCR识别 + 题型匹配评分 + DeepSeek AI评语 + 抄袭检测
 */
async function autoGrade(id) {
  const row = submissionList.value.find((s) => s.id === id)
  if (row && !row.fileUrl) {
    try {
      await ElMessageBox.confirm(
        '该提交没有上传图片文件，无法进行OCR识别和版面抄袭检测。AI将仅基于文本内容批改，请确认。',
        '⚠️ 无图片文件 — 无法OCR',
        { confirmButtonText: '仍然批改', cancelButtonText: '取消', type: 'warning' }
      )
    } catch {
      return
    }
  }

  try {
    ElMessage.info('正在进行AI全自动批改（含OCR+抄袭检测），请稍候...')
    const res = await request.post(`/api/submission/auto-grade/${id}`)
    if (res.code === 200) {
      const submission = res.data
      let msg = 'AI全自动批改完成'
      if (submission.isCheated === 1) {
        msg += ' — ⚠️ 检测到疑似抄袭'
      }
      if (submission.aiComment && submission.aiComment.includes('无图片')) {
        ElMessage.warning(msg + '（无图片，仅基于文本）')
      } else {
        ElMessage.success(msg)
      }
      loadSubmissionList()
    } else {
      ElMessage.error(res.msg || 'AI全自动批改失败')
    }
  } catch (error) {
    if (error === 'cancel') return
    console.error('AI全自动批改失败:', error)
    const errorMsg = error.response?.data?.msg || error.message || 'AI全自动批改失败，请稍后重试'
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
        const res = await request.post(`/api/submission/auto-grade/${id}`)
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

// 音频播放
// 抄袭检测
const plagiarismVisible = ref(false)
const plagiarismStudentName = ref('')
const plagiarismLoading = ref(false)
const plagiarismResult = ref(null)
const plagiarismCompareVisible = ref(false)
const plagiarismCompareData = ref(null)

async function checkStudentPlagiarism(row) {
  plagiarismStudentName.value = row.studentName
  plagiarismResult.value = null
  plagiarismLoading.value = true
  plagiarismVisible.value = true
  try {
    const res = await request.post('/api/plagiarism/check-by-submission/' + row.id)
    if (res.code === 200) {
      plagiarismResult.value = res.data
      loadSubmissionList()
    }
  } catch (e) {
    ElMessage.error('查重服务异常')
  } finally {
    plagiarismLoading.value = false
  }
}

function showPlagiarismCompare(pair) {
  plagiarismCompareData.value = pair
  plagiarismCompareVisible.value = true
}

function cleanOcrText(raw) {
  if (!raw) return '(无内容)'
  if (raw.startsWith('{')) {
    try { const j = JSON.parse(raw); return j.combined_text || j.note || raw } catch {}
  }
  return raw
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
  loadPaperList()
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

/* ==================== 类型 Tab ==================== */
.type-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.type-tab {
  padding: 8px 18px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fff;
  color: #64748b;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s;
}
.type-tab:hover {
  border-color: #3b82f6;
  color: #3b82f6;
}
.type-tab.active {
  background: #3b82f6;
  color: #fff;
  border-color: #3b82f6;
}

.type-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}
.type-HOMEWORK {
  background: #dbeafe;
  color: #2563eb;
}
.type-CHOICE {
  background: #fef3c7;
  color: #d97706;
}
.type-EXAM {
  background: #ede9fe;
  color: #7c3aed;
}
.type-PRACTICE {
  background: #d1fae5;
  color: #059669;
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

/* 图片预览 */
.detail-image-wrapper {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px;
  text-align: center;
  max-height: 400px;
  overflow: auto;
}
.detail-image-wrapper {
  position: relative;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px;
  text-align: center;
  max-height: 500px;
  overflow: auto;
}
.detail-canvas-overlay {
  pointer-events: none;
  border-radius: 4px;
}
/* OCR 区域卡片 */
.ocr-regions-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 8px;
}
.ocr-region-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 8px;
  text-align: center;
  width: 140px;
}
.ocr-region-label {
  font-size: 11px;
  color: #3b82f6;
  font-weight: 600;
  margin-bottom: 4px;
}
.ocr-region-canvas {
  width: 120px;
  height: auto;
  border-radius: 4px;
  border: 1px solid #e2e8f0;
}
.ocr-region-text {
  font-size: 13px;
  font-weight: 700;
  color: #1e293b;
  margin-top: 4px;
  font-family: 'Courier New', monospace;
}
.ocr-region-conf {
  font-size: 11px;
  color: #94a3b8;
}

.layout-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
  justify-content: center;
}
.legend-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  border: 1px solid;
  font-weight: 500;
}
.detail-preview-image {
  max-width: 100%;
  max-height: 380px;
  object-fit: contain;
  border-radius: 4px;
  display: block;
  margin: 0 auto;
}
.image-fallback-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #e6a23c;
  background: #fdf6ec;
  padding: 6px 12px;
  border-radius: 4px;
}

/* ==================== 表单排版 ==================== */
.task-form,
.grade-form {
  padding-right: 20px;
}

/* 抄袭检测行内面板 */
.plagiarism-panel {
  margin-top: 20px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
}
.plagiarism-panel-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  font-size: 13px;
  color: #475569;
}
.plagiarism-pairs {
  padding: 8px 12px;
  max-height: 300px;
  overflow-y: auto;
}
.plagiarism-pair {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s;
}
.plagiarism-pair:hover {
  background: #f1f5f9;
}
.plagiarism-pair.alert {
  background: #fef2f2;
}
.plagiarism-pair.alert:hover {
  background: #fee2e2;
}
.pair-a,
.pair-b {
  font-size: 13px;
  color: #334155;
  min-width: 80px;
}
.pair-a {
  text-align: right;
}
.pair-bar {
  flex: 1;
  height: 8px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
}
.pair-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.5s;
}
.pair-pct {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  min-width: 36px;
  text-align: center;
}
.pair-badge {
  font-size: 14px;
}
</style>
