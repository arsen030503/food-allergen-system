<script setup>
import { useAppStore } from '../stores/app'
import { useUiStore } from '../stores/ui'
import { getTimeAgo } from '../utils/time'

const app = useAppStore()
const ui = useUiStore()

function myMatches(item) {
  const found = item.result?.detectedAllergens || []
  return found.filter((a) => app.userAllergens.includes(a.allergen)).length
}

async function clearHistory() {
  if (!window.confirm('Clear all scan history?')) return
  try {
    await app.clearHistory()
    ui.showToast('History cleared')
  } catch {
    ui.showToast('Failed to clear history', 'danger')
  }
}
</script>

<template>
  <div class="history-wrapper">
    <div class="history-page">
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:8px">
        <h1 class="page-title">Scan History</h1>
        <button class="btn btn-outline btn-sm" @click="clearHistory">🗑 Clear All</button>
      </div>
      <p class="page-sub">Your last 10 ingredient scans</p>

      <div class="history-grid" v-if="app.scanHistory.length">
        <div v-for="item in app.scanHistory" :key="item.id" class="history-card">
          <div class="history-icon" :class="myMatches(item) === 0 ? 'safe' : myMatches(item) > 2 ? 'danger' : 'warning'">
            <svg viewBox="0 0 24 24"><use :href="myMatches(item) === 0 ? '#ic-shield' : '#ic-alert'"/></svg>
          </div>
          <div class="history-info">
            <div style="font-weight:700;font-size:15px">{{ item.productName || 'Unnamed Product' }}</div>
            <div class="history-ingredients">{{ item.ingredients }}</div>
            <div class="history-time">{{ getTimeAgo(item.scannedAt) }}</div>
          </div>
          <div>
            <div class="history-count" :class="myMatches(item) === 0 ? 'safe' : 'danger'">{{ myMatches(item) }}</div>
            <div style="font-size:10px;font-family:var(--font-mono);color:var(--text-300);text-align:center">allergens</div>
          </div>
          <span class="badge" :class="myMatches(item) === 0 ? 'badge-safe' : myMatches(item) > 2 ? 'badge-danger' : 'badge-warning'">
            {{ myMatches(item) === 0 ? '✓ Safe' : myMatches(item) > 2 ? '⚠ Danger' : '⚡ Caution' }}
          </span>
        </div>
      </div>

      <div v-else class="empty-state">
        <span class="empty-icon">📋</span>
        <div class="empty-title">No scans yet</div>
        <p style="font-size:13px;color:var(--text-300)">Your scan history will appear here</p>
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
</style>