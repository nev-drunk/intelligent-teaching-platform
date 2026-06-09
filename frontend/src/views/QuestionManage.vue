<template>
  <div class="app">
    <!-- ════════════════════════════ TABS ════════════════════════════ -->
    <div class="tab-bar">
      <button
        :class="['tab', activeTab === 'workspace' ? 'tab--active' : '']"
        @click="switchTab('workspace')"
      >
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <rect x="1" y="1" width="6" height="6" rx="1.5" fill="currentColor" opacity=".7" />
          <rect x="9" y="1" width="6" height="6" rx="1.5" fill="currentColor" />
          <rect x="1" y="9" width="6" height="6" rx="1.5" fill="currentColor" opacity=".4" />
          <rect x="9" y="9" width="6" height="6" rx="1.5" fill="currentColor" opacity=".7" />
        </svg>
        题库工作台
      </button>
      <button
        :class="['tab', activeTab === 'history' ? 'tab--active' : '']"
        @click="switchTab('history')"
      >
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path
            d="M2 4h12M2 8h8M2 12h10"
            stroke="currentColor"
            stroke-width="1.8"
            stroke-linecap="round"
          />
        </svg>
        已发布试卷
      </button>
    </div>

    <div class="page-body">
      <!-- ══════════ WORKSPACE TAB ══════════ -->
      <transition name="slide-fade" mode="out-in">
        <div v-if="activeTab === 'workspace'" key="workspace" class="workspace">
          <!-- ── 顶部统计卡片 ── -->
          <div class="stats-row">
            <div class="stat-card">
              <div class="stat-icon stat-icon--blue">
                <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
                  <path
                    d="M4 18V9a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v9M12 18V5a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v13"
                    stroke="white"
                    stroke-width="1.8"
                    stroke-linecap="round"
                  />
                </svg>
              </div>
              <div class="stat-content">
                <span class="stat-num">{{ questionList.length }}</span>
                <span class="stat-label">题库总量</span>
              </div>
              <div class="stat-trend up">↑ 实时</div>
            </div>
            <div class="stat-card">
              <div class="stat-icon stat-icon--amber">
                <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
                  <path
                    d="M11 2L13.5 8.5H20L14.5 12.5L16.5 19L11 15L5.5 19L7.5 12.5L2 8.5H8.5Z"
                    stroke="white"
                    stroke-width="1.8"
                    stroke-linejoin="round"
                  />
                </svg>
              </div>
              <div class="stat-content">
                <span class="stat-num">{{ aiCount }}</span>
                <span class="stat-label">AI 生成题</span>
              </div>
              <div class="stat-trend" :class="aiCount > 0 ? 'up' : ''">
                {{ aiCount > 0 ? '✦ 智能' : '— 无' }}
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon stat-icon--green">
                <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
                  <path
                    d="M4 12l5 5 9-10"
                    stroke="white"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              </div>
              <div class="stat-content">
                <span class="stat-num">{{ manualCount }}</span>
                <span class="stat-label">手动录入</span>
              </div>
              <div class="stat-trend">教师创建</div>
            </div>
            <div class="stat-card">
              <div class="stat-icon stat-icon--purple">
                <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
                  <rect
                    x="3"
                    y="5"
                    width="16"
                    height="14"
                    rx="2"
                    stroke="white"
                    stroke-width="1.8"
                  />
                  <path d="M3 9h16" stroke="white" stroke-width="1.8" />
                  <path
                    d="M8 3v4M14 3v4"
                    stroke="white"
                    stroke-width="1.8"
                    stroke-linecap="round"
                  />
                </svg>
              </div>
              <div class="stat-content">
                <span class="stat-num">{{ paperHistory.length }}</span>
                <span class="stat-label">已发试卷</span>
              </div>
              <div class="stat-trend">本课程</div>
            </div>
          </div>

          <!-- ── 可视化图表 ── -->
          <div class="viz-row">
            <!-- 题目构成饼图 -->
            <div class="card viz-card">
              <div class="card-title">
                <span class="card-title-dot dot--blue"></span>
                题目来源构成
              </div>
              <div class="donut-wrap">
                <svg class="donut-svg" viewBox="0 0 180 180">
                  <circle cx="90" cy="90" r="60" fill="none" stroke="#EEF2FF" stroke-width="24" />
                  <circle
                    cx="90"
                    cy="90"
                    r="60"
                    fill="none"
                    stroke="#2563EB"
                    stroke-width="24"
                    :stroke-dasharray="donutAiDash"
                    stroke-dashoffset="-94.25"
                    stroke-linecap="round"
                    style="transition: stroke-dasharray 0.8s ease"
                  />
                  <circle
                    cx="90"
                    cy="90"
                    r="60"
                    fill="none"
                    stroke="#10B981"
                    stroke-width="24"
                    :stroke-dasharray="donutManualDash"
                    :stroke-dashoffset="donutManualOffset"
                    stroke-linecap="round"
                    style="transition: stroke-dasharray 0.8s ease"
                  />
                  <text x="90" y="85" text-anchor="middle" class="donut-center-num">
                    {{ questionList.length }}
                  </text>
                  <text x="90" y="102" text-anchor="middle" class="donut-center-label">总题数</text>
                </svg>
                <div class="donut-legend">
                  <div class="legend-item">
                    <span class="legend-dot" style="background: #2563eb"></span>
                    <span class="legend-text"
                      >AI 生成 <strong>{{ aiCount }}</strong> 题</span
                    >
                  </div>
                  <div class="legend-item">
                    <span class="legend-dot" style="background: #10b981"></span>
                    <span class="legend-text"
                      >手动录入 <strong>{{ manualCount }}</strong> 题</span
                    >
                  </div>
                </div>
              </div>
            </div>

            <!-- 题型分布柱状图 -->
            <div class="card viz-card">
              <div class="card-title">
                <span class="card-title-dot dot--green"></span>
                题型分布
              </div>
              <div class="bar-chart">
                <div v-for="(item, i) in typeDistribution" :key="i" class="bar-row">
                  <span class="bar-label">{{ item.label }}</span>
                  <div class="bar-track">
                    <div
                      class="bar-fill"
                      :style="{ width: item.pct + '%', background: item.color }"
                      style="transition: width 0.8s ease"
                    ></div>
                  </div>
                  <span class="bar-val">{{ item.count }}</span>
                </div>
              </div>
            </div>

            <!-- AI 出题入口 -->
            <div class="card ai-card">
              <div class="card-title">
                <span class="card-title-dot dot--amber"></span>
                AI 智能出题
              </div>
              <p class="ai-desc">
                对接 DeepSeek 大模型，输入知识点关键词，一键生成标准单选题并自动入库。
              </p>
              <div class="field">
                <label class="field-label">命题知识点</label>
                <input
                  v-model="keyword"
                  class="input"
                  placeholder="如：Transformer 注意力机制"
                  @keyup.enter="handleAiGenerate"
                />
              </div>
              <button
                class="btn btn--amber btn--full"
                :disabled="isAiLoading"
                @click="handleAiGenerate"
              >
                <span v-if="isAiLoading" class="btn-spinner"></span>
                <svg v-else width="15" height="15" viewBox="0 0 15 15" fill="none">
                  <path
                    d="M7.5 1L9.2 5.8H14.3L10.3 8.7L11.8 13.5L7.5 10.5L3.2 13.5L4.7 8.7L0.7 5.8H5.8Z"
                    fill="currentColor"
                  />
                </svg>
                {{ isAiLoading ? '模型生成中…' : '生成题目' }}
              </button>

              <div v-if="lastAiQuestion" class="ai-preview">
                <div class="ai-preview-tag">✦ 最新生成</div>
                <p class="ai-preview-content">{{ lastAiQuestion.content }}</p>
                <p class="ai-preview-answer">
                  答案：<strong>{{ lastAiQuestion.answer }}</strong>
                </p>
              </div>
            </div>
          </div>

          <!-- ── 录入 + 组卷区 ── -->
          <div class="main-row">
            <!-- 手动录入 -->
            <div class="card entry-card">
              <div class="card-title">
                <span class="card-title-dot dot--blue"></span>
                手动录入题目
              </div>
              <div class="field">
                <label class="field-label">题干内容</label>
                <textarea
                  v-model="form.content"
                  class="textarea"
                  rows="4"
                  placeholder="请输入题干…"
                ></textarea>
                <button
                  @click="toggleRecording"
                  :class="['asr-btn', isRecording ? 'asr-btn--on' : '']"
                >
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                    <rect
                      x="4"
                      y="1"
                      width="6"
                      height="8"
                      rx="3"
                      :fill="isRecording ? '#EF4444' : '#2563EB'"
                    />
                    <path
                      d="M2 7a5 5 0 0 0 10 0"
                      :stroke="isRecording ? '#EF4444' : '#2563EB'"
                      stroke-width="1.5"
                      stroke-linecap="round"
                      fill="none"
                    />
                    <path
                      d="M7 12v2"
                      :stroke="isRecording ? '#EF4444' : '#2563EB'"
                      stroke-width="1.5"
                      stroke-linecap="round"
                    />
                  </svg>
                  {{ isRecording ? '录音中，点击结束' : '语音识别录入' }}
                </button>
              </div>
              <div class="field">
                <label class="field-label"
                  >选项 <span class="field-hint">JSON 数组格式</span></label
                >
                <input
                  v-model="form.options"
                  class="input"
                  placeholder='["A.选项1","B.选项2","C.选项3","D.选项4"]'
                />
              </div>
              <div class="field-row">
                <div class="field" style="width: 90px">
                  <label class="field-label">答案</label>
                  <input v-model="form.answer" class="input" placeholder="A" />
                </div>
                <div class="field" style="flex: 1">
                  <label class="field-label">题型</label>
                  <select v-model="form.type" class="input">
                    <option value="SINGLE">单选题</option>
                    <option value="MULTI">多选题</option>
                    <option value="JUDGE">判断题</option>
                    <option value="ESSAY">简答题</option>
                  </select>
                </div>
              </div>
              <button class="btn btn--blue btn--full" @click="handleAdd">
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                  <path
                    d="M7 1v12M1 7h12"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                  />
                </svg>
                保存入库
              </button>
            </div>

            <!-- 组卷发布 -->
            <div class="card assemble-card">
              <div class="card-title">
                <span class="card-title-dot dot--purple"></span>
                组卷 &amp; 发布
                <span class="selected-badge" v-if="selectedQuestionIds.length"
                  >已选 {{ selectedQuestionIds.length }} 题</span
                >
              </div>
              <div class="field">
                <label class="field-label">试卷名称</label>
                <input v-model="paperTitle" class="input" placeholder="如：第一章阶段测验" />
              </div>
              <div class="field">
                <label class="field-label">目标班级</label>
                <select v-model="selectedClassId" class="input">
                  <option value="" disabled>请选择班级</option>
                  <option v-for="cls in classList" :key="cls.id" :value="cls.id">
                    {{ cls.name }}
                  </option>
                </select>
              </div>
              <div class="score-summary" v-if="selectedQuestionIds.length">
                <div class="score-summary-row">
                  <span>题目数量</span><strong>{{ selectedQuestionIds.length }} 题</strong>
                </div>
                <div class="score-summary-row">
                  <span>合计分值</span
                  ><strong class="score-total">{{ totalSelectedScore }} 分</strong>
                </div>
              </div>
              <div class="empty-select" v-else>
                <svg width="36" height="36" viewBox="0 0 36 36" fill="none">
                  <rect
                    x="6"
                    y="10"
                    width="24"
                    height="18"
                    rx="3"
                    stroke="#CBD5E1"
                    stroke-width="1.5"
                  />
                  <path d="M6 15h24" stroke="#CBD5E1" stroke-width="1.5" />
                  <path
                    d="M12 10V8a2 2 0 0 1 4 0v2M20 10V8a2 2 0 0 1 4 0v2"
                    stroke="#CBD5E1"
                    stroke-width="1.5"
                  />
                </svg>
                <p>请在下方题库中勾选题目</p>
              </div>
              <button
                class="btn btn--blue btn--full"
                @click="handleCreatePaper"
                :disabled="!selectedQuestionIds.length"
              >
                <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                  <path
                    d="M2 7l3 3 7-7"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
                发布试卷
              </button>
            </div>
          </div>

          <!-- ── 题库列表 ── -->
          <div class="card table-card">
            <div class="table-card-header">
              <div class="card-title" style="margin-bottom: 0">
                <span class="card-title-dot dot--blue"></span>
                题库列表
              </div>
              <div class="table-actions">
                <div class="search-wrap">
                  <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                    <circle cx="6" cy="6" r="4.5" stroke="#94A3B8" stroke-width="1.5" />
                    <path
                      d="M9.5 9.5L12 12"
                      stroke="#94A3B8"
                      stroke-width="1.5"
                      stroke-linecap="round"
                    />
                  </svg>
                  <input v-model="searchText" class="search-input" placeholder="搜索题干关键词…" />
                </div>
                <button class="btn btn--ghost" @click="fetchList">
                  <svg width="13" height="13" viewBox="0 0 13 13" fill="none">
                    <path
                      d="M11 6.5a4.5 4.5 0 1 1-1.1-3"
                      stroke="currentColor"
                      stroke-width="1.5"
                      stroke-linecap="round"
                    />
                    <path
                      d="M10 1v3h-3"
                      stroke="currentColor"
                      stroke-width="1.5"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                  </svg>
                  刷新
                </button>
              </div>
            </div>

            <div class="table-wrap">
              <table class="data-table">
                <thead>
                  <tr>
                    <th width="44">选</th>
                    <th width="72">分值</th>
                    <th width="58">ID</th>
                    <th width="88">题型</th>
                    <th>题干内容</th>
                    <th width="100">来源</th>
                    <th width="130">录入时间</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="filteredList.length === 0">
                    <td colspan="7" class="table-empty">暂无题目，请手动录入或点击 AI 生成</td>
                  </tr>
                  <tr
                    v-for="item in filteredList"
                    :key="item.id"
                    :class="{ 'row--selected': selectedQuestionIds.includes(item.id) }"
                  >
                    <td>
                      <input
                        type="checkbox"
                        :value="item.id"
                        v-model="selectedQuestionIds"
                        class="checkbox"
                      />
                    </td>
                    <td>
                      <input
                        v-if="selectedQuestionIds.includes(item.id)"
                        v-model="questionScores[item.id]"
                        type="number"
                        min="1"
                        max="50"
                        placeholder="5"
                        class="score-input"
                      />
                      <span v-else class="score-dash">—</span>
                    </td>
                    <td>
                      <span class="id-chip">#{{ item.id }}</span>
                    </td>
                    <td>
                      <span :class="['type-badge', 'type-badge--' + item.type.toLowerCase()]">{{
                        typeLabel(item.type)
                      }}</span>
                    </td>
                    <td class="content-cell" :title="item.content">{{ item.content }}</td>
                    <td>
                      <span
                        :class="[
                          'source-badge',
                          item.isLlmGenerated === 1 ? 'source-badge--ai' : 'source-badge--manual'
                        ]"
                      >
                        {{ item.isLlmGenerated === 1 ? '✦ AI' : '✎ 手动' }}
                      </span>
                    </td>
                    <td class="time-cell">{{ formatDate(item.createTime) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </transition>

      <!-- ══════════ HISTORY TAB ══════════ -->
      <transition name="slide-fade" mode="out-in">
        <div v-if="activeTab === 'history'" key="history" class="workspace">
          <!-- 试卷统计 -->
          <div class="stats-row">
            <div class="stat-card">
              <div class="stat-icon stat-icon--blue">
                <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
                  <rect
                    x="4"
                    y="3"
                    width="14"
                    height="17"
                    rx="2"
                    stroke="white"
                    stroke-width="1.8"
                  />
                  <path
                    d="M8 8h6M8 12h4"
                    stroke="white"
                    stroke-width="1.8"
                    stroke-linecap="round"
                  />
                </svg>
              </div>
              <div class="stat-content">
                <span class="stat-num">{{ paperHistory.length }}</span>
                <span class="stat-label">已发试卷</span>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon stat-icon--green">
                <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
                  <circle cx="11" cy="11" r="8" stroke="white" stroke-width="1.8" />
                  <path d="M11 7v4l3 2" stroke="white" stroke-width="1.8" stroke-linecap="round" />
                </svg>
              </div>
              <div class="stat-content">
                <span class="stat-num">{{ totalPaperQuestions }}</span>
                <span class="stat-label">累计题目数</span>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon stat-icon--amber">
                <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
                  <path
                    d="M11 3L13.5 9H20L14.5 13L16.5 19L11 15L5.5 19L7.5 13L2 9H8.5Z"
                    stroke="white"
                    stroke-width="1.8"
                    stroke-linejoin="round"
                  />
                </svg>
              </div>
              <div class="stat-content">
                <span class="stat-num">{{ avgScore }}</span>
                <span class="stat-label">平均总分</span>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon stat-icon--purple">
                <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
                  <path
                    d="M4 19v-6a2 2 0 0 1 2-2h4m0 0V7m0 4h6a2 2 0 0 1 2 2v6"
                    stroke="white"
                    stroke-width="1.8"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              </div>
              <div class="stat-content">
                <span class="stat-num">{{ classesUsed }}</span>
                <span class="stat-label">覆盖班级</span>
              </div>
            </div>
          </div>

          <!-- 班级覆盖可视化 -->
          <div class="viz-row">
            <div class="card viz-card" style="flex: 1.5">
              <div class="card-title">
                <span class="card-title-dot dot--blue"></span>
                试卷发布时间线
              </div>
              <div v-if="paperHistory.length === 0" class="viz-empty">暂无发布记录</div>
              <div v-else class="timeline">
                <div
                  v-for="(paper, i) in paperHistory.slice(0, 5)"
                  :key="paper.id"
                  class="timeline-item"
                >
                  <div class="timeline-dot" :class="i === 0 ? 'timeline-dot--active' : ''"></div>
                  <div class="timeline-body">
                    <div class="timeline-title">{{ paper.title }}</div>
                    <div class="timeline-meta">
                      <span class="tl-class">{{ getClassName(paper.classId) }}</span>
                      <span class="tl-score">{{ paper.totalScore }} 分</span>
                      <span class="tl-count">{{ paper.questionCount }} 题</span>
                    </div>
                  </div>
                  <div class="timeline-time">{{ formatDate(paper.createTime) }}</div>
                </div>
              </div>
            </div>

            <div class="card viz-card" style="flex: 1">
              <div class="card-title">
                <span class="card-title-dot dot--amber"></span>
                班级分布
              </div>
              <div class="class-dist">
                <div v-for="cls in classDistribution" :key="cls.id" class="class-dist-item">
                  <div class="class-dist-info">
                    <span class="class-dot" :style="{ background: cls.color }"></span>
                    <span>{{ cls.name }}</span>
                  </div>
                  <div class="class-dist-bar-wrap">
                    <div
                      class="class-dist-bar"
                      :style="{ width: cls.pct + '%', background: cls.color }"
                    ></div>
                  </div>
                  <span class="class-dist-num">{{ cls.count }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 试卷表格 -->
          <div class="card table-card">
            <div class="table-card-header">
              <div class="card-title" style="margin-bottom: 0">
                <span class="card-title-dot dot--blue"></span>
                已发布试卷明细
              </div>
              <button class="btn btn--ghost" @click="fetchPaperHistory">
                <svg width="13" height="13" viewBox="0 0 13 13" fill="none">
                  <path
                    d="M11 6.5a4.5 4.5 0 1 1-1.1-3"
                    stroke="currentColor"
                    stroke-width="1.5"
                    stroke-linecap="round"
                  />
                  <path
                    d="M10 1v3h-3"
                    stroke="currentColor"
                    stroke-width="1.5"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
                刷新
              </button>
            </div>
            <div class="table-wrap">
              <table class="data-table">
                <thead>
                  <tr>
                    <th width="130">试卷编号</th>
                    <th>试卷名称</th>
                    <th width="150">目标班级</th>
                    <th width="100">总分</th>
                    <th width="90">题目数</th>
                    <th width="170">发布时间</th>
                    <th width="100">操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="paperHistory.length === 0">
                    <td colspan="7" class="table-empty">该教师名下暂无已发布试卷</td>
                  </tr>
                  <tr v-for="paper in paperHistory" :key="paper.id">
                    <td>
                      <span class="id-chip">PAPER-{{ String(paper.id).padStart(4, '0') }}</span>
                    </td>
                    <td class="paper-title-cell">{{ paper.title }}</td>
                    <td>
                      <span class="class-badge">{{ getClassName(paper.classId) }}</span>
                    </td>
                    <td>
                      <span class="score-highlight">{{ paper.totalScore }} 分</span>
                    </td>
                    <td>{{ paper.questionCount }} 题</td>
                    <td class="time-cell">{{ formatDate(paper.createTime) }}</td>
                    <td>
                      <button class="btn btn--ghost btn--view" @click="viewPaper(paper)">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                          <path
                            d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0z"
                            stroke="currentColor"
                            stroke-width="2"
                            stroke-linecap="round"
                            stroke-linejoin="round"
                          />
                          <path
                            d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                            stroke="currentColor"
                            stroke-width="2"
                            stroke-linecap="round"
                            stroke-linejoin="round"
                          />
                        </svg>
                        查看
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </transition>
    </div>

    <!-- ── 查看试卷弹窗 ── -->
    <transition name="modal-fade">
      <div v-if="viewPaperModal" class="modal-overlay" @click.self="closeViewPaperModal">
        <div class="modal-content modal-content--paper">
          <div class="modal-header">
            <div class="modal-title">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                <path
                  d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <polyline
                  points="14 2 14 8 20 8"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <line
                  x1="16"
                  y1="13"
                  x2="8"
                  y2="13"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                />
                <line
                  x1="16"
                  y1="17"
                  x2="8"
                  y2="17"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                />
                <polyline
                  points="10 9 9 9 8 9"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                />
              </svg>
              试卷详情
            </div>
            <button class="modal-close" @click="closeViewPaperModal">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                <path
                  d="M6 18L18 6M6 6l12 12"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                />
              </svg>
            </button>
          </div>
          <div class="modal-body">
            <div v-if="currentPaper" class="paper-header-info">
              <div class="paper-title">{{ currentPaper.title }}</div>
              <div class="paper-meta">
                <span class="paper-meta-item">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                    <path
                      d="M17 20h5v-2a3 3 0 0 0-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 0 1 5.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 0 1 9.288 0M15 7a3 3 0 1 1-6 0 3 3 0 0 1 6 0zm6 3a2 2 0 1 1-4 0 2 2 0 0 1 4 0zM7 10a2 2 0 1 1-4 0 2 2 0 0 1 4 0z"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                  </svg>
                  {{ getClassName(currentPaper.classId) }}
                </span>
                <span class="paper-meta-item">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                    <path
                      d="M12 8v4l3 3m6-3a9 9 0 1 1-18 0 9 9 0 0 1 18 0z"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                  </svg>
                  {{ formatDate(currentPaper.createTime) }}
                </span>
                <span class="paper-meta-item">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                    <path
                      d="M9 19v-6a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2zm0 0V9a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v10m-6 0a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2m0 0V5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-2a2 2 0 0 1-2-2z"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                  </svg>
                  共 {{ currentPaper.questionCount }} 题 / {{ currentPaper.totalScore }} 分
                </span>
              </div>
            </div>
            <div class="paper-questions">
              <div
                v-for="(item, index) in currentPaperQuestions"
                :key="item.id || index"
                class="question-item"
              >
                <div class="question-number">{{ index + 1 }}</div>
                <div class="question-content">
                  <div class="question-header">
                    <span
                      class="type-badge"
                      :class="`type-badge--${item.type?.toLowerCase() || 'single'}`"
                    >
                      {{ typeLabel(item.type) }}
                    </span>
                    <span class="question-score">{{ item.score }} 分</span>
                  </div>
                  <div class="question-text">{{ item.content }}</div>
                  <div v-if="item.options" class="question-options">
                    <div
                      v-for="(opt, idx) in item.options.split(';').filter(Boolean)"
                      :key="idx"
                      class="option-item"
                    >
                      {{ opt }}
                    </div>
                  </div>
                  <div class="question-answer">
                    <span class="answer-label">参考答案：</span>
                    <span class="answer-value">{{ item.answer }}</span>
                  </div>
                </div>
              </div>
              <div v-if="currentPaperQuestions.length === 0" class="empty-paper">暂无题目数据</div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn--ghost" @click="closeViewPaperModal">关闭</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- ── Toast ── -->
    <transition name="toast-fade">
      <div v-if="toast.show" :class="['toast', 'toast--' + toast.type]">
        <div class="toast-icon">
          <svg
            v-if="toast.type === 'success'"
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
          >
            <path
              d="M9 12l2 2 4-4m6 2a9 9 0 1 1-18 0 9 9 0 0 1 18 0z"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none">
            <path
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </div>
        <div class="toast-content">
          <span class="toast-title">{{ toast.type === 'success' ? '成功' : '提示' }}</span>
          <span class="toast-message">{{ toast.msg }}</span>
        </div>
        <button class="toast-close" @click="toast.show = false">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <path
              d="M6 18L18 6M6 6l12 12"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
            />
          </svg>
        </button>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { listQuestions, aiGenerate, saveQuestion } from '@/api/question'
import { listPapers, publishPaper, getPaper } from '@/api/paper'
import request from '@/api/request'

// ── State ─────────────────────────────────────────────────────────────────────
const currentTeacherId = ref(1)
const courseId = ref(1)
const activeTab = ref('workspace')

const form = ref({ content: '', options: '', answer: '', type: 'SINGLE' })
const keyword = ref('')
const isAiLoading = ref(false)
const lastAiQuestion = ref(null)

const questionList = ref([])
const paperHistory = ref([])
const selectedQuestionIds = ref([])
const questionScores = ref({})
const paperTitle = ref('')
const selectedClassId = ref('')
const searchText = ref('')
const isRecording = ref(false)

const classList = ref([
  { id: 101, name: '软件工程 1 班' },
  { id: 102, name: '计算机科学 2 班' }
])

// ── Toast ─────────────────────────────────────────────────────────────────────
const toast = ref({ show: false, msg: '', type: 'success' })
function showToast(msg, type = 'success') {
  toast.value = { show: true, msg, type }
  setTimeout(() => {
    toast.value.show = false
  }, 3000)
}

// ── 查看试卷弹窗 ───────────────────────────────────────────────────────────────
const viewPaperModal = ref(false)
const currentPaper = ref(null)
const currentPaperQuestions = ref([])

async function viewPaper(paper) {
  currentPaper.value = paper
  currentPaperQuestions.value = []
  viewPaperModal.value = true
  try {
    const res = await getPaper(paper.id)
    if (res && res.questions) {
      currentPaperQuestions.value = res.questions
    }
  } catch (e) {
    console.error('viewPaper', e)
    showToast('获取试卷详情失败', 'error')
  }
}

function closeViewPaperModal() {
  viewPaperModal.value = false
  currentPaper.value = null
  currentPaperQuestions.value = []
}

// ── Computed ──────────────────────────────────────────────────────────────────
const aiCount = computed(() => questionList.value.filter((q) => q.isLlmGenerated === 1).length)
const manualCount = computed(() => questionList.value.length - aiCount.value)

const filteredList = computed(() => {
  if (!searchText.value.trim()) return questionList.value
  return questionList.value.filter((q) => q.content.includes(searchText.value))
})

const totalSelectedScore = computed(() => {
  return selectedQuestionIds.value.reduce((sum, id) => {
    return sum + (parseInt(questionScores.value[id]) || 5)
  }, 0)
})

// ── 饼图计算 ──────────────────────────────────────────────────────────────────
const CIRCUMFERENCE = 2 * Math.PI * 60 // ≈ 376.99

const donutAiDash = computed(() => {
  const total = questionList.value.length
  if (!total) return `0 ${CIRCUMFERENCE}`
  const arc = (aiCount.value / total) * CIRCUMFERENCE
  return `${arc} ${CIRCUMFERENCE - arc}`
})

const donutManualDash = computed(() => {
  const total = questionList.value.length
  if (!total) return `0 ${CIRCUMFERENCE}`
  const aiArc = (aiCount.value / total) * CIRCUMFERENCE
  const manualArc = (manualCount.value / total) * CIRCUMFERENCE
  return `${manualArc} ${CIRCUMFERENCE - manualArc}`
})

const donutManualOffset = computed(() => {
  const total = questionList.value.length
  if (!total) return '0'
  const aiArc = (aiCount.value / total) * CIRCUMFERENCE
  return -(94.25 + aiArc)
})

// ── 题型分布 ──────────────────────────────────────────────────────────────────
const TYPE_MAP = { SINGLE: '单选', MULTI: '多选', JUDGE: '判断', ESSAY: '简答', GAP: '填空' }
const TYPE_COLORS = {
  SINGLE: '#2563EB',
  MULTI: '#8B5CF6',
  JUDGE: '#10B981',
  ESSAY: '#F59E0B',
  GAP: '#EF4444'
}

const typeDistribution = computed(() => {
  const counts = {}
  questionList.value.forEach((q) => {
    counts[q.type] = (counts[q.type] || 0) + 1
  })
  const max = Math.max(...Object.values(counts), 1)
  return Object.entries(counts).map(([type, count]) => ({
    label: TYPE_MAP[type] || type,
    count,
    pct: Math.round((count / max) * 100),
    color: TYPE_COLORS[type] || '#94A3B8'
  }))
})

// ── 历史页统计 ────────────────────────────────────────────────────────────────
const totalPaperQuestions = computed(() =>
  paperHistory.value.reduce((s, p) => s + (p.questionCount || 0), 0)
)
const avgScore = computed(() => {
  if (!paperHistory.value.length) return 0
  return Math.round(
    paperHistory.value.reduce((s, p) => s + (p.totalScore || 0), 0) / paperHistory.value.length
  )
})
const classesUsed = computed(() => new Set(paperHistory.value.map((p) => p.classId)).size)

const CLASS_COLORS = ['#2563EB', '#10B981', '#8B5CF6', '#F59E0B']
const classDistribution = computed(() => {
  const counts = {}
  paperHistory.value.forEach((p) => {
    counts[p.classId] = (counts[p.classId] || 0) + 1
  })
  const max = Math.max(...Object.values(counts), 1)
  return Object.entries(counts).map(([id, count], i) => ({
    id,
    name: getClassName(Number(id)),
    count,
    pct: Math.round((count / max) * 100),
    color: CLASS_COLORS[i % CLASS_COLORS.length]
  }))
})

// ── Helpers ───────────────────────────────────────────────────────────────────
function getClassName(id) {
  return classList.value.find((c) => c.id === Number(id))?.name || `班级${id}`
}

function typeLabel(type) {
  return TYPE_MAP[type] || type
}

function formatDate(s) {
  // 1. 如果是空值，或者是不小心变成字符串的 "undefined" / "null"，直接返回 '—'
  if (!s || s === 'undefined' || s === 'null') return '—'

  // 2. 如果后端给的是 10 位数字时间戳（秒），自动乘以 1000 转换为毫秒
  if (typeof s === 'number' && String(s).length === 10) {
    s = s * 1000
  }

  const d = new Date(s)

  // 3. 核心防御：检查 new Date() 转换后是否合法
  // 如果是 Invalid Date，isNaN(d.getTime()) 会返回 true
  if (isNaN(d.getTime())) {
    console.error('formatDate 收到无法解析的时间数据:', s) // 方便你调试看看到底传进来了什么
    return '—' // 或者返回原始值 s，至少不会显示丑陋的 NaN
  }

  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(
    d.getMinutes()
  )}`
}

function switchTab(tab) {
  activeTab.value = tab
  if (tab === 'history') fetchPaperHistory()
}

// ── API calls ─────────────────────────────────────────────────────────────────
async function fetchList() {
  try {
    const res = await listQuestions(courseId.value)
    questionList.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error('fetchList', e)
    showToast('加载题目失败，请检查后端连接', 'error')
  }
}

async function fetchPaperHistory() {
  try {
    const res = await listPapers()
    paperHistory.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error('fetchPaperHistory', e)
  }
}

function handleTeacherChange() {
  fetchList()
  fetchPaperHistory()
}

async function handleAdd() {
  if (!form.value.content.trim()) {
    showToast('请填写题干内容', 'error')
    return
  }
  if (!form.value.answer.trim()) {
    showToast('请填写标准答案', 'error')
    return
  }

  // ── AI 查重检测 ──
  try {
    const dupRes = await request.post('/api/questions/check-duplicate', {
      content: form.value.content,
      courseId: courseId.value
    })
    if (dupRes.code === 200 && dupRes.data?.length > 0) {
      const topItem = dupRes.data[0]
      if (topItem.score > 0.8) {
        const confirmed = confirm(
          `⚠️ 题目查重警告：\n\n与题库中已有题目相似度达 ${Math.round(topItem.score * 100)}%\n相似题目：${topItem.text.slice(0, 80)}...\n\n确定仍要保存吗？`
        )
        if (!confirmed) return
      }
    }
  } catch (e) {
    console.warn('题目查重服务暂不可用，跳过查重', e)
  }

  try {
    await saveQuestion({
      courseId: courseId.value,
      ...form.value
    })
    form.value = { content: '', options: '', answer: '', type: 'SINGLE' }
    showToast('题目已保存入库')
    fetchList()
  } catch (e) {
    showToast('保存失败，请检查后端连接', 'error')
  }
}

async function handleAiGenerate() {
  if (!keyword.value.trim()) {
    showToast('请先填写命题知识点', 'error')
    return
  }
  isAiLoading.value = true
  try {
    const res = await aiGenerate({
      courseId: courseId.value,
      keyword: keyword.value
    })
    lastAiQuestion.value = res
    keyword.value = ''
    showToast('AI 出题成功并已入库')
    fetchList()
  } catch (e) {
    showToast('AI 接口异常，请检查 API Key', 'error')
  } finally {
    isAiLoading.value = false
  }
}

async function handleCreatePaper() {
  if (!paperTitle.value.trim()) {
    showToast('请填写试卷名称', 'error')
    return
  }
  if (!selectedClassId.value) {
    showToast('请选择目标班级', 'error')
    return
  }
  if (!selectedQuestionIds.value.length) {
    showToast('请至少勾选一道题目', 'error')
    return
  }

  const items = selectedQuestionIds.value.map((id) => ({
    questionId: id,
    score: parseInt(questionScores.value[id]) || 5
  }))

  try {
    await publishPaper({
      courseId: courseId.value,
      title: paperTitle.value,
      classId: selectedClassId.value,
      teacherId: currentTeacherId.value,
      items
    })
    showToast('试卷发布成功')
    paperTitle.value = ''
    selectedClassId.value = ''
    selectedQuestionIds.value = []
    questionScores.value = {}
    fetchPaperHistory()
  } catch (e) {
    showToast(e.message || '发布失败，请检查后端', 'error')
  }
}

// ── ASR ───────────────────────────────────────────────────────────────────────
let recognition = null
if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
  const SR = window.SpeechRecognition || window.webkitSpeechRecognition
  recognition = new SR()
  recognition.continuous = true
  recognition.interimResults = true
  recognition.lang = 'zh-CN'
  recognition.onresult = (e) => {
    let chunk = ''
    for (let i = e.resultIndex; i < e.results.length; i++)
      if (e.results[i].isFinal) chunk += e.results[i][0].transcript
    if (chunk) form.value.content += chunk
  }
  recognition.onerror = () => {
    isRecording.value = false
  }
  recognition.onend = () => {
    isRecording.value = false
  }
}

function toggleRecording() {
  if (!recognition) {
    showToast('当前浏览器不支持语音识别，请使用 Chrome', 'error')
    return
  }
  if (isRecording.value) {
    recognition.stop()
    isRecording.value = false
  } else {
    recognition.start()
    isRecording.value = true
  }
}

onMounted(() => {
  fetchList()
  fetchPaperHistory()
})
</script>

<style scoped>
/* ═══════════════════════ CSS Variables ═══════════════════════ */
:root,
* {
  --bg: #f1f5f9;
  --surface: #ffffff;
  --border: #e2e8f0;
  --text: #1e293b;
  --text-2: #475569;
  --text-3: #94a3b8;
  --blue: #2563eb;
  --blue-lt: #eff6ff;
  --blue-md: #dbeafe;
  --green: #10b981;
  --green-lt: #ecfdf5;
  --amber: #f59e0b;
  --amber-lt: #fffbeb;
  --purple: #8b5cf6;
  --purple-lt: #f5f3ff;
  --red: #ef4444;
  --red-lt: #fef2f2;
  --radius: 10px;
  --shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 4px 16px rgba(0, 0, 0, 0.04);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.08), 0 8px 32px rgba(0, 0, 0, 0.05);
}

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}
.app {
  font-family: 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', -apple-system, system-ui,
    sans-serif;
  font-size: 14px;
  color: var(--text);
  background: transparent;
  min-height: 100%;
}

/* ═════════ Tab bar ═════════ */
.tab-bar {
  background: var(--surface);
  padding: 0 28px;
  display: flex;
  gap: 4px;
  border-bottom: 1px solid var(--border);
  margin-bottom: 20px;
}
.tab {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 14px 18px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-3);
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: -1px;
}
.tab:hover {
  color: var(--text-2);
}
.tab--active {
  color: var(--blue);
  border-bottom-color: var(--blue);
}

/* ═════════ Page body ═════════ */
.page-body {
  padding: 24px 28px 40px;
  max-width: 1400px;
  margin: 0 auto;
}
.workspace {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ═════════ Stats row ═════════ */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.stat-card {
  background: var(--surface);
  border-radius: var(--radius);
  border: 1px solid var(--border);
  padding: 18px 20px;
  box-shadow: var(--shadow);
  display: flex;
  align-items: center;
  gap: 14px;
}
.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-icon--blue {
  background: var(--blue);
}
.stat-icon--green {
  background: var(--green);
}
.stat-icon--amber {
  background: var(--amber);
}
.stat-icon--purple {
  background: var(--purple);
}
.stat-content {
  flex: 1;
  min-width: 0;
}
.stat-num {
  display: block;
  font-size: 26px;
  font-weight: 700;
  color: var(--text);
  line-height: 1.1;
}
.stat-label {
  font-size: 12px;
  color: var(--text-3);
  margin-top: 3px;
  display: block;
}
.stat-trend {
  font-size: 11px;
  color: var(--text-3);
  white-space: nowrap;
}
.stat-trend.up {
  color: var(--green);
}

/* ═════════ Viz row ═════════ */
.viz-row {
  display: flex;
  gap: 16px;
}
.viz-card {
  flex: 1;
}

/* ═════════ Card ═════════ */
.card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border);
  padding: 20px 24px;
  box-shadow: var(--shadow-sm);
  transition: all 0.25s ease;
}
.card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--border-light);
}
.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}
.card-title-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.dot--blue {
  background: var(--primary);
}
.dot--green {
  background: var(--success);
}
.dot--amber {
  background: var(--warning);
}
.dot--purple {
  background: var(--purple);
}

/* ═════════ Donut chart ═════════ */
.donut-wrap {
  display: flex;
  align-items: center;
  gap: 20px;
}
.donut-svg {
  width: 120px;
  height: 120px;
  flex-shrink: 0;
}
.donut-center-num {
  font-size: 22px;
  font-weight: 700;
  fill: var(--text);
}
.donut-center-label {
  font-size: 11px;
  fill: var(--text-3);
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}
.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 3px;
  flex-shrink: 0;
}
.legend-text {
  font-size: 13px;
  color: var(--text-2);
}
.legend-text strong {
  color: var(--text);
}

/* ═════════ Bar chart ═════════ */
.bar-chart {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.bar-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.bar-label {
  font-size: 12px;
  color: var(--text-2);
  width: 32px;
  flex-shrink: 0;
}
.bar-track {
  flex: 1;
  height: 8px;
  background: var(--bg);
  border-radius: 4px;
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  border-radius: 4px;
  min-width: 4px;
}
.bar-val {
  font-size: 12px;
  font-weight: 600;
  color: var(--text);
  width: 24px;
  text-align: right;
}

/* ═════════ AI card ═════════ */
.ai-card {
  flex: 1.1;
}
.ai-desc {
  font-size: 13px;
  color: var(--text-3);
  line-height: 1.6;
  margin-top: -10px;
  margin-bottom: 16px;
}
.ai-preview {
  margin-top: 16px;
  background: var(--amber-lt);
  border: 1px solid #fde68a;
  border-radius: 8px;
  padding: 12px 14px;
}
.ai-preview-tag {
  font-size: 11px;
  font-weight: 600;
  color: var(--amber);
  margin-bottom: 6px;
}
.ai-preview-content {
  font-size: 13px;
  color: var(--text);
  line-height: 1.5;
  margin-bottom: 6px;
}
.ai-preview-answer {
  font-size: 12px;
  color: var(--text-2);
}
.ai-preview-answer strong {
  color: var(--blue);
}

/* ═════════ Main row ═════════ */
.main-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.entry-card,
.assemble-card {
}

/* ═════════ Fields ═════════ */
.field {
  margin-bottom: 14px;
}
.field-row {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}
.field-label {
  display: block;
  font-size: 11px;
  font-weight: 600;
  color: var(--text-3);
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: 0.4px;
}
.field-hint {
  font-weight: 400;
  text-transform: none;
  color: var(--text-3);
  opacity: 0.7;
}

.input,
.textarea {
  width: 100%;
  padding: 9px 12px;
  border: 1px solid var(--border);
  border-radius: 8px;
  font-size: 14px;
  background: var(--bg);
  color: var(--text);
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
  font-family: inherit;
  appearance: none;
}
.textarea {
  resize: vertical;
}
.input:focus,
.textarea:focus {
  border-color: var(--blue);
  background: #fff;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.asr-btn {
  display: flex;
  align-items: center;
  gap: 7px;
  width: 100%;
  margin-top: 8px;
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px dashed #bfdbfe;
  background: var(--blue-lt);
  color: var(--blue);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}
.asr-btn--on {
  border-color: #fca5a5;
  background: var(--red-lt);
  color: var(--red);
  animation: pulse-asr 1.2s infinite;
}
@keyframes pulse-asr {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.65;
  }
}

/* ═════════ Buttons ═════════ */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: var(--radius-md);
  border: none;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
  position: relative;
}
.btn:active {
  transform: scale(0.98);
}
.btn--blue {
  background: var(--primary);
  color: #fff;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.25);
}
.btn--blue:hover {
  background: var(--primary-hover);
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.35);
  transform: translateY(-0.5px);
}
.btn--amber {
  background: var(--warning);
  color: #fff;
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.25);
}
.btn--amber:hover {
  background: #d97706;
  box-shadow: 0 4px 14px rgba(245, 158, 11, 0.35);
  transform: translateY(-0.5px);
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
.btn--danger {
  border-color: var(--danger);
  color: var(--danger);
}
.btn--danger:hover {
  background: #fef2f2;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.2);
}
.btn--view {
  color: var(--primary);
  border-color: var(--primary-light);
}
.btn--view:hover {
  background: var(--primary-light);
}
.btn--full {
  width: 100%;
}
.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
  box-shadow: none !important;
}
.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* ═════════ Score summary / empty ═════════ */
.score-summary {
  background: var(--blue-lt);
  border: 1px solid var(--blue-md);
  border-radius: 8px;
  padding: 14px 16px;
  margin-bottom: 14px;
}
.score-summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: var(--text-2);
  padding: 3px 0;
}
.score-total {
  color: var(--blue);
  font-size: 16px;
}
.empty-select {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
  color: var(--text-3);
  font-size: 13px;
  gap: 10px;
  margin-bottom: 14px;
}
.selected-badge {
  margin-left: auto;
  background: var(--blue);
  color: #fff;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
}

/* ═════════ Table ═════════ */
.table-card {
  padding: 22px 0 0;
}
.table-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px 18px;
}
.table-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.search-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--bg);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 7px 12px;
}
.search-input {
  border: none;
  background: transparent;
  font-size: 13px;
  color: var(--text);
  outline: none;
  width: 160px;
  font-family: inherit;
}

.table-wrap {
  overflow-x: auto;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.data-table th {
  padding: 10px 14px;
  background: #f8fafc;
  color: var(--text-3);
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.4px;
  border-bottom: 1px solid var(--border);
  text-align: left;
  white-space: nowrap;
}
.data-table td {
  padding: 13px 14px;
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
  color: var(--text-2);
}
.data-table tr:last-child td {
  border-bottom: none;
}
.data-table tbody tr:hover td {
  background: #f8fafc;
}
.row--selected td {
  background: #eff6ff !important;
}
.table-empty {
  text-align: center;
  padding: 48px 20px !important;
  color: var(--text-3);
  font-style: italic;
}

.checkbox {
  width: 15px;
  height: 15px;
  accent-color: var(--blue);
  cursor: pointer;
}
.score-input {
  width: 56px;
  padding: 5px 7px;
  text-align: center;
  border: 1px solid var(--blue-md);
  border-radius: 6px;
  background: var(--blue-lt);
  color: var(--blue);
  font-weight: 600;
  font-size: 13px;
  outline: none;
}
.score-dash {
  color: #cbd5e1;
}
.content-cell {
  max-width: 340px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--text);
  font-weight: 500;
}
.paper-title-cell {
  font-weight: 600;
  color: var(--text);
}
.time-cell {
  color: var(--text-3);
  font-size: 12px;
  white-space: nowrap;
}
.id-chip {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 11px;
  color: var(--text-3);
}

.type-badge {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 5px;
  font-size: 11px;
  font-weight: 600;
}
.type-badge--single {
  background: var(--blue-lt);
  color: var(--blue);
}
.type-badge--multi {
  background: var(--purple-lt);
  color: var(--purple);
}
.type-badge--judge {
  background: var(--green-lt);
  color: var(--green);
}
.type-badge--essay {
  background: var(--amber-lt);
  color: var(--amber);
}

.source-badge {
  display: inline-block;
  padding: 3px 9px;
  border-radius: 5px;
  font-size: 11px;
  font-weight: 600;
}
.source-badge--ai {
  background: var(--amber-lt);
  color: var(--amber);
}
.source-badge--manual {
  background: #f1f5f9;
  color: var(--text-3);
}

.class-badge {
  background: #eff6ff;
  color: var(--blue);
  padding: 3px 9px;
  border-radius: 5px;
  font-size: 12px;
  font-weight: 500;
}
.score-highlight {
  color: var(--amber);
  font-weight: 700;
}

/* ═════════ Timeline ═════════ */
.timeline {
  display: flex;
  flex-direction: column;
  gap: 0;
}
.timeline-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 14px 0;
  border-bottom: 1px solid var(--border);
  position: relative;
}
.timeline-item:last-child {
  border-bottom: none;
}
.timeline-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--border);
  flex-shrink: 0;
  margin-top: 4px;
}
.timeline-dot--active {
  background: var(--blue);
  box-shadow: 0 0 0 3px var(--blue-md);
}
.timeline-body {
  flex: 1;
  min-width: 0;
}
.timeline-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 4px;
}
.timeline-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.tl-class {
  background: var(--blue-lt);
  color: var(--blue);
  padding: 2px 7px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
}
.tl-score {
  color: var(--amber);
  font-size: 11px;
  font-weight: 600;
}
.tl-count {
  color: var(--text-3);
  font-size: 11px;
}
.timeline-time {
  font-size: 11px;
  color: var(--text-3);
  white-space: nowrap;
  flex-shrink: 0;
}

/* ═════════ Class dist ═════════ */
.class-dist {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.class-dist-item {
  display: flex;
  align-items: center;
  gap: 10px;
}
.class-dist-info {
  display: flex;
  align-items: center;
  gap: 7px;
  width: 130px;
  flex-shrink: 0;
  font-size: 12px;
  color: var(--text-2);
}
.class-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.class-dist-bar-wrap {
  flex: 1;
  height: 6px;
  background: var(--bg);
  border-radius: 3px;
  overflow: hidden;
}
.class-dist-bar {
  height: 100%;
  border-radius: 3px;
  transition: width 0.8s ease;
  min-width: 4px;
}
.class-dist-num {
  font-size: 12px;
  font-weight: 600;
  color: var(--text);
  width: 20px;
  text-align: right;
}
.viz-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100px;
  color: var(--text-3);
  font-size: 13px;
}

/* ═════════ Modal ═════════ */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}
.modal-content {
  background: var(--surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  width: 100%;
  max-width: 800px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.modal-content--paper {
  max-width: 900px;
}
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 24px;
  border-bottom: 1px solid var(--border-card);
  background: var(--bg-page);
}
.modal-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}
.modal-close {
  background: transparent;
  border: none;
  cursor: pointer;
  color: var(--text-tertiary);
  padding: 6px;
  border-radius: 6px;
  transition: all 0.2s;
}
.modal-close:hover {
  color: var(--text-secondary);
  background: rgba(0, 0, 0, 0.05);
}
.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1px solid var(--border-card);
}

/* ── Paper modal styles ── */
.paper-header-info {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border-card);
}
.paper-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}
.paper-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}
.paper-meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}
.paper-questions {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.question-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: var(--bg-page);
  border-radius: var(--radius-md);
}
.question-number {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--blue);
  color: #fff;
  border-radius: 50%;
  font-size: 13px;
  font-weight: 600;
}
.question-content {
  flex: 1;
  min-width: 0;
}
.question-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.question-score {
  font-size: 12px;
  font-weight: 600;
  color: var(--amber);
  padding: 2px 8px;
  background: var(--amber-lt);
  border-radius: 4px;
}
.question-text {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.6;
  margin-bottom: 10px;
}
.question-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 10px;
}
.option-item {
  padding: 6px 12px;
  background: var(--bg-card);
  border: 1px solid var(--border-card);
  border-radius: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}
.question-answer {
  display: flex;
  align-items: center;
  gap: 6px;
  padding-top: 10px;
  border-top: 1px dashed var(--border-card);
}
.answer-label {
  font-size: 12px;
  color: var(--text-tertiary);
}
.answer-value {
  font-size: 13px;
  font-weight: 600;
  color: var(--green);
}
.empty-paper {
  text-align: center;
  padding: 48px;
  color: var(--text-tertiary);
  font-size: 14px;
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: all 0.3s ease;
}
.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
.modal-fade-enter-from .modal-content,
.modal-fade-leave-to .modal-content {
  transform: scale(0.95);
}

/* ═════════ Toast ═════════ */
.toast {
  position: fixed;
  top: 24px;
  right: 24px;
  z-index: 999;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  border-radius: var(--radius-md);
  font-size: var(--font-sm);
  font-weight: 500;
  box-shadow: var(--shadow-lg);
  background: var(--surface);
  border: 1px solid var(--border-card);
  color: var(--text-primary);
  min-width: 280px;
  max-width: 400px;
}
.toast--success {
  background: var(--green-light);
  border-color: var(--green-eco);
}
.toast--success .toast-icon {
  color: var(--green-eco);
}
.toast--success .toast-title {
  color: var(--green-eco);
}
.toast--error {
  background: var(--red-light);
  border-color: var(--red-primary);
}
.toast--error .toast-icon {
  color: var(--red-primary);
}
.toast--error .toast-title {
  color: var(--red-primary);
}
.toast-icon {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
}
.toast-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.toast-title {
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.3px;
}
.toast-message {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}
.toast-close {
  flex-shrink: 0;
  background: transparent;
  border: none;
  cursor: pointer;
  color: var(--text-tertiary);
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;
}
.toast-close:hover {
  color: var(--text-secondary);
  background: rgba(0, 0, 0, 0.05);
}
.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.toast-fade-enter-from {
  opacity: 0;
  transform: translateX(20px);
}
.toast-fade-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

/* ═════════ Transitions ═════════ */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.25s ease;
}
.slide-fade-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
