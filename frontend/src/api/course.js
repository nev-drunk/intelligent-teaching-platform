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
  },

  // ==================== 2. 课程资源管理 API ====================

  /** 更新资源信息（保存分割结果） */
  updateResource(data) {
    return request.put('/api/courses/resources', data).then((res) => res.data)
  },

  /** 获取指定课程的资源列表 */
  getResourceList(courseId) {
    return request.get(`/api/courses/${courseId}/resources`).then((res) => res.data)
  },

  /** * 上传课程资源文件
   * @param {File} file - 浏览器选择的文件对象
   * @param {Number|String} courseId - 课程ID
   * @param {String} title - 资源名称（如：思维导图）
   */
  uploadResource(file, courseId, title) {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('courseId', courseId)
    formData.append('title', title)

    return request
      .post('/api/courses/resources/upload', formData, {
        headers: {
          // 声明这是文件上传请求
          'Content-Type': 'multipart/form-data'
        }
      })
      .then((res) => res.data)
  },

  /** 删除资源 */
  deleteResource(id) {
    return request.delete(`/api/courses/resources/${id}`)
  }
}

export default courseApi
