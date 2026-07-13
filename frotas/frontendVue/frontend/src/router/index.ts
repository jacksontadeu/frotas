import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
    },
    {
      path: '/cadastros/cadastrar-base',
      name: 'cadastrar-base',
      component: () => import('../views/bases/CadastrarBaseView.vue'),
    },
    {
      path: '/listagem/listar-bases',
      name: 'listar-bases',
      component: () => import('../views/bases/ListarBasesView.vue'),
    },
    {
      path: '/cadastros/cadastrar-veiculo',
      name: 'cadastrar-veiculo',
      component: () => import('../views/veiculos/CadastrarVeiculoView.vue'),
    },
    {
      path: '/listagem/listar-veiculos',
      name: 'listar-veiculos',
      component: () => import('../views/veiculos/ListarVeiculosView.vue'),
    },
    {
      path: '/cadastros/cadastrar-manutencao',
      name: 'cadastrar-manutencao',
      component: () => import('../views/manutencao/CadastrarManutencaoView.vue'),
    },
  ],
})

export default router
