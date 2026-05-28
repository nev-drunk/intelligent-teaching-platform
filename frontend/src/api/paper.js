import request from './request'

export function listPapers() {
  return request.get('/api/papers').then((res) => res.data)
}

export function listPapersByCourse(courseId) {
  return request.get(`/api/papers/course/${courseId}`).then((res) => res.data)
}

export function getPaper(id) {
  return request.get(`/api/papers/${id}`).then((res) => res.data)
}

export function savePaper(paper) {
  return request.post('/api/papers', paper).then((res) => res.data)
}

export function createAndPublishPaper(payload) {
  return request.post('/api/papers/create-paper', payload).then((res) => res)
}

export function publishPaper(payload) {
  return request.post('/api/papers/publish', payload).then((res) => res.data)
}

export function deletePaper(id) {
  return request.delete(`/api/papers/${id}`)
}

export function listPapersByTeacher(teacherId, courseId) {
  return request
    .get(`/api/papers/teachers/${teacherId}/courses/${courseId}`)
    .then((res) => res.data)
}
