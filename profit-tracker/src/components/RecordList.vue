<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { getRecords, deleteRecord as apiDeleteRecord } from '../api/record.js'
import { formatMoney } from '../utils/format.js'

const loading = ref(false)
const records = ref([])
const dialogVisible = ref(false)
const _targetId = ref(null)

// 控制 body 滚动锁定
watch(dialogVisible, (val) => {
  document.body.style.overflow = val ? 'hidden' : ''
})

async function loadRecords() {
  loading.value = true
  try {
    const res = await getRecords()
    records.value = res?.data?.code === 200 ? res.data.data : []
  } catch (e) {
    console.error('加载记录失败:', e)
  } finally {
    loading.value = false
  }
}

async function deleteRecordById(id) {
  await apiDeleteRecord(id)
  await loadRecords()
}

function confirmDelete(id) {
  dialogVisible.value = true
  _targetId.value = id
}

async function doDelete() {
  if (_targetId.value != null) {
    await deleteRecordById(_targetId.value)
    dialogVisible.value = false
  }
}

function handleKeyDown(e) {
  if (!dialogVisible.value) return
  if (e.key === 'Enter' || e.key === 'y') {
    doDelete()
  } else if (e.key === 'Escape' || e.key === 'n') {
    dialogVisible.value = false
  }
}

window.addEventListener('keydown', handleKeyDown)
onMounted(loadRecords)

function getNetProfitClass(record) {
  const net = (record.income || 0) - (record.cost || 0)
  return net >= 0 ? 'positive' : 'negative'
}

function getIncome(record) {
  return (record.income || 0) + (record.cbgIncome || 0) + (record.propIncome || 0)
}
</script>

<template>
  <div class="list">
    <div v-if="records.length === 0" class="empty">
      <div class="empty-icon">📝</div>
      <div class="empty-text">暂无记录，点击上方按钮添加第一条</div>
    </div>

    <div v-else class="record-table">
      <table>
        <thead>
          <tr>
            <th>日期</th>
            <th>玩法</th>
            <th>活动</th>
            <th>收入</th>
            <th>成本</th>
            <th>净利</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in records" :key="r.id">
            <td>{{ r.date }}</td>
            <td><span class="tag">{{ r.mode }}</span></td>
            <td>{{ r.activity || '-' }}</td>
            <td class="income-cell">{{ formatMoney(getIncome(r)) }}</td>
            <td class="cost-cell">{{ formatMoney(r.cost) }}</td>
            <td :class="getNetProfitClass(r)">{{ formatMoney(getIncome(r) - r.cost) }}</td>
            <td class="actions">
              <button class="btn-sm delete" @click="confirmDelete(r.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="dialogVisible" class="modal-overlay" @click.self="dialogVisible = false">
      <div class="confirm-box">
        <p>确定要删除这条记录吗？此操作不可恢复。</p>
        <div class="confirm-actions">
          <button class="btn cancel" @click="dialogVisible = false">取消</button>
          <button class="btn submit" @click="doDelete">确认删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.list { display: flex; flex-direction: column; gap: 16px; }
.empty { text-align: center; padding: 60px 20px; color: #c0c4cc; }
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.empty-text { font-size: 14px; }
.record-table { overflow-x: auto; background: #fff; border-radius: 12px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08); }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #ebeef5; font-size: 14px; }
th { background: #f5f7fa; color: #909399; font-weight: 600; font-size: 13px; white-space: nowrap; }
tr:hover td { background: #fafafa; }
.tag { display: inline-block; padding: 2px 8px; background: #ecf5ff; color: #409eff; border-radius: 4px; font-size: 12px; }
.income-cell { color: #67c23a; font-weight: 600; }
.cost-cell { color: #e6a23c; }
.positive { color: #67c23a; font-weight: 600; }
.negative { color: #f56c6c; font-weight: 600; }
.actions { white-space: nowrap; }
.btn-sm { padding: 4px 12px; border-radius: 4px; border: 1px solid #f56c6c; background: #fff; color: #f56c6c; cursor: pointer; font-size: 12px; transition: all 0.2s; }
.btn-sm:hover { background: #f56c6c; color: #fff; }
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 0, 0, 0.5); display: flex; align-items: center; justify-content: center; z-index: 1001; }
.confirm-box { background: #fff; padding: 24px; border-radius: 12px; text-align: center; min-width: 300px; }
.confirm-box p { margin-bottom: 16px; color: #606266; }
.confirm-actions { display: flex; justify-content: center; gap: 12px; }
.btn { padding: 8px 20px; border-radius: 6px; border: 1px solid #dcdfe6; cursor: pointer; font-size: 14px; }
.btn.cancel { background: #fff; color: #606266; }
.btn.submit { background: #f56c6c; color: #fff; border-color: #f56c6c; }
</style>
