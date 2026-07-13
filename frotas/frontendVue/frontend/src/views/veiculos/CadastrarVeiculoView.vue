<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { veiculoService } from '../../services/veiculoService'
import { baseService } from '../../services/baseService'
import type { VeiculoDTORequest, BaseResponse } from '../../types'
import { TIPOS_VEICULO } from '../../types'

const router = useRouter()

const form = ref<VeiculoDTORequest>({
  nome: '',
  placaVeiculo: '',
  frota: '',
  base_id: null,
  tipoVeiculo: '',
  cor: '',
  anoDeFabricacao: null,
})

const bases = ref<BaseResponse[]>([])
const loading = ref(false)
const loadingBases = ref(true)
const error = ref<string | null>(null)
const success = ref(false)

async function carregarBases() {
  try {
    bases.value = await baseService.listarTodas()
  } catch {
    error.value = 'Erro ao carregar bases. Verifique se o servidor está rodando.'
  } finally {
    loadingBases.value = false
  }
}

async function handleSubmit() {
  error.value = null
  loading.value = true
  try {
    await veiculoService.cadastrar(form.value)
    success.value = true
    setTimeout(() => router.push('/listagem/listar-veiculos'), 1500)
  } catch (e: any) {
    error.value = e?.response?.data?.message || 'Erro ao cadastrar veículo.'
  } finally {
    loading.value = false
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
      <div v-if="success" class="alert alert-success">
        ✅ Veículo cadastrado com sucesso! Redirecionando...
      </div>
      <div v-if="error" class="alert alert-danger">⚠️ {{ error }}</div>

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
            :disabled="loading || success"
          />
        </div>

        <!-- Placa / Frota -->
        <div class="row">
          <div class="form-group">
            <label for="vPlaca" class="form-label">Placa</label>
            <input
              id="vPlaca"
              v-model="form.placaVeiculo"
              type="text"
              class="form-control"
              placeholder="Ex: ABC1234"
              required
              :disabled="loading || success"
            />
          </div>
          <div class="form-group">
            <label for="vFrota" class="form-label">Frota</label>
            <input
              id="vFrota"
              v-model="form.frota"
              type="text"
              class="form-control"
              placeholder="Número da frota"
              required
              :disabled="loading || success"
            />
          </div>
        </div>

        <!-- Cor / Ano -->
        <div class="row">
          <div class="form-group">
            <label for="vCor" class="form-label">Cor</label>
            <input
              id="vCor"
              v-model="form.cor"
              type="text"
              class="form-control"
              placeholder="Ex: Branco"
              required
              :disabled="loading || success"
            />
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
              :disabled="loading || success"
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
              :disabled="loading || success"
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
              :disabled="loading || success"
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
            :disabled="loading || success"
          >
            <span v-if="loading">⏳ Salvando...</span>
            <span v-else>🚗 Cadastrar Veículo</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
