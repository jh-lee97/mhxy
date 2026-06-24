import api from './index.js'

/** 获取活跃计划（今日任务） */
export function getActivePlan() {
  return api.get('/tasks/active')
}

/** 获取计划详情 */
export function getPlanDetail(planId) {
  return api.get(`/tasks/detail/${planId}`)
}

/** 获取所有周计划 */
export function getWeeklyPlans() {
  return api.get('/tasks/weekly')
}

/** 创建新计划 */
export function createPlan(data) {
  return api.post('/tasks/plan', data)
}

/** 更新计划 */
export function updatePlan(data) {
  return api.put('/tasks/plan', data)
}

/** 删除计划 */
export function deletePlan(planId) {
  return api.delete(`/tasks/plan/${planId}`)
}

/** 切换计划状态 */
export function togglePlanStatus(planId) {
  return api.patch(`/tasks/plan/${planId}/toggle`)
}

/** 完成单个任务 */
export function completeTask(data) {
  return api.post('/tasks/completion', data)
}

/** 批量完成某天所有任务 */
export function batchCompleteDay(planId, date, status, remark) {
  return api.post('/tasks/batch-complete', null, {
    params: { planId, date, status, remark }
  })
}

/** 获取某天任务完成情况 */
export function getDayCompletions(planId, date) {
  return api.get(`/tasks/day/${date}`, { params: { planId } })
}

/** 获取每日统计 */
export function getDailyStats(planId) {
  return api.get(`/tasks/stats/${planId}`)
}

/** 获取今日统计 */
export function getTodayStats() {
  return api.get('/tasks/stats/today')
}
