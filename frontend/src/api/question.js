import request from './request'

export function listQuestions(courseId) {
  const params = courseId ? { courseId } : {}
  return request.get('/api/questions', { params }).then((res) => res.data)
}

export function getQuestion(id) {
  return request.get(`/api/questions/${id}`).then((res) => res.data)
}

export function saveQuestion(question) {
  return request.post('/api/questions', question).then((res) => res.data)
}

export function deleteQuestion(id) {
  return request.delete(`/api/questions/${id}`)
}

export function aiGenerate({ prompt, count = 3, courseId = 1, keyword }) {
  const body = prompt
    ? { prompt, count, courseId }
    : { courseId, keyword }
  return request.post('/api/questions/ai-generate', body).then((res) => res.data)
}
