import { ref } from 'vue'

type Theme = 'dark-mode' | 'light-mode'

// Estado global compartilhado entre componentes
const theme = ref<Theme>(
  (localStorage.getItem('theme') as Theme) ?? 'dark-mode'
)

export function useTheme() {
  function applyTheme(t: Theme) {
    document.body.classList.remove('dark-mode', 'light-mode')
    document.body.classList.add(t)
    localStorage.setItem('theme', t)
    theme.value = t
  }

  function toggleTheme() {
    applyTheme(theme.value === 'dark-mode' ? 'light-mode' : 'dark-mode')
  }

  // Aplica imediatamente (idempotente)
  function initTheme() {
    applyTheme(theme.value)
  }

  return { theme, toggleTheme, initTheme }
}
