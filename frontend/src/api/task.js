import request from './request'

/**
 * 任务管理 API — 对接后端 /api/tasks
 * 数据表：tb_task
 */

/**
 * 获取教师的任务列表
 * @param {number} teacherId - 教师ID
 * @returns {Promise}
 */
export function getTasksByTeacher(teacherId) {
  return request.get('/api/tasks', { params: { teacherId } })
}

/**
 * 获取任务列表（可按课程和班级筛选）
 * @param {object} params - 查询参数
 * @param {number} params.courseId - 课程ID（可选）
 * @param {number} params.classId - 班级ID（可选）
 * @param {number} params.teacherId - 教师ID（可选）
 * @returns {Promise}
 */
export function fetchTasks(params) {
  return request.get('/api/tasks', { params })
}

/**
 * 获取单个任务详情
 * @param {number} taskId - 任务ID
 * @returns {Promise}
 */
export function getTaskById(taskId) {
  return request.get(`/api/tasks/${taskId}`)
}

/**
 * 创建新任务
 * @param {object} task - 任务数据对象
 * @param {string} task.title - 任务名称
 * @param {string} task.type - 任务类型 (HOMEWORK|EXAM|PRACTICE)
 * @param {number} task.courseId - 课程ID
 * @param {number} task.classId - 班级ID
 * @param {string} task.contentText - 任务描述
 * @param {number} task.paperId - 试卷ID（可选）
 * @param {string} task.deadline - 截止时间
 * @returns {Promise}
 */
export function createTask(task) {
  return request.post('/api/tasks', task)
}

/**
 * 更新任务
 * @param {number} taskId - 任务ID
 * @param {object} task - 任务数据
 * @returns {Promise}
 */
export function updateTask(taskId, task) {
  return request.put(`/api/tasks/${taskId}`, task)
}

/**
 * 删除任务
 * @param {number} taskId - 任务ID
 * @returns {Promise}
 */
export function deleteTask(taskId) {
  return request.delete(`/api/tasks/${taskId}`)
}

/**
 * 获取任务的所有提交记录
 * @param {number} taskId - 任务ID
 * @returns {Promise}
 */
export function getTaskSubmissions(taskId) {
  return request.get(`/api/tasks/${taskId}/submissions`)
}
