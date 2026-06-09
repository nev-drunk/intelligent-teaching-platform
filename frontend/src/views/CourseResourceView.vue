<template>
  <div class="resource-page">
    <div class="page-header">
      <div class="header-left">
        <button class="btn-back" @click="goBack"><span>&larr;</span> 返回课程列表</button>
        <h1 class="page-title">课程资源管理</h1>
      </div>
      <button class="btn-primary" @click="showUploadModal = true">
        <span class="icon">+</span> 上传资源
      </button>
    </div>

    <div class="card">
      <div class="card-header">
        <span class="card-title">课件列表</span>
        <span class="card-subtitle">共 {{ resources.length }} 个资源</span>
      </div>
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>资源名称</th>
              <th>文件路径</th>
              <th>分割状态</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="res in resources" :key="res.id">
              <td class="cell-highlight">{{ res.title }}</td>
              <td class="cell-filename">{{ res.fileUrl }}</td>
              <td>
                <span
                  class="status-badge"
                  :class="res.segmentStatus === 1 ? 'status-done' : 'status-pending'"
                >
                  {{ res.segmentStatus === 1 ? '已处理' : '未处理' }}
                </span>
              </td>
              <td>{{ formatDate(res.updateTime) }}</td>
              <td class="cell-actions">
                <button class="btn-text btn-edit" @click="handleCrop(res)">图像分割</button>
                <button class="btn-text btn-info" @click="previewFile(res)">预览</button>
                <button
                  class="btn-text btn-layout"
                  @click="handleLayoutDetect(res)"
                  :disabled="layoutDetecting === res.id"
                >
                  {{ layoutDetecting === res.id ? '检测中...' : '🧩 课件分析' }}
                </button>
                <button
                  class="btn-text btn-paper"
                  @click="handlePaperAnalyze(res)"
                  :disabled="paperAnalyzing === res.id"
                >
                  {{ paperAnalyzing === res.id ? '分析中...' : '📄 试卷分析' }}
                </button>
                <button class="btn-text btn-danger" @click="handleDelete(res.id)">删除</button>
              </td>
            </tr>
            <tr v-if="resources.length === 0">
              <td colspan="5" class="cell-empty">暂无资源数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="showUploadModal" class="modal-overlay" @click.self="closeUploadModal">
      <div class="modal-card">
        <div class="modal-header">
          <h3>上传课程资源</h3>
          <button class="btn-close" @click="closeUploadModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>资源名称 <span class="required">*</span></label>
            <input v-model="uploadForm.title" type="text" placeholder="请输入资源名称" />
          </div>
          <div class="form-group">
            <label>选择文件 <span class="required">*</span></label>
            <div
              class="upload-area"
              @click="triggerFileInput"
              @drop.prevent="handleDrop"
              @dragover.prevent
            >
              <input
                ref="fileInput"
                type="file"
                style="display: none"
                @change="handleFileChange"
                accept=".ppt,.pptx,.pdf,.mp4,.jpg,.png"
              />
              <div v-if="!selectedFile" class="upload-placeholder">
                <div class="upload-icon">&#128193;</div>
                <p>点击或拖拽文件到此处上传</p>
                <span class="upload-hint">支持 PPT、PDF、视频、图片等格式</span>
              </div>
              <div v-else class="upload-file-info">
                <div class="file-icon">&#128196;</div>
                <div class="file-detail">
                  <p class="file-name">{{ selectedFile.name }}</p>
                  <p class="file-size">{{ formatFileSize(selectedFile.size) }}</p>
                </div>
                <button class="btn-remove-file" @click.stop="selectedFile = null">&times;</button>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="closeUploadModal">取消</button>
          <button class="btn-primary" @click="handleUpload" :disabled="uploading">
            {{ uploading ? '上传中...' : '确认上传' }}
          </button>
        </div>
      </div>
    </div>

    <!-- AI 版面检测结果弹窗 -->
    <div v-if="showLayoutModal" class="modal-overlay" @click.self="closeLayoutModal">
      <div class="ai-detection-layout">
        <!-- 左侧：图片预览 -->
        <div class="layout-left-preview">
          <canvas ref="layoutCanvasRef" class="layout-canvas"></canvas>
        </div>

        <!-- 右侧：操作面板 -->
        <div class="layout-right-panel">
          <div class="panel-header">🤖 检测区域列表</div>

          <div v-if="layoutBoxes.length === 0" class="layout-empty">未检测到区域</div>

          <!-- 分类筛选 -->
          <div v-if="layoutBoxes.length > 0" class="filter-section">
            <label
              v-for="cat in layoutCategories"
              :key="cat.key"
              class="category-filter-item"
              :style="{ borderColor: cat.color }"
            >
              <input type="checkbox" v-model="cat.checked" @change="onCategoryFilterChange" />
              <span class="category-filter-dot" :style="{ background: cat.color }"></span>
              {{ cat.name }}({{ cat.count }})
            </label>
            <div class="filter-actions">
              <el-button size="small" text @click="selectAllBoxes(true)">全选</el-button>
              <el-button size="small" text @click="selectAllBoxes(false)">取消</el-button>
            </div>
          </div>

          <!-- 滚动列表 -->
          <div class="scroll-list-container">
            <div
              v-for="({ box, idx }, i) in filteredLayoutBoxes"
              :key="idx"
              class="layout-box-item"
              :class="{
                'layout-box-item--checked': selectedBoxIndices.has(idx),
                'layout-box-item--hovered': hoveredBoxIndex === idx
              }"
              @click="toggleBoxSelection(idx)"
              @mouseenter="onBoxMouseEnter(idx)"
              @mouseleave="onBoxMouseLeave"
            >
              <input
                type="checkbox"
                :checked="selectedBoxIndices.has(idx)"
                @click.stop
                @change="toggleBoxSelection(idx)"
                class="layout-box-checkbox"
              />
              <span class="layout-box-color" :style="{ background: getLabelColor(box.label) }"></span>
              <span class="layout-box-label">{{ getLabelName(box.label) }}</span>
              <span class="layout-box-conf">{{ box.confidence != null ? Math.round(box.confidence * 100) : 0 }}%</span>
            </div>
          </div>

          <!-- 统计摘要 -->
          <div v-if="layoutSummary" class="layout-summary">
            <span v-if="layoutSummary.text_block_count" class="summary-tag" style="background:#dbeafe;color:#2563eb">文本块 {{ layoutSummary.text_block_count }}</span>
            <span v-if="layoutSummary.table_count" class="summary-tag" style="background:#d1fae5;color:#059669">表格 {{ layoutSummary.table_count }}</span>
            <span v-if="layoutSummary.diagram_count" class="summary-tag" style="background:#fef3c7;color:#d97706">图表 {{ layoutSummary.diagram_count }}</span>
            <span v-if="layoutSummary.formula_count" class="summary-tag" style="background:#ede9fe;color:#7c3aed">公式 {{ layoutSummary.formula_count }}</span>
          </div>

          <!-- 底部按钮 -->
          <div class="panel-footer">
            <span v-if="selectedBoxIndices.size > 0" class="selected-hint">
              已选 {{ selectedBoxIndices.size }} 个
            </span>
            <button class="btn-secondary" @click="closeLayoutModal">关闭</button>
            <button
              class="btn-primary"
              :disabled="selectedBoxIndices.size === 0 || savingCropped"
              @click="saveCroppedRegions"
            >
              {{ savingCropped ? '保存中...' : `📦 保存选中素材到资源库 (${selectedBoxIndices.size})` }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 试卷分析结果弹窗 -->
    <div v-if="showPaperModal" class="modal-overlay" @click.self="closePaperModal">
      <div class="ai-detection-layout">
        <div class="layout-left-preview">
          <canvas ref="paperCanvasRef" class="layout-canvas"></canvas>
        </div>
        <div class="layout-right-panel">
          <div class="panel-header">📄 试卷分析结果 (paper-8n)</div>
          <div v-if="paperResult" class="paper-stats">
            <div class="paper-stat-item">检测区域: <strong>{{ paperResult.layout_boxes?.length || 0 }}</strong></div>
            <div class="paper-stat-item">作答区: <strong>{{ paperResult.ocr_regions?.length || 0 }}</strong></div>
            <div class="paper-stat-item">异常分数: <strong>{{ (paperResult.anomaly_score * 100).toFixed(0) }}%</strong></div>
          </div>
          <div class="filter-section" v-if="paperResult?.layout_boxes">
            <span v-for="(count, label) in paperLabelCounts" :key="label" class="paper-label-tag" :style="{background: getLayoutColor(label)}">
              {{ label }}: {{ count }}
            </span>
          </div>
          <div class="scroll-list-container">
            <div v-if="paperResult?.ocr_regions?.length" class="paper-ocr-list">
              <div v-for="(r, i) in paperResult.ocr_regions" :key="i" class="paper-ocr-item">
                <span class="paper-ocr-idx">#{{ i+1 }}</span>
                <span class="paper-ocr-label">{{ r.box.label }}</span>
                <code class="paper-ocr-text">{{ r.ocr_text || '(空)' }}</code>
              </div>
            </div>
            <div v-else class="layout-empty">未检测到作答区域</div>
          </div>
          <div class="panel-footer">
            <button class="btn-secondary" @click="closePaperModal">关闭</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showCropModal" class="modal-overlay" @click.self="closeCropModal">
      <div class="modal-card modal-large">
        <div class="modal-header">
          <h3>图像分割 - 提取关键区域</h3>
          <button class="btn-close" @click="closeCropModal">&times;</button>
        </div>
        <div class="modal-body crop-body">
          <div class="crop-wrapper">
            <vue-cropper
              v-if="cropImageUrl"
              :key="cropperKey"
              ref="cropperRef"
              :img="cropImageUrl"
              :output-size="1"
              :output-type="'png'"
              :info="true"
              :can-scale="true"
              :auto-crop="true"
              :auto-crop-width="200"
              :auto-crop-height="200"
              :fixed-box="false"
              :fixed="false"
              :full="true"
              :center-box="true"
              :high="true"
              mode="contain"
              class="cropper-box"
            />
            <div v-else class="cropper-empty">请先上传图片资源</div>
          </div>
          <div class="crop-preview">
            <h4>分割预览</h4>
            <div class="preview-box">
              <img v-if="previewUrl" :src="previewUrl" alt="预览" />
              <div v-else class="preview-empty">框选区域后显示预览</div>
            </div>
            <div class="crop-actions">
              <button class="btn-secondary" @click="resetCrop">重置</button>
              <button class="btn-primary" @click="getCropData">获取裁剪结果</button>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="closeCropModal">关闭</button>
          <button class="btn-primary" @click="saveCropResult">保存分割结果</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import 'vue-cropper/dist/index.css'
import { VueCropper } from 'vue-cropper'
// 📌 引入你之前封装好的统一模块（请确保路径与你实际文件存放路径一致）
import courseApi from '@/api/course'

const route = useRoute()
const router = useRouter()
const courseId = route.params.courseId

const resources = ref([])
const showUploadModal = ref(false)
const showCropModal = ref(false)
const selectedFile = ref(null)
const fileInput = ref(null)
const uploading = ref(false)
const uploadForm = ref({ title: '' })

const cropperRef = ref(null)
const cropImageUrl = ref('')
const previewUrl = ref('')
const currentResource = ref(null)
const cropperKey = ref(0)

// AI 版面检测相关
const showLayoutModal = ref(false)
const layoutDetecting = ref(null)
const layoutBoxes = ref([])
const layoutCanvasRef = ref(null)
const layoutImageUrl = ref('')
const layoutSummary = ref(null)
const selectedBoxIndices = ref(new Set())
const hoveredBoxIndex = ref(null)
const savingCropped = ref(false)
const currentLayoutResourceId = ref(null)

// ── 试卷分析 (paper-8n) ──
const showPaperModal = ref(false)
const paperAnalyzing = ref(null)
const paperResult = ref(null)
const paperCanvasRef = ref(null)
const paperImageUrl = ref('')
const paperLabelCounts = computed(() => {
  if (!paperResult.value?.layout_boxes) return {}
  const c = {}
  paperResult.value.layout_boxes.forEach(b => { c[b.label] = (c[b.label] || 0) + 1 })
  return c
})

function getLayoutColor(label) {
  const m = { Text: '#2563eb', Title: '#7c3aed', Header: '#ea580c', Footer: '#4f46e5', Figure: '#db2777', Table: '#059669', Equation: '#ca8a04' }
  return m[label] || '#64748b'
}

async function handlePaperAnalyze(resource) {
  paperAnalyzing.value = resource.id
  try {
    const imageUrl = 'http://localhost:8081' + resource.fileUrl
    const res = await fetch(`http://localhost:8081/api/courses/resources/${resource.id}/paper-analyze`, {
      method: 'POST',
      headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
    })
    const result = await res.json()
    const data = result.data || result

    if (data.error) { alert('试卷分析失败: ' + data.error); return }

    paperResult.value = data
    paperImageUrl.value = imageUrl
    showPaperModal.value = true

    await new Promise(r => setTimeout(r, 200))
    renderPaperCanvas()
  } catch (e) {
    console.error('试卷分析失败:', e)
    alert('试卷分析服务不可用: ' + e.message)
  } finally {
    paperAnalyzing.value = null
  }
}

function renderPaperCanvas() {
  const canvas = paperCanvasRef.value
  if (!canvas || !paperImageUrl.value || !paperResult.value) return
  const img = new Image()
  img.crossOrigin = 'anonymous'
  img.onload = () => {
    canvas.width = img.naturalWidth
    canvas.height = img.naturalHeight
    const ctx = canvas.getContext('2d')
    ctx.drawImage(img, 0, 0)
    const boxes = paperResult.value.layout_boxes || []
    boxes.forEach(box => {
      const x = box.x1, y = box.y1, w = box.x2 - box.x1, h = box.y2 - box.y1
      const color = getLayoutColor(box.label)
      ctx.fillStyle = color + '30'
      ctx.fillRect(x, y, w, h)
      ctx.strokeStyle = color
      ctx.lineWidth = 3
      ctx.strokeRect(x, y, w, h)
      ctx.fillStyle = color
      const lbl = `${box.label} ${Math.round(box.confidence*100)}%`
      ctx.font = '11px sans-serif'
      const tm = ctx.measureText(lbl)
      ctx.fillRect(x, Math.max(0, y-16), tm.width+6, 16)
      ctx.fillStyle = '#fff'
      ctx.fillText(lbl, x+3, Math.max(11, y-4))
    })
  }
  img.src = paperImageUrl.value
}

function closePaperModal() {
  showPaperModal.value = false
  paperResult.value = null
  paperImageUrl.value = ''
}

// 分类筛选
const LAYOUT_CATEGORY_META = {
  text_block: { name: '文本块', color: '#3b82f6' },
  table: { name: '表格', color: '#10b981' },
  diagram: { name: '图表', color: '#f59e0b' },
  formula: { name: '公式', color: '#8b5cf6' }
}
const layoutCategories = ref([])

function rebuildCategories() {
  const counts = {}
  layoutBoxes.value.forEach((b) => {
    counts[b.label] = (counts[b.label] || 0) + 1
  })
  layoutCategories.value = Object.entries(LAYOUT_CATEGORY_META).map(([key, meta]) => ({
    key,
    name: meta.name,
    color: meta.color,
    count: counts[key] || 0,
    checked: true
  }))
}

const filteredLayoutBoxes = computed(() => {
  const enabled = new Set(
    layoutCategories.value.filter((c) => c.checked).map((c) => c.key)
  )
  return layoutBoxes.value
    .map((box, idx) => ({ box, idx }))
    .filter(({ box }) => enabled.has(box.label))
})

// 勾选/取消单个区域
function toggleBoxSelection(globalIdx) {
  const next = new Set(selectedBoxIndices.value)
  if (next.has(globalIdx)) next.delete(globalIdx)
  else next.add(globalIdx)
  selectedBoxIndices.value = next
}

// 鼠标悬停
function onBoxMouseEnter(idx) {
  hoveredBoxIndex.value = idx
}
function onBoxMouseLeave() {
  hoveredBoxIndex.value = null
}

// 监听选中/悬停变化 → 重绘 Canvas
watch([selectedBoxIndices, hoveredBoxIndex], () => {
  nextTick(() => renderLayoutCanvas())
}, { deep: true })

// 全选/取消全选（仅影响当前筛选可见的区域）
function selectAllBoxes(select) {
  const next = new Set(selectedBoxIndices.value)
  filteredLayoutBoxes.value.forEach(({ idx }) => {
    if (select) next.add(idx)
    else next.delete(idx)
  })
  selectedBoxIndices.value = next
}

// 分类筛选变化时
function onCategoryFilterChange() {
  // 不清空已有选择，只是切换可见性
}

// 加载原图用于 Canvas 裁剪
function loadImageForCrop(url) {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.onload = () => resolve(img)
    img.onerror = reject
    img.src = url
  })
}

// 核心：裁剪选中区域并上传为资源
async function saveCroppedRegions() {
  if (selectedBoxIndices.value.size === 0) return
  savingCropped.value = true

  try {
    const img = await loadImageForCrop(layoutImageUrl.value)
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')

    let successCount = 0
    const indices = [...selectedBoxIndices.value].sort((a, b) => a - b)

    for (const idx of indices) {
      const box = layoutBoxes.value[idx]
      if (!box) continue

      const x = Math.round(box.x1)
      const y = Math.round(box.y1)
      const w = Math.round(box.x2 - box.x1)
      const h = Math.round(box.y2 - box.y1)

      if (w <= 0 || h <= 0) continue

      // Canvas 裁剪
      canvas.width = w
      canvas.height = h
      ctx.clearRect(0, 0, w, h)
      ctx.drawImage(img, x, y, w, h, 0, 0, w, h)

      // 转 Blob → File
      const blob = await new Promise((resolve) => canvas.toBlob(resolve, 'image/png'))
      const file = new File([blob], `${getLabelName(box.label)}_${idx + 1}.png`, { type: 'image/png' })

      // 上传为新资源
      try {
        await courseApi.uploadResource(file, courseId, `[${getLabelName(box.label)}] 自动裁剪 #${idx + 1}`)
        successCount++
      } catch (e) {
        console.error(`上传区域 ${idx} 失败:`, e)
      }
    }

    alert(`素材保存完成！成功 ${successCount}/${indices.length} 个`)
    selectedBoxIndices.value = new Set()
    closeLayoutModal()
    fetchResources()
  } catch (e) {
    console.error('裁剪保存失败:', e)
    alert('素材保存失败: ' + e.message)
  } finally {
    savingCropped.value = false
  }
}

// 类别颜色映射
const LABEL_COLORS = {
  text_block: '#3b82f6',
  table: '#10b981',
  diagram: '#f59e0b',
  formula: '#8b5cf6'
}
const LABEL_NAMES = {
  text_block: '文本块',
  table: '表格',
  diagram: '图表',
  formula: '公式'
}

function getLabelColor(label) {
  return LABEL_COLORS[label] || '#94a3b8'
}

function getLabelName(label) {
  return LABEL_NAMES[label] || label
}

/**
 * AI 版面检测 — 调用后端接口
 */
async function handleLayoutDetect(resource) {
  layoutDetecting.value = resource.id
  currentLayoutResourceId.value = resource.id
  selectedBoxIndices.value = new Set()
  try {
    // 构建文件 URL（用于 Canvas 渲染）
    const imageUrl = 'http://localhost:8081' + resource.fileUrl

    // 调用后端课件分析接口（无需上传文件，直接根据已存储的 file_url 分析）
    const res = await fetch(
      `http://localhost:8081/api/courses/resources/${resource.id}/analyze`,
      {
        method: 'POST',
        headers: {
          Authorization: 'Bearer ' + localStorage.getItem('token')
        }
      }
    )
    const data = await res.json()

    if (data.code === 200 && data.data) {
      // 优先使用返回的检测结果
      const regions = data.data.segmentedRegions
      if (regions) {
        try {
          const parsed = JSON.parse(regions)
          // 支持两种格式: 纯数组 boxes 或 {boxes: [...], summary: {...}}
          if (Array.isArray(parsed)) {
            layoutBoxes.value = parsed
            layoutSummary.value = null
          } else if (parsed.boxes) {
            layoutBoxes.value = parsed.boxes
            layoutSummary.value = parsed.summary || null
          } else {
            layoutBoxes.value = []
            layoutSummary.value = null
          }
        } catch {
          layoutBoxes.value = []
          layoutSummary.value = null
        }
      } else {
        layoutBoxes.value = []
        layoutSummary.value = null
      }

      layoutImageUrl.value = imageUrl
      rebuildCategories()
      showLayoutModal.value = true

      // 在下一个 tick 渲染 Canvas
      await new Promise((r) => setTimeout(r, 100))
      renderLayoutCanvas()
    } else {
      alert('版面检测失败: ' + (data.msg || '未知错误'))
    }
  } catch (e) {
    console.error('AI版面检测失败:', e)
    alert('AI版面检测服务不可用，请确保 Flask 服务已启动: ' + e.message)
  } finally {
    layoutDetecting.value = null
  }
}

/**
 * 在 Canvas 上绘制图片和检测框
 */
function renderLayoutCanvas() {
  const canvas = layoutCanvasRef.value
  if (!canvas || !layoutImageUrl.value) return

  const ctx = canvas.getContext('2d')
  const img = new Image()
  img.crossOrigin = 'anonymous'

  img.onload = () => {
    canvas.width = img.naturalWidth
    canvas.height = img.naturalHeight

    // 绘制原图
    ctx.drawImage(img, 0, 0)

    // 绘制检测框
    layoutBoxes.value.forEach((box, idx) => {
      const x = box.x1
      const y = box.y1
      const w = box.x2 - box.x1
      const h = box.y2 - box.y1
      const categoryColor = getLabelColor(box.label)

      const isSelected = selectedBoxIndices.value.has(idx)
      const isHovered = hoveredBoxIndex.value === idx
      const isHighlighted = isSelected || isHovered

      if (isHighlighted) {
        // 红色半透明填充
        ctx.fillStyle = 'rgba(255, 0, 0, 0.15)'
        ctx.fillRect(x, y, w, h)
        // 红色粗边框
        ctx.strokeStyle = '#FF0000'
        ctx.lineWidth = 3
      } else {
        ctx.strokeStyle = categoryColor
        ctx.lineWidth = 2
      }
      ctx.strokeRect(x, y, w, h)

      // 标签背景
      const labelText = `${getLabelName(box.label)} ${box.confidence != null ? Math.round(box.confidence * 100) : 0}%`
      ctx.font = '13px "PingFang SC", "Microsoft YaHei", sans-serif'
      const textMetrics = ctx.measureText(labelText)
      const textWidth = textMetrics.width + 12
      const textHeight = 22

      ctx.fillStyle = isHighlighted ? '#FF0000' : categoryColor
      ctx.fillRect(x, y - textHeight - 2, textWidth, textHeight)

      ctx.fillStyle = '#ffffff'
      ctx.fillText(labelText, x + 6, y - 8)
    })
  }

  img.onerror = () => {
    console.error('图片加载失败')
  }

  img.src = layoutImageUrl.value
}

function closeLayoutModal() {
  showLayoutModal.value = false
  layoutBoxes.value = []
  layoutImageUrl.value = ''
  layoutSummary.value = null
  selectedBoxIndices.value = new Set()
  hoveredBoxIndex.value = null
  layoutCategories.value = []
  currentLayoutResourceId.value = null
}

// 1️⃣ 获取资源列表
// 1️⃣ 获取资源列表
const fetchResources = async () => {
  try {
    const res = await courseApi.getResourceList(courseId)
    // 💡 显式获取 res 里面的 data 数组
    if (res && res.code === 200) {
      resources.value = res.data || []
    } else {
      resources.value = res || [] // 兼容防错
    }
  } catch (err) {
    console.error('获取资源列表失败', err)
    alert(err.message || '获取资源列表失败')
  }
}

const triggerFileInput = () => {
  fileInput.value.click()
}

const handleFileChange = (e) => {
  const file = e.target.files[0]
  if (file) selectedFile.value = file
}

const handleDrop = (e) => {
  const file = e.dataTransfer.files[0]
  if (file) selectedFile.value = file
}

// 2️⃣ 上传资源文件
const handleUpload = async () => {
  if (!uploadForm.value.title.trim()) {
    alert('请输入资源名称')
    return
  }
  if (!selectedFile.value) {
    alert('请选择文件')
    return
  }

  uploading.value = true

  try {
    // 调用封装好的上传接口，不再使用零散的全局 axios
    await courseApi.uploadResource(selectedFile.value, courseId, uploadForm.value.title)
    alert('上传成功')
    closeUploadModal()
    fetchResources()
  } catch (err) {
    console.error('上传失败', err)
    alert(err.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

// 3️⃣ 删除特定资源
const handleDelete = async (id) => {
  if (!confirm('确定要删除这个资源吗？')) return
  try {
    await courseApi.deleteResource(id)
    alert('删除成功')
    fetchResources()
  } catch (err) {
    console.error('删除失败', err)
    alert(err.message || '删除失败')
  }
}

const handleCrop = (res) => {
  currentResource.value = res
  cropImageUrl.value = 'http://localhost:8081' + res.fileUrl
  cropperKey.value++
  showCropModal.value = true
  previewUrl.value = ''
}

const previewFile = (res) => {
  window.open('http://localhost:8081' + res.fileUrl, '_blank')
}

const getCropData = () => {
  cropperRef.value.getCropBlob((blob) => {
    previewUrl.value = URL.createObjectURL(blob)
  })
}

const resetCrop = () => {
  cropperRef.value.refresh()
  previewUrl.value = ''
}

// 4️⃣ 保存图像分割结果
const saveCropResult = async () => {
  if (!previewUrl.value) {
    alert('请先进行裁剪')
    return
  }

  try {
    const updateData = {
      id: currentResource.value.id,
      courseId: currentResource.value.courseId,
      title: currentResource.value.title,
      fileUrl: currentResource.value.fileUrl,
      segmentStatus: 1, // 成功传给后端 1
      segmentedRegions: JSON.stringify({ cropped: true, time: new Date().toISOString() })
    }

    // 发送更新请求
    await courseApi.updateResource(updateData)

    alert('分割结果已保存')
    closeCropModal()
    await fetchResources() // 重新获取列表，刷新状态标签
  } catch (err) {
    console.error('保存失败', err)
    alert(err.message || '保存失败')
  }
}

const closeUploadModal = () => {
  showUploadModal.value = false
  uploadForm.value.title = ''
  selectedFile.value = null
}

const closeCropModal = () => {
  showCropModal.value = false
  cropImageUrl.value = ''
  previewUrl.value = ''
  currentResource.value = null
}

const goBack = () => {
  router.push({ name: 'courses' })
}

const formatDate = (dateStr) => {
  // 1️⃣ 严格空值校验：如果没有时间，直接安全返回横线，不让后续报错
  if (dateStr === null || dateStr === undefined || dateStr === '' || dateStr === 'null') {
    return '-'
  }

  try {
    // 2️⃣ 特殊兼容：Java LocalDateTime 序列化出来的数组格式 [2026, 5, 29, 10, 30]
    if (Array.isArray(dateStr)) {
      if (dateStr.length >= 3) {
        return `${dateStr[0]}/${dateStr[1]}/${dateStr[2]}`
      }
      return '-'
    }

    let date

    // 3️⃣ 判断是否是纯数字（时间戳，如 1717000000000）
    if (!isNaN(dateStr) && !isNaN(parseFloat(dateStr))) {
      date = new Date(Number(dateStr))
    } else if (typeof dateStr === 'string') {
      // 4️⃣ 处理带有 'T' 的 ISO 字符串，剔除可能导致旧浏览器报错的毫秒尾巴
      const safeStr = dateStr.replace('T', ' ').split('.')[0]
      date = new Date(safeStr)
    } else {
      date = new Date(dateStr)
    }

    // 5️⃣ 如果解析出来是 Invalid Date，保底原样转换为字符串返回，绝不抛出错误
    if (date.toString() === 'Invalid Date') {
      return String(dateStr)
    }

    // 6️⃣ 成功解析，格式化输出：年/月/日
    return date.toLocaleDateString('zh-CN')
  } catch (error) {
    // 7️⃣ 兜底捕获：哪怕中间发生任何报错，也静默吃掉，原样返回，确保页面绝不崩溃
    console.error('时间解析发生意外错误，已拦截:', error)
    return typeof dateStr === 'object' ? JSON.stringify(dateStr) : String(dateStr)
  }
}
const formatFileSize = (size) => {
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  return (size / (1024 * 1024)).toFixed(2) + ' MB'
}

onMounted(fetchResources)
</script>

<style scoped>
/* 保持原样式不变，此处省略 */
.resource-page {
  padding: 24px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.btn-back {
  background: #f5f7fa;
  color: #606266;
  border: 1px solid #dcdfe6;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
}
.btn-back:hover {
  background: #e4e7ed;
}
.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}
.btn-primary {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: #fff;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}
.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}
.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}
.btn-secondary {
  background: #f5f7fa;
  color: #606266;
  border: 1px solid #dcdfe6;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}
.btn-secondary:hover {
  background: #e4e7ed;
}
.card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}
.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}
.card-subtitle {
  font-size: 13px;
  color: #909399;
}
.table-wrapper {
  padding: 0;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
}
.data-table th {
  background: #fafbfc;
  padding: 14px 24px;
  text-align: left;
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  border-bottom: 1px solid #f0f0f0;
}
.data-table td {
  padding: 14px 24px;
  font-size: 14px;
  color: #4a5568;
  border-bottom: 1px solid #f6f7f9;
}
.data-table tbody tr:hover {
  background: #f5f9ff;
  transition: background 0.2s ease;
}
.cell-highlight {
  font-weight: 600;
  color: #2c3e50;
}
.cell-filename {
  color: #909399;
  font-size: 13px;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}
.status-pending {
  background: #fdf6ec;
  color: #e6a23c;
}
.status-done {
  background: #f0f9eb;
  color: #67c23a;
}
.cell-actions {
  display: flex;
  gap: 12px;
}
.btn-text {
  background: none;
  border: none;
  font-size: 13px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s ease;
}
.btn-edit {
  color: #409eff;
}
.btn-edit:hover {
  background: rgba(64, 158, 255, 0.1);
}
.btn-danger {
  color: #f56c6c;
}
.btn-danger:hover {
  background: rgba(245, 108, 108, 0.1);
}
.btn-info {
  color: #67c23a;
}
.btn-info:hover {
  background: rgba(103, 194, 58, 0.1);
}
.cell-empty {
  text-align: center;
  color: #909399;
  padding: 40px;
}
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}
.modal-card {
  background: #fff;
  border-radius: 16px;
  width: 520px;
  max-width: 90vw;
  box-shadow: 0 20px 48px rgba(0, 0, 0, 0.15);
  animation: modalIn 0.3s ease;
}
.modal-large {
  width: 960px;
  max-width: 92vw;
}
@keyframes modalIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
.modal-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}
.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  color: #909399;
  cursor: pointer;
  line-height: 1;
}
.btn-close:hover {
  color: #606266;
}
.modal-body {
  padding: 24px;
}
.form-group {
  margin-bottom: 20px;
}
.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #4a5568;
}
.required {
  color: #f56c6c;
}
.form-group input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  font-size: 14px;
  color: #2c3e50;
  background: #fafbfc;
  transition: all 0.3s ease;
  box-sizing: border-box;
}
.form-group input:focus {
  outline: none;
  border-color: #409eff;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.15);
}
.upload-area {
  border: 2px dashed #dcdfe6;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafbfc;
}
.upload-area:hover {
  border-color: #409eff;
  background: #f5f9ff;
}
.upload-placeholder .upload-icon {
  font-size: 48px;
  margin-bottom: 12px;
}
.upload-placeholder p {
  font-size: 15px;
  color: #4a5568;
  margin: 0 0 8px;
}
.upload-hint {
  font-size: 12px;
  color: #909399;
}
.upload-file-info {
  display: flex;
  align-items: center;
  gap: 16px;
  text-align: left;
}
.file-icon {
  font-size: 36px;
}
.file-detail {
  flex: 1;
}
.file-name {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 4px;
}
.file-size {
  font-size: 12px;
  color: #909399;
  margin: 0;
}
.btn-remove-file {
  background: none;
  border: none;
  font-size: 20px;
  color: #909399;
  cursor: pointer;
}
.btn-remove-file:hover {
  color: #f56c6c;
}
.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
}
.crop-body {
  display: flex;
  gap: 24px;
  padding: 24px;
}
.crop-wrapper {
  flex: 1;
  min-height: 400px;
  background: #f5f7fa;
  border-radius: 12px;
  overflow: hidden;
}
.cropper-box {
  width: 100%;
  height: 400px;
}
.cropper-empty {
  width: 100%;
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;
}
.crop-preview {
  width: 240px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.crop-preview h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #4a5568;
}
.preview-box {
  flex: 1;
  background: #f5f7fa;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  min-height: 200px;
}
.preview-box img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}
.preview-empty {
  color: #909399;
  font-size: 13px;
}
.crop-actions {
  display: flex;
  gap: 10px;
}
.crop-actions .btn-secondary,
.crop-actions .btn-primary {
  flex: 1;
  padding: 8px 12px;
  font-size: 13px;
}

/* ── AI 版面检测样式 ── */
.btn-layout {
  color: #8b5cf6;
}
.btn-layout:hover {
  background: rgba(139, 92, 246, 0.1);
}
.btn-layout:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.btn-paper {
  color: #059669;
}
.btn-paper:hover {
  background: rgba(5, 150, 105, 0.1);
}
.btn-paper:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 试卷分析 */
.paper-stats {
  display: flex;
  gap: 12px;
  margin-bottom: 10px;
  flex-shrink: 0;
}
.paper-stat-item {
  font-size: 13px;
  color: #475569;
}
.paper-stat-item strong {
  color: #1e293b;
}
.paper-label-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  color: #334155;
  display: inline-block;
}
.paper-ocr-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.paper-ocr-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  background: #f8fafc;
  border-radius: 6px;
  font-size: 13px;
}
.paper-ocr-idx {
  color: #94a3b8;
  font-size: 11px;
  width: 24px;
}
.paper-ocr-label {
  color: #059669;
  font-weight: 500;
  font-size: 11px;
  min-width: 50px;
}
.paper-ocr-text {
  color: #1e293b;
  font-weight: 600;
  font-family: 'Courier New', monospace;
  background: #e2e8f0;
  padding: 1px 6px;
  border-radius: 3px;
}

/* ======== 弹窗主体 ======== */
.ai-detection-layout {
  display: flex !important;
  width: 960px;
  max-width: 92vw;
  height: 70vh;
  gap: 0;
  overflow: hidden;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 48px rgba(0, 0, 0, 0.15);
}

/* ======== 左侧：图片预览 ======== */
.layout-left-preview {
  flex: 1.5;
  height: 100%;
  background: #f7f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: auto;
  border-radius: 16px 0 0 16px;
}
.layout-left-preview canvas {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  display: block;
}

/* ======== 右侧：操作面板 ======== */
.layout-right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  border-left: 1px solid #e8e8e8;
  padding: 20px;
  min-width: 0;
}
.panel-header {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 12px;
  flex-shrink: 0;
}
.filter-section {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
  flex-shrink: 0;
}
.filter-actions {
  display: flex;
  gap: 4px;
  width: 100%;
}

/* ======== 滚动列表 ======== */
.scroll-list-container {
  flex: 1;
  overflow-y: auto !important;
  margin-bottom: 12px;
  padding-right: 5px;
  min-height: 0;
}

/* ======== 统计摘要 ======== */
.layout-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
  flex-shrink: 0;
}
.summary-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

/* ======== 底部按钮 ======== */
.panel-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
  padding-top: 12px;
  border-top: 1px solid #e8e8e8;
  flex-shrink: 0;
}
.layout-empty {
  font-size: 13px;
  color: #94a3b8;
  padding: 16px 0;
  text-align: center;
}
.layout-box-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  background: #f8fafc;
  border-radius: 6px;
  margin-bottom: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.15s;
}
.layout-box-item:hover {
  background: #eef2ff;
}
.layout-box-item--checked {
  background: #dbeafe;
  border: 1px solid #3b82f6;
}
.layout-box-item--hovered {
  background: #fef2f2;
  border: 1px solid #f87171;
}
.layout-box-checkbox {
  width: 14px;
  height: 14px;
  accent-color: #3b82f6;
  cursor: pointer;
  flex-shrink: 0;
}
.layout-box-color {
  width: 12px;
  height: 12px;
  border-radius: 3px;
  flex-shrink: 0;
}
.layout-box-label {
  flex: 1;
  color: #334155;
  font-weight: 500;
}
.layout-box-conf {
  color: #64748b;
  font-weight: 600;
  font-size: 12px;
}
.category-filter-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  background: #fff;
  transition: border-color 0.15s;
}
.category-filter-item:hover {
  border-color: #3b82f6;
}
.category-filter-dot {
  width: 8px;
  height: 8px;
  border-radius: 2px;
  flex-shrink: 0;
}
.selected-hint {
  font-size: 13px;
  color: #3b82f6;
  margin-right: auto;
}
</style>
