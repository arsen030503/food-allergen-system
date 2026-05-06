<script setup>
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAppStore } from '../stores/app'
import { useUiStore } from '../stores/ui'

const app = useAppStore()
const ui = useUiStore()
const { t } = useI18n()

const productName = ref('')
const ingredients = ref('')

const matchesForUser = computed(() => app.analyzeResult?.matchesForUser || [])

const allDetected = computed(() => app.analyzeResult?.detectedAllergens || [])

const profileConfigured = computed(() => app.analyzeResult?.userProfileConfigured === true)

const isSafe = computed(() => app.analyzeResult?.safeForUser === true)

onMounted(() => {
  if (app.quickScanDraft) {
    ingredients.value = app.quickScanDraft
    app.setQuickScanDraft('')
    analyze()
  }
})

async function analyze() {
  if (!ingredients.value.trim()) {
    ui.showToast(t('analyze.enterIngredients'), 'danger')
    return
  }
  try {
    await app.analyzeIngredients({
      ingredients: ingredients.value.trim(),
      productName: productName.value.trim(),
    })
  } catch {
    ui.showToast(t('analyze.cannotConnect'), 'danger')
  }
}

function clearForm() {
  productName.value = ''
  ingredients.value = ''
  app.clearAnalyzeResult()
}

function loadExample() {
  productName.value = t('analyze.productNamePlaceholder')
  ingredients.value = 'Sugar, wheat flour, whole milk powder, hazelnut paste 12%, cocoa butter, cocoa mass, skimmed milk powder, whey powder, soy lecithin, vanillin, salt, butter'
}
</script>

<template>
  <div class="analyze-wrapper">
    <div class="analyze-page">
      <div class="analyze-left">
        <h1 class="page-title">{{ t('analyze.title') }}</h1>
        <p class="page-sub">{{ t('analyze.subtitle') }}</p>

        <div class="input-group">
          <label class="input-label">{{ t('analyze.productName') }} <span style="color:var(--text-100)">({{ t('analyze.optional') }})</span></label>
          <input v-model="productName" type="text" class="text-input" :placeholder="t('analyze.productNamePlaceholder')"/>
        </div>

        <div class="input-group">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px">
            <label class="input-label">{{ t('analyze.ingredientsLabel') }}</label>
            <span style="font-size:12px;color:var(--text-300);font-family:var(--font-mono)">{{ ingredients.length }} / 1000</span>
          </div>
          <textarea v-model="ingredients" class="big-textarea" :placeholder="t('analyze.ingredientsPlaceholder')" maxlength="1000"></textarea>
        </div>

        <div class="analyze-actions">
          <button class="btn btn-primary" :class="{ loading: app.analyzing }" @click="analyze">
            <span class="spinner"></span>
            <span class="btn-text">🔍 {{ t('analyze.analyzeButton') }}</span>
          </button>
          <button class="btn btn-outline" @click="clearForm">✕ {{ t('common.clear') }}</button>
          <button class="btn btn-secondary" @click="loadExample">{{ t('analyze.loadExample') }}</button>
        </div>

        <div class="tips-box">
          <div class="tips-title">💡 {{ t('analyze.tipsTitle') }}</div>
          <ul>
            <li>{{ t('analyze.tip1') }}</li>
            <li>{{ t('analyze.tip2') }}</li>
            <li>{{ t('analyze.tip3') }}</li>
          </ul>
        </div>
      </div>

      <div class="analyze-right">
        <div v-if="!app.analyzeResult" class="result-hero waiting">
          <div class="result-label">{{ t('analyze.status') }}</div>
          <div class="result-title">{{ t('analyze.readyToScan') }}</div>
          <div class="result-desc">{{ t('analyze.readySub') }}</div>
        </div>

        <div v-else-if="!profileConfigured" class="result-hero no-profile">
          <div class="result-label">{{ t('analyze.setupRequired') }}</div>
          <div class="result-title" style="color:var(--warning-600)">⚠️ {{ t('analyze.noAllergensSelected') }}</div>
          <div class="result-desc" style="color:var(--warning-600)">{{ t('analyze.setupSub') }}</div>
        </div>

        <template v-else>
          <div class="result-hero" :class="isSafe ? 'safe' : 'danger'">
            <div class="result-label">{{ t('analyze.done') }}</div>
            <div class="result-title">{{ isSafe ? `✅ ${t('analyze.safeForYou')}` : `⚠️ ${t('analyze.allergensDetected')}` }}</div>
            <div class="result-desc">
              {{ isSafe ? t('analyze.safeDesc') : t('analyze.unsafeDesc', { count: matchesForUser.length }) }}
            </div>
          </div>

          <div class="section-label">{{ t('analyze.profileMatches') }}</div>
          <div class="allergen-list">
            <div v-if="matchesForUser.length === 0" class="analyze-muted-safe" style="text-align:center;padding:20px;font-weight:600">✅ {{ t('analyze.noMatches') }}</div>
            <div v-for="item in matchesForUser" :key="`${item.allergen}-${item.matchedIngredient}`" class="allergen-card" :class="{ warning: item.severity !== 'HIGH' }">
              <span class="allergen-emoji">{{ app.getEmoji(item.allergen) }}</span>
              <div class="allergen-info">
                <div class="allergen-name">{{ item.allergen }}</div>
                <div class="allergen-match">{{ item.matchedIngredient }}<span v-if="item.description"> — {{ item.description }}</span></div>
              </div>
              <div class="sev-dot" :class="item.severity"></div>
            </div>
          </div>

          <template v-if="allDetected.length">
            <div class="section-label" style="margin-top:16px">{{ t('analyze.allDetected') }}</div>
            <div class="allergen-list">
              <div v-for="item in allDetected" :key="`all-${item.allergen}-${item.matchedIngredient}`" class="allergen-card" :class="{ warning: item.severity !== 'HIGH' }">
                <span class="allergen-emoji">{{ app.getEmoji(item.allergen) }}</span>
                <div class="allergen-info">
                  <div class="allergen-name">{{ item.allergen }}</div>
                  <div class="allergen-match">{{ item.matchedIngredient }}<span v-if="item.description"> — {{ item.description }}</span></div>
                </div>
                <div class="sev-dot" :class="item.severity"></div>
              </div>
            </div>
          </template>

          <div class="section-label" style="margin-top:16px">{{ t('analyze.summary') }}</div>
          <div class="summary-grid">
            <div class="summary-stat"><div class="summary-n" :class="matchesForUser.length ? 'danger' : 'green'">{{ matchesForUser.length }}</div><div class="summary-l">{{ t('analyze.yourFound') }}</div></div>
            <div class="summary-stat"><div class="summary-n">{{ app.userAllergens.length }}</div><div class="summary-l">{{ t('analyze.yourProfile') }}</div></div>
            <div class="summary-stat"><div class="summary-n">{{ app.analyzeResult?.totalFound ?? 0 }}</div><div class="summary-l">{{ t('analyze.inProduct') }}</div></div>
            <div class="summary-stat"><div class="summary-n" :class="isSafe ? 'green' : 'danger'">{{ isSafe ? t('analyze.safe') : t('analyze.unsafe') }}</div><div class="summary-l">{{ t('analyze.statusLabel') }}</div></div>
          </div>

          <div class="status-bar" :class="isSafe ? 'is-safe' : 'not-safe'">
            {{ isSafe ? `✅ ${t('analyze.safeToEat')}` : `🚫 ${t('analyze.notSafe')}` }}
          </div>

          <div style="display:flex;gap:8px;margin-top:12px">
            <button class="btn btn-secondary btn-sm" style="flex:1;justify-content:center" @click="ui.showToast(t('analyze.savedToHistory'))">💾 {{ t('analyze.savedToHistory') }}</button>
            <button class="btn btn-outline btn-sm" style="flex:1;justify-content:center" @click="clearForm">🔍 {{ t('dashboard.newScan') }}</button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.analyze-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  width: 100%;
  min-height: 100vh;
}
.analyze-page {
  display: grid;
  grid-template-columns: 1fr 400px;
  flex: 1;
  width: 100%;
  min-height: 100vh;
}
.analyze-left {
  padding: 48px;
  background: var(--surface);
  overflow-y: auto;
  min-width: 0;
}
.analyze-right {
  background: var(--card);
  border-left: 1px solid var(--layout-divider);
  padding: 36px;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  min-width: 0;
}
.tips-box {
  margin-top: 32px;
  padding: 20px;
  background: var(--green-50);
  border-radius: var(--radius-md);
  border: 1.5px solid var(--green-200);
}
.tips-title {
  font-weight: 700;
  color: var(--green-800);
  font-size: 13px;
  margin-bottom: 8px;
}
.tips-box ul {
  font-size: 13px;
  color: var(--green-700);
  line-height: 1.8;
  padding-left: 16px;
}
.result-hero.no-profile {
  background: #fff8e1;
  border: 1.5px solid var(--warning-400);
  border-radius: var(--radius-lg);
  padding: 28px;
  margin-bottom: 24px;
}
@media (max-width: 1100px) {
  .analyze-page { grid-template-columns: 1fr; }
  .analyze-right { border-left: none; border-top: 1px solid var(--layout-divider); }
}
</style>