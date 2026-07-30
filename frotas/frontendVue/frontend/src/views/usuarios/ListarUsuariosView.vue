<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { usuarioService } from '../../services/usuarioService'
import type { UsuarioDTORequest } from '../../services/usuarioService'
import type { UsuarioResponse } from '../../types'
import AppModal from '../../components/AppModal.vue'
import { extractErrorMessage } from '../../composables/useErrorMessage'

// ────────────────────────────────
// Estado da lista
// ────────────────────────────────
const usuarios = ref<UsuarioResponse[]>([])
const loading = ref(true)
const loadingError = ref<string | null>(null)
const busca = ref('')

const usuariosFiltrados = computed(() => {
  const q = busca.value.toLowerCase().trim()
  if (!q) return usuarios.value
  return usuarios.value.filter(
    (u) =>
      u.nome.toLowerCase().includes(q) ||
      u.email.toLowerCase().includes(q) ||
      (u.role ?? '').toLowerCase().includes(q),
  )
})

async function carregarUsuarios() {
  try {
    loading.value = true
    loadingError.value = null
    usuarios.value = await usuarioService.listarTodos()
  } catch {
    loadingError.value = 'Erro ao carregar usuários. Verifique se o servidor está rodando.'
  } finally {
    loading.value = false
  }
}

// ────────────────────────────────
// Formulário (cadastro / edição)
// ────────────────────────────────
const showFormModal = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const editingId = ref<number | null>(null)
const formLoading = ref(false)

const form = reactive<UsuarioDTORequest & { confirmarSenha: string }>({
  nome: '',
  email: '',
  senha: '',
  confirmarSenha: '',
  telefone: '',
  role: 'ADMIN',
})

// Os valores batem com o enum Java: Role.valueOf("ADMIN") e Role.valueOf("TECNICO")
const rolesOptions = [
  { value: 'ADMIN', label: '👑 Administrador' },
  { value: 'TECNICO', label: '🔧 Técnico' },
]



function abrirEdicao(usuario: UsuarioResponse) {
  formMode.value = 'edit'
  editingId.value = usuario.id
  form.nome = usuario.nome
  form.email = usuario.email
  form.senha = ''
  form.confirmarSenha = ''
  form.telefone = usuario.telefone ?? ''
  // A resposta do server usa ROLE_ADMIN; precisa converter para o enum Java: ADMIN
  form.role = (usuario.role ?? 'ROLE_ADMIN').replace('ROLE_', '')
  formErrors.nome = ''
  formErrors.email = ''
  formErrors.senha = ''
  formErrors.confirmarSenha = ''
  showFormModal.value = true
}

function fecharFormModal() {
  showFormModal.value = false
}

// ────────────────────────────────
// Validação
// ────────────────────────────────
const formErrors = reactive({
  nome: '',
  email: '',
  senha: '',
  confirmarSenha: '',
})

function validarForm(): boolean {
  let ok = true
  formErrors.nome = ''
  formErrors.email = ''
  formErrors.senha = ''
  formErrors.confirmarSenha = ''

  if (!form.nome.trim()) {
    formErrors.nome = 'Nome é obrigatório.'
    ok = false
  }
  if (!form.email.trim() || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    formErrors.email = 'E-mail inválido.'
    ok = false
  }
  if (formMode.value === 'create' && !form.senha) {
    formErrors.senha = 'Senha é obrigatória.'
    ok = false
  }
  if (form.senha && form.senha.length < 6) {
    formErrors.senha = 'A senha deve ter pelo menos 6 caracteres.'
    ok = false
  }
  if (form.senha && form.senha !== form.confirmarSenha) {
    formErrors.confirmarSenha = 'As senhas não coincidem.'
    ok = false
  }
  return ok
}

// ────────────────────────────────
// Submissão
// ────────────────────────────────
async function handleSubmit() {
  if (!validarForm()) return
  formLoading.value = true
  try {
    const payload: UsuarioDTORequest = {
      nome: form.nome,
      email: form.email,
      telefone: form.telefone,
      role: form.role,
    }
    if (form.senha) payload.senha = form.senha

    if (formMode.value === 'create') {
      const novo = await usuarioService.cadastrar(payload)
      usuarios.value.push(novo)
      abrirFeedback('success', `Usuário "${novo.nome}" cadastrado com sucesso!`)
    } else if (editingId.value !== null) {
      const atualizado = await usuarioService.atualizar(editingId.value, payload)
      const idx = usuarios.value.findIndex((u) => u.id === editingId.value)
      if (idx !== -1) usuarios.value[idx] = atualizado
      abrirFeedback('success', `Usuário "${atualizado.nome}" atualizado com sucesso!`)
    }
    showFormModal.value = false
  } catch (e: any) {
    abrirFeedback('error', extractErrorMessage(e, 'Erro ao salvar usuário.'))
    showFormModal.value = false
  } finally {
    formLoading.value = false
  }
}

// ────────────────────────────────
// Exclusão
// ────────────────────────────────
const deletingId = ref<number | null>(null)
const pendingDeleteId = ref<number | null>(null)
const pendingDeleteNome = ref('')

function confirmarExclusao(id: number, nome: string) {
  pendingDeleteId.value = id
  pendingDeleteNome.value = nome
  abrirFeedback('confirm', `Tem certeza que deseja excluir o usuário "${nome}"? Esta ação não pode ser desfeita.`)
}

async function executarExclusao() {
  fecharFeedback()
  if (!pendingDeleteId.value) return
  const id = pendingDeleteId.value
  try {
    deletingId.value = id
    await usuarioService.excluir(id)
    usuarios.value = usuarios.value.filter((u) => u.id !== id)
    abrirFeedback('success', `Usuário "${pendingDeleteNome.value}" excluído com sucesso!`)
  } catch (e: any) {
    abrirFeedback('error', extractErrorMessage(e, 'Erro ao excluir usuário.'))
  } finally {
    deletingId.value = null
    pendingDeleteId.value = null
  }
}

// ────────────────────────────────
// Modal de feedback (sucesso/erro/confirm)
// ────────────────────────────────
const showFeedback = ref(false)
const feedbackType = ref<'success' | 'error' | 'confirm'>('success')
const feedbackMessage = ref('')

function abrirFeedback(type: 'success' | 'error' | 'confirm', message: string) {
  feedbackType.value = type
  feedbackMessage.value = message
  showFeedback.value = true
}

function fecharFeedback() {
  showFeedback.value = false
}

// ────────────────────────────────
// Modal de Detalhes do Usuário
// ────────────────────────────────
const usuarioDetalhes = ref<UsuarioResponse | null>(null)

function abrirDetalhes(usuario: UsuarioResponse) {
  usuarioDetalhes.value = usuario
}

function fecharDetalhes() {
  usuarioDetalhes.value = null
}

function editarPeloModalDetalhes() {
  if (!usuarioDetalhes.value) return
  const usuario = usuarioDetalhes.value
  fecharDetalhes()
  abrirEdicao(usuario)
}

function excluirPeloModalDetalhes() {
  if (!usuarioDetalhes.value) return
  const { id, nome } = usuarioDetalhes.value
  fecharDetalhes()
  confirmarExclusao(id, nome)
}

// ────────────────────────────────
// Helpers de exibição
// ────────────────────────────────
const roleLabel: Record<string, string> = {
  ROLE_ADMIN: 'Administrador',
  ROLE_TECNICO: 'Técnico',
}

const roleClass: Record<string, string> = {
  ROLE_ADMIN: 'badge-orange',
  ROLE_TECNICO: 'badge-blue',
}

onMounted(carregarUsuarios)
</script>

<template>
  <div class="page-content">
    <!-- Cabeçalho da página -->
    <div class="page-header-row">
      <div>
        <h1 class="page-title">👤 Usuários</h1>
        <p class="page-subtitle">{{ usuariosFiltrados.length }} usuário(s) encontrado(s)</p>
      </div>
      <RouterLink to="/cadastros/cadastrar-usuario" class="btn btn-primary">
        + Novo Usuário
      </RouterLink>
    </div>

    <!-- Busca -->
    <div class="search-bar">
      <span class="search-icon">🔍</span>
      <input
        v-model="busca"
        type="text"
        class="form-control search-input"
        placeholder="Buscar por nome, e-mail ou perfil..."
      />
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>Carregando usuários...</p>
    </div>

    <!-- Erro -->
    <div v-else-if="loadingError" class="alert alert-danger">⚠️ {{ loadingError }}</div>

    <!-- Vazio -->
    <div v-else-if="usuariosFiltrados.length === 0" class="empty-state">
      <span class="empty-state-icon">👤</span>
      <p>{{ busca ? 'Nenhum usuário encontrado para essa busca.' : 'Nenhum usuário cadastrado ainda.' }}</p>
      <RouterLink v-if="!busca" to="/cadastros/cadastrar-usuario" class="btn btn-primary">
        Cadastrar primeiro usuário
      </RouterLink>
    </div>

    <!-- Tabela de usuários -->
    <div v-else class="table-wrapper">
      <table class="users-table">
        <thead>
          <tr>
            <th>#</th>
            <th>Nome</th>
            <th>E-mail</th>
            <th>Telefone</th>
            <th>Perfil</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="usuario in usuariosFiltrados"
            :key="usuario.id"
            class="user-row clickable"
            @click="abrirDetalhes(usuario)"
          >
            <td class="col-id">{{ usuario.id }}</td>
            <td class="col-nome">
              <div class="user-avatar-name">
                <div class="user-avatar">{{ usuario.nome.charAt(0).toUpperCase() }}</div>
                <span>{{ usuario.nome }}</span>
              </div>
            </td>
            <td class="col-email">{{ usuario.email }}</td>
            <td class="col-telefone">{{ usuario.telefone || '—' }}</td>
            <td class="col-role">
              <span :class="['badge', roleClass[usuario.role ?? ''] ?? 'badge-gray']">
                {{ roleLabel[usuario.role ?? ''] ?? usuario.role ?? '—' }}
              </span>
            </td>
            <td class="col-acoes">
              <div class="acoes-wrap">
                <button
                  class="btn btn-warning btn-sm"
                  title="Editar usuário"
                  @click.stop="abrirEdicao(usuario)"
                >
                  ✏️ Editar
                </button>
                <button
                  class="btn btn-danger btn-sm"
                  title="Excluir usuário"
                  :disabled="deletingId === usuario.id"
                  @click.stop="confirmarExclusao(usuario.id, usuario.nome)"
                >
                  {{ deletingId === usuario.id ? '⏳' : '🗑️' }} Excluir
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <!-- ══════════════════════════════
       Modal de Detalhes do Usuário
       ══════════════════════════════ -->
  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="usuarioDetalhes" class="form-overlay" @click.self="fecharDetalhes">
        <div class="detalhes-dialog">
          <!-- Header -->
          <div class="form-dialog-header">
            <div class="detalhes-header-info">
              <div class="user-avatar user-avatar-lg">{{ usuarioDetalhes.nome.charAt(0).toUpperCase() }}</div>
              <div>
                <h2 class="form-dialog-title" style="margin-bottom: 0.2rem;">{{ usuarioDetalhes.nome }}</h2>
                <span :class="['badge', roleClass[usuarioDetalhes.role ?? ''] ?? 'badge-gray']">
                  {{ roleLabel[usuarioDetalhes.role ?? ''] ?? usuarioDetalhes.role ?? '—' }}
                </span>
              </div>
            </div>
            <button class="form-dialog-close" title="Fechar" @click="fecharDetalhes">✕</button>
          </div>

          <!-- Body -->
          <div class="detalhes-dialog-body">
            <div class="modal-grid-info">
              <div class="modal-info-item">
                <span class="modal-info-label">🆔 ID</span>
                <span class="modal-info-value">#{{ usuarioDetalhes.id }}</span>
              </div>
              <div class="modal-info-item">
                <span class="modal-info-label">📧 E-mail</span>
                <span class="modal-info-value">{{ usuarioDetalhes.email }}</span>
              </div>
              <div class="modal-info-item">
                <span class="modal-info-label">📞 Telefone</span>
                <span class="modal-info-value">{{ usuarioDetalhes.telefone || '—' }}</span>
              </div>
              <div class="modal-info-item">
                <span class="modal-info-label">🔑 Perfil</span>
                <span class="modal-info-value">
                  {{ roleLabel[usuarioDetalhes.role ?? ''] ?? usuarioDetalhes.role ?? '—' }}
                </span>
              </div>
            </div>
          </div>

          <!-- Footer -->
          <div class="form-dialog-footer">
            <button
              class="btn btn-danger"
              :disabled="deletingId === usuarioDetalhes.id"
              @click="excluirPeloModalDetalhes"
            >
              🗑️ Excluir
            </button>
            <button class="btn btn-warning" @click="editarPeloModalDetalhes">
              ✏️ Editar
            </button>
            <button class="btn btn-secondary" @click="fecharDetalhes">Fechar</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>

  <!-- ══════════════════════════════
       Modal Formulário (cadastro / edição)
       ══════════════════════════════ -->
  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="showFormModal" class="form-overlay" @click.self="fecharFormModal">
        <div class="form-dialog">
          <!-- Header -->
          <div class="form-dialog-header">
            <h2 class="form-dialog-title">
              {{ formMode === 'create' ? '➕ Novo Usuário' : '✏️ Editar Usuário' }}
            </h2>
            <button class="form-dialog-close" title="Fechar" @click="fecharFormModal">✕</button>
          </div>

          <!-- Body -->
          <form class="form-dialog-body" novalidate @submit.prevent="handleSubmit">
            <!-- Nome -->
            <div class="form-group">
              <label for="uNome" class="form-label">Nome completo <span class="required">*</span></label>
              <input
                id="uNome"
                v-model="form.nome"
                type="text"
                class="form-control"
                :class="{ 'input-invalid': formErrors.nome }"
                placeholder="Ex: João da Silva"
                :disabled="formLoading"
              />
              <p v-if="formErrors.nome" class="field-error">{{ formErrors.nome }}</p>
            </div>

            <!-- E-mail -->
            <div class="form-group">
              <label for="uEmail" class="form-label">E-mail <span class="required">*</span></label>
              <input
                id="uEmail"
                v-model="form.email"
                type="email"
                class="form-control"
                :class="{ 'input-invalid': formErrors.email }"
                placeholder="usuario@email.com"
                :disabled="formLoading"
              />
              <p v-if="formErrors.email" class="field-error">{{ formErrors.email }}</p>
            </div>

            <!-- Telefone -->
            <div class="form-group">
              <label for="uTelefone" class="form-label">Telefone</label>
              <input
                id="uTelefone"
                v-model="form.telefone"
                type="tel"
                class="form-control"
                placeholder="(00) 00000-0000"
                :disabled="formLoading"
              />
            </div>

            <!-- Perfil -->
            <div class="form-group">
              <label for="uRole" class="form-label">Perfil de acesso <span class="required">*</span></label>
              <select
                id="uRole"
                v-model="form.role"
                class="form-select"
                :disabled="formLoading"
              >
                <option v-for="opt in rolesOptions" :key="opt.value" :value="opt.value">
                  {{ opt.label }}
                </option>
              </select>
            </div>

            <!-- Senha -->
            <div class="form-row-2">
              <div class="form-group">
                <label for="uSenha" class="form-label">
                  Senha {{ formMode === 'edit' ? '(deixe em branco para manter)' : '*' }}
                </label>
                <input
                  id="uSenha"
                  v-model="form.senha"
                  type="password"
                  class="form-control"
                  :class="{ 'input-invalid': formErrors.senha }"
                  placeholder="••••••••"
                  :disabled="formLoading"
                />
                <p v-if="formErrors.senha" class="field-error">{{ formErrors.senha }}</p>
              </div>
              <div class="form-group">
                <label for="uConfirmar" class="form-label">Confirmar senha</label>
                <input
                  id="uConfirmar"
                  v-model="form.confirmarSenha"
                  type="password"
                  class="form-control"
                  :class="{ 'input-invalid': formErrors.confirmarSenha }"
                  placeholder="••••••••"
                  :disabled="formLoading"
                />
                <p v-if="formErrors.confirmarSenha" class="field-error">{{ formErrors.confirmarSenha }}</p>
              </div>
            </div>

            <!-- Ações -->
            <div class="form-dialog-footer">
              <button type="button" class="btn btn-secondary" :disabled="formLoading" @click="fecharFormModal">
                Cancelar
              </button>
              <button type="submit" class="btn btn-primary" :disabled="formLoading">
                <span v-if="formLoading">⏳ Salvando...</span>
                <span v-else>{{ formMode === 'create' ? '👤 Cadastrar' : '💾 Salvar Alterações' }}</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </Transition>
  </Teleport>

  <!-- Modal de feedback (sucesso / erro / confirmação) -->
  <AppModal
    :show="showFeedback"
    :type="feedbackType"
    :message="feedbackMessage"
    confirm-label="Sim, excluir"
    cancel-label="Cancelar"
    @close="fecharFeedback"
    @confirm="executarExclusao"
  />
</template>

<style scoped>
/* ── Layout ── */
.page-content {
  padding: 3rem 2rem;
  max-width: 1100px;
  margin: 0 auto;
}

.page-header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

/* ── Elementos clicáveis ── */
.clickable {
  cursor: pointer;
}

/* ── Busca ── */
.search-bar {
  position: relative;
  margin-bottom: 1.5rem;
}

.search-icon {
  position: absolute;
  left: 0.9rem;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1rem;
  pointer-events: none;
}

.search-input {
  padding-left: 2.5rem;
}

/* ── Tabela ── */
.table-wrapper {
  overflow-x: auto;
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border);
}

.users-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}

.users-table thead tr {
  background: var(--color-surface-alt, rgba(255,255,255,0.04));
  border-bottom: 1px solid var(--color-border);
}

.users-table th {
  padding: 0.9rem 1rem;
  text-align: left;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  font-size: 0.75rem;
  letter-spacing: 0.05em;
  white-space: nowrap;
}

.users-table td {
  padding: 0.85rem 1rem;
  border-bottom: 1px solid var(--color-border);
  color: var(--text-primary);
  vertical-align: middle;
}

.user-row:last-child td {
  border-bottom: none;
}

.user-row {
  transition: background 0.15s ease;
}

.user-row:hover {
  background: rgba(238, 130, 39, 0.04);
}

/* Avatar com inicial */
.user-avatar-name {
  display: flex;
  align-items: center;
  gap: 0.65rem;
}

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: var(--gradient);
  color: #fff;
  font-weight: 700;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-avatar-lg {
  width: 48px;
  height: 48px;
  font-size: 1.2rem;
}

.col-id {
  color: var(--text-secondary);
  font-size: 0.8rem;
  width: 40px;
}

.col-email {
  color: var(--text-secondary);
}

.acoes-wrap {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

/* Badges de role */
.badge-orange {
  background: rgba(238, 130, 39, 0.15);
  color: var(--accent-3);
  border: 1px solid rgba(238, 130, 39, 0.3);
}

.badge-blue {
  background: rgba(59, 130, 246, 0.15);
  color: #60a5fa;
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.badge-gray {
  background: rgba(150,150,150,0.12);
  color: var(--text-secondary);
  border: 1px solid rgba(150,150,150,0.2);
}

/* ── Modal de formulário / detalhes (overlay compartilhado) ── */
.form-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.form-dialog {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  width: 100%;
  max-width: 560px;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.4);
  animation: dialogIn 0.22s cubic-bezier(0.34, 1.3, 0.64, 1);
  max-height: 90vh;
  overflow-y: auto;
}

@keyframes dialogIn {
  from { opacity: 0; transform: scale(0.95) translateY(10px); }
  to   { opacity: 1; transform: scale(1) translateY(0); }
}

.form-dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.4rem 1.6rem 0;
}

.form-dialog-title {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--text-primary);
}

.form-dialog-close {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-secondary);
  font-size: 1.1rem;
  padding: 4px;
  border-radius: 6px;
  transition: color 0.2s, background 0.2s;
  line-height: 1;
}

.form-dialog-close:hover {
  color: var(--text-primary);
  background: rgba(255,255,255,0.07);
}

.form-dialog-body {
  padding: 1.2rem 1.6rem 1.6rem;
  display: flex;
  flex-direction: column;
  gap: 0;
}

.form-row-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

@media (max-width: 500px) {
  .form-row-2 {
    grid-template-columns: 1fr;
  }
}

.form-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1.5rem;
  padding: 0 1.6rem 1.6rem;
}

/* ── Modal de Detalhes do Usuário ── */
.detalhes-dialog {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  width: 100%;
  max-width: 520px;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.4);
  animation: dialogIn 0.22s cubic-bezier(0.34, 1.3, 0.64, 1);
  max-height: 90vh;
  overflow-y: auto;
}

.detalhes-header-info {
  display: flex;
  align-items: center;
  gap: 0.85rem;
}

.detalhes-dialog-body {
  padding: 1.2rem 1.6rem 1.6rem;
}

.modal-grid-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem;
  background: var(--color-surface-alt, rgba(255,255,255,0.04));
  border: 1px solid var(--color-border);
  padding: 1rem;
  border-radius: var(--radius-md, 10px);
}

.modal-info-item {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.modal-info-label {
  font-size: 0.72rem;
  font-weight: 700;
  text-transform: uppercase;
  color: var(--text-secondary);
}

.modal-info-value {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--text-primary);
  word-break: break-word;
}

/* Validação inline */
.required {
  color: #f87171;
}

.input-invalid {
  border-color: rgba(239, 68, 68, 0.6) !important;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.12) !important;
}

.field-error {
  margin-top: 0.3rem;
  font-size: 0.78rem;
  color: #f87171;
  font-weight: 500;
  animation: fadeSlideIn 0.2s ease;
}

@keyframes fadeSlideIn {
  from { opacity: 0; transform: translateY(-4px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* Transição do modal */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.2s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

/* Responsive */
@media (max-width: 800px) {
  .page-content {
    padding: 1.5rem 1rem;
  }

  .users-table th:nth-child(4),
  .users-table td:nth-child(4) {
    display: none;
  }
}

@media (max-width: 600px) {
  .users-table th:nth-child(1),
  .users-table td:nth-child(1) {
    display: none;
  }
}
</style>