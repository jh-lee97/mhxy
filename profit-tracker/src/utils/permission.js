import { getPermissions } from './auth.js'

/** 权限指令 v-permission */
export function setupPermissionDirective(app) {
  app.directive('permission', {
    mounted(el, binding) {
      const permissions = getPermissions()
      const code = binding.value
      
      // 如果是数组，检查是否有任何一个权限
      if (Array.isArray(code)) {
        const hasAny = code.some(c => permissions.includes(c))
        if (!hasAny) {
          el.parentNode?.removeChild(el)
        }
        return
      }
      
      // 单个权限检查
      if (!permissions.includes(code)) {
        el.parentNode?.removeChild(el)
      }
    }
  })
}

/** 权限检查函数（供 JS 代码调用） */
export function hasPermission(code) {
  const permissions = getPermissions()
  return permissions.includes(code)
}

/** 检查是否有任一权限 */
export function hasAnyPermission(codes) {
  const permissions = getPermissions()
  return codes.some(c => permissions.includes(c))
}

/** 检查是否有所有权限 */
export function hasAllPermissions(codes) {
  const permissions = getPermissions()
  return codes.every(c => permissions.includes(c))
}
