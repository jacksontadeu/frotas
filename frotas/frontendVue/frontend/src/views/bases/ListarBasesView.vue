<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { baseService } from '../../services/baseService'
import { usuarioService } from '../../services/usuarioService'
import type { BaseResponse, BaseDTORequest, UsuarioResponse } from '../../types'
import AppModal from '../../components/AppModal.vue'
import { extractErrorMessage } from '../../composables/useErrorMessage'

// ── Modo de visualização (persistido) ───────────────────────────────────────
type ViewMode = 'cards' | 'list'
const viewMode = ref<ViewMode>(
  (localStorage.getItem('bases-view-mode') as ViewMode) ?? 'cards'
)
function setViewMode(mode: ViewMode) {
  viewMode.value = mode
  localStorage.setItem('bases-view-mode', mode)
}

// ── Helpers para extrair dados normalizados da base ─────────────────────────
// Normaliza o campo 'responsavel' independente do formato retornado pela API:
// pode ser: objeto { id, nome, email, telefone }, string com nome, ou null.
function normalizeResponsavel(raw: any): UsuarioResponse | null {
  if (!raw) return null
  if (typeof raw === 'string') {
    // API retornou apenas o nome como string
    return { id: 0, nome: raw, email: '', telefone: '' }
  }
  if (typeof raw === 'object' && raw.nome) return raw as UsuarioResponse
  return null
}

function getEmail(base: any): string {
  return base.emailBase || base.email || base.responsavel?.email || ''
}
function getTelefone(base: any): string {
  return base.telefoneBase || base.telefone || base.responsavel?.telefone || ''
}
function getResponsavelNome(base: BaseResponse): string {
  const resp = normalizeResponsavel(base.responsavel)
  return resp?.nome || ''
}

// ── Lista de bases ──────────────────────────────────────────────────────────
const bases = ref<BaseResponse[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const deletingId = ref<number | null>(null)

async function carregarBases() {
  try {
    loading.value = true
    error.value = null
    const resultado = await baseService.listarTodas()
    bases.value = resultado
    // Debug: inspecione no console o que a API retorna
    if (resultado.length > 0) {
      console.log('[ListarBases] Primeiro registro recebido da API:', JSON.stringify(resultado[0], null, 2))
    }
  } catch (e) {
    error.value = 'Erro ao carregar bases. Verifique se o servidor está rodando.'
  } finally {
    loading.value = false
  }
}

// ── Modal de confirmação / resultado ────────────────────────────────────────
const showModal = ref(false)
const modalType = ref<'success' | 'error' | 'confirm'>('confirm')
const modalMessage = ref('')
const pendingDeleteId = ref<number | null>(null)
const pendingDeleteNome = ref('')

function confirmarExclusao(id: number, nome: string) {
  pendingDeleteId.value = id
  pendingDeleteNome.value = nome
  modalType.value = 'confirm'
  modalMessage.value = `Tem certeza que deseja excluir a base "${nome}"? Esta ação não pode ser desfeita.`
  showModal.value = true
}

async function executarExclusao() {
  showModal.value = false
  if (!pendingDeleteId.value) return
  const id = pendingDeleteId.value
  try {
    deletingId.value = id
    await baseService.excluir(id)
    bases.value = bases.value.filter((b) => b.id !== id)
    modalType.value = 'success'
    modalMessage.value = `Base "${pendingDeleteNome.value}" excluída com sucesso!`
    showModal.value = true
  } catch (e: any) {
    modalType.value = 'error'
    modalMessage.value = extractErrorMessage(e, 'Erro ao excluir base.')
    showModal.value = true
  } finally {
    deletingId.value = null
    pendingDeleteId.value = null
  }
}

function fecharModal() {
  showModal.value = false
}

// ── Modal de Edição ─────────────────────────────────────────────────────────
const showEditModal = ref(false)
const savingEdit = ref(false)
const editForm = ref<BaseDTORequest & { id: number }>({
  id: 0,
  nome: '',
  emailBase: '',
  telefoneBase: '',
  responsavelId: null,
  usuarioId: null,
})

const usuarios = ref<UsuarioResponse[]>([])
const loadingUsuarios = ref(false)
const erroUsuarios = ref<string | null>(null)
const usuarioSelecionadoEdit = ref<UsuarioResponse | null>(null)
const showBuscaUsuarioModal = ref(false)
const buscaUsuarioQuery = ref('')

const usuariosFiltrados = computed(() => {
  if (!buscaUsuarioQuery.value.trim()) return usuarios.value
  const q = buscaUsuarioQuery.value.toLowerCase()
  return usuarios.value.filter(
    (u) =>
      u.nome.toLowerCase().includes(q) ||
      u.email.toLowerCase().includes(q) ||
      (u.telefone && u.telefone.includes(q))
  )
})

async function carregarUsuarios() {
  loadingUsuarios.value = true
  erroUsuarios.value = null
  try {
    const list = await usuarioService.listarTodos()
    usuarios.value = Array.isArray(list) ? list : []
    if (usuarios.value.length === 0) {
      erroUsuarios.value = 'Nenhum usuário encontrado no banco de dados.'
    }
  } catch (err: any) {
    usuarios.value = []
    erroUsuarios.value = extractErrorMessage(err, 'Erro ao carregar usuários.')
  } finally {
    loadingUsuarios.value = false
  }
}

function abrirEdicao(base: BaseResponse) {
  const resp = normalizeResponsavel(base.responsavel)
  editForm.value = {
    id: base.id,
    nome: base.nome,
    emailBase: getEmail(base),
    telefoneBase: getTelefone(base),
    // se responsavel veio só como string (sem id real), não vincula id
    responsavelId: resp && resp.id ? resp.id : null,
    usuarioId: resp && resp.id ? resp.id : null,
  }
  // só define como selecionado se tiver dados de usuário reais (com id)
  usuarioSelecionadoEdit.value = (resp && resp.id) ? resp : null
  showEditModal.value = true
  if (usuarios.value.length === 0 && !loadingUsuarios.value) {
    carregarUsuarios()
  }
}

function fecharEdicao() {
  showEditModal.value = false
  buscaUsuarioQuery.value = ''
  showBuscaUsuarioModal.value = false
}

function onSelectUsuarioChangeEdit(event: Event) {
  const target = event.target as HTMLSelectElement
  const idStr = target.value
  if (!idStr) { removerResponsavelEdit(); return }
  const id = Number(idStr)
  const user = usuarios.value.find((u) => u.id === id)
  if (user) selecionarUsuarioEdit(user)
}

function selecionarUsuarioEdit(u: UsuarioResponse) {
  usuarioSelecionadoEdit.value = u
  editForm.value.responsavelId = u.id
  editForm.value.usuarioId = u.id
  editForm.value.emailBase = u.email
  editForm.value.telefoneBase = u.telefone || ''
  showBuscaUsuarioModal.value = false
}

function removerResponsavelEdit() {
  usuarioSelecionadoEdit.value = null
  editForm.value.responsavelId = null
  editForm.value.usuarioId = null
  editForm.value.emailBase = ''
  editForm.value.telefoneBase = ''
}

async function salvarEdicao() {
  savingEdit.value = true
  try {
    const { id, ...data } = editForm.value
    await baseService.atualizar(id, data)

    // Atualiza localmente com todos os campos para exibição imediata
    const idx = bases.value.findIndex((b) => b.id === id)
    if (idx !== -1) {
      // Monta o responsavel normalizado para a listagem local
      const respLocal = usuarioSelecionadoEdit.value
        ? {
            id: usuarioSelecionadoEdit.value.id,
            nome: usuarioSelecionadoEdit.value.nome,
            email: usuarioSelecionadoEdit.value.email,
            telefone: usuarioSelecionadoEdit.value.telefone,
          }
        : bases.value[idx].responsavel
      const updated: any = {
        ...bases.value[idx],
        nome: data.nome,
        emailBase: data.emailBase,
        telefoneBase: data.telefoneBase,
        responsavel: respLocal,
      }
      bases.value = bases.value.map((b) => (b.id === id ? updated : b))
    }
    fecharEdicao()
    modalType.value = 'success'
    modalMessage.value = `Base "${data.nome}" atualizada com sucesso!`
    showModal.value = true
  } catch (e: any) {
    fecharEdicao()
    modalType.value = 'error'
    modalMessage.value = extractErrorMessage(e, 'Erro ao atualizar base.')
    showModal.value = true
  } finally {
    savingEdit.value = false
  }
}

onMounted(carregarBases)
</script>

<template>
  <div class="page-content">
    <!-- Cabeçalho -->
    <div class="page-header-row">
      <div>
        <h1 class="page-title">🏢 Bases</h1>
        <p class="page-subtitle">{{ bases.length }} base(s) cadastrada(s)</p>
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
        <RouterLink to="/cadastros/cadastrar-base" class="btn btn-primary">
          + Nova Base
        </RouterLink>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>Carregando bases...</p>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="alert alert-danger">⚠️ {{ error }}</div>

    <!-- Empty -->
    <div v-else-if="bases.length === 0" class="empty-state">
      <span class="empty-state-icon">🏢</span>
      <p>Nenhuma base cadastrada ainda.</p>
      <RouterLink to="/cadastros/cadastrar-base" class="btn btn-primary">
        Cadastrar primeira base
      </RouterLink>
    </div>

    <!-- ── GRID DE CARDS ── -->
    <div v-else-if="viewMode === 'cards'" class="bases-grid">
      <div
        v-for="base in bases"
        :key="base.id"
        class="base-card"
      >
        <!-- Stripe superior colorido -->
        <div class="base-card-stripe"></div>

        <!-- Topo do card -->
        <div class="base-card-top">
          <div class="base-id-badge">#{{ base.id }}</div>
          <h3 class="base-card-nome">{{ base.nome }}</h3>
        </div>

        <!-- Divider -->
        <div class="base-card-divider"></div>

        <!-- Informações -->
        <div class="base-card-info">
          <div class="info-row">
            <span class="info-icon">📧</span>
            <div class="info-text">
              <span class="info-label">Email</span>
              <span class="info-value">{{ getEmail(base) || '—' }}</span>
            </div>
          </div>
          <div class="info-row">
            <span class="info-icon">📞</span>
            <div class="info-text">
              <span class="info-label">Telefone</span>
              <span class="info-value">{{ getTelefone(base) || '—' }}</span>
            </div>
          </div>
          <div class="info-row">
            <span class="info-icon">👤</span>
            <div class="info-text">
              <span class="info-label">Responsável</span>
              <span class="info-value" :class="{ 'responsavel-nome': !!getResponsavelNome(base) }">
                {{ getResponsavelNome(base) || '—' }}
              </span>
            </div>
          </div>
          
        </div>

        <!-- Ações -->
        <div class="base-card-footer">
          <button class="btn-card-action btn-card-edit" @click="abrirEdicao(base)">
            ✏️ Alterar
          </button>
          <button
            class="btn-card-action btn-card-delete"
            :disabled="deletingId === base.id"
            @click="confirmarExclusao(base.id, base.nome)"
          >
            {{ deletingId === base.id ? '⏳ Excluindo...' : '🗑️ Excluir' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ── MODO LISTA ── -->
    <div v-else class="bases-list-wrap">
      <!-- Cabeçalho da tabela -->
      <div class="list-table-header">
        <span class="lt-col lt-id">#</span>
        <span class="lt-col lt-nome">Nome da Base</span>
        <span class="lt-col lt-email">Email</span>
        <span class="lt-col lt-tel">Telefone</span>
        <span class="lt-col lt-resp">Responsável</span>
        <span class="lt-col lt-actions">Ações</span>
      </div>

      <ul class="list-table-body">
        <li
          v-for="base in bases"
          :key="base.id"
          class="list-table-row"
        >
          <span class="lt-col lt-id">
            <span class="base-id-badge">#{{ base.id }}</span>
          </span>
          <span class="lt-col lt-nome">
            <span class="list-nome">{{ base.nome }}</span>
          </span>
          <span class="lt-col lt-email">
            <span class="list-data-value">{{ getEmail(base) || '—' }}</span>
          </span>
          <span class="lt-col lt-tel">
            <span class="list-data-value">{{ getTelefone(base) || '—' }}</span>
          </span>
          <span class="lt-col lt-resp">
            <span
              v-if="getResponsavelNome(base)"
              class="responsavel-chip-sm"
            >👤 {{ getResponsavelNome(base) }}</span>
            <span v-else class="text-muted">—</span>
          </span>
          <span class="lt-col lt-actions">
            <button
              class="btn-row-action btn-row-edit"
              @click="abrirEdicao(base)"
              title="Alterar base"
            >✏️</button>
            <button
              class="btn-row-action btn-row-delete"
              :disabled="deletingId === base.id"
              @click="confirmarExclusao(base.id, base.nome)"
              title="Excluir base"
            >{{ deletingId === base.id ? '⏳' : '🗑️' }}</button>
          </span>
        </li>
      </ul>
    </div>
  </div>

  <!-- ─────────────────────────────────────── Modal de Edição ── -->
  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="showEditModal" class="modal-backdrop" @click.self="fecharEdicao">
        <Transition name="modal-pop">
          <div v-if="showEditModal" class="edit-modal-card">
            <!-- Topo colorido -->
            <div class="edit-modal-stripe"></div>

            <!-- Header -->
            <div class="edit-modal-header">
              <div>
                <h3>✏️ Alterar Base</h3>
                <p class="edit-modal-subtitle">Edite os dados da base e salve</p>
              </div>
              <button class="btn-close" @click="fecharEdicao">✖</button>
            </div>

            <!-- Body -->
            <div class="edit-modal-body">
              <!-- Nome -->
              <div class="form-group">
                <label class="form-label">Nome da Base</label>
                <input
                  v-model="editForm.nome"
                  type="text"
                  class="form-control"
                  placeholder="Nome da base"
                  :disabled="savingEdit"
                />
              </div>

              <!-- Responsável -->
              <div class="form-group">
                <div class="responsavel-header">
                  <label class="form-label">Responsável pela Base</label>
                  <div class="responsavel-actions">
                    <button
                      type="button"
                      class="btn btn-secondary btn-sm"
                      @click="carregarUsuarios"
                      :disabled="loadingUsuarios || savingEdit"
                    >
                      🔄 {{ loadingUsuarios ? 'Buscando...' : 'Atualizar' }}
                    </button>
                    <button
                      type="button"
                      class="btn btn-secondary btn-sm"
                      @click="showBuscaUsuarioModal = true"
                      :disabled="savingEdit"
                    >
                      🔍 {{ usuarioSelecionadoEdit ? 'Buscar / Alterar' : 'Procurar' }}
                    </button>
                  </div>
                </div>

                <select
                  class="form-control"
                  :value="editForm.usuarioId || ''"
                  @change="onSelectUsuarioChangeEdit"
                  :disabled="loadingUsuarios || savingEdit"
                >
                  <option value="">
                    {{ loadingUsuarios ? '⏳ Carregando...' : usuarios.length > 0 ? '-- Selecione um Responsável --' : '-- Nenhum usuário encontrado --' }}
                  </option>
                  <option v-for="u in usuarios" :key="u.id" :value="u.id">
                    {{ u.nome }} ({{ u.email }})
                  </option>
                </select>

                <div v-if="erroUsuarios && usuarios.length === 0" class="info-alert info-alert-warning" style="margin-top:0.35rem;">
                  ⚠️ {{ erroUsuarios }}
                </div>

                <!-- Card responsável selecionado -->
                <div v-if="usuarioSelecionadoEdit" class="usuario-selecionado-card">
                  <div class="usuario-info-main">
                    <span class="usuario-icon">👤</span>
                    <div>
                      <p class="usuario-nome-edit">{{ usuarioSelecionadoEdit.nome }}</p>
                      <p class="usuario-contato">
                        <span>📧 {{ usuarioSelecionadoEdit.email }}</span>
                        <span class="dot-sep">•</span>
                        <span>📞 {{ usuarioSelecionadoEdit.telefone || 'Não informado' }}</span>
                      </p>
                    </div>
                  </div>
                  <button type="button" class="btn-remove-user" @click="removerResponsavelEdit">✖</button>
                </div>
                <p v-else class="help-text text-muted">Selecione um usuário para autopreencher email e telefone.</p>
              </div>

              <!-- Email -->
              <div class="form-group">
                <label class="form-label">
                  Email do Responsável
                  <span v-if="usuarioSelecionadoEdit" class="auto-badge">⚡ Auto</span>
                </label>
                <input
                  v-model="editForm.emailBase"
                  type="email"
                  class="form-control"
                  placeholder="Email"
                  :readonly="!!usuarioSelecionadoEdit"
                  :disabled="savingEdit"
                />
              </div>

              <!-- Telefone -->
              <div class="form-group">
                <label class="form-label">
                  Telefone do Responsável
                  <span v-if="usuarioSelecionadoEdit" class="auto-badge">⚡ Auto</span>
                </label>
                <input
                  v-model="editForm.telefoneBase"
                  type="tel"
                  class="form-control"
                  placeholder="Telefone"
                  :readonly="!!usuarioSelecionadoEdit"
                  :disabled="savingEdit"
                />
              </div>
            </div>

            <!-- Footer -->
            <div class="edit-modal-footer">
              <button class="btn btn-secondary" @click="fecharEdicao" :disabled="savingEdit">Cancelar</button>
              <button
                class="btn btn-primary"
                @click="salvarEdicao"
                :disabled="savingEdit || !editForm.nome"
              >
                <span v-if="savingEdit">⏳ Salvando...</span>
                <span v-else>💾 Salvar Alterações</span>
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>

  <!-- ────────────────── Modal busca de usuário ── -->
  <Teleport to="body">
    <div v-if="showBuscaUsuarioModal" class="modal-backdrop" @click.self="showBuscaUsuarioModal = false" style="z-index:1200;">
      <div class="busca-modal-card">
        <div class="modal-header">
          <h3>🔍 Selecionar Responsável</h3>
          <button class="btn-close" @click="showBuscaUsuarioModal = false">✖</button>
        </div>
        <div class="modal-body">
          <input
            v-model="buscaUsuarioQuery"
            type="text"
            class="form-control"
            placeholder="Buscar por nome, email ou telefone..."
            autofocus
            style="margin-bottom:1rem;"
          />
          <div v-if="loadingUsuarios" class="empty-state-small">
            <span class="spinner" style="display:inline-block;"></span> Carregando...
          </div>
          <div v-else-if="usuariosFiltrados.length === 0" class="empty-state-small">
            <span v-if="erroUsuarios">⚠️ {{ erroUsuarios }}</span>
            <span v-else>Nenhum usuário encontrado.</span>
          </div>
          <ul v-else class="usuarios-list">
            <li v-for="u in usuariosFiltrados" :key="u.id" class="usuario-item" @click="selecionarUsuarioEdit(u)">
              <span class="user-avatar">👤</span>
              <div class="user-details">
                <span class="user-name">{{ u.nome }}</span>
                <span class="user-meta">📧 {{ u.email }}<template v-if="u.telefone"> • 📞 {{ u.telefone }}</template></span>
              </div>
              <span class="btn-select">Selecionar</span>
            </li>
          </ul>
        </div>
        <div class="modal-footer-small">
          <button class="btn btn-secondary" @click="showBuscaUsuarioModal = false">Fechar</button>
        </div>
      </div>
    </div>
  </Teleport>

  <!-- Modal genérico -->
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
  margin-bottom: 1.75rem;
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
  box-shadow: 0 2px 8px rgba(238,130,39,0.35);
}

/* ── Modo Lista (tabela) ── */
.bases-list-wrap {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.list-table-header {
  display: grid;
  grid-template-columns: 60px 1fr 1fr 1fr 1fr 100px;
  padding: 0.7rem 1.25rem;
  background: var(--color-surface-2);
  border-bottom: 1px solid var(--color-border);
  gap: 1rem;
}

/* Layout puro, compartilhado entre cabeçalho e linhas de dados */
.lt-col {
  display: flex;
  align-items: center;
}

/* Estilo tipográfico (uppercase, tamanho, peso) restrito ao cabeçalho,
   para não vazar para as células de dados nas linhas da tabela */
.list-table-header .lt-col {
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--text-muted);
}

.list-table-body {
  list-style: none;
  padding: 0;
  margin: 0;
}

.list-table-row {
  display: grid;
  grid-template-columns: 60px 1fr 1fr 1fr 1fr 100px;
  padding: 0.85rem 1.25rem;
  gap: 1rem;
  border-bottom: 1px solid var(--color-border);
  align-items: center;
  transition: background 0.15s ease;
}

.list-table-row:last-child {
  border-bottom: none;
}

.list-table-row:hover {
  background: var(--color-surface-2);
}

.list-nome {
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--text-primary);
}

.list-data-value {
  font-size: 0.85rem;
  color: var(--text-secondary);
  word-break: break-word;
}

.responsavel-chip-sm {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  background: rgba(238, 130, 39, 0.1);
  border: 1px solid rgba(238, 130, 39, 0.22);
  color: var(--accent-3, #f6a55c);
  padding: 0.2rem 0.55rem;
  border-radius: 99px;
  font-size: 0.78rem;
  font-weight: 600;
  white-space: nowrap;
}

.lt-actions {
  justify-content: flex-end;
  gap: 0.35rem;
}

.btn-row-action {
  width: 34px;
  height: 34px;
  border-radius: var(--radius-sm);
  border: 1px solid transparent;
  cursor: pointer;
  font-size: 1rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s ease;
}

.btn-row-edit {
  background: rgba(99, 102, 241, 0.1);
  border-color: rgba(99, 102, 241, 0.2);
}
.btn-row-edit:hover {
  background: rgba(99, 102, 241, 0.22);
  border-color: rgba(99, 102, 241, 0.45);
}

.btn-row-delete {
  background: rgba(239, 68, 68, 0.08);
  border-color: rgba(239, 68, 68, 0.18);
}
.btn-row-delete:hover:not(:disabled) {
  background: rgba(239, 68, 68, 0.2);
  border-color: rgba(239, 68, 68, 0.4);
}
.btn-row-delete:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .list-table-header { display: none; }
  .list-table-row {
    grid-template-columns: 1fr;
    gap: 0.5rem;
    padding: 1rem 1.25rem;
  }
  .lt-col::before {
    content: attr(data-label);
    font-size: 0.7rem;
    font-weight: 700;
    text-transform: uppercase;
    color: var(--text-muted);
    display: block;
    margin-bottom: 0.1rem;
  }
  .lt-actions { justify-content: flex-start; }
}

/* ── Grid de Cards ── */
.bases-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.25rem;
}

/* ── Card ── */
.base-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.base-card:hover {
  transform: translateY(-3px);
  border-color: rgba(238, 130, 39, 0.4);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.35), 0 0 0 1px rgba(238, 130, 39, 0.1);
}

.base-card-stripe {
  height: 4px;
  background: var(--gradient);
  flex-shrink: 0;
}

.base-card-top {
  padding: 1.1rem 1.25rem 0.85rem;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.base-id-badge {
  display: inline-flex;
  align-items: center;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  color: var(--accent-1);
  background: rgba(238, 130, 39, 0.12);
  border: 1px solid rgba(238, 130, 39, 0.22);
  padding: 0.1rem 0.5rem;
  border-radius: 99px;
  width: fit-content;
}

.base-card-nome {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.3;
}

.base-card-divider {
  height: 1px;
  background: var(--color-border);
  margin: 0 1.25rem;
}

/* ── Info Rows ── */
.base-card-info {
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

.responsavel-nome {
  font-weight: 600;
  color: var(--accent-3, #f6a55c);
}

/* ── Rodapé do Card (ações) ── */
.base-card-footer {
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
}

.btn-card-edit {
  background: rgba(99, 102, 241, 0.12);
  color: #a5b4fc;
  border-color: rgba(99, 102, 241, 0.25);
}

.btn-card-edit:hover {
  background: rgba(99, 102, 241, 0.22);
  border-color: rgba(99, 102, 241, 0.5);
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

/* ── Modal de Edição ── */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1100;
  padding: 1rem;
}

.edit-modal-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  width: 100%;
  max-width: 560px;
  max-height: 92vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 24px 80px rgba(0,0,0,0.6);
  overflow: hidden;
}

.edit-modal-stripe {
  height: 4px;
  background: linear-gradient(90deg, #818cf8, #6366f1);
  flex-shrink: 0;
}

.edit-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 1.1rem 1.25rem 0.9rem;
  border-bottom: 1px solid var(--color-border);
}

.edit-modal-header h3 {
  margin: 0 0 0.15rem 0;
  font-size: 1.1rem;
  font-weight: 700;
}

.edit-modal-subtitle {
  margin: 0;
  font-size: 0.8rem;
  color: var(--text-muted);
}

.edit-modal-body {
  padding: 1.25rem;
  overflow-y: auto;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.edit-modal-footer {
  padding: 0.85rem 1.25rem;
  border-top: 1px solid var(--color-border);
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  background: var(--color-surface-2);
}

/* ── Modal de Busca ── */
.busca-modal-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  width: 100%;
  max-width: 520px;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 24px 80px rgba(0,0,0,0.6);
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--color-border);
}

.modal-header h3 {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
}

.modal-body {
  padding: 1.25rem;
  overflow-y: auto;
  flex: 1;
}

.modal-footer-small {
  padding: 0.85rem 1.25rem;
  border-top: 1px solid var(--color-border);
  display: flex;
  justify-content: flex-end;
}

.empty-state-small {
  text-align: center;
  padding: 2rem 1rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.usuarios-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.usuario-item {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  padding: 0.75rem 1rem;
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s ease;
}

.usuario-item:hover {
  background: rgba(238, 130, 39, 0.12);
  border-color: var(--accent-1);
}

.user-avatar { font-size: 1.5rem; }
.user-details { display: flex; flex-direction: column; flex: 1; }
.user-name { font-weight: 700; font-size: 0.95rem; color: var(--text-primary); }
.user-meta { font-size: 0.78rem; color: var(--text-secondary); }
.btn-select { font-size: 0.8rem; font-weight: 600; color: var(--accent-1); }

/* ── Responsável card ── */
.responsavel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.responsavel-actions { display: flex; gap: 0.5rem; }

.usuario-selecionado-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(238, 130, 39, 0.08);
  border: 1px solid rgba(238, 130, 39, 0.25);
  border-radius: var(--radius-md);
  padding: 0.85rem 1.1rem;
  margin-top: 0.5rem;
}

.usuario-info-main { display: flex; align-items: center; gap: 0.85rem; }
.usuario-icon { font-size: 1.75rem; }
.usuario-nome-edit { margin: 0 0 0.15rem 0; font-size: 0.95rem; font-weight: 700; color: var(--text-primary); }
.usuario-contato { margin: 0; font-size: 0.8rem; color: var(--text-secondary); display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap; }
.dot-sep { color: var(--text-muted); }

.btn-remove-user {
  background: transparent;
  border: none;
  color: var(--text-muted);
  font-size: 1rem;
  cursor: pointer;
  padding: 0.25rem;
  transition: color 0.2s;
}
.btn-remove-user:hover { color: var(--color-danger); }

.btn-close {
  background: transparent;
  border: none;
  color: var(--text-muted);
  font-size: 1.1rem;
  cursor: pointer;
  transition: color 0.2s;
  padding: 0.25rem;
}
.btn-close:hover { color: var(--text-primary); }

.auto-badge {
  font-size: 0.7rem;
  background: rgba(238, 130, 39, 0.15);
  color: var(--accent-1);
  padding: 0.1rem 0.4rem;
  border-radius: 4px;
  font-weight: 600;
  margin-left: 0.4rem;
}

.help-text { font-size: 0.85rem; margin-top: 0.35rem; }

.info-alert { font-size: 0.85rem; padding: 0.5rem 0.75rem; border-radius: var(--radius-sm); }
.info-alert-warning {
  background: rgba(255, 193, 7, 0.1);
  border: 1px solid rgba(255, 193, 7, 0.3);
  color: var(--text-primary);
}

/* ── Transitions ── */
.modal-fade-enter-active, .modal-fade-leave-active { transition: opacity 0.2s ease; }
.modal-fade-enter-from, .modal-fade-leave-to { opacity: 0; }

.modal-pop-enter-active { transition: opacity 0.22s ease, transform 0.22s cubic-bezier(0.16,1,0.3,1); }
.modal-pop-leave-active { transition: opacity 0.15s ease, transform 0.15s ease; }
.modal-pop-enter-from { opacity: 0; transform: scale(0.9) translateY(20px); }
.modal-pop-leave-to { opacity: 0; transform: scale(0.95); }
</style>