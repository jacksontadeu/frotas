<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { veiculoService } from '../../services/veiculoService'
import { baseService } from '../../services/baseService'
import type { VeiculoDTORequest, BaseResponse } from '../../types'
import { TIPOS_VEICULO, CORES_VEICULO } from '../../types'
import AppModal from '../../components/AppModal.vue'
import { extractErrorMessage } from '../../composables/useErrorMessage'

const router = useRouter()

const form = ref<VeiculoDTORequest>({
  nome: '',
  placaVeiculo: '',
  frota: '',
  base_id: null,
  tipoVeiculo: '' as any,
  cor: '' as any,
  anoDeFabricacao: null,
})

const bases = ref<BaseResponse[]>([])
const loading = ref(false)
const loadingBases = ref(true)

// Modal
const showModal = ref(false)
const modalType = ref<'success' | 'error'>('success')
const modalMessage = ref('')

// ---- Validação de Placa ----
const placaStatus = ref<'idle' | 'checking' | 'ok' | 'duplicate'>('idle')
const placaMsg = ref('')

async function validarPlaca() {
  const placa = form.value.placaVeiculo.trim()
  if (!placa) { placaStatus.value = 'idle'; placaMsg.value = ''; return }
  placaStatus.value = 'checking'
  placaMsg.value = ''
  try {
    const encontrado = await veiculoService.buscarPorPlaca(placa)
    if (encontrado) {
      placaStatus.value = 'duplicate'
      placaMsg.value = `⚠️ Placa "${placa.toUpperCase()}" já está cadastrada (${encontrado.nome}).`
    } else {
      placaStatus.value = 'ok'
      placaMsg.value = '✅ Placa disponível.'
    }
  } catch {
    placaStatus.value = 'idle'
    placaMsg.value = ''
  }
}

// ---- Validação de Frota ----
const frotaStatus = ref<'idle' | 'checking' | 'ok' | 'duplicate'>('idle')
const frotaMsg = ref('')

async function validarFrota() {
  const frota = form.value.frota.trim()
  if (!frota) { frotaStatus.value = 'idle'; frotaMsg.value = ''; return }
  frotaStatus.value = 'checking'
  frotaMsg.value = ''
  try {
    const encontrado = await veiculoService.buscarPorFrota(frota)
    if (encontrado) {
      frotaStatus.value = 'duplicate'
      frotaMsg.value = `⚠️ Frota "${frota}" já está em uso (${encontrado.nome} — ${encontrado.placaVeiculo}).`
    } else {
      frotaStatus.value = 'ok'
      frotaMsg.value = '✅ Número de frota disponível.'
    }
  } catch {
    frotaStatus.value = 'idle'
    frotaMsg.value = ''
  }
}

async function carregarBases() {
  try {
    bases.value = await baseService.listarTodas()
  } catch {
    modalType.value = 'error'
    modalMessage.value = 'Erro ao carregar bases. Verifique se o servidor está rodando.'
    showModal.value = true
  } finally {
    loadingBases.value = false
  }
}

async function handleSubmit() {
  if (placaStatus.value === 'duplicate') {
    modalType.value = 'error'
    modalMessage.value = `A placa "${form.value.placaVeiculo.toUpperCase()}" já existe no sistema. Corrija antes de salvar.`
    showModal.value = true
    return
  }
  if (frotaStatus.value === 'duplicate') {
    modalType.value = 'error'
    modalMessage.value = `O número de frota "${form.value.frota}" já existe no sistema. Corrija antes de salvar.`
    showModal.value = true
    return
  }

  loading.value = true
  try {
    await veiculoService.cadastrar(form.value)
    modalType.value = 'success'
    modalMessage.value = 'Veículo cadastrado com sucesso!'
    showModal.value = true
  } catch (e: any) {
    modalType.value = 'error'
    modalMessage.value = extractErrorMessage(e, 'Erro ao cadastrar veículo.')
    showModal.value = true
  } finally {
    loading.value = false
  }
}

function fecharModal() {
  showModal.value = false
  if (modalType.value === 'success') {
    router.push('/listagem/listar-veiculos')
  }
}

onMounted(carregarBases)
</script>

<template>
  <div class="page-content">
    <div class="page-header">
      <h1 class="page-title">Cadastrar Veículo</h1>
      <p class="page-subtitle">Adicione um novo veículo à frota</p>
    </div>

    <div class="form-card" style="max-width: 680px;">
      <form @submit.prevent="handleSubmit" novalidate>
        <!-- Nome -->
        <div class="form-group">
          <label for="vNome" class="form-label">Nome do Veículo</label>
          <input
            id="vNome"
            v-model="form.nome"
            type="text"
            class="form-control"
            placeholder="Ex: Caminhão Scania R450"
            required
            :disabled="loading"
          />
        </div>

        <!-- Placa / Frota -->
        <div class="row">
          <!-- Placa -->
          <div class="form-group">
            <label for="vPlaca" class="form-label">Placa</label>
            <div class="field-validate-wrap">
              <input
                id="vPlaca"
                v-model="form.placaVeiculo"
                type="text"
                class="form-control"
                :class="{
                  'input-valid':   placaStatus === 'ok',
                  'input-invalid': placaStatus === 'duplicate',
                }"
                placeholder="Ex: ABC1234"
                required
                :disabled="loading"
                @blur="validarPlaca"
              />
              <span v-if="placaStatus === 'checking'" class="field-spinner">⏳</span>
              <span v-else-if="placaStatus === 'ok'"  class="field-icon field-ok">✅</span>
              <span v-else-if="placaStatus === 'duplicate'" class="field-icon field-err">❌</span>
            </div>
            <p v-if="placaMsg" class="field-hint" :class="placaStatus === 'ok' ? 'hint-ok' : 'hint-err'">
              {{ placaMsg }}
            </p>
          </div>

          <!-- Frota -->
          <div class="form-group">
            <label for="vFrota" class="form-label">Frota</label>
            <div class="field-validate-wrap">
              <input
                id="vFrota"
                v-model="form.frota"
                type="text"
                class="form-control"
                :class="{
                  'input-valid':   frotaStatus === 'ok',
                  'input-invalid': frotaStatus === 'duplicate',
                }"
                placeholder="Número da frota"
                required
                :disabled="loading"
                @blur="validarFrota"
              />
              <span v-if="frotaStatus === 'checking'" class="field-spinner">⏳</span>
              <span v-else-if="frotaStatus === 'ok'"  class="field-icon field-ok">✅</span>
              <span v-else-if="frotaStatus === 'duplicate'" class="field-icon field-err">❌</span>
            </div>
            <p v-if="frotaMsg" class="field-hint" :class="frotaStatus === 'ok' ? 'hint-ok' : 'hint-err'">
              {{ frotaMsg }}
            </p>
          </div>
        </div>

        <!-- Cor / Ano -->
        <div class="row">
          <div class="form-group">
            <label for="vCor" class="form-label">Cor</label>
            <select
              id="vCor"
              v-model="form.cor"
              class="form-select"
              required
              :disabled="loading"
            >
              <option value="" disabled selected>Selecione a cor</option>
              <option v-for="cor in CORES_VEICULO" :key="cor" :value="cor">
                {{ cor }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label for="vAno" class="form-label">Ano de Fabricação</label>
            <input
              id="vAno"
              v-model.number="form.anoDeFabricacao"
              type="number"
              class="form-control"
              placeholder="Ex: 2022"
              min="1900"
              :max="new Date().getFullYear() + 1"
              required
              :disabled="loading"
            />
          </div>
        </div>

        <!-- Base / Tipo -->
        <div class="row">
          <div class="form-group">
            <label for="vBase" class="form-label">Base</label>
            <div v-if="loadingBases" class="form-control" style="color: var(--text-muted);">
              Carregando bases...
            </div>
            <select
              v-else
              id="vBase"
              v-model.number="form.base_id"
              class="form-select"
              required
              :disabled="loading"
            >
              <option value="" disabled selected>Selecione a base</option>
              <option
                v-for="base in bases"
                :key="base.id"
                :value="base.id"
              >
                {{ base.nome }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label for="vTipo" class="form-label">Tipo do Veículo</label>
            <select
              id="vTipo"
              v-model="form.tipoVeiculo"
              class="form-select"
              required
              :disabled="loading"
            >
              <option value="" disabled selected>Selecione o tipo</option>
              <option v-for="tipo in TIPOS_VEICULO" :key="tipo" :value="tipo">
                {{ tipo }}
              </option>
            </select>
          </div>
        </div>

        <div class="form-actions">
          <RouterLink to="/listagem/listar-veiculos" class="btn btn-secondary">
            Cancelar
          </RouterLink>
          <button
            type="submit"
            class="btn btn-primary"
            :disabled="loading || placaStatus === 'duplicate' || frotaStatus === 'duplicate'"
          >
            <span v-if="loading">⏳ Salvando...</span>
            <span v-else>🚗 Cadastrar Veículo</span>
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
/* Wrapper de campo com ícone flutuante */
.field-validate-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.field-validate-wrap .form-control {
  padding-right: 2.5rem;
}

.field-spinner,
.field-icon {
  position: absolute;
  right: 0.85rem;
  font-size: 1rem;
  pointer-events: none;
  line-height: 1;
}

.field-ok  { color: #22c55e; }
.field-err { color: #ef4444; }

/* Bordas de validação */
.input-valid {
  border-color: rgba(34, 197, 94, 0.55) !important;
  box-shadow: 0 0 0 3px rgba(34, 197, 94, 0.12) !important;
}

.input-invalid {
  border-color: rgba(239, 68, 68, 0.6) !important;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.12) !important;
}

/* Mensagem abaixo do campo */
.field-hint {
  margin-top: 0.35rem;
  font-size: 0.78rem;
  font-weight: 500;
  animation: fadeSlideIn 0.2s ease;
}

.hint-ok  { color: #22c55e; }
.hint-err { color: #f87171; }

@keyframes fadeSlideIn {
  from { opacity: 0; transform: translateY(-4px); }
  to   { opacity: 1; transform: translateY(0); }
}
</style>