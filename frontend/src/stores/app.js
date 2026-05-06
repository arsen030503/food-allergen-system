import { defineStore } from 'pinia'
import * as allergenApi from '../api/allergen'
import * as authApi from '../api/auth'
import { useAuthStore } from './auth'
import { getAllergenEmoji, parseUserAllergens, toCsv } from '../utils/allergens'

export const useAppStore = defineStore('app', {
  state: () => ({
    allAllergens: [],
    userAllergens: [],
    scanHistory: [],
    scanStats: { totalScans: 0, safeScans: 0, userAllergenCount: 0 },
    currentFilter: 'ALL',
    analyzing: false,
    analyzeResult: null,
    quickScanDraft: '',
  }),
  getters: {
    filteredAllergens: (state) =>
      state.currentFilter === 'ALL'
        ? state.allAllergens
        : state.allAllergens.filter((a) => a.standard === state.currentFilter),
    totalScans: (state) => state.scanStats.totalScans,
    safeScans: (state) => state.scanStats.safeScans,
  },
  actions: {
    syncFromCurrentUser() {
      const auth = useAuthStore()
      this.userAllergens = parseUserAllergens(auth.user?.myAllergens)
    },
    async hydrateAllergens() {
      if (this.allAllergens.length) return
      this.allAllergens = await allergenApi.getAllergens()
    },
    async hydrateHistory() {
      this.scanHistory = await allergenApi.getHistory()
    },
    async hydrateStats() {
      this.scanStats = await allergenApi.getStats()
    },
    async initialize() {
      this.syncFromCurrentUser()
      await Promise.all([this.hydrateAllergens(), this.hydrateHistory(), this.hydrateStats()])
    },
    async analyzeIngredients(payload) {
      this.analyzing = true
      try {
        this.analyzeResult = await allergenApi.analyze(payload)
        await Promise.all([this.hydrateHistory(), this.hydrateStats()])
        return this.analyzeResult
      } finally {
        this.analyzing = false
      }
    },
    clearAnalyzeResult() {
      this.analyzeResult = null
    },
    setQuickScanDraft(value) {
      this.quickScanDraft = value
    },
    setFilter(value) {
      this.currentFilter = value
    },
    async clearHistory() {
      await allergenApi.clearHistory()
      this.scanHistory = []
      await this.hydrateStats()
    },
    async toggleAllergen(name, enabled) {
      if (enabled && !this.userAllergens.includes(name)) {
        this.userAllergens.push(name)
      }
      if (!enabled) {
        this.userAllergens = this.userAllergens.filter((item) => item !== name)
      }

      const csv = toCsv(this.userAllergens)
      await authApi.updateAllergens(csv)

      const auth = useAuthStore()
      if (auth.user) {
        auth.user = { ...auth.user, myAllergens: csv }
      }
      await Promise.all([this.hydrateHistory(), this.hydrateStats()])
    },
    async updateName(fullName) {
      await authApi.updateName(fullName)
      const auth = useAuthStore()
      if (auth.user) {
        auth.user = { ...auth.user, fullName }
      }
    },
    async updateAvatar(avatarData) {
      await authApi.updateAvatar(avatarData)
      const auth = useAuthStore()
      if (auth.user) {
        auth.user = { ...auth.user, avatarData }
      }
    },
    getEmoji(name) {
      return getAllergenEmoji(name)
    },
  },
})
