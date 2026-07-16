import axios from 'axios'

// Em dev, o proxy do Vite redireciona para http://localhost:8080
// Em produção, configure VITE_API_BASE_URL
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '',
  headers: {
    'Content-Type': 'application/json',
  },
})

// Adiciona o token JWT no cabeçalho das requisições se disponível
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export default api
