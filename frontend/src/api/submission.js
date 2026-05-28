import request from './request'

/**
 * 获取提交列表（分页）
 */
export function fetchSubmissionList(params) {
  return request.get('/api/submission/list', { params })
}

/**
 * 根据ID获取提交记录
 */
export function fetchSubmissionById(id) {
  return request.get(`/api/submission/${id}`)
}

/**
 * 上传作业提交
 */
export function uploadSubmission(formData) {
  return request.post('/api/submission/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * AI批改
 */
export function aiGradeSubmission(id) {
  return request.post(`/api/submission/ai-grade/${id}`)
}

/**
 * 教师批改
 */
export function teacherGradeSubmission(data) {
  return request.put('/api/submission/teacher-grade', data)
}

/**
 * 删除提交记录
 */
export function deleteSubmission(id) {
  return request.delete(`/api/submission/${id}`)
}

/**
 * 根据教师ID获取提交列表
 */
export function fetchSubmissionsByTeacherId(teacherId) {
  return request.get(`/api/submission/teacher/${teacherId}`)
}

/**
 * 更新评语（兼容旧版API）
 */
export function fetchSubmissions(teacherId) {
  return request.get(`/api/submission/teacher/${teacherId}`)
}

/**
 * 更新评语和分数（兼容旧版API）
 */
export function updateComment(id, comment, score) {
  return request.put('/api/submission/teacher-grade', {
    submissionId: id,
    teacherComment: comment,
    teacherScore: score
  })
}
