import { createRouter, createWebHistory } from 'vue-router'
import AuthPage from '../views/AuthPage.vue'
import AppShellPage from '../views/AppShellPage.vue'
import DashboardPage from '../views/DashboardPage.vue'
import AnalyzePage from '../views/AnalyzePage.vue'
import HistoryPage from '../views/HistoryPage.vue'
import AllergensPage from '../views/AllergensPage.vue'
import ProfilePage from '../views/ProfilePage.vue'
import AdminPage from '../views/AdminPage.vue'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/',
    redirect: '/app/dashboard',
  },
  {
    path: '/auth',
    name: 'auth',
    component: AuthPage,
    meta: { guestOnly: true },
  },
  {
    path: '/app',
    component: AppShellPage,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: { name: 'dashboard' },
      },
      {
        path: 'dashboard',
        name: 'dashboard',
        component: DashboardPage,
      },
      {
        path: 'analyze',
        name: 'analyze',
        component: AnalyzePage,
      },
      {
        path: 'history',
        name: 'history',
        component: HistoryPage,
      },
      {
        path: 'allergens',
        name: 'allergens',
        component: AllergensPage,
      },
      {
        path: 'profile',
        name: 'profile',
        component: ProfilePage,
      },
      {
        path: 'admin',
        name: 'admin',
        component: AdminPage,
        meta: { requiresAdmin: true },
      },
    ],
  },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})

let bootstrapped = false

router.beforeEach(async (to) => {
  const auth = useAuthStore()

  if (!bootstrapped) {
    await auth.bootstrapSession()
    bootstrapped = true
  }

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'auth' }
  }

  if (to.meta.guestOnly && auth.isAuthenticated) {
    return { name: 'dashboard' }
  }

  if (to.meta.requiresAdmin && !auth.isAdmin) {
    return { name: 'dashboard' }
  }

  return true
})
