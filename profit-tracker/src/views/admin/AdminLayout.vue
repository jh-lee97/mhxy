<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/authStore.js'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  UserFilled,
  Key,
  Lock,
  Fold,
  Expand,
  Reading,
  List
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const store = useAuthStore()

const activeMenu = ref('users')
const sidebarCollapsed = ref(false)

// 固定的管理后台菜单项（只有管理员可访问）
const fixedAdminMenus = [
  { key: 'users', path: '/admin/users', label: '用户管理', icon: UserFilled },
  { key: 'roles', path: '/admin/roles', label: '角色管理', icon: Key },
  { key: 'permissions', path: '/admin/permissions', label: '权限管理', icon: Lock },
  { key: 'guides', path: '/admin/guides', label: '攻略管理', icon: Reading },
  { key: 'tasks', path: '/admin/tasks', label: '任务计划', icon: List }
]

// 从 authStore 读取动态菜单
const dynamicMenus = computed(() => {
  return store.menus || []
})

// 检查用户是否有某个菜单的访问权限
function hasMenuAccess(menuKey) {
  // 管理员默认有所有权限
  if (store.roles && store.roles.includes('ADMIN')) {
    return true
  }
  // 检查权限标识
  const permissionMap = {
    'users': 'user:manage',
    'roles': 'role:manage',
    'permissions': 'permission:manage',
    'guides': 'guide:manage',
    'tasks': 'task:view'
  }
  const permission = permissionMap[menuKey]
  if (!permission) return true
  return store.permissions && store.permissions.includes(permission)
}

// 过滤出当前用户有权限访问的菜单
const accessibleMenus = computed(() => {
  return fixedAdminMenus.filter(menu => hasMenuAccess(menu.key))
})

// 根据当前路由获取激活的菜单 key
function getActiveMenuKey() {
  const fullPath = route.fullPath
  // 如果是管理后台路由，提取 key
  const adminMatch = fullPath.match(/^\/admin\/(\w+)/)
  if (adminMatch) {
    return adminMatch[1]
  }
  return accessibleMenus.value[0]?.key || 'users'
}

// 监听路由变化，更新激活菜单
watch(() => route.path, () => {
  activeMenu.value = getActiveMenuKey()
})

onMounted(() => {
  activeMenu.value = getActiveMenuKey()
})

function handleMenuSelect(key) {
  activeMenu.value = key
  router.push('/admin/' + key)
}

function handleBack() {
  router.push('/')
}

function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    store.clearAuth()
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {
    // 用户取消
  })
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
        <el-icon class="collapse-toggle" :size="20" @click="toggleSidebar">
          <component :is="sidebarCollapsed ? Expand : Fold" />
        </el-icon>
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
        <el-menu-item v-for="item in accessibleMenus" :key="item.key" :index="item.key">
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
            <component :is="sidebarCollapsed ? Expand : Fold" />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item><a @click="handleBack">工作台</a></el-breadcrumb-item>
            <el-breadcrumb-item>
              {{ accessibleMenus.find(m => m.key === activeMenu)?.label || '管理台' }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <span class="username">{{ store.username }}</span>
          <el-button type="primary" plain size="small" @click="handleBack">返回首页</el-button>
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
  position: relative;
  display: flex;
  flex-direction: column;
}

.sidebar.collapsed {
  width: 64px;
}

.sidebar-header {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  border-bottom: 1px solid #2d3a4b;
  justify-content: flex-start;
}

.collapse-toggle {
  cursor: pointer;
  color: #fff;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 6px;
  background-color: rgba(255, 255, 255, 0.08);
  transition: background-color 0.2s;
}

.collapse-toggle:hover {
  background-color: rgba(255, 255, 255, 0.16);
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
