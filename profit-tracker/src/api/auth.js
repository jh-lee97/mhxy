import api from './index.js'

/** 用户名密码登录 */
export function login(data) {
  return api.post('/auth/login', data)
}

/** 手机号验证码登录 */
export function phoneLogin(data) {
  return api.post('/auth/phone-login', data)
}

/** 用户注册 */
export function register(data) {
  return api.post('/auth/register', data)
}

/** 发送登录验证码 */
export function sendCode(data) {
  return api.post('/auth/send-code', data)
}

/** 发送登录验证码（仅需手机号） */
export function sendLoginCode(data) {
  return api.post('/auth/send-login-code', data)
}

/** 发送注册验证码 */
export function sendRegisterCode(data) {
  return api.post('/auth/send-register-code', data)
}

/** 发送重置密码验证码 */
export function sendResetCode(data) {
  return api.post('/auth/send-reset-code', data)
}

/** 重置密码 */
export function resetPassword(data) {
  return api.post('/auth/reset-password', data)
}

/** 获取当前用户信息 */
export function getCurrentUser() {
  return api.get('/auth/me')
}

/** 修改密码 */
export function changePassword(data) {
  return api.put('/auth/change-password', data)
}

/** 获取当前用户权限和菜单 */
export function getUserPermissions() {
  return api.get('/auth/permissions')
}
