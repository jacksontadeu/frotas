<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { manutencaoService } from '../../services/manutencaoService'
import { veiculoService } from '../../services/veiculoService'
import type { ManutencaoDTORequest, VeiculoResponse, Servico } from '../../types'
import { TIPOS_MANUTENCAO, SERVICOS_LIST } from '../../types'
import AppModal from '../../components/AppModal.vue'
import { extractErrorMessage } from '../../composables/useErrorMessage'

const router = useRouter()

const form = ref<ManutencaoDTORequest>({
  dataAgendamento: '',
  kilometragem: null,
  tipoManutencao: '',
  veiculo_id: null,
  servicos: [],
})

function toggleServico(servico: Servico) {
  if (!form.value.servicos) {
    form.value.servicos = []
  }
  const index = form.value.servicos.indexOf(servico)
  if (index > -1) {
    form.value.servicos.splice(index, 1)
  } else {
    form.value.servicos.push(servico)
  }
}

// --- Autocomplete de placa ---
const placaInput = ref('')
const sugestoes = ref<VeiculoResponse[]>([])
const sugestoesVisiveis = ref(false)
const autoCompleteLoading = ref(false)
const veiculoSelecionado = ref<VeiculoResponse | null>(null)
const autoCompleteRef = ref<HTMLElement | null>(null)

let debounceTimer: ReturnType<typeof setTimeout> | null = null

watch(placaInput, (val) => {
  // Limpa seleção anterior se o usuário editar o campo
  if (veiculoSelecionado.value && val !== veiculoSelecionado.value.placaVeiculo) {
    veiculoSelecionado.value = null
    form.value.veiculo_id = null
  }

  if (debounceTimer) clearTimeout(debounceTimer)

  if (!val.trim()) {
    sugestoes.value = []
    sugestoesVisiveis.value = false
    return
  }

  debounceTimer = setTimeout(async () => {
    autoCompleteLoading.value = true
    try {
      sugestoes.value = await veiculoService.buscarPorPrefixo(val)
      sugestoesVisiveis.value = sugestoes.value.length > 0
    } catch {
      sugestoes.value = []
    } finally {
      autoCompleteLoading.value = false
    }
  }, 250)
})

function selecionarSugestao(veiculo: VeiculoResponse) {
  veiculoSelecionado.value = veiculo
  form.value.veiculo_id = veiculo.id
  placaInput.value = veiculo.placaVeiculo
  sugestoes.value = []
  sugestoesVisiveis.value = false
}

function fecharSugestoes() {
  sugestoesVisiveis.value = false
}

// Fecha o dropdown ao clicar fora
function handleClickOutside(e: MouseEvent) {
  if (autoCompleteRef.value && !autoCompleteRef.value.contains(e.target as Node)) {
    fecharSugestoes()
  }
}

// Form
const loading = ref(false)
const showModal = ref(false)
const modalType = ref<'success' | 'error'>('success')
const modalMessage = ref('')

async function handleSubmit() {
  if (!form.value.veiculo_id) {
    modalType.value = 'error'
    modalMessage.value = 'Selecione um veículo válido a partir das sugestões.'
    showModal.value = true
    return
  }
  loading.value = true
  try {
    await manutencaoService.cadastrar(form.value)
    modalType.value = 'success'
    modalMessage.value = 'Manutenção registrada com sucesso!'
    showModal.value = true
  } catch (e: any) {
    modalType.value = 'error'
    modalMessage.value = extractErrorMessage(e, 'Erro ao cadastrar manutenção.')
    showModal.value = true
  } finally {
    loading.value = false
  }
}

function fecharModal() {
  showModal.value = false
  if (modalType.value === 'success') {
    router.push('/listagem/listar-manutencoes')
  }
}

onMounted(() => document.addEventListener('mousedown', handleClickOutside))
onBeforeUnmount(() => document.removeEventListener('mousedown', handleClickOutside))
</script>

<template>
  <div class="page-content">
    <div class="page-header">
      <h1 class="page-title">Cadastrar Manutenção</h1>
      <p class="page-subtitle">Registre uma manutenção preventiva ou corretiva</p>
    </div>

    <div class="form-card">
      <form @submit.prevent="handleSubmit" novalidate>

        <!-- Autocomplete de Placa -->
        <div class="form-group">
          <label for="mPlaca" class="form-label">Veículo (Placa)</label>
          <div class="autocomplete-wrapper" ref="autoCompleteRef">
            <div class="autocomplete-input-wrap">
              <input
                id="mPlaca"
                v-model="placaInput"
                type="text"
                class="form-control"
                :class="{ 'input-valid': veiculoSelecionado }"
                placeholder="Digite a placa para buscar..."
                autocomplete="off"
                :disabled="loading"
                @focus="sugestoesVisiveis = sugestoes.length > 0"
              />
              <span v-if="autoCompleteLoading" class="autocomplete-spinner">⏳</span>
              <span v-else-if="veiculoSelecionado" class="autocomplete-check">✅</span>
            </div>

            <!-- Dropdown de sugestões -->
            <transition name="dropdown">
              <ul
                v-if="sugestoesVisiveis && sugestoes.length > 0"
                class="autocomplete-dropdown"
                role="listbox"
              >
                <li
                  v-for="v in sugestoes"
                  :key="v.id"
                  class="autocomplete-item"
                  role="option"
                  @mousedown.prevent="selecionarSugestao(v)"
                >
                  <span class="autocomplete-placa">{{ v.placaVeiculo }}</span>
                  <span class="autocomplete-info">
                    {{ v.nome }} — {{ v.tipoVeiculo }}
                    <em v-if="v.base?.nome">· {{ v.base.nome }}</em>
                  </span>
                </li>
              </ul>
            </transition>
          </div>

          <!-- Veículo confirmado -->
          <div v-if="veiculoSelecionado" class="veiculo-selecionado-badge">
            🚛 <strong>{{ veiculoSelecionado.nome }}</strong>
            &nbsp;|&nbsp; Placa: <strong>{{ veiculoSelecionado.placaVeiculo }}</strong>
            &nbsp;|&nbsp; {{ veiculoSelecionado.tipoVeiculo }}
            <span v-if="veiculoSelecionado.base?.nome">&nbsp;· {{ veiculoSelecionado.base.nome }}</span>
          </div>
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
            :disabled="loading"
          />
        </div>

        <!-- Data Realização -->
        <div class="form-group">
          <label for="mData" class="form-label">Data de Agendamento</label>
          <input
            id="mData"
            v-model="form.dataAgendamento"
            type="date"
            class="form-control"
            required
            :disabled="loading"
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
            :disabled="loading"
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

        <!-- Serviços Solicitados (Opcional) -->
        <div class="form-group">
          <label class="form-label">Serviços Solicitados (Opcional)</label>
          <div class="servicos-grid">
            <label
              v-for="s in SERVICOS_LIST"
              :key="s.value"
              class="servico-tile"
              :class="{ selected: form.servicos?.includes(s.value) }"
            >
              <input
                type="checkbox"
                :value="s.value"
                :checked="form.servicos?.includes(s.value)"
                @change="toggleServico(s.value)"
              />
              <span class="servico-icon">{{ s.icon }}</span>
              <div class="servico-info">
                <span class="servico-label">{{ s.label }}</span>
                <span class="servico-desc">{{ s.description }}</span>
              </div>
            </label>
          </div>
        </div>

        <div class="form-actions">
          <RouterLink to="/listagem/listar-manutencoes" class="btn btn-secondary">
            Cancelar
          </RouterLink>
          <button
            type="submit"
            class="btn btn-primary"
            :disabled="loading || !form.veiculo_id || !form.dataAgendamento || !form.tipoManutencao"
          >
            <span v-if="loading">⏳ Salvando...</span>
            <span v-else>🔧 Registrar Manutenção</span>
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

<style scoped>
/* ---- Autocomplete ---- */
.autocomplete-wrapper {
  position: relative;
}

.autocomplete-input-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.autocomplete-input-wrap .form-control {
  padding-right: 2.5rem;
}

.autocomplete-spinner,
.autocomplete-check {
  position: absolute;
  right: 0.85rem;
  font-size: 1rem;
  pointer-events: none;
  line-height: 1;
}

.input-valid {
  border-color: rgba(34, 197, 94, 0.5) !important;
  box-shadow: 0 0 0 3px rgba(34, 197, 94, 0.12) !important;
}

/* Dropdown */
.autocomplete-dropdown {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  z-index: 200;
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  list-style: none;
  margin: 0;
  padding: 0.25rem 0;
  max-height: 260px;
  overflow-y: auto;
}

.autocomplete-item {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
  padding: 0.6rem 1rem;
  cursor: pointer;
  transition: background var(--transition);
  border-bottom: 1px solid rgba(255,255,255,0.04);
}

.autocomplete-item:last-child {
  border-bottom: none;
}

.autocomplete-item:hover {
  background: rgba(238, 130, 39, 0.12);
}

.autocomplete-placa {
  font-weight: 700;
  font-size: 0.95rem;
  color: var(--color-primary);
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.autocomplete-info {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

/* Badge do veículo selecionado */
.veiculo-selecionado-badge {
  margin-top: 0.5rem;
  padding: 0.5rem 0.85rem;
  background: rgba(34, 197, 94, 0.08);
  border: 1px solid rgba(34, 197, 94, 0.25);
  border-radius: var(--radius-sm);
  font-size: 0.85rem;
  color: var(--text-primary);
  animation: fadeSlideIn 0.2s ease;
}

/* Serviços grid */
.servicos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.servico-tile {
  display: flex;
  align-items: flex-start;
  gap: 0.6rem;
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 0.65rem 0.85rem;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}

.servico-tile input[type="checkbox"] {
  display: none;
}

.servico-tile:hover {
  border-color: var(--color-border-hover);
  background: var(--color-surface-3);
}

.servico-tile.selected {
  border-color: var(--accent-1);
  background: rgba(238, 130, 39, 0.1);
}

.servico-icon {
  font-size: 1.3rem;
  line-height: 1;
}

.servico-info {
  display: flex;
  flex-direction: column;
}

.servico-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-primary);
}

.servico-desc {
  font-size: 0.7rem;
  color: var(--text-secondary);
  line-height: 1.2;
}

/* Animação do dropdown */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
