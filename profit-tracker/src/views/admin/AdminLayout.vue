<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useProfitStore } from '../../stores/profitStore.js'
import { ElMessage } from 'element-plus'
import {
  UserFilled,
  Key,
  Lock,
  SwitchButton,
  Setting,
  Back
} from '@element-plus/icons-vue'

const router = useRouter()
const store = useProfitStore()

const activeMenu = ref('users')
const sidebarCollapsed = ref(false)

const menuItems = [
  { key: 'users', label: '用户管理', icon: UserFilled },
  { key: 'roles', label: '角色管理', icon: Key },
  { key: 'permissions', label: '权限管理', icon: Lock }
]

function handleMenuSelect(key) {
  activeMenu.value = key
  router.push('/admin/' + key)
}

function handleBack() {
  router.push('/')
}

function handleLogout() {
  store.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}

function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
}
</script>

<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <div class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <el-icon :size="24" color="#409EFF"><SwitchButton /></el-icon>
        <span v-show="!sidebarCollapsed" class="sidebar-title">后台管理</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="sidebarCollapsed"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        @select="handleMenuSelect"
      >
        <el-menu-item v-for="item in menuItems" :key="item.key" :index="item.key">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.label }}</template>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 主内容区 -->
    <div class="main-container">
      <!-- 顶部导航 -->
      <div class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleSidebar" :size="20">
            <component :is="sidebarCollapsed ? 'Expand' : 'Fold'" />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item><a @click="handleBack">工作台</a></el-breadcrumb-item>
            <el-breadcrumb-item>
              {{ menuItems.find(m => m.key === activeMenu)?.label || '管理台' }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <span class="username">{{ store.username }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  width: 220px;
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
}

.sidebar.collapsed {
  width: 64px;
}

.sidebar-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid #2d3a4b;
  padding: 0 10px;
}

.sidebar-title {
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  white-space: nowrap;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 60px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.collapse-btn {
  cursor: pointer;
  color: #606266;
}

.collapse-btn:hover {
  color: #409EFF;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.username {
  font-size: 14px;
  color: #606266;
}

.content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f0f2f5;
}
</style>
