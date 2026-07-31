<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { manutencaoService } from '../../services/manutencaoService'
import type { ManutencaoResponse, Servico } from '../../types'
import { SERVICOS_LIST } from '../../types'

const manutencoes = ref<ManutencaoResponse[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

// ── Modal de Detalhes ─────────────────────────────────────────────────────────
const manutencaoSelecionada = ref<ManutencaoResponse | null>(null)

function abrirDetalhes(manutencao: ManutencaoResponse) {
  manutencaoSelecionada.value = manutencao
}

function fecharModal() {
  manutencaoSelecionada.value = null
}

// ── Função para Imprimir/Gerar PDF Apenas da Modal ───────────────────────────
function imprimirDetalhes() {
  document.body.classList.add('printing-modal-active')
  window.print()
  document.body.classList.remove('printing-modal-active')
}

// ── Modo de visualização ──────────────────────────────────────────────────────
type ViewMode = 'cards' | 'list'
const viewMode = ref<ViewMode>(
  (localStorage.getItem('manutencoes-view-mode') as ViewMode) ?? 'cards'
)
function setViewMode(mode: ViewMode) {
  viewMode.value = mode
  localStorage.setItem('manutencoes-view-mode', mode)
}

// ── Filtros ──────────────────────────────────────────────────────────────────
const filtroBusca = ref('')
const filtroDataDe = ref('')
const filtroDataAte = ref('')

// ── Filtros de Base e Status ──────────────────────────────────────────────────
const filtroBase = ref('')
const filtroStatus = ref<'TODOS' | 'EM_ABERTO' | 'FINALIZADA'>('TODOS')

const basesDisponiveis = computed(() => {
  const nomes = new Set(manutencoes.value.map((m) => m.veiculo?.base?.nome || 'Sem base definida'))
  return Array.from(nomes).sort((a, b) => a.localeCompare(b, 'pt-BR'))
})

async function carregarManutencoes() {
  try {
    loading.value = true
    error.value = null
    manutencoes.value = await manutencaoService.listarTodas()
  } catch {
    error.value = 'Erro ao carregar lista de manutenções. Verifique se o servidor está rodando.'
  } finally {
    loading.value = false
  }
}

function limparFiltros() {
  filtroBusca.value = ''
  filtroDataDe.value = ''
  filtroDataAte.value = ''
  filtroBase.value = ''
  filtroStatus.value = 'TODOS'
}

const temFiltroAtivo = computed(
  () =>
    !!filtroBusca.value ||
    !!filtroDataDe.value ||
    !!filtroDataAte.value ||
    !!filtroBase.value ||
    filtroStatus.value !== 'TODOS'
)

// ── Manutenções filtradas ─────────────────────────────────────────────────────
const manutencoesFiltradas = computed(() => {
  return manutencoes.value.filter((m) => {
    if (filtroBusca.value.trim()) {
      const q = filtroBusca.value.trim().toLowerCase()
      const placa = m.veiculo?.placaVeiculo?.toLowerCase() ?? ''
      if (!placa.includes(q)) return false
    }
    if (filtroDataDe.value) {
      if (!m.dataAgendamento || m.dataAgendamento < filtroDataDe.value) return false
    }
    if (filtroDataAte.value) {
      if (!m.dataAgendamento || m.dataAgendamento > filtroDataAte.value) return false
    }
    if (filtroBase.value) {
      const nomeBase = m.veiculo?.base?.nome || 'Sem base definida'
      if (nomeBase !== filtroBase.value) return false
    }
    if (filtroStatus.value !== 'TODOS') {
      if (m.status !== filtroStatus.value) return false
    }
    return true
  })
})

// ── Agrupamento por Base ──────────────────────────────────────────────────────
const gruposPorBase = computed(() => {
  const mapa = new Map<string, ManutencaoResponse[]>()
  for (const m of manutencoesFiltradas.value) {
    const nomeBase = m.veiculo?.base?.nome || 'Sem base definida'
    if (!mapa.has(nomeBase)) mapa.set(nomeBase, [])
    mapa.get(nomeBase)!.push(m)
  }
  for (const lista of mapa.values()) {
    lista.sort((a, b) => (b.dataAgendamento ?? '').localeCompare(a.dataAgendamento ?? ''))
  }
  const grupos = Array.from(mapa.entries()).map(([base, itens]) => ({ base, itens }))
  grupos.sort((a, b) => a.base.localeCompare(b.base, 'pt-BR'))
  return grupos
})

const tipoIcon: Record<string, string> = {
  CARRO: '🚗',
  CAMINHAO: '🚛',
  VAN: '🚐',
}

const formatarData = (dataStr?: string) => {
  if (!dataStr) return ''
  const [ano, mes, dia] = dataStr.split('-')
  return `${dia}/${mes}/${ano}`
}

const formatarTipo = (tipo: string) => {
  const map: Record<string, string> = {
    PREVENTIVA_TROCA_DE_OLEO: 'Prev. - Troca de Óleo',
    PREVENTIVA_KIT_CORREIA_DENTADA: 'Prev. - Kit Correia',
    CORRETIVA: 'Corretiva',
  }
  return map[tipo] || tipo
}

const getServicoInfo = (servico: Servico) => SERVICOS_LIST.find((s) => s.value === servico)

onMounted(carregarManutencoes)
</script>

<template>
  <div class="page-content">
    <!-- Cabeçalho -->
    <div class="page-header-row">
      <div>
        <h1 class="page-title">🔧 Manutenções</h1>
        <p class="page-subtitle">
          {{ manutencoesFiltradas.length }} de {{ manutencoes.length }} manutenção(ões)
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

        <RouterLink to="/cadastros/cadastrar-manutencao" class="btn btn-primary">
          + Registrar Manutenção
        </RouterLink>
      </div>
    </div>

    <!-- Barra de Filtros -->
    <div v-if="!loading && !error && manutencoes.length > 0" class="filtros-bar">
      <div class="busca-wrap">
        <span class="busca-icon">🔍</span>
        <input
          v-model="filtroBusca"
          type="text"
          class="form-control"
          placeholder="Buscar por placa..."
        />
        <button v-if="filtroBusca" class="busca-clear" @click="filtroBusca = ''" title="Limpar">✖</button>
      </div>

      <div class="data-wrap">
        <label class="filtro-label" for="filtroDataDe">Agendado de</label>
        <input id="filtroDataDe" v-model="filtroDataDe" type="date" class="form-control" />
      </div>

      <div class="data-wrap">
        <label class="filtro-label" for="filtroDataAte">até</label>
        <input id="filtroDataAte" v-model="filtroDataAte" type="date" class="form-control" />
      </div>

      <div class="ordenacao-wrap">
        <label class="filtro-label" for="filtroBase">Base</label>
        <select id="filtroBase" v-model="filtroBase" class="form-control">
          <option value="">Todas as bases</option>
          <option v-for="base in basesDisponiveis" :key="base" :value="base">
            {{ base }}
          </option>
        </select>
      </div>

      <div class="ordenacao-wrap">
        <label class="filtro-label" for="filtroStatus">Status</label>
        <select id="filtroStatus" v-model="filtroStatus" class="form-control">
          <option value="TODOS">Todos</option>
          <option value="EM_ABERTO">Em Aberto</option>
          <option value="FINALIZADA">Finalizada</option>
        </select>
      </div>

      <button v-if="temFiltroAtivo" class="btn btn-secondary btn-sm limpar-btn" @click="limparFiltros">
        ✖ Limpar filtros
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>Carregando manutenções...</p>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="alert alert-danger">⚠️ {{ error }}</div>

    <!-- Empty: sem manutenções -->
    <div v-else-if="manutencoes.length === 0" class="empty-state">
      <span class="empty-state-icon">🔧</span>
      <p>Nenhuma manutenção registrada ainda.</p>
      <RouterLink to="/cadastros/cadastrar-manutencao" class="btn btn-primary">
        Registrar primeira manutenção
      </RouterLink>
    </div>

    <!-- Empty: filtro sem resultado -->
    <div v-else-if="manutencoesFiltradas.length === 0" class="empty-state">
      <span class="empty-state-icon">🔍</span>
      <p>Nenhuma manutenção encontrada com os filtros aplicados.</p>
      <button class="btn btn-secondary" @click="limparFiltros">Limpar filtros</button>
    </div>

    <!-- ── GRUPOS POR BASE ── -->
    <div v-else class="grupos-wrap">
      <section v-for="grupo in gruposPorBase" :key="grupo.base" class="grupo-base">

        <!-- Cabeçalho do grupo -->
        <div class="grupo-base-header">
          <span class="grupo-base-icon">🏢</span>
          <h2 class="grupo-base-nome">{{ grupo.base }}</h2>
          <span class="grupo-base-count">{{ grupo.itens.length }} manutenção(ões)</span>
        </div>

        <!-- ── CARDS ── -->
        <div v-if="viewMode === 'cards'" class="manutencoes-grid">
          <div
            v-for="m in grupo.itens"
            :key="m.id"
            class="manutencao-card clickable"
            @click="abrirDetalhes(m)"
          >
            <!-- Stripe superior colorida por status -->
            <div class="card-stripe" :class="m.status === 'EM_ABERTO' ? 'stripe-open' : 'stripe-closed'"></div>

            <!-- Topo: veículo + status -->
            <div class="card-top">
              <span class="card-veiculo-icon">{{ tipoIcon[m.veiculo.tipoVeiculo] ?? '🚙' }}</span>
              <div class="card-top-text">
                <h3 class="card-nome">{{ m.veiculo.nome }}</h3>
                <span class="numero-manutencao">Nº {{ m.numeroManutencao }}</span>
                <span class="placa-badge">{{ m.veiculo.placaVeiculo }}</span>
              </div>
              <span
                class="status-badge"
                :class="m.status === 'EM_ABERTO' ? 'status-open' : 'status-closed'"
              >
                {{ m.status === 'EM_ABERTO' ? 'Em Aberto' : 'Finalizada' }}
              </span>
            </div>

            <div class="card-divider"></div>

            <!-- Infos -->
            <div class="card-info">
              <div class="info-row">
                <span class="info-icon">🔧</span>
                <div class="info-text">
                  <span class="info-label">Tipo</span>
                  <span
                    class="badge"
                    :class="m.tipoManutencao === 'CORRETIVA' ? 'badge-danger' : 'badge-purple'"
                    style="font-size: 0.7rem; width: fit-content;"
                  >
                    {{ formatarTipo(m.tipoManutencao) }}
                  </span>
                </div>
              </div>
              <div class="info-row">
                <span class="info-icon">📅</span>
                <div class="info-text">
                  <span class="info-label">Agendado</span>
                  <span class="info-value">{{ formatarData(m.dataAgendamento) }}</span>
                </div>
              </div>
              <div class="info-row" v-if="m.dataRealizacao">
                <span class="info-icon">✅</span>
                <div class="info-text">
                  <span class="info-label">Realizado</span>
                  <span class="info-value">{{ formatarData(m.dataRealizacao) }}</span>
                </div>
              </div>
              <div class="info-row" v-if="m.dataProximaManutencao">
                <span class="info-icon">📅</span>
                <div class="info-text">
                  <span class="info-label">Próxima</span>
                  <span class="info-value" style="color: var(--color-success);">{{ formatarData(m.dataProximaManutencao) }}</span>
                </div>
              </div>
              <div class="info-row">
                <span class="info-icon">📍</span>
                <div class="info-text">
                  <span class="info-label">Kilometragem</span>
                  <span class="info-value">{{ m.kilometragem }} km</span>
                </div>
              </div>
            </div>

            <!-- Checklist (apenas finalizadas) -->
            <div v-if="m.status === 'FINALIZADA' && m.servicos && m.servicos.length > 0" class="card-checklist">
              <span class="checklist-summary-title">Serviços:</span>
              <div class="checklist-pills">
                <span v-for="s in m.servicos" :key="s" class="checklist-pill">
                  {{ getServicoInfo(s)?.icon }} {{ getServicoInfo(s)?.label || s }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- ── LISTA ── -->
        <ul v-else class="manutencoes-lista">
          <li
            v-for="m in grupo.itens"
            :key="m.id"
            class="list-item manutencao-item clickable"
            @click="abrirDetalhes(m)"
          >
            <div class="manutencao-top">
              <div class="info-primary">
                <span
                  class="status-badge"
                  :class="m.status === 'EM_ABERTO' ? 'status-open' : 'status-closed'"
                >
                  {{ m.status === 'EM_ABERTO' ? 'Em Aberto' : 'Finalizada' }}
                </span>
                <span class="veiculo-icon-small">{{ tipoIcon[m.veiculo.tipoVeiculo] ?? '🚙' }}</span>
                <div>
                  <h5 class="manutencao-veiculo-nome">
                    {{ m.veiculo.nome }}
                    <span class="numero-manutencao">Nº {{ m.numeroManutencao }}</span>
                    <span class="placa-badge">{{ m.veiculo.placaVeiculo }}</span>
                  </h5>
                </div>
              </div>

              <div class="manutencao-badges">
                <span class="badge" :class="m.tipoManutencao === 'CORRETIVA' ? 'badge-danger' : 'badge-purple'">
                  {{ formatarTipo(m.tipoManutencao) }}
                </span>
                <span class="info-meta">📍 {{ m.kilometragem }} km</span>
                <span class="info-meta">📅 Agendada: {{ formatarData(m.dataAgendamento) }}</span>
                <span v-if="m.dataRealizacao" class="info-meta">
                  ✅ Realizada: {{ formatarData(m.dataRealizacao) }}
                </span>
                <span v-if="m.dataProximaManutencao" class="info-meta info-meta-success">
                  📅 Próxima: {{ formatarData(m.dataProximaManutencao) }}
                </span>
              </div>
            </div>

            <div v-if="m.status === 'FINALIZADA'" class="checklist-summary">
              <span class="checklist-summary-title">Serviços executados:</span>
              <div class="checklist-pills">
                <span v-for="s in m.servicos" :key="s" class="checklist-pill">
                  {{ getServicoInfo(s)?.icon }} {{ getServicoInfo(s)?.label || s }}
                </span>
                <span v-if="!m.servicos || m.servicos.length === 0" class="checklist-pill-none">
                  Nenhum item marcado
                </span>
              </div>
            </div>
          </li>
        </ul>

      </section>
    </div>

    <!-- ── MODAL DE DETALHES ── -->
    <Teleport to="body">
      <div v-if="manutencaoSelecionada" class="modal-backdrop" @click.self="fecharModal">
        <div class="modal-content printable-modal">
          <div class="modal-header">
            <div class="modal-header-info">
              <span class="modal-icon">{{ tipoIcon[manutencaoSelecionada.veiculo.tipoVeiculo] ?? '🚙' }}</span>
              <div>
                <h3 class="modal-title">Detalhes da Manutenção</h3>
                <span class="numero-manutencao">Nº {{ manutencaoSelecionada.numeroManutencao }}</span>
              </div>
            </div>
            <button class="modal-close-btn no-print" @click="fecharModal" title="Fechar">✖</button>
          </div>

          <div class="modal-body">
            <!-- Bloco do Veículo -->
            <div class="modal-section">
              <h4 class="modal-section-title">🚙 Informações do Veículo</h4>
              <div class="modal-grid">
                <div>
                  <span class="modal-label">Veículo</span>
                  <span class="modal-value">{{ manutencaoSelecionada.veiculo.nome }}</span>
                </div>
                <div>
                  <span class="modal-label">Placa</span>
                  <span class="modal-value"><span class="placa-badge">{{ manutencaoSelecionada.veiculo.placaVeiculo }}</span></span>
                </div>
                <div>
                  <span class="modal-label">Base</span>
                  <span class="modal-value">{{ manutencaoSelecionada.veiculo.base?.nome || 'Sem base definida' }}</span>
                </div>
                <div>
                  <span class="modal-label">Tipo de Veículo</span>
                  <span class="modal-value">{{ manutencaoSelecionada.veiculo.tipoVeiculo }}</span>
                </div>
              </div>
            </div>

            <!-- Bloco da Manutenção -->
            <div class="modal-section">
              <h4 class="modal-section-title">🔧 Dados da Manutenção</h4>
              <div class="modal-grid">
                <div>
                  <span class="modal-label">Status</span>
                  <span
                    class="status-badge"
                    :class="manutencaoSelecionada.status === 'EM_ABERTO' ? 'status-open' : 'status-closed'"
                  >
                    {{ manutencaoSelecionada.status === 'EM_ABERTO' ? 'Em Aberto' : 'Finalizada' }}
                  </span>
                </div>
                <div>
                  <span class="modal-label">Tipo de Manutenção</span>
                  <span class="badge" :class="manutencaoSelecionada.tipoManutencao === 'CORRETIVA' ? 'badge-danger' : 'badge-purple'">
                    {{ formatarTipo(manutencaoSelecionada.tipoManutencao) }}
                  </span>
                </div>
                <div>
                  <span class="modal-label">Quilometragem</span>
                  <span class="modal-value">{{ manutencaoSelecionada.kilometragem }} km</span>
                </div>
                <div>
                  <span class="modal-label">Agendado Para</span>
                  <span class="modal-value">{{ formatarData(manutencaoSelecionada.dataAgendamento) }}</span>
                </div>
                <div>
                  <span class="modal-label">Data de Realização</span>
                  <span class="modal-value">{{ formatarData(manutencaoSelecionada.dataRealizacao) || 'Pendente' }}</span>
                </div>
                <div>
                  <span class="modal-label">Próxima Manutenção</span>
                  <span class="modal-value" style="color: var(--color-success);">
                    {{ formatarData(manutencaoSelecionada.dataProximaManutencao) || 'Não agendada' }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Serviços Executados -->
            <div class="modal-section">
              <h4 class="modal-section-title">📋 Serviços Executados</h4>
              <div v-if="manutencaoSelecionada.servicos && manutencaoSelecionada.servicos.length > 0" class="checklist-pills">
                <span v-for="s in manutencaoSelecionada.servicos" :key="s" class="checklist-pill">
                  {{ getServicoInfo(s)?.icon }} {{ getServicoInfo(s)?.label || s }}
                </span>
              </div>
              <p v-else class="checklist-pill-none">Nenhum serviço registrado/executado.</p>
            </div>
          </div>

          <div class="modal-footer no-print">
            <button class="btn btn-secondary" @click="imprimirDetalhes">
              🖨️ Imprimir / Salvar PDF
            </button>
            <button class="btn btn-primary" @click="fecharModal">Fechar</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
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

/* ── Toggle visualização ── */
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

/* ── Interatividade nos itens ── */
.clickable {
  cursor: pointer;
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
  min-width: 200px;
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
.busca-clear:hover { color: var(--text-primary); }

.data-wrap {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  min-width: 155px;
}

.ordenacao-wrap {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  min-width: 175px;
}

.filtro-label {
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  color: var(--text-muted);
}

.limpar-btn {
  align-self: flex-end;
  white-space: nowrap;
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

.grupo-base-icon { font-size: 1.15rem; }

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

/* ── CARDS ── */
.manutencoes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.25rem;
}

.manutencao-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.manutencao-card:hover {
  transform: translateY(-3px);
  border-color: rgba(238, 130, 39, 0.4);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.35), 0 0 0 1px rgba(238, 130, 39, 0.1);
}

.card-stripe {
  height: 4px;
  flex-shrink: 0;
}

.stripe-open  { background: linear-gradient(90deg, #f59e0b, #fbbf24); }
.stripe-closed { background: linear-gradient(90deg, #10b981, #34d399); }

.card-top {
  padding: 1.1rem 1.25rem 0.85rem;
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
}

.card-veiculo-icon {
  font-size: 1.75rem;
  line-height: 1;
  flex-shrink: 0;
}

.card-top-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.card-nome {
  margin: 0;
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.3;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.numero-manutencao {
  font-size: 0.72rem;
  font-weight: 600;
  color: var(--accent-1);
  letter-spacing: 0.02em;
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

.card-divider {
  height: 1px;
  background: var(--color-border);
  margin: 0 1.25rem;
}

.card-info {
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
  line-height: 1.4;
}

.card-checklist {
  border-top: 1px solid var(--color-border);
  padding: 0.65rem 1.25rem;
  background: var(--color-surface-2);
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

/* ── Status badge ── */
.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.3rem 0.65rem;
  border-radius: var(--radius-sm);
  font-size: 0.72rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  white-space: nowrap;
  flex-shrink: 0;
}

.status-open {
  background: rgba(245, 158, 11, 0.15);
  color: var(--color-warning);
  border: 1px solid rgba(245, 158, 11, 0.3);
}

.status-closed {
  background: rgba(16, 185, 129, 0.15);
  color: var(--color-success);
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.badge-danger {
  background: rgba(239, 68, 68, 0.15);
  color: var(--color-danger);
}

/* ── Checklist ── */
.checklist-summary {
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  padding: 0.5rem 0.75rem;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.checklist-summary-title {
  font-size: 0.72rem;
  font-weight: 700;
  text-transform: uppercase;
  color: var(--text-muted);
  white-space: nowrap;
}

.checklist-pills {
  display: flex;
  gap: 0.4rem;
  flex-wrap: wrap;
}

.checklist-pill {
  font-size: 0.72rem;
  background: rgba(238, 130, 39, 0.1);
  color: var(--accent-1);
  border: 1px solid rgba(238, 130, 39, 0.2);
  padding: 0.15rem 0.5rem;
  border-radius: 4px;
  font-weight: 600;
}

.checklist-pill-none {
  font-size: 0.75rem;
  color: var(--text-muted);
  font-style: italic;
}

/* ── Lista ── */
.manutencoes-lista {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}

.manutencao-item {
  flex-direction: column;
  gap: 0.75rem;
  align-items: stretch;
  transition: transform 0.15s ease, border-color 0.15s ease;
}

.manutencao-item:hover {
  border-color: rgba(238, 130, 39, 0.4);
}

.manutencao-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.info-primary {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.veiculo-icon-small {
  font-size: 1.5rem;
  line-height: 1;
}

.manutencao-veiculo-nome {
  margin: 0;
  font-weight: 700;
  font-size: 0.95rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.manutencao-badges {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.info-meta {
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--text-secondary);
}

.info-meta-success { color: var(--color-success); }

/* ── Modal ── */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.65);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal-content {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.5);
  overflow: hidden;
}

.modal-header {
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--color-surface-2);
}

.modal-header-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.modal-icon {
  font-size: 1.75rem;
}

.modal-title {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-primary);
}

.modal-close-btn {
  background: transparent;
  border: none;
  color: var(--text-muted);
  font-size: 1rem;
  cursor: pointer;
  padding: 0.3rem;
  border-radius: var(--radius-sm);
  transition: color 0.2s;
}

.modal-close-btn:hover {
  color: var(--text-primary);
}

.modal-body {
  padding: 1.5rem;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.modal-section {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.modal-section-title {
  margin: 0;
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--accent-1);
  border-bottom: 1px solid var(--color-border);
  padding-bottom: 0.35rem;
}

.modal-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.modal-label {
  display: block;
  font-size: 0.72rem;
  font-weight: 700;
  text-transform: uppercase;
  color: var(--text-muted);
  margin-bottom: 0.25rem;
}

.modal-value {
  font-size: 0.9rem;
  color: var(--text-primary);
  font-weight: 500;
}

.modal-footer {
  padding: 1rem 1.5rem;
  border-top: 1px solid var(--color-border);
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  background: var(--color-surface-2);
}

/* ── ESTILOS DE IMPRESSÃO EXCLUSIVA DA MANUTENÇÃO ── */
@media print {
  /* Esconde o app e os elementos do fundo da página */
  body.printing-modal-active > *:not(.modal-backdrop) {
    display: none !important;
  }

  /* Desativa a sobreposição escura da modal */
  .modal-backdrop {
    position: static !important;
    background: transparent !important;
    padding: 0 !important;
    display: block !important;
  }

  /* Formata a caixa da modal para ajustar à página */
  .printable-modal {
    position: relative !important;
    inset: auto !important;
    width: 100% !important;
    max-width: 100% !important;
    box-shadow: none !important;
    border: 1px solid #ccc !important;
    background: #fff !important;
    color: #000 !important;
    border-radius: 8px !important;
  }

  .modal-body {
    overflow: visible !important;
  }

  /* Oculta os botões e ícones interativos na impressão */
  .no-print {
    display: none !important;
  }

  /* Ajusta cores para garantir legibilidade no papel ou arquivo PDF */
  .modal-title, 
  .modal-value, 
  .modal-section-title {
    color: #000 !important;
  }

  .placa-badge, 
  .checklist-pill, 
  .status-badge, 
  .badge {
    border: 1px solid #999 !important;
    color: #000 !important;
    background: #f5f5f5 !important;
  }
}

/* ── Responsivo ── */
@media (max-width: 768px) {
  .filtros-bar {
    flex-direction: column;
    align-items: stretch;
  }
  .data-wrap,
  .ordenacao-wrap {
    min-width: 0;
  }
  .manutencao-top {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 576px) {
  .checklist-summary {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }
}
</style>