import request from './request'

/**
 * 课程管理 API — 对接后端 /api/courses
 * 数据表：tb_course
 */
const courseApi = {
  /** 获取课程列表（可按教师筛选） */
  list(teacherId) {
    return request
      .get('/api/courses', {
        params: teacherId ? { teacherId } : {}
      })
      .then((res) => res.data)
  },

  /** 获取课程详情 */
  getById(id) {
    return request.get(`/api/courses/${id}`).then((res) => res.data)
  },

  /** 创建课程 */
  create(data) {
    return request.post('/api/courses', data).then((res) => res.data)
  },

  /** 更新课程 */
  update(id, data) {
    return request.put(`/api/courses/${id}`, data).then((res) => res.data)
  },

  /** 删除课程 */
  delete(id) {
    return request.delete(`/api/courses/${id}`)
  }
}

export default courseApi
