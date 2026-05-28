import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/student/home',
    name: 'StudentHome',
    component: () => import('../views/student/Home.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' }
  },
  {
    path: '/student/discussion/:code',
    name: 'StudentDiscussion',
    component: () => import('../views/student/Discussion.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' }
  },
  {
    path: '/student/task/:code',
    name: 'StudentTask',
    component: () => import('../views/student/Task.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' }
  },
  {
    path: '/student/profile',
    name: 'StudentProfile',
    component: () => import('../views/student/Profile.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' }
  },
  {
    path: '/student/forum',
    name: 'StudentForum',
    component: () => import('../views/student/Forum.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' }
  },
  {
    path: '/student/topic/:code',
    name: 'StudentTopic',
    component: () => import('../views/student/Topic.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' }
  },
  {
    path: '/student/news/list',
    name: 'StudentNewsList',
    component: () => import('../views/student/NewsList.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' }
  },
  {
    path: '/student/news/:id',
    name: 'StudentNewsDetail',
    component: () => import('../views/student/NewsDetail.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' }
  },
  {
    path: '/student/material/:id',
    name: 'StudentMaterialDetail',
    component: () => import('../views/MaterialDetail.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' }
  },
  {
    path: '/student/leader',
    name: 'StudentLeader',
    component: () => import('../views/student/Leader.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' }
  },
  {
    path: '/student/leader/:id',
    name: 'StudentLeaderDetail',
    component: () => import('../views/student/LeaderDetail.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' }
  },
  {
    path: '/student/thought',
    name: 'StudentThought',
    component: () => import('../views/student/Thought.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' }
  },
  {
    path: '/student/thought/list',
    name: 'StudentThoughtList',
    component: () => import('../views/student/ThoughtList.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' }
  },
  {
    path: '/student/science',
    name: 'StudentScience',
    component: () => import('../views/student/Science.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' }
  },
  {
    path: '/student/science/empower',
    redirect: '/student/science/empower/list'
  },
  {
    path: '/student/science/empower/list',
    name: 'StudentScienceEmpowerList',
    component: () => import('../views/student/ScienceEmpowerList.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' }
  },
  {
    path: '/student/science/empower/:id',
    name: 'StudentScienceEmpowerWatch',
    component: () => import('../views/student/ScienceEmpowerWatch.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' }
  },
  {
    path: '/student/science/news/list',
    name: 'StudentScienceNewsList',
    component: () => import('../views/student/ScienceModuleList.vue'),
    meta: { requiresAuth: true, role: 'STUDENT', module: 'news' }
  },
  {
    path: '/student/science/news/:id',
    name: 'StudentScienceNewsWatch',
    component: () => import('../views/student/ScienceModuleWatch.vue'),
    meta: { requiresAuth: true, role: 'STUDENT', module: 'news' }
  },
  {
    path: '/student/science/knowledge/list',
    name: 'StudentScienceKnowledgeList',
    component: () => import('../views/student/ScienceModuleList.vue'),
    meta: { requiresAuth: true, role: 'STUDENT', module: 'knowledge' }
  },
  {
    path: '/student/science/knowledge/:id',
    name: 'StudentScienceKnowledgeWatch',
    component: () => import('../views/student/ScienceModuleWatch.vue'),
    meta: { requiresAuth: true, role: 'STUDENT', module: 'knowledge' }
  },
  {
    path: '/teacher/home',
    name: 'TeacherHome',
    component: () => import('../views/teacher/Home.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' }
  },
  {
    path: '/teacher/science',
    name: 'TeacherScience',
    component: () => import('../views/teacher/Science.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' }
  },
  {
    path: '/teacher/science/empower',
    redirect: '/teacher/science/empower/list'
  },
  {
    path: '/teacher/science/empower/list',
    name: 'TeacherScienceEmpowerList',
    component: () => import('../views/teacher/ScienceEmpowerList.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' }
  },
  {
    path: '/teacher/science/empower/:id',
    name: 'TeacherScienceEmpowerWatch',
    component: () => import('../views/teacher/ScienceEmpowerWatch.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' }
  },
  {
    path: '/teacher/science/news/list',
    name: 'TeacherScienceNewsList',
    component: () => import('../views/teacher/ScienceModuleList.vue'),
    meta: { requiresAuth: true, role: 'TEACHER', module: 'news' }
  },
  {
    path: '/teacher/science/news/:id',
    name: 'TeacherScienceNewsWatch',
    component: () => import('../views/teacher/ScienceModuleWatch.vue'),
    meta: { requiresAuth: true, role: 'TEACHER', module: 'news' }
  },
  {
    path: '/teacher/science/knowledge/list',
    name: 'TeacherScienceKnowledgeList',
    component: () => import('../views/teacher/ScienceModuleList.vue'),
    meta: { requiresAuth: true, role: 'TEACHER', module: 'knowledge' }
  },
  {
    path: '/teacher/science/knowledge/:id',
    name: 'TeacherScienceKnowledgeWatch',
    component: () => import('../views/teacher/ScienceModuleWatch.vue'),
    meta: { requiresAuth: true, role: 'TEACHER', module: 'knowledge' }
  },
  {
    path: '/teacher/thought',
    name: 'TeacherThought',
    component: () => import('../views/teacher/Thought.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' }
  },
  {
    path: '/teacher/thought/list',
    name: 'TeacherThoughtList',
    component: () => import('../views/teacher/ThoughtList.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' }
  },
  {
    path: '/teacher/manage',
    name: 'TeacherManage',
    component: () => import('../views/teacher/Manage.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' }
  },
  {
    path: '/teacher/discussion/:id',
    name: 'TeacherDiscussion',
    component: () => import('../views/teacher/Discussion.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' }
  },
  {
    path: '/teacher/task/:id',
    name: 'TeacherTask',
    component: () => import('../views/teacher/Task.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' }
  },
  {
    path: '/teacher/news/list',
    name: 'TeacherNewsList',
    component: () => import('../views/teacher/NewsList.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' }
  },
  {
    path: '/teacher/news/:id',
    name: 'TeacherNewsDetail',
    component: () => import('../views/teacher/NewsDetail.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' }
  },
  {
    path: '/teacher/material/:id',
    name: 'TeacherMaterialDetail',
    component: () => import('../views/MaterialDetail.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' }
  },
  {
    path: '/teacher/forum',
    name: 'TeacherForum',
    component: () => import('../views/teacher/Forum.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' }
  },
  {
    path: '/teacher/topic/:code',
    name: 'TeacherTopic',
    component: () => import('../views/teacher/Topic.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' }
  },
  {
    path: '/teacher/leader',
    name: 'TeacherLeader',
    component: () => import('../views/teacher/Leader.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' }
  },
  {
    path: '/teacher/leader/:id',
    name: 'TeacherLeaderDetail',
    component: () => import('../views/teacher/LeaderDetail.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' }
  },
  {
    path: '/admin/home',
    name: 'AdminHome',
    component: () => import('../views/admin/Home.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth) {
    const user = localStorage.getItem('user')
    if (!user) {
      next('/login')
    } else {
      let userData
      try {
        userData = JSON.parse(user)
      } catch (e) {
        localStorage.removeItem('user')
        next('/login')
        return
      }
      if (!userData || !userData.role) {
        localStorage.removeItem('user')
        next('/login')
        return
      }
      if (to.meta.role && userData.role !== to.meta.role) {
        next('/login')
      } else {
        next()
      }
    }
  } else {
    next()
  }
})

export default router