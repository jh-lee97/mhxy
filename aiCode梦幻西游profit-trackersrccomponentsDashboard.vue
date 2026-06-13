<script setup>
import { ref, onMounted } from 'vue'
import { useProfitStore } from '../stores/profitStore.js'

const store = useProfitStore()
const stats = ref(null)
const records = ref([])

async function loadData() {
  const [s, r] = await Promise.all([store.getStats(), store.getRecords()])
  stats.value = s
  records.value = r || []
}

onMounted(loadData)

function getProfitClass(value) {
  return value != null && value >= 0 ? 'positive' : 'negative'
}
</script>

<template>
  <div class="dashboard">
    <div class="stat-cards">
      <div class="card today">
        <div class="card-label">今日收入</div>
        <div class="card-value income">{{ stats?.todayIncome?.toFixed(2) ?? '0.00' }}</div>
        <div class="card-sub">成本: {{ stats?.todayCost?.toFixed(2) ?? '0.00' }} | 净利: <span :class="getProfitClass(stats?.todayProfit)">{{ stats?.todayProfit?.toFixed(2) ?? '0.00' }}</span></div>
      </div>
      <div class="card today">
        <div class="card-label">本周收入</div>
        <div class="card-value income">{{ stats?.weekIncome?.toFixed(2) ?? '0.00' }}</div>
        <div class="card-sub">成本: {{ stats?.weekCost?.toFixed(2) ?? '0.00' }} | 净利: <span :class="getProfitClass(stats?.weekProfit)">{{ stats?.weekProfit?.toFixed(2) ?? '0.00' }}</span></div>
      </div>
      <div class="card today">
        <div class="card-label">累计收入</div>
        <div class="card-value income">{{ stats?.totalIncome?.toFixed(2) ?? '0.00' }}</div>
        <div class="card-sub">总成本: {{ stats?.totalCost?.toFixed(2) ?? '0.00' }} | 总净利: <span :class="getProfitClass(stats?.totalProfit)">{{ stats?.totalProfit?.toFixed(2) ?? '0.00' }}</span></div>
      </div>
      <div class="card today">
        <div class="card-label">记录总数</div>
        <div class="card-value">{{ records.length }}</div>
        <div class="card-sub">条记录</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.stat-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}
.card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s;
}
.card:hover { transform: translateY(-2px); }
.card-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.card-value { font-size: 28px; font-weight: 700; color: #303133; }
.card-value.income { color: #67c23a; }
.card-sub { font-size: 12px; color: #c0c4cc; margin-top: 4px; }
.positive { color: #67c23a; }
.negative { color: #f56c6c; }
</style>
