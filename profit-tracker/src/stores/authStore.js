import { reactive, readonly } from 'vue'
import { getToken, saveToken, clearToken } from '../utils/auth.js'

const state = reactive({
  token: getToken() || null,
  username: localStorage.getItem('username') || '',
  userId: localStorage.getItem('userId') ? parseInt(localStorage.getItem('userId')) : null,
  permissions: JSON.parse(localStorage.getItem('permissions') || '[]'),
  roles: JSON.parse(localStorage.getItem('roles') || '[]'),
  menus: JSON.parse(localStorage.getItem('menus') || '[]')
})

export function useAuthStore() {
  /** 保存 token */
  function setToken(tokenVal) {
    saveToken(tokenVal)
    state.token = tokenVal
  }

  /** 保存用户名 */
  function setUsername(usernameVal) {
    state.username = usernameVal
    localStorage.setItem('username', usernameVal)
  }

  /** 保存用户 ID */
  function setUserId(userIdVal) {
    state.userId = userIdVal
    localStorage.setItem('userId', userIdVal)
  }

  /** 加载用户权限和菜单 */
  function setAuthData(permissions, roles, menus) {
    state.permissions = permissions
    state.roles = roles
    state.menus = menus
    localStorage.setItem('permissions', JSON.stringify(permissions))
    localStorage.setItem('roles', JSON.stringify(roles))
    localStorage.setItem('menus', JSON.stringify(menus))
  }

  /** 登出：清除所有状态 */
  function clearAuth() {
    clearToken()
    state.token = null
    state.username = ''
    state.userId = null
    state.permissions = []
    state.roles = []
    state.menus = []
  }

  /** 检查是否有指定权限 */
  function hasPermission(code) {
    return state.permissions.includes(code)
  }

  /** 检查是否有指定角色 */
  function hasRole(role) {
    return state.roles.includes(role)
  }

  /** 是否是管理员 */
  function isAdmin() {
    return state.roles.includes('ADMIN') || state.permissions.includes('user:manage')
  }

  return {
    ...readonly(state),
    setToken,
    setUsername,
    setUserId,
    setAuthData,
    clearAuth,
    hasPermission,
    hasRole,
    isAdmin
  }
}
