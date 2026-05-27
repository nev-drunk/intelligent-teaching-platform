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
                                      `segmented_regions` JSON DEFAULT NULL COMMENT '💥[图像分割算法] 存储课件图片分割后的关键区域坐标及文字',
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
                               `options` JSON DEFAULT NULL COMMENT '选择题选项(如 ["A.xxx", "B.xxx"])，非选择题为NULL',
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
                                 `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (`id`)
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
                                    `title` VARCHAR(150) NOT NULL COMMENT '问卷标题(如: 2026期中教学满意度调查)',
                                    `content_json` JSON NOT NULL COMMENT '问卷题目题干(以JSON数组格式存放各种量表题)',
                                    `status` TINYINT DEFAULT '1' COMMENT '状态: 0关闭, 1开启',
                                    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷调查表';

DROP TABLE IF EXISTS `tb_evaluation_report`;
CREATE TABLE `tb_evaluation_report` (
                                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价报告ID',
                                        `teacher_id` BIGINT NOT NULL COMMENT '被评价教师ID',
                                        `course_id` BIGINT NOT NULL COMMENT '关联课程ID',
                                        `avg_satisfaction` DECIMAL(4,2) COMMENT '根据问卷汇总出的客观满意度得分',
                                        `llm_analysis_report` TEXT COMMENT '💥[对话大模型] AI综合问卷、学生成绩、出勤率生成的教学效果诊断报告',
                                        `generate_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学效果评价及大模型诊断报告表';

-- ----------------------------
-- 7. 问题中心与系统门户 (对应功能：问题中心、网站门户)
-- ----------------------------
DROP TABLE IF EXISTS `tb_issue_center`;
CREATE TABLE `tb_issue_center` (
                                   `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '提问ID',
                                   `course_id` BIGINT NOT NULL COMMENT '关联课程ID',
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
-- 8. 默认测试基础初始化数据
-- ----------------------------
INSERT INTO `tb_teacher` (`id`, `username`, `password`, `name`) VALUES (1, 'admin', '123456', '张教授');
INSERT INTO `tb_class` (`id`, `class_name`, `teacher_id`) VALUES (1, '2024级软件工程1班', 1);
INSERT INTO `tb_course` (`id`, `course_name`, `teacher_id`, `description`) VALUES (1, '大模型应用与微调技术', 1, '本课程讲授深度学习、大模型接口调用及提示词工程。');

SET FOREIGN_KEY_CHECKS = 1;

ALTER TABLE tb_exam_paper ADD COLUMN class_id BIGINT COMMENT '发布的班级ID';

-- 1. 给试卷表增加教师归属字段，用于多老师独立发布隔离
ALTER TABLE tb_exam_paper ADD COLUMN teacher_id BIGINT COMMENT '教师ID';

-- 2. (可选) 如果你想让题目也区分是谁录入的，也可以给题目表追加
ALTER TABLE tb_question ADD COLUMN teacher_id BIGINT DEFAULT 1 COMMENT '教师ID';