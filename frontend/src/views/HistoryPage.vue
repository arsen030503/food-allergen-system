<script setup>
import { onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAppStore } from '../stores/app'
import { useUiStore } from '../stores/ui'
import { getTimeAgo } from '../utils/time'

const app = useAppStore()
const ui = useUiStore()
const { t } = useI18n()

onMounted(async () => {
  try {
    await app.hydrateHistory()
  } catch {
    ui.showToast(t('history.loadFailed'), 'danger')
  }
})

function profileMatchCount(item) {
  return item.profileMatchCount ?? 0
}

function isHistorySafe(item) {
  return item.safeForUser === true
}

async function clearHistory() {
  if (!window.confirm(t('history.confirmClear'))) return
  try {
    await app.clearHistory()
    ui.showToast(t('history.cleared'))
  } catch {
    ui.showToast(t('history.clearFailed'), 'danger')
  }
}

async function removeEntry(item) {
  if (!window.confirm(t('history.confirmRemove'))) return
  try {
    await app.deleteHistoryEntry(item.id)
    ui.showToast(t('history.removed'))
  } catch {
    ui.showToast(t('history.removeFailed'), 'danger')
  }
}
</script>

<template>
  <div class="history-wrapper">
    <div class="history-page">
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:8px">
        <h1 class="page-title">{{ t('history.title') }}</h1>
        <button class="btn btn-outline btn-sm" @click="clearHistory">🗑 {{ t('history.clearAll') }}</button>
      </div>
      <p class="page-sub">{{ t('history.subtitle') }}</p>

      <div class="history-grid" v-if="app.scanHistory.length">
        <div v-for="item in app.scanHistory" :key="item.id" class="history-card">
          <div class="history-icon" :class="isHistorySafe(item) ? 'safe' : profileMatchCount(item) > 2 ? 'danger' : 'warning'">
            <svg viewBox="0 0 24 24"><use :href="isHistorySafe(item) ? '#ic-shield' : '#ic-alert'"/></svg>
          </div>
          <div class="history-info">
            <div style="font-weight:700;font-size:15px">{{ item.productName || t('common.unnamedProduct') }}</div>
            <div class="history-ingredients">{{ item.ingredients }}</div>
            <div class="history-time">{{ getTimeAgo(item.scannedAt, t) }}</div>
          </div>
          <div>
            <div class="history-count" :class="isHistorySafe(item) ? 'safe' : 'danger'">{{ profileMatchCount(item) }}</div>
            <div style="font-size:10px;font-family:var(--font-mono);color:var(--text-300);text-align:center">{{ t('history.allergens') }}</div>
          </div>
          <button type="button" class="btn btn-ghost btn-sm history-remove" :title="t('history.removeTitle')" @click.stop="removeEntry(item)">✕</button>
          <span class="badge" :class="isHistorySafe(item) ? 'badge-safe' : profileMatchCount(item) > 2 ? 'badge-danger' : 'badge-warning'">
            {{ isHistorySafe(item) ? `✓ ${t('history.safe')}` : profileMatchCount(item) > 2 ? `⚠ ${t('history.danger')}` : `⚡ ${t('history.caution')}` }}
          </span>
        </div>
      </div>

      <div v-else class="empty-state">
        <span class="empty-icon">📋</span>
        <div class="empty-title">{{ t('history.noScansTitle') }}</div>
        <p style="font-size:13px;color:var(--text-300)">{{ t('history.noScansSub') }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.history-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  width: 100%;
  min-height: 100vh;
  background: var(--surface);
}
.history-page {
  padding: 48px;
  flex: 1;
}
.history-remove {
  align-self: center;
  padding: 4px 10px;
  border-radius: 8px;
  border: 1px solid var(--border, #e5e7eb);
  background: transparent;
  color: var(--text-300, #6b7280);
  cursor: pointer;
  font-size: 14px;
  line-height: 1;
}
.history-remove:hover {
  background: rgba(239, 68, 68, 0.08);
  color: #b91c1c;
  border-color: #fecaca;
}
</style>