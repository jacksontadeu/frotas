# 💻 Novotriunfo Frotas - Frontend

> Interface de Usuário moderna em Single Page Application (SPA) para gestão e controle de manutenção de frotas. Desenvolvida em **Vue 3**, **TypeScript** e **Vite**.

## 🎨 Características Visuais
Este frontend foi desenhado com um foco especial em estética e usabilidade moderna:
*   **Design Glassmorphism:** Painéis translúcidos, cantos arredondados, bordas sutis e desfoque de fundo (`backdrop-filter`).
*   **Tema Dark Mode:** Paleta escura elegante com contrastes em tons de azul e verde ciano.
*   **Totalmente Responsivo:** Menu de navegação colapsável e cards adaptáveis para visualização otimizada em smartphones e desktops.

## 🚀 Tecnologias
*   **Vue 3** (Composition API com `<script setup>`)
*   **TypeScript**
*   **Vite**
*   **Vue Router**
*   **Axios** (Integração de APIs REST)

## 📁 Estrutura de Arquivos Principal
*   `src/components/`: Componentes reutilizáveis como `AppHeader.vue`.
*   `src/composables/`: Hooks de estado como `useAuth.ts` para controle de autenticação JWT.
*   `src/services/`: Camada de serviços (`api.ts`, `manutencaoService.ts`) que consome a API do backend.
*   `src/views/`: Telas principais do sistema organizadas por módulos (Bases, Veículos, Manutenções, Login, Home).
*   `src/style.css`: Estilos globais e tokens de cores da aplicação.

## ⚙️ Instalação e Execução

1. Certifique-se de ter o **Node.js** instalado.
2. Navegue até a pasta do projeto:
   ```bash
   cd frontendVue/frontend
   ```
3. Instale as dependências:
   ```bash
   npm install
   ```
4. Inicie o servidor de desenvolvimento:
   ```bash
   npm run dev
   ```
5. Abra no navegador:
   *   O app iniciará por padrão em `http://localhost:5173`.

---
*Para obter detalhes sobre o setup do banco de dados MySQL e do backend em Spring Boot, consulte o [README.md Principal do Projeto](file:///media/jackson/DEV/DEV/Novotriunfo/frotas/README.md) na raiz.*
