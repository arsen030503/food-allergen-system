<script setup>
defineProps({
  type: {
    type: String,
    default: 'card' // 'card', 'text', 'avatar', 'stat'
  },
  count: {
    type: Number,
    default: 1
  }
})
</script>

<template>
  <div v-if="type === 'card'" class="skeleton-list">
    <div v-for="i in count" :key="i" class="skeleton-card">
      <div class="skeleton-line skeleton-line-title"></div>
      <div class="skeleton-line skeleton-line-text"></div>
      <div class="skeleton-line skeleton-line-text short"></div>
    </div>
  </div>

  <div v-else-if="type === 'stat'" class="skeleton-stats">
    <div v-for="i in count" :key="i" class="skeleton-stat">
      <div class="skeleton-line skeleton-line-large"></div>
      <div class="skeleton-line skeleton-line-small"></div>
    </div>
  </div>

  <div v-else-if="type === 'avatar'" class="skeleton-avatar">
    <div class="skeleton-avatar-img"></div>
    <div class="skeleton-info">
      <div class="skeleton-line skeleton-line-text"></div>
      <div class="skeleton-line skeleton-line-text short"></div>
    </div>
  </div>

  <div v-else class="skeleton-line skeleton-line-text"></div>
</template>

<style scoped>
.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.skeleton-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #f0f0f0;
}

.skeleton-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.skeleton-stat {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #f0f0f0;
  text-align: center;
}

.skeleton-avatar {
  display: flex;
  gap: 16px;
  align-items: center;
}

.skeleton-avatar-img {
  width: 88px;
  height: 88px;
  border-radius: 12px;
  flex-shrink: 0;
}

.skeleton-info {
  flex: 1;
}

.skeleton-line {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 8px;
}

.skeleton-line:last-child {
  margin-bottom: 0;
}

.skeleton-line-title {
  height: 16px;
  width: 40%;
}

.skeleton-line-text {
  height: 12px;
  width: 100%;
}

.skeleton-line-text.short {
  width: 60%;
}

.skeleton-line-large {
  height: 28px;
  width: 60%;
  margin: 0 auto 8px;
}

.skeleton-line-small {
  height: 10px;
  width: 70%;
  margin: 0 auto;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>

