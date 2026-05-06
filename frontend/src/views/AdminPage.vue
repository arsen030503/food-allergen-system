<template>
  <div class="admin-page">
    <header class="admin-hero">
      <div>
        <h1 class="admin-title">{{ t('admin.title') }}</h1>
        <p class="admin-sub">{{ t('admin.subtitle') }}</p>
      </div>
      <button type="button" class="btn-refresh" :disabled="loading" @click="fetchUsers">↻ {{ t('common.refresh') }}</button>
    </header>

    <div v-if="loading" class="state-msg">{{ t('admin.loadingUsers') }}</div>
    <div v-else-if="error" class="state-msg state-err">{{ error }}</div>

    <div v-else class="user-table-wrap">
      <table class="user-table">
        <thead>
          <tr>
            <th>{{ t('admin.user') }}</th>
            <th>{{ t('admin.role') }}</th>
            <th>{{ t('admin.status') }}</th>
            <th>{{ t('admin.allergens') }}</th>
            <th class="th-actions">{{ t('admin.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.userId" :class="{ rowBlocked: user.blocked }">
            <td class="td-user">
              <div class="user-cell">
                <div class="avatar sm">
                  <img v-if="avatarSrc(user)" :src="avatarSrc(user)" alt="" />
                  <svg v-else viewBox="0 0 24 24"><use href="#ic-user" /></svg>
                </div>
                <div>
                  <div class="name">{{ user.fullName }}</div>
                  <div class="email">{{ user.email }}</div>
                </div>
              </div>
            </td>
            <td>
              <select
                class="role-select"
                :value="user.role"
                :disabled="user.userId === auth.user?.userId"
                @change="changeRole(user.userId, $event.target.value)"
              >
                <option value="USER">USER</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </td>
            <td>
              <span v-if="user.blocked" class="pill pill-bad">{{ t('admin.blocked') }}</span>
              <span v-else class="pill pill-ok">{{ t('admin.active') }}</span>
            </td>
            <td class="td-allergens"><span class="allergen-preview">{{ user.myAllergens || '-' }}</span></td>
            <td class="td-actions">
              <button type="button" class="btn-mini" @click="openEdit(user)">{{ t('admin.edit') }}</button>
              <button type="button" class="btn-mini" @click="openHistory(user)">{{ t('admin.history') }}</button>
              <button type="button" class="btn-mini btn-warn" @click="resetPassword(user)">{{ t('admin.resetPassword') }}</button>
              <button
                v-if="user.role !== 'ADMIN'"
                type="button"
                class="btn-mini"
                @click="toggleBlock(user)"
              >
                {{ user.blocked ? t('admin.unblock') : t('admin.block') }}
              </button>
              <button
                type="button"
                class="btn-mini btn-danger"
                :disabled="user.userId === auth.user?.userId"
                @click="removeUser(user.userId)"
              >
                {{ t('common.remove') }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Edit user -->
    <Teleport to="body">
      <div v-if="editingUser" class="modal-overlay" @click.self="closeEdit">
        <div class="modal panel">
          <h2>{{ t('admin.editUser') }}</h2>
          <form class="modal-form" @submit.prevent="saveUser">
            <label class="field">
              <span>{{ t('admin.fullName') }}</span>
              <input v-model="editForm.fullName" type="text" required />
            </label>
            <label class="field">
              <span>{{ t('admin.allergensCsv') }}</span>
              <textarea v-model="editForm.myAllergens" rows="4"></textarea>
            </label>
            <div class="modal-actions">
              <button type="button" class="btn-secondary" @click="closeEdit">{{ t('common.cancel') }}</button>
              <button type="submit" class="btn-primary">{{ t('common.save') }}</button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>

    <!-- History drawer -->
    <Teleport to="body">
      <div v-if="historyUser" class="modal-overlay" @click.self="closeHistory">
        <div class="modal panel wide">
          <div class="hist-head">
            <h2>{{ t('admin.scanHistory') }} - {{ historyUser.fullName }}</h2>
            <div class="hist-actions">
              <button type="button" class="btn-secondary btn-sm" @click="clearHist">{{ t('admin.clearAll') }}</button>
              <button type="button" class="btn-secondary btn-sm" @click="closeHistory">{{ t('common.close') }}</button>
            </div>
          </div>
          <div v-if="histLoading" class="state-msg">{{ t('common.loading') }}</div>
          <ul v-else class="hist-list">
            <li v-for="row in histRows" :key="row.id" class="hist-row">
              <div>
                <strong>{{ row.productName || t('admin.product') }}</strong>
                <div class="hist-ing">{{ row.ingredients }}</div>
              </div>
              <button type="button" class="btn-mini btn-danger" @click="removeHistRow(row.id)">{{ t('admin.delete') }}</button>
            </li>
            <li v-if="!histRows.length" class="state-msg">{{ t('admin.noScans') }}</li>
          </ul>
        </div>
      </div>
    </Teleport>

    <!-- Reset password result -->
    <Teleport to="body">
      <div v-if="resetPlain" class="modal-overlay" @click.self="resetPlain = ''">
        <div class="modal panel">
          <h2>{{ t('admin.temporaryPassword') }}</h2>
          <p class="hint">{{ t('admin.copyNow') }}</p>
          <div class="pwd-box">{{ resetPlain }}</div>
          <button type="button" class="btn-primary" @click="copyPwd">{{ t('common.copy') }}</button>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '../stores/auth'
import { useUiStore } from '../stores/ui'
import * as authApi from '../api/auth'

const auth = useAuthStore()
const ui = useUiStore()
const router = useRouter()
const { t } = useI18n()

const users = ref([])
const loading = ref(false)
const error = ref('')
const editingUser = ref(null)
const editForm = ref({ fullName: '', myAllergens: '' })

const historyUser = ref(null)
const histRows = ref([])
const histLoading = ref(false)

const resetPlain = ref('')

function avatarSrc(user) {
  return user.avatarThumbData || user.avatarData || user.avatarFullData || ''
}

const fetchUsers = async () => {
  loading.value = true
  error.value = ''
  try {
    users.value = await authApi.getAllUsers()
  } catch (err) {
    error.value = err.response?.data?.message || err.response?.data?.error || t('admin.failedLoadUsers')
  } finally {
    loading.value = false
  }
}

const removeUser = async (userId) => {
  if (!confirm(t('admin.confirmRemoveUser'))) return
  try {
    await authApi.deleteUser(userId)
    ui.showToast(t('admin.userRemoved'))
    await fetchUsers()
  } catch {
    ui.showToast(t('admin.removeUserFailed'), 'danger')
  }
}

const changeRole = async (userId, role) => {
  try {
    await authApi.updateUserRole(userId, role)
    await fetchUsers()
    ui.showToast(t('admin.roleUpdated'))
  } catch {
    ui.showToast(t('admin.roleUpdateFailed'), 'danger')
  }
}

const openEdit = (user) => {
  editingUser.value = user
  editForm.value = { fullName: user.fullName, myAllergens: user.myAllergens || '' }
}

const closeEdit = () => {
  editingUser.value = null
}

const saveUser = async () => {
  try {
    await authApi.updateUserName(editingUser.value.userId, editForm.value.fullName)
    await authApi.updateUserAllergens(editingUser.value.userId, editForm.value.myAllergens)
    await fetchUsers()
    closeEdit()
    ui.showToast(t('admin.saved'))
  } catch {
    ui.showToast(t('admin.saveFailed'), 'danger')
  }
}

const resetPassword = async (user) => {
  if (!confirm(t('admin.confirmReset', { email: user.email }))) return
  try {
    const res = await authApi.resetUserPassword(user.userId)
    resetPlain.value = res.temporaryPassword
    ui.showToast(t('admin.passwordReset'))
  } catch (e) {
    ui.showToast(e.response?.data?.message || e.response?.data?.error || t('admin.resetFailed'), 'danger')
  }
}

const copyPwd = async () => {
  try {
    await navigator.clipboard.writeText(resetPlain.value)
    ui.showToast(t('admin.copied'))
  } catch {
    ui.showToast(t('admin.copyFailed'), 'danger')
  }
}

const toggleBlock = async (user) => {
  try {
    await authApi.setUserBlocked(user.userId, !user.blocked)
    await fetchUsers()
    ui.showToast(user.blocked ? t('admin.unblocked') : t('admin.blockedToast'))
  } catch (e) {
    ui.showToast(e.response?.data?.message || e.response?.data?.error || t('admin.failedShort'), 'danger')
  }
}

const openHistory = async (user) => {
  historyUser.value = user
  histLoading.value = true
  histRows.value = []
  try {
    histRows.value = await authApi.getUserHistory(user.userId)
  } catch {
    ui.showToast(t('admin.couldNotLoadHistory'), 'danger')
  } finally {
    histLoading.value = false
  }
}

const closeHistory = () => {
  historyUser.value = null
  histRows.value = []
}

const clearHist = async () => {
  if (!historyUser.value || !confirm(t('admin.confirmClearHistory'))) return
  try {
    await authApi.clearUserHistory(historyUser.value.userId)
    histRows.value = []
    ui.showToast(t('admin.historyCleared'))
  } catch {
    ui.showToast(t('admin.failedShort'), 'danger')
  }
}

const removeHistRow = async (id) => {
  if (!historyUser.value || !confirm(t('admin.confirmDeleteScan'))) return
  try {
    await authApi.deleteUserHistoryEntry(historyUser.value.userId, id)
    histRows.value = histRows.value.filter((r) => r.id !== id)
    ui.showToast(t('admin.deleted'))
  } catch {
    ui.showToast(t('admin.failedShort'), 'danger')
  }
}

onMounted(async () => {
  if (!auth.isAdmin) {
    await router.push({ name: 'dashboard' })
    return
  }
  fetchUsers()
})
</script>

<style scoped>
.admin-page {
  padding: 32px 40px 48px;
  max-width: 1400px;
  margin: 0 auto;
  font-family: 'Inter', 'Geist', var(--font-sans), system-ui, sans-serif;
}
.admin-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 28px;
  gap: 16px;
}
.admin-title {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0 0 4px;
  letter-spacing: -0.02em;
}
.admin-sub {
  margin: 0;
  color: var(--text-300, #64748b);
  font-size: 0.95rem;
}
.btn-refresh {
  border-radius: 10px;
  border: 1px solid var(--border, #e2e8f0);
  background: var(--surface-elevated, #fff);
  padding: 10px 16px;
  cursor: pointer;
  font-weight: 600;
}
.btn-refresh:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.state-msg {
  padding: 24px;
  text-align: center;
  color: var(--text-300);
}
.state-err {
  color: #b91c1c;
}
.user-table-wrap {
  overflow-x: auto;
  border-radius: 16px;
  border: 1px solid var(--border, #e2e8f0);
  background: var(--surface-elevated, #fff);
  box-shadow: 0 4px 24px rgba(15, 23, 42, 0.06);
}
.user-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
}
.user-table th {
  text-align: left;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border, #e2e8f0);
  color: var(--text-300);
  font-weight: 600;
  text-transform: uppercase;
  font-size: 0.7rem;
  letter-spacing: 0.06em;
}
.user-table td {
  padding: 14px 16px;
  border-bottom: 1px solid var(--border, #f1f5f9);
  vertical-align: middle;
}
.rowBlocked {
  opacity: 0.75;
  background: #fef2f2;
}
.td-user {
  min-width: 220px;
}
.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  background: linear-gradient(145deg, #e0f2fe, #dbeafe);
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatar.sm img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.avatar.sm svg {
  width: 22px;
  height: 22px;
  opacity: 0.6;
}
.name {
  font-weight: 600;
  color: #0f172a;
}
.email {
  font-size: 0.8rem;
  color: #64748b;
}
.role-select {
  border-radius: 8px;
  border: 1px solid #cbd5e1;
  padding: 6px 10px;
  font-size: 0.8rem;
}
.pill {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
}
.pill-ok {
  background: #ecfdf5;
  color: #047857;
}
.pill-bad {
  background: #fef2f2;
  color: #b91c1c;
}
.td-allergens {
  max-width: 200px;
}
.allergen-preview {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #475569;
  font-size: 0.8rem;
}
.th-actions {
  width: 280px;
}
.td-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.btn-mini {
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  padding: 6px 10px;
  font-size: 0.75rem;
  cursor: pointer;
  font-weight: 500;
}
.btn-mini:hover {
  background: #f1f5f9;
}
.btn-warn {
  border-color: #fcd34d;
  background: #fffbeb;
}
.btn-danger {
  border-color: #fecaca;
  background: #fef2f2;
  color: #b91c1c;
}
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  backdrop-filter: blur(4px);
}
.modal.panel {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  max-width: 440px;
  width: 100%;
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.15);
}
.modal.panel.wide {
  max-width: 560px;
  max-height: 85vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.modal h2 {
  margin: 0 0 16px;
  font-size: 1.15rem;
}
.modal-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.field span {
  display: block;
  font-size: 0.8rem;
  font-weight: 600;
  margin-bottom: 6px;
  color: #475569;
}
.field input,
.field textarea {
  width: 100%;
  box-sizing: border-box;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  padding: 10px 12px;
  font: inherit;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 8px;
}
.btn-primary {
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #059669, #10b981);
  color: #fff;
  padding: 10px 18px;
  font-weight: 600;
  cursor: pointer;
}
.btn-secondary {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #fff;
  padding: 10px 18px;
  cursor: pointer;
  font-weight: 500;
}
.btn-sm {
  padding: 8px 12px;
  font-size: 0.8rem;
}
.hist-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-shrink: 0;
}
.hist-actions {
  display: flex;
  gap: 8px;
}
.hist-list {
  list-style: none;
  margin: 16px 0 0;
  padding: 0;
  overflow-y: auto;
  flex: 1;
}
.hist-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
  font-size: 0.85rem;
}
.hist-ing {
  color: #64748b;
  margin-top: 4px;
  max-height: 48px;
  overflow: hidden;
}
.pwd-box {
  font-family: ui-monospace, monospace;
  background: #f1f5f9;
  padding: 14px;
  border-radius: 10px;
  margin: 12px 0;
  word-break: break-all;
  font-size: 0.9rem;
}
.hint {
  font-size: 0.85rem;
  color: #64748b;
  margin: 0 0 8px;
}
@media (max-width: 900px) {
  .user-table .td-actions {
    flex-direction: column;
  }
}
</style>
