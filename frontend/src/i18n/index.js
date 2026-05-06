import { createI18n } from "vue-i18n"
import en from "./messages/en"
import ru from "./messages/ru"
import kg from "./messages/kg"

export const LOCALE_STORAGE_KEY = "app-locale"
export const SUPPORTED_LOCALES = ["en", "ru", "kg"]
export const DEFAULT_LOCALE = "en"

export function normalizeLocale(raw) {
  const value = (raw || "").toString().trim().toLowerCase()
  if (SUPPORTED_LOCALES.includes(value)) return value
  if (value.startsWith("en")) return "en"
  if (value.startsWith("ru")) return "ru"
  if (value.startsWith("kg") || value.startsWith("ky")) return "kg"
  return DEFAULT_LOCALE
}

export function getStoredLocale() {
  return normalizeLocale(localStorage.getItem(LOCALE_STORAGE_KEY))
}

export function saveLocale(locale) {
  localStorage.setItem(LOCALE_STORAGE_KEY, normalizeLocale(locale))
}

const initialLocale = getStoredLocale()

export const i18n = createI18n({
  legacy: false,
  locale: initialLocale,
  fallbackLocale: DEFAULT_LOCALE,
  messages: { en, ru, kg },
})

export function setLocale(locale) {
  const normalized = normalizeLocale(locale)
  i18n.global.locale.value = normalized
  saveLocale(normalized)
  return normalized
}

