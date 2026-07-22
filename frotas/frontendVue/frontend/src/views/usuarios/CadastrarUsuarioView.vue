<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { RouterLink } from 'vue-router'
import { usuarioService } from '../../services/usuarioService'
import AppModal from '../../components/AppModal.vue'
import { extractErrorMessage } from '../../composables/useErrorMessage'

const router = useRouter()

// ────────────────────────────────
// Formulário
// ────────────────────────────────
const form = ref({
  nome: '',
  email: '',
  telefone: '',
  senha: '',
  confirmarSenha: '',
  role: 'ADMIN',
})

const loading = ref(false)

// ────────────────────────────────
// Validação inline
// ────────────────────────────────
const errors = ref({
  nome: '',
  email: '',
  senha: '',
  confirmarSenha: '',
})

function validarCampos(): boolean {
  let ok = true
  errors.value.nome = ''
  errors.value.email = ''
  errors.value.senha = ''
  errors.value.confirmarSenha = ''

  if (!form.value.nome.trim()) {
    errors.value.nome = 'Nome é obrigatório.'
    ok = false
  }
  if (!form.value.email.trim() || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) {
    errors.value.email = 'Informe um e-mail válido.'
    ok = false
  }
  if (!form.value.senha) {
    errors.value.senha = 'Senha é obrigatória.'
    ok = false
  } else if (form.value.senha.length < 6) {
    errors.value.senha = 'A senha deve ter pelo menos 6 caracteres.'
    ok = false
  }
  if (form.value.senha !== form.value.confirmarSenha) {
    errors.value.confirmarSenha = 'As senhas não coincidem.'
    ok = false
  }
  return ok
}

// ────────────────────────────────
// Modal de resultado
// ────────────────────────────────
const showModal = ref(false)
const modalType = ref<'success' | 'error'>('success')
const modalMessage = ref('')

async function handleSubmit() {
  if (!validarCampos()) return

  loading.value = true
  try {
    await usuarioService.cadastrar({
      nome: form.value.nome,
      email: form.value.email,
      telefone: form.value.telefone,
      senha: form.value.senha,
      role: form.value.role,
    })
    modalType.value = 'success'
    modalMessage.value = 'Usuário cadastrado com sucesso!'
    showModal.value = true
  } catch (e: any) {
    modalType.value = 'error'
    modalMessage.value = extractErrorMessage(e, 'Erro ao cadastrar usuário.')
    showModal.value = true
  } finally {
    loading.value = false
  }
}

function fecharModal() {
  showModal.value = false
  if (modalType.value === 'success') {
    router.push('/listagem/listar-usuarios')
  }
}
</script>

<template>
  <div class="page-content">
    <div class="page-header">
      <h1 class="page-title">Cadastrar Usuário</h1>
      <p class="page-subtitle">Adicione um novo usuário ao sistema</p>
    </div>

    <div class="form-card" style="max-width: 640px;">
      <form novalidate @submit.prevent="handleSubmit">

        <!-- Nome -->
        <div class="form-group">
          <label for="uNome" class="form-label">Nome completo <span class="required">*</span></label>
          <input
            id="uNome"
            v-model="form.nome"
            type="text"
            class="form-control"
            :class="{ 'input-invalid': errors.nome }"
            placeholder="Ex: João da Silva"
            required
            :disabled="loading"
          />
          <p v-if="errors.nome" class="field-hint hint-err">{{ errors.nome }}</p>
        </div>

        <!-- E-mail -->
        <div class="form-group">
          <label for="uEmail" class="form-label">E-mail <span class="required">*</span></label>
          <input
            id="uEmail"
            v-model="form.email"
            type="email"
            class="form-control"
            :class="{ 'input-invalid': errors.email }"
            placeholder="usuario@email.com"
            required
            :disabled="loading"
          />
          <p v-if="errors.email" class="field-hint hint-err">{{ errors.email }}</p>
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
            :disabled="loading"
          />
        </div>

        <!-- Perfil de acesso -->
        <div class="form-group">
          <label for="uRole" class="form-label">Perfil de acesso <span class="required">*</span></label>
          <select
            id="uRole"
            v-model="form.role"
            class="form-select"
            :disabled="loading"
          >
            <option value="ADMIN">👑 Administrador</option>
            <option value="TECNICO">🔧 Técnico</option>
          </select>
          <p class="field-hint hint-info">
            <span v-if="form.role === 'ADMIN'">Administrador tem acesso total ao sistema.</span>
            <span v-else>Técnico tem acesso apenas às telas de atendimento de manutenções.</span>
          </p>
        </div>

        <!-- Senha / Confirmar Senha -->
        <div class="row">
          <div class="form-group">
            <label for="uSenha" class="form-label">Senha <span class="required">*</span></label>
            <div class="field-validate-wrap">
              <input
                id="uSenha"
                v-model="form.senha"
                type="password"
                class="form-control"
                :class="{ 'input-invalid': errors.senha }"
                placeholder="Mínimo 6 caracteres"
                required
                :disabled="loading"
              />
              <span
                v-if="form.senha && !errors.senha"
                class="field-icon field-ok"
              >✅</span>
              <span
                v-else-if="errors.senha"
                class="field-icon field-err"
              >❌</span>
            </div>
            <p v-if="errors.senha" class="field-hint hint-err">{{ errors.senha }}</p>
          </div>

          <div class="form-group">
            <label for="uConfirmar" class="form-label">Confirmar senha <span class="required">*</span></label>
            <div class="field-validate-wrap">
              <input
                id="uConfirmar"
                v-model="form.confirmarSenha"
                type="password"
                class="form-control"
                :class="{
                  'input-valid':   form.confirmarSenha && form.senha === form.confirmarSenha,
                  'input-invalid': errors.confirmarSenha,
                }"
                placeholder="Repita a senha"
                required
                :disabled="loading"
              />
              <span
                v-if="form.confirmarSenha && form.senha === form.confirmarSenha"
                class="field-icon field-ok"
              >✅</span>
              <span
                v-else-if="errors.confirmarSenha"
                class="field-icon field-err"
              >❌</span>
            </div>
            <p v-if="errors.confirmarSenha" class="field-hint hint-err">{{ errors.confirmarSenha }}</p>
          </div>
        </div>

        <!-- Ações -->
        <div class="form-actions">
          <RouterLink to="/listagem/listar-usuarios" class="btn btn-secondary">
            Cancelar
          </RouterLink>
          <button
            type="submit"
            class="btn btn-primary"
            :disabled="loading"
          >
            <span v-if="loading">⏳ Salvando...</span>
            <span v-else>👤 Cadastrar Usuário</span>
          </button>
        </div>

      </form>
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
/* Wrapper de campo com ícone flutuante */
.field-validate-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.field-validate-wrap .form-control {
  padding-right: 2.5rem;
}

.field-icon {
  position: absolute;
  right: 0.85rem;
  font-size: 1rem;
  pointer-events: none;
  line-height: 1;
}

.field-ok  { color: #22c55e; }
.field-err { color: #ef4444; }

/* Bordas de validação */
.input-valid {
  border-color: rgba(34, 197, 94, 0.55) !important;
  box-shadow: 0 0 0 3px rgba(34, 197, 94, 0.12) !important;
}

.input-invalid {
  border-color: rgba(239, 68, 68, 0.6) !important;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.12) !important;
}

/* Mensagens abaixo do campo */
.field-hint {
  margin-top: 0.35rem;
  font-size: 0.78rem;
  font-weight: 500;
  animation: fadeSlideIn 0.2s ease;
}

.hint-err  { color: #f87171; }
.hint-info { color: var(--text-secondary); font-weight: 400; }

/* Campo obrigatório */
.required {
  color: #f87171;
}

@keyframes fadeSlideIn {
  from { opacity: 0; transform: translateY(-4px); }
  to   { opacity: 1; transform: translateY(0); }
}
</style>
