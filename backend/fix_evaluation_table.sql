USE smart_study_teacher;

ALTER TABLE tb_evaluation_report
ADD COLUMN response_count INT;

DESCRIBE tb_evaluation_report;