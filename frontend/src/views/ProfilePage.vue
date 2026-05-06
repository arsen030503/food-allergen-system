<script setup>
import { computed, ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useAppStore } from '../stores/app'
import { useUiStore } from '../stores/ui'
import * as authApi from '../api/auth'

const auth = useAuthStore()
const app = useAppStore()
const ui = useUiStore()

const editingName = ref(false)
const fullNameInput = ref(auth.user?.fullName || '')

const avatarDisplay = computed(
  () => auth.user?.avatarFullData || auth.user?.avatarData || auth.user?.avatarThumbData || ''
)

const uniqueAllergens = computed(() => {
  const names = app.allAllergens.map((item) => item.name)
  return [...new Set(names)]
})

const memberSince = computed(() => {
  const raw = auth.user?.createdAt
  if (!raw) return '—'
  const date = new Date(raw)
  if (Number.isNaN(date.getTime())) return raw
  return date.toLocaleDateString(undefined, { month: 'long', year: 'numeric', day: 'numeric' })
})

const pwdCurrent = ref('')
const pwdNew = ref('')
const pwdNew2 = ref('')

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
  if (!value) {
    ui.showToast('Name cannot be empty', 'danger')
    return
  }
  try {
    await app.updateName(value)
    editingName.value = false
    ui.showToast('Name updated successfully')
  } catch {
    ui.showToast('Failed to update name', 'danger')
  }
}

async function changePassword() {
  if (pwdNew.value !== pwdNew2.value) {
    ui.showToast('New passwords do not match', 'danger')
    return
  }
  if (pwdNew.value.length < 8) {
    ui.showToast('New password must be at least 8 characters', 'danger')
    return
  }
  try {
    await authApi.changePassword(pwdCurrent.value, pwdNew.value)
    pwdCurrent.value = ''
    pwdNew.value = ''
    pwdNew2.value = ''
    ui.showToast('Password updated')
  } catch (e) {
    ui.showToast(e.response?.data?.message || e.response?.data?.error || 'Failed', 'danger')
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
  <div class="profile-shell">
    <div class="profile-inner">
      <header class="ph">
        <div>
          <p class="ph-kicker">Account</p>
          <h1 class="ph-title">Profile</h1>
          <p class="ph-desc">Photo, security, and your allergen list — in one place.</p>
        </div>
      </header>

      <div class="profile-grid">
        <section class="card card-accent">
          <h2 class="card-title">Photo & identity</h2>
          <div class="hero-avatar" @click="triggerProfileAvatarUpload">
            <img v-if="avatarDisplay" :src="avatarDisplay" alt="Profile" />
            <div v-else class="hero-avatar-placeholder">
              <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2">
                <use href="#ic-user" />
              </svg>
            </div>
            <div class="hero-avatar-badge">
              <svg width="18" height="18" viewBox="0 0 24 24"><use href="#ic-camera" /></svg>
              Update
            </div>
          </div>
          <input id="profile-avatar-input" type="file" accept="image/*" class="sr-only" @change="handleProfileAvatarUpload" />

          <div class="identity-block">
            <div class="identity-name">{{ auth.user?.fullName || '—' }}</div>
            <div class="identity-email">{{ auth.user?.email || '—' }}</div>
            <div class="identity-meta">
              <span class="meta-pill">Member since {{ memberSince }}</span>
              <span v-if="auth.user?.role === 'ADMIN'" class="meta-pill meta-admin">Admin</span>
            </div>
          </div>

          <div class="field-row">
            <label class="label">Display name</label>
            <div class="name-row">
              <input v-model="fullNameInput" class="input" :readonly="!editingName" />
              <template v-if="!editingName">
                <button type="button" class="btn-soft" @click="startEditName">Edit</button>
              </template>
              <template v-else>
                <button type="button" class="btn-primary-inline" @click="saveName">Save</button>
                <button type="button" class="btn-soft" @click="cancelEditName">Cancel</button>
              </template>
            </div>
          </div>
        </section>

        <section class="card">
          <h2 class="card-title">Security</h2>
          <p class="card-sub">Change your password any time.</p>
          <div class="stack">
            <label class="label">Current password</label>
            <input v-model="pwdCurrent" type="password" class="input" autocomplete="current-password" />
            <label class="label">New password</label>
            <input v-model="pwdNew" type="password" class="input" autocomplete="new-password" />
            <label class="label">Confirm new password</label>
            <input v-model="pwdNew2" type="password" class="input" autocomplete="new-password" />
            <button type="button" class="btn-primary-wide" @click="changePassword">Update password</button>
          </div>
        </section>

        <section class="card card-wide">
          <div class="card-head">
            <h2 class="card-title">My allergens</h2>
            <p class="card-sub">Toggle what applies to you — scans will highlight these matches.</p>
          </div>
          <div class="allergen-grid">
            <div
              v-for="name in uniqueAllergens"
              :key="name"
              class="allergen-card"
              :class="{ 'is-on': app.userAllergens.includes(name) }"
            >
              <div class="ac-emoji">
                <svg
                  v-if="app.getEmoji(name).startsWith('<svg')"
                  v-html="app.getEmoji(name)"
                  style="width:28px;height:28px;vertical-align:middle"
                />
                <template v-else>{{ app.getEmoji(name) }}</template>
              </div>
              <div class="ac-body">
                <div class="ac-name">{{ name }}</div>
                <div class="ac-hint">
                  {{ (app.allAllergens.find((item) => item.name === name)?.triggerIngredients || '').split(',').slice(0, 2).join(', ') }}
                </div>
              </div>
              <label class="switch">
                <input :checked="app.userAllergens.includes(name)" type="checkbox" @change="toggle(name, $event.target.checked)" />
                <span class="switch-ui" />
              </label>
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-shell {
  flex: 1;
  min-height: 100vh;
  background: linear-gradient(180deg, var(--surface, #f8fafc) 0%, #eef2f7 100%);
}
.profile-inner {
  max-width: 1100px;
  margin: 0 auto;
  padding: 40px 24px 64px;
  font-family: 'Inter', 'Geist', var(--font-sans), system-ui, sans-serif;
}
.ph {
  margin-bottom: 28px;
}
.ph-kicker {
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: #64748b;
  margin: 0 0 6px;
}
.ph-title {
  font-size: 2rem;
  font-weight: 800;
  letter-spacing: -0.03em;
  margin: 0 0 8px;
  color: #0f172a;
}
.ph-desc {
  margin: 0;
  color: #64748b;
  font-size: 1rem;
  max-width: 480px;
  line-height: 1.5;
}
.profile-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 22px;
  align-items: start;
}
.card {
  background: #fff;
  border-radius: 20px;
  padding: 26px;
  box-shadow: 0 4px 32px rgba(15, 23, 42, 0.07);
  border: 1px solid rgba(15, 23, 42, 0.06);
}
.card-accent {
  background: linear-gradient(145deg, #ffffff 0%, #f0fdf4 100%);
}
.card-wide {
  grid-column: 1 / -1;
}
.card-title {
  font-size: 1.1rem;
  font-weight: 700;
  margin: 0 0 6px;
  color: #0f172a;
}
.card-sub {
  margin: 0 0 18px;
  font-size: 0.9rem;
  color: #64748b;
  line-height: 1.45;
}
.card-head {
  margin-bottom: 18px;
}
.hero-avatar {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 28px;
  overflow: hidden;
  cursor: pointer;
  margin: 0 auto 20px;
  box-shadow: 0 12px 40px rgba(16, 185, 129, 0.25);
}
.hero-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.hero-avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #d1fae5, #a7f3d0);
  color: #065f46;
}
.hero-avatar-badge {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 8px;
  background: rgba(15, 23, 42, 0.72);
  color: #fff;
  font-size: 0.72rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.identity-block {
  text-align: center;
  margin-bottom: 22px;
}
.identity-name {
  font-size: 1.25rem;
  font-weight: 700;
  color: #0f172a;
}
.identity-email {
  font-size: 0.9rem;
  color: #64748b;
  margin-top: 4px;
}
.identity-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  margin-top: 12px;
}
.meta-pill {
  font-size: 0.72rem;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 999px;
  background: #f1f5f9;
  color: #475569;
}
.meta-admin {
  background: #ecfdf5;
  color: #047857;
}
.field-row {
  margin-top: 8px;
}
.label {
  display: block;
  font-size: 0.78rem;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 8px;
}
.name-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
.input {
  flex: 1;
  min-width: 160px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  padding: 10px 14px;
  font: inherit;
  background: #fff;
}
.input:focus {
  outline: none;
  border-color: #34d399;
  box-shadow: 0 0 0 3px rgba(52, 211, 153, 0.2);
}
.btn-soft {
  border: 1px solid #e2e8f0;
  background: #fff;
  border-radius: 10px;
  padding: 8px 14px;
  font-weight: 600;
  font-size: 0.85rem;
  cursor: pointer;
  color: #334155;
}
.btn-primary-inline {
  border: none;
  border-radius: 10px;
  padding: 8px 14px;
  font-weight: 600;
  font-size: 0.85rem;
  cursor: pointer;
  background: linear-gradient(135deg, #059669, #10b981);
  color: #fff;
}
.stack {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.btn-primary-wide {
  margin-top: 8px;
  border: none;
  border-radius: 12px;
  padding: 12px 18px;
  font-weight: 700;
  cursor: pointer;
  background: linear-gradient(135deg, #0f766e, #14b8a6);
  color: #fff;
}
.allergen-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 12px;
}
.allergen-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.allergen-card.is-on {
  border-color: #6ee7b7;
  background: #ecfdf5;
  box-shadow: 0 4px 20px rgba(16, 185, 129, 0.12);
}
.ac-emoji {
  font-size: 26px;
  flex-shrink: 0;
}
.ac-body {
  flex: 1;
  min-width: 0;
}
.ac-name {
  font-weight: 700;
  font-size: 0.95rem;
  color: #0f172a;
}
.ac-hint {
  font-size: 0.75rem;
  color: #94a3b8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 2px;
}
.switch {
  position: relative;
  width: 44px;
  height: 26px;
  flex-shrink: 0;
}
.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}
.switch-ui {
  position: absolute;
  inset: 0;
  background: #cbd5e1;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.2s;
}
.switch-ui::after {
  content: '';
  position: absolute;
  width: 20px;
  height: 20px;
  left: 3px;
  bottom: 3px;
  background: #fff;
  border-radius: 50%;
  transition: transform 0.2s;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
}
.switch input:checked + .switch-ui {
  background: #10b981;
}
.switch input:checked + .switch-ui::after {
  transform: translateX(18px);
}
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  border: 0;
}
@media (max-width: 900px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
