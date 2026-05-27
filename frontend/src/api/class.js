import request from './request'

export function fetchClasses(teacherId) {
  return request.get('/classes', { params: teacherId ? { teacherId } : {} })
}
