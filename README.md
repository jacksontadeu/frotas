# 🚛 Novotriunfo Frotas

> Um sistema moderno e robusto para controle e gestão de manutenção de frotas de veículos. Desenvolvido com uma arquitetura desacoplada utilizando **Spring Boot (Java)** no backend e **Vue 3 (TypeScript + Vite)** no frontend.

---

## 💻 Sobre o Projeto

O **Novotriunfo Frotas** é uma solução completa projetada para otimizar a gestão de frotas e o fluxo de manutenção de veículos. O sistema permite o cadastro de bases operacionais, controle detalhado de veículos e o gerenciamento do ciclo de vida de manutenções — desde a abertura do chamado até o atendimento técnico detalhado com checklist e conclusão.

O painel visual adota um estilo **Dark Mode premium com Glassmorphism** (efeito de vidro translúcido, gradientes suaves e micro-interações responsivas), oferecendo uma experiência de usuário excepcional tanto em desktops quanto em dispositivos móveis.

---

## 🛠️ Tecnologias Utilizadas

### **Backend**
*   **Java 17**
*   **Spring Boot 3.x**
    *   *Spring Security* & *JWT* para autenticação e autorização robustas.
    *   *Spring Data JPA* para persistência de dados.
    *   *Spring Web* para APIs RESTful.
*   **MySQL 8** (Banco de dados relacional).
*   **Flyway Migration** (Gerenciamento e versionamento do schema do banco de dados).
*   **Maven** (Gerenciamento de dependências e build).

### **Frontend**
*   **Vue 3** (Composition API com `<script setup>`).
*   **TypeScript** (Tipagem estática e segurança no código).
*   **Vite** (Build tool ultrarrápido).
*   **Vue Router** (Navegação SPA e Guards de segurança).
*   **Vanilla CSS** (Design responsivo customizado e Glassmorphism).

---

## 🔑 Funcionalidades Principais

*   **Autenticação Segura (JWT):**
    *   Controle de acesso baseado em cargos/roles (`ADMIN` e `TECNICO`).
    *   Guards de rota no frontend para impedir acessos não autorizados.
    *   Tela de login estilizada com efeitos de desfoque de fundo e transições suaves.
*   **Gestão de Bases:**
    *   Cadastro de bases operacionais.
    *   Listagem em tempo real das bases ativas.
*   **Gestão de Veículos:**
    *   Cadastro de veículos (placa, modelo, marca, ano, base associada).
    *   Listagem interativa com filtros e busca.
*   **Fluxo de Manutenções:**
    *   **Abertura:** Administradores criam ordens de manutenção associando veículos e detalhando a falha relatada.
    *   **Painel do Técnico:** Visualização dedicada das manutenções atribuídas.
    *   **Atendimento Técnico:** Checklist interativo de inspeção (freios, pneus, elétrica, etc.), registro de observações, peças substituídas e atualização do status em tempo real.

---

## 📁 Estrutura do Projeto

```text
frotas/
├── backend/                  # API Spring Boot (Java)
│   ├── src/main/java/        # Código-fonte Java (Controllers, Services, Repositories, Entities, Security)
│   ├── src/main/resources/   # Configurações (application.yaml, migrations do Flyway, templates)
│   └── pom.xml               # Dependências do Maven
│
├── frontendVue/
│   └── frontend/             # Single Page Application (Vue 3 + TS)
│       ├── src/
│       │   ├── assets/       # Imagens, logotipos e mídias globais
│       │   ├── components/   # Componentes Vue reutilizáveis (Header, Cards, Formulários)
│       │   ├── composables/  # Logic Hooks reutilizáveis (useAuth, etc.)
│       │   ├── router/       # Configuração de rotas e Navigation Guards
│       │   ├── services/     # Integração com a API Backend (Axios/Fetch)
│       │   ├── types/        # Interfaces e tipos do TypeScript
│       │   ├── views/        # Páginas da aplicação (Login, Home, Bases, Veículos, Manutenções)
│       │   ├── App.vue       # Componente raiz
│       │   ├── style.css     # Design System (tokens CSS, Glassmorphism, responsividade)
│       │   └── main.ts       # Inicialização do Vue
│       ├── package.json      # Scripts e dependências NPM
│       └── vite.config.ts    # Configurações do Vite
└── README.md                 # Documentação principal do projeto
```

---

## 🚀 Como Executar o Projeto

### **Pré-requisitos**
*   Java JDK 17 ou superior instalado.
*   Node.js (versão 18 ou superior) & npm instalados.
*   MySQL rodando localmente (ou via Docker).

---

### **1. Configurando e Rodando o Backend**

1. Acesse a pasta do backend:
   ```bash
   cd backend
   ```

2. Certifique-se de que o banco de dados MySQL está ativo. Crie o schema `triunfodb` se necessário:
   ```sql
   CREATE DATABASE triunfodb;
   ```

3. Verifique as configurações de conexão no arquivo [application.yaml](file:///media/jackson/DEV/DEV/Novotriunfo/frotas/backend/src/main/resources/application.yaml):
   *   URL: `jdbc:mysql://localhost:3307/triunfodb`
   *   Usuário: `user`
   *   Senha: `xxxxx`
   *(Ajuste a porta e credenciais conforme o seu ambiente local).*

4. Execute o backend usando o Maven Wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
   *O servidor iniciará por padrão na porta `8080`.*

---

### **2. Configurando e Rodando o Frontend**

1. Acesse a pasta do frontend:
   ```bash
   cd frontendVue/frontend
   ```

2. Instale as dependências do projeto:
   ```bash
   npm install
   ```

3. Certifique-se de que a URL base da API em `src/services/api.ts` está apontando para o seu backend local (`http://localhost:8080`).

4. Inicie o servidor de desenvolvimento do Vite:
   ```bash
   npm run dev
   ```
   *O frontend estará disponível em `http://localhost:5173` (ou na porta indicada no console).*

---

## 🎨 Padrão Visual (Design System)

A interface utiliza regras de CSS customizadas baseadas nos seguintes pilares:
*   **Fundo Escuro Premium:** Paleta de cores escuras e profundas com tons de cinza azulado.
*   **Efeito Glassmorphism:** Painéis com `backdrop-filter: blur(16px)`, bordas semi-transparentes de `rgba(255, 255, 255, 0.08)` e fundos translúcidos que criam sensação de profundidade tridimensional.
*   **Responsividade:** Desenvolvido sob medida com Media Queries inteligentes para garantir navegação perfeita de celulares até monitores ultra-wide.
*   **Micro-interações:** Botões e cartões reagem suavemente ao mouse (hover) com pequenas elevações e transições de cor de destaque (azul/ciano/esmeralda).

---

Desenvolvido por **Jackson Tadeu** 🚀
