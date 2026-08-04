<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { veiculoService } from '../../services/veiculoService'
import { manutencaoService } from '../../services/manutencaoService'
import { baseService } from '../../services/baseService'
import type { VeiculoResponse, ManutencaoResponse, BaseResponse } from '../../types'
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

// ── Manutenções por veículo ──────────────────────────────────────────────────
const manutencoes = ref<ManutencaoResponse[]>([])



function formatarKm(km: number | null | undefined): string {
  if (km == null) return '—'
  return km.toLocaleString('pt-BR') + ' km'
}

function formatarData(data: string | null | undefined): string {
  if (!data) return '—'
  const [ano, mes, dia] = data.split('T')[0].split('-')
  return `${dia}/${mes}/${ano}`
}

type StatusManutencao = 'ok' | 'alerta' | 'vencida' | 'sem-info'

function statusProximaManutencao(data: string | null | undefined): StatusManutencao {
  if (!data) return 'sem-info'
  const hoje = new Date()
  hoje.setHours(0, 0, 0, 0)
  const proxima = new Date(data + 'T00:00:00')
  const diffDias = Math.ceil((proxima.getTime() - hoje.getTime()) / 86400000)
  if (diffDias < 0) return 'vencida'
  if (diffDias <= 30) return 'alerta'
  return 'ok'
}



async function carregarVeiculos() {
  try {
    loading.value = true
    error.value = null
    const [veiculosData, manutencoesData] = await Promise.all([
      veiculoService.listarTodos(),
      manutencaoService.listarTodas(),
    ])
    veiculos.value = veiculosData
    manutencoes.value = manutencoesData
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

// ── Modal de Detalhes do Veículo ─────────────────────────────────────────────
const veiculoDetalhes = ref<VeiculoResponse | null>(null)

function abrirDetalhes(veiculo: VeiculoResponse) {
  veiculoDetalhes.value = veiculo
}

function fecharDetalhes() {
  veiculoDetalhes.value = null
}

function imprimirDetalhes() {
  document.body.classList.add('printing-modal-active')
  window.print()
  document.body.classList.remove('printing-modal-active')
}

const manutencoesDoVeiculoSelecionado = computed(() => {
  if (!veiculoDetalhes.value) return []
  return manutencoes.value
    .filter((m) => m.veiculo.id === veiculoDetalhes.value!.id)
    .sort((a, b) => (b.dataAgendamento ?? '').localeCompare(a.dataAgendamento ?? ''))
})

const formatarTipoManutencao = (tipo: string) => {
  const map: Record<string, string> = {
    PREVENTIVA_TROCA_DE_OLEO: 'Prev. - Troca de Óleo',
    PREVENTIVA_KIT_CORREIA_DENTADA: 'Prev. - Kit Correia',
    CORRETIVA: 'Corretiva',
  }
  return map[tipo] || tipo
}

const router = useRouter()
function navegarParaManutencao(veiculoId: number) {
  router.push({ name: 'cadastrar-manutencao', query: { veiculoId } })
}

const tipoIcon: Record<string, string> = {
  CARRO: '🚗',
  CAMINHAO: '🚛',
  VAN: '🚐',
}

// ── Modal Trocar Base ────────────────────────────────────────────────────────
const showModalBase = ref(false)
const basesDisponiveis = ref<BaseResponse[]>([])
const baseSelecionadaId = ref<number | null>(null)
const veiculoAlvoBase = ref<VeiculoResponse | null>(null)
const salvandoBase = ref(false)

async function abrirModalBase(veiculo: VeiculoResponse) {
  veiculoAlvoBase.value = veiculo
  baseSelecionadaId.value = veiculo.base?.id ?? null
  basesDisponiveis.value = await baseService.listarTodas()
  showModalBase.value = true
}

async function confirmarTrocaBase() {
  if (!veiculoAlvoBase.value || !baseSelecionadaId.value) return
  try {
    salvandoBase.value = true
    await veiculoService.trocarBase(veiculoAlvoBase.value.id, baseSelecionadaId.value)
    // Atualiza localmente
    const baseNova = basesDisponiveis.value.find((b) => b.id === baseSelecionadaId.value)!
    const idx = veiculos.value.findIndex((v) => v.id === veiculoAlvoBase.value!.id)
    if (idx !== -1) veiculos.value[idx] = { ...veiculos.value[idx], base: baseNova }
    showModalBase.value = false
    modalType.value = 'success'
    modalMessage.value = `Base do veículo "${veiculoAlvoBase.value.nome}" alterada para "${baseNova.nome}" com sucesso!`
    showModal.value = true
  } catch (e: any) {
    showModalBase.value = false
    modalType.value = 'error'
    modalMessage.value = extractErrorMessage(e, 'Erro ao trocar a base do veículo.')
    showModal.value = true
  } finally {
    salvandoBase.value = false
  }
}

// ── Modal Atualizar Kilometragem ─────────────────────────────────────────────
const showModalKm = ref(false)
const novaKm = ref<number | null>(null)
const kmMinima = ref(0)
const veiculoAlvoKm = ref<VeiculoResponse | null>(null)
const salvandoKm = ref(false)
const erroKm = ref<string | null>(null)

function abrirModalKm(veiculo: VeiculoResponse) {
  veiculoAlvoKm.value = veiculo
  kmMinima.value = veiculo.kilometragemAtual ?? 0
  novaKm.value = null
  erroKm.value = null
  showModalKm.value = true
}

async function confirmarAtualizacaoKm() {
  if (!veiculoAlvoKm.value || novaKm.value == null) return
  if (novaKm.value < kmMinima.value) {
    erroKm.value = `A kilometragem não pode ser menor que a atual (${kmMinima.value.toLocaleString('pt-BR')} km).`
    return
  }
  try {
    salvandoKm.value = true
    erroKm.value = null
    await veiculoService.atualizarKilometragem(veiculoAlvoKm.value.id, novaKm.value)
    // Atualiza localmente
    const idx = veiculos.value.findIndex((v) => v.id === veiculoAlvoKm.value!.id)
    if (idx !== -1) veiculos.value[idx] = { ...veiculos.value[idx], kilometragemAtual: novaKm.value }
    showModalKm.value = false
    modalType.value = 'success'
    modalMessage.value = `Kilometragem do veículo "${veiculoAlvoKm.value.nome}" atualizada para ${novaKm.value.toLocaleString('pt-BR')} km!`
    showModal.value = true
  } catch (e: any) {
    erroKm.value = extractErrorMessage(e, 'Erro ao atualizar a kilometragem.')
  } finally {
    salvandoKm.value = false
  }
}

// ── Modo de visualização (persistido) ───────────────────────────────────────
type ViewMode = 'cards' | 'list'
const viewMode = ref<ViewMode>(
  (localStorage.getItem('veiculos-view-mode') as ViewMode) ?? 'cards'
)
function setViewMode(mode: ViewMode) {
  viewMode.value = mode
  localStorage.setItem('veiculos-view-mode', mode)
}

// ── Busca (placa / frota) ────────────────────────────────────────────────────
const buscaQuery = ref('')

// ── Filtro por Base ─────────────────────────────────────────────────────────
const basesDisponiveisFiltro = computed(() => {
  const nomes = new Set<string>()
  veiculos.value.forEach(v => {
    if (v.base?.nome) nomes.add(v.base.nome)
  })
  return Array.from(nomes).sort()
})
const filtroBase = ref<string>('todas')

const veiculosFiltrados = computed(() => {
  let resultado = veiculos.value

  if (filtroBase.value !== 'todas') {
    if (filtroBase.value === 'sem-base') {
      resultado = resultado.filter(v => !v.base?.nome)
    } else {
      resultado = resultado.filter(v => v.base?.nome === filtroBase.value)
    }
  }

  if (buscaQuery.value.trim()) {
    const q = buscaQuery.value.trim().toLowerCase()
    resultado = resultado.filter(
      (v) =>
        v.placaVeiculo?.toLowerCase().includes(q) ||
        v.frota?.toLowerCase().includes(q)
    )
  }
  return resultado
})

// ── Veículos agrupados por base ──────────────────────────────────────────────
const gruposPorBase = computed(() => {
  const mapa = new Map<string, VeiculoResponse[]>()
  for (const v of veiculosFiltrados.value) {
    const nomeBase = v.base?.nome || 'Sem base definida'
    if (!mapa.has(nomeBase)) mapa.set(nomeBase, [])
    mapa.get(nomeBase)!.push(v)
  }
  for (const lista of mapa.values()) {
    lista.sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'))
  }
  const grupos = Array.from(mapa.entries()).map(([base, itens]) => ({ base, itens }))
  grupos.sort((a, b) => a.base.localeCompare(b.base, 'pt-BR'))
  return grupos
})

onMounted(carregarVeiculos)
</script>

<template>
  <div class="page-content">
    <div class="page-header-row">
      <div>
        <h1 class="page-title">🚛 Veículos</h1>
        <p class="page-subtitle">
          {{ veiculosFiltrados.length }} de {{ veiculos.length }} veículo(s)
        </p>
      </div>
      <div class="header-actions">
        <!-- Toggle visualização -->
        <div class="view-toggle" role="group" aria-label="Modo de visualização">
          <button
            :class="['view-toggle-btn', { active: viewMode === 'cards' }]"
            @click="setViewMode('cards')"
            title="Visualização em cards"
          >
            <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
              <rect x="1" y="1" width="6" height="6" rx="1.5"/>
              <rect x="9" y="1" width="6" height="6" rx="1.5"/>
              <rect x="1" y="9" width="6" height="6" rx="1.5"/>
              <rect x="9" y="9" width="6" height="6" rx="1.5"/>
            </svg>
            Cards
          </button>
          <button
            :class="['view-toggle-btn', { active: viewMode === 'list' }]"
            @click="setViewMode('list')"
            title="Visualização em lista"
          >
            <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
              <rect x="1" y="2" width="14" height="2" rx="1"/>
              <rect x="1" y="7" width="14" height="2" rx="1"/>
              <rect x="1" y="12" width="14" height="2" rx="1"/>
            </svg>
            Lista
          </button>
        </div>
        <RouterLink to="/cadastros/cadastrar-veiculo" class="btn btn-primary">
          + Novo Veículo
        </RouterLink>
      </div>
    </div>

    <!-- Filtros: busca por placa/frota + filtro por base -->
    <div v-if="!loading && !error && veiculos.length > 0" class="filtros-bar">
      <div class="busca-wrap">
        <span class="busca-icon">🔍</span>
        <input
          v-model="buscaQuery"
          type="text"
          class="form-control"
          placeholder="Buscar por placa ou frota..."
        />
        <button
          v-if="buscaQuery"
          class="busca-clear"
          @click="buscaQuery = ''"
          title="Limpar busca"
        >✖</button>
      </div>

      <div class="ordenacao-wrap">
        <label class="ordenacao-label" for="filtro-base">Filtrar por Base</label>
        <select id="filtro-base" v-model="filtroBase" class="form-control">
          <option value="todas">Todas as bases</option>
          <option v-for="baseNome in basesDisponiveisFiltro" :key="baseNome" :value="baseNome">{{ baseNome }}</option>
          
        </select>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>Carregando veículos...</p>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="alert alert-danger">⚠️ {{ error }}</div>

    <!-- Empty (sem veículos cadastrados) -->
    <div v-else-if="veiculos.length === 0" class="empty-state">
      <span class="empty-state-icon">🚗</span>
      <p>Nenhum veículo cadastrado ainda.</p>
      <RouterLink to="/cadastros/cadastrar-veiculo" class="btn btn-primary">
        Cadastrar primeiro veículo
      </RouterLink>
    </div>

    <!-- Empty (busca sem resultado) -->
    <div v-else-if="veiculosFiltrados.length === 0" class="empty-state">
      <span class="empty-state-icon">🔍</span>
      <p>Nenhum veículo encontrado para "{{ buscaQuery }}".</p>
      <button class="btn btn-secondary" @click="buscaQuery = ''">Limpar busca</button>
    </div>

    <!-- ── GRID DE CARDS (agrupado por base) ── -->
    <div v-else-if="viewMode === 'cards'" class="grupos-wrap">
      <section v-for="grupo in gruposPorBase" :key="grupo.base" class="grupo-base">
        <div class="grupo-base-header">
          <span class="grupo-base-icon">🏢</span>
          <h2 class="grupo-base-nome">{{ grupo.base }}</h2>
          <span class="grupo-base-count">{{ grupo.itens.length }} veículo(s)</span>
        </div>

        <div class="veiculos-grid">
          <div
            v-for="veiculo in grupo.itens"
            :key="veiculo.id"
            class="veiculo-card clickable"
            @click="abrirDetalhes(veiculo)"
          >
            <div class="veiculo-card-stripe"></div>

            <div class="veiculo-card-top">
              <span class="veiculo-card-icon">{{ tipoIcon[veiculo.tipoVeiculo] ?? '🚙' }}</span>
              <div class="veiculo-card-top-text">
                <h3 class="veiculo-card-nome">{{ veiculo.nome }}</h3>
                <span class="placa-badge">{{ veiculo.placaVeiculo }}</span>
              </div>
              <span class="badge badge-purple">{{ veiculo.tipoVeiculo }}</span>
            </div>

            <div class="veiculo-card-divider"></div>

            <div class="veiculo-card-info">
              <div class="info-row">
                <span class="info-icon">📋</span>
                <div class="info-text">
                  <span class="info-label">Frota</span>
                  <span class="info-value">{{ veiculo.frota || '—' }}</span>
                </div>
              </div>
              <div class="info-row">
                <span class="info-icon">🎨</span>
                <div class="info-text">
                  <span class="info-label">Cor</span>
                  <span class="info-value">{{ veiculo.cor || '—' }}</span>
                </div>
              </div>
              <div class="info-row">
                <span class="info-icon">📅</span>
                <div class="info-text">
                  <span class="info-label">Ano</span>
                  <span class="info-value">{{ veiculo.anoDeFabricacao || '—' }}</span>
                </div>
              </div>

              <!-- Kilometragem -->
              <div class="info-row">
                <span class="info-icon">🛣️</span>
                <div class="info-text">
                  <span class="info-label">Kilometragem Atual</span>
                  <span class="info-value">{{ formatarKm(veiculo.kilometragemAtual) }}</span>
                </div>
              </div>

              <!-- Últ. Óleo -->
              <div class="info-row">
                <span class="info-icon">🛢️</span>
                <div class="info-text">
                  <span class="info-label">Últ. Troca Óleo</span>
                  <span class="info-value">
                    {{ formatarData(veiculo.dataUltimaTrocaOleo) }}
                  </span>
                </div>
              </div>

              <!-- Últ. Correia -->
              <div class="info-row">
                <span class="info-icon">⚙️</span>
                <div class="info-text">
                  <span class="info-label">Últ. Troca Correia</span>
                  <span class="info-value">
                    {{ formatarData(veiculo.dataUltimaCorreiaDentada) }}
                  </span>
                </div>
              </div>

              <!-- Últ. Corretiva -->
              <div class="info-row">
                <span class="info-icon">🔧</span>
                <div class="info-text">
                  <span class="info-label">Últ. Corretiva</span>
                  <span class="info-value">
                    {{ formatarData(veiculo.dataUltimaCorretiva) }}
                    <span style="color: var(--text-muted); font-size: 0.85em; margin-left: 0.3rem;">({{ formatarKm(veiculo.kmCorretiva) }})</span>
                  </span>
                </div>
              </div>

              <!-- Próx. Óleo -->
              <div class="info-row">
                <span class="info-icon">🛢️</span>
                <div class="info-text">
                  <span class="info-label">Próx. Troca Óleo</span>
                  <span
                    class="info-value proxima-manutencao"
                    :class="'status-' + statusProximaManutencao(veiculo.dataProximaTrocaOleo)"
                  >
                    <span v-if="statusProximaManutencao(veiculo.dataProximaTrocaOleo) !== 'sem-info'"
                      class="status-dot" :class="'dot-' + statusProximaManutencao(veiculo.dataProximaTrocaOleo)"></span>
                    {{ formatarData(veiculo.dataProximaTrocaOleo) }} <span style="color: var(--text-muted); font-size: 0.85em; margin-left: 0.3rem;">({{ formatarKm(veiculo.kmTrocaOleo) }})</span>
                  </span>
                </div>
              </div>

              <!-- Próx. Correia -->
              <div class="info-row">
                <span class="info-icon">⚙️</span>
                <div class="info-text">
                  <span class="info-label">Próx. Troca Correia</span>
                  <span
                    class="info-value proxima-manutencao"
                    :class="'status-' + statusProximaManutencao(veiculo.dataProximaCorreiaDentada)"
                  >
                    <span v-if="statusProximaManutencao(veiculo.dataProximaCorreiaDentada) !== 'sem-info'"
                      class="status-dot" :class="'dot-' + statusProximaManutencao(veiculo.dataProximaCorreiaDentada)"></span>
                    {{ formatarData(veiculo.dataProximaCorreiaDentada) }} <span style="color: var(--text-muted); font-size: 0.85em; margin-left: 0.3rem;">({{ formatarKm(veiculo.kmTrocaCorreiaDentada) }})</span>
                  </span>
                </div>
              </div>
            </div>

            <div class="veiculo-card-footer">
              <button
                class="btn-card-action btn-card-manutencao"
                @click.stop="navegarParaManutencao(veiculo.id)"
                title="Registrar manutenção"
              >
                🔧 Manutenção
              </button>
              <button
                class="btn-card-action btn-card-km"
                @click.stop="abrirModalKm(veiculo)"
                title="Atualizar kilometragem"
              >
                🛣️ KM
              </button>
              <button
                class="btn-card-action btn-card-base"
                @click.stop="abrirModalBase(veiculo)"
                title="Trocar base"
              >
                🏢 Base
              </button>
              <button
                class="btn-card-action btn-card-delete"
                :disabled="deletingId === veiculo.id"
                @click.stop="confirmarExclusao(veiculo.id, veiculo.nome)"
              >
                {{ deletingId === veiculo.id ? '⏳' : '🗑️' }} Excluir
              </button>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- ── LISTA (agrupada por base) ── -->
    <div v-else class="grupos-wrap">
      <section v-for="grupo in gruposPorBase" :key="grupo.base" class="grupo-base">
        <div class="grupo-base-header">
          <span class="grupo-base-icon">🏢</span>
          <h2 class="grupo-base-nome">{{ grupo.base }}</h2>
          <span class="grupo-base-count">{{ grupo.itens.length }} veículo(s)</span>
        </div>

        <ul style="list-style: none; display: flex; flex-direction: column; gap: 0.75rem;">
          <li
            v-for="veiculo in grupo.itens"
            :key="veiculo.id"
            class="list-item clickable"
            @click="abrirDetalhes(veiculo)"
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
                <span>🛣️ KM: <strong>{{ formatarKm(veiculo.kilometragemAtual) }}</strong></span>

                <span>🛢️ Últ. Óleo: <strong>{{ formatarData(veiculo.dataUltimaTrocaOleo) }}</strong></span>
                <span>⚙️ Últ. Correia: <strong>{{ formatarData(veiculo.dataUltimaCorreiaDentada) }}</strong></span>
                <span>🔧 Últ. Corretiva: <strong>{{ formatarData(veiculo.dataUltimaCorretiva) }} ({{ formatarKm(veiculo.kmCorretiva) }})</strong></span>
                <span
                  :class="'list-proxima status-' + statusProximaManutencao(veiculo.dataProximaTrocaOleo)"
                >
                  <span v-if="statusProximaManutencao(veiculo.dataProximaTrocaOleo) !== 'sem-info'"
                    class="status-dot" :class="'dot-' + statusProximaManutencao(veiculo.dataProximaTrocaOleo)"></span>
                  🛢️ Próx. Óleo: <strong>{{ formatarData(veiculo.dataProximaTrocaOleo) }} ({{ formatarKm(veiculo.kmTrocaOleo) }})</strong>
                </span>
                <span
                  :class="'list-proxima status-' + statusProximaManutencao(veiculo.dataProximaCorreiaDentada)"
                >
                  <span v-if="statusProximaManutencao(veiculo.dataProximaCorreiaDentada) !== 'sem-info'"
                    class="status-dot" :class="'dot-' + statusProximaManutencao(veiculo.dataProximaCorreiaDentada)"></span>
                  ⚙️ Próx. Correia: <strong>{{ formatarData(veiculo.dataProximaCorreiaDentada) }} ({{ formatarKm(veiculo.kmTrocaCorreiaDentada) }})</strong>
                </span>
              </p>
            </div>
            <div class="list-item-actions">
              <button
                class="btn btn-warning btn-sm"
                @click.stop="navegarParaManutencao(veiculo.id)"
              >
                🔧 Manutenção
              </button>
              <button
                class="btn btn-km btn-sm"
                @click.stop="abrirModalKm(veiculo)"
              >
                🛣️ Atualizar KM
              </button>
              <button
                class="btn btn-base btn-sm"
                @click.stop="abrirModalBase(veiculo)"
              >
                🏢 Trocar Base
              </button>
              <button
                class="btn btn-danger btn-sm"
                :disabled="deletingId === veiculo.id"
                @click.stop="confirmarExclusao(veiculo.id, veiculo.nome)"
              >
                {{ deletingId === veiculo.id ? '⏳' : '🗑️' }} Excluir
              </button>
            </div>
          </li>
        </ul>
      </section>
    </div>
  </div>

  <!-- Modal de confirmação/exclusão -->
  <AppModal
    :show="showModal"
    :type="modalType"
    :message="modalMessage"
    confirm-label="Sim, excluir"
    cancel-label="Cancelar"
    @close="fecharModal"
    @confirm="executarExclusao"
  />

  <!-- ── Modal Trocar Base ── -->
  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="showModalBase" class="custom-modal-overlay" @click.self="showModalBase = false">
        <div class="custom-modal" role="dialog" aria-modal="true">
          <div class="custom-modal-header">
            <span class="custom-modal-icon">🏢</span>
            <h2 class="custom-modal-title">Trocar Base</h2>
            <button class="custom-modal-close" @click="showModalBase = false">✕</button>
          </div>
          <div class="custom-modal-body">
            <p class="custom-modal-subtitle">
              Veículo: <strong>{{ veiculoAlvoBase?.nome }}</strong>
              — Base atual: <strong>{{ veiculoAlvoBase?.base?.nome ?? '—' }}</strong>
            </p>
            <div class="base-select-wrap">
              <span class="base-select-icon">🏢</span>
              <select
                id="select-base"
                v-model.number="baseSelecionadaId"
                class="form-control base-select"
              >
                <option :value="null" disabled>Selecione uma base...</option>
                <option
                  v-for="base in basesDisponiveis"
                  :key="base.id"
                  :value="base.id"
                >
                  {{ base.nome }}
                </option>
              </select>
              <span class="base-select-arrow">▾</span>
            </div>
          </div>
          <div class="custom-modal-footer">
            <button class="btn btn-secondary" @click="showModalBase = false">Cancelar</button>
            <button
              class="btn btn-primary"
              :disabled="salvandoBase || !baseSelecionadaId"
              @click="confirmarTrocaBase"
            >
              {{ salvandoBase ? '⏳ Salvando...' : '✅ Confirmar Troca' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>

  <!-- ── Modal Atualizar Kilometragem ── -->
  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="showModalKm" class="custom-modal-overlay" @click.self="showModalKm = false">
        <div class="custom-modal custom-modal--sm" role="dialog" aria-modal="true">
          <div class="custom-modal-header">
            <span class="custom-modal-icon">🛣️</span>
            <h2 class="custom-modal-title">Atualizar Kilometragem</h2>
            <button class="custom-modal-close" @click="showModalKm = false">✕</button>
          </div>
          <div class="custom-modal-body">
            <p class="custom-modal-subtitle">
              Veículo: <strong>{{ veiculoAlvoKm?.nome }}</strong>
            </p>
            <div class="km-atual-info">
              <span class="km-atual-label">KM atual registrada</span>
              <span class="km-atual-valor">{{ (veiculoAlvoKm?.kilometragemAtual ?? 0).toLocaleString('pt-BR') }} km</span>
            </div>
            <div class="form-group">
              <label class="form-label" for="input-nova-km">Nova Kilometragem</label>
              <div class="km-input-wrap">
                <input
                  id="input-nova-km"
                  v-model.number="novaKm"
                  type="number"
                  class="form-control km-input"
                  :min="kmMinima"
                  placeholder="Ex: 125000"
                  @keyup.enter="confirmarAtualizacaoKm"
                />
                <span class="km-input-suffix">km</span>
              </div>
              <p v-if="erroKm" class="km-erro">⚠️ {{ erroKm }}</p>
            </div>
          </div>
          <div class="custom-modal-footer">
            <button class="btn btn-secondary" @click="showModalKm = false">Cancelar</button>
            <button
              class="btn btn-primary"
              :disabled="salvandoKm || novaKm == null"
              @click="confirmarAtualizacaoKm"
            >
              {{ salvandoKm ? '⏳ Salvando...' : '✅ Atualizar' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>

  <!-- ── Modal Detalhes do Veículo ── -->
  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="veiculoDetalhes" class="custom-modal-overlay printable-overlay" @click.self="fecharDetalhes">
        <div class="custom-modal printable-modal" role="dialog" aria-modal="true">
          <div class="custom-modal-header no-print">
            <span class="custom-modal-icon">{{ tipoIcon[veiculoDetalhes.tipoVeiculo] ?? '🚙' }}</span>
            <h2 class="custom-modal-title">Detalhes do Veículo</h2>
            <button class="custom-modal-close" @click="fecharDetalhes">✕</button>
          </div>

          <!-- Versão impressão do header (simplificada) -->
          <div class="print-only" style="display: none; padding: 1.5rem; border-bottom: 2px solid #ddd;">
            <h2 style="margin: 0; font-size: 1.5rem;">Detalhes do Veículo - <span class="placa-badge">{{ veiculoDetalhes.placaVeiculo }}</span></h2>
          </div>

          <div class="custom-modal-body">
            <p class="custom-modal-subtitle no-print">
              Visualizando detalhes completos de <strong>{{ veiculoDetalhes.nome }}</strong>.
            </p>

            <div class="modal-section">
              <h4 class="modal-section-title">🚙 Informações Gerais</h4>
              <div class="modal-grid-info">
                <div class="modal-info-item">
                  <span class="modal-info-label">Veículo / Modelo</span>
                  <span class="modal-info-value">{{ veiculoDetalhes.nome }}</span>
                </div>
                <div class="modal-info-item">
                  <span class="modal-info-label">Placa</span>
                  <span class="modal-info-value"><span class="placa-badge">{{ veiculoDetalhes.placaVeiculo }}</span></span>
                </div>
                <div class="modal-info-item">
                  <span class="modal-info-label">Frota</span>
                  <span class="modal-info-value">{{ veiculoDetalhes.frota || '—' }}</span>
                </div>
                <div class="modal-info-item">
                  <span class="modal-info-label">Cor</span>
                  <span class="modal-info-value">{{ veiculoDetalhes.cor || '—' }}</span>
                </div>
                <div class="modal-info-item">
                  <span class="modal-info-label">Ano</span>
                  <span class="modal-info-value">{{ veiculoDetalhes.anoDeFabricacao || '—' }}</span>
                </div>
                <div class="modal-info-item">
                  <span class="modal-info-label">KM Atual</span>
                  <span class="modal-info-value">{{ formatarKm(veiculoDetalhes.kilometragemAtual) }}</span>
                </div>

                <div class="modal-info-item">
                  <span class="modal-info-label">Tipo</span>
                  <span class="modal-info-value badge badge-purple" style="font-size: 0.75rem;">{{ veiculoDetalhes.tipoVeiculo }}</span>
                </div>
                <div class="modal-info-item">
                  <span class="modal-info-label">Base Atual</span>
                  <span class="modal-info-value">{{ veiculoDetalhes.base?.nome || 'Sem base definida' }}</span>
                </div>
              </div>
            </div>

            <div class="modal-section" style="margin-top: 1.5rem;">
              <h4 class="modal-section-title">🛣️ Desempenho e Manutenção</h4>
              <div class="modal-grid-info">
                <div class="modal-info-item">
                  <span class="modal-info-label">Kilometragem Atual</span>
                  <span class="modal-info-value" style="color: #38bdf8; font-weight: 700;">{{ formatarKm(veiculoDetalhes.kilometragemAtual) }}</span>
                </div>
                <div class="modal-info-item">
                  <span class="modal-info-label">Últ. Troca Óleo</span>
                  <span class="modal-info-value">
                    {{ formatarData(veiculoDetalhes.dataUltimaTrocaOleo) }}
                  </span>
                </div>
                <div class="modal-info-item">
                  <span class="modal-info-label">Últ. Troca Correia</span>
                  <span class="modal-info-value">
                    {{ formatarData(veiculoDetalhes.dataUltimaCorreiaDentada) }}
                  </span>
                </div>
                <div class="modal-info-item">
                  <span class="modal-info-label">Últ. Corretiva</span>
                  <span class="modal-info-value">
                    {{ formatarData(veiculoDetalhes.dataUltimaCorretiva) }}
                    <span style="color: var(--text-muted); font-size: 0.85em; margin-left: 0.3rem;">({{ formatarKm(veiculoDetalhes.kmCorretiva) }})</span>
                  </span>
                </div>
                <div class="modal-info-item">
                  <span class="modal-info-label">Próx. Troca Óleo</span>
                  <span class="modal-info-value proxima-manutencao" :class="'status-' + statusProximaManutencao(veiculoDetalhes.dataProximaTrocaOleo)">
                    <span v-if="statusProximaManutencao(veiculoDetalhes.dataProximaTrocaOleo) === 'vencida'" class="status-dot dot-vencida"></span>
                    <span v-else-if="statusProximaManutencao(veiculoDetalhes.dataProximaTrocaOleo) === 'alerta'" class="status-dot dot-alerta"></span>
                    <span v-else-if="statusProximaManutencao(veiculoDetalhes.dataProximaTrocaOleo) === 'ok'" class="status-dot dot-ok"></span>
                    {{ formatarData(veiculoDetalhes.dataProximaTrocaOleo) }} <span style="color: var(--text-muted); font-size: 0.85em; margin-left: 0.3rem;">({{ formatarKm(veiculoDetalhes.kmTrocaOleo) }})</span>
                  </span>
                </div>
                <div class="modal-info-item">
                  <span class="modal-info-label">Próx. Troca Correia</span>
                  <span class="modal-info-value proxima-manutencao" :class="'status-' + statusProximaManutencao(veiculoDetalhes.dataProximaCorreiaDentada)">
                    <span v-if="statusProximaManutencao(veiculoDetalhes.dataProximaCorreiaDentada) === 'vencida'" class="status-dot dot-vencida"></span>
                    <span v-else-if="statusProximaManutencao(veiculoDetalhes.dataProximaCorreiaDentada) === 'alerta'" class="status-dot dot-alerta"></span>
                    <span v-else-if="statusProximaManutencao(veiculoDetalhes.dataProximaCorreiaDentada) === 'ok'" class="status-dot dot-ok"></span>
                    {{ formatarData(veiculoDetalhes.dataProximaCorreiaDentada) }} <span style="color: var(--text-muted); font-size: 0.85em; margin-left: 0.3rem;">({{ formatarKm(veiculoDetalhes.kmTrocaCorreiaDentada) }})</span>
                  </span>
                </div>
              </div>
            </div>

            <div class="modal-section" style="margin-top: 1.5rem;">
              <h4 class="modal-section-title">🔧 Histórico de Manutenções</h4>
              <div v-if="manutencoesDoVeiculoSelecionado.length > 0" class="manutencoes-historico-lista">
                <div v-for="m in manutencoesDoVeiculoSelecionado" :key="m.id" class="historico-item">
                  <div class="historico-item-header">
                    <span class="badge" :class="m.tipoManutencao === 'CORRETIVA' ? 'badge-danger' : 'badge-purple'" style="font-size: 0.7rem;">
                      {{ formatarTipoManutencao(m.tipoManutencao) }}
                    </span>
                    <span class="status-badge" :class="m.status === 'EM_ABERTO' ? 'status-open' : 'status-closed'" style="font-size: 0.7rem;">
                      {{ m.status === 'EM_ABERTO' ? 'Em Aberto' : 'Finalizada' }}
                    </span>
                  </div>
                  <div class="historico-item-body">
                    <span class="historico-info">📅 Agendado: <strong>{{ formatarData(m.dataAgendamento) }}</strong></span>
                    <span v-if="m.dataRealizacao" class="historico-info">✅ Realizado: <strong>{{ formatarData(m.dataRealizacao) }}</strong></span>
                    <span class="historico-info">🛣️ KM: <strong>{{ m.kilometragem }} km</strong></span>
                  </div>
                </div>
              </div>
              <p v-else class="text-muted" style="font-size: 0.85rem; margin-top: 0.5rem;">
                Nenhuma manutenção registrada para este veículo.
              </p>
            </div>
          </div>
          <div class="custom-modal-footer no-print">
            <button class="btn btn-secondary" @click="imprimirDetalhes">
              🖨️ Imprimir
            </button>
            <button class="btn btn-primary" @click="fecharDetalhes">Fechar</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* ── Estilos do Modal de Detalhes ── */
.modal-section-title {
  margin: 0 0 1rem 0;
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--text-primary);
  border-bottom: 1px solid var(--color-border);
  padding-bottom: 0.5rem;
}

.modal-grid-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.25rem;
}

.modal-info-item {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.modal-info-label {
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  color: var(--text-muted);
}

.modal-info-value {
  font-size: 0.95rem;
  color: var(--text-primary);
  font-weight: 500;
}

.clickable {
  cursor: pointer;
}

/* ── Histórico de Manutenções ── */
.manutencoes-historico-lista {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.historico-item {
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.historico-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.historico-item-body {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.historico-info strong {
  color: var(--text-primary);
}

/* ── Layout ── */
.page-header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1.25rem;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

/* ── Toggle Visualização ── */
.view-toggle {
  display: flex;
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 3px;
  gap: 2px;
}

.view-toggle-btn {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.35rem 0.75rem;
  border-radius: calc(var(--radius-md) - 3px);
  border: none;
  background: transparent;
  color: var(--text-muted);
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.view-toggle-btn:hover {
  color: var(--text-primary);
  background: var(--color-surface-3);
}

.view-toggle-btn.active {
  background: var(--gradient);
  color: #fff;
  box-shadow: 0 2px 8px rgba(238, 130, 39, 0.35);
}

/* ── Barra de filtros ── */
.filtros-bar {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  align-items: flex-end;
  margin-bottom: 1.5rem;
}

.busca-wrap {
  position: relative;
  flex: 1;
  min-width: 220px;
  display: flex;
  align-items: center;
}

.busca-icon {
  position: absolute;
  left: 0.75rem;
  font-size: 0.9rem;
  pointer-events: none;
  opacity: 0.6;
}

.busca-wrap .form-control {
  padding-left: 2.25rem;
  padding-right: 2.25rem;
}

.busca-clear {
  position: absolute;
  right: 0.65rem;
  background: transparent;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  font-size: 0.85rem;
  padding: 0.2rem;
}
.busca-clear:hover {
  color: var(--text-primary);
}

.ordenacao-wrap {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  min-width: 200px;
}

.ordenacao-label {
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  color: var(--text-muted);
}

/* ── Grupos por Base ── */
.grupos-wrap {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.grupo-base-header {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin-bottom: 1rem;
  padding-bottom: 0.6rem;
  border-bottom: 1px solid var(--color-border);
}

.grupo-base-icon {
  font-size: 1.15rem;
}

.grupo-base-nome {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--text-primary);
  flex: 1;
}

.grupo-base-count {
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--text-muted);
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  padding: 0.2rem 0.6rem;
  border-radius: 99px;
}

/* ── Grid de Cards ── */
.veiculos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.25rem;
}

.veiculo-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.veiculo-card:hover {
  transform: translateY(-3px);
  border-color: rgba(238, 130, 39, 0.4);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.35), 0 0 0 1px rgba(238, 130, 39, 0.1);
}

.veiculo-card-stripe {
  height: 4px;
  background: var(--gradient);
  flex-shrink: 0;
}

.veiculo-card-top {
  padding: 1.1rem 1.25rem 0.85rem;
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
}

.veiculo-card-icon {
  font-size: 1.75rem;
  line-height: 1;
}

.veiculo-card-top-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.veiculo-card-nome {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.3;
}

.placa-badge {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--accent-1);
  background: rgba(238, 130, 39, 0.12);
  border: 1px solid rgba(238, 130, 39, 0.22);
  padding: 0.1rem 0.5rem;
  border-radius: 99px;
}

.veiculo-card-divider {
  height: 1px;
  background: var(--color-border);
  margin: 0 1.25rem;
}

.veiculo-card-info {
  padding: 0.9rem 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
  flex: 1;
}

.info-row {
  display: flex;
  align-items: flex-start;
  gap: 0.6rem;
}

.info-icon {
  font-size: 1rem;
  flex-shrink: 0;
  margin-top: 0.05rem;
  width: 1.4rem;
  text-align: center;
}

.info-text {
  display: flex;
  flex-direction: column;
  gap: 0.05rem;
  min-width: 0;
}

.info-label {
  font-size: 0.7rem;
  font-weight: 600;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  color: var(--text-muted);
  line-height: 1.2;
}

.info-value {
  font-size: 0.875rem;
  color: var(--text-primary);
  word-break: break-word;
  line-height: 1.4;
}

.base-nome {
  font-weight: 600;
  color: var(--accent-3, #f6a55c);
}

.veiculo-card-footer {
  padding: 0.85rem 1.25rem;
  border-top: 1px solid var(--color-border);
  display: flex;
  gap: 0.6rem;
  background: var(--color-surface-2);
}

.btn-card-action {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.35rem;
  padding: 0.5rem 0.75rem;
  border-radius: var(--radius-sm);
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s ease;
  text-decoration: none;
}

.btn-card-manutencao {
  background: rgba(245, 158, 11, 0.12);
  color: #fbbf24;
  border-color: rgba(245, 158, 11, 0.25);
}

.btn-card-manutencao:hover {
  background: rgba(245, 158, 11, 0.22);
  border-color: rgba(245, 158, 11, 0.5);
}

.btn-card-delete {
  background: rgba(239, 68, 68, 0.1);
  color: #f87171;
  border-color: rgba(239, 68, 68, 0.2);
}

.btn-card-delete:hover:not(:disabled) {
  background: rgba(239, 68, 68, 0.2);
  border-color: rgba(239, 68, 68, 0.45);
}

.btn-card-delete:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ── Status Próxima Manutenção ── */
.proxima-manutencao {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  font-weight: 600;
}

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  animation: pulse-dot 2s infinite;
}

.dot-vencida {
  background: #ef4444;
  box-shadow: 0 0 0 0 rgba(239,68,68,0.5);
}

.dot-alerta {
  background: #f59e0b;
  box-shadow: 0 0 0 0 rgba(245,158,11,0.5);
}

.dot-ok {
  background: #22c55e;
  box-shadow: 0 0 0 0 rgba(34,197,94,0.5);
  animation: none;
}

@keyframes pulse-dot {
  0%   { transform: scale(1); opacity: 1; }
  50%  { transform: scale(1.4); opacity: 0.7; }
  100% { transform: scale(1); opacity: 1; }
}

.status-vencida .info-value,
.status-vencida {
  color: #f87171 !important;
}

.status-alerta .info-value,
.status-alerta {
  color: #fbbf24 !important;
}

.status-ok .info-value,
.status-ok {
  color: #4ade80 !important;
}

/* lista: span de próxima manutenção */
.list-proxima {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  font-size: inherit;
}

@media (max-width: 576px) {
  .filtros-bar {
    flex-direction: column;
    align-items: stretch;
  }
  .ordenacao-wrap {
    min-width: 0;
  }
}

/* ── Botões novos nos cards ── */
.btn-card-km {
  background: rgba(56, 189, 248, 0.1);
  color: #38bdf8;
  border-color: rgba(56, 189, 248, 0.25);
}
.btn-card-km:hover {
  background: rgba(56, 189, 248, 0.2);
  border-color: rgba(56, 189, 248, 0.5);
}

.btn-card-base {
  background: rgba(139, 92, 246, 0.1);
  color: #a78bfa;
  border-color: rgba(139, 92, 246, 0.25);
}
.btn-card-base:hover {
  background: rgba(139, 92, 246, 0.2);
  border-color: rgba(139, 92, 246, 0.5);
}

/* ── Botões novos na lista ── */
.btn-km {
  background: rgba(56, 189, 248, 0.12);
  color: #38bdf8;
  border: 1px solid rgba(56, 189, 248, 0.3);
}
.btn-km:hover {
  background: rgba(56, 189, 248, 0.22);
}

.btn-base {
  background: rgba(139, 92, 246, 0.12);
  color: #a78bfa;
  border: 1px solid rgba(139, 92, 246, 0.3);
}
.btn-base:hover {
  background: rgba(139, 92, 246, 0.22);
}

/* ── Modais customizados ── */
.custom-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.65);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 1rem;
}

.custom-modal {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  width: 100%;
  max-width: 560px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.5);
}

.custom-modal--sm {
  max-width: 420px;
}

.custom-modal-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-surface-2);
  flex-shrink: 0;
}

.custom-modal-icon {
  font-size: 1.5rem;
  line-height: 1;
}

.custom-modal-title {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-primary);
  flex: 1;
}

.custom-modal-close {
  background: transparent;
  border: none;
  color: var(--text-muted);
  font-size: 1.1rem;
  cursor: pointer;
  padding: 0.25rem 0.5rem;
  border-radius: var(--radius-sm);
  transition: all 0.15s ease;
}
.custom-modal-close:hover {
  background: var(--color-surface-3);
  color: var(--text-primary);
}

.custom-modal-body {
  padding: 1.5rem;
  overflow-y: auto;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.custom-modal-subtitle {
  margin: 0;
  font-size: 0.88rem;
  color: var(--text-muted);
  line-height: 1.5;
}

.custom-modal-footer {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  padding: 1rem 1.5rem;
  border-top: 1px solid var(--color-border);
  background: var(--color-surface-2);
  flex-shrink: 0;
}

/* ── Dropdown de seleção de base ── */
.base-select-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.base-select-icon {
  position: absolute;
  left: 0.9rem;
  font-size: 1rem;
  pointer-events: none;
  z-index: 1;
}

.base-select {
  padding-left: 2.5rem !important;
  padding-right: 2.5rem !important;
  appearance: none;
  -webkit-appearance: none;
  cursor: pointer;
  border-color: rgba(139, 92, 246, 0.35) !important;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.base-select:focus {
  border-color: #a78bfa !important;
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.18) !important;
  outline: none;
}

.base-select-arrow {
  position: absolute;
  right: 0.9rem;
  font-size: 0.75rem;
  color: var(--text-muted);
  pointer-events: none;
}

/* ── Input de kilometragem ── */
.km-atual-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.85rem 1rem;
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

.km-atual-label {
  font-size: 0.78rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-muted);
}

.km-atual-valor {
  font-size: 1.1rem;
  font-weight: 700;
  color: #38bdf8;
}

.km-input-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.km-input {
  padding-right: 3rem !important;
}

.km-input-suffix {
  position: absolute;
  right: 0.9rem;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-muted);
  pointer-events: none;
}

.km-erro {
  margin: 0.5rem 0 0;
  font-size: 0.82rem;
  color: #f87171;
  line-height: 1.4;
}

/* ── Animação dos modais ── */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.2s ease;
}
.modal-fade-enter-active .custom-modal,
.modal-fade-leave-active .custom-modal {
  transition: transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 0.2s ease;
}
.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
.modal-fade-enter-from .custom-modal {
  transform: scale(0.92) translateY(12px);
  opacity: 0;
}

/* ── Estilos de Impressão ── */
@media print {
  body * {
    visibility: hidden;
  }
  .printable-modal, .printable-modal * {
    visibility: visible;
  }
  .printable-modal {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    max-width: 100%;
    box-shadow: none;
    border: none;
  }
  .no-print {
    display: none !important;
  }
  .print-only {
    display: block !important;
  }
  .printable-overlay {
    background: transparent;
    position: static;
  }
}
</style>