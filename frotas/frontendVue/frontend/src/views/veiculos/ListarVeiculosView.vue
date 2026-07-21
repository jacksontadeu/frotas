<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { veiculoService } from '../../services/veiculoService'
import type { VeiculoResponse } from '../../types'
import AppModal from '../../components/AppModal.vue'
import { extractErrorMessage } from '../../composables/useErrorMessage'

const veiculos = ref<VeiculoResponse[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const deletingId = ref<number | null>(null)

// Modal
const showModal = ref(false)
const modalType = ref<'success' | 'error' | 'confirm'>('confirm')
const modalMessage = ref('')
const pendingDeleteId = ref<number | null>(null)
const pendingDeleteNome = ref('')

async function carregarVeiculos() {
  try {
    loading.value = true
    error.value = null
    veiculos.value = await veiculoService.listarTodos()
  } catch {
    error.value = 'Erro ao carregar veículos. Verifique se o servidor está rodando.'
  } finally {
    loading.value = false
  }
}

function confirmarExclusao(id: number, nome: string) {
  pendingDeleteId.value = id
  pendingDeleteNome.value = nome
  modalType.value = 'confirm'
  modalMessage.value = `Tem certeza que deseja excluir o veículo "${nome}"? Esta ação não pode ser desfeita.`
  showModal.value = true
}

async function executarExclusao() {
  showModal.value = false
  if (!pendingDeleteId.value) return
  const id = pendingDeleteId.value
  try {
    deletingId.value = id
    await veiculoService.excluir(id)
    veiculos.value = veiculos.value.filter((v) => v.id !== id)
    modalType.value = 'success'
    modalMessage.value = `Veículo "${pendingDeleteNome.value}" excluído com sucesso!`
    showModal.value = true
  } catch (e: any) {
    modalType.value = 'error'
    modalMessage.value = extractErrorMessage(e, 'Erro ao excluir veículo.')
    showModal.value = true
  } finally {
    deletingId.value = null
    pendingDeleteId.value = null
  }
}

function fecharModal() {
  showModal.value = false
}

const tipoIcon: Record<string, string> = {
  CARRO: '🚗',
  CAMINHAO: '🚛',
  VAN: '🚐',
}

onMounted(carregarVeiculos)
</script>

<template>
  <div class="page-content">
    <div class="page-header" style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem;">
      <div>
        <h1 class="page-title">🚛 Veículos</h1>
        <p class="page-subtitle">{{ veiculos.length }} veículo(s) cadastrado(s)</p>
      </div>
      <RouterLink to="/cadastros/cadastrar-veiculo" class="btn btn-primary">
        + Novo Veículo
      </RouterLink>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>Carregando veículos...</p>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="alert alert-danger">⚠️ {{ error }}</div>

    <!-- Empty -->
    <div v-else-if="veiculos.length === 0" class="empty-state">
      <span class="empty-state-icon">🚗</span>
      <p>Nenhum veículo cadastrado ainda.</p>
      <RouterLink to="/cadastros/cadastrar-veiculo" class="btn btn-primary">
        Cadastrar primeiro veículo
      </RouterLink>
    </div>

    <!-- List -->
    <ul v-else style="list-style: none; display: flex; flex-direction: column; gap: 0.75rem;">
      <li
        v-for="veiculo in veiculos"
        :key="veiculo.id"
        class="list-item"
      >
        <div class="list-item-info">
          <h5>
            <span style="margin-right: 0.5rem;">{{ tipoIcon[veiculo.tipoVeiculo] ?? '🚙' }}</span>
            {{ veiculo.nome }}
            <span class="badge badge-purple" style="margin-left: 0.5rem;">{{ veiculo.tipoVeiculo }}</span>
          </h5>
          <p>
            <span>🔖 Placa: <strong>{{ veiculo.placaVeiculo }}</strong></span>
            <span>📋 Frota: {{ veiculo.frota }}</span>
            <span>🎨 Cor: {{ veiculo.cor }}</span>
            <span>📅 Ano: {{ veiculo.anoDeFabricacao }}</span>
            <span>🏢 Base: {{ veiculo.base?.nome }}</span>
          </p>
        </div>
        <div class="list-item-actions">
          <RouterLink
            to="/cadastros/cadastrar-manutencao"
            class="btn btn-warning btn-sm"
          >
            🔧 Manutenção
          </RouterLink>
          <button
            class="btn btn-danger btn-sm"
            :disabled="deletingId === veiculo.id"
            @click="confirmarExclusao(veiculo.id, veiculo.nome)"
          >
            {{ deletingId === veiculo.id ? '⏳' : '🗑️' }} Excluir
          </button>
        </div>
      </li>
    </ul>
  </div>

  <!-- Modal -->
  <AppModal
    :show="showModal"
    :type="modalType"
    :message="modalMessage"
    confirm-label="Sim, excluir"
    cancel-label="Cancelar"
    @close="fecharModal"
    @confirm="executarExclusao"
  />
</template>
