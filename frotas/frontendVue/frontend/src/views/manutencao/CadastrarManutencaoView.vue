<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { manutencaoService } from '../../services/manutencaoService'
import { veiculoService } from '../../services/veiculoService'
import type { ManutencaoDTORequest, VeiculoResponse } from '../../types'
import { TIPOS_MANUTENCAO } from '../../types'

const router = useRouter()

const form = ref<ManutencaoDTORequest>({
  dataRealizacao: '',
  kilometragem: null,
  tipoManutencao: '',
  veiculo_id: null,
})

// Campo de texto da placa selecionada (exibição)
const placaSelecionada = ref('')

// Modal
const modalOpen = ref(false)
const placaBusca = ref('')
const buscaLoading = ref(false)
const buscaError = ref<string | null>(null)
const veiculoEncontrado = ref<VeiculoResponse | null>(null)

// Form
const loading = ref(false)
const error = ref<string | null>(null)
const success = ref(false)

// ---- Modal ----
function abrirModal() {
  modalOpen.value = true
  placaBusca.value = ''
  buscaError.value = null
  veiculoEncontrado.value = null
}

function fecharModal() {
  modalOpen.value = false
}

async function buscarPlaca() {
  if (!placaBusca.value.trim()) {
    buscaError.value = 'Digite uma placa válida!'
    return
  }
  buscaLoading.value = true
  buscaError.value = null
  veiculoEncontrado.value = null
  try {
    const found = await veiculoService.buscarPorPlaca(placaBusca.value.trim())
    if (found) {
      veiculoEncontrado.value = found
    } else {
      buscaError.value = 'Veículo não encontrado para essa placa.'
    }
  } catch {
    buscaError.value = 'Erro ao buscar veículo. Verifique se o servidor está rodando.'
  } finally {
    buscaLoading.value = false
  }
}

function selecionarVeiculo() {
  if (!veiculoEncontrado.value) return
  form.value.veiculo_id = veiculoEncontrado.value.id
  placaSelecionada.value = veiculoEncontrado.value.placaVeiculo
  fecharModal()
}

// ---- Submit ----
async function handleSubmit() {
  error.value = null
  loading.value = true
  try {
    await manutencaoService.cadastrar(form.value)
    success.value = true
    setTimeout(() => router.push('/listagem/listar-veiculos'), 1500)
  } catch (e: any) {
    error.value = e?.response?.data?.message || 'Erro ao cadastrar manutenção.'
  } finally {
    loading.value = false
  }
}

// Fechar modal com ESC
function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape' && modalOpen.value) fecharModal()
}
onMounted(() => document.addEventListener('keydown', handleKeydown))
</script>

<template>
  <div class="page-content">
    <div class="page-header">
      <h1 class="page-title">Cadastrar Manutenção</h1>
      <p class="page-subtitle">Registre uma manutenção preventiva ou corretiva</p>
    </div>

    <div class="form-card">
      <div v-if="success" class="alert alert-success">
        ✅ Manutenção registrada com sucesso! Redirecionando...
      </div>
      <div v-if="error" class="alert alert-danger">⚠️ {{ error }}</div>

      <form @submit.prevent="handleSubmit" novalidate>

        <!-- Placa com busca -->
        <div class="form-group">
          <label for="mPlaca" class="form-label">Veículo (Placa)</label>
          <div class="input-group">
            <input
              id="mPlaca"
              v-model="placaSelecionada"
              type="text"
              class="form-control"
              placeholder="Clique em Pesquisar para buscar"
              readonly
              :disabled="loading || success"
              style="cursor: default;"
            />
            <button
              type="button"
              class="btn btn-secondary"
              :disabled="loading || success"
              @click="abrirModal"
            >
              🔍 Pesquisar
            </button>
          </div>
          <p v-if="veiculoEncontrado || form.veiculo_id" class="text-muted" style="margin-top: 0.4rem;">
            ✅ Veículo ID: {{ form.veiculo_id }}
          </p>
        </div>

        <!-- Quilometragem -->
        <div class="form-group">
          <label for="mKm" class="form-label">Quilometragem Atual</label>
          <input
            id="mKm"
            v-model.number="form.kilometragem"
            type="number"
            class="form-control"
            placeholder="Ex: 125000"
            min="0"
            required
            :disabled="loading || success"
          />
        </div>

        <!-- Data Realização -->
        <div class="form-group">
          <label for="mData" class="form-label">Data de Realização</label>
          <input
            id="mData"
            v-model="form.dataRealizacao"
            type="date"
            class="form-control"
            required
            :disabled="loading || success"
          />
        </div>

        <!-- Tipo de Manutenção -->
        <div class="form-group">
          <label for="mTipo" class="form-label">Tipo de Manutenção</label>
          <select
            id="mTipo"
            v-model="form.tipoManutencao"
            class="form-select"
            required
            :disabled="loading || success"
          >
            <option value="" disabled>Selecione o tipo</option>
            <option
              v-for="tipo in TIPOS_MANUTENCAO"
              :key="tipo.value"
              :value="tipo.value"
            >
              {{ tipo.label }}
            </option>
          </select>
        </div>

        <div class="form-actions">
          <RouterLink to="/listagem/listar-veiculos" class="btn btn-secondary">
            Cancelar
          </RouterLink>
          <button
            type="submit"
            class="btn btn-primary"
            :disabled="loading || success || !form.veiculo_id || !form.dataRealizacao || !form.tipoManutencao"
          >
            <span v-if="loading">⏳ Salvando...</span>
            <span v-else>🔧 Registrar Manutenção</span>
          </button>
        </div>
      </form>
    </div>
  </div>

  <!-- Modal de busca de placa -->
  <Teleport to="body">
    <div v-if="modalOpen" class="modal-overlay" @click.self="fecharModal">
      <div class="modal-box" role="dialog" aria-modal="true" aria-labelledby="modalTitle">
        <div class="modal-header">
          <h3 id="modalTitle" class="modal-title">🔍 Pesquisar Veículo por Placa</h3>
          <button
            class="btn btn-secondary btn-icon"
            aria-label="Fechar"
            @click="fecharModal"
          >✕</button>
        </div>

        <div class="form-group">
          <label for="placaBusca" class="form-label">Digite a placa do veículo</label>
          <div class="input-group">
            <input
              id="placaBusca"
              v-model="placaBusca"
              type="text"
              class="form-control"
              placeholder="Ex: ABC1234"
              @keyup.enter="buscarPlaca"
              :disabled="buscaLoading"
            />
            <button
              type="button"
              class="btn btn-primary"
              :disabled="buscaLoading"
              @click="buscarPlaca"
            >
              {{ buscaLoading ? '⏳' : '🔍' }}
            </button>
          </div>
        </div>

        <!-- Resultado da busca -->
        <div v-if="buscaError" class="alert alert-danger">{{ buscaError }}</div>

        <div v-if="veiculoEncontrado" class="veiculo-result">
          <div class="veiculo-result-info">
            <p class="veiculo-result-nome">{{ veiculoEncontrado.nome }}</p>
            <p class="text-muted">
              Placa: <strong>{{ veiculoEncontrado.placaVeiculo }}</strong> |
              Tipo: {{ veiculoEncontrado.tipoVeiculo }} |
              Base: {{ veiculoEncontrado.base?.nome }}
            </p>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" @click="fecharModal">Cancelar</button>
          <button
            class="btn btn-primary"
            :disabled="!veiculoEncontrado"
            @click="selecionarVeiculo"
          >
            ✅ Selecionar
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.veiculo-result {
  background: var(--color-surface-2);
  border: 1px solid rgba(99, 102, 241, 0.3);
  border-radius: var(--radius-md);
  padding: 1rem;
  margin-top: 0.75rem;
  animation: fadeSlideIn 0.2s ease;
}

.veiculo-result-nome {
  font-weight: 700;
  font-size: 1rem;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}
</style>
