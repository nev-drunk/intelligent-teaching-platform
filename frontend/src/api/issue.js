import request from './request'

/**
 * 问题中心答疑 API — 对接后端 /api/issue
 * 数据表：tb_issue_center
 */
const issueApi = {
  /** 获取问题列表（可按课程筛选） */
  list(courseId) {
    return request.get('/api/issue/list', {
      params: courseId ? { courseId } : {}
    }).then((res) => res.data)
  },

  /** 获取问题详情 */
  getById(id) {
    return request.get(`/api/issue/${id}`).then((res) => res.data)
  },

  /** 学生发布问题 */
  create(data) {
    return request.post('/api/issue/create', data).then((res) => res.data)
  },

  /** 发布问题并返回 Top3 相似历史问题 */
  createWithSimilarity(data) {
    return request.post('/api/issue/create-with-similarity', data).then((res) => res.data)
  },

  /** 教师回复问题 */
  reply(id, teacherReply) {
    return request.put(`/api/issue/${id}/reply`, { teacherReply })
  },

  /** 删除问题 */
  delete(id) {
    return request.delete(`/api/issue/${id}`)
  }
}

export default issueApi
