<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { baseService } from '../../services/baseService'
import { usuarioService } from '../../services/usuarioService'
import type { BaseDTORequest, UsuarioResponse } from '../../types'
import AppModal from '../../components/AppModal.vue'
import { extractErrorMessage } from '../../composables/useErrorMessage'

const router = useRouter()

const form = ref<BaseDTORequest>({
  nome: '',
  emailBase: '',
  telefoneBase: '',
  responsavelId: null,
  usuarioId: null,
})

const loading = ref(false)

// Busca de Responsável (Usuário)
const usuarios = ref<UsuarioResponse[]>([])
const loadingUsuarios = ref(false)
const erroUsuarios = ref<string | null>(null)
const showSearchModal = ref(false)
const buscaUsuarioQuery = ref('')
const usuarioSelecionado = ref<UsuarioResponse | null>(null)

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
    erroUsuarios.value = extractErrorMessage(err, 'Erro ao conectar ao servidor para buscar usuários.')
  } finally {
    loadingUsuarios.value = false
  }
}

async function abrirBuscaUsuario() {
  showSearchModal.value = true
  if (usuarios.value.length === 0 && !loadingUsuarios.value) {
    await carregarUsuarios()
  }
}

function selecionarUsuario(u: UsuarioResponse) {
  usuarioSelecionado.value = u
  form.value.responsavelId = u.id
  form.value.usuarioId = u.id
  // Preenche automaticamente com os dados do responsável selecionado no banco
  form.value.emailBase = u.email
  form.value.telefoneBase = u.telefone || ''
  showSearchModal.value = false
}

function onSelectUsuarioChange(event: Event) {
  const target = event.target as HTMLSelectElement
  const idStr = target.value
  if (!idStr) {
    removerResponsavel()
    return
  }
  const id = Number(idStr)
  const user = usuarios.value.find((u) => u.id === id)
  if (user) {
    selecionarUsuario(user)
  }
}

function removerResponsavel() {
  usuarioSelecionado.value = null
  form.value.responsavelId = null
  form.value.usuarioId = null
  form.value.emailBase = ''
  form.value.telefoneBase = ''
}

// Modal de Resultado
const showModal = ref(false)
const modalType = ref<'success' | 'error'>('success')
const modalMessage = ref('')

async function handleSubmit() {
  loading.value = true
  try {
    await baseService.cadastrar(form.value)
    modalType.value = 'success'
    modalMessage.value = 'Base cadastrada com sucesso!'
    showModal.value = true
  } catch (e: any) {
    modalType.value = 'error'
    modalMessage.value = extractErrorMessage(e, 'Erro ao cadastrar base. Verifique se o servidor está rodando.')
    showModal.value = true
  } finally {
    loading.value = false
  }
}

function fecharModal() {
  showModal.value = false
  if (modalType.value === 'success') {
    router.push('/listagem/listar-bases')
  }
}

onMounted(() => {
  carregarUsuarios()
})
</script>

<template>
  <div class="page-content">
    <div class="page-header">
      <h1 class="page-title">Cadastrar Base</h1>
      <p class="page-subtitle">Registre uma nova base operacional da frota</p>
    </div>

    <div class="form-card">
      <form @submit.prevent="handleSubmit" novalidate>
        <div class="form-group">
          <label for="nomeBase" class="form-label">Nome da Base</label>
          <input
            id="nomeBase"
            v-model="form.nome"
            type="text"
            class="form-control"
            placeholder="Digite o nome da base"
            required
            :disabled="loading"
          />
        </div>

        <!-- Seção Responsável da Base -->
        <div class="form-group">
          <div class="responsavel-header">
            <label for="selectResponsavel" class="form-label">Responsável pela Base</label>
            <div class="responsavel-actions">
              <button
                type="button"
                class="btn btn-secondary btn-sm"
                @click="carregarUsuarios"
                :disabled="loading || loadingUsuarios"
                title="Recarregar lista do banco"
              >
                🔄 {{ loadingUsuarios ? 'Buscando...' : 'Atualizar' }}
              </button>
              <button
                type="button"
                class="btn btn-secondary btn-sm"
                @click="abrirBuscaUsuario"
                :disabled="loading"
              >
                🔍 {{ usuarioSelecionado ? 'Buscar / Alterar' : 'Procurar Usuário' }}
              </button>
            </div>
          </div>

          <!-- Seleção direta via Dropdown -->
          <div class="select-user-wrapper">
            <select
              id="selectResponsavel"
              class="form-control"
              :value="form.usuarioId || ''"
              @change="onSelectUsuarioChange"
              :disabled="loading || loadingUsuarios"
            >
              <option value="">
                {{
                  loadingUsuarios
                    ? '⏳ Carregando usuários do banco...'
                    : usuarios.length > 0
                    ? '-- Selecione um Usuário Responsável --'
                    : '-- Nenhum usuário encontrado no banco --'
                }}
              </option>
              <option v-for="u in usuarios" :key="u.id" :value="u.id">
                {{ u.nome }} ({{ u.email }})
              </option>
            </select>
          </div>

          <div v-if="erroUsuarios && usuarios.length === 0" class="info-alert info-alert-warning">
            ⚠️ {{ erroUsuarios }}
          </div>

          <!-- Card do Usuário Selecionado -->
          <div v-if="usuarioSelecionado" class="usuario-selecionado-card">
            <div class="usuario-info-main">
              <span class="usuario-icon">👤</span>
              <div class="usuario-detalhes">
                <h4 class="usuario-nome">{{ usuarioSelecionado.nome }}</h4>
                <p class="usuario-contato">
                  <span>📧 <strong>Email:</strong> {{ usuarioSelecionado.email }}</span>
                  <span class="dot-separator">•</span>
                  <span>📞 <strong>Telefone:</strong> {{ usuarioSelecionado.telefone || 'Não informado' }}</span>
                </p>
              </div>
            </div>
            <button
              type="button"
              class="btn-remove-user"
              title="Remover Responsável"
              @click="removerResponsavel"
            >
              ✖
            </button>
          </div>
          <p v-else class="text-muted help-text">
            Selecione um usuário acima para autopreencher o email e telefone do responsável.
          </p>
        </div>

        <!-- Campos Autopreenchidos -->
        <div class="form-group">
          <label for="emailBase" class="form-label flex-between">
            <span>Email do Responsável</span>
            <span v-if="usuarioSelecionado" class="auto-badge">⚡ Preenchido automaticamente</span>
          </label>
          <input
            id="emailBase"
            v-model="form.emailBase"
            type="email"
            class="form-control"
            placeholder="Preenchido automaticamente ao selecionar responsável"
            :readonly="!!usuarioSelecionado"
            :disabled="loading"
          />
        </div>

        <div class="form-group">
          <label for="telefoneBase" class="form-label flex-between">
            <span>Telefone do Responsável</span>
            <span v-if="usuarioSelecionado" class="auto-badge">⚡ Preenchido automaticamente</span>
          </label>
          <input
            id="telefoneBase"
            v-model="form.telefoneBase"
            type="tel"
            class="form-control"
            placeholder="Preenchido automaticamente ao selecionar responsável"
            :readonly="!!usuarioSelecionado"
            :disabled="loading"
          />
        </div>

        <div class="form-actions">
          <RouterLink to="/listagem/listar-bases" class="btn btn-secondary">
            Cancelar
          </RouterLink>
          <button
            type="submit"
            class="btn btn-primary"
            :disabled="loading || !form.nome || (!form.usuarioId && !form.emailBase)"
          >
            <span v-if="loading">⏳ Salvando...</span>
            <span v-else>💾 Cadastrar Base</span>
          </button>
        </div>
      </form>
    </div>
  </div>

  <!-- Modal de Pesquisa de Usuário -->
  <div v-if="showSearchModal" class="modal-backdrop" @click.self="showSearchModal = false">
    <div class="modal-card">
      <div class="modal-header">
        <h3>🔍 Selecionar Responsável</h3>
        <button type="button" class="btn-close" @click="showSearchModal = false">✖</button>
      </div>

      <div class="modal-body">
        <div class="search-input-wrap">
          <input
            v-model="buscaUsuarioQuery"
            type="text"
            class="form-control"
            placeholder="Buscar por nome, email ou telefone..."
            autofocus
          />
        </div>

        <div v-if="loadingUsuarios" class="loading-state">
          <span class="spinner"></span> Carregando usuários do banco de dados...
        </div>

        <div v-else-if="usuariosFiltrados.length === 0" class="empty-state">
          <span v-if="erroUsuarios">⚠️ {{ erroUsuarios }}</span>
          <span v-else>Nenhum usuário encontrado.</span>
        </div>

        <ul v-else class="usuarios-list">
          <li
            v-for="u in usuariosFiltrados"
            :key="u.id"
            class="usuario-item"
            @click="selecionarUsuario(u)"
          >
            <span class="user-avatar">👤</span>
            <div class="user-details">
              <span class="user-name">{{ u.nome }}</span>
              <span class="user-meta">
                📧 {{ u.email }}
                <template v-if="u.telefone"> &nbsp;•&nbsp; 📞 {{ u.telefone }}</template>
              </span>
            </div>
            <span class="btn-select">Selecionar</span>
          </li>
        </ul>
      </div>

      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" @click="showSearchModal = false">
          Fechar
        </button>
      </div>
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
.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.auto-badge {
  font-size: 0.75rem;
  background: rgba(238, 130, 39, 0.15);
  color: var(--accent-1, #ee8227);
  padding: 0.15rem 0.5rem;
  border-radius: 4px;
  font-weight: 600;
}

.select-user-wrapper {
  margin-bottom: 0.5rem;
}

.responsavel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.responsavel-actions {
  display: flex;
  gap: 0.5rem;
}

.help-text {
  font-size: 0.85rem;
  margin-top: 0.25rem;
}

.info-alert {
  font-size: 0.85rem;
  padding: 0.5rem 0.75rem;
  border-radius: var(--radius-sm, 4px);
  margin-top: 0.35rem;
}

.info-alert-warning {
  background: rgba(255, 193, 7, 0.12);
  border: 1px solid rgba(255, 193, 7, 0.3);
  color: var(--text-primary);
}

.usuario-selecionado-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(238, 130, 39, 0.08);
  border: 1px solid rgba(238, 130, 39, 0.25);
  border-radius: var(--radius-md);
  padding: 0.85rem 1.15rem;
  margin-top: 0.35rem;
}

.usuario-info-main {
  display: flex;
  align-items: center;
  gap: 0.85rem;
}

.usuario-icon {
  font-size: 1.75rem;
}

.usuario-nome {
  margin: 0 0 0.2rem 0;
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
}

.usuario-contato {
  margin: 0;
  font-size: 0.825rem;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.dot-separator {
  color: var(--text-muted);
}

.btn-remove-user {
  background: transparent;
  border: none;
  color: var(--text-muted);
  font-size: 1.1rem;
  cursor: pointer;
  padding: 0.25rem;
  transition: color 0.2s;
}

.btn-remove-user:hover {
  color: var(--color-danger);
}

/* Modal de Busca */
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.65);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  width: 100%;
  max-width: 520px;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  box-shadow: var(--shadow-xl);
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
  font-size: 1.1rem;
  font-weight: 700;
}

.btn-close {
  background: transparent;
  border: none;
  color: var(--text-muted);
  font-size: 1.1rem;
  cursor: pointer;
}

.modal-body {
  padding: 1.25rem;
  overflow-y: auto;
  flex: 1;
}

.search-input-wrap {
  margin-bottom: 1rem;
}

.loading-state, .empty-state {
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

.user-avatar {
  font-size: 1.5rem;
}

.user-details {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.user-name {
  font-weight: 700;
  font-size: 0.95rem;
  color: var(--text-primary);
}

.user-meta {
  font-size: 0.78rem;
  color: var(--text-secondary);
}

.btn-select {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--accent-1);
}

.modal-footer {
  padding: 0.85rem 1.25rem;
  border-top: 1px solid var(--color-border);
  display: flex;
  justify-content: flex-end;
}
</style>