<script setup>
import { onMounted, ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { fetchClasses, createClass, updateClass, deleteClass } from '@/api/class'

const auth = useAuthStore()
const classes = ref([])
const loading = ref(true)
const showModal = ref(false)
const editingClass = ref(null)
const searchQuery = ref('')
const errorMessage = ref('')

const filteredClasses = computed(() => {
  if (!searchQuery.value.trim()) return classes.value
  const query = searchQuery.value.toLowerCase()
  return classes.value.filter((c) => c.className.toLowerCase().includes(query))
})

onMounted(async () => {
  await loadClasses()
})

async function loadClasses() {
  loading.value = true
  errorMessage.value = ''
  try {
    const res = await fetchClasses(Number(auth.teacherId) || undefined)
    classes.value = res.data || []
  } catch (e) {
    errorMessage.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

function openCreateModal() {
  editingClass.value = {
    id: null,
    className: '',
    teacherId: Number(auth.teacherId) || 1
  }
  showModal.value = true
}

function openEditModal(cls) {
  editingClass.value = { ...cls }
  showModal.value = true
}

async function saveClass() {
  if (!editingClass.value.className.trim()) {
    errorMessage.value = '班级名称不能为空'
    return
  }
  errorMessage.value = ''
  try {
    if (editingClass.value.id) {
      await updateClass(editingClass.value.id, editingClass.value)
    } else {
      await createClass(editingClass.value)
    }
    showModal.value = false
    editingClass.value = null
    await loadClasses()
  } catch (e) {
    errorMessage.value = e.message || '保存失败'
  }
}

async function deleteClassConfirm(id) {
  if (!confirm('确定要删除这个班级吗？删除后无法恢复。')) return
  errorMessage.value = ''
  try {
    await deleteClass(id)
    await loadClasses()
  } catch (e) {
    errorMessage.value = e.message || '删除失败'
  }
}
</script>

<template>
  <div class="classes-page">
    <div class="page-header">
      <div>
        <h2>班级管理</h2>
        <p>管理您所负责的班级信息</p>
      </div>
      <button class="btn btn--primary" @click="openCreateModal">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
          <path
            d="M12 5v14M5 12h14"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
          />
        </svg>
        新增班级
      </button>
    </div>

    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>

    <div class="search-bar">
      <div class="search-input-wrapper">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
          <path
            d="M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16M21 21l-4.35-4.35"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="搜索班级名称..."
          class="search-input"
        />
      </div>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="filteredClasses.length" class="table-container">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>班级名称</th>
            <th>管理教师ID</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in filteredClasses" :key="c.id">
            <td>{{ c.id }}</td>
            <td>{{ c.className }}</td>
            <td>{{ c.teacherId }}</td>
            <td class="actions">
              <button class="btn btn--ghost btn--small" @click="openEditModal(c)">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                  <path
                    d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7M14.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
                编辑
              </button>
              <button
                class="btn btn--ghost btn--danger btn--small"
                @click="deleteClassConfirm(c.id)"
              >
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                  <path
                    d="M3 6h18M19 6v14c0 1.1-.9 2-2 2H7c-1.1 0-2-.9-2-2V6m3 0V4c0-1.1.9-2 2-2h4c1.1 0 2 .9 2 2v2"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-else class="empty">
      <p>暂无班级数据</p>
      <button class="btn btn--primary" @click="openCreateModal">添加班级</button>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ editingClass?.id ? '编辑班级' : '新增班级' }}</h3>
          <button class="modal-close" @click="showModal = false">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path
                d="M18 6L6 18M6 6l12 12"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
              />
            </svg>
          </button>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label class="form-label">班级名称</label>
            <input
              v-model="editingClass.className"
              type="text"
              class="form-input"
              placeholder="请输入班级名称"
            />
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn--ghost" @click="showModal = false">取消</button>
          <button class="btn btn--primary" @click="saveClass">
            {{ editingClass?.id ? '保存修改' : '创建班级' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.classes-page {
  max-width: 900px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 6px;
}

.page-header p {
  font-size: 14px;
  color: var(--text-muted);
  margin: 0;
}

.error-message {
  padding: 12px 16px;
  background: #fef2f2;
  border: 1px solid var(--danger);
  border-radius: var(--radius-md);
  color: var(--danger);
  font-size: 14px;
  margin-bottom: 20px;
}

.search-bar {
  margin-bottom: 20px;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  color: var(--text-muted);
}

.search-input-wrapper:focus-within {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 14px;
  color: var(--text-primary);
  outline: none;
  font-family: inherit;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
  border: none;
}

.btn--primary {
  background: var(--primary);
  color: #fff;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.25);
}

.btn--primary:hover {
  background: var(--primary-hover);
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.35);
}

.btn--ghost {
  background: transparent;
  color: var(--text-muted);
  border: 1px solid var(--border);
  padding: 8px 14px;
  font-size: 13px;
  font-weight: 500;
}

.btn--ghost:hover {
  background: var(--bg-hover);
  color: var(--text-secondary);
}

.btn--small {
  padding: 6px 10px;
  font-size: 12px;
  gap: 5px;
}

.btn--danger {
  border-color: var(--danger);
  color: var(--danger);
}

.btn--danger:hover {
  background: #fef2f2;
}

.table-container {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table thead {
  background: var(--bg-hover);
}

.table th {
  padding: 14px 20px;
  text-align: left;
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.table td {
  padding: 14px 20px;
  text-align: left;
  color: var(--text-secondary);
  font-size: 14px;
  border-bottom: 1px solid var(--border-light);
}

.table tbody tr:hover {
  background: var(--bg-hover);
}

.table tbody tr:last-child td {
  border-bottom: none;
}

.table td:first-child {
  color: var(--primary);
  font-weight: 500;
}

.actions {
  display: flex;
  gap: 8px;
}

.loading,
.empty {
  padding: 40px;
  text-align: center;
  color: var(--text-muted);
}

.empty p {
  margin-bottom: 16px;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  width: 90%;
  max-width: 400px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-light);
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.modal-close {
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 6px;
  border-radius: var(--radius-sm);
  color: var(--text-muted);
  transition: all 0.15s;
}

.modal-close:hover {
  background: var(--bg-hover);
  color: var(--text-secondary);
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.form-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  font-size: 14px;
  color: var(--text-primary);
  background: var(--bg-page);
  outline: none;
  transition: all 0.2s;
  font-family: inherit;
}

.form-input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 18px 24px;
  border-top: 1px solid var(--border-light);
}
</style>
