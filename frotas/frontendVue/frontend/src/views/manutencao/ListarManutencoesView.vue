<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { manutencaoService } from '../../services/manutencaoService'
import type { ManutencaoResponse } from '../../types'

const manutencoes = ref<ManutencaoResponse[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

async function carregarManutencoes() {
  try {
    loading.value = true
    error.value = null
    manutencoes.value = await manutencaoService.listarTodas()
  } catch (e) {
    error.value = 'Erro ao carregar lista de manutenções. Verifique se o servidor está rodando.'
  } finally {
    loading.value = false
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
    <div class="page-header" style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem;">
      <div>
        <h1 class="page-title">🔧 Manutenções</h1>
        <p class="page-subtitle">{{ manutencoes.length }} manutenção(ões) registrada(s)</p>
      </div>
      <RouterLink to="/cadastros/cadastrar-manutencao" class="btn btn-primary">
        + Registrar Manutenção
      </RouterLink>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>Carregando manutenções...</p>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="alert alert-danger">⚠️ {{ error }}</div>

    <!-- Empty -->
    <div v-else-if="manutencoes.length === 0" class="empty-state">
      <span class="empty-state-icon">🔧</span>
      <p>Nenhuma manutenção registrada ainda.</p>
      <RouterLink to="/cadastros/cadastrar-manutencao" class="btn btn-primary">
        Registrar primeira manutenção
      </RouterLink>
    </div>

    <!-- List -->
    <ul v-else style="list-style: none; display: flex; flex-direction: column; gap: 1rem;">
      <li
        v-for="m in manutencoes"
        :key="m.id"
        class="list-item"
        style="align-items: stretch; flex-direction: column; gap: 0.75rem;"
      >
        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.75rem;">
          <div class="info-primary" style="display: flex; align-items: center; gap: 0.75rem;">
            <!-- Status Badge at front -->
            <span
              class="status-badge"
              :class="m.status === 'EM_ABERTO' ? 'status-open' : 'status-closed'"
            >
              {{ m.status === 'EM_ABERTO' ? 'Em Aberto' : 'Finalizada' }}
            </span>

            <span class="veiculo-icon-small" style="font-size: 1.5rem;">
              {{ tipoIcon[m.veiculo.tipoVeiculo] ?? '🚙' }}
            </span>
            <div>
              <h5 style="margin: 0; font-weight: 700;">
                {{ m.veiculo.nome }} 
                <span class="text-muted" style="font-weight: 500; font-size: 0.85rem;">({{ m.veiculo.placaVeiculo }})</span>
              </h5>
            </div>
          </div>

          <div style="display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap;">
            <span class="badge" :class="m.tipoManutencao === 'corretiva' ? 'badge-danger' : 'badge-purple'">
              {{ formatarTipo(m.tipoManutencao) }}
            </span>
            <span style="font-size: 0.85rem; font-weight: 500; color: var(--text-secondary);">
              📍 {{ m.kilometragem }} Km
            </span>
            <span style="font-size: 0.85rem; font-weight: 500; color: var(--text-secondary);">
              📅 Realizada: {{ formatarData(m.dataRealizacao) }}
            </span>
            <span v-if="m.dataProximaManutencao" style="font-size: 0.85rem; font-weight: 500; color: var(--color-success);">
              📅 Próxima: {{ formatarData(m.dataProximaManutencao) }}
            </span>
          </div>
        </div>

        <!-- Checklist Summary for Completed Maintenances -->
        <div v-if="m.status === 'FINALIZADA'" class="checklist-summary">
          <span class="checklist-summary-title">Serviços executados:</span>
          <div class="checklist-pills">
            <span v-if="m.trocaOleo" class="checklist-pill">🛢️ Troca de Óleo</span>
            <span v-if="m.revisaoArrefecimento" class="checklist-pill">🧪 Arrefecimento</span>
            <span v-if="m.revisaoFreios" class="checklist-pill">🛑 Freios</span>
            <span v-if="m.embreagem" class="checklist-pill">⚙️ Embreagem</span>
            <span v-if="m.faroisLampadas" class="checklist-pill">💡 Faróis/Lâmpadas</span>
            <span v-if="!m.trocaOleo && !m.revisaoArrefecimento && !m.revisaoFreios && !m.embreagem && !m.faroisLampadas" class="checklist-pill-none">
              Nenhum item marcado no checklist
            </span>
          </div>
        </div>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.35rem 0.75rem;
  border-radius: var(--radius-sm);
  font-size: 0.8rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  white-space: nowrap;
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
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  color: var(--text-muted);
}

.checklist-pills {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.checklist-pill {
  font-size: 0.75rem;
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

@media (max-width: 576px) {
  .checklist-summary {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }
}
</style>
