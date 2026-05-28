import request from './request'

export function fetchStudentList(params) {
  return request.get('/api/student/list', { params })
}

export function fetchStudents(classId) {
  return request.get('/api/student', { params: { classId } })
}

export function fetchStudent(id) {
  return request.get(`/api/student/${id}`)
}

export function createStudent(student) {
  return request.post('/api/student/add', student)
}

export function updateStudent(student) {
  return request.put('/api/student/update', student)
}

export function deleteStudent(id) {
  return request.delete(`/api/student/delete/${id}`)
}
