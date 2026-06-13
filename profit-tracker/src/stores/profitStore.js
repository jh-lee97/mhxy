import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
})

const DEFAULT_USER_ID = 1

/** 金额格式化：小于1万显示原值，大于等于1万显示"万" */
function formatMoney(val) {
  const num = Number(val) || 0
  if (num === 0) return '0.00'
  if (Math.abs(num) < 10000) return num.toFixed(2)
  return (num / 10000).toFixed(2) + '万'
}

export function useProfitStore() {
  function getRecords() {
    return api.get('/records', { params: { userId: DEFAULT_USER_ID } })
      .then(res => res.data.code === 200 ? res.data.data : [])
      .catch(() => [])
  }

  function addRecord(record) {
    return api.post('/records', { ...record, userId: DEFAULT_USER_ID })
      .then(res => res.data.code === 200 ? res.data.data : null)
      .catch(err => { console.error('addRecord error:', err); return null })
  }

  function deleteRecord(id) {
    return api.delete('/records/' + id, { params: { userId: DEFAULT_USER_ID } })
      .then(res => res.data.code === 200)
      .catch(() => false)
  }

  function getStats() {
    return api.get('/records/stats', { params: { userId: DEFAULT_USER_ID } })
      .then(res => res.data.code === 200 ? res.data.data : null)
      .catch(() => null)
  }

  function getChartRecords() {
    return api.get('/records/chart', { params: { userId: DEFAULT_USER_ID } })
      .then(res => res.data.code === 200 ? res.data.data : [])
      .catch(() => [])
  }

  return {
    getRecords, addRecord, deleteRecord, getStats, getChartRecords,
    formatMoney
  }
}
