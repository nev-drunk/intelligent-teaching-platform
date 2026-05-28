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
        <div class="page-title">发布管理</div>
        <div class="page-subtitle">查看已发布的试卷与作业，追踪完成情况</div>
      </div>
    </div>
    <div class="card">
      <div class="card-body no-padding">
        <div class="table-wrap">
          <table>
            <thead>
            <tr>
              <th>试卷名称</th>
              <th style="width:120px;">课程ID</th>
              <th style="width:90px;">总分</th>
              <th style="width:120px;">创建时间</th>
              <th style="width:100px;">操作</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="paper in publishedPapers" :key="paper.id">
              <td><strong>{{ paper.title }}</strong></td>
              <td>{{ paper.courseId }}</td>
              <td>{{ paper.totalScore }}</td>
              <td>{{ paper.createTime ? new Date(paper.createTime).toLocaleDateString() : '-' }}</td>
              <td>
                <button class="btn btn-ghost btn-sm" @click="viewPaper(paper)">查看</button>
                <button class="btn btn-ghost btn-sm" style="color:var(--red-primary);" @click="deletePaper(paper.id)">删除</button>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
    <!-- 试卷详情模态框 -->
    <PaperDetailModal 
      :visible="showDetailModal" 
      :paper="selectedPaper"
      @close="showDetailModal = false"
    />
  </div>
</template>

<script setup>
import { inject, computed, onMounted, ref } from 'vue';
import PaperDetailModal from '@/components/exam/PaperDetailModal.vue';
import { getPaper } from '@/api/paper';

const global = inject('global');
const { showToast, loadPapers, deletePaperFromState } = global;

const showDetailModal = ref(false);
const selectedPaper = ref({});

// 加载试卷数据
onMounted(() => {
  loadPapers();
});

const publishedPapers = computed(() => {
  return global.state.papers || [];
});

const viewPaper = async (paper) => {
  try {
    // 调用 API 获取包含题目详情的完整试卷数据
    const fullPaper = await getPaper(paper.id);
    selectedPaper.value = fullPaper;
    showDetailModal.value = true;
  } catch (error) {
    console.error('获取试卷详情失败:', error);
    showToast('❌ 获取试卷详情失败');
  }
};

const archivePaper = async (id) => {
  showToast('ℹ️ 归档功能暂未实现');
};

const deletePaper = async (id) => {
  if (!confirm('确定要删除这份试卷吗？此操作不可恢复！')) return;
  try {
    await deletePaperFromState(id);
    showToast('✅ 试卷已删除');
  } catch (error) {
    showToast('❌ 删除失败，请重试');
  }
};
</script>