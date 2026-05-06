import axios from 'axios'
import { i18n, normalizeLocale, getStoredLocale } from '../i18n'

const apiBase =
  document.querySelector('meta[name="api-base"]')?.content || '/api'

export const http = axios.create({
  baseURL: apiBase,
  withCredentials: true,
})

http.interceptors.request.use((config) => {
  const locale = normalizeLocale(i18n.global.locale.value || getStoredLocale())
  config.headers = config.headers || {}
  config.headers['Accept-Language'] = locale
  return config
})

