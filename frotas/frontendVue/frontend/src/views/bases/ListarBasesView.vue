<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { baseService } from '../../services/baseService'
import type { BaseResponse } from '../../types'

const bases = ref<BaseResponse[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const deletingId = ref<number | null>(null)

async function carregarBases() {
  try {
    loading.value = true
    error.value = null
    bases.value = await baseService.listarTodas()
  } catch (e) {
    error.value = 'Erro ao carregar bases. Verifique se o servidor está rodando.'
  } finally {
    loading.value = false
  }
}

async function excluirBase(id: number, nome: string) {
  if (!confirm(`Tem certeza que deseja excluir a base "${nome}"?`)) return
  try {
    deletingId.value = id
    await baseService.excluir(id)
    bases.value = bases.value.filter((b) => b.id !== id)
  } catch (e) {
    error.value = 'Erro ao excluir base.'
  } finally {
    deletingId.value = null
  }
}

onMounted(carregarBases)
</script>

<template>
  <div class="page-content">
    <div class="page-header" style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem;">
      <div>
        <h1 class="page-title">🏢 Bases</h1>
        <p class="page-subtitle">{{ bases.length }} base(s) cadastrada(s)</p>
      </div>
      <RouterLink to="/cadastros/cadastrar-base" class="btn btn-primary">
        + Nova Base
      </RouterLink>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>Carregando bases...</p>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="alert alert-danger">⚠️ {{ error }}</div>

    <!-- Empty -->
    <div v-else-if="bases.length === 0" class="empty-state">
      <span class="empty-state-icon">🏢</span>
      <p>Nenhuma base cadastrada ainda.</p>
      <RouterLink to="/cadastros/cadastrar-base" class="btn btn-primary">
        Cadastrar primeira base
      </RouterLink>
    </div>

    <!-- List -->
    <ul v-else style="list-style: none; display: flex; flex-direction: column; gap: 0.75rem;">
      <li
        v-for="base in bases"
        :key="base.id"
        class="list-item"
      >
        <div class="list-item-info">
          <h5>
            <span class="badge badge-purple" style="margin-right: 0.5rem;">#{{ base.id }}</span>
            {{ base.nome }}
          </h5>
          <p>
            <span>📍 {{ base.localidade }}</span>
            <span>✉️ {{ base.emailBase }}</span>
          </p>
        </div>
        <div class="list-item-actions">
          <button
            class="btn btn-danger btn-sm"
            :disabled="deletingId === base.id"
            @click="excluirBase(base.id, base.nome)"
          >
            {{ deletingId === base.id ? '⏳' : '🗑️' }} Excluir
          </button>
        </div>
      </li>
    </ul>
  </div>
</template>
