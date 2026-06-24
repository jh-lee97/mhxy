<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getActivePlan, completeTask, batchCompleteDay, getTodayStats } from '../api/task.js'

const loading = ref(false)
const plan = ref(null)
const todayStats = ref(null)
const dayCompletions = ref({})
const expandState = ref({})
const skipRemark = ref('')

const DAYS_CN = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']

// 当前星期几 (1-7)
const todayDayOfWeek = new Date().getDay() === 0 ? 7 : new Date().getDay()
const todayStr = new Date().toISOString().split('T')[0]

// 获取某天的所有任务（扁平列表）
function getDayTasks(dayOfWeek) {
  if (!plan.value || !plan.value.dayTasks) return []
  return plan.value.dayTasks.filter(t => t.dayOfWeek === dayOfWeek)
}

// 获取今日任务
const todayTasks = computed(() => {
  return getDayTasks(todayDayOfWeek)
})

const isToday = computed(() => {
  return plan.value && plan.value.startDate <= todayStr && plan.value.endDate >= todayStr
})

async function loadActivePlan() {
  loading.value = true
  try {
    const res = await getActivePlan()
    if (res.data?.code === 200 && res.data.data) {
      plan.value = res.data.data
      // 加载今天的任务完成情况
      await loadTodayCompletions()
    }
  } catch (e) {
    console.error('加载任务计划失败:', e)
  } finally {
    loading.value = false
  }
}

async function loadTodayCompletions() {
  if (!plan.value) return
  try {
    const res = await getTodayStats()
    if (res.data?.code === 200) {
      todayStats.value = res.data.data
    }
  } catch (e) {
    console.error('加载今日统计失败:', e)
  }
}

function toggleExpand(dayOfWeek) {
  expandState.value[dayOfWeek] = !expandState.value[dayOfWeek]
}

async function handleComplete(task, status, remark = '') {
  if (!plan.value) return
  try {
    const res = await completeTask({
      checklistId: task.id,
      completeDate: todayStr,
      status: status,
      remark: remark
    })
    if (res.data?.code === 200) {
      await loadTodayCompletions()
      ElMessage.success(status === 1 ? '任务已完成 ✅' : '任务已跳过')
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '操作失败')
  }
}

async function handleBatchComplete(status, remark = '') {
  if (!plan.value) return
  try {
    await batchCompleteDay(plan.value.id, todayStr, status, remark)
    await loadTodayCompletions()
    ElMessage.success(status === 1 ? '全部任务已完成 ✅' : '全部任务已跳过')
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '操作失败')
  }
}

onMounted(() => {
  loadActivePlan()
})
</script>

<template>
  <div class="task-checklist" v-loading="loading">
    <!-- 无计划时 -->
    <div class="empty-state" v-if="!plan || !plan.planName">
      <div class="empty-icon">📋</div>
      <p>当前没有活跃的任务计划</p>
      <p class="empty-hint">请先在管理后台创建本周任务计划</p>
    </div>

    <!-- 有计划时 -->
    <div v-else>
      <div class="plan-header">
        <div class="plan-info">
          <h3>📅 {{ plan.planName }}</h3>
          <span class="plan-dates">{{ plan.startDate }} ~ {{ plan.endDate }}</span>
        </div>
        <div class="plan-actions">
          <el-button size="small" type="success" @click="handleBatchComplete(1)">
            ✅ 全部完成
          </el-button>
        </div>
      </div>

      <!-- 今日统计 -->
      <div class="today-summary" v-if="todayStats">
        <div class="summary-item">
          <span class="label">今日进度</span>
          <span class="value">{{ todayStats.completedTasks }}/{{ todayStats.totalTasks }}</span>
        </div>
        <div class="summary-item">
          <span class="label">完成率</span>
          <el-progress
            :percentage="Math.round(todayStats.completionRate || 0)"
            :stroke-width="12"
            :show-text="true"
          />
        </div>
        <div class="summary-item">
          <span class="label">预估收入</span>
          <span class="value income">{{ todayStats.estimatedIncome || 0 }} 万</span>
        </div>
        <div class="summary-item">
          <span class="label">实际收入</span>
          <span class="value actual">{{ todayStats.actualIncome || 0 }} 万</span>
        </div>
      </div>

      <!-- 每日任务列表 -->
      <div class="day-list">
        <div
          v-for="day in [todayDayOfWeek, ...Array.from({length: 7}, (_, i) => i + 1).filter(d => d !== todayDayOfWeek)]"
          :key="day"
          class="day-card"
          :class="{ 'today': day === todayDayOfWeek }"
        >
          <div class="day-header" @click="toggleExpand(day)">
            <div class="day-title">
              <span class="day-name">{{ DAYS_CN[day] }}</span>
              <span v-if="day === todayDayOfWeek" class="today-badge">今天</span>
              <span class="task-count">
                {{ getDayTasks(day).length }} 个任务
              </span>
            </div>
            <div class="day-right">
              <el-icon v-if="getDayTasks(day).length > 0" :size="16">
                <component :is="expandState[day] ? 'ArrowUp' : 'ArrowDown'" />
              </el-icon>
            </div>
          </div>

          <!-- 任务详情 -->
          <div class="day-tasks" v-show="expandState[day] || day === todayDayOfWeek">
            <div
              v-for="task in getDayTasks(day)"
              :key="task.id"
              class="task-item"
              :class="{ 'mandatory': task.mandatory === 1 }"
            >
              <div class="task-main">
                <span class="task-name">{{ task.taskName }}</span>
                <span v-if="task.category" class="task-category">{{ task.category }}</span>
                <span v-if="task.mandatory === 1" class="task-required">必做</span>
                <span v-if="task.description" class="task-desc">{{ task.description }}</span>
              </div>
              <div class="task-meta">
                <span v-if="task.estimatedIncome" class="meta-item income">
                  💰 {{ task.estimatedIncome }}万
                </span>
                <span v-if="task.estimatedTime" class="meta-item time">
                  ⏱ {{ task.estimatedTime }}h
                </span>
              </div>
              <div class="task-actions" v-if="day === todayDayOfWeek">
                <el-button size="small" type="success" @click.stop="handleComplete(task, 1)">
                  ✅ 完成
                </el-button>
                <el-button size="small" @click.stop="handleComplete(task, 2)">
                  ⏭ 跳过
                </el-button>
              </div>
            </div>
            <div v-if="getDayTasks(day).length === 0" class="no-tasks">
              暂无任务
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.task-checklist {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-hint {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 8px;
}

.plan-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.plan-info h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.plan-dates {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.today-summary {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  padding: 16px;
  background: linear-gradient(135deg, #f6f8fc 0%, #e8f0fe 100%);
  border-radius: 12px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.summary-item .label {
  font-size: 12px;
  color: #909399;
}

.summary-item .value {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}

.summary-item .value.income {
  color: #67c23a;
}

.summary-item .value.actual {
  color: #409eff;
}

.day-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.day-card {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: box-shadow 0.2s;
}

.day-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.day-card.today {
  border: 2px solid #409eff;
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  user-select: none;
}

.day-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.day-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.today-badge {
  background: #409eff;
  color: #fff;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
}

.task-count {
  font-size: 12px;
  color: #909399;
}

.day-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.day-tasks {
  padding: 0 16px 12px;
  border-top: 1px solid #f0f0f0;
}

.task-item {
  padding: 10px 0;
  border-bottom: 1px solid #fafafa;
}

.task-item:last-child {
  border-bottom: none;
}

.task-item.mandatory {
  background: #fffbe6;
  padding: 10px 8px;
  border-radius: 6px;
  margin: 4px 0;
}

.task-main {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.task-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.task-category {
  font-size: 11px;
  color: #909399;
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
}

.task-required {
  font-size: 11px;
  color: #f56c6c;
  background: #fef0f0;
  padding: 2px 6px;
  border-radius: 3px;
}

.task-desc {
  font-size: 12px;
  color: #606266;
}

.task-meta {
  display: flex;
  gap: 12px;
  margin-top: 4px;
}

.meta-item {
  font-size: 12px;
}

.meta-item.income {
  color: #67c23a;
}

.meta-item.time {
  color: #e6a23c;
}

.task-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.no-tasks {
  text-align: center;
  padding: 16px;
  color: #c0c4cc;
  font-size: 13px;
}
</style>
