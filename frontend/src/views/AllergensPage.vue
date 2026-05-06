<script setup>
import { computed, ref } from 'vue'
import { useAppStore } from '../stores/app'

const app = useAppStore()
const searchQuery = ref('')

const allergens = computed(() => {
  let filtered = app.filteredAllergens
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(item =>
      item.name.toLowerCase().includes(query) ||
      item.triggerIngredients.toLowerCase().includes(query)
    )
  }
  return filtered
})

function setFilter(value) {
  app.setFilter(value)
}

function clearSearch() {
  searchQuery.value = ''
}
</script>

<template>
  <div class="allergens-wrapper">
    <div class="allergens-page">
      <div class="allergens-header">
        <div>
          <h1 class="page-title">Allergen Database</h1>
          <p class="page-sub">EU (14 allergens) + FDA (9 allergens) — Complete reference guide</p>
        </div>
        <div class="filter-buttons">
          <button class="btn btn-outline btn-sm" :class="{ 'btn-primary': app.currentFilter === 'ALL' }" @click="setFilter('ALL')">All</button>
          <button class="btn btn-outline btn-sm" :class="{ 'btn-primary': app.currentFilter === 'EU' }" @click="setFilter('EU')">EU Only</button>
          <button class="btn btn-outline btn-sm" :class="{ 'btn-primary': app.currentFilter === 'FDA' }" @click="setFilter('FDA')">FDA Only</button>
        </div>
      </div>

      <div style="position:relative;margin-bottom:32px">
        <input v-model="searchQuery" type="text" class="search-input" placeholder="Search allergens by name or ingredients..."/>
        <button v-if="searchQuery" class="search-clear-btn" @click="clearSearch">✕</button>
      </div>

      <div class="allergens-grid" v-if="allergens.length">
        <div v-for="item in allergens" :key="`${item.name}-${item.standard}`" class="allergen-db-card">
          <div class="allergen-db-header">
            <span class="allergen-db-emoji">{{ app.getEmoji(item.name) }}</span>
            <div>
              <div class="allergen-db-name">{{ item.name }}</div>
              <div class="allergen-db-std">{{ item.standard }} Standard</div>
            </div>
          </div>
          <div class="allergen-db-triggers">{{ item.triggerIngredients }}</div>
          <div class="allergen-db-footer">
            <span class="std-badge" :class="item.standard === 'EU' ? 'std-eu' : 'std-fda'">{{ item.standard }}</span>
            <span class="sev-badge" :class="`sev-${item.severity}`">{{ item.severity }}</span>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <span class="empty-icon">🔍</span>
        <div class="empty-title">No allergens found</div>
        <p style="font-size:13px;color:var(--text-300)">Try different keywords</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.allergens-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  width: 100%;
  min-height: 100vh;
  background: var(--surface);
}
.allergens-page {
  padding: 48px;
  flex: 1;
}
.allergens-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 32px;
  gap: 32px;
}
.filter-buttons {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
@media (max-width: 768px) {
  .allergens-header { flex-direction: column; }
  .allergens-grid { grid-template-columns: 1fr; }
}
</style>