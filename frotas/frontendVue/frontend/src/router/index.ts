import { createRouter, createWebHistory } from 'vue-router'
import { useAuth } from '../composables/useAuth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { hideHeader: true, guestOnly: true },
    },
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
      meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] },
    },
    {
      path: '/cadastros/cadastrar-base',
      name: 'cadastrar-base',
      component: () => import('../views/bases/CadastrarBaseView.vue'),
      meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] },
    },
    {
      path: '/listagem/listar-bases',
      name: 'listar-bases',
      component: () => import('../views/bases/ListarBasesView.vue'),
      meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] },
    },
    {
      path: '/cadastros/cadastrar-veiculo',
      name: 'cadastrar-veiculo',
      component: () => import('../views/veiculos/CadastrarVeiculoView.vue'),
      meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] },
    },
    {
      path: '/listagem/listar-veiculos',
      name: 'listar-veiculos',
      component: () => import('../views/veiculos/ListarVeiculosView.vue'),
      meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] },
    },
    {
      path: '/cadastros/cadastrar-manutencao',
      name: 'cadastrar-manutencao',
      component: () => import('../views/manutencao/CadastrarManutencaoView.vue'),
      meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] },
    },
    {
      path: '/listagem/listar-manutencoes',
      name: 'listar-manutencoes',
      component: () => import('../views/manutencao/ListarManutencoesView.vue'),
      meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] },
    },
    {
      path: '/atendimento',
      name: 'atendimento-manutencao',
      component: () => import('../views/manutencao/AtendimentoManutencaoView.vue'),
      meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] },
    },
    {
      path: '/listagem/listar-usuarios',
      name: 'listar-usuarios',
      component: () => import('../views/usuarios/ListarUsuariosView.vue'),
      meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] },
    },
    {
      path: '/cadastros/cadastrar-usuario',
      name: 'cadastrar-usuario',
      component: () => import('../views/usuarios/CadastrarUsuarioView.vue'),
      meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] },
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/',
    },
  ],
})

// Guarda global de rotas
router.beforeEach((to, _from, next) => {
  const { isAuthenticated, user, initAuth } = useAuth()

  // Garante que o usuário seja restaurado do token em qualquer navegação
  if (sessionStorage.getItem('token') && (!isAuthenticated.value || !user.value)) {
    initAuth()
  }

  // Se a rota requer autenticação e o usuário não está autenticado
  if (to.meta.requiresAuth && !isAuthenticated.value) {
    next({ name: 'login' })
  }
  // Se o usuário está logado mas tenta ir para a tela de login
  else if (to.meta.guestOnly && isAuthenticated.value) {
    next({ name: 'home' })
  }
  // Se está autenticado, verifica permissões de papéis
  else if (to.meta.requiresAuth) {
    const userRole = user.value?.role
    const allowedRoles = (to.meta.roles as string[]) || ['ROLE_ADMIN']

    if (userRole && allowedRoles.includes(userRole)) {
      next()
    } else {
      next({ name: 'login' })
    }
  } else {
    next()
  }
})

export default router
