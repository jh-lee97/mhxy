<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import Dashboard from '../components/Dashboard.vue'
import AddRecordForm from '../components/AddRecordForm.vue'
import RecordList from '../components/RecordList.vue'
import ProfitChart from '../components/ProfitChart.vue'
import { useAuthStore } from '../stores/authStore.js'
import { addRecord as apiAddRecord } from '../api/record.js'

const router = useRouter()
const store = useProfitStore()
const formRef = ref(null)

const isLoggedIn = computed(() => store.token !== null)
const isAdmin = computed(() => store.isAdmin())

function goToAdmin() {
  router.push('/admin')
}

async function handleAdd(record) {
  await store.addRecord(record)
  if (formRef.value && formRef.value.closeDialog) {
    formRef.value.closeDialog()
  }
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    store.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  if (isLoggedIn.value) {
    store.getRecords()
    store.getStats()
    store.getChartRecords()
  }
})
</script>

<template>
  <div class="home">
    <header class="header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="title">💰 工作台</h1>
          <p class="subtitle">记录每一笔收益，清晰掌握赚钱效率</p>
        </div>
        <div class="header-right" v-if="isLoggedIn">
          <span class="user-info">👤 {{ store.username }}</span>
          <span v-if="isAdmin" class="admin-badge">👑 管理员</span>
          <button v-if="isAdmin" class="admin-btn" @click="goToAdmin">后台管理</button>
          <button class="logout-btn" @click="handleLogout">退出</button>
        </div>
      </div>
    </header>

    <main class="main" v-if="isLoggedIn">
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
.home { min-height: 100vh; background: #f0f2f5; }
.header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; padding: 32px 24px 28px; }
.header-content { max-width: 1200px; margin: 0 auto; display: flex; justify-content: space-between; align-items: center; }
.header-left { }
.title { margin: 0; font-size: 24px; font-weight: 700; }
.subtitle { margin: 8px 0 0; font-size: 14px; opacity: 0.8; }
.header-right { display: flex; align-items: center; gap: 16px; }
.user-info { font-size: 14px; opacity: 0.9; }
.admin-badge { font-size: 13px; color: #ffd700; font-weight: 600; }
.admin-btn { background: rgba(255,255,255,0.3); border: 1px solid rgba(255,255,255,0.5); color: #fff; padding: 6px 16px; border-radius: 6px; cursor: pointer; font-size: 13px; transition: background 0.2s; }
.admin-btn:hover { background: rgba(255,255,255,0.4); }
.logout-btn { background: rgba(255,255,255,0.2); border: 1px solid rgba(255,255,255,0.4); color: #fff; padding: 6px 16px; border-radius: 6px; cursor: pointer; font-size: 13px; transition: background 0.2s; }
.logout-btn:hover { background: rgba(255,255,255,0.3); }
.main { max-width: 1200px; margin: 0 auto; padding: 24px; display: flex; flex-direction: column; gap: 24px; }
.section { display: flex; flex-direction: column; gap: 16px; }
.list-header { display: flex; justify-content: space-between; align-items: center; }
.list-header h3 { margin: 0; font-size: 18px; color: #303133; }
.footer { text-align: center; padding: 20px; color: #c0c4cc; font-size: 12px; }
</style>
