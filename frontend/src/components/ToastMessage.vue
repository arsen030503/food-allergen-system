<script setup>
import { computed } from 'vue'
import { useUiStore } from '../stores/ui'

const ui = useUiStore()
const classes = computed(() => ['toast', ui.toastMessage ? 'show' : '', ui.toastType].join(' ').trim())
</script>

<template>
  <div :class="classes">{{ ui.toastMessage }}</div>
</template>

