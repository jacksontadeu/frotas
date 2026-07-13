<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { baseService } from '../../services/baseService'
import type { BaseDTORequest } from '../../types'

const router = useRouter()

const form = ref<BaseDTORequest>({
  nome: '',
  localidade: '',
  emailBase: '',
})

const loading = ref(false)
const error = ref<string | null>(null)
const success = ref(false)

async function handleSubmit() {
  error.value = null
  loading.value = true
  try {
    await baseService.cadastrar(form.value)
    success.value = true
    setTimeout(() => router.push('/listagem/listar-bases'), 1500)
  } catch (e: any) {
    error.value = e?.response?.data?.message || 'Erro ao cadastrar base. Verifique se o servidor está rodando.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page-content">
    <div class="page-header">
      <h1 class="page-title">Cadastrar Base</h1>
      <p class="page-subtitle">Registre uma nova base operacional da frota</p>
    </div>

    <div class="form-card">
      <div v-if="success" class="alert alert-success">
        ✅ Base cadastrada com sucesso! Redirecionando...
      </div>

      <div v-if="error" class="alert alert-danger">
        ⚠️ {{ error }}
      </div>

      <form @submit.prevent="handleSubmit" novalidate>
        <div class="form-group">
          <label for="nomeBase" class="form-label">Nome da Base</label>
          <input
            id="nomeBase"
            v-model="form.nome"
            type="text"
            class="form-control"
            placeholder="Digite o nome da base"
            required
            :disabled="loading || success"
          />
        </div>

        <div class="form-group">
          <label for="localidadeBase" class="form-label">Localidade</label>
          <input
            id="localidadeBase"
            v-model="form.localidade"
            type="text"
            class="form-control"
            placeholder="Digite a localidade da base"
            required
            :disabled="loading || success"
          />
        </div>

        <div class="form-group">
          <label for="emailBase" class="form-label">Email da Base</label>
          <input
            id="emailBase"
            v-model="form.emailBase"
            type="email"
            class="form-control"
            placeholder="email@exemplo.com"
            required
            :disabled="loading || success"
          />
        </div>

        <div class="form-actions">
          <RouterLink to="/listagem/listar-bases" class="btn btn-secondary">
            Cancelar
          </RouterLink>
          <button
            type="submit"
            class="btn btn-primary"
            :disabled="loading || success || !form.nome || !form.localidade || !form.emailBase"
          >
            <span v-if="loading">⏳ Salvando...</span>
            <span v-else>💾 Cadastrar Base</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
