import api from './index.js'

/** 用户管理 */
export function getUsers(params) {
  return api.get('/admin/users', { params })
}

export function updateUserStatus(userId, status) {
  return api.put(`/admin/users/${userId}/status`, null, { params: { status } })
}

export function assignUserRole(userId, roleId) {
  return api.put(`/admin/users/${userId}/role`, null, { params: { roleId } })
}

export function resetUserPassword(userId, newPassword) {
  return api.post(`/admin/users/${userId}/reset-password`, { newPassword })
}

export function deleteUser(userId) {
  return api.delete(`/admin/users/${userId}`)
}

/** 角色管理 */
export function getRoles() {
  return api.get('/admin/roles')
}

export function createRole(data) {
  return api.post('/admin/roles', data)
}

export function updateRole(id, data) {
  return api.put(`/admin/roles/${id}`, data)
}

export function deleteRole(id) {
  return api.delete(`/admin/roles/${id}`)
}

export function getRolePermissions(roleId) {
  return api.get(`/admin/roles/${roleId}/permissions`)
}

export function assignRolePermissions(roleId, permissionIds) {
  return api.put(`/admin/roles/${roleId}/permissions`, { permissionIds })
}

/** 权限管理 */
export function getPermissions() {
  return api.get('/admin/permissions')
}

export function createPermission(data) {
  return api.post('/admin/permissions', data)
}

export function updatePermission(id, data) {
  return api.put(`/admin/permissions/${id}`, data)
}

export function deletePermission(id) {
  return api.delete(`/admin/permissions/${id}`)
}
