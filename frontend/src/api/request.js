import axios from 'axios'

const request = axios.create({
  baseURL: '',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  },
  responseType: 'json',
  responseEncoding: 'utf8'
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    // JWT Token 添加 Bearer 前缀
    config.headers.Authorization = 'Bearer ' + token
  }
  config.headers['Accept'] = 'application/json;charset=UTF-8'
  return config
})

request.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body.code === 'number' && body.code !== 200) {
      return Promise.reject(new Error(body.msg || '请求失败'))
    }
    return body
  },
  (err) => {
    const msg = err.response?.data?.msg || err.message || '网络错误'
    return Promise.reject(new Error(msg))
  }
)

export default request
