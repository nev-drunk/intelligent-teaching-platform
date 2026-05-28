<template>
  <div class="modal-overlay" v-if="visible">
    <div class="modal wide">
      <div class="modal-header">
        <h3>📋 试卷预览</h3>
        <button class="modal-close" @click="visible = false">✕</button>
      </div>
      <div class="modal-body">
        <!-- 试卷信息 -->
        <div class="paper-preview-header">
          <h2>{{ paperName }}</h2>
          <div class="paper-meta">
            <span>总分：<strong>{{ totalScore }}</strong> 分</span>
            <span>限时：<strong>{{ timeLimit }}</strong> 分钟</span>
            <span>题目数：<strong>{{ questions.length }}</strong> 题</span>
          </div>
        </div>

        <hr style="border:none;border-top:2px solid var(--border-light);margin:20px 0;">

        <!-- 题目列表 -->
        <div class="questions-preview">
          <div v-for="(q, index) in questions" :key="q.id" class="question-preview-item">
            <div class="question-header">
              <span class="question-number">{{ index + 1 }}</span>
              <span class="question-type-badge" :class="typeBadge(q.type)">{{ typeLabel(q.type) }}</span>
              <span class="question-difficulty" :class="diffBadge(q.difficulty)">{{ diffLabel(q.difficulty) }}</span>
            </div>
            <div class="question-stem">{{ q.content || q.stem }}</div>
            
            <!-- 选项 -->
            <div v-if="parsedOptions(q).length > 0" class="question-options">
              <div v-for="(opt, idx) in parsedOptions(q)" :key="idx" class="option-item">
                <span class="option-label">{{ String.fromCharCode(65 + idx) }}.</span>
                <span class="option-text">{{ opt }}</span>
              </div>
            </div>

            <!-- 答案（仅用于预览） -->
            <div class="question-answer">
              <strong>参考答案：</strong>{{ q.answer }}
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="questions.length === 0" class="empty-state">
          <p>暂无题目，请先选择题目</p>
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-ghost" @click="visible = false">关闭</button>
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
  paperName: {
    type: String,
    default: ''
  },
  totalScore: {
    type: Number,
    default: 100
  },
  timeLimit: {
    type: Number,
    default: 90
  },
  questions: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['update:visible']);

const parsedOptions = (q) => {
  if (!q.options) return [];
  if (Array.isArray(q.options)) return q.options;
  try { return JSON.parse(q.options); } catch { return []; }
};

const typeLabel = (t) => ({ SINGLE: '单选题', MULTI: '多选题', JUDGE: '判断题', GAP: '填空题', ESSAY: '简答题', single: '单选题', multi: '多选题', judge: '判断题', fill: '填空题' }[t] || t);
const typeBadge = (t) => ({ SINGLE: 'badge-blue', MULTI: 'badge-green', JUDGE: 'badge-orange', GAP: 'badge-ghost', ESSAY: 'badge-purple', single: 'badge-blue', multi: 'badge-green', judge: 'badge-orange', fill: 'badge-ghost' }[t] || 'badge-ghost');
const diffLabel = (d) => ({ easy: '简单', medium: '中等', hard: '困难' }[d] || d);
const diffBadge = (d) => ({ easy: 'badge-green', medium: 'badge-orange', hard: 'badge-ghost' }[d] || 'badge-ghost');

const printPaper = () => {
  window.print();
};
</script>

<style scoped>
.paper-preview-header h2 {
  font-size: 24px;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.paper-meta {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: var(--text-secondary);
}

.paper-meta strong {
  color: var(--blue-primary);
  font-size: 16px;
}

.questions-preview {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.question-preview-item {
  padding: 16px;
  background: var(--bg-tertiary);
  border-radius: 8px;
  border-left: 4px solid var(--blue-primary);
}

.question-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
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

.question-type-badge {
  font-size: 12px;
  padding: 2px 8px;
}

.question-difficulty {
  font-size: 12px;
  padding: 2px 8px;
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
