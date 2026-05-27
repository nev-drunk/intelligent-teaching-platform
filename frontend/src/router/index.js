import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 1. 根路径重定向
    { path: '/', redirect: '/login' },

    // 2. 免登录公开路由
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { public: true, title: '登录' }
    },
    {
      path: '/portal',
      name: 'portal-public',
      component: () => import('@/views/portal/PortalPublicView.vue'),
      meta: { public: true, title: '网站门户' }
    },

    // 3. 后台管理系统路由（全部需要登录认证）
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: '/admin/dashboard' }, // 建议默认重定向到工作台或门户管理
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/DashboardView.vue'),
          meta: { title: '工作台' }
        },
        {
          path: 'portal',
          name: 'portal-manage',
          component: () => import('@/views/portal/PortalManageView.vue'),
          meta: { title: '网站门户系统' }
        },
        {
          path: 'classes',
          name: 'classes',
          component: () => import('@/views/ClassesView.vue'),
          meta: { title: '班级管理' }
        },
        // ⚡ 从第一份代码合并进来的“题目管理”子路由
        {
          path: 'question',
          name: 'QuestionManage',
          component: () => import('@/views/QuestionManage.vue'),
          meta: { title: '题目管理' }
        },
        {
          path: 'homework-comments',
          name: 'homework-comments',
          component: () => import('@/views/HomeworkCommentsView.vue'),
          meta: { title: '作业评语' }
        },
        {
          path: 'teaching-notifications',
          name: 'teaching-notifications',
          component: () => import('@/views/TeachingNotificationsView.vue'),
          meta: { title: '教学通知' }
        },
        // 从第一份代码合并进来的“关于我们”页面（移入后台系统）
        {
          path: 'about',
          name: 'about',
          component: () => import('@/views/AboutView.vue'),
          meta: { title: '关于系统' }
        }
      ]
    },
    // 4. 404 兜底路由（可选，建议加上防止输错 URL 白屏）
    {
      path: '/:pathMatch(.*)*',
      redirect: '/'
    }
  ]
})

/**
 * 全局前置路由守卫
 */
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  // 动态修改页面网页标签标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 智能教学平台`
  }

  // 验证登录状态
  if (to.meta.requiresAuth && !token) {
    // 未登录且访问受限页面：重定向到登录页，并记录原本想去的路径
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.path === '/login' && token) {
    // 已登录还想去登录页：直接拦截并送回后台首页
    next('/admin/dashboard')
  } else {
    // 放行
    next()
  }
})

export default router
