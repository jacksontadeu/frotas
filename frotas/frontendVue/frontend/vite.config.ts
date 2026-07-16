import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      // Proxy para evitar CORS em desenvolvimento
      // Requisições para /base, /veiculo, /manutencao são redirecionadas ao backend
      '/base': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/veiculo': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/manutencao': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/login': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
