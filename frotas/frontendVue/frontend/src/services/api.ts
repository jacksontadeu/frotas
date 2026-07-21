import axios from 'axios'
import router from '../router'

// Em dev, o proxy do Vite redireciona para http://localhost:8080
// Em produção, configure VITE_API_BASE_URL
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '',
  headers: {
    'Content-Type': 'application/json',
  },
})

// Adiciona o token JWT no cabeçalho das requisições se disponível
// Usa sessionStorage para maior segurança (token não persiste ao fechar o browser)
api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Interceptor de resposta: redireciona para /login em caso de 401 ou 403
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      // Apenas redireciona se não estiver já na página de login
      if (!window.location.pathname.includes('/login')) {
        sessionStorage.removeItem('token')
        router.push('/login')   // SPA navigation — sem reload completo
      }
    }
    return Promise.reject(error)
  }
)

export default api
