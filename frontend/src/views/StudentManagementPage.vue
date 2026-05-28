<template>
  <div class="student-management-page">
    <!-- 顶部搜索栏 -->
    <div class="search-bar">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="学生姓名">
          <el-input
            v-model="searchForm.studentName"
            placeholder="请输入学生姓名"
            class="search-input"
          />
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model="searchForm.studentNo" placeholder="请输入学号" class="search-input" />
        </el-form-item>
        <el-form-item label="所属班级">
          <el-select v-model="searchForm.classId" placeholder="请选择班级" class="search-select">
            <el-option label="全部班级" :value="''" />
            <el-option
              v-for="cls in classList"
              :key="cls.id"
              :label="cls.className"
              :value="cls.id"
            />
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

    <!-- 数据表格 -->
    <el-card class="table-card">
      <div class="table-header">
        <h3>学生列表</h3>
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>
          新增学生
        </el-button>
      </div>
      <el-table :data="studentList" stripe border :loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="studentNo" label="学号" width="150" />
        <el-table-column prop="studentName" label="姓名" width="120" />
        <el-table-column prop="className" label="所属班级" width="180" />
        <el-table-column prop="createTime" label="创建时间" width="200" :formatter="formatDate" />
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="openEditDialog(row)">
              <el-icon><Edit /></el-icon>
              ✏️ 编辑
            </el-button>
            <el-button size="small" type="danger" plain @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              🗑️ 删除
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" destroy-on-close>
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        class="dialog-form"
      >
        <el-form-item label="学生姓名" prop="studentName">
          <el-input v-model="formData.studentName" placeholder="请输入学生姓名" />
        </el-form-item>
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="formData.studentNo" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="所属班级" prop="classId">
          <el-select v-model="formData.classId" placeholder="请选择班级">
            <el-option
              v-for="cls in classList"
              :key="cls.id"
              :label="cls.className"
              :value="cls.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSubmit">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { fetchStudentList, createStudent, updateStudent, deleteStudent } from '@/api/student'
import { fetchClasses } from '@/api/class'

// 搜索表单
const searchForm = reactive({
  studentName: '',
  studentNo: '',
  classId: ''
})

// 分页配置
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 状态
const loading = ref(false)
const studentList = ref([])
const classList = ref([])

// 弹窗相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const formData = reactive({
  id: null,
  studentName: '',
  studentNo: '',
  classId: null
})

const dialogTitle = computed(() => (isEdit.value ? '修改学生' : '新增学生'))

// 表单校验规则
const formRules = {
  studentName: [
    { required: true, message: '请输入学生姓名', trigger: 'blur' },
    { min: 1, max: 50, message: '姓名长度在1-50个字符之间', trigger: 'blur' }
  ],
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { min: 1, max: 20, message: '学号长度在1-20个字符之间', trigger: 'blur' }
  ],
  classId: [{ required: true, message: '请选择班级', trigger: 'change' }]
}

// 初始化
onMounted(() => {
  loadClassList()
  loadStudentList()
})

// 加载班级列表
async function loadClassList() {
  try {
    const res = await fetchClasses()
    if (res.code === 200) {
      classList.value = res.data
    }
  } catch (error) {
    console.error('加载班级列表失败:', error)
  }
}

// 加载学生列表
async function loadStudentList() {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      studentName: searchForm.studentName || undefined,
      studentNo: searchForm.studentNo || undefined,
      classId: searchForm.classId ? Number(searchForm.classId) : undefined
    }
    const res = await fetchStudentList(params)
    if (res.code === 200) {
      studentList.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载学生列表失败:', error)
    ElMessage.error('加载学生列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.currentPage = 1
  loadStudentList()
}

// 重置
function handleReset() {
  searchForm.studentName = ''
  searchForm.studentNo = ''
  searchForm.classId = ''
  pagination.currentPage = 1
  loadStudentList()
}

// 分页大小改变
function handleSizeChange(val) {
  pagination.pageSize = val
  pagination.currentPage = 1
  loadStudentList()
}

// 当前页改变
function handleCurrentChange(val) {
  pagination.currentPage = val
  loadStudentList()
}

// 打开新增弹窗
function openAddDialog() {
  isEdit.value = false
  resetFormData()
  dialogVisible.value = true
}

// 打开编辑弹窗
function openEditDialog(row) {
  isEdit.value = true
  formData.id = row.id
  formData.studentName = row.studentName
  formData.studentNo = row.studentNo
  formData.classId = row.classId
  dialogVisible.value = true
}

// 重置表单数据
function resetFormData() {
  formData.id = null
  formData.studentName = ''
  formData.studentNo = ''
  formData.classId = null
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    const data = {
      studentName: formData.studentName,
      studentNo: formData.studentNo,
      classId: formData.classId
    }

    if (isEdit.value) {
      data.id = formData.id
      await updateStudent(data)
      ElMessage.success('学生信息更新成功')
    } else {
      await createStudent(data)
      ElMessage.success('学生信息创建成功')
    }

    dialogVisible.value = false
    loadStudentList()
  } catch (error) {
    console.error('提交失败:', error)
    if (error.response?.data?.msg) {
      ElMessage.error(error.response.data.msg)
    } else {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    }
  }
}

// 删除学生
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除学生「${row.studentName}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteStudent(row.id)
    ElMessage.success('删除成功')
    loadStudentList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 日期格式化
function formatDate(row, column) {
  if (!row.createTime) return ''

  let date
  // 处理数组格式的日期 [year, month, day, hour, minute, second]
  if (Array.isArray(row.createTime)) {
    const [year, month, day, hour, minute, second] = row.createTime
    // 注意：JavaScript 的月份是从 0 开始的，需要减 1
    date = new Date(year, month - 1, day, hour || 0, minute || 0, second || 0)
  } else if (typeof row.createTime === 'string') {
    // 处理字符串格式的日期
    date = new Date(row.createTime)
  } else {
    return ''
  }

  // 验证日期是否有效
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
</script>

<style scoped>
.student-management-page {
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

.search-form {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-input {
  width: 180px;
}

.search-select {
  width: 200px;
}

.table-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e8e8e8;
}

.table-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e8e8e8;
}

.dialog-form {
  padding: 10px 0;
}

.el-form-item__label {
  font-weight: 500;
}
</style>
