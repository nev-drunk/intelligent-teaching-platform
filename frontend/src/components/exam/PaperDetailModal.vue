<template>
  <div class="modal-overlay" v-if="visible">
    <div class="modal wide">
      <div class="modal-header">
        <h3>📄 试卷详情</h3>
        <button class="modal-close" @click="$emit('close')">✕</button>
      </div>
      <div class="modal-body">
        <!-- 试卷基本信息 -->
        <div class="paper-info-card">
          <h2>{{ paper.title }}</h2>
          <div class="paper-meta-grid">
            <div class="meta-item">
              <span class="meta-label">总分</span>
              <span class="meta-value">{{ paper.totalScore }} 分</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">课程ID</span>
              <span class="meta-value">{{ paper.courseId }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">题目数</span>
              <span class="meta-value">{{ questionCount }} 题</span>
            </div>
            <div class="meta-item" style="grid-column: span 2;">
              <span class="meta-label">创建时间</span>
              <span class="meta-value">{{ createTime }}</span>
            </div>
          </div>
        </div>

        <hr style="border:none;border-top:2px solid var(--border-light);margin:20px 0;">

        <!-- 题目列表 -->
        <h4 style="margin-bottom:16px;color:var(--text-primary);">试卷题目</h4>
        
        <div v-if="questions && questions.length > 0" class="questions-list">
          <div v-for="(q, index) in questions" :key="q.questionId || q.id" class="question-item">
            <div class="question-header">
              <span class="question-number">{{ index + 1 }}</span>
              <span class="question-type-badge" :class="typeBadge(q.type)">{{ typeLabel(q.type) }}</span>
              <span class="question-source-badge" :class="q.isLlmGenerated === 1 ? 'badge-red' : 'badge-green'">
                {{ q.isLlmGenerated === 1 ? 'AI生成' : '手动录入' }}
              </span>
            </div>
            <div class="question-stem">{{ q.content || q.stem }}</div>
            
            <!-- 选项 -->
            <div v-if="q.options" class="question-options">
              <div v-for="(opt, idx) in parseOptions(q.options)" :key="idx" class="option-item">
                <span class="option-label">{{ String.fromCharCode(65 + idx) }}.</span>
                <span class="option-text">{{ opt }}</span>
              </div>
            </div>

            <!-- 答案 -->
            <div class="question-answer">
              <strong>参考答案：</strong>{{ q.answer }}
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          <p>该试卷暂无题目</p>
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-ghost" @click="$emit('close')">关闭</button>
        <button class="btn btn-primary" @click="printPaper">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6,9 6,2 18,2 18,9"/><path d="M6,18 H4 A2,2 0 0,1 2,16 V11 A2,2 0 0,1 4,9 H20 A2,2 0 0,1 22,11 V16 A2,2 0 0,1 20,18 H18"/><rect x="6" y="14" width="12" height="8"/></svg>
          打印试卷
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  paper: {
    type: Object,
    default: () => ({})
  }
});

defineEmits(['close']);

const questions = computed(() => props.paper.questions || []);
const questionCount = computed(() => questions.value.length);
const createTime = computed(() => props.paper.createTime ? new Date(props.paper.createTime).toLocaleString() : '-');

const typeLabel = (t) => ({ SINGLE: '单选题', MULTI: '多选题', JUDGE: '判断题', GAP: '填空题', ESSAY: '简答题' }[t] || t);
const typeBadge = (t) => ({ SINGLE: 'badge-blue', MULTI: 'badge-green', JUDGE: 'badge-orange', GAP: 'badge-ghost', ESSAY: 'badge-purple' }[t] || 'badge-ghost');

// 解析选项（可能是JSON字符串或数组）
const parseOptions = (options) => {
  if (!options) return [];
  if (Array.isArray(options)) return options;
  try {
    return JSON.parse(options);
  } catch (e) {
    return [];
  }
};

const printPaper = () => {
  window.print();
};
</script>

<style scoped>
.paper-info-card {
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  padding: 20px;
  border-radius: 8px;
  border: 1px solid var(--border-light);
}

.paper-info-card h2 {
  font-size: 22px;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.paper-meta-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.meta-label {
  font-size: 12px;
  color: var(--text-tertiary);
  font-weight: 500;
}

.meta-value {
  font-size: 15px;
  color: var(--text-primary);
  font-weight: 600;
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.question-item {
  padding: 16px;
  background: var(--bg-tertiary);
  border-radius: 8px;
  border-left: 4px solid var(--blue-primary);
}

.question-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.question-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: var(--blue-primary);
  color: white;
  border-radius: 50%;
  font-weight: bold;
  font-size: 14px;
}

.question-type-badge,
.question-source-badge {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 12px;
}

.question-stem {
  font-size: 15px;
  line-height: 1.6;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.question-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.option-item {
  display: flex;
  gap: 8px;
  padding: 8px 12px;
  background: white;
  border-radius: 4px;
}

.option-label {
  font-weight: bold;
  color: var(--blue-primary);
  min-width: 20px;
}

.option-text {
  color: var(--text-primary);
}

.question-answer {
  padding: 8px 12px;
  background: #fff3cd;
  border-radius: 4px;
  font-size: 14px;
  color: #856404;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: var(--text-tertiary);
}
</style>
