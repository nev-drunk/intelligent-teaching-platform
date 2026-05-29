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
import { ref, onMounted } from 'vue'
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
  width: 900px;
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
</style>
