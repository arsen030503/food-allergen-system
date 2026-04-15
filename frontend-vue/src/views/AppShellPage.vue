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

onMounted(async () => {
  try {
    await app.initialize()
    const saved = localStorage.getItem('sidebar-collapsed')
    if (saved !== null) {
      sidebarCollapsed.value = JSON.parse(saved)
    }
  } catch {
    ui.showToast('Failed to load dashboard data', 'danger')
  }
})

function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
  localStorage.setItem('sidebar-collapsed', JSON.stringify(sidebarCollapsed.value))
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
const avatarData = computed(() => auth.user?.avatarData || '')

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
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-collapse-btn" @click="toggleSidebar" :title="sidebarCollapsed ? 'Expand' : 'Collapse'">
        <svg viewBox="0 0 24 24" :class="{ rotated: !sidebarCollapsed }"><use href="#ic-chevron-left"/></svg>
      </div>

      <div class="sidebar-brand">
        <div class="brand-logo">
          <svg width="28" height="28" fill="none" stroke="rgba(255,255,255,.9)" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><use href="#ic-flask"/></svg>
        </div>
        <div class="brand-name" v-if="!sidebarCollapsed">AllerScan</div>
        <div class="brand-sub" v-if="!sidebarCollapsed">Allergen Detection System</div>
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
        <div v-if="!sidebarCollapsed" class="user-name">{{ userName }}</div>
        <div v-if="!sidebarCollapsed" class="user-email">{{ userEmail }}</div>
        <div v-if="!sidebarCollapsed" class="user-stats">
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
        >
          <svg class="nav-icon" viewBox="0 0 24 24"><use :href="`#${item.icon}`"/></svg>
          <span v-if="!sidebarCollapsed">{{ item.label }}</span>
        </RouterLink>
      </nav>

      <div class="sidebar-footer">
        <button class="nav-item nav-item-danger" @click="signOut" :title="sidebarCollapsed ? 'Sign Out' : ''">
          <svg class="nav-icon" viewBox="0 0 24 24"><use href="#ic-logout"/></svg>
          <span v-if="!sidebarCollapsed">Sign Out</span>
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
.app-shell {
  display: flex;
  min-height: 100vh;
  width: 100%;
}

.sidebar {
  width: var(--sidebar-w, 260px);
  min-height: 100vh;
  background: var(--green-800);
  display: flex;
  flex-direction: column;
  padding: 32px 0;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 100;
  transition: width 0.3s ease;
  flex-shrink: 0;
  font-family: 'Inter', 'Geist', var(--font-sans), Arial, sans-serif;
}

.sidebar.collapsed {
  width: var(--sidebar-w-collapsed, 70px);
  padding: 24px 0;
}

.sidebar-nav {
  padding-left: 0;
  padding: 16px 12px 0 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 14px 11px 24px;
  border-radius: var(--radius-sm);
  color: rgba(229,231,235,0.85);
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all .18s;
  border: none;
  background: none;
  width: 100%;
  text-align: left;
  line-height: 1.2;
  position: relative;
}
.nav-item .nav-icon {
  font-size: 20px;
  width: 28px;
  min-width: 28px;
  text-align: left;
  display: flex;
  align-items: center;
  justify-content: center;
}
.nav-item.active {
  background: rgba(255,255,255,.12);
  color: #fff;
  border-radius: 12px;
  padding-left: 32px;
}
.nav-item:hover {
  background: rgba(255,255,255,.08);
  color: #fff;
}
.nav-item-danger {
  color: #ffb4b4 !important;
  opacity: 0.7;
}
.nav-item-danger:hover {
  background: rgba(255,80,80,0.08) !important;
  color: #ff8080 !important;
}
.sidebar-footer {
  padding: 20px 24px 0 24px;
  border-top: 1px solid rgba(229,231,235,0.12);
  margin-top: 12px;
}
.sidebar-footer .nav-item {
  color: #e5e7eb !important;
  opacity: 0.7;
  font-size: 15px;
  font-weight: 500;
  margin-top: 0;
}
.sidebar-footer .nav-item:hover {
  background: rgba(255,255,255,.08);
  color: #fff !important;
}
.user-name, .user-email, .user-stat-l {
  color: #e5e7eb;
}
.user-name {
  font-weight: 600;
  font-size: 16px;
}
.user-email {
  font-size: 13px;
  opacity: 0.7;
}

.main {
  margin-left: var(--sidebar-w, 260px);
  flex: 1;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  transition: margin-left 0.3s ease;
  width: calc(100% - var(--sidebar-w, 260px));
  overflow-x: hidden;
}

.app-shell.sidebar-collapsed .main {
  margin-left: var(--sidebar-w-collapsed, 70px);
  width: calc(100% - var(--sidebar-w-collapsed, 70px));
}

@media (max-width: 900px) {
  .sidebar {
    display: none;
  }
  .main {
    margin-left: 0;
    width: 100%;
  }
}
</style>