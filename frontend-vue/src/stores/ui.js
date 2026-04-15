import { defineStore } from 'pinia'

export const useUiStore = defineStore('ui', {
  state: () => ({
    toastMessage: '',
    toastType: '',
  }),
  actions: {
    showToast(message, type = '') {
      this.toastMessage = message
      this.toastType = type
      clearTimeout(this._toastTimer)
      this._toastTimer = setTimeout(() => {
        this.toastMessage = ''
        this.toastType = ''
      }, 3000)
    },
  },
})

