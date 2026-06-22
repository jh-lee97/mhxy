import api from './index.js'

/** 获取收益记录列表 */
export function getRecords() {
  return api.get('/records')
}

/** 新增收益记录 */
export function addRecord(data) {
  return api.post('/records', data)
}

/** 更新收益记录 */
export function updateRecord(data) {
  return api.put('/records', data)
}

/** 删除收益记录 */
export function deleteRecord(id) {
  return api.delete(`/records/${id}`)
}

/** 获取统计信息 */
export function getStats() {
  return api.get('/records/stats')
}

/** 获取图表数据（近7日） */
export function getChartRecords() {
  return api.get('/records/chart')
}
