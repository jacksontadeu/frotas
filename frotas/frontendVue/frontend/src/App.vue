<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import AppHeader from './components/AppHeader.vue'
import { useAuth } from './composables/useAuth'

const { initAuth } = useAuth()

onMounted(() => {
  initAuth()
})
</script>

<template>
  <div id="app">
    <AppHeader v-if="!$route.meta.hideHeader" />
    <main>
      <RouterView />
    </main>
  </div>
</template>
