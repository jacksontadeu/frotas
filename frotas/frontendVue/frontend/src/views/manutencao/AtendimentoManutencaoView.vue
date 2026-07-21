<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { manutencaoService } from '../../services/manutencaoService'
import { extractErrorMessage } from '../../composables/useErrorMessage'
import type { ManutencaoResponse } from '../../types'

const manutencoes = ref<ManutencaoResponse[]>([])
const loading = ref(true)
const submitting = ref<number | null>(null)
const error = ref<string | null>(null)
const success = ref<string | null>(null)

// ID da manutenção atualmente expandida
const expandedId = ref<number | null>(null)

// Modo de visualização: 'list' (lista) ou 'grid' (cards)
const viewMode = ref<'list' | 'grid'>('list')

// Checklist para a manutenção selecionada
const checklist = ref({
  trocaOleo: false,
  revisaoArrefecimento: false,
  revisaoFreios: false,
  embreagem: false,
  faroisLampadas: false,
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

function toggleExpand(id: number) {
  if (expandedId.value === id) {
    expandedId.value = null
  } else {
    expandedId.value = id
    // Reseta o checklist ao abrir
    checklist.value = {
      trocaOleo: false,
      revisaoArrefecimento: false,
      revisaoFreios: false,
      embreagem: false,
      faroisLampadas: false,
    }
  }
  success.value = null
  error.value = null
}

async function finalizarAtendimento(id: number) {
  submitting.value = id
  error.value = null
  success.value = null
  try {
    await manutencaoService.atender(id, checklist.value)
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
        <h1 class="page-title">🔧 Atendimento de Manutenções</h1>
        <p class="page-subtitle">Selecione uma manutenção em aberto para realizar o checklist e finalizá-la.</p>
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

    <!-- Empty State -->
    <div v-else-if="manutencoes.length === 0" class="empty-state glass-card">
      <span class="empty-state-icon">✅</span>
      <h3>Nenhuma manutenção em aberto</h3>
      <p>Tudo em dia! Não há atendimentos pendentes no momento.</p>
      <button class="btn btn-secondary" @click="carregarManutencoes">
        🔄 Atualizar Lista
      </button>
    </div>

    <!-- List / Grid of Open Maintenances -->
    <div v-else :class="['atendimento-container', viewMode === 'grid' ? 'grid-mode' : 'list-mode']">
      <div
        v-for="m in manutencoes"
        :key="m.id"
        class="atendimento-card glass-card"
        :class="{ active: expandedId === m.id, 'is-grid': viewMode === 'grid' }"
      >
        <!-- Header: Informações básicas da manutenção -->
        <div class="atendimento-header" @click="toggleExpand(m.id)">
          <div class="veiculo-info">
            <span class="veiculo-icon">{{ tipoIcon[m.veiculo.tipoVeiculo] ?? '🚙' }}</span>
            <div class="veiculo-details">
              <h4>{{ m.veiculo.nome }}</h4>
              <p class="veiculo-meta">
                <span>Placa: <strong>{{ m.veiculo.placaVeiculo }}</strong></span>
                <span class="meta-dot">•</span>
                <span>Base: {{ m.veiculo.base?.nome }}</span>
              </p>
            </div>
          </div>
          
          <div class="manutencao-status-info">
            <span class="badge" :class="m.tipoManutencao === 'corretiva' ? 'badge-danger' : 'badge-purple'">
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
            <h5 class="checklist-title">📋 Checklist de Serviços Realizados</h5>
            <p class="checklist-subtitle">Selecione as manutenções efetuadas no veículo:</p>

            <div class="checklist-grid">
              <!-- Item 1: Troca de óleo -->
              <label class="checklist-item-tile" :class="{ checked: checklist.trocaOleo }">
                <input type="checkbox" v-model="checklist.trocaOleo" />
                <div class="tile-content">
                  <span class="tile-icon">🛢️</span>
                  <div class="tile-text">
                    <span class="tile-label">Troca de Óleo</span>
                    <span class="tile-desc">Substituição do lubrificante e filtro do motor</span>
                  </div>
                </div>
              </label>

              <!-- Item 2: Arrefecimento -->
              <label class="checklist-item-tile" :class="{ checked: checklist.revisaoArrefecimento }">
                <input type="checkbox" v-model="checklist.revisaoArrefecimento" />
                <div class="tile-content">
                  <span class="tile-icon">🧪</span>
                  <div class="tile-text">
                    <span class="tile-label">Sistema de Arrefecimento</span>
                    <span class="tile-desc">Revisão e limpeza do radiador e aditivos</span>
                  </div>
                </div>
              </label>

              <!-- Item 3: Freios -->
              <label class="checklist-item-tile" :class="{ checked: checklist.revisaoFreios }">
                <input type="checkbox" v-model="checklist.revisaoFreios" />
                <div class="tile-content">
                  <span class="tile-icon">🛑</span>
                  <div class="tile-text">
                    <span class="tile-label">Revisão de Freios</span>
                    <span class="tile-desc">Verificação de pastilhas, discos e fluido</span>
                  </div>
                </div>
              </label>

              <!-- Item 4: Embreagem -->
              <label class="checklist-item-tile" :class="{ checked: checklist.embreagem }">
                <input type="checkbox" v-model="checklist.embreagem" />
                <div class="tile-content">
                  <span class="tile-icon">⚙️</span>
                  <div class="tile-text">
                    <span class="tile-label">Embreagem</span>
                    <span class="tile-desc">Inspeção do pedal, disco e rolamento</span>
                  </div>
                </div>
              </label>

              <!-- Item 5: Faróis e Lâmpadas -->
              <label class="checklist-item-tile" :class="{ checked: checklist.faroisLampadas }">
                <input type="checkbox" v-model="checklist.faroisLampadas" />
                <div class="tile-content">
                  <span class="tile-icon">💡</span>
                  <div class="tile-text">
                    <span class="tile-label">Faróis e Lâmpadas</span>
                    <span class="tile-desc">Teste e troca de luzes e lanternas</span>
                  </div>
                </div>
              </label>
            </div>

            <!-- Submit Actions -->
            <div class="atendimento-actions">
              <button
                type="button"
                class="btn btn-secondary"
                :disabled="submitting !== null"
                @click="toggleExpand(m.id)"
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
}
</style>