SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 教师及权限架构 (系统基石)
-- ----------------------------
DROP TABLE IF EXISTS `tb_teacher`;
CREATE TABLE `tb_teacher` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '教师唯一ID',
                              `username` VARCHAR(50) NOT NULL COMMENT '登录账号',
                              `password` VARCHAR(100) NOT NULL COMMENT '加密密码',
                              `name` VARCHAR(50) NOT NULL COMMENT '教师姓名',
                              `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像图片路径',
                              `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
                              `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师用户表';

DROP TABLE IF EXISTS `tb_class`;
CREATE TABLE `tb_class` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '班级ID',
                            `class_name` VARCHAR(50) NOT NULL COMMENT '班级名称',
                            `teacher_id` BIGINT NOT NULL COMMENT '所属班主任/管理教师ID',
                            PRIMARY KEY (`id`),
                            KEY `fk_class_teacher` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级信息表';

-- ----------------------------
-- 2. 课程与资源模块 (对应功能：课程管理、课程资源管理)
-- ----------------------------
DROP TABLE IF EXISTS `tb_course`;
CREATE TABLE `tb_course` (
                             `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程ID',
                             `course_name` VARCHAR(100) NOT NULL COMMENT '课程名称',
                             `teacher_id` BIGINT NOT NULL COMMENT '任课教师ID',
                             `description` TEXT COMMENT '课程简介(大模型分析数据源之一)',
                             `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程主表';

DROP TABLE IF EXISTS `tb_course_resource`;
CREATE TABLE `tb_course_resource` (
                                      `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '资源ID',
                                      `course_id` BIGINT NOT NULL COMMENT '所属课程ID',
                                      `title` VARCHAR(150) NOT NULL COMMENT '资源名称(如：课件PPT/教学视频)',
                                      `file_url` VARCHAR(255) NOT NULL COMMENT '原始文件存储路径',
                                      `segment_status` TINYINT DEFAULT '0' COMMENT '图像分割状态: 0未处理, 1已切图提取关键内容',
                                      `segmented_regions` TEXT COMMENT '💥[图像分割算法] 存储课件图片分割后的关键区域坐标及文字',
                                      `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                      PRIMARY KEY (`id`),
                                      KEY `fk_res_course` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程资源媒体表';

-- ----------------------------
-- 3. 题库与试卷模块 (对应功能：试题试卷管理、大模型/ASR出题)
-- ----------------------------
DROP TABLE IF EXISTS `tb_question`;
CREATE TABLE `tb_question` (
                               `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '试题ID',
                               `course_id` BIGINT NOT NULL COMMENT '所属课程ID',
                               `type` VARCHAR(20) NOT NULL COMMENT '题型: SINGLE(单选), MULTI(多选), JUDGE(判断), GAP(填空), ESSAY(简答)',
                               `content` TEXT NOT NULL COMMENT '题目题干内容',
                               `options` TEXT COMMENT '选择题选项(如 ["A.xxx", "B.xxx"])，非选择题为NULL',
                               `answer` TEXT NOT NULL COMMENT '标准答案(用于AI核对标准)',
                               `is_llm_generated` TINYINT DEFAULT '0' COMMENT '💥[对话大模型] 是否为AI辅助生成的题目: 0否, 1是',
                               `asr_audio_url` VARCHAR(255) DEFAULT NULL COMMENT '💥[ASR算法] 教师语音录入的原始音频文件路径(留痕)',
                               `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能化题库表';

DROP TABLE IF EXISTS `tb_exam_paper`;
CREATE TABLE `tb_exam_paper` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '试卷ID',
                                 `course_id` BIGINT NOT NULL COMMENT '所属课程ID',
                                 `title` VARCHAR(100) NOT NULL COMMENT '试卷名称',
                                 `total_score` INT DEFAULT '100' COMMENT '总分',
                                 `class_id` BIGINT DEFAULT NULL COMMENT '发布目标班级ID',
                                 `teacher_id` BIGINT DEFAULT NULL COMMENT '组卷教师ID',
                                 `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (`id`),
                                 KEY `idx_paper_teacher_course` (`teacher_id`, `course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷主表';

DROP TABLE IF EXISTS `tb_paper_question`;
CREATE TABLE `tb_paper_question` (
                                     `id` BIGINT NOT NULL AUTO_INCREMENT,
                                     `paper_id` BIGINT NOT NULL COMMENT '试卷ID',
                                     `question_id` BIGINT NOT NULL COMMENT '试题ID',
                                     `sort` INT DEFAULT '0' COMMENT '题目在试卷中的顺序排序',
                                     `score` INT NOT NULL COMMENT '该题在这张卷子里的分值',
                                     PRIMARY KEY (`id`),
                                     KEY `idx_paper` (`paper_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷-试题中间映射表';

-- ----------------------------
-- 4. 作业、测评与实训核心业务 (对应功能：作业、测评、实训管理)
-- ----------------------------
DROP TABLE IF EXISTS `tb_task`;
CREATE TABLE `tb_task` (
                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务任务ID',
                           `course_id` BIGINT NOT NULL COMMENT '所属课程ID',
                           `class_id` BIGINT NOT NULL COMMENT '接收该任务的班级ID',
                           `title` VARCHAR(100) NOT NULL COMMENT '任务名称',
                           `type` VARCHAR(20) NOT NULL COMMENT '任务类型: HOMEWORK(普通作业), EXAM(在线测评), PRACTICE(实训项目)',
                           `content_text` TEXT COMMENT '任务文本描述',
                           `paper_id` BIGINT DEFAULT NULL COMMENT '关联的试卷ID (若是在线测评/考试则必填)',
                           `deadline` DATETIME NOT NULL COMMENT '截止交作业时间',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业/测评/实训发布统一任务表';

-- ----------------------------
-- 5. 学生提交、AI判定与成绩管理 (核心算法对接口！对应功能：成绩管理)
-- ----------------------------
DROP TABLE IF EXISTS `tb_submission`;
CREATE TABLE `tb_submission` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '提交记录ID',
                                 `task_id` BIGINT NOT NULL COMMENT '关联的任务ID',
                                 `student_id` BIGINT NOT NULL COMMENT '学生ID',
                                 `student_name` VARCHAR(50) NOT NULL COMMENT '学生姓名(冗余字段方便查询)',
                                 `submit_text` TEXT COMMENT '学生文本回答',
                                 `file_url` VARCHAR(255) DEFAULT NULL COMMENT '学生上传的作业/试卷/实训报告图片路径',

    -- 算法核心字段
                                 `ocr_raw_text` TEXT COMMENT '💥[图像分类/OCR] 识别出来的试卷手写文本串',
                                 `ai_score` INT DEFAULT NULL COMMENT '💥[图像分类] AI针对客观题分类自动批改出的分数',
                                 `ai_comment` TEXT COMMENT '💥[AI批改] AI生成的批改意见和反馈',
                                 `plagiarism_rate` DECIMAL(5,2) DEFAULT '0.00' COMMENT '💥[目标检测/文本查重] 抄袭痕迹检测概率 (0-100%)',
                                 `is_cheated` TINYINT DEFAULT '0' COMMENT '💥[目标检测] 是否判定违规作答/作弊: 0否, 1是',
                                 `ai_review_voice_url` VARCHAR(255) DEFAULT NULL COMMENT '💥[TTS算法] 教师文本评语转换后的语音播报文件路径',

                                 `teacher_score` INT DEFAULT NULL COMMENT '教师最终复核给出的分数',
                                 `teacher_comment` TEXT COMMENT '教师手写文本评语',
                                 `status` VARCHAR(20) DEFAULT 'SUBMITTED' COMMENT '状态: SUBMITTED(已交), AI_PROCESSED(AI已阅), GRADED(批改完成)',
                                 `submit_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (`id`),
                                 KEY `idx_task_student` (`task_id`,`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生作业试卷提交与AI判分表';

-- ----------------------------
-- 6. 问卷、评价与质量分析 (对应功能：问卷调查、教学效果评价)
-- ----------------------------
DROP TABLE IF EXISTS `tb_questionnaire`;
CREATE TABLE `tb_questionnaire` (
                                    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '问卷ID',
                                    `teacher_id` BIGINT NOT NULL COMMENT '发起调查的教师ID',
                                    `class_id` BIGINT DEFAULT NULL COMMENT '目标班级ID',
                                    `course_id` BIGINT DEFAULT NULL COMMENT '关联课程ID',
                                    `title` VARCHAR(150) NOT NULL COMMENT '问卷标题(如: 2026期中教学满意度调查)',
                                    `content_json` TEXT NOT NULL COMMENT '问卷题目题干(以JSON数组格式存放各种量表题)',
                                    `status` TINYINT DEFAULT '1' COMMENT '状态: 0关闭, 1开启',
                                    `response_count` INT DEFAULT '0' COMMENT '问卷填写人数统计',
                                    `avg_score` DOUBLE DEFAULT 0 COMMENT '问卷平均评分',
                                    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷调查表';

DROP TABLE IF EXISTS `tb_evaluation_report`;
CREATE TABLE `tb_evaluation_report` (
                                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价报告ID',
                                        `teacher_id` BIGINT NOT NULL COMMENT '被评价教师ID',
                                        `course_id` BIGINT NOT NULL COMMENT '关联课程ID',
                                        `course_name` VARCHAR(100) DEFAULT NULL COMMENT '课程名称',
                                        `teacher_name` VARCHAR(50) DEFAULT NULL COMMENT '教师姓名',
                                        `avg_satisfaction` DECIMAL(4,2) COMMENT '根据问卷汇总出的客观满意度得分',
                                        `response_count` INT DEFAULT '0' COMMENT '问卷填写人数',
                                        `llm_analysis_report` TEXT COMMENT '💥[对话大模型] AI综合问卷、学生成绩、出勤率生成的教学效果诊断报告',
                                        `generate_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学效果评价及大模型诊断报告表';

DROP TABLE IF EXISTS `tb_questionnaire_answer`;
CREATE TABLE `tb_questionnaire_answer` (
                                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '答案记录ID',
                                           `questionnaire_id` BIGINT NOT NULL COMMENT '关联问卷ID',
                                           `student_id` BIGINT NOT NULL COMMENT '学生ID',
                                           `student_name` VARCHAR(50) NOT NULL COMMENT '学生姓名(冗余字段)',
                                           `scores_json` TEXT COMMENT '学生各题得分的JSON数据',
                                           `total_score` DECIMAL(5,2) DEFAULT NULL COMMENT '问卷总分',
                                           `submit_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
                                           PRIMARY KEY (`id`),
                                           KEY `idx_questionnaire_student` (`questionnaire_id`, `student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生问卷答案提交表';

-- ----------------------------
-- 7. 问题中心与系统门户 (对应功能：问题中心、网站门户)
-- ----------------------------
DROP TABLE IF EXISTS `tb_issue_center`;
CREATE TABLE `tb_issue_center` (
                                   `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '提问ID',
                                   `course_id` BIGINT NOT NULL COMMENT '关联课程ID',
                                   `student_id` BIGINT DEFAULT NULL COMMENT '提问学生ID',
                                   `student_name` VARCHAR(50) NOT NULL COMMENT '提问学生',
                                   `question_text` TEXT NOT NULL COMMENT '学生提问的问题内容',
                                   `ai_suggested_answer` TEXT COMMENT '💥[对话大模型] AI助教根据课程知识库自动秒回的建议参考答案',
                                   `teacher_reply` TEXT COMMENT '教师官方真实回复',
                                   `status` TINYINT DEFAULT '0' COMMENT '0未解决, 1教师已解答',
                                   `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问题中心答疑表';

DROP TABLE IF EXISTS `tb_portal_notice`;
CREATE TABLE `tb_portal_notice` (
                                    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
                                    `teacher_id` BIGINT NOT NULL COMMENT '发布教师ID',
                                    `title` VARCHAR(150) NOT NULL COMMENT '门户通知标题',
                                    `content` TEXT NOT NULL COMMENT '通知正文公告',
                                    `tts_audio_url` VARCHAR(255) DEFAULT NULL COMMENT '💥[TTS算法] 公告文本转换后的语音播报路径',
                                    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网站门户通知公告表';

-- ----------------------------
-- 8. 学生表
-- ----------------------------
DROP TABLE IF EXISTS `tb_student`;
CREATE TABLE `tb_student` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '学生ID',
    `student_name` VARCHAR(50) NOT NULL COMMENT '学生姓名',
    `class_id` BIGINT NOT NULL COMMENT '所属班级ID',
    `student_no` VARCHAR(20) DEFAULT NULL COMMENT '学号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `fk_student_class` (`class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生信息表';

-- ----------------------------
-- 9. 演示数据
-- ----------------------------

-- 教师
INSERT INTO `tb_teacher` (`id`, `username`, `password`, `name`) VALUES (1, 'admin', '123456', '张教授');

-- 班级
INSERT INTO `tb_class` (`id`, `class_name`, `teacher_id`) VALUES (1, '2024级软件工程1班', 1), (2, '2024级计算机科学2班', 1);

-- 课程
INSERT INTO `tb_course` (`id`, `course_name`, `teacher_id`, `description`) VALUES
(1, '大模型应用与微调技术', 1, '讲授深度学习基础、Transformer架构、大模型接口调用与提示词工程'),
(2, '程序设计基础(Python)', 1, 'Python基础语法、数据结构、面向对象编程入门');

-- 学生
INSERT INTO `tb_student` (`id`, `student_name`, `class_id`, `student_no`) VALUES
(1, '李明', 1, '2024001'), (2, '王小红', 1, '2024002'), (3, '张伟', 1, '2024003'),
(4, '刘芳', 2, '2024004'), (5, '陈强', 2, '2024005'),
(6, '赵强', 1, '2024006'), (7, '孙丽', 2, '2024007'), (8, '周杰', 1, '2024008');

-- 题库
INSERT INTO `tb_question` (`id`, `course_id`, `type`, `content`, `options`, `answer`, `is_llm_generated`) VALUES
(1, 1, 'SINGLE', 'Transformer模型的核心机制是什么？', '["A. CNN","B. RNN","C. 自注意力机制","D. LSTM"]', 'C', 0),
(2, 1, 'SINGLE', '以下哪个是Python定义函数的关键字？', '["A. func","B. def","C. fn","D. lambda"]', 'B', 0),
(3, 1, 'JUDGE', 'BERT是基于Transformer架构的预训练模型', NULL, 'T', 0),
(4, 1, 'JUDGE', 'GPT属于CNN架构', NULL, 'F', 0),
(5, 1, 'GAP', '深度学习中的____机制允许模型关注输入的不同部分', NULL, '注意力', 0),
(6, 1, 'SINGLE', 'Python中列表(list)和元组(tuple)的主要区别是？', '["A. 列表有序元组无序","B. 列表可变元组不可变","C. 没有区别","D. 元组可以排序"]', 'B', 0),
(7, 2, 'SINGLE', 'Python中用于输出到控制台的函数是？', '["A. input()","B. print()","C. echo()","D. write()"]', 'B', 0),
(8, 2, 'SINGLE', '以下哪个是Python的不可变数据类型？', '["A. list","B. dict","C. str","D. set"]', 'C', 0);

-- 试卷
INSERT INTO `tb_exam_paper` (`id`, `course_id`, `title`, `total_score`, `class_id`, `teacher_id`) VALUES
(1, 1, '大模型期中测验', 40, 1, 1);

INSERT INTO `tb_paper_question` (`paper_id`, `question_id`, `sort`, `score`) VALUES
(1, 1, 1, 10), (1, 2, 2, 10), (1, 3, 3, 10), (1, 4, 4, 10);

-- 任务（三种类型全覆盖）
INSERT INTO `tb_task` (`id`, `course_id`, `class_id`, `title`, `type`, `content_text`, `paper_id`, `deadline`) VALUES
(1, 1, 1, '📝 作业：分析Transformer架构', 'HOMEWORK', '请详细分析Transformer架构的核心组件及其工作原理，不少于100字', NULL, '2026-06-30 23:59:59'),
(2, 1, 1, '📋 选择题小测：深度学习基础', 'CHOICE', '[ANSWERS:C,B,T,F] 请上传选择题答题卡图片', NULL, '2026-06-30 23:59:59'),
(3, 1, 1, '📄 大模型期中测验', 'EXAM', '期中正式考试，关联试卷进行AI批改', 1, '2026-06-30 23:59:59');

-- 提交（文本作业 + 图片提交 + 试卷提交）
INSERT INTO `tb_submission` (`id`, `task_id`, `student_id`, `student_name`, `submit_text`, `file_url`, `status`, `ai_score`, `ai_comment`, `plagiarism_rate`, `submit_time`) VALUES
-- 文本作业(task 1)：用于演示DeepSeek评阅+文本查重
(1, 1, 1, '李明', 'Transformer架构由编码器和解码器组成，核心是自注意力机制和多头注意力。编码器将输入序列映射到隐空间，解码器从隐空间生成输出序列。位置编码用于保留序列顺序信息。', NULL, 'AI_PROCESSED', 85, '共0题，答对0题，得分0/0 【AI评语】对Transformer架构理解深入，表述清晰。', 0.00, '2026-06-01 10:00:00'),
(2, 1, 2, '王小红', 'Transformer架构包含编码器和解码器，核心组件是自注意力机制和多头注意力。编码器负责将输入映射到隐空间，解码器从隐空间生成输出。同时使用位置编码保留顺序信息。', NULL, 'AI_PROCESSED', 80, '共0题，答对0题，得分0/0 【AI评语】理解基本到位，建议更详细描述前馈网络层的作用。', 79.50, '2026-06-01 11:00:00'),
(3, 1, 3, '张伟', '深度学习在自然语言处理中有广泛应用。循环神经网络处理序列数据但存在梯度消失问题。LSTM和GRU是改进变体。注意力机制解决了长距离依赖问题，Transformer基于此设计。', NULL, 'AI_PROCESSED', 70, '共0题，答对0题，得分0/0 【AI评语】内容偏离了Transformer架构主题。', 0.00, '2026-06-01 12:00:00'),
(4, 1, 6, '赵强', 'Transformer架构由编码器和解码器组成，核心是自注意力机制和多头注意力。编码器将输入序列映射到隐空间，解码器从隐空间生成输出序列。位置编码用于保留序列顺序信息。', NULL, 'AI_PROCESSED', 85, '共0题，答对0题，得分0/0 【AI评语】与李明提交内容高度相似。', 100.00, '2026-06-02 09:00:00'),
-- 选择题(task 2)：图片提交
(5, 2, 1, '李明', NULL, 'uploads/submissions/demo_choice_card.png', 'AI_PROCESSED', 30, '选择题作业：共4题，答对3题，得分30/40', 0.00, '2026-06-03 10:00:00'),
(6, 2, 2, '王小红', NULL, 'uploads/submissions/demo_choice_card2.png', 'AI_PROCESSED', 20, '选择题作业：共4题，答对2题，得分20/40', 0.00, '2026-06-03 11:00:00'),
-- 试卷考试(task 3, paperId=1)：
(7, 3, 3, '张伟', NULL, 'uploads/submissions/demo_exam_paper.png', 'AI_PROCESSED', 30, '共4题，答对3题，得分30/40', 30.00, '2026-06-05 14:00:00'),
(8, 3, 6, '赵强', NULL, 'uploads/submissions/demo_exam_paper2.png', 'AI_PROCESSED', 20, '共4题，答对2题，得分20/40', 30.00, '2026-06-05 14:30:00');

-- 教学评价报告
INSERT INTO `tb_evaluation_report` (`id`, `teacher_id`, `course_id`, `course_name`, `teacher_name`, `avg_satisfaction`, `response_count`, `llm_analysis_report`, `generate_time`) VALUES
(1, 1, 1, '大模型应用与微调技术', '张教授', 87.50, 45, '【教学效果诊断报告】\n\n📊 数据概览：\n本次评价满意度得分 87.50 分，整体处于优秀水平。\n\n✅ 教学亮点：\n1. 教师授课态度认真负责，教学准备充分\n2. 课程内容覆盖全面，理论与实践结合紧密\n3. 课后作业设计合理，能有效巩固知识点\n\n⚠️ 存在问题：\n1. 课堂互动环节偏少，学生主动参与率有待提高\n2. 部分难点讲解速度偏快，基础薄弱学生跟不上\n\n💡 改进建议：\n1. 增加课堂小组讨论和随堂测试环节\n2. 针对难点录制微课视频供学生反复观看', '2026-06-01 10:00:00'),
(2, 1, 2, '程序设计基础(Python)', '张教授', 82.30, 38, NULL, '2026-06-01 10:00:00'),
(3, 1, 1, '大模型应用与微调技术', '张教授', 91.20, 52, '【教学效果诊断报告】\n\n整体满意度91.2分，教学效果显著。学生普遍反映课程内容丰富。', '2026-06-08 10:00:00');

-- 问题中心示例
INSERT INTO `tb_issue_center` (`id`, `course_id`, `student_id`, `student_name`, `question_text`, `ai_suggested_answer`, `status`) VALUES
(1, 1, 1, '李明', 'Transformer中的位置编码为什么用正弦余弦而不是直接学一个embedding？', '正弦余弦位置编码具有良好的外推性，可以处理比训练时更长的序列。这是Transformer原始论文的设计选择，后来的模型如BERT也使用了可学习的位置编码。', 1),
(2, 1, 2, '王小红', '多头注意力的"头"数量如何选择？', '头数通常是模型维度的约数，常见配置：d_model=512时h=8，d_model=768时h=12。头数越多，每个头关注的子空间越小，但计算量越大。', 1);

-- 门户公告
INSERT INTO `tb_portal_notice` (`id`, `teacher_id`, `title`, `content`, `create_time`) VALUES
(1, 1, '关于期末考试安排的通知', '本学期期末考试定于2026年7月1日进行，请同学们提前做好准备，认真复习。', '2026-06-01 08:00:00'),
(2, 1, '大模型应用课程作业提交提醒', '第一次编程作业截止日期为6月30日，请还未提交的同学尽快完成并提交。', '2026-06-05 08:00:00');

SET FOREIGN_KEY_CHECKS = 1;
