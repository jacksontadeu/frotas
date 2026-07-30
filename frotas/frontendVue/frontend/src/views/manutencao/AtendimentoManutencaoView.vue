<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { manutencaoService } from '../../services/manutencaoService'
import { extractErrorMessage } from '../../composables/useErrorMessage'
import type { ManutencaoResponse, Servico } from '../../types'
import { SERVICOS_LIST } from '../../types'

const manutencoes = ref<ManutencaoResponse[]>([])
const loading = ref(true)
const submitting = ref<number | null>(null)
const error = ref<string | null>(null)
const success = ref<string | null>(null)

// ID da manutenção atualmente expandida
const expandedId = ref<number | null>(null)

// Modo de visualização: 'list' (lista) ou 'grid' (cards)
const viewMode = ref<'list' | 'grid'>('list')

// Lista de serviços selecionados para o atendimento
const servicosSelecionados = ref<Servico[]>([])
const formKilometragem = ref<number | null>(null)
const formDataRealizacao = ref<string>('')
// Descrição livre para manutenção corretiva
const formDescricaoServico = ref<string>('')

// Limite de caracteres da descrição corretiva
const LIMITE_DESCRICAO = 2000
const contadorDescricao = computed(() => formDescricaoServico.value.length)

// ── Filtros ───────────────────────────────────────────────────────────────────
const filtroBusca = ref('')
const filtroDataDe = ref('')
const filtroDataAte = ref('')
const filtroBase = ref('')

const basesDisponiveis = computed(() => {
  const nomes = new Set(manutencoes.value.map((m) => m.veiculo?.base?.nome || 'Sem base definida'))
  return Array.from(nomes).sort((a, b) => a.localeCompare(b, 'pt-BR'))
})

const temFiltroAtivo = computed(
  () =>
    !!filtroBusca.value ||
    !!filtroDataDe.value ||
    !!filtroDataAte.value ||
    !!filtroBase.value
)

function limparFiltros() {
  filtroBusca.value = ''
  filtroDataDe.value = ''
  filtroDataAte.value = ''
  filtroBase.value = ''
}

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
    return true
  })
})

async function carregarManutencoes() {
  try {
    loading.value = true
    error.value = null
    manutencoes.value = await manutencaoService.listarAbertas()
  } catch (err: any) {
    error.value = extractErrorMessage(err, 'Erro ao carregar manutenções em aberto. Verifique se o servidor está rodando.')
  } finally {
    loading.value = false
  }
}

function toggleExpand(manutencao: ManutencaoResponse) {
  if (expandedId.value === manutencao.id) {
    expandedId.value = null
    servicosSelecionados.value = []
    formKilometragem.value = null
    formDataRealizacao.value = ''
    formDescricaoServico.value = ''
  } else {
    expandedId.value = manutencao.id
    // Inicializa com os dados atuais da manutenção
    servicosSelecionados.value = manutencao.servicos ? [...manutencao.servicos] : []
    formKilometragem.value = manutencao.kilometragem
    formDataRealizacao.value = manutencao.dataRealizacao
    formDescricaoServico.value = manutencao.descricaoServico ?? ''
  }
  success.value = null
  error.value = null
}

function isServicoSelecionado(servico: Servico): boolean {
  return servicosSelecionados.value.includes(servico)
}

function toggleServico(servico: Servico) {
  const index = servicosSelecionados.value.indexOf(servico)
  if (index > -1) {
    servicosSelecionados.value.splice(index, 1)
  } else {
    servicosSelecionados.value.push(servico)
  }
}

async function finalizarAtendimento(id: number) {
  const manutencao = manutencoes.value.find((m) => m.id === id)
  const isCorretiva = manutencao?.tipoManutencao === 'corretiva'

  submitting.value = id
  error.value = null
  success.value = null
  try {
    await manutencaoService.atender(id, {
      servicos: isCorretiva ? [] : servicosSelecionados.value,
      descricaoServico: isCorretiva ? formDescricaoServico.value : undefined,
      kilometragem: formKilometragem.value,
      dataRealizacao: formDataRealizacao.value,
    })
    success.value = 'Manutenção concluída com sucesso!'
    expandedId.value = null
    // Aguarda um momento e recarrega a lista
    setTimeout(() => {
      carregarManutencoes()
      success.value = null
    }, 1500)
  } catch (err: any) {
    error.value = err?.response?.data?.message || 'Erro ao finalizar atendimento da manutenção.'
  } finally {
    submitting.value = null
  }
}

const tipoIcon: Record<string, string> = {
  CARRO: '🚗',
  CAMINHAO: '🚛',
  VAN: '🚐',
}

const formatarData = (dataStr: string) => {
  if (!dataStr) return ''
  const [ano, mes, dia] = dataStr.split('-')
  return `${dia}/${mes}/${ano}`
}

const formatarTipo = (tipo: string) => {
  const map: Record<string, string> = {
    preventiva: 'Preventiva',
    corretiva: 'Corretiva',
    inspecao: 'Inspeção',
  }
  return map[tipo] || tipo
}

onMounted(carregarManutencoes)
</script>

<template>
  <div class="page-content">
    <div class="page-header-row">
      <div class="page-header">
        <h1 class="page-title">🔧 Finalizar Manutenções</h1>
        <p class="page-subtitle">
          {{ manutencoesFiltradas.length }} de {{ manutencoes.length }} manutenção(ões) em aberto
        </p>
      </div>

      <!-- Alternador de Visualização (Lista x Cards) -->
      <div v-if="!loading && manutencoes.length > 0" class="view-toggle">
        <button
          type="button"
          class="toggle-btn"
          :class="{ active: viewMode === 'list' }"
          @click="viewMode = 'list'"
          title="Visualização em Lista"
        >
          ☰ Lista
        </button>
        <button
          type="button"
          class="toggle-btn"
          :class="{ active: viewMode === 'grid' }"
          @click="viewMode = 'grid'"
          title="Visualização em Cards"
        >
          🔲 Cards
        </button>
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

      <button v-if="temFiltroAtivo" class="btn btn-secondary btn-sm limpar-btn" @click="limparFiltros">
        ✖ Limpar filtros
      </button>
    </div>

    <!-- Alert Success -->
    <div v-if="success" class="alert alert-success" style="margin-bottom: 1.5rem; text-align: center;">
      🎉 {{ success }}
    </div>

    <!-- Alert Error -->
    <div v-if="error" class="alert alert-danger" style="margin-bottom: 1.5rem;">
      ⚠️ {{ error }}
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>Carregando manutenções em aberto...</p>
    </div>

    <!-- Empty State: nenhuma manutenção -->
    <div v-else-if="manutencoes.length === 0" class="empty-state glass-card">
      <span class="empty-state-icon">✅</span>
      <h3>Nenhuma manutenção em aberto</h3>
      <p>Tudo em dia! Não há atendimentos pendentes no momento.</p>
      <button class="btn btn-secondary" @click="carregarManutencoes">
        🔄 Atualizar Lista
      </button>
    </div>

    <!-- Empty State: filtro sem resultado -->
    <div v-else-if="manutencoesFiltradas.length === 0" class="empty-state glass-card">
      <span class="empty-state-icon">🔍</span>
      <p>Nenhuma manutenção encontrada com os filtros aplicados.</p>
      <button class="btn btn-secondary" @click="limparFiltros">Limpar filtros</button>
    </div>

    <!-- List / Grid of Open Maintenances -->
    <div v-else :class="['atendimento-container', viewMode === 'grid' ? 'grid-mode' : 'list-mode']">
      <div
        v-for="m in manutencoesFiltradas"
        :key="m.id"
        class="atendimento-card glass-card"
        :class="{ active: expandedId === m.id, 'is-grid': viewMode === 'grid' }"
      >
        <!-- Header: Informações básicas da manutenção -->
        <div class="atendimento-header" @click="toggleExpand(m)">
          <div class="veiculo-info">
            <span class="veiculo-icon">{{ tipoIcon[m.veiculo.tipoVeiculo] ?? '🚙' }}</span>
            <div class="veiculo-details">
              <h4>{{ m.veiculo.nome }}</h4>
              <p class="numero-manutencao">Nº {{ m.numeroManutencao }}</p>
              <p class="veiculo-meta">
                <span>Placa: <strong>{{ m.veiculo.placaVeiculo }}</strong></span>
                <span class="meta-dot">•</span>
                <span>Base: {{ m.veiculo.base?.nome }}</span>
              </p>
            </div>
          </div>

          <div class="manutencao-status-info">
            <span class="badge" :class="m.tipoManutencao === 'CORRETIVA' ? 'badge-danger' : 'badge-purple'">
              {{ formatarTipo(m.tipoManutencao) }}
            </span>
            <span class="header-km">📍 {{ m.kilometragem }} Km</span>
            <span class="header-date">📅 {{ formatarData(m.dataRealizacao) }}</span>
            <span class="chevron-icon" :class="{ rotated: expandedId === m.id }">
              ▼
            </span>
          </div>
        </div>

        <!-- Body: Checklist (Exibido apenas quando expandido) -->
        <Transition name="expand">
          <div v-if="expandedId === m.id" class="atendimento-body">
            <hr class="divider" />

            <!-- Dados Editáveis da Manutenção -->
            <div class="atendimento-dados-editaveis">
              <div class="form-group" style="margin-bottom: 0;">
                <label for="aKm" class="form-label">📍 Quilometragem Atual (Km)</label>
                <input
                  id="aKm"
                  v-model.number="formKilometragem"
                  type="number"
                  class="form-control"
                  placeholder="Ex: 125000"
                  min="0"
                />
              </div>
              <div class="form-group" style="margin-bottom: 0;">
                <label for="aData" class="form-label">📅 Data de Realização</label>
                <input
                  id="aData"
                  v-model="formDataRealizacao"
                  type="date"
                  class="form-control"
                />
              </div>
            </div>

            <!-- Corretiva: campo de descrição livre -->
            <template v-if="m.tipoManutencao === 'CORRETIVA'">
              <h5 class="checklist-title">🔧 Descrição do Serviço Realizado</h5>
              <p class="checklist-subtitle">Descreva detalhadamente o serviço corretivo executado no veículo:</p>
              <div class="descricao-corretiva-wrap">
                <textarea
                  v-model="formDescricaoServico"
                  class="form-control descricao-corretiva"
                  placeholder="Ex: Substituição do alternador, troca da correia dentada, ajuste do sistema de freios..."
                  rows="5"
                  :maxlength="LIMITE_DESCRICAO"
                ></textarea>
                <div class="descricao-footer">
                  <span class="descricao-hint">💡 Seja específico sobre as peças trocadas e procedimentos realizados.</span>
                  <span
                    class="descricao-contador"
                    :class="{ 'contador-limite': contadorDescricao >= LIMITE_DESCRICAO }"
                  >
                    {{ contadorDescricao }} / {{ LIMITE_DESCRICAO }}
                  </span>
                </div>
              </div>
            </template>

            <!-- Preventiva / Inspeção: checklist de serviços -->
            <template v-else>
              <h5 class="checklist-title">📋 Checklist de Serviços Realizados</h5>
              <p class="checklist-subtitle">Selecione as manutenções efetuadas no veículo:</p>
              <div class="checklist-grid">
                <label
                  v-for="s in SERVICOS_LIST"
                  :key="s.value"
                  class="checklist-item-tile"
                  :class="{ checked: isServicoSelecionado(s.value) }"
                >
                  <input
                    type="checkbox"
                    :checked="isServicoSelecionado(s.value)"
                    @change="toggleServico(s.value)"
                  />
                  <div class="tile-content">
                    <span class="tile-icon">{{ s.icon }}</span>
                    <div class="tile-text">
                      <span class="tile-label">{{ s.label }}</span>
                      <span class="tile-desc">{{ s.description }}</span>
                    </div>
                  </div>
                </label>
              </div>
            </template>

            <!-- Submit Actions -->
            <div class="atendimento-actions">
              <button
                type="button"
                class="btn btn-secondary"
                :disabled="submitting !== null"
                @click="toggleExpand(m)"
              >
                Cancelar
              </button>
              <button
                type="button"
                class="btn btn-primary"
                :disabled="submitting !== null"
                @click="finalizarAtendimento(m.id)"
              >
                <span v-if="submitting === m.id">⏳ Finalizando...</span>
                <span v-else>✅ Concluir Manutenção</span>
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ── Filtros ── */
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

/* ── Header row ── */
.page-header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.view-toggle {
  display: inline-flex;
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 0.25rem;
  gap: 0.25rem;
}

.toggle-btn {
  background: transparent;
  border: none;
  color: var(--text-secondary);
  padding: 0.45rem 0.9rem;
  font-size: 0.85rem;
  font-weight: 600;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
}

.toggle-btn:hover:not(.active) {
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.05);
}

.toggle-btn.active {
  background: var(--accent-1);
  color: #ffffff;
  box-shadow: 0 2px 10px rgba(238, 130, 39, 0.35);
}

/* Containers */
.atendimento-container.list-mode {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.atendimento-container.grid-mode {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(330px, 1fr));
  gap: 1.25rem;
  align-items: start;
}

.atendimento-card {
  padding: 1.25rem 1.75rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.atendimento-card:hover {
  transform: translateY(-2px);
}

.atendimento-card.active {
  border-color: rgba(238, 130, 39, 0.4);
  box-shadow: var(--shadow-lg), 0 0 25px rgba(238, 130, 39, 0.1);
  transform: none;
}

/* Grid-specific card adjustments */
.atendimento-card.is-grid.active {
  grid-column: 1 / -1;
}

.atendimento-card.is-grid .atendimento-header {
  flex-direction: column;
  align-items: flex-start;
  gap: 0.85rem;
}

.atendimento-card.is-grid .manutencao-status-info {
  width: 100%;
  justify-content: space-between;
  padding-top: 0.75rem;
  border-top: 1px dashed rgba(255, 255, 255, 0.08);
}

/* Header layout */
.atendimento-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  user-select: none;
}

.veiculo-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.veiculo-icon {
  font-size: 2.25rem;
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  padding: 0.5rem;
  border-radius: var(--radius-md);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: background 0.3s;
}

.atendimento-card:hover .veiculo-icon {
  background: var(--color-surface-3);
}

.veiculo-details h4 {
  font-size: 1.15rem;
  font-weight: 700;
  margin-bottom: 0.2rem;
}

.numero-manutencao {
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--accent-1);
  margin-bottom: 0.25rem;
  letter-spacing: 0.02em;
}

.veiculo-meta {
  font-size: 0.85rem;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.meta-dot {
  color: var(--text-muted);
}

.manutencao-status-info {
  display: flex;
  align-items: center;
  gap: 1.25rem;
}

.header-km, .header-date {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--text-secondary);
  white-space: nowrap;
}

.badge-danger {
  background: rgba(239, 68, 68, 0.15);
  color: var(--color-danger);
}

.chevron-icon {
  font-size: 0.8rem;
  color: var(--text-muted);
  transition: transform 0.3s ease;
  display: inline-block;
  margin-left: 0.5rem;
}

.chevron-icon.rotated {
  transform: rotate(180deg);
  color: var(--accent-1);
}

/* Body and checklist styling */
.atendimento-body {
  margin-top: 1rem;
}

.atendimento-dados-editaveis {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
  margin-bottom: 1.25rem;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--color-border);
  padding: 1rem;
  border-radius: var(--radius-md);
}

.checklist-title {
  font-size: 1rem;
  font-weight: 700;
  margin-bottom: 0.25rem;
  color: var(--text-primary);
}

.checklist-subtitle {
  font-size: 0.85rem;
  color: var(--text-secondary);
  margin-bottom: 1.25rem;
}

/* Grid of checklist tiles */
.checklist-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.checklist-item-tile {
  display: block;
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 1rem;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  user-select: none;
}

.checklist-item-tile input[type="checkbox"] {
  position: absolute;
  opacity: 0;
  cursor: pointer;
  height: 0;
  width: 0;
}

.tile-content {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
}

.tile-icon {
  font-size: 1.75rem;
  line-height: 1;
}

.tile-text {
  display: flex;
  flex-direction: column;
}

.tile-label {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.15rem;
  transition: color 0.2s;
}

.tile-desc {
  font-size: 0.75rem;
  color: var(--text-secondary);
  line-height: 1.3;
}

/* Checked Tile Effect */
.checklist-item-tile:hover {
  border-color: var(--color-border-hover);
  background: var(--color-surface-3);
}

.checklist-item-tile.checked {
  border-color: var(--accent-1);
  background: rgba(238, 130, 39, 0.08);
  box-shadow: 0 0 10px rgba(238, 130, 39, 0.05);
}

.checklist-item-tile.checked .tile-label {
  color: var(--accent-1);
}

/* Corretiva: textarea de descrição */
.descricao-corretiva-wrap {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  margin-bottom: 1.5rem;
}

.descricao-corretiva {
  width: 100%;
  resize: vertical;
  min-height: 120px;
  font-family: inherit;
  font-size: 0.95rem;
  line-height: 1.6;
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  padding: 0.85rem 1rem;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.descricao-corretiva:focus {
  outline: none;
  border-color: var(--color-danger);
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.15);
}

.descricao-corretiva::placeholder {
  color: var(--text-muted);
  font-style: italic;
}

.descricao-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}

.descricao-hint {
  font-size: 0.78rem;
  color: var(--text-muted);
  font-style: italic;
}

.descricao-contador {
  font-size: 0.78rem;
  color: var(--text-muted);
  white-space: nowrap;
  font-variant-numeric: tabular-nums;
}

.descricao-contador.contador-limite {
  color: var(--color-danger);
  font-weight: 700;
}

/* Actions */
.atendimento-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1.5rem;
}

/* Animation for Expand */
.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease-out;
  max-height: 400px;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
  overflow: hidden;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .page-header-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .view-toggle {
    width: 100%;
  }

  .toggle-btn {
    flex: 1;
    justify-content: center;
  }

  .atendimento-container.grid-mode {
    grid-template-columns: 1fr;
  }

  .atendimento-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }

  .manutencao-status-info {
    width: 100%;
    justify-content: space-between;
    font-size: 0.85rem;
  }

  .checklist-grid {
    grid-template-columns: 1fr;
  }

  .atendimento-actions {
    flex-direction: column-reverse;
  }

  .atendimento-actions .btn {
    width: 100%;
    justify-content: center;
  }

  .filtros-bar {
    flex-direction: column;
  }

  .busca-wrap,
  .data-wrap,
  .ordenacao-wrap {
    width: 100%;
    min-width: unset;
  }

  .limpar-btn {
    width: 100%;
  }

  .descricao-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.3rem;
  }
}
</style>