import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '@/pages/HomePage.vue'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import UserManagePage from '@/pages/admin/UserManagePage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: HomePage,
    },
    {
      path: '/create',
      name: 'Create Article',
      component: () => import('@/pages/article/ArticleCreatePage.vue'),
    },
    {
      path: '/article/list',
      name: 'Article List',
      component: () => import('@/pages/article/ArticleListPage.vue'),
    },
    {
      path: '/article/:taskId',
      name: 'Article Detail',
      component: () => import('@/pages/article/ArticleDetailPage.vue'),
    },
    {
      path: '/user/login',
      name: 'Login',
      component: UserLoginPage,
    },
    {
      path: '/user/register',
      name: 'Register',
      component: UserRegisterPage,
    },
    {
      path: '/admin/userManage',
      name: 'User Management',
      component: UserManagePage,
    },
    {
      path: '/admin/statistics',
      name: 'Analytics',
      component: () => import('@/pages/admin/StatisticsPage.vue'),
    },
    {
      path: '/vip',
      name: 'Membership',
      component: () => import('@/pages/VipPage.vue'),
    },
  ],
})

export default router
