import request from './request'

export function fetchCarousel() {
  return request.get('/api/portal/carousel')
}

export function fetchNotices() {
  return request.get('/api/portal/notices')
}

export function publishNotice(data) {
  return request.post('/api/portal/notices', data)
}

export function deleteNotice(id) {
  return request.delete(`/api/portal/notices/${id}`)
}
