import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { useTheme } from './composables/useTheme'
import { useAuth } from './composables/useAuth'
import './style.css'

// Inicializa tema ao carregar
const { initTheme } = useTheme()
initTheme()

// Restaura sessão do usuário a partir do token salvo no sessionStorage
// OBRIGATÓRIO: deve ser chamado antes do mount para que o router guard
// consiga verificar user.value?.role corretamente
const { initAuth } = useAuth()
initAuth()

const app = createApp(App)
app.use(router)
app.mount('#app')


