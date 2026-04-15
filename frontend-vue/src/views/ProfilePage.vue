<script setup>
import { computed, ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useAppStore } from '../stores/app'
import { useUiStore } from '../stores/ui'

const auth = useAuthStore()
const app = useAppStore()
const ui = useUiStore()

const editingName = ref(false)
const fullNameInput = ref(auth.user?.fullName || '')

const uniqueAllergens = computed(() => {
  const names = app.allAllergens.map((item) => item.name)
  return [...new Set(names)]
})

const memberSince = computed(() => {
  const raw = auth.user?.createdAt
  if (!raw) return 'April 2026'
  const date = new Date(raw)
  if (Number.isNaN(date.getTime())) return raw
  return date.toLocaleDateString('en-US', { month: 'long', year: 'numeric' })
})

function startEditName() {
  fullNameInput.value = auth.user?.fullName || ''
  editingName.value = true
}

function cancelEditName() {
  fullNameInput.value = auth.user?.fullName || ''
  editingName.value = false
}

function triggerProfileAvatarUpload() {
  const input = document.getElementById('profile-avatar-input')
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

async function handleProfileAvatarUpload(event) {
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

async function saveName() {
  const value = fullNameInput.value.trim()
  if (!value) { ui.showToast('Name cannot be empty', 'danger'); return }
  try {
    await app.updateName(value)
    editingName.value = false
    ui.showToast('Name updated successfully')
  } catch {
    ui.showToast('Failed to update name', 'danger')
  }
}

async function toggle(name, enabled) {
  try {
    await app.toggleAllergen(name, enabled)
    ui.showToast(enabled ? `${name} added to your profile` : `${name} removed from profile`)
  } catch {
    ui.showToast('Failed to update allergens', 'danger')
  }
}
</script>

<template>
  <div class="profile-wrapper">
    <div class="profile-page">
      <div class="profile-header">
        <h1 class="page-title">My Allergen Profile</h1>
        <p class="profile-header-sub">Manage your account and personal allergen sensitivities</p>
      </div>
      <div class="profile-content-grid">
        <div class="profile-section profile-account">
          <div class="profile-section-title">Account Information</div>
          <div class="profile-section-sub">Your profile details and preferences</div>
          <div class="profile-avatar-section">
            <div class="profile-avatar-big" title="Click to change photo" @click="triggerProfileAvatarUpload">
              <div class="profile-avatar-img">
                <img v-if="auth.user?.avatarData" :src="auth.user.avatarData" alt="avatar"/>
                <svg v-else width="48" height="48" fill="none" stroke="rgba(255,255,255,.75)" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><use href="#ic-user"/></svg>
              </div>
              <div class="pa-overlay">
                <svg viewBox="0 0 24 24"><use href="#ic-camera"/></svg>
                <span>Change</span>
              </div>
            </div>
            <input id="profile-avatar-input" type="file" accept="image/*" style="display:none" @change="handleProfileAvatarUpload"/>
            <div class="profile-avatar-meta">
              <div class="profile-avatar-name">{{ auth.user?.fullName || '—' }}</div>
              <div class="profile-avatar-email">{{ auth.user?.email || '—' }}</div>
            </div>
          </div>
          <div class="account-grid">
            <div class="account-field">
              <div class="account-field-label">Full Name</div>
              <div class="account-field-value">
                <input v-model="fullNameInput" :readonly="!editingName" :style="editingName ? 'border-bottom:1.5px solid var(--green-500)' : ''"/>
                <button v-if="!editingName" class="edit-btn" @click="startEditName">Edit</button>
                <button v-if="editingName" class="save-btn" style="display:inline-flex" @click="saveName">Save</button>
                <button v-if="editingName" class="edit-btn" @click="cancelEditName">Cancel</button>
              </div>
            </div>
            <div class="account-field">
              <div class="account-field-label">Email</div>
              <div class="account-field-value">{{ auth.user?.email || '—' }}</div>
            </div>
            <div class="account-field">
              <div class="account-field-label">Member Since</div>
              <div class="account-field-value">{{ memberSince }}</div>
            </div>
            <div class="account-field">
              <div class="account-field-label">Plan</div>
              <div class="account-field-value">🟢 Free Plan</div>
            </div>
          </div>
        </div>
        <div class="profile-section profile-allergens">
          <div class="profile-section-title">My Allergens</div>
          <div class="profile-section-sub">Select allergens you're sensitive to — results will only show these</div>
          <div class="allergens-list">
            <div v-for="name in uniqueAllergens" :key="name" class="allergen-toggle-row" :class="{ 'active-allergen': app.userAllergens.includes(name) }">
              <span class="toggle-emoji"><svg v-if="app.getEmoji(name).startsWith('<svg')" v-html="app.getEmoji(name)" style="width:32px;height:32px;vertical-align:middle"/> <template v-else>{{ app.getEmoji(name) }}</template></span>
              <div class="toggle-info">
                <div class="toggle-name" :class="{ 'active-allergen': app.userAllergens.includes(name) }">{{ name }}</div>
                <div class="toggle-triggers">{{ (app.allAllergens.find((item) => item.name === name)?.triggerIngredients || '').split(',').slice(0, 3).join(', ') }}...</div>
              </div>
              <label class="toggle-switch">
                <input :checked="app.userAllergens.includes(name)" type="checkbox" @change="toggle(name, $event.target.checked)"/>
                <span class="toggle-track"></span>
              </label>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  width: 100%;
  min-height: 100vh;
  background: var(--surface);
  font-family: 'Inter', 'Geist', var(--font-sans), Arial, sans-serif;
}
.profile-page {
  padding: 48px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 32px;
  align-content: start;
}
.profile-header {
  margin-bottom: 0;
}
.profile-header-sub {
  color: var(--text-300);
  font-size: 14px;
}
.profile-content-grid {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 32px;
  align-items: stretch;
}
.profile-section {
  background: rgba(255,255,255,0.97);
  border-radius: 18px;
  box-shadow: 0 2px 16px 0 rgba(0,0,0,0.03);
  padding: 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
}
.profile-account {
  min-width: 0;
}
.profile-allergens {
  min-width: 0;
}
.account-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
  margin-top: 24px;
  width: 100%;
}
.account-field {
  background: #f8fafc;
  border-radius: 12px;
  padding: 18px 16px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 90px;
  box-sizing: border-box;
}
.account-field-label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 6px;
  font-weight: 500;
}
.account-field-value {
  font-size: 16px;
  color: #22223b;
  display: flex;
  align-items: center;
  gap: 10px;
}
.edit-btn, .save-btn {
  margin-left: 8px;
  font-size: 13px;
  padding: 4px 12px;
  border-radius: 8px;
  border: none;
  background: var(--green-100);
  color: var(--green-700);
  cursor: pointer;
  font-weight: 500;
  transition: background .18s;
}
.edit-btn:hover, .save-btn:hover {
  background: var(--green-200);
}
.profile-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 18px;
}
.profile-avatar-big {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: var(--green-100);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  cursor: pointer;
  margin-bottom: 10px;
  overflow: hidden;
}
.profile-avatar-img img, .profile-avatar-img svg {
  width: 48px;
  height: 48px;
  border-radius: 50%;
}
.pa-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  background: rgba(0,0,0,0.32);
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 4px 0;
  border-bottom-left-radius: 50%;
  border-bottom-right-radius: 50%;
}
.profile-avatar-meta {
  text-align: center;
}
.profile-avatar-name {
  font-weight: 600;
  font-size: 16px;
}
.profile-avatar-email {
  font-size: 13px;
  color: #6b7280;
}
.profile-section-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 4px;
  color: #22223b;
}
.profile-section-sub {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 18px;
}
.allergens-list {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-start;
}
.allergen-toggle-row {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #f8fafc;
  border-radius: 12px;
  padding: 14px 18px;
  min-height: 56px;
  width: 100%;
  box-sizing: border-box;
  transition: background .18s;
}
.allergen-toggle-row.active-allergen {
  background: var(--green-50);
}
.toggle-emoji {
  font-size: 32px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.toggle-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.toggle-name {
  font-size: 16px;
  font-weight: 600;
  color: #22223b;
}
.toggle-triggers {
  color: #9ca3af;
  font-size: 13px;
  margin-top: 2px;
}
.toggle-switch {
  display: flex;
  align-items: center;
  gap: 0;
}
.toggle-switch input[type="checkbox"] {
  accent-color: var(--green-500);
  width: 18px;
  height: 18px;
}
.toggle-track {
  display: none;
}
@media (max-width: 900px) {
  .profile-page {
    padding: 20px 8px;
  }
  .profile-content-grid {
    grid-template-columns: 1fr;
    gap: 18px;
  }
}
</style>