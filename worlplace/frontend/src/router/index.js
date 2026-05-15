
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { public: true }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/ForgotPassword.vue'),
    meta: { public: true }
  },
  {
    path: '/dashboard',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'pet',
        name: 'Pet',
        component: () => import('@/views/Pet.vue'),
        meta: { title: '宠物管理' }
      },
      {
        path: 'health',
        name: 'Health',
        component: () => import('@/views/Health.vue'),
        meta: { title: '健康管理' }
      },
      {
        path: 'service',
        name: 'Service',
        component: () => import('@/views/ServiceAppointment.vue'),
        meta: { title: '服务预约' }
      },
      {
        path: 'shop',
        name: 'Shop',
        component: () => import('@/views/Shop.vue'),
        meta: { title: '商城' }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/Cart.vue'),
        meta: { title: '购物车' }
      },
      {
        path: 'my-orders',
        name: 'MyOrders',
        component: () => import('@/views/MyOrders.vue'),
        meta: { title: '我的订单' }
      },
      {
        path: 'address',
        name: 'Address',
        component: () => import('@/views/Address.vue'),
        meta: { title: '收货地址' }
      },
      {
        path: 'community',
        name: 'Community',
        component: () => import('@/views/Community.vue'),
        meta: { title: '社区' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'user-management',
        name: 'UserManagement',
        component: () => import('@/views/UserManagement.vue'),
        meta: { title: '用户管理', requireAdmin: true }
      },
      {
        path: 'pet-management',
        name: 'PetManagement',
        component: () => import('@/views/PetManagement.vue'),
        meta: { title: '宠物管理', requireAdmin: true }
      },
      {
        path: 'health-management',
        name: 'HealthManagement',
        component: () => import('@/views/HealthManagement.vue'),
        meta: { title: '健康数据管理', requireAdmin: true }
      },

      {
        path: 'appointment-management',
        name: 'AppointmentManagement',
        component: () => import('@/views/AppointmentManagement.vue'),
        meta: { title: '预约管理', requireAdmin: true }
      },
      {
        path: 'my-appointment',
        name: 'MyAppointment',
        component: () => import('@/views/MyAppointment.vue'),
        meta: { title: '我的预约' }
      },
      {
        path: 'service-management',
        name: 'ServiceManagement',
        component: () => import('@/views/ServiceManagement.vue'),
        meta: { title: '服务管理', requireAdmin: true }
      },
      {
        path: 'product-management',
        name: 'ProductManagement',
        component: () => import('@/views/ProductManagement.vue'),
        meta: { title: '商品管理', requireAdmin: true }
      },
      {
        path: 'order-management',
        name: 'OrderManagement',
        component: () => import('@/views/OrderManagement.vue'),
        meta: { title: '订单管理', requireAdmin: true }
      },
      {
        path: 'notifications',
        name: 'NotificationCenter',
        component: () => import('@/views/NotificationCenter.vue'),
        meta: { title: '通知中心' }
      },
      {
        path: 'chat-config',
        name: 'ChatConfig',
        component: () => import('@/views/ChatConfig.vue'),
        meta: { title: '客服设置', requireAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  // 公开页面直接放行
  if (to.meta.public) {
    next()
    return
  }
  
  // 需要登录的页面
  if (!token) {
    // 未登录用户访问需要登录的页面，跳转到登录页
    next('/login')
  } else {
    next()
  }
})

export default router
