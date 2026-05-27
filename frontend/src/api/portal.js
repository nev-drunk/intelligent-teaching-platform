import request from './request'

export function fetchCarousel() {
  return request.get('/portal/carousel')
}

export function fetchNotices() {
  return request.get('/portal/notices')
}

export function publishNotice(data) {
  return request.post('/portal/notices', data)
}

export function deleteNotice(id) {
  return request.delete(`/portal/notices/${id}`)
}
