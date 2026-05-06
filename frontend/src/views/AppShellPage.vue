<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useAppStore } from '../stores/app'
import { useUiStore } from '../stores/ui'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const app = useAppStore()
const ui = useUiStore()

const sidebarCollapsed = ref(false)

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
    ui.showToast('Failed to load dashboard data', 'danger')
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
    { name: 'dashboard', label: 'Dashboard', icon: 'ic-home' },
    { name: 'analyze', label: 'Scan Ingredients', icon: 'ic-scan' },
    { name: 'history', label: 'Scan History', icon: 'ic-history' },
    { name: 'allergens', label: 'Allergen Database', icon: 'ic-db' },
    { name: 'profile', label: 'My Profile', icon: 'ic-profile' },
  ]
  if (auth.isAdmin) {
    items.push({ name: 'admin', label: 'Admin Panel', icon: 'ic-admin' })
  }
  return items
})

const userName = computed(() => auth.user?.fullName || 'User')
const userEmail = computed(() => auth.user?.email || '...')
const avatarData = computed(
  () => auth.user?.avatarThumbData || auth.user?.avatarData || auth.user?.avatarFullData || ''
)

async function signOut() {
  await auth.signOut()
  app.scanHistory = []
  app.userAllergens = []
  ui.showToast('Signed out successfully')
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
    ui.showToast('Image too large - max 5 MB', 'danger')
    event.target.value = ''
    return
  }
  try {
    const dataUrl = await readAsDataUrl(file)
    await app.updateAvatar(dataUrl)
    ui.showToast('Profile photo updated')
  } catch {
    ui.showToast('Failed to upload avatar', 'danger')
  } finally {
    event.target.value = ''
  }
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
        aria-label="Свернуть меню"
        title="Свернуть"
        @click.stop="collapseSidebar"
      >
        <svg viewBox="0 0 24 24" class="sidebar-close-icon"><use href="#ic-close"/></svg>
      </button>

      <div class="sidebar-brand">
        <div class="brand-logo">
          <svg width="28" height="28" fill="none" stroke="rgba(255,255,255,.9)" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><use href="#ic-flask"/></svg>
        </div>
        <div class="brand-name" v-if="sidebarExpanded">AllerScan</div>
        <div class="brand-sub" v-if="sidebarExpanded">Allergen Detection System</div>
      </div>

      <div class="sidebar-user">
        <div class="user-avatar-wrap" title="Change photo" @click="triggerAvatarUpload">
          <div class="user-avatar">
            <img v-if="avatarData" :src="avatarData" alt="avatar" />
            <svg v-else class="avatar-svg" viewBox="0 0 24 24"><use href="#ic-user"/></svg>
          </div>
          <div class="avatar-overlay"><svg viewBox="0 0 24 24"><use href="#ic-camera"/></svg></div>
        </div>
        <input id="avatar-input" type="file" accept="image/*" style="display:none" @change="handleAvatarUpload" />
        <div v-if="sidebarExpanded" class="user-name">{{ userName }}</div>
        <div v-if="sidebarExpanded" class="user-email">{{ userEmail }}</div>
        <div v-if="sidebarExpanded" class="user-stats">
          <div class="user-stat">
            <span class="user-stat-n">{{ app.userAllergens.length }}</span>
            <span class="user-stat-l">Allergens</span>
          </div>
          <div class="user-stat">
            <span class="user-stat-n">{{ app.totalScans }}</span>
            <span class="user-stat-l">Scans</span>
          </div>
          <div class="user-stat">
            <span class="user-stat-n">{{ app.safeScans }}</span>
            <span class="user-stat-l">Safe</span>
          </div>
        </div>
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
        <button class="nav-item nav-item-danger" @click="signOut" :title="sidebarCollapsed ? 'Sign Out' : ''">
          <svg class="nav-icon" viewBox="0 0 24 24"><use href="#ic-logout"/></svg>
          <span v-if="sidebarExpanded">Sign Out</span>
        </button>
      </div>
    </aside>

    <!-- MAIN -->
    <main class="main">
      <RouterView />
    </main>

  </div>
</template>

<style scoped>
/* Layout shell only; sidebar/main/nav rules come from /public/css/app.css */
.app-shell {
  display: flex;
  min-height: 100vh;
  width: 100%;
}
</style>