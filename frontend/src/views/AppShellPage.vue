<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../stores/auth'
import { useAppStore } from '../stores/app'
import { useUiStore } from '../stores/ui'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const app = useAppStore()
const ui = useUiStore()
const { t } = useI18n()

const sidebarCollapsed = ref(false)
const settingsOpen = ref(false)
const settingsLocale = ref(app.locale)
const settingsTheme = ref(app.theme)

const sidebarExpanded = computed(() => !sidebarCollapsed.value)

onMounted(async () => {
  try {
    await app.initialize()
    const saved = localStorage.getItem('sidebar-collapsed')
    if (saved !== null) {
      try {
        sidebarCollapsed.value = JSON.parse(saved)
      } catch {
        /* ignore */
      }
    } else {
      const pinned = localStorage.getItem('sidebar-pinned')
      if (pinned !== null) {
        try {
          sidebarCollapsed.value = JSON.parse(pinned) !== true
        } catch {
          /* ignore */
        }
        localStorage.removeItem('sidebar-pinned')
      }
    }
  } catch {
    ui.showToast(t('shell.failedLoadDashboard'), 'danger')
  }
})

function expandSidebar() {
  sidebarCollapsed.value = false
  localStorage.setItem('sidebar-collapsed', JSON.stringify(false))
}

function collapseSidebar() {
  sidebarCollapsed.value = true
  localStorage.setItem('sidebar-collapsed', JSON.stringify(true))
}

/** В узком режиме клик по «пустому» месту раскрывает (не по ссылкам, кнопкам, аватару) */
function onSidebarBlankClick(e) {
  if (!sidebarCollapsed.value) return
  if (e.target.closest('a, button, label, input, .user-avatar-wrap')) return
  expandSidebar()
}

const navItems = computed(() => {
  const items = [
    { name: 'dashboard', label: t('nav.dashboard'), icon: 'ic-home' },
    { name: 'analyze', label: t('nav.analyze'), icon: 'ic-scan' },
    { name: 'history', label: t('nav.history'), icon: 'ic-history' },
    { name: 'allergens', label: t('nav.allergens'), icon: 'ic-db' },
    { name: 'profile', label: t('nav.profile'), icon: 'ic-profile' },
  ]
  if (auth.isAdmin) {
    items.push({ name: 'admin', label: t('nav.admin'), icon: 'ic-admin' })
  }
  return items
})

const userName = computed(() => auth.user?.fullName || t('shell.user'))
const userEmail = computed(() => auth.user?.email || '...')
const avatarData = computed(
  () => auth.user?.avatarThumbData || auth.user?.avatarData || auth.user?.avatarFullData || ''
)
const localeOptions = computed(() => [
  { value: 'en', label: t('locale.en') },
  { value: 'ru', label: t('locale.ru') },
  { value: 'kg', label: t('locale.kg') },
])
const themeOptions = computed(() => [
  { value: 'light', label: t('theme.light') },
  { value: 'dark', label: t('theme.dark') },
])

async function signOut() {
  await auth.signOut()
  app.scanHistory = []
  app.userAllergens = []
  ui.showToast(t('shell.signedOut'))
  await router.push({ name: 'auth' })
}

function triggerAvatarUpload() {
  const input = document.getElementById('avatar-input')
  if (input) input.click()
}

function readAsDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => resolve(e.target?.result)
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

async function handleAvatarUpload(event) {
  const file = event.target.files?.[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) {
    ui.showToast(t('shell.imageTooLarge'), 'danger')
    event.target.value = ''
    return
  }
  try {
    const dataUrl = await readAsDataUrl(file)
    await app.updateAvatar(dataUrl)
    ui.showToast(t('shell.photoUpdated'))
  } catch {
    ui.showToast(t('shell.avatarUploadFailed'), 'danger')
  } finally {
    event.target.value = ''
  }
}

function openSettings() {
  settingsLocale.value = app.locale
  settingsTheme.value = app.theme
  settingsOpen.value = true
}

function closeSettings() {
  settingsOpen.value = false
}

function saveSettings() {
  app.setLocale(settingsLocale.value)
  app.setTheme(settingsTheme.value)
  settingsOpen.value = false
  ui.showToast(t('settings.saved'))
}
</script>

<template>
  <div class="app-shell" :class="{ 'sidebar-collapsed': sidebarCollapsed }">

    <!-- SIDEBAR -->
    <aside
      class="sidebar"
      :class="{ collapsed: sidebarCollapsed }"
      @click="onSidebarBlankClick"
    >
      <button
        v-if="sidebarExpanded"
        type="button"
        class="sidebar-close-btn"
        :aria-label="t('shell.collapseMenu')"
        :title="t('shell.collapse')"
        @click.stop="collapseSidebar"
      >
        <svg viewBox="0 0 24 24" class="sidebar-close-icon"><use href="#ic-close"/></svg>
      </button>

      <div class="sidebar-brand">
        <div class="brand-logo">
          <svg width="28" height="28" fill="none" stroke="rgba(255,255,255,.9)" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><use href="#ic-flask"/></svg>
        </div>
        <div class="brand-name" v-if="sidebarExpanded">AllerScan</div>
        <div class="brand-sub" v-if="sidebarExpanded">{{ t('shell.brandSub') }}</div>
      </div>

      <div class="sidebar-user">
        <div class="user-avatar-wrap" :title="t('shell.changePhoto')" @click="triggerAvatarUpload">
          <div class="user-avatar">
            <img v-if="avatarData" :src="avatarData" alt="avatar" />
            <svg v-else class="avatar-svg" viewBox="0 0 24 24"><use href="#ic-user"/></svg>
          </div>
          <div class="avatar-overlay"><svg viewBox="0 0 24 24"><use href="#ic-camera"/></svg></div>
        </div>
        <input id="avatar-input" type="file" accept="image/*" style="display:none" @change="handleAvatarUpload" />
        <div v-if="sidebarExpanded" class="user-name">{{ userName }}</div>
        <div v-if="sidebarExpanded" class="user-email">{{ userEmail }}</div>
      </div>

      <nav class="sidebar-nav">
        <RouterLink
          v-for="item in navItems"
          :key="item.name"
          :to="{ name: item.name }"
          class="nav-item"
          :class="{ active: route.name === item.name }"
          :title="sidebarCollapsed ? item.label : ''"
          :data-label="item.label"
        >
          <svg class="nav-icon" viewBox="0 0 24 24"><use :href="`#${item.icon}`"/></svg>
          <span v-if="sidebarExpanded">{{ item.label }}</span>
        </RouterLink>
      </nav>

      <div class="sidebar-footer">
        <button class="nav-item" @click="openSettings" :title="sidebarCollapsed ? t('settings.title') : ''" :data-label="t('settings.title')">
          <svg class="nav-icon" viewBox="0 0 24 24"><use href="#ic-settings"/></svg>
          <span v-if="sidebarExpanded">{{ t('settings.title') }}</span>
        </button>
        <button class="nav-item nav-item-danger" @click="signOut" :title="sidebarCollapsed ? t('nav.signOut') : ''">
          <svg class="nav-icon" viewBox="0 0 24 24"><use href="#ic-logout"/></svg>
          <span v-if="sidebarExpanded">{{ t('nav.signOut') }}</span>
        </button>
      </div>
    </aside>

    <!-- MAIN -->
    <main class="main">
      <RouterView />
    </main>

    <Teleport to="body">
      <div v-if="settingsOpen" class="settings-overlay" @click.self="closeSettings">
        <div class="settings-modal">
          <h2>{{ t('settings.title') }}</h2>

          <label class="settings-field">
            <span>{{ t('settings.language') }}</span>
            <select v-model="settingsLocale">
              <option v-for="locale in localeOptions" :key="locale.value" :value="locale.value">{{ locale.label }}</option>
            </select>
          </label>

          <label class="settings-field">
            <span>{{ t('settings.theme') }}</span>
            <select v-model="settingsTheme">
              <option v-for="theme in themeOptions" :key="theme.value" :value="theme.value">{{ theme.label }}</option>
            </select>
          </label>

          <div class="settings-actions">
            <button type="button" class="btn btn-outline btn-sm" @click="closeSettings">{{ t('common.cancel') }}</button>
            <button type="button" class="btn btn-primary btn-sm" @click="saveSettings">{{ t('common.save') }}</button>
          </div>
        </div>
      </div>
    </Teleport>

  </div>
</template>

<style scoped>
/* Layout shell only; sidebar/main/nav rules come from /public/css/app.css */
.app-shell {
  display: flex;
  min-height: 100vh;
  width: 100%;
}

.settings-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.settings-modal {
  width: 100%;
  max-width: 420px;
  background: var(--card);
  color: var(--text-900);
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.3);
}

.settings-modal h2 {
  margin-bottom: 16px;
  font-size: 20px;
}

.settings-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 14px;
}

.settings-field span {
  font-size: 13px;
  color: var(--text-500);
}

.settings-field select {
  border-radius: 8px;
  border: 1px solid var(--border);
  background: var(--card);
  color: var(--text-900);
  padding: 8px 10px;
  font-size: 14px;
}

.settings-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>