<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getGuides } from '../api/guide.js'

const router = useRouter()

const loading = ref(false)

// 预设分类
const categories = ref(['全部', '日常任务', '副本', '活动', '装备打造'])
const activeCategory = ref('全部')

const guideList = ref([])

async function loadGuides() {
  loading.value = true
  try {
    const res = await getGuides(activeCategory.value === '全部' ? null : activeCategory.value)
    if (res.data.code === 200) {
      guideList.value = res.data.data || []
    }
  } catch (e) {
    console.error('加载攻略失败:', e)
    ElMessage.error('加载攻略失败')
  } finally {
    loading.value = false
  }
}

function handleCategoryChange(category) {
  activeCategory.value = category
  loadGuides()
}

function openDetail(guide) {
  router.push(`/guide/${guide.id}`)
}

onMounted(() => {
  loadGuides()
})
</script>

<template>
  <div class="guide-section">
    <h3>📖 游戏攻略</h3>

    <!-- 分类导航 -->
    <div class="category-nav">
      <el-button
        v-for="cat in categories"
        :key="cat"
        :type="activeCategory === cat ? 'primary' : ''"
        size="small"
        round
        @click="handleCategoryChange(cat)"
      >
        {{ cat }}
      </el-button>
    </div>

    <!-- 攻略列表 -->
    <div v-loading="loading" class="guide-list">
      <el-empty v-if="!loading && guideList.length === 0" description="暂无攻略内容" />
      <el-card
        v-for="guide in guideList"
        :key="guide.id"
        class="guide-card"
        shadow="hover"
        @click="openDetail(guide)"
      >
        <template #header>
          <div class="card-header">
            <span class="guide-category">{{ guide.category }}</span>
            <span class="guide-title">{{ guide.title }}</span>
          </div>
        </template>
        <p class="guide-summary">{{ guide.summary || '暂无摘要' }}</p>
        <div class="card-footer">
          <span class="read-more">阅读全文 →</span>
          <span class="update-time">{{ guide.updatedAt }}</span>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.guide-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.guide-section h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.category-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.guide-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.guide-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.guide-card:hover {
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.guide-category {
  font-size: 12px;
  color: #409eff;
  font-weight: 600;
}

.guide-title {
  font-size: 15px;
  font-weight: 700;
  color: #303133;
}

.guide-summary {
  font-size: 13px;
  color: #606266;
  margin: 8px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.read-more {
  font-size: 13px;
  color: #409eff;
  font-weight: 500;
}

.update-time {
  font-size: 12px;
  color: #c0c4cc;
}
</style>
