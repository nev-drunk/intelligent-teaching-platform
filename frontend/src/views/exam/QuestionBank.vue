<template>
  <div class="view-container">
    <!-- 顶部Tabs -->
    <div class="tabs-bar">
      <router-link :to="{ name: 'exam-questions' }" class="tab-item" active-class="active">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="4" y="3" width="16" height="18" rx="2"/><line x1="8" y1="8" x2="16" y2="8"/></svg>
        题库管理
      </router-link>
      <router-link :to="{ name: 'exam-assemble' }" class="tab-item" active-class="active">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="5" y="2" width="14" height="20" rx="2"/><line x1="9" y1="7" x2="15" y2="7"/></svg>
        试卷组装
      </router-link>
      <router-link :to="{ name: 'exam-publish' }" class="tab-item" active-class="active">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="12,2 22,10 18,10 18,22 6,22 6,10 2,10"/></svg>
        发布管理
      </router-link>
    </div>

    <div class="top-bar">
      <div>
        <div class="page-title">题库管理</div>
        <div class="page-subtitle">管理全部试题，支持手动录入与AI出题</div>
      </div>
      <div class="top-bar-actions">
        <button class="btn btn-orange btn-sm" @click="aiPanelOpen = !aiPanelOpen">
          AI 出题
        </button>
        <button class="btn btn-primary btn-sm" @click="state.addModalVisible = true">手动录入</button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card" v-for="stat in stats" :key="stat.label">
        <div class="stat-icon-wrapper" :class="stat.color">
          <div class="stat-icon" :class="stat.color">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="4" y="3" width="16" height="18" rx="2"/><line x1="8" y1="8" x2="16" y2="8"/></svg>
          </div>
        </div>
        <div>
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <div class="card">
      <div class="card-body">
        <div class="toolbar">
          <div class="search-input-wrap">
            <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="10" cy="10" r="7"/><line x1="16" y1="16" x2="22" y2="22"/></svg>
            <input type="text" placeholder="搜索题目内容、知识点..." v-model="searchTerm">
          </div>
          <select class="filter-select" v-model="typeFilter">
            <option value="">全部题型</option>
            <option value="SINGLE">单选题</option>
            <option value="MULTI">多选题</option>
            <option value="JUDGE">判断题</option>
            <option value="GAP">填空题</option>
            <option value="ESSAY">简答题</option>
          </select>
          <button class="btn btn-ghost btn-sm" @click="clearFilters">清除筛选</button>
        </div>
      </div>
    </div>

    <!-- 表格 -->
    <div class="card">
      <div class="card-body no-padding">
        <div class="table-wrap">
          <table>
            <thead>
            <tr>
              <th style="width:40px;"></th>
              <th>题干</th>
              <th style="width:90px;">题型</th>
              <th style="width:80px;">来源</th>
              <th style="width:100px;">操作</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="q in filteredQuestions" :key="q.id">
              <td><input type="checkbox" class="table-checkbox" :checked="state.selectedQuestionIds.has(q.id)" @change="toggle(q.id)"></td>
              <td><span class="text-ellipsis" :title="q.content">{{ q.content }}</span></td>
              <td><span class="badge" :class="typeBadge(q.type)">{{ typeLabel(q.type) }}</span></td>
              <td><span class="badge" :class="q.isLlmGenerated === 1 ? 'badge-orange' : 'badge-green'">{{ q.isLlmGenerated === 1 ? 'AI生成' : '手动录入' }}</span></td>
              <td>
                <button class="btn btn-ghost btn-sm" @click="editQuestion(q)">编辑</button>
                <button class="btn btn-ghost btn-sm" style="color:var(--red-primary);" @click="deleteQuestion(q.id)">删除</button>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-bar">
      <span>共 <strong>{{ filteredQuestions.length }}</strong> 道题目</span>
      <div class="pagination-buttons">
        <button class="btn btn-ghost btn-sm" disabled>上一页</button>
        <button class="btn btn-primary btn-sm" style="min-width:36px;">1</button>
        <button class="btn btn-ghost btn-sm">2</button>
        <button class="btn btn-ghost btn-sm">3</button>
        <button class="btn btn-ghost btn-sm">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { inject, ref, computed, onMounted } from 'vue';

const global = inject('global');
const { state, toggleQuestionSelection, showToast, loadQuestions, deleteQuestion } = global;

const searchTerm = ref('');
const typeFilter = ref('');
const aiPanelOpen = ref(false);

onMounted(() => {
  loadQuestions();
});

const filteredQuestions = computed(() => {
  let list = state.questions || [];
  if (typeFilter.value) list = list.filter(q => q.type === typeFilter.value);
  const term = searchTerm.value.trim().toLowerCase();
  if (term) list = list.filter(q => q.content && q.content.toLowerCase().includes(term));
  return list;
});

const stats = computed(() => {
  const questions = state.questions || [];
  const singles = questions.filter(q => q.type === 'SINGLE').length;
  const multis = questions.filter(q => q.type === 'MULTI').length;
  const others = questions.filter(q => q.type === 'JUDGE' || q.type === 'GAP' || q.type === 'ESSAY').length;
  return [
    { label: '题目总数', value: questions.length, color: 'blue' },
    { label: '单选题', value: singles, color: 'blue' },
    { label: '多选题', value: multis, color: 'green' },
    { label: '判断/填空/简答', value: others, color: 'orange' },
  ];
});

const toggle = (id) => toggleQuestionSelection(id);

const clearFilters = () => {
  searchTerm.value = '';
  typeFilter.value = '';
};

const typeLabel = (type) => ({ SINGLE: '单选题', MULTI: '多选题', JUDGE: '判断题', GAP: '填空题', ESSAY: '简答题' }[type] || type);
const typeBadge = (type) => ({ SINGLE: 'badge-blue', MULTI: 'badge-green', JUDGE: 'badge-orange', GAP: 'badge-ghost', ESSAY: 'badge-ghost' }[type] || 'badge-ghost');

const editQuestion = (q) => {
  global.state.editingQuestion = q;
  state.addModalVisible = true;
};
</script>

<style scoped>
.view-container {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg, 24px);
}

.tabs-bar {
  display: flex;
  gap: 2px;
  padding-bottom: var(--spacing-md, 16px);
  border-bottom: 1px solid var(--border-light, #f3f4f6);
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary, #6b7280);
  text-decoration: none;
  border-bottom: 2px solid transparent;
  transition: all 0.15s ease-in-out;
  border-radius: 8px 8px 0 0;
}

.tab-item:hover {
  color: var(--text-primary, #4b5563);
  background: var(--bg-sidebar-hover, #f1f5f9);
}

.tab-item.active {
  color: var(--blue-primary, #2563eb);
  border-bottom-color: var(--blue-primary, #2563eb);
  font-weight: 600;
}

.pagination-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  color: var(--text-tertiary, #9ca3af);
  font-size: var(--font-sm, 13px);
}

.pagination-bar strong {
  color: var(--text-primary, #4b5563);
  font-weight: 600;
}

.pagination-buttons {
  display: flex;
  gap: 6px;
}

.stat-icon-wrapper {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md, 12px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon-wrapper.blue {
  background: var(--blue-light, #eff6ff);
}

.stat-icon-wrapper.green {
  background: var(--green-light, #f0fdf4);
}

.stat-icon-wrapper.orange {
  background: var(--orange-light, #fff7ed);
}

.stat-icon {
  width: 22px;
  height: 22px;
}

.stat-icon.blue {
  color: var(--blue-primary, #2563eb);
}

.stat-icon.green {
  color: var(--green-primary, #22c55e);
}

.stat-icon.orange {
  color: var(--orange-vital, #f97316);
}
</style>
