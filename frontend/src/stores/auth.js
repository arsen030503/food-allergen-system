import { defineStore } from 'pinia'
import * as authApi from '../api/auth'
import { i18n } from '../i18n'

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
    readError(error, fallbackKey = 'errors.requestFailed') {
      return (
        error?.response?.data?.error ||
        error?.response?.data?.message ||
        error?.message ||
        i18n.global.t(fallbackKey)
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
        await authApi.login({ email, password })
        this.user = await authApi.getMe()
        return this.user
      } catch (error) {
        this.error = this.readError(error, 'errors.loginFailed')
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
        await authApi.register({ fullName, email, password })
        this.user = await authApi.getMe()
        return this.user
      } catch (error) {
        this.error = this.readError(error, 'errors.registrationFailed')
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
