import api from './index.js'

/** 获取攻略列表（可按分类筛选） */
export function getGuides(category) {
  return api.get('/guides', { params: category ? { category } : {} })
}

/** 获取攻略详情 */
export function getGuideDetail(id) {
  return api.get(`/guides/${id}`)
}

/** 创建攻略（管理员） */
export function createGuide(data) {
  return api.post('/admin/guides', data)
}

/** 更新攻略（管理员） */
export function updateGuide(id, data) {
  return api.put(`/admin/guides/${id}`, data)
}

/** 删除攻略（管理员） */
export function deleteGuide(id) {
  return api.delete(`/admin/guides/${id}`)
}
