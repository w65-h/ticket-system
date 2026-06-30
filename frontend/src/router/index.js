import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/user/Login.vue')
  },
  {
    path: '/',
    component: () => import('../components/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'ticket',
        name: 'Ticket',
        component: () => import('../views/ticket/TicketList.vue'),
        meta: { title: '工单管理' }
      },
      {
        path: 'ticket/create',
        name: 'TicketCreate',
        component: () => import('../views/ticket/TicketCreate.vue'),
        meta: { title: '创建工单' }
      },
      {
        path: 'system',
        name: 'System',
        component: () => import('../views/system/DeptManage.vue'),
        meta: { title: '系统管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    next()
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

export default router