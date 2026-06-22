import api from '../api/index.js'
import { getToken, saveToken, clearToken } from '../utils/auth.js'
import { formatMoney } from '../utils/format.js'

const DEFAULT_USER_ID = 1

export function useProfitStore() {
  let token = getToken()
  let username = localStorage.getItem('username') || ''
  let userId = localStorage.getItem('userId') ? parseInt(localStorage.getItem('userId')) : null
  let permissions = JSON.parse(localStorage.getItem('permissions') || '[]')
  let roles = JSON.parse(localStorage.getItem('roles') || '[]')
  let menus = JSON.parse(localStorage.getItem('menus') || '[]')

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
  function login(usernameVal, password) {
    return api.post('/auth/login', { username: usernameVal, password })
      .then(res => {
        if (res.data.code === 200) {
          const { token: t, username: u, userId: uid } = res.data.data
          saveToken(t)
          localStorage.setItem('username', u)
          localStorage.setItem('userId', uid || '')
          token = t
          username = u
          userId = uid

          // 新：登录后加载权限
          return loadPermissions().then(() => true)
        }
        throw new Error(res.data.msg || '登录失败')
      })
  }

  /** 手机号+验证码登录 */
  function phoneLogin(phone, code) {
    return api.post('/auth/phone-login', { phone, code })
      .then(res => {
        if (res.data.code === 200) {
          const { token: t, username: u, userId: uid } = res.data.data
          saveToken(t)
          localStorage.setItem('username', u)
          localStorage.setItem('userId', uid || '')
          token = t
          username = u
          userId = uid

          // 新：登录后加载权限
          return loadPermissions().then(() => true)
        }
        throw new Error(res.data.msg || '登录失败')
      })
  }

  /** 注册 */
  function register(usernameVal, password, nickname, phone, code) {
    return api.post('/auth/register', { username: usernameVal, password, nickname, phone, code })
      .then(res => {
        if (res.data.code === 200) {
          const { token: t, userId: uid } = res.data.data
          saveToken(t)
          localStorage.setItem('userId', uid || '')
          token = t
          userId = uid

          // 新：登录后加载权限
          return loadPermissions().then(() => true)
        }
        throw new Error(res.data.msg || '注册失败')
      })
  }

  /** 加载用户权限和菜单 */
  function loadPermissions() {
    return api.get('/auth/permissions')
      .then(res => {
        if (res.data.code === 200) {
          const data = res.data.data
          permissions = data.permissions || []
          roles = data.roles || []
          menus = data.menus || []
          
          localStorage.setItem('permissions', JSON.stringify(permissions))
          localStorage.setItem('roles', JSON.stringify(roles))
          localStorage.setItem('menus', JSON.stringify(menus))
          return true
        }
        return false
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

  /** 发送重置密码验证码（只需手机号） */
  function sendResetCode(phone) {
    return api.post('/auth/send-reset-code', { phone })
      .then(res => {
        if (res.data.code === 200) {
          return true
        }
        throw new Error(res.data.msg || '发送验证码失败')
      })
  }

  /** 发送登录验证码（手机号登录专用） */
  function sendLoginCode(phone) {
    return api.post('/auth/send-login-code', { phone })
      .then(res => {
        if (res.data.code === 200) {
          return true
        }
        throw new Error(res.data.msg || '发送验证码失败')
      })
  }

  /** 发送验证码 */
  function sendVerificationCode(usernameVal, phone) {
    return api.post('/auth/send-code', { username: usernameVal, phone })
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
    userId = null
    permissions = []
    roles = []
    menus = []
    localStorage.removeItem('username')
    localStorage.removeItem('userId')
    localStorage.removeItem('permissions')
    localStorage.removeItem('roles')
    localStorage.removeItem('menus')
  }

  /** 检查是否有指定权限 */
  function hasPermission(code) {
    return permissions.includes(code)
  }

  /** 检查是否有指定角色 */
  function hasRole(role) {
    return roles.includes(role)
  }

  /** 是否是管理员 */
  function isAdmin() {
    return roles.includes('ADMIN') || permissions.includes('user:manage')
  }

  return {
    get token() { return token },
    get username() { return username },
    get userId() { return userId },
    get permissions() { return permissions },
    get roles() { return roles },
    get menus() { return menus },
    hasPermission,
    hasRole,
    isAdmin,
    getRecords, addRecord, deleteRecord, getStats, getChartRecords,
    formatMoney, login, phoneLogin, register, sendVerificationCode, sendLoginCode, sendResetCode, sendRegisterCode, resetPasswordWithCode, logout,
    loadPermissions
  }
}
