import { createRouter, createWebHashHistory } from 'vue-router'
import { getUserPermissions } from '../api/auth.js'
import { useAuthStore } from '../stores/authStore.js'

// 管理后台子路由配置
const adminRoutes = [
  {
    path: 'users',
    name: 'UserManage',
    component: () => import('../views/admin/UserManage.vue'),
    meta: { title: '用户管理', icon: 'UserFilled' }
  },
  {
    path: 'roles',
    name: 'RoleManage',
    component: () => import('../views/admin/RoleManage.vue'),
    meta: { title: '角色管理', icon: 'Key' }
  },
  {
    path: 'permissions',
    name: 'PermissionManage',
    component: () => import('../views/admin/PermissionManage.vue'),
    meta: { title: '权限管理', icon: 'Lock' }
  },
  {
    path: 'guides',
    name: 'GuideManage',
    component: () => import('../views/admin/GuideManage.vue'),
    meta: { title: '攻略管理', icon: 'Reading' }
  },
  {
    path: 'tasks',
    name: 'TaskPlanManage',
    component: () => import('../views/admin/TaskPlanManage.vue'),
    meta: { title: '任务计划管理', icon: 'List' }
  }
]

// 构建路由表
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/HomeView.vue'),
    meta: { title: '工作台' }
  },
  {
    path: '/admin',
    redirect: '/admin/users',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { requiresAdmin: true, title: '管理后台' },
    children: adminRoutes
  },
  {
    path: '/guide/:id',
    name: 'GuideDetail',
    component: () => import('../views/GuideDetailView.vue'),
    meta: { title: '攻略详情' }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')
  const store = useAuthStore()

  // 未登录
  if (to.path !== '/login' && !token) {
    next('/login')
    return
  }

  // 已登录访问登录页，跳回首页
  if (to.path === '/login' && token) {
    next('/')
    return
  }

  // 需要管理员权限
  if (to.matched.some(record => record.meta.requiresAdmin) && token) {
    const roles = JSON.parse(localStorage.getItem('roles') || '[]')
    if (!roles.includes('ADMIN')) {
      next('/')
      return
    }
    
    // 加载权限（如果还没有缓存）
    if (!store.menus || store.menus.length === 0) {
      try {
        const res = await getUserPermissions()
        if (res.data.code === 200 && res.data.data) {
          const permissions = res.data.data.permissions || []
          const rolesList = res.data.data.roles || []
          const menus = res.data.data.menus || []
          
          store.setAuthData(permissions, rolesList, menus)
        }
      } catch (err) {
        console.error('[路由守卫] 权限加载失败:', err)
        // 权限加载失败，清除 token 并跳转登录
        store.clearAuth()
        localStorage.removeItem('token')
        next('/login')
        return
      }
    }
  }

  next()
})

export default router
