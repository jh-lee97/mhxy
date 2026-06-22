/** 从 localStorage 获取 token */
export function getToken() {
  return localStorage.getItem('token') || null
}

/** 保存 token 到 localStorage */
export function saveToken(token) {
  localStorage.setItem('token', token)
}

/** 清除 token 和相关数据 */
export function clearToken() {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('userId')
  localStorage.removeItem('permissions')
  localStorage.removeItem('roles')
  localStorage.removeItem('menus')
}

/** 获取用户权限列表 */
export function getPermissions() {
  try {
    return JSON.parse(localStorage.getItem('permissions') || '[]')
  } catch {
    return []
  }
}

/** 获取用户角色列表 */
export function getRoles() {
  try {
    return JSON.parse(localStorage.getItem('roles') || '[]')
  } catch {
    return []
  }
}
