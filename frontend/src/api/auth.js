import request from './request'

/**
 * 登录接口
 * @param {string} username - 用户名
 * @param {string} password - 密码
 * @param {boolean} rememberMe - 是否记住我
 */
export function login(username, password, rememberMe = false) {
  return request.post('/api/auth/login', { username, password, rememberMe })
}

/**
 * 注册接口
 * @param {object} data - 注册数据
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @param {string} data.name - 姓名
 * @param {string} [data.phone] - 手机号（可选）
 */
export function register(data) {
  return request.post('/api/auth/register', data)
}

/**
 * 获取当前用户信息
 */
export function fetchMe() {
  return request.get('/api/auth/me')
}

/**
 * 退出登录
 */
export function logout() {
  return request.post('/api/auth/logout')
}
