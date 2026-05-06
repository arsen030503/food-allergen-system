<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '../stores/app'
import { getTimeAgo } from '../utils/time'

const router = useRouter()
const app = useAppStore()

const quickIngredients = ref('')
const recentScans = computed(() => app.scanHistory.slice(0, 4))

const tipsOfDay = [
  '💡 Always check food labels for "may contain" warnings - they might contain your allergens!',
  '🔍 EU regulations require 14 major allergens to be clearly labeled on food packaging.',
  '🧪 Cross-contamination is serious - use separate utensils when handling allergen-free foods.',
  '📖 Keep an updated list of your allergens with you when shopping or eating out.',
  '⚠️ Some allergens hide under different names - know the common names for your allergens.',
  '🏥 Educate friends and family about your allergies for safer social eating.',
  '📱 Scan products regularly - recipes and ingredients change over time.',
  '🌍 FDA and EU standards may differ - check which standard your region follows.',
]

const tipOfDay = computed(() => {
  const today = new Date()
  const dayOfYear = Math.floor((today - new Date(today.getFullYear(), 0, 0)) / 86400000)
  return tipsOfDay[dayOfYear % tipsOfDay.length]
})

async function quickAnalyze() {
  if (!quickIngredients.value.trim()) return
  app.setQuickScanDraft(quickIngredients.value.trim())
  await router.push({ name: 'analyze' })
}
</script>

<template>
  <div class="dashboard-wrapper">
    <div class="dashboard-hero">
      <div class="hero-layout">
        <div class="hero-copy">
          <p class="hero-greeting">Welcome back 👋</p>
          <h1 class="hero-title">What are you <em>eating</em><br />today?</h1>
          <p class="hero-sub">
            Scan any product and get instant allergen alerts based on your personal profile.
          </p>
        </div>
        <aside class="hero-cta-card" aria-label="Quick analysis">
          <div class="hero-cta-label">Quick analysis</div>
          <div class="hero-cta-title">Scan ingredients right now</div>
          <button type="button" class="btn btn-primary hero-cta-btn" @click="router.push({ name: 'analyze' })">
            Analyze now →
          </button>
        </aside>
      </div>
    </div>

    <div class="dashboard-content">
      <div class="dashboard-left">
        <div class="quick-scan-box">
          <div class="quick-scan-intro">
            <h2 class="quick-scan-title">Quick scan</h2>
            <p class="quick-scan-sub">Paste ingredients for instant analysis</p>
          </div>
          <textarea
            v-model="quickIngredients"
            class="quick-textarea"
            rows="4"
            placeholder="e.g. wheat flour, milk, eggs, sugar, butter..."
          />
          <div class="quick-scan-actions">
            <button type="button" class="btn btn-primary btn-sm" @click="quickAnalyze">Analyze</button>
          </div>
        </div>

        <div class="section-label">Recent Scans</div>

        <div class="scan-list" v-if="recentScans.length">
          <div v-for="scan in recentScans" :key="scan.id" class="scan-card" @click="router.push({ name: 'history' })">
            <div class="scan-icon" style="background:var(--green-100)">
              <svg style="stroke:var(--green-600)" viewBox="0 0 24 24"><use href="#ic-history"/></svg>
            </div>
            <div class="scan-info">
              <div class="scan-name">{{ scan.productName || 'Unnamed Product' }}</div>
              <div class="scan-meta">{{ getTimeAgo(scan.scannedAt) }}</div>
            </div>
            <span class="badge badge-safe">View</span>
          </div>
        </div>

        <div v-else class="empty-state" style="padding:30px 0">
          <span class="empty-icon">📋</span>
          <div class="empty-title">No scans yet</div>
          <p style="font-size:13px;color:var(--text-300)">Start scanning ingredients to see results here</p>
        </div>
      </div>

      <div class="dashboard-right">
        <div class="tip-of-day-box">
          <div class="tip-label">✨ Tip of the Day</div>
          <div class="tip-content">{{ tipOfDay }}</div>
        </div>

        <div class="section-label" style="margin-top:28px;margin-bottom:20px">Your Stats</div>
        <div class="stats-row">
          <div class="stat-card"><span class="stat-n danger">{{ app.scanStats.userAllergenCount }}</span><div class="stat-l">My Allergens</div></div>
          <div class="stat-card"><span class="stat-n">{{ app.totalScans }}</span><div class="stat-l">Total Scans</div></div>
          <div class="stat-card"><span class="stat-n green">{{ app.safeScans }}</span><div class="stat-l">Safe Products</div></div>
        </div>

        <div style="margin-top:28px">
          <div class="section-label">My Active Allergens</div>
          <div style="display:flex;flex-wrap:wrap;gap:8px;margin-top:8px">
            <span v-for="name in app.userAllergens" :key="name" class="badge badge-danger">{{ app.getEmoji(name) }} {{ name }}</span>
            <span v-if="!app.userAllergens.length" style="color:var(--text-300);font-size:13px">No allergens selected</span>
          </div>
        </div>

        <div style="margin-top:28px">
          <div class="section-label">Quick Actions</div>
          <div style="display:flex;flex-direction:column;gap:8px">
            <button class="btn btn-secondary" style="justify-content:center" @click="router.push({ name: 'analyze' })">🔍 New Scan</button>
            <button class="btn btn-outline" style="justify-content:center" @click="router.push({ name: 'history' })">📋 View History</button>
            <button class="btn btn-outline" style="justify-content:center" @click="router.push({ name: 'allergens' })">📊 Allergen Database</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-wrapper {
  display: flex;
  flex-direction: column;
  flex: 1;
  width: 100%;
  min-height: 100vh;
}

.dashboard-hero {
  background: linear-gradient(165deg, var(--green-800) 0%, #152f1c 55%, var(--green-900) 100%);
  padding: clamp(32px, 5vw, 56px) clamp(24px, 4vw, 56px) clamp(40px, 6vw, 64px);
  position: relative;
  overflow: hidden;
  width: 100%;
  flex-shrink: 0;
}

.dashboard-hero::before {
  content: '';
  position: absolute;
  left: -20%;
  top: -40%;
  width: 70%;
  height: 140%;
  background: radial-gradient(ellipse, rgba(255, 255, 255, 0.06) 0%, transparent 65%);
  pointer-events: none;
}

.dashboard-hero::after {
  content: '';
  position: absolute;
  right: -10%;
  bottom: -30%;
  width: 50%;
  height: 100%;
  background: radial-gradient(ellipse, rgba(74, 179, 90, 0.12) 0%, transparent 70%);
  pointer-events: none;
}

.hero-layout {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  gap: clamp(28px, 4vw, 48px);
  max-width: 1120px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.hero-copy {
  flex: 1;
  min-width: 0;
  max-width: 640px;
  text-align: left;
}

.hero-copy .hero-greeting {
  margin: 0 0 12px;
}

.hero-copy .hero-title {
  margin: 0;
  font-size: clamp(2rem, 4.5vw, 3.15rem);
  line-height: 1.08;
}

.hero-copy .hero-sub {
  margin: 16px 0 0;
  max-width: 34rem;
}

.dashboard-hero .hero-cta-card {
  margin-top: 0;
  flex-shrink: 0;
  width: 100%;
  max-width: 320px;
  padding: clamp(20px, 3vw, 28px);
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.2);
}

.dashboard-hero .hero-cta-label {
  opacity: 0.85;
}

.dashboard-hero .hero-cta-title {
  font-size: 1.15rem;
  line-height: 1.35;
}

.hero-cta-btn {
  width: 100%;
  margin-top: 4px;
  justify-content: center;
}

.quick-scan-intro {
  margin-bottom: 16px;
}

.quick-scan-intro .quick-scan-title {
  margin: 0 0 6px;
  font-size: 1.125rem;
  letter-spacing: -0.02em;
}

.quick-scan-intro .quick-scan-sub {
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.45;
  color: var(--green-700);
  opacity: 0.92;
}

.quick-scan-actions {
  margin-top: 14px;
  display: flex;
  justify-content: flex-start;
}

.dashboard-content {
  display: grid;
  grid-template-columns: 1fr 400px;
  flex: 1;
  width: 100%;
}

.dashboard-left {
  padding: clamp(28px, 4vw, 48px) clamp(24px, 4vw, 48px);
  background: var(--surface);
  min-width: 0;
}

.dashboard-left .quick-scan-box {
  box-shadow: var(--shadow-sm);
  margin-bottom: 28px;
}

.dashboard-right {
  background: var(--card);
  border-left: 1px solid #eee;
  padding: clamp(24px, 3vw, 36px);
  min-width: 0;
}

.tip-of-day-box {
  background: var(--green-50);
  border: 1.5px solid var(--green-200);
  border-radius: 14px;
  padding: 18px 20px;
  margin-bottom: 0;
}

.tip-label {
  font-size: 10px;
  font-family: var(--font-mono);
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--green-700);
  font-weight: 600;
  margin-bottom: 10px;
}

.tip-content {
  font-size: 14px;
  color: var(--green-800);
  line-height: 1.6;
  font-weight: 500;
}

@media (max-width: 900px) {
  .hero-layout {
    flex-direction: column;
    align-items: stretch;
  }

  .hero-copy {
    max-width: none;
  }

  .dashboard-hero .hero-cta-card {
    max-width: none;
  }
}

@media (max-width: 1100px) {
  .dashboard-content {
    grid-template-columns: 1fr;
  }
  .dashboard-right {
    border-left: none;
    border-top: 1px solid #eee;
  }
}
</style>