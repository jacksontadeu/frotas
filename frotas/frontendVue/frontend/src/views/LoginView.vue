<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'

const router = useRouter()
const { login, errorMsg, isLoading, isTecnico } = useAuth()

const email = ref('')
const senha = ref('')
const showPassword = ref(false)

async function handleSubmit() {
  if (!email.value || !senha.value) return
  
  const success = await login({
    email: email.value,
    senha: senha.value,
  })

  if (success) {
    if (isTecnico.value) {
      router.push('/atendimento')
    } else {
      router.push('/')
    }
  }
}
</script>

<template>
  <div class="login-wrapper">
    <div class="login-overlay"></div>
    
    <div class="login-card glass-card">
      <div class="login-header">
        <span class="logo-emoji">🚛</span>
        <h1 class="login-title">NOVO TRIUNFO</h1>
        <p class="login-subtitle">Controle de Frotas — Acesso Administrativo</p>
      </div>

      <form @submit.prevent="handleSubmit" class="login-form">
        <div class="form-group">
          <label for="email" class="form-label">E-mail</label>
          <div class="input-icon-wrapper">
            <span class="input-icon">✉️</span>
            <input
              id="email"
              v-model="email"
              type="email"
              class="form-control with-icon"
              placeholder="seu-email@novotriunfo.com.br"
              required
              :disabled="isLoading"
            />
          </div>
        </div>

        <div class="form-group">
          <label for="senha" class="form-label">Senha</label>
          <div class="input-icon-wrapper">
            <span class="input-icon">🔒</span>
            <input
              id="senha"
              v-model="senha"
              :type="showPassword ? 'text' : 'password'"
              class="form-control with-icon"
              placeholder="Digite sua senha"
              required
              :disabled="isLoading"
            />
            <button
              type="button"
              class="password-toggle"
              @click="showPassword = !showPassword"
              aria-label="Alternar exibição de senha"
            >
              {{ showPassword ? '👁️' : '🙈' }}
            </button>
          </div>
        </div>

        <div v-if="errorMsg" class="alert alert-danger" role="alert">
          ⚠️ {{ errorMsg }}
        </div>

        <button
          type="submit"
          class="btn btn-primary btn-w-full btn-login"
          :disabled="isLoading || !email || !senha"
        >
          <template v-if="isLoading">
            <span class="spinner-small"></span>
            Autenticando...
          </template>
          <template v-else>
            Entrar ➔
          </template>
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.login-wrapper {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1001;
  padding: 1.5rem;
  background-image: url('../assets/logo.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  transition: background-image var(--transition-slow);
}

.login-overlay {
  position: absolute;
  inset: 0;
  z-index: 1;
  backdrop-filter: blur(8px);
}

/* Light / Dark Mode adaptive overlays */
body.dark-mode .login-overlay {
  background: radial-gradient(circle, rgba(10, 10, 15, 0.6) 0%, rgba(10, 10, 15, 0.85) 100%);
}

body.light-mode .login-overlay {
  background: radial-gradient(circle, rgba(240, 242, 248, 0.5) 0%, rgba(240, 242, 248, 0.8) 100%);
}

.login-card {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 440px;
  border-radius: var(--radius-xl);
  padding: 3rem 2.5rem;
  box-shadow: var(--shadow-lg), var(--shadow-glow);
  animation: slideUp 0.5s cubic-bezier(0.16, 1, 0.3, 1);
  border: 1px solid var(--color-border);
}

@media (max-width: 480px) {
  .login-card {
    padding: 2rem 1.5rem;
  }
}

.login-header {
  text-align: center;
  margin-bottom: 2rem;
}

.logo-emoji {
  font-size: 3rem;
  display: inline-block;
  margin-bottom: 0.5rem;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

.login-title {
  font-size: 1.75rem;
  font-weight: 800;
  letter-spacing: -0.5px;
  background: var(--gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-subtitle {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-top: 0.25rem;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.input-icon-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 1rem;
  color: var(--text-muted);
  font-size: 1.1rem;
  pointer-events: none;
}

.form-control.with-icon {
  padding-left: 2.75rem;
}

.password-toggle {
  position: absolute;
  right: 1rem;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.25rem;
  font-size: 1.1rem;
  color: var(--text-muted);
  transition: color var(--transition);
  outline: none;
}

.password-toggle:hover {
  color: var(--text-primary);
}

.btn-login {
  height: 48px;
  margin-top: 0.5rem;
  font-size: 1rem;
}

.spinner-small {
  width: 1.2rem;
  height: 1.2rem;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  display: inline-block;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
