<template>
  <div class="course-page">
    <div class="page-header">
      <h1 class="page-title">课程管理</h1>
      <button class="btn-primary" @click="showAddModal = true">
        <span class="icon">+</span> 新建课程
      </button>
    </div>

    <div class="card">
      <div class="card-header">
        <span class="card-title">课程列表</span>
        <span class="card-subtitle">共 {{ courses.length }} 门课程</span>
      </div>
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th style="width: 20%">课程名称</th>
              <th style="width: 40%">课程简介</th>
              <th style="width: 20%">创建时间</th>
              <th style="width: 20%">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="course in courses" :key="course.id">
              <td class="cell-highlight">{{ course.courseName }}</td>
              <td>{{ course.description || '暂无简介' }}</td>
              <td>{{ formatDate(course.createTime) }}</td>
              <td class="cell-actions">
                <button class="btn-text btn-edit" @click="handleEdit(course)">编辑</button>
                <button class="btn-text btn-danger" @click="handleDelete(course.id)">删除</button>
                <button class="btn-text btn-info" @click="goToResource(course.id)">资源管理</button>
              </td>
            </tr>
            <tr v-if="courses.length === 0">
              <td colspan="4" class="cell-empty">暂无课程数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 新建/编辑课程弹窗 -->
    <div v-if="showAddModal || showEditModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-card">
        <div class="modal-header">
          <h3>{{ showEditModal ? '编辑课程' : '新建课程' }}</h3>
          <button class="btn-close" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>课程名称 <span class="required">*</span></label>
            <input v-model="form.courseName" type="text" placeholder="请输入课程名称" />
          </div>
          <div class="form-group">
            <label>课程简介</label>
            <textarea v-model="form.description" rows="4" placeholder="请输入课程简介"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="closeModal">取消</button>
          <button class="btn-primary" @click="handleSubmit">确认</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const courses = ref([])
const showAddModal = ref(false)
const showEditModal = ref(false)
const form = ref({
  id: null,
  courseName: '',
  description: ''
})

const teacherId = 1 // 当前登录教师ID，实际应从登录状态获取

const fetchCourses = async () => {
  try {
    const res = await axios.get(`/api/courses?teacherId=${teacherId}`)
    if (res.data.code === 200) {
      courses.value = res.data.data
    }
  } catch (err) {
    console.error('获取课程列表失败', err)
  }
}

const handleSubmit = async () => {
  if (!form.value.courseName.trim()) {
    alert('课程名称不能为空')
    return
  }
  try {
    const url = showEditModal.value ? `/api/courses/${form.value.id}` : '/api/courses'
    const method = showEditModal.value ? 'put' : 'post'
    const payload = showEditModal.value
      ? { courseName: form.value.courseName, description: form.value.description }
      : { courseName: form.value.courseName, description: form.value.description, teacherId }
    const res = await axios[method](url, payload)
    if (res.data.code === 200) {
      closeModal()
      fetchCourses()
    } else {
      alert(res.data.msg)
    }
  } catch (err) {
    console.error('保存课程失败', err)
    alert('保存失败')
  }
}

const handleEdit = (course) => {
  form.value = { id: course.id, courseName: course.courseName, description: course.description }
  showEditModal.value = true
}

const handleDelete = async (id) => {
  if (!confirm('确定要删除这门课程吗？')) return
  try {
    const res = await axios.delete(`/api/courses/${id}`)
    if (res.data.code === 200) {
      fetchCourses()
    } else {
      alert(res.data.msg)
    }
  } catch (err) {
    console.error('删除课程失败', err)
    alert('删除失败')
  }
}

const goToResource = (courseId) => {
  router.push({ name: 'course-resource', params: { courseId } })
}

const closeModal = () => {
  showAddModal.value = false
  showEditModal.value = false
  form.value = { id: null, courseName: '', description: '' }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

onMounted(fetchCourses)
</script>

<style>
.course-page {
  course-page {
    /* 确保它获取 100% 的宽度 */
    width: 100% !important;
    /* 强制覆盖可能存在的任何 max-width 限制 */
    max-width: none !important;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
  }

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
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
    width: 100% !important; /* 让卡片横向铺满 */
    /* margin-bottom: 24px; */
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
    width: 100%;
  }

  .data-table {
    width: 100% !important;
    border-collapse: collapse;
    table-layout: auto; /* 将 fixed 改为 auto 试试，让浏览器自动拉伸列宽 */
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
    word-break: break-all;
  }

  .data-table tbody tr:hover {
    background: #f5f9ff;
    transition: background 0.2s ease;
  }

  .cell-highlight {
    font-weight: 600;
    color: #2c3e50;
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

  /* 弹窗样式 */
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

  .form-group input,
  .form-group textarea {
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

  .form-group input:focus,
  .form-group textarea:focus {
    outline: none;
    border-color: #409eff;
    background: #fff;
    box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.15);
  }

  .form-group textarea {
    resize: vertical;
    min-height: 80px;
  }

  .modal-footer {
    padding: 16px 24px;
    border-top: 1px solid #f0f0f0;
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}
</style>
