import { defineStore } from 'pinia'
import * as allergenApi from '../api/allergen'
import * as authApi from '../api/auth'
import { useAuthStore } from './auth'
import { getAllergenEmoji, parseUserAllergens, toCsv } from '../utils/allergens'
import { getStoredLocale, setLocale as applyLocale } from '../i18n'

const THEME_STORAGE_KEY = 'app-theme'
const SUPPORTED_THEMES = ['light', 'dark']

function normalizeTheme(value) {
  return SUPPORTED_THEMES.includes(value) ? value : 'light'
}

function getStoredTheme() {
  return normalizeTheme(localStorage.getItem(THEME_STORAGE_KEY))
}

function applyTheme(theme) {
  const normalized = normalizeTheme(theme)
  document.documentElement.setAttribute('data-theme', normalized)
  localStorage.setItem(THEME_STORAGE_KEY, normalized)
  return normalized
}

applyTheme(getStoredTheme())

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
    locale: getStoredLocale(),
    theme: getStoredTheme(),
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
    setLocale(locale) {
      this.locale = applyLocale(locale)
    },
    setTheme(theme) {
      this.theme = applyTheme(theme)
    },
    setFilter(value) {
      this.currentFilter = value
    },
    async clearHistory() {
      await allergenApi.clearHistory()
      this.scanHistory = []
      try {
        await this.hydrateStats()
      } catch {
        /* stats failure should not look like clear failed */
      }
    },
    async deleteHistoryEntry(id) {
      await allergenApi.deleteHistoryEntry(id)
      await this.hydrateHistory()
      try {
        await this.hydrateStats()
      } catch {
        /* ignore */
      }
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
      const fresh = await authApi.getMe()
      const auth = useAuthStore()
      if (auth.user) {
        auth.user = fresh
      }
    },
    getEmoji(name) {
      return getAllergenEmoji(name)
    },
  },
})
