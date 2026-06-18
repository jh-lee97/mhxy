/** 从 localStorage 获取 token */
export function getToken() {
  return localStorage.getItem('token') || null
}

/** 保存 token 到 localStorage */
export function saveToken(token) {
  localStorage.setItem('token', token)
}

/** 清除 token */
export function clearToken() {
  localStorage.removeItem('token')
}
