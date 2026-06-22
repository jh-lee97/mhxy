import { createRouter, createWebHashHistory } from 'vue-router'

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
    meta: { requiresAdmin: true }
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('../views/admin/AdminLayout.vue'),
    children: [
      {
        path: '',
        redirect: '/admin/users'
      },
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
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
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
  if (to.meta.requiresAdmin && token) {
    const roles = JSON.parse(localStorage.getItem('roles') || '[]')
    if (!roles.includes('ADMIN')) {
      next('/')
      return
    }
    // 加载权限
    if (!localStorage.getItem('permissions')) {
      fetch('http://localhost:8080/api/auth/permissions')
        .then(res => res.json())
        .then(data => {
          if (data.code === 200 && data.data) {
            localStorage.setItem('permissions', JSON.stringify(data.data.permissions || []))
            localStorage.setItem('roles', JSON.stringify(data.data.roles || []))
            localStorage.setItem('menus', JSON.stringify(data.data.menus || []))
          }
        })
        .catch(() => {})
    }
  }

  next()
})

export default router
