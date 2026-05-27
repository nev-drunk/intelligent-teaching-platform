<script setup>
import { onMounted, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { fetchClasses } from '@/api/class'

const auth = useAuthStore()
const classes = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await fetchClasses(Number(auth.teacherId) || undefined)
    classes.value = res.data || []
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="classes-page">
    <p class="desc">基于 <code>tb_class</code> 表，展示当前教师所带班级。</p>
    <div v-if="loading" class="loading">加载中…</div>
    <table v-else-if="classes.length" class="table">
      <thead>
        <tr>
          <th>ID</th>
          <th>班级名称</th>
          <th>管理教师ID</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="c in classes" :key="c.id">
          <td>{{ c.id }}</td>
          <td>{{ c.className }}</td>
          <td>{{ c.teacherId }}</td>
        </tr>
      </tbody>
    </table>
    <p v-else class="empty">暂无班级数据</p>
  </div>
</template>

<style scoped>
.desc {
  color: var(--color-text-muted);
  margin: 0 0 24px;
  font-size: 14px;
}

.table {
  width: 100%;
  border-collapse: collapse;
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-card);
}

.table thead {
  background: var(--color-bg-hover);
}

.table th {
  padding: 14px 20px;
  text-align: left;
  color: var(--color-text-secondary);
  font-weight: 600;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.table td {
  padding: 14px 20px;
  text-align: left;
  color: var(--color-text-secondary);
  font-size: 14px;
  border-bottom: 1px solid var(--color-border-light);
  transition: background-color 0.15s ease;
}

.table tbody tr {
  transition: background-color 0.15s ease;
}

.table tbody tr:hover {
  background: var(--color-bg-hover);
}

.table tbody tr:last-child td {
  border-bottom: none;
}

.table tbody tr td:first-child {
  color: var(--color-tech-blue);
  font-weight: 500;
}

.loading,
.empty {
  color: var(--color-text-muted);
  padding: 40px;
  text-align: center;
}
</style>
