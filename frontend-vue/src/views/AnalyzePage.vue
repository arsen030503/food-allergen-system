<script setup>
import { computed, onMounted, ref } from 'vue'
import { useAppStore } from '../stores/app'
import { useUiStore } from '../stores/ui'

const app = useAppStore()
const ui = useUiStore()

const productName = ref('')
const ingredients = ref('')

const myResult = computed(() => {
  const detected = app.analyzeResult?.detectedAllergens || []
  return detected.filter((item) => app.userAllergens.includes(item.allergen))
})

const isSafe = computed(() => myResult.value.length === 0)

onMounted(() => {
  if (app.quickScanDraft) {
    ingredients.value = app.quickScanDraft
    app.setQuickScanDraft('')
    analyze()
  }
})

async function analyze() {
  if (!ingredients.value.trim()) {
    ui.showToast('Please enter ingredients first', 'danger')
    return
  }
  try {
    await app.analyzeIngredients({
      ingredients: ingredients.value.trim(),
      productName: productName.value.trim(),
    })
  } catch {
    ui.showToast('Cannot connect to server', 'danger')
  }
}

function clearForm() {
  productName.value = ''
  ingredients.value = ''
  app.clearAnalyzeResult()
}

function loadExample() {
  productName.value = 'Milka Chocolate Bar'
  ingredients.value = 'Sugar, wheat flour, whole milk powder, hazelnut paste 12%, cocoa butter, cocoa mass, skimmed milk powder, whey powder, soy lecithin, vanillin, salt, butter'
}
</script>

<template>
  <div class="analyze-wrapper">
    <div class="analyze-page">
      <div class="analyze-left">
        <h1 class="page-title">Scan Ingredients</h1>
        <p class="page-sub">Paste the ingredient list from any food product</p>

        <div class="input-group">
          <label class="input-label">Product Name <span style="color:var(--text-100)">(optional)</span></label>
          <input v-model="productName" type="text" class="text-input" placeholder="e.g. Milka Chocolate Bar"/>
        </div>

        <div class="input-group">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px">
            <label class="input-label">Ingredients List</label>
            <span style="font-size:12px;color:var(--text-300);font-family:var(--font-mono)">{{ ingredients.length }} / 1000</span>
          </div>
          <textarea v-model="ingredients" class="big-textarea" placeholder="Paste ingredients here, separated by commas..." maxlength="1000"></textarea>
        </div>

        <div class="analyze-actions">
          <button class="btn btn-primary" :class="{ loading: app.analyzing }" @click="analyze">
            <span class="spinner"></span>
            <span class="btn-text">🔍 Analyze Ingredients</span>
          </button>
          <button class="btn btn-outline" @click="clearForm">✕ Clear</button>
          <button class="btn btn-secondary" @click="loadExample">Load Example</button>
        </div>

        <div class="tips-box">
          <div class="tips-title">💡 Tips</div>
          <ul>
            <li>First select your allergens in <strong>My Profile</strong></li>
            <li>Paste the full ingredient list from the packaging</li>
            <li>Results show only allergens from your profile</li>
          </ul>
        </div>
      </div>

      <div class="analyze-right">
        <div v-if="!app.analyzeResult" class="result-hero waiting">
          <div class="result-label">Analysis Status</div>
          <div class="result-title" style="color:var(--green-800)">Ready to Scan</div>
          <div class="result-desc" style="color:var(--green-600)">Paste your ingredient list and click Analyze to detect allergens.</div>
        </div>

        <div v-else-if="app.userAllergens.length === 0" class="result-hero no-profile">
          <div class="result-label">Setup Required</div>
          <div class="result-title" style="color:var(--warning-600)">⚠️ No Allergens Selected</div>
          <div class="result-desc" style="color:var(--warning-600)">Go to <strong>My Profile</strong> and toggle your allergens first.</div>
        </div>

        <template v-else>
          <div class="result-hero" :class="isSafe ? 'safe' : 'danger'">
            <div class="result-label">Analysis Complete</div>
            <div class="result-title">{{ isSafe ? '✅ Safe for You' : '⚠️ Allergens Detected' }}</div>
            <div class="result-desc">
              {{ isSafe ? 'This product is safe based on your allergen profile.' : `Found ${myResult.length} allergen${myResult.length > 1 ? 's' : ''} in this product.` }}
            </div>
          </div>

          <div class="section-label">Detected Allergens</div>
          <div class="allergen-list">
            <div v-if="myResult.length === 0" style="text-align:center;padding:20px;color:var(--green-600);font-weight:600">✅ None of your allergens found</div>
            <div v-for="item in myResult" :key="`${item.allergen}-${item.matchedIngredient}`" class="allergen-card" :class="{ warning: item.severity !== 'HIGH' }">
              <span class="allergen-emoji">{{ app.getEmoji(item.allergen) }}</span>
              <div class="allergen-info">
                <div class="allergen-name">{{ item.allergen }}</div>
                <div class="allergen-match">{{ item.matchedIngredient }}<span v-if="item.description"> — {{ item.description }}</span></div>
              </div>
              <div class="sev-dot" :class="item.severity"></div>
            </div>
          </div>

          <div class="section-label" style="margin-top:16px">Scan Summary</div>
          <div class="summary-grid">
            <div class="summary-stat"><div class="summary-n" :class="myResult.length ? 'danger' : 'green'">{{ myResult.length }}</div><div class="summary-l">Your Allergens Found</div></div>
            <div class="summary-stat"><div class="summary-n">{{ app.userAllergens.length }}</div><div class="summary-l">Your Profile</div></div>
            <div class="summary-stat"><div class="summary-n">{{ ingredients.split(',').filter(Boolean).length }}</div><div class="summary-l">Ingredients</div></div>
            <div class="summary-stat"><div class="summary-n" :class="isSafe ? 'green' : 'danger'">{{ isSafe ? 'Safe' : 'Unsafe' }}</div><div class="summary-l">Status</div></div>
          </div>

          <div class="status-bar" :class="isSafe ? 'is-safe' : 'not-safe'">
            {{ isSafe ? '✅ Safe to Eat' : '🚫 Not Safe for You' }}
          </div>

          <div style="display:flex;gap:8px;margin-top:12px">
            <button class="btn btn-secondary btn-sm" style="flex:1;justify-content:center" @click="ui.showToast('Report saved to history')">💾 Save Report</button>
            <button class="btn btn-outline btn-sm" style="flex:1;justify-content:center" @click="clearForm">🔍 New Scan</button>
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
  border-left: 1px solid #eee;
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
  .analyze-right { border-left: none; border-top: 1px solid #eee; }
}
</style>