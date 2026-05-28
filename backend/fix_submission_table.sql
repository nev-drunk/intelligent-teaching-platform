USE smart_study_teacher;

-- 添加缺失的字段
ALTER TABLE tb_submission
ADD COLUMN ai_comment TEXT;

-- 查看表结构确认
DESCRIBE tb_submission;