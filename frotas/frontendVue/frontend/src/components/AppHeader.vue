<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useTheme } from '../composables/useTheme'
import { useAuth } from '../composables/useAuth'

const { theme, toggleTheme } = useTheme()
const { isAuthenticated, isAdmin, isTecnico, logout } = useAuth()
const router = useRouter()

const menuOpen = ref(false)
const openDropdown = ref<string | null>(null)
const navRef = ref<HTMLElement | null>(null)

const themeIcon = computed(() => theme.value === 'dark-mode' ? '☀️' : '🌙')
const themeLabel = computed(() =>
  theme.value === 'dark-mode' ? 'Ativar modo claro' : 'Ativar modo escuro'
)

interface NavItem {
  to: string
  label: string
}

interface NavSection {
  key: string
  label: string
  items: NavItem[]
}

const navSections: NavSection[] = [
  {
    key: 'base',
    label: 'Base',
    items: [
      { to: '/cadastros/cadastrar-base', label: 'Cadastrar Base' },
      { to: '/listagem/listar-bases', label: 'Listar Bases' },
      { to: '/listagem/listar-bases', label: 'Alterar Base' },
      { to: '/listagem/listar-bases', label: 'Excluir Base' },
    ],
  },
  {
    key: 'veiculos',
    label: 'Veículos',
    items: [
      { to: '/cadastros/cadastrar-veiculo', label: 'Cadastrar Veículo' },
      { to: '/listagem/listar-veiculos', label: 'Listar Veículos' },
      { to: '/veiculos/trocar-base', label: 'Trocar Veículo de Base' },
      { to: '/listagem/listar-veiculos', label: 'Alterar Veículo' },
      { to: '/listagem/listar-veiculos', label: 'Excluir Veículo' },
    ],
  },
  {
    key: 'manutencao',
    label: 'Manutenção',
    items: [
      { to: '/cadastros/cadastrar-manutencao', label: 'Cadastrar Manutenção' },
      { to: '/listagem/listar-manutencoes', label: 'Listar Todas as Manutenções' },
      { to: '/atendimento', label: 'Finalizar Manutenções' },
    ],
  },
  {
    key: 'usuarios',
    label: 'Usuários',
    items: [
      { to: '/cadastros/cadastrar-usuario', label: 'Cadastrar Usuário' },
      { to: '/listagem/listar-usuarios', label: 'Listar Usuários' },
    ],
  },
]

function toggleMenu() {
  menuOpen.value = !menuOpen.value
  openDropdown.value = null
}

function closeMenu() {
  menuOpen.value = false
  openDropdown.value = null
}

function toggleDropdown(key: string) {
  openDropdown.value = openDropdown.value === key ? null : key
}

function handleLogout() {
  logout()
  router.push('/login')
  closeMenu()
}

function handleClickOutside(event: MouseEvent) {
  if (navRef.value && !navRef.value.contains(event.target as Node)) {
    openDropdown.value = null
    menuOpen.value = false
  }
}

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    openDropdown.value = null
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <nav ref="navRef" class="navbar" role="navigation" aria-label="Navegação principal">
    <RouterLink :to="isTecnico ? '/atendimento' : '/'" class="navbar-brand" @click="closeMenu">
      🚛 NOVO TRIUNFO
    </RouterLink>

    <button
      class="navbar-toggler"
      type="button"
      :aria-expanded="menuOpen"
      aria-label="Alternar navegação"
      @click="toggleMenu"
    >
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
        <template v-if="menuOpen">
          <line x1="18" y1="6" x2="6" y2="18" />
          <line x1="6" y1="6" x2="18" y2="18" />
        </template>
        <template v-else>
          <line x1="4" y1="8" x2="20" y2="8" />
          <line x1="4" y1="16" x2="20" y2="16" />
        </template>
      </svg>
    </button>

    <div class="navbar-collapse" :class="{ show: menuOpen }">
      <ul class="navbar-nav">
        <!-- Links para Administrador -->
        <template v-if="isAdmin">
          <li class="nav-item">
            <RouterLink to="/" class="nav-link" @click="closeMenu">
              Início
            </RouterLink>
          </li>

          <li
            v-for="section in navSections"
            :key="section.key"
            class="nav-item dropdown"
            :class="{ open: openDropdown === section.key }"
          >
            <button
              class="nav-link dropdown-trigger"
              type="button"
              :aria-expanded="openDropdown === section.key"
              @click.stop="toggleDropdown(section.key)"
            >
              {{ section.label }}
              <svg class="chevron" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="6 9 12 15 18 9" />
              </svg>
            </button>

            <ul class="dropdown-menu">
              <li v-for="item in section.items" :key="item.to + item.label">
                <RouterLink
                  :to="item.to"
                  class="dropdown-item"
                  @click="closeMenu"
                >
                  {{ item.label }}
                </RouterLink>
              </li>
            </ul>
          </li>
        </template>

        <!-- Links para Técnico -->
        <template v-else-if="isTecnico">
          <li class="nav-item">
            <RouterLink to="/atendimento" class="nav-link" @click="closeMenu">
              🔧 Atendimento
            </RouterLink>
          </li>
        </template>
      </ul>

      <div class="navbar-actions">
        <button
          class="theme-toggle"
          :title="themeLabel"
          :aria-label="themeLabel"
          @click="toggleTheme"
        >
          {{ themeIcon }}
        </button>

        <button
          v-if="isAuthenticated"
          class="btn-logout"
          title="Sair do sistema"
          @click="handleLogout"
        >
          🚪 Sair
        </button>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.navbar {
  position: relative;
}

/* Item de dropdown no desktop */
.nav-item.dropdown {
  position: relative;
}

.dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  cursor: pointer;
  font: inherit;
}

.chevron {
  transition: transform 0.2s ease;
}

.nav-item.dropdown.open .chevron {
  transform: rotate(180deg);
}

.dropdown-menu {
  display: none;
  flex-direction: column;
  list-style: none;
  margin: 0;
  padding: 6px;
  min-width: 220px;
}

.nav-item.dropdown.open .dropdown-menu {
  display: flex;
}

/* Ajuste das cores dos itens */
.dropdown-item {
  display: block;
  padding: 8px 12px;
  border-radius: 6px;
  text-decoration: none;
  white-space: nowrap;
  transition: color 0.2s ease, background 0.2s ease;
}

/* Desktop: dropdown flutuante */
@media (min-width: 768px) {
  .dropdown-menu {
    position: absolute;
    top: 100%;
    left: 0;
    z-index: 20;
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.35);
  }
}

/* Mobile: dropdown em accordion, dentro do collapse */
@media (max-width: 767px) {
  .dropdown-menu {
    padding-left: 12px;
    background: transparent !important;
    border: none !important;
    box-shadow: none !important;
  }
  .dropdown-trigger {
    width: 100%;
    justify-content: space-between;
  }
}

/* -------- DARK MODE -------- */
.dark-mode .dropdown-menu {
  background: #1a1a1a;
  border: 1px solid #2a2a2a;
}

.dark-mode .dropdown-item {
  color: #ccc;
}

.dark-mode .dropdown-item:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

/* -------- LIGHT MODE -------- */
.light-mode .dropdown-menu {
  background: #fff;
  border: 1px solid #ddd;
}

.light-mode .dropdown-item {
  color: #000;
}

.light-mode .dropdown-item:hover {
  color: #000;
  background: rgba(0, 0, 0, 0.05);
}

/* -------- LOGOUT BUTTON -------- */
.btn-logout {
  background: rgba(220, 53, 69, 0.15);
  border: 1px solid rgba(220, 53, 69, 0.3);
  color: #dc3545;
  padding: 6px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.875rem;
  transition: all 0.25s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-logout:hover {
  background: #dc3545;
  color: #fff;
  box-shadow: 0 0 12px rgba(220, 53, 69, 0.4);
}

.dark-mode .btn-logout {
  background: rgba(239, 83, 80, 0.15);
  border-color: rgba(239, 83, 80, 0.3);
  color: #ef5350;
}

.dark-mode .btn-logout:hover {
  background: #ef5350;
  color: #fff;
}

.navbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

@media (max-width: 767px) {
  .navbar-actions {
    margin-top: 1rem;
    width: 100%;
    justify-content: space-between;
  }
}
</style>

