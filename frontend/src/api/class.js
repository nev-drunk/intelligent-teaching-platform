import request from './request'

export function fetchClasses(teacherId) {
  return request.get('/api/classes', { params: teacherId ? { teacherId } : {} })
}

export function fetchClass(id) {
  return request.get(`/api/classes/${id}`)
}

export function createClass(classData) {
  return request.post('/api/classes', classData)
}

export function updateClass(id, classData) {
  return request.put(`/api/classes/${id}`, classData)
}

export function deleteClass(id) {
  return request.delete(`/api/classes/${id}`)
}

export function listClasses(teacherId) {
  return fetchClasses(teacherId).then((res) => res.data)
}
