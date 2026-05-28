import request from './request'

/**
 * 问卷调查 API — 对接后端 /api/questionnaire
 * 数据表：tb_questionnaire
 */
const questionnaireApi = {
  /** 获取问卷列表（可按教师筛选） */
  list(teacherId) {
    return request.get('/api/questionnaire/list', {
      params: teacherId ? { teacherId } : {}
    })
  },

  /** 获取问卷详情 */
  getById(id) {
    return request.get(`/api/questionnaire/${id}`)
  },

  /** 创建问卷 */
  create(data) {
    return request.post('/api/questionnaire/create', data)
  },

  /** 更新问卷 */
  update(id, data) {
    return request.put(`/api/questionnaire/${id}`, data)
  },

  /** 删除问卷 */
  delete(id) {
    return request.delete(`/api/questionnaire/${id}`)
  },

  /** 切换问卷状态（发布/关闭） */
  toggleStatus(id, status) {
    return request.put(`/api/questionnaire/${id}/status`, { status })
  },

  /** 学生提交问卷答案 */
  submitAnswer(id, answerData) {
    return request.post(`/api/questionnaire/${id}/submit`, answerData)
  },

  /** 查询某份问卷的所有答案 */
  getAnswers(id) {
    return request.get(`/api/questionnaire/${id}/answers`)
  },

  /** 关闭问卷并自动生成评价报告 */
  closeAndGenerate(id) {
    return request.post(`/api/questionnaire/${id}/close-and-generate`)
  }
}

export default questionnaireApi
