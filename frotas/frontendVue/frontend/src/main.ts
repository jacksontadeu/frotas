import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { useTheme } from './composables/useTheme'
import './style.css'



// Inicializa tema ao carregar
const { initTheme } = useTheme()
initTheme()

const app = createApp(App)
app.use(router)
app.mount('#app')


