import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'

import HomeView from '../views/HomeView.vue'
import CourseView from '../views/CourseView.vue'
import CourseResourceView from '../views/CourseResourceView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),

  routes: [
    // 根路径
    { path: '/', redirect: '/login' },

    /**
     * =========================
     * 公共页面（无需登录）
     * =========================
     */

    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: {
        public: true,
        title: '登录'
      }
    },

    {
      path: '/portal',
      name: 'portal-public',
      component: () => import('@/views/portal/PortalPublicView.vue'),
      meta: {
        public: true,
        title: '网站门户'
      }
    },

    // 课程页面（从队友代码合并）
    {
      path: '/course',
      name: 'course',
      component: CourseView,
      meta: {
        title: '课程中心'
      }
    },

    {
      path: '/course-resource/:courseId',
      name: 'course-resource',
      component: CourseResourceView,
      meta: {
        title: '课程资源'
      }
    },

    {
      path: '/home',
      name: 'home',
      component: HomeView,
      meta: {
        title: '首页'
      }
    },

    /**
     * =========================
     * 后台管理系统
     * =========================
     */

    {
      path: '/admin',

      component: AdminLayout,

      meta: {
        requiresAuth: true
      },

      children: [
        {
          path: '',
          redirect: '/admin/dashboard'
        },

        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/DashboardView.vue'),
          meta: {
            title: '工作台'
          }
        },

        {
          path: 'portal',
          name: 'portal-manage',
          component: () => import('@/views/portal/PortalManageView.vue'),
          meta: {
            title: '网站门户系统'
          }
        },

        {
          path: 'classes',
          name: 'classes',
          component: () => import('@/views/ClassesView.vue'),
          meta: {
            title: '班级管理'
          }
        },

        {
          path: 'question',
          name: 'QuestionManage',
          component: () => import('@/views/QuestionManage.vue'),
          meta: {
            title: '题目管理'
          }
        },

        {
          path: 'homework-comments',
          name: 'homework-comments',
          component: () => import('@/views/HomeworkCommentsView.vue'),
          meta: {
            title: '作业评语'
          }
        },

        {
          path: 'teaching-notifications',
          name: 'teaching-notifications',
          component: () => import('@/views/TeachingNotificationsView.vue'),
          meta: {
            title: '教学通知'
          }
        },

        {
          path: 'about',
          name: 'about',
          component: () => import('@/views/AboutView.vue'),
          meta: {
            title: '关于系统'
          }
        }
      ]
    },

    /**
     * 404
     */
    {
      path: '/:pathMatch(.*)*',
      redirect: '/'
    }
  ]
})

/**
 * 全局前置守卫
 */
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  // 动态标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 智能教学平台`
  }

  // 需要登录
  if (to.meta.requiresAuth && !token) {
    next({
      path: '/login',
      query: {
        redirect: to.fullPath
      }
    })
  } else if (to.path === '/login' && token) {
    next('/admin/dashboard')
  } else {
    next()
  }
})

export default router
