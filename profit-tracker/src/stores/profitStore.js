import api from '../api/index.js'
import { getToken, saveToken, clearToken } from '../utils/auth.js'
import { formatMoney } from '../utils/format.js'

const DEFAULT_USER_ID = 1

export function useProfitStore() {
  let token = getToken()
  let username = localStorage.getItem('username') || ''
  let roleLevel = parseInt(localStorage.getItem('roleLevel') || '1')
  let userId = localStorage.getItem('userId') ? parseInt(localStorage.getItem('userId')) : null

  function getRecords() {
    return api.get('/records')
      .then(res => res.data.code === 200 ? res.data.data : [])
      .catch(() => [])
  }

  function addRecord(record) {
    return api.post('/records', record)
      .then(res => res.data.code === 200 ? res.data.data : null)
      .catch(err => { console.error('addRecord error:', err); return null })
  }

  function deleteRecord(id) {
    return api.delete('/records/' + id)
      .then(res => res.data.code === 200)
      .catch(() => false)
  }

  function getStats() {
    return api.get('/records/stats')
      .then(res => res.data.code === 200 ? res.data.data : null)
      .catch(() => null)
  }

  function getChartRecords() {
    return api.get('/records/chart')
      .then(res => res.data.code === 200 ? res.data.data : [])
      .catch(() => [])
  }

  /** 登录 */
  function login(username, password) {
    return api.post('/auth/login', { username, password })
      .then(res => {
        if (res.data.code === 200) {
          const { token: t, username: u, userId: uid, roleLevel: rl, roleName: rn } = res.data.data
          saveToken(t)
          localStorage.setItem('username', u)
          localStorage.setItem('userId', uid || '')
          localStorage.setItem('roleLevel', rl || 1)
          localStorage.setItem('roleName', rn || '')
          token = t
          username = u
          roleLevel = rl || 1
          return true
        }
        throw new Error(res.data.msg || '登录失败')
      })
  }

  /** 注册 */
  function register(username, password, nickname, phone, code) {
    return api.post('/auth/register', { username, password, nickname, phone, code })
      .then(res => {
        if (res.data.code === 200) {
          const { token: t, userId: uid, roleLevel: rl, roleName: rn } = res.data.data
          saveToken(t)
          localStorage.setItem('userId', uid || '')
          localStorage.setItem('roleLevel', rl || 1)
          localStorage.setItem('roleName', rn || '')
          token = t
          roleLevel = rl || 1
          return true
        }
        throw new Error(res.data.msg || '注册失败')
      })
  }

  /** 发送注册验证码 */
  function sendRegisterCode(phone) {
    return api.post('/auth/send-register-code', { phone })
      .then(res => {
        if (res.data.code === 200) {
          return true
        }
        throw new Error(res.data.msg || '发送验证码失败')
      })
  }

  /** 发送验证码 */
  function sendVerificationCode(username, phone) {
    return api.post('/auth/send-code', { username, phone })
      .then(res => {
        if (res.data.code === 200) {
          return true
        }
        throw new Error(res.data.msg || '发送验证码失败')
      })
  }

  /** 通过手机号+验证码重置密码 */
  function resetPasswordWithCode(phone, code, newPassword) {
    return api.post('/auth/reset-password', { phone, code, newPassword })
      .then(res => {
        if (res.data.code === 200) {
          return true
        }
        throw new Error(res.data.msg || '重置密码失败')
      })
  }

  /** 登出 */
  function logout() {
    clearToken()
    token = null
    username = ''
    roleLevel = 1
    userId = null
    localStorage.removeItem('username')
    localStorage.removeItem('userId')
    localStorage.removeItem('roleLevel')
    localStorage.removeItem('roleName')
  }

  /** 获取当前用户角色等级 */
  function getRoleLevel() {
    return roleLevel
  }

  /** 获取当前用户角色名称 */
  function getRoleName() {
    return localStorage.getItem('roleName') || ''
  }

  /** 是否是管理员 */
  function isAdmin() {
    return roleLevel >= 100
  }

  return {
    get token() { return token },
    get username() { return username },
    get userId() { return userId },
    get roleLevel() { return roleLevel },
    get roleName() { return getRoleName() },
    isAdmin,
    getRecords, addRecord, deleteRecord, getStats, getChartRecords,
    formatMoney, login, register, sendVerificationCode, sendRegisterCode, resetPasswordWithCode, logout,
    getRoleLevel, getRoleName
  }
}
