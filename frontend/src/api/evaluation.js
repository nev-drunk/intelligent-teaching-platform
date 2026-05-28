import request from './request'

/**
 * 教学效果评价 API — 对接后端 /api/evaluation
 * 数据表：tb_evaluation_report
 */
const evaluationApi = {
  /** 获取评价报告列表（可按教师筛选） */
  list(teacherId) {
    return request.get('/api/evaluation/list', {
      params: teacherId ? { teacherId } : {}
    }).then((res) => res.data)
  },

  /** 获取报告详情 */
  getById(id) {
    return request.get(`/api/evaluation/${id}`).then((res) => res.data)
  },

  /** 创建评价报告 */
  create(data) {
    return request.post('/api/evaluation/create', data).then((res) => res.data)
  },

  /** 更新满意度 */
  update(id, data) {
    return request.put(`/api/evaluation/${id}`, data)
  },

  /** 删除报告 */
  delete(id) {
    return request.delete(`/api/evaluation/${id}`)
  },

  /** AI 生成诊断报告 */
  generateAiReport(id) {
    return request.post(`/api/evaluation/${id}/ai-report`).then((res) => res.data)
  }
}

export default evaluationApi
