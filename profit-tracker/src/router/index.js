import { createRouter, createWebHashHistory } from 'vue-router'
import { getUserPermissions } from '../api/auth.js'

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
    children: [
      {
        path: 'users',
        name: 'UserManage',
        component: () => import('../views/admin/UserManage.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'roles',
        name: 'RoleManage',
        component: () => import('../views/admin/RoleManage.vue'),
        meta: { title: '角色管理' }
      },
      {
        path: 'permissions',
        name: 'PermissionManage',
        component: () => import('../views/admin/PermissionManage.vue'),
        meta: { title: '权限管理' }
      },
      {
        path: 'guides',
        name: 'GuideManage',
        component: () => import('../views/admin/GuideManage.vue'),
        meta: { title: '攻略管理' }
      }
    ]
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
    if (!localStorage.getItem('permissions')) {
      try {
        const res = await getUserPermissions()
        if (res.data.code === 200 && res.data.data) {
          localStorage.setItem('permissions', JSON.stringify(res.data.data.permissions || []))
          localStorage.setItem('roles', JSON.stringify(res.data.data.roles || []))
          localStorage.setItem('menus', JSON.stringify(res.data.data.menus || []))
        }
      } catch (err) {
        console.error('[路由守卫] 权限加载失败:', err)
        // 权限加载失败，清除 token 并跳转登录
        localStorage.removeItem('token')
        localStorage.removeItem('roles')
        localStorage.removeItem('permissions')
        localStorage.removeItem('menus')
        next('/login')
        return
      }
    }
  }

  next()
})

export default router
