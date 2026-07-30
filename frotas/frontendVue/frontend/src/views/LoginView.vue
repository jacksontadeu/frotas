<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'

const router = useRouter()
const { login, errorMsg, isLoading } = useAuth()

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
    router.push('/')
  }
}
</script>

<template>
  <div class="login-page">

    <!-- ─── TOP BAR (estilo Santander) ─── -->
    <header class="login-topbar">
      <div class="topbar-inner">
        <!-- Marca -->
        <div class="topbar-brand">
          <img src="../assets/logo.png" alt="Novo Triunfo" class="topbar-logo" />
          <span class="topbar-name">Novo Triunfo</span>
          <span class="topbar-divider">|</span>
          <span class="topbar-sub">Controle de Frotas</span>
        </div>

        <!-- Formulário inline -->
        <form class="topbar-form" @submit.prevent="handleSubmit" novalidate>
          <div class="topbar-field">
            <label for="tb-email" class="topbar-label">E-mail</label>
            <input
              id="tb-email"
              v-model="email"
              type="email"
              class="topbar-input"
              placeholder="seu@email.com"
              autocomplete="username"
              required
              :disabled="isLoading"
            />
          </div>

          <div class="topbar-field">
            <label for="tb-senha" class="topbar-label">Senha</label>
            <div class="topbar-pass-wrap">
              <input
                id="tb-senha"
                v-model="senha"
                :type="showPassword ? 'text' : 'password'"
                class="topbar-input"
                placeholder="••••••••"
                autocomplete="current-password"
                required
                :disabled="isLoading"
              />
              <button
                type="button"
                class="topbar-eye"
                @click="showPassword = !showPassword"
                :aria-label="showPassword ? 'Ocultar senha' : 'Mostrar senha'"
              >
                {{ showPassword ? '👁️' : '🙈' }}
              </button>
            </div>
          </div>

          <button
            type="submit"
            id="btn-logar"
            class="topbar-btn"
            :disabled="isLoading || !email || !senha"
          >
            <span v-if="isLoading" class="topbar-spinner"></span>
            <span v-else>Logar</span>
          </button>
        </form>
      </div>

      <!-- Erro inline abaixo do topbar -->
      <div v-if="errorMsg" class="topbar-error">
        ⚠️ {{ errorMsg }}
      </div>
    </header>

    <!-- ─── HERO / CORPO ─── -->
    <main class="login-hero">
      <!-- fundo com logo grande -->
      <div class="hero-bg">
        <img src="../assets/logo.png" alt="" class="hero-watermark" aria-hidden="true" />
        <div class="hero-overlay"></div>
      </div>

      <div class="hero-content">
        <div class="hero-badge">Sistema de Gestão</div>
        <h1 class="hero-title">Controle Inteligente<br />de Frotas</h1>
        <p class="hero-desc">
          Gerencie toda a sua frota de veículos em um só lugar — manutenções,
          bases, atendimentos e muito mais com agilidade e precisão.
        </p>

        <div class="hero-features">
          <div class="feature-card">
            <span class="feature-icon">🚛</span>
            <div>
              <strong>Gestão de Veículos</strong>
              <p>Cadastro e controle completo de toda a frota com histórico detalhado.</p>
            </div>
          </div>
          <div class="feature-card">
            <span class="feature-icon">🔧</span>
            <div>
              <strong>Manutenção Preventiva</strong>
              <p>Acompanhe preventivas e corretivas, evitando paradas não planejadas.</p>
            </div>
          </div>
          <div class="feature-card">
            <span class="feature-icon">📍</span>
            <div>
              <strong>Controle por Base</strong>
              <p>Organize a frota por localidade e base operacional com facilidade.</p>
            </div>
          </div>
          <div class="feature-card">
            <span class="feature-icon">📊</span>
            <div>
              <strong>Relatórios em Tempo Real</strong>
              <p>Visualize dados atualizados sobre status, quilometragem e histórico.</p>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- ─── RODAPÉ ─── -->
    <footer class="login-footer">
      <span>© {{ new Date().getFullYear() }} Novo Triunfo Transportes — Todos os direitos reservados.</span>
    </footer>
  </div>
</template>

<style scoped>
/* ═══════════════════════════════════════
   PÁGINA COMPLETA
═══════════════════════════════════════ */
.login-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  font-family: 'Inter', 'Segoe UI', sans-serif;
  background: #0a0a0f;
}

/* ═══════════════════════════════════════
   TOP BAR — Estilo Santander
═══════════════════════════════════════ */
.login-topbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #1a1a2e;
  border-bottom: 2px solid var(--color-primary, #ee8227);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.5);
}

.topbar-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0.75rem 2rem;
  display: flex;
  align-items: center;
  gap: 2rem;
  flex-wrap: wrap;
}

/* Marca */
.topbar-brand {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  flex-shrink: 0;
}

.topbar-logo {
  height: 38px;
  width: 38px;
  object-fit: contain;
  border-radius: 8px;
  background: rgba(255,255,255,0.08);
  padding: 3px;
}

.topbar-name {
  font-size: 1.1rem;
  font-weight: 800;
  color: #fff;
  letter-spacing: -0.3px;
}

.topbar-divider {
  color: rgba(255,255,255,0.25);
  font-size: 1.1rem;
}

.topbar-sub {
  font-size: 0.8rem;
  color: rgba(255,255,255,0.5);
  font-weight: 500;
}

/* Formulário inline */
.topbar-form {
  display: flex;
  align-items: flex-end;
  gap: 0.75rem;
  margin-left: auto;
  flex-wrap: wrap;
}

.topbar-field {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.topbar-label {
  font-size: 0.68rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: rgba(255,255,255,0.45);
}

.topbar-input {
  height: 36px;
  padding: 0 0.85rem;
  border-radius: 8px;
  border: 1px solid rgba(255,255,255,0.12);
  background: rgba(255,255,255,0.07);
  color: #fff;
  font-size: 0.875rem;
  width: 200px;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.topbar-input::placeholder { color: rgba(255,255,255,0.3); }

.topbar-input:focus {
  border-color: var(--color-primary, #ee8227);
  box-shadow: 0 0 0 3px rgba(238,130,39,0.2);
}

.topbar-input:disabled { opacity: 0.5; cursor: not-allowed; }

/* Campo de senha */
.topbar-pass-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.topbar-pass-wrap .topbar-input {
  padding-right: 2.2rem;
}

.topbar-eye {
  position: absolute;
  right: 0.5rem;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 0.9rem;
  color: rgba(255,255,255,0.4);
  padding: 0;
  line-height: 1;
  transition: color 0.15s;
}

.topbar-eye:hover { color: rgba(255,255,255,0.8); }

/* Botão Logar */
.topbar-btn {
  height: 36px;
  padding: 0 1.4rem;
  border-radius: 8px;
  border: none;
  background: var(--color-primary, #ee8227);
  color: #fff;
  font-size: 0.875rem;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.4rem;
  transition: background 0.2s, transform 0.15s, box-shadow 0.2s;
  white-space: nowrap;
  flex-shrink: 0;
}

.topbar-btn:hover:not(:disabled) {
  background: #d4741e;
  box-shadow: 0 4px 16px rgba(238,130,39,0.4);
  transform: translateY(-1px);
}

.topbar-btn:disabled { opacity: 0.5; cursor: not-allowed; transform: none; }

.topbar-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: topSpin 0.6s linear infinite;
  display: inline-block;
}

@keyframes topSpin { to { transform: rotate(360deg); } }

/* Mensagem de erro */
.topbar-error {
  background: rgba(239, 68, 68, 0.12);
  border-top: 1px solid rgba(239, 68, 68, 0.3);
  color: #f87171;
  font-size: 0.8rem;
  font-weight: 500;
  padding: 0.45rem 2rem;
  text-align: center;
  animation: fadeSlideIn 0.2s ease;
}

@keyframes fadeSlideIn {
  from { opacity: 0; transform: translateY(-4px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* ═══════════════════════════════════════
   HERO
═══════════════════════════════════════ */
.login-hero {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 5rem 2rem 4rem;
  overflow: hidden;
}

/* Logo de fundo gigante */
.hero-bg {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

.hero-watermark {
  width: min(680px, 80vw);
  opacity: 0.07;
  filter: blur(1px) saturate(0);
  animation: heroFloat 8s ease-in-out infinite;
}

@keyframes heroFloat {
  0%, 100% { transform: translateY(0) scale(1); }
  50%       { transform: translateY(-12px) scale(1.02); }
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: radial-gradient(
    ellipse at center,
    rgba(10,10,15,0.3) 0%,
    rgba(10,10,15,0.9) 100%
  );
}

/* Conteúdo do hero */
.hero-content {
  position: relative;
  z-index: 1;
  max-width: 900px;
  text-align: center;
  animation: heroFadeIn 0.8s cubic-bezier(0.16,1,0.3,1);
}

@keyframes heroFadeIn {
  from { opacity: 0; transform: translateY(30px); }
  to   { opacity: 1; transform: translateY(0); }
}

.hero-badge {
  display: inline-block;
  padding: 0.3rem 1rem;
  background: rgba(238,130,39,0.15);
  border: 1px solid rgba(238,130,39,0.35);
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 600;
  letter-spacing: 0.08em;
  color: #ee8227;
  text-transform: uppercase;
  margin-bottom: 1.5rem;
}

.hero-title {
  font-size: clamp(2.2rem, 6vw, 4rem);
  font-weight: 900;
  line-height: 1.1;
  letter-spacing: -1px;
  color: #fff;
  margin-bottom: 1.25rem;
  background: linear-gradient(135deg, #fff 30%, rgba(255,255,255,0.65) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-desc {
  font-size: 1.1rem;
  color: rgba(255,255,255,0.6);
  max-width: 560px;
  margin: 0 auto 3rem;
  line-height: 1.7;
}

/* Cards de features */
.hero-features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  text-align: left;
}

.feature-card {
  display: flex;
  align-items: flex-start;
  gap: 0.85rem;
  padding: 1.25rem;
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 14px;
  transition: background 0.2s, border-color 0.2s, transform 0.2s;
  cursor: default;
}

.feature-card:hover {
  background: rgba(238,130,39,0.08);
  border-color: rgba(238,130,39,0.3);
  transform: translateY(-3px);
}

.feature-icon {
  font-size: 1.6rem;
  flex-shrink: 0;
  margin-top: 0.1rem;
}

.feature-card strong {
  display: block;
  font-size: 0.9rem;
  font-weight: 700;
  color: #fff;
  margin-bottom: 0.3rem;
}

.feature-card p {
  font-size: 0.8rem;
  color: rgba(255,255,255,0.5);
  line-height: 1.5;
  margin: 0;
}

/* ═══════════════════════════════════════
   RODAPÉ
═══════════════════════════════════════ */
.login-footer {
  text-align: center;
  padding: 1rem 2rem;
  font-size: 0.75rem;
  color: rgba(255,255,255,0.25);
  background: rgba(0,0,0,0.3);
  border-top: 1px solid rgba(255,255,255,0.06);
}

/* ═══════════════════════════════════════
   RESPONSIVO
═══════════════════════════════════════ */
@media (max-width: 768px) {
  .topbar-inner {
    padding: 0.75rem 1rem;
    gap: 0.75rem;
  }

  .topbar-sub { display: none; }

  .topbar-input { width: 140px; }

  .topbar-btn { padding: 0 1rem; }

  .login-hero { padding: 3rem 1.25rem 2.5rem; }
}

@media (max-width: 480px) {
  .topbar-form {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
    gap: 0.5rem;
  }

  .topbar-input { width: 100%; }

  .topbar-btn { width: 100%; justify-content: center; height: 40px; }
}
</style>
