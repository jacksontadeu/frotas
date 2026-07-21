<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { baseService } from '../../services/baseService'
import type { BaseDTORequest } from '../../types'
import AppModal from '../../components/AppModal.vue'
import { extractErrorMessage } from '../../composables/useErrorMessage'

const router = useRouter()

const form = ref<BaseDTORequest>({
  nome: '',
  localidade: '',
  emailBase: '',
})

const loading = ref(false)

// Modal
const showModal = ref(false)
const modalType = ref<'success' | 'error'>('success')
const modalMessage = ref('')

async function handleSubmit() {
  loading.value = true
  try {
    await baseService.cadastrar(form.value)
    modalType.value = 'success'
    modalMessage.value = 'Base cadastrada com sucesso!'
    showModal.value = true
  } catch (e: any) {
    modalType.value = 'error'
    modalMessage.value = extractErrorMessage(e, 'Erro ao cadastrar base. Verifique se o servidor está rodando.')
    showModal.value = true
  } finally {
    loading.value = false
  }
}

function fecharModal() {
  showModal.value = false
  if (modalType.value === 'success') {
    router.push('/listagem/listar-bases')
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
            :disabled="loading"
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
            :disabled="loading"
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
            :disabled="loading"
          />
        </div>

        <div class="form-actions">
          <RouterLink to="/listagem/listar-bases" class="btn btn-secondary">
            Cancelar
          </RouterLink>
          <button
            type="submit"
            class="btn btn-primary"
            :disabled="loading || !form.nome || !form.localidade || !form.emailBase"
          >
            <span v-if="loading">⏳ Salvando...</span>
            <span v-else>💾 Cadastrar Base</span>
          </button>
        </div>
      </form>
    </div>
  </div>

  <!-- Modal de resultado -->
  <AppModal
    :show="showModal"
    :type="modalType"
    :message="modalMessage"
    @close="fecharModal"
  />
</template>
