import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'
import ExamLayout from '@/layouts/ExamLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),

  routes: [
    { path: '/', redirect: '/login' },

    // ========== 公开页面 ==========
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { public: true, title: '登录' }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { public: true, title: '注册' }
    },
    {
      path: '/portal',
      name: 'portal-public',
      component: () => import('@/views/portal/PortalPublicView.vue'),
      meta: { public: true, title: '网站门户' }
    },

    // ========== 后台管理（统一 AdminLayout）==========
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: { name: 'dashboard' } },

        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/views/DashboardView.vue'),
          meta: { title: '工作台' }
        },
        {
          path: 'courses',
          name: 'courses',
          component: () => import('@/views/CourseView.vue'),
          meta: { title: '课程管理' }
        },
        {
          path: 'courses/:courseId/resources',
          name: 'course-resource',
          component: () => import('@/views/CourseResourceView.vue'),
          meta: { title: '课程资源' }
        },
        {
          path: 'classes',
          name: 'classes',
          component: () => import('@/views/ClassesView.vue'),
          meta: { title: '班级管理' }
        },
        {
          path: 'student',
          name: 'student',
          component: () => import('@/views/StudentManagementPage.vue'),
          meta: { title: '学生管理' }
        },
        {
          path: 'exam',
          component: ExamLayout,
          meta: { title: '题库与组卷' },
          redirect: { name: 'exam-questions' },
          children: [
            {
              path: 'questions',
              name: 'exam-questions',
              component: () => import('@/views/QuestionManage.vue'),
              meta: { title: '题库管理' }
            },
            {
              path: 'assemble',
              name: 'exam-assemble',
              component: () => import('@/views/exam/PaperAssemble.vue'),
              meta: { title: '试卷组装' }
            },
            {
              path: 'publish',
              name: 'exam-publish',
              component: () => import('@/views/exam/PublishManage.vue'),
              meta: { title: '发布管理' }
            }
          ]
        },
        {
          path: 'portal',
          name: 'portal-manage',
          component: () => import('@/views/portal/PortalManageView.vue'),
          meta: { title: '网站门户系统' }
        },
        {
          path: '/admin/homework-comments',
          name: 'homework-comments',
          component: () => import('@/views/HomeworkCommentsView.vue'),
          meta: { title: '作业评语' }
        },
        {
          path: '/admin/submission',
          name: 'submission',
          component: () => import('@/views/SubmissionManagement.vue'),
          meta: { title: '作业批改' }
        },
        {
          path: 'teaching-notifications',
          name: 'teaching-notifications',
          component: () => import('@/views/TeachingNotificationsView.vue'),
          meta: { title: '教学通知' }
        },
        {
          path: 'questionnaire',
          name: 'questionnaire',
          component: () => import('@/views/questionnaire/QuestionnairePage.vue'),
          meta: { title: '问卷调查' }
        },
        {
          path: 'issue-center',
          name: 'issue-center',
          component: () => import('@/views/issue/IssueCenterPage.vue'),
          meta: { title: '问题中心' }
        },
        {
          path: 'evaluation',
          name: 'evaluation',
          component: () => import('@/views/evaluation/EvaluationPage.vue'),
          meta: { title: '教学评价' }
        }
      ]
    },

    // ========== 旧路径兼容重定向 ==========
    { path: '/home', redirect: { name: 'dashboard' } },
    { path: '/course', redirect: { name: 'courses' } },
    {
      path: '/course-resource/:courseId',
      redirect: (to) => ({
        name: 'course-resource',
        params: { courseId: to.params.courseId }
      })
    },
    { path: '/admin/question', redirect: { name: 'exam-questions' } },
    { path: '/admin/exam', redirect: { name: 'exam-questions' } },

    { path: '/:pathMatch(.*)*', redirect: '/login' }
  ]
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)

  const title = [...to.matched].reverse().find((r) => r.meta.title)?.meta.title
  if (title) {
    document.title = `${title} - 智能教学平台`
  }

  if (requiresAuth && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.path === '/login' && token) {
    next(to.query.redirect || { name: 'dashboard' })
  } else {
    next()
  }
})

export default router
