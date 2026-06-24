<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getGuideDetail } from '../api/guide.js'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const guide = ref(null)

async function loadGuide() {
  loading.value = true
  try {
    const res = await getGuideDetail(route.params.id)
    if (res.data.code === 200) {
      guide.value = res.data.data
    }
  } catch (e) {
    console.error('加载攻略详情失败:', e)
    ElMessage.error('加载攻略详情失败')
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/')
}

onMounted(() => {
  loadGuide()
})
</script>

<template>
  <div class="guide-detail" v-loading="loading">
    <div v-if="guide" class="detail-container">
      <!-- 返回按钮 -->
      <div class="back-btn">
        <el-button type="primary" plain @click="goBack">
          ← 返回首页
        </el-button>
      </div>

      <!-- 攻略内容 -->
      <el-card class="detail-card" shadow="hover">
        <template #header>
          <div class="detail-header">
            <h1 class="detail-title">{{ guide.title }}</h1>
            <div class="detail-meta">
              <el-tag size="large">{{ guide.category }}</el-tag>
              <span class="update-time">{{ guide.updatedAt }}</span>
            </div>
          </div>
        </template>

        <div class="detail-content" v-if="guide.content">
          <div class="content-text" v-html="guide.content"></div>
        </div>
        <el-empty v-else description="暂无内容" />
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.guide-detail {
  min-height: 100vh;
  background: #f0f2f5;
  padding: 24px;
}

.detail-container {
  max-width: 800px;
  margin: 0 auto;
}

.back-btn {
  margin-bottom: 20px;
}

.detail-card {
  border-radius: 8px;
}

.detail-header {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  line-height: 1.4;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.update-time {
  font-size: 13px;
  color: #909399;
}

.detail-content {
  padding: 8px 0;
}

.content-text {
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
  font-size: 15px;
}
</style>
