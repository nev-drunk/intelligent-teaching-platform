# Intelligent Teaching Platform

智能化在线教学支持服务平台（教师中心系统），旨在辅助教师进行课程管理、作业管理、测评分析以及教学效果评估，提高教学效率与质量。

---

## 项目结构

```
intelligent-teaching-platform
├── backend/     # 后端（Spring Boot 2.x + MyBatis-Plus）
├── frontend/    # 前端（Vue 3 + Vite + Element Plus）
└── ai-service/  # AI 微服务（Flask + ONNX / YOLO / SBERT）
```

---

## 技术栈

### 前端

- Vue 3（Composition API）
- Vue Router 4
- Pinia（状态管理）
- Vite 5（构建工具）
- Element Plus（UI 组件库）
- ECharts（数据可视化）
- Axios（HTTP 请求）

### 后端

- Spring Boot 2.x
- MyBatis-Plus
- MySQL 8.x
- JWT 认证
- 百度 OCR SDK

### AI 服务

- Flask（Python Web 框架）
- ONNX Runtime（OCR 推理）
- YOLOv8（版面检测 / 课件识别）
- Sentence-BERT（文本相似度）
- 阿里云 DashScope（TTS 语音合成 / 大模型对话）
- DeepSeek API（AI 批改评语）

- 这里为了交作业ai服务冗余，可以视情况调整

---

## 功能模块

- 网站门户 — 公告发布、TTS 语音播报
- 课程管理 — 课程 CRUD、班级关联
- 课程资源管理 — 课件上传、版面分析、内容识别
- 作业管理 — 作业发布、提交、AI 批改（OCR + DeepSeek 评语）
- 测评管理 — 试卷组卷、答题卡识别
- 成绩管理 — 成绩统计、可视化分析
- 实训管理 — 实训任务发布与提交
- 问卷调查管理 — 问卷创建与数据收集
- 教学效果评价 — 多维度评价、AI 诊断报告
- 问题中心（答疑）— 学生提问、教师答疑

---

## 智能算法模块

| 模块 | 技术方案 | 用途 |
|------|---------|------|
| 图像分类 | YOLOv8 | 作业/试卷自动分类、客观题辅助批改 |
| 目标检测 | YOLOv8 | 答题卡版面检测、抄袭行为检测 |
| OCR 识别 | ONNX + EMNIST | 手写体识别、答题卡读取 |
| 文本相似度 | Sentence-BERT | 作业抄袭检测 |
| 大模型对话 | DeepSeek API | 辅助备课、生成试题、AI 评语 |
| 语音合成 (TTS) | DashScope qwen3-tts-flash | 评语与通知语音播报 |
| 语音识别 (ASR) | DashScope | 语音录入试题、语音批改 |

---

## 环境要求

| 依赖 | 版本 |
|------|------|
| Node.js | 18.x+ |
| npm | 9.x+ |
| JDK | 17+ |
| MySQL | 8.x |
| Python | 3.10+ |

---

## 快速开始

### 1. 环境变量配置

```bash
# 在项目根目录复制环境变量模板
cp .env.example .env

# 编辑 .env 文件，填入真实的 API 密钥和配置
# 注意：.env 已被 .gitignore 忽略，不会提交到 Git
```

需要配置的环境变量：

| 变量名 | 说明 |
|--------|------|
| `VITE_API_BASE_URL` | 前端访问后端的基地址（默认 `http://localhost:8081`） |
| `DB_PASSWORD` | 数据库密码 |
| `DEEPSEEK_API_KEY` | DeepSeek 大模型 API 密钥 |
| `BAIDU_OCR_APP_ID` | 百度 OCR 应用 ID |
| `BAIDU_OCR_API_KEY` | 百度 OCR API Key |
| `BAIDU_OCR_SECRET_KEY` | 百度 OCR Secret Key |
| `AI_SERVICE_URL` | AI 微服务地址（默认 `http://localhost:5000`） |
| `DASHSCOPE_API_KEY` | 阿里云 DashScope API 密钥（TTS / 大模型） |
| `JWT_SECRET` | JWT 签名密钥 |

### 2. 数据库初始化

```sql
-- 创建数据库（字符集 utf8mb4）
CREATE DATABASE IF NOT EXISTS smart_study_teacher
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

-- 后端启动时会自动执行 init.sql 初始化表结构（如已配置 spring.sql.init.mode=always）
```

### 3. 启动 AI 服务

```bash
cd ai-service
pip install -r requirements.txt
python main.py
# AI 服务运行在 http://localhost:5000
```

> AI 模型文件需放置在 `ai-service/models/` 目录下（该目录已被 .gitignore 忽略）

### 4. 启动后端

使用 IDEA 打开 `backend` 目录，运行 Spring Boot 主类 `BackendApplication`。

后端运行在 `http://localhost:8081`。

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问 `http://localhost:5173`。

---

## 启动顺序

```
1. MySQL 数据库
2. AI 服务 (Flask, :5000)
3. 后端 (Spring Boot, :8081)
4. 前端 (Vite, :5173)
```

---

## 当前进度

- [x] 项目基础结构搭建
- [x] 前后端项目初始化与联调
- [x] 用户登录与 JWT 认证
- [x] 课程 / 资源 / 作业 CRUD
- [x] AI 服务集成（OCR、版面检测、课件识别、相似度、TTS）
- [x] 作业 AI 批改（OCR + DeepSeek + 抄袭检测）
- [x] 测评管理与答题卡识别
- [x] 门户公告 + TTS 语音播报
- [x] 敏感配置迁移至 .env 环境变量
- [ ] 生产环境部署优化

---

## 开发说明

- 前端通过 Vite 代理转发 `/api` 请求到后端，开发环境无需手动处理跨域
- 所有 API 密钥、数据库密码等敏感信息通过 `.env` 文件管理，**严禁硬编码在源码中**
- 模型文件（`.pt`、`.onnx`、`.pth` 等）不会被 Git 追踪

---

## 项目说明

本项目为课程设计项目，重点在于系统功能实现与整体架构设计，优先保证系统可运行与可展示。
