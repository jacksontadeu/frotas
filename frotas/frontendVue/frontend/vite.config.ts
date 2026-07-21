import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
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
      // /login é usado como rota Vue (GET) e endpoint REST (POST).
      // bypass: GET navega pelo Vue Router; POST vai ao backend Spring Boot.
      '/login': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        bypass(req) {
          if (req.method !== 'POST') return req.url ?? '/'
          // sem retorno = undefined → Vite proxia o POST para o backend
        },
      },
    },
  },
})

