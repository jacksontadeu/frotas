<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
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

const router = useRouter()
function navegarParaManutencao(veiculoId: number) {
  router.push({ name: 'cadastrar-manutencao', query: { veiculoId } })
}

const tipoIcon: Record<string, string> = {
  CARRO: '🚗',
  CAMINHAO: '🚛',
  VAN: '🚐',
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

// ── Ordenação dos grupos de base ────────────────────────────────────────────
type Ordenacao = 'base_asc' | 'base_desc'
const ordenacao = ref<Ordenacao>('base_asc')

const veiculosFiltrados = computed(() => {
  if (!buscaQuery.value.trim()) return veiculos.value
  const q = buscaQuery.value.trim().toLowerCase()
  return veiculos.value.filter(
    (v) =>
      v.placaVeiculo?.toLowerCase().includes(q) ||
      v.frota?.toLowerCase().includes(q)
  )
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
  grupos.sort((a, b) =>
    ordenacao.value === 'base_desc'
      ? b.base.localeCompare(a.base, 'pt-BR')
      : a.base.localeCompare(b.base, 'pt-BR')
  )
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

    <!-- Filtros: busca por placa/frota + ordenação por base -->
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
        <label class="ordenacao-label" for="ordenacao">Ordenar bases</label>
        <select id="ordenacao" v-model="ordenacao" class="form-control">
          <option value="base_asc">Base (A-Z)</option>
          <option value="base_desc">Base (Z-A)</option>
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
            class="veiculo-card"
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
            </div>

            <div class="veiculo-card-footer">
              <button
                class="btn-card-action btn-card-manutencao"
                @click="navegarParaManutencao(veiculo.id)"
              >
                🔧 Manutenção
              </button>
              <button
                class="btn-card-action btn-card-delete"
                :disabled="deletingId === veiculo.id"
                @click="confirmarExclusao(veiculo.id, veiculo.nome)"
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
              </p>
            </div>
            <div class="list-item-actions">
              <button
                class="btn btn-warning btn-sm"
                @click="navegarParaManutencao(veiculo.id)"
              >
                🔧 Manutenção
              </button>
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
      </section>
    </div>
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

@media (max-width: 576px) {
  .filtros-bar {
    flex-direction: column;
    align-items: stretch;
  }
  .ordenacao-wrap {
    min-width: 0;
  }
}
</style>