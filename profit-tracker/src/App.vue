<script setup>
import { ref, onMounted } from 'vue'
import Dashboard from './components/Dashboard.vue'
import AddRecordForm from './components/AddRecordForm.vue'
import RecordList from './components/RecordList.vue'
import ProfitChart from './components/ProfitChart.vue'
import { useProfitStore } from './stores/profitStore.js'

const store = useProfitStore()
const formRef = ref(null)

async function handleAdd(record) {
  await store.addRecord(record)
  if (formRef.value && formRef.value.closeDialog) {
    formRef.value.closeDialog()
  }
}

onMounted(() => {
  store.getRecords()
  store.getStats()
  store.getChartRecords()
})
</script>

<template>
  <div class="app">
    <header class="header">
      <div class="header-content">
        <h1 class="title">💰 梦幻西游 · 五开收益记录</h1>
        <p class="subtitle">记录每一笔收益，清晰掌握赚钱效率</p>
      </div>
    </header>

    <main class="main">
      <section class="section">
        <Dashboard />
      </section>

      <section class="section">
        <ProfitChart />
      </section>

      <section class="section">
        <div class="list-header">
          <h3>收益记录</h3>
          <AddRecordForm ref="formRef" @add="handleAdd" />
        </div>
        <RecordList />
      </section>
    </main>

    <footer class="footer">
      <p>数据存储在后端数据库中，需要后端服务运行 (localhost:8080)</p>
    </footer>
  </div>
</template>

<style scoped>
.app { min-height: 100vh; background: #f0f2f5; }
.header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; padding: 32px 24px 28px; }
.header-content { max-width: 1200px; margin: 0 auto; }
.title { margin: 0; font-size: 24px; font-weight: 700; }
.subtitle { margin: 8px 0 0; font-size: 14px; opacity: 0.8; }
.main { max-width: 1200px; margin: 0 auto; padding: 24px; display: flex; flex-direction: column; gap: 24px; }
.section { display: flex; flex-direction: column; gap: 16px; }
.list-header { display: flex; justify-content: space-between; align-items: center; }
.list-header h3 { margin: 0; font-size: 18px; color: #303133; }
.footer { text-align: center; padding: 20px; color: #c0c4cc; font-size: 12px; }
</style>
