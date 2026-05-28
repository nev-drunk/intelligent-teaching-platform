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
        <div class="page-title">试卷组装</div>
        <div class="page-subtitle">勾选题目组合成试卷，设置信息后发布给指定班级</div>
      </div>
      <div class="top-bar-actions">
        <span>已选：<strong style="color:var(--blue-primary);font-size:var(--font-lg);">{{ state.selectedQuestionIds.size }}</strong> 题</span>
        <button class="btn btn-primary btn-sm" :disabled="state.selectedQuestionIds.size === 0" @click="previewPaper">预览试卷</button>
        <button class="btn btn-success btn-sm" :disabled="!canPublish" @click="publishPaper">发布试卷</button>
      </div>
    </div>
    <div class="assemble-layout">
      <!-- 左侧题目列表 -->
      <div class="card">
        <div class="card-header">
          <span class="card-title">选择题目</span>
          <div class="toolbar">
            <div class="search-input-wrap" style="max-width:240px;">
              <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="10" cy="10" r="7"/><line x1="16" y1="16" x2="22" y2="22"/></svg>
              <input type="text" placeholder="搜索题目..." v-model="searchTerm">
            </div>
            <select class="filter-select" v-model="typeFilter">
              <option value="">全部题型</option>
              <option value="SINGLE">单选题</option>
              <option value="MULTI">多选题</option>
              <option value="JUDGE">判断题</option>
              <option value="GAP">填空题</option>
            </select>
          </div>
        </div>
        <div class="card-body no-padding">
          <div class="table-wrap">
            <table>
              <thead>
              <tr>
                <th style="width:40px;"><input type="checkbox" class="table-checkbox" v-model="selectAll" @change="toggleAll"></th>
                <th>题干</th>
                <th style="width:80px;">题型</th>
                <th style="width:70px;">来源</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="q in filteredQuestions" :key="q.id" :class="{ 'selected-row': state.selectedQuestionIds.has(q.id) }">
                <td><input type="checkbox" class="table-checkbox" :checked="state.selectedQuestionIds.has(q.id)" @change="toggleQuestionSelection(q.id)"></td>
                <td><span class="text-ellipsis" :title="q.content">{{ q.content }}</span></td>
                <td>{{ typeLabel(q.type) }}</td>
                <td><span class="badge" :class="q.isLlmGenerated === 1 ? 'badge-red' : 'badge-green'">{{ q.isLlmGenerated === 1 ? 'AI' : '手动' }}</span></td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
      <!-- 右侧面板 -->
      <div class="assemble-right-panel">
        <div class="panel-title">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="var(--blue-primary)" stroke-width="2"><rect x="5" y="2" width="14" height="20" rx="2"/></svg>
          已选题目 <span class="selected-count">{{ state.selectedQuestionIds.size }}</span> 题
        </div>
        <ul class="selected-topic-list">
          <li v-if="selectedList.length === 0" style="color:var(--text-tertiary);justify-content:center;border-left-color:transparent;font-style:italic;">暂未选择题目</li>
          <li v-for="q in selectedList" :key="q.id">
            <span style="font-size:11px;color:var(--text-tertiary);">[{{ typeLabel(q.type) }}]</span>
            <span style="flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">{{ q.content }}</span>
            <button class="remove-topic" @click="removeQuestionFromSelection(q.id)">×</button>
          </li>
        </ul>
        <hr style="border:none;border-top:1px solid var(--border-light);">
        <div class="form-group">
          <label>课程 ID</label>
          <input type="number" placeholder="例如：1" v-model.number="state.paperCourseId">
        </div>
        <div class="form-group">
          <label>发布班级</label>
          <select v-model.number="state.paperClassId">
            <option :value="null" disabled>请选择班级</option>
            <option v-for="c in state.classes" :key="c.id" :value="c.id">{{ c.className }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>试卷名称</label>
          <input type="text" placeholder="例如：高一数学期中测验" v-model="state.paperName">
        </div>
        <div class="form-group">
          <label>总分</label>
          <input type="number" placeholder="100" v-model.number="state.paperScore">
        </div>
        <button class="btn btn-success btn-lg" style="width:100%;justify-content:center;" :disabled="!canPublish" @click="publishPaperHandler">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polygon points="12,2 22,10 18,10 18,22 6,22 6,10 2,10"/></svg>
          确认发布
        </button>
      </div>
    </div>
    <!-- 预览模态框 -->
    <PaperPreviewModal 
      :visible="showPreview" 
      :paper-name="state.paperName"
      :total-score="state.paperScore"
      :time-limit="state.paperTime"
      :questions="selectedList"
      @update:visible="showPreview = $event"
    />
  </div>
</template>

<script setup>
import { inject, ref, computed, onMounted } from 'vue';
import PaperPreviewModal from '@/components/exam/PaperPreviewModal.vue';

const global = inject('global');
const { state, toggleQuestionSelection, removeQuestionFromSelection, showToast, canPublish, loadQuestions, publishPaper } = global;

const searchTerm = ref('');
const typeFilter = ref('');
const selectAll = ref(false);
const showPreview = ref(false);

// 加载题目数据
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

const selectedList = computed(() => (state.questions || []).filter(q => state.selectedQuestionIds.has(q.id)));

const toggleAll = () => {
  const ids = filteredQuestions.value.map(q => q.id);
  if (selectAll.value) {
    ids.forEach(id => {
      if (!state.selectedQuestionIds.has(id)) toggleQuestionSelection(id);
    });
  } else {
    ids.forEach(id => {
      if (state.selectedQuestionIds.has(id)) toggleQuestionSelection(id);
    });
  }
};

const toggleClass = (id) => {
  const newSet = new Set(state.selectedClasses);
  newSet.has(id) ? newSet.delete(id) : newSet.add(id);
  state.selectedClasses = newSet;
};

const publishPaperHandler = async () => {
  if (!canPublish.value) return;
  if (!state.paperClassId) {
    showToast('⚠️ 请选择发布班级');
    return;
  }
  try {
    const count = state.selectedQuestionIds.size;
    const defaultScore = Math.floor(state.paperScore / count) || 5;
    const items = Array.from(state.selectedQuestionIds).map((id) => ({
      questionId: id,
      score: defaultScore
    }));
    const paperData = {
      courseId: state.paperCourseId || 1,
      title: state.paperName,
      classId: state.paperClassId,
      items
    };
    await publishPaper(paperData);
    showToast(`✅ 试卷「${state.paperName}」已成功发布！`);
    global.clearSelections();
  } catch (error) {
    console.error('发布试卷失败:', error);
    showToast('❌ 发布失败，请重试');
  }
};

const previewPaper = () => {
  if (selectedList.value.length === 0) {
    showToast('⚠️ 请先选择题目');
    return;
  }
  showPreview.value = true;
};

const typeLabel = (t) => ({ SINGLE: '单选题', MULTI: '多选题', JUDGE: '判断题', GAP: '填空题', ESSAY: '简答题' }[t] || t);
</script>