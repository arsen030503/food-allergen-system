import { defineStore } from 'pinia'
import * as authApi from '../api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    loading: false,
    error: '',
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.user?.userId),
    isAdmin: (state) => state.user?.role === 'ADMIN',
  },
  actions: {
    readError(error, fallback = 'Request failed') {
      return (
        error?.response?.data?.error ||
        error?.response?.data?.message ||
        error?.message ||
        fallback
      )
    },
    async bootstrapSession() {
      this.loading = true
      this.error = ''
      try {
        this.user = await authApi.getMe()
      } catch (error) {
        this.user = null
        this.error = ''
      } finally {
        this.loading = false
      }
    },
    async signIn(email, password) {
      this.loading = true
      this.error = ''
      try {
        this.user = await authApi.login({ email, password })
        return this.user
      } catch (error) {
        this.error = this.readError(error, 'Login failed')
        throw error
      } finally {
        this.loading = false
      }
    },
    async signUp({ firstName, lastName, email, password }) {
      this.loading = true
      this.error = ''
      try {
        const fullName = `${firstName} ${lastName}`.trim()
        this.user = await authApi.register({ fullName, email, password })
        return this.user
      } catch (error) {
        this.error = this.readError(error, 'Registration failed')
        throw error
      } finally {
        this.loading = false
      }
    },
    async signOut() {
      try {
        await authApi.logout()
      } finally {
        this.user = null
        this.error = ''
      }
    },
  },
})
