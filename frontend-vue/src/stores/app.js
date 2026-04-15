import { defineStore } from 'pinia'
import * as allergenApi from '../api/allergen'
import * as authApi from '../api/auth'
import { useAuthStore } from './auth'
import { getAllergenEmoji, parseUserAllergens, toCsv } from '../utils/allergens'

function normalizeHistoryItem(item) {
  const names = (item.detectedAllergens || '')
    .split(',')
    .map((value) => value.trim())
    .filter(Boolean)
  return {
    id: item.id,
    productName: item.productName,
    ingredients: item.ingredients,
    scannedAt: item.scannedAt,
    result: {
      detectedAllergens: names.map((name) => ({ allergen: name })),
    },
  }
}

export const useAppStore = defineStore('app', {
  state: () => ({
    allAllergens: [],
    userAllergens: [],
    scanHistory: [],
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
    totalScans: (state) => state.scanHistory.length,
    safeScans: (state) =>
      state.scanHistory.filter((item) => {
        const found = item.result?.detectedAllergens || []
        return found.filter((a) => state.userAllergens.includes(a.allergen)).length === 0
      }).length,
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
      const rows = await allergenApi.getHistory()
      this.scanHistory = rows.map(normalizeHistoryItem)
    },
    async initialize() {
      this.syncFromCurrentUser()
      await Promise.all([this.hydrateAllergens(), this.hydrateHistory()])
    },
    async analyzeIngredients(payload) {
      this.analyzing = true
      try {
        this.analyzeResult = await allergenApi.analyze(payload)
        await this.hydrateHistory()
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


