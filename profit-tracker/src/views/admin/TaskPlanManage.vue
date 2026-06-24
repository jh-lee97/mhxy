<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getWeeklyPlans,
  createPlan,
  updatePlan,
  deletePlan,
  togglePlanStatus
} from '../../api/task.js'

const loading = ref(false)
const plans = ref([])
const dialogVisible = ref(false)
const submitting = ref(false)

const emptyForm = {
  id: null,
  planName: '',
  year: new Date().getFullYear(),
  weekNumber: getCurrentWeek(),
  startDate: getMondayOfCurrentWeek(),
  endDate: getSundayOfCurrentWeek(),
  status: 1,
  dayTasks: getDefaultDayTasks()
}

const form = ref({ ...emptyForm })
const isEdit = ref(false)

const categories = ['日常', '副本', '活动', '其他']

const DAY_LABELS = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']

function getCurrentWeek() {
  const now = new Date()
  const oneJan = new Date(now.getFullYear(), 0, 1)
  return Math.ceil(((now - oneJan) / 86400000 + oneJan.getDay() + 1) / 7)
}

function getMondayOfCurrentWeek() {
  const now = new Date()
  const day = now.getDay() || 7
  const monday = new Date(now)
  monday.setDate(now.getDate() - day + 1)
  return monday.toISOString().split('T')[0]
}

function getSundayOfCurrentWeek() {
  const now = new Date()
  const day = now.getDay() || 7
  const sunday = new Date(now)
  sunday.setDate(now.getDate() - day + 7)
  return sunday.toISOString().split('T')[0]
}

function getDefaultDayTasks() {
  // 默认每周任务模板
  const defaults = {
    1: [ // 周一
      { dayOfWeek: 1, taskName: '师门任务', category: '日常', estimatedIncome: 15, estimatedTime: 0.5, description: '每天20次', sortOrder: 1, mandatory: 1 },
      { dayOfWeek: 1, taskName: '捉鬼任务', category: '日常', estimatedIncome: 30, estimatedTime: 1, description: '', sortOrder: 2, mandatory: 1 },
      { dayOfWeek: 1, taskName: '宝图任务', category: '日常', estimatedIncome: 20, estimatedTime: 1, description: '', sortOrder: 3, mandatory: 0 }
    ],
    2: [ // 周二
      { dayOfWeek: 2, taskName: '师门任务', category: '日常', estimatedIncome: 15, estimatedTime: 0.5, description: '', sortOrder: 1, mandatory: 1 },
      { dayOfWeek: 2, taskName: '捉鬼任务', category: '日常', estimatedIncome: 30, estimatedTime: 1, description: '', sortOrder: 2, mandatory: 1 },
      { dayOfWeek: 2, taskName: '秘境寻宝', category: '副本', estimatedIncome: 25, estimatedTime: 0.5, description: '每日3次', sortOrder: 3, mandatory: 1 }
    ],
    3: [ // 周三
      { dayOfWeek: 3, taskName: '师门任务', category: '日常', estimatedIncome: 15, estimatedTime: 0.5, description: '', sortOrder: 1, mandatory: 1 },
      { dayOfWeek: 3, taskName: '捉鬼任务', category: '日常', estimatedIncome: 30, estimatedTime: 1, description: '', sortOrder: 2, mandatory: 1 },
      { dayOfWeek: 3, taskName: '跑商任务', category: '日常', estimatedIncome: 40, estimatedTime: 1, description: '', sortOrder: 3, mandatory: 0 }
    ],
    4: [ // 周四
      { dayOfWeek: 4, taskName: '师门任务', category: '日常', estimatedIncome: 15, estimatedTime: 0.5, description: '', sortOrder: 1, mandatory: 1 },
      { dayOfWeek: 4, taskName: '捉鬼任务', category: '日常', estimatedIncome: 30, estimatedTime: 1, description: '', sortOrder: 2, mandatory: 1 },
      { dayOfWeek: 4, taskName: '副本挑战', category: '副本', estimatedIncome: 50, estimatedTime: 1, description: '英雄冢/幽冥', sortOrder: 3, mandatory: 0 }
    ],
    5: [ // 周五
      { dayOfWeek: 5, taskName: '师门任务', category: '日常', estimatedIncome: 15, estimatedTime: 0.5, description: '', sortOrder: 1, mandatory: 1 },
      { dayOfWeek: 5, taskName: '捉鬼任务', category: '日常', estimatedIncome: 30, estimatedTime: 1, description: '', sortOrder: 2, mandatory: 1 },
      { dayOfWeek: 5, taskName: '宝图任务', category: '日常', estimatedIncome: 20, estimatedTime: 1, description: '', sortOrder: 3, mandatory: 0 }
    ],
    6: [ // 周六
      { dayOfWeek: 6, taskName: '师门任务', category: '日常', estimatedIncome: 15, estimatedTime: 0.5, description: '', sortOrder: 1, mandatory: 1 },
      { dayOfWeek: 6, taskName: '捉鬼任务', category: '日常', estimatedIncome: 30, estimatedTime: 1, description: '', sortOrder: 2, mandatory: 1 },
      { dayOfWeek: 6, taskName: '周末活动', category: '活动', estimatedIncome: 60, estimatedTime: 1.5, description: '华山/群雄/科举', sortOrder: 3, mandatory: 1 },
      { dayOfWeek: 6, taskName: '副本挑战', category: '副本', estimatedIncome: 50, estimatedTime: 1, description: '', sortOrder: 4, mandatory: 0 }
    ],
    7: [ // 周日
      { dayOfWeek: 7, taskName: '师门任务', category: '日常', estimatedIncome: 15, estimatedTime: 0.5, description: '', sortOrder: 1, mandatory: 1 },
      { dayOfWeek: 7, taskName: '捉鬼任务', category: '日常', estimatedIncome: 30, estimatedTime: 1, description: '', sortOrder: 2, mandatory: 1 },
      { dayOfWeek: 7, taskName: '宝图任务', category: '日常', estimatedIncome: 20, estimatedTime: 1, description: '', sortOrder: 3, mandatory: 0 },
      { dayOfWeek: 7, taskName: '跑商任务', category: '日常', estimatedIncome: 40, estimatedTime: 1, description: '整理下周计划', sortOrder: 4, mandatory: 0 }
    ]
  }
  return defaults
}

function getDefaultTasksForDay(dayOfWeek) {
  const defaults = getDefaultDayTasks()
  return defaults[dayOfWeek] ? JSON.parse(JSON.stringify(defaults[dayOfWeek])) : []
}

async function loadPlans() {
  loading.value = true
  try {
    const res = await getWeeklyPlans()
    if (res.data?.code === 200) {
      plans.value = res.data.data || []
    }
  } catch (e) {
    console.error('加载计划失败:', e)
  } finally {
    loading.value = false
  }
}

function handleCreate() {
  form.value = {
    ...emptyForm,
    year: new Date().getFullYear(),
    weekNumber: getCurrentWeek(),
    startDate: getMondayOfCurrentWeek(),
    endDate: getSundayOfCurrentWeek(),
    dayTasks: getDefaultDayTasks()
  }
  isEdit.value = false
  dialogVisible.value = true
}

function handleEdit(plan) {
  form.value = JSON.parse(JSON.stringify(plan))
  // 确保 dayTasks 是按星期分组
  if (!form.value.dayTasks || form.value.dayTasks.length === 0) {
    form.value.dayTasks = getDefaultDayTasks()
  }
  isEdit.value = true
  dialogVisible.value = true
}

function handleDelete(plan) {
  ElMessageBox.confirm(`确定要删除计划「${plan.planName}」吗？此操作不可恢复。`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deletePlan(plan.id)
      ElMessage.success('删除成功')
      await loadPlans()
    } catch (e) {
      ElMessage.error(e.response?.data?.msg || '删除失败')
    }
  }).catch(() => {})
}

function handleToggleStatus(plan) {
  togglePlanStatus(plan.id).then(() => {
    ElMessage.success(plan.status === 1 ? '已停用' : '已激活')
    loadPlans()
  }).catch(e => {
    ElMessage.error(e.response?.data?.msg || '操作失败')
  })
}

async function handleSubmit() {
  if (!form.value.planName || !form.value.startDate || !form.value.endDate) {
    ElMessage.warning('请填写完整信息')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await updatePlan(form.value)
      ElMessage.success('更新成功')
    } else {
      await createPlan(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await loadPlans()
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '操作失败')
  } finally {
    submitting.value = false
  }
}

function getTasksByDay(dayOfWeek) {
  if (!form.value.dayTasks) return []
  return form.value.dayTasks.filter(t => t.dayOfWeek === dayOfWeek)
}

function addTask(dayOfWeek) {
  if (!form.value.dayTasks) form.value.dayTasks = []
  form.value.dayTasks.push({
    dayOfWeek,
    taskName: '',
    category: '日常',
    estimatedIncome: 0,
    estimatedTime: 0,
    description: '',
    sortOrder: (getTasksByDay(dayOfWeek).length + 1),
    mandatory: 1
  })
}

function removeTask(index) {
  form.value.dayTasks.splice(index, 1)
}

function moveTask(dayOfWeek, index, direction) {
  const tasks = getTasksByDay(dayOfWeek)
  const newIndex = index + direction
  if (newIndex < 0 || newIndex >= tasks.length) return
  ;[tasks[index], tasks[newIndex]] = [tasks[newIndex], tasks[index]]
}

onMounted(loadPlans)
</script>

<template>
  <div class="task-plan-manage">
    <div class="page-header">
      <h2>每周任务计划管理</h2>
      <el-button type="primary" @click="handleCreate">+ 新建计划</el-button>
    </div>

    <!-- 计划列表 -->
    <el-table :data="plans" v-loading="loading" stripe>
      <el-table-column prop="planName" label="计划名称" width="180" />
      <el-table-column label="日期范围" width="240">
        <template #default="{ row }">
          {{ row.startDate }} ~ {{ row.endDate }}
        </template>
      </el-table-column>
      <el-table-column prop="year" label="年份" width="80" />
      <el-table-column prop="weekNumber" label="周数" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'info' : 'info'">
            {{ row.status === 1 ? '进行中' : row.status === 2 ? '已完成' : '已停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button
            size="small"
            :type="row.status === 1 ? 'warning' : 'success'"
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 1 ? '停用' : '激活' }}
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑计划' : '新建计划'"
      width="900px"
      destroy-on-close
    >
      <div class="form-section">
        <h4>基本信息</h4>
        <div class="basic-form">
          <div class="form-item">
            <label>计划名称</label>
            <el-input v-model="form.planName" placeholder="如：2025年第26周" />
          </div>
          <div class="form-item">
            <label>日期范围</label>
            <div class="date-range">
              <el-date-picker v-model="form.startDate" type="date" placeholder="周一" format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
              <span>至</span>
              <el-date-picker v-model="form.endDate" type="date" placeholder="周日" format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
            </div>
          </div>
        </div>
      </div>

      <div class="form-section">
        <h4>每日任务模板</h4>
        <p class="section-hint">点击"添加任务"为每天添加任务，拖拽或上下箭头调整顺序</p>

        <div v-for="day in [1,2,3,4,5,6,7]" :key="day" class="day-section">
          <div class="day-header">
            <span class="day-label">{{ DAY_LABELS[day] }}</span>
            <el-button size="small" type="primary" plain @click="addTask(day)">+ 添加任务</el-button>
          </div>
          <div class="task-list">
            <div
              v-for="(task, index) in getTasksByDay(day)"
              :key="index"
              class="task-row"
            >
              <div class="task-controls">
                <el-button size="small" :icon="'Top'" circle @click="moveTask(day, index, -1)" />
                <el-button size="small" :icon="'Bottom'" circle @click="moveTask(day, index, 1)" />
              </div>
              <el-input v-model="task.taskName" placeholder="任务名称" style="width: 140px" />
              <el-select v-model="task.category" placeholder="分类" style="width: 90px">
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
              </el-select>
              <el-input-number v-model="task.estimatedIncome" :min="0" :step="5" placeholder="预估收入" style="width: 100px" />
              <el-input-number v-model="task.estimatedTime" :min="0" :step="0.5" placeholder="预估耗时" style="width: 90px" />
              <el-switch v-model="task.mandatory" active-text="必做" inactive-text="选做" style="width: 80px" />
              <el-button size="small" type="danger" :icon="'Delete'" circle @click="removeTask(form.dayTasks.indexOf(task))" />
            </div>
            <div v-if="getTasksByDay(day).length === 0" class="empty-day">
              暂无任务，点击"添加任务"开始编辑
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.task-plan-manage {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.form-section {
  margin-bottom: 20px;
}

.form-section h4 {
  margin: 0 0 12px;
  font-size: 15px;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 8px;
}

.section-hint {
  font-size: 12px;
  color: #909399;
  margin: 0 0 12px;
}

.basic-form {
  display: flex;
  gap: 20px;
}

.basic-form .form-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.basic-form label {
  font-size: 13px;
  color: #606266;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.day-section {
  margin-bottom: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f5f7fa;
}

.day-label {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
}

.task-list {
  padding: 8px 12px;
}

.task-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
}

.task-controls {
  display: flex;
  gap: 4px;
}

.empty-day {
  text-align: center;
  padding: 12px;
  color: #c0c4cc;
  font-size: 13px;
}
</style>
