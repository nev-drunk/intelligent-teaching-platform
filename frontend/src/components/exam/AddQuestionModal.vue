<template>
  <div class="modal-overlay" v-if="modelValue">
    <div class="modal wide">
      <div class="modal-header">
        <h3>{{ form.id ? '编辑题目' : '手动录入题目' }}</h3>
        <button class="modal-close" @click="$emit('update:modelValue', false)">✕</button>
      </div>
      <div class="modal-body">
        <div class="form-row">
          <div class="form-group">
            <label>题型</label>
            <select v-model="form.type">
              <option value="SINGLE">单选题</option>
              <option value="MULTI">多选题</option>
              <option value="JUDGE">判断题</option>
              <option value="GAP">填空题</option>
              <option value="ESSAY">简答题</option>
            </select>
          </div>
          <div class="form-group">
            <label>课程ID</label>
            <input type="number" placeholder="例如：1" v-model="form.courseId">
          </div>
        </div>
        <div class="form-group">
          <label>题干</label>
          <textarea rows="3" placeholder="请输入题目内容..." v-model="form.content"></textarea>
        </div>
        <div class="form-group" v-if="form.type === 'SINGLE' || form.type === 'MULTI'">
          <label>选项（JSON格式）</label>
          <textarea rows="2" placeholder='例如：["A. 选项1", "B. 选项2"]' v-model="form.optionsStr"></textarea>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>正确答案</label>
            <input type="text" placeholder="例如：A 或 对 或 具体答案" v-model="form.answer">
          </div>
          <div class="form-group">
            <label>AI生成</label>
            <select v-model="form.isLlmGenerated">
              <option :value="0">否</option>
              <option :value="1">是</option>
            </select>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-ghost" @click="$emit('update:modelValue', false)">取消</button>
        <button class="btn btn-primary" @click="save">保存题目</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, watch } from 'vue';

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:modelValue']);

const form = ref({
  id: null,
  type: 'SINGLE',
  courseId: 1,
  content: '',
  optionsStr: '',
  answer: '',
  isLlmGenerated: 0,
});
const global = inject('global');
const { showToast, saveQuestion } = global;

// 重置表单函数（必须在 watch 之前定义）
const resetForm = () => {
  form.value = {
    id: null,
    type: 'SINGLE',
    courseId: 1,
    content: '',
    optionsStr: '',
    answer: '',
    isLlmGenerated: 0,
  };
};

// 监听 editingQuestion 变化，填充表单
watch(() => global.state.editingQuestion, (newQuestion) => {
  if (newQuestion) {
    form.value = {
      id: newQuestion.id,
      type: newQuestion.type || 'SINGLE',
      courseId: newQuestion.courseId || 1,
      content: newQuestion.content || '',
      optionsStr: newQuestion.options || '',
      answer: newQuestion.answer || '',
      isLlmGenerated: newQuestion.isLlmGenerated || 0,
    };
  } else {
    resetForm();
  }
}, { immediate: true });

const save = async () => {
  if (!form.value.content.trim()) { showToast('请输入题目内容'); return; }
  try {
    // 构建保存数据
    const data = {
      ...form.value,
      options: form.value.optionsStr || null
    };
    await saveQuestion(data);
    emit('update:modelValue', false);
    global.state.editingQuestion = null;
    showToast(form.value.id ? '✅ 题目更新成功！' : '✅ 题目保存成功！');
    resetForm();
  } catch (error) {
    showToast('❌ 保存失败，请重试');
  }
};

// 关闭时重置
watch(() => props.modelValue, (newVal) => {
  if (!newVal) {
    global.state.editingQuestion = null;
    resetForm();
  }
});
</script>