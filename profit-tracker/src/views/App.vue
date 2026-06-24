<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../stores/authStore.js'

const store = useAuthStore()

const isLoggedIn = computed(() => store.token !== null)
</script>

<template>
  <div class="app">
    <router-view />
  </div>
</template>

<style scoped>
.app { min-height: 100vh; background: #f0f2f5; }
</style>
