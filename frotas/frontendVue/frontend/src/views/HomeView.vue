<script setup lang="ts">
import { RouterLink } from 'vue-router'

const cards = [
  {
    icon: '🏢',
    title: 'Bases',
    description: 'Gerencie as bases operacionais da frota.',
    to: '/cadastros/cadastrar-base',
  },
  {
    icon: '🚗',
    title: 'Veículos',
    description: 'Controle todos os veículos da frota.',
    to: '/cadastros/cadastrar-veiculo',
  },
  {
    icon: '🔧',
    title: 'Manutenções',
    description: 'Registre manutenções preventivas e corretivas.',
    to: '/cadastros/cadastrar-manutencao',
  },
]
</script>

<template>
  <div class="page-content">
    <!-- Hero -->
    <section class="hero">
      <div class="hero-badge">Sistema de Gestão</div>
      <h1 class="hero-title">Controle de <span class="gradient-text">Frotas</span></h1>
      <p class="hero-subtitle">
        Gerencie sua frota de veículos, bases e manutenções em um único lugar,
        com eficiência e controle total.
      </p>
    </section>

    <!-- Cards de acesso rápido -->
    <section class="cards-grid">
      <RouterLink
        v-for="(card, i) in cards"
        :key="card.title"
        :to="card.to"
        class="quick-card"
        :style="{ animationDelay: `${i * 0.1}s` }"
      >
        <div class="quick-card-icon">{{ card.icon }}</div>
        <h3 class="quick-card-title">{{ card.title }}</h3>
        <p class="quick-card-desc">{{ card.description }}</p>
      </RouterLink>
    </section>
  </div>
</template>

<style scoped>
.page-content {
  padding: 3rem 2rem;
  max-width: 1100px;
  margin: 0 auto;
}

/* Hero */
.hero {
  text-align: center;
  padding: 3rem 0 3.5rem;
  animation: fadeSlideIn 0.5s ease;
}

@media (max-width: 800px) {
  .page-content {
    padding: 1.5rem 1rem;
  }
  .hero {
    padding: 1.5rem 0 2rem;
  }
}

.hero-badge {
  display: inline-block;
  padding: 0.35rem 1rem;
  background: rgba(99, 102, 241, 0.12);
  border: 1px solid rgba(99, 102, 241, 0.3);
  border-radius: 999px;
  color: var(--accent-3);
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  margin-bottom: 1.25rem;
}

.hero-title {
  font-size: clamp(2.5rem, 6vw, 4rem);
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: -0.02em;
  margin-bottom: 1.25rem;
  color: var(--text-primary);
}

.gradient-text {
  background: var(--gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 1.1rem;
  color: var(--text-secondary);
  max-width: 540px;
  margin: 0 auto;
  line-height: 1.7;
}

/* Cards */
.cards-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* 3 colunas lado a lado */
  gap: 1.5rem;
}

@media (max-width: 800px) {
  .cards-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
}

.quick-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  padding: 2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  transition: border-color var(--transition), box-shadow var(--transition), transform var(--transition);
  animation: fadeSlideIn 0.5s ease both;
  text-align: center;
  cursor: pointer; /* deixa claro que é clicável */
  text-decoration: none; /* remove sublinhado do RouterLink */
  color: inherit; /* mantém cores do texto */
}

.quick-card:hover {
  border-color: rgba(99, 102, 241, 0.4);
  box-shadow: var(--shadow-md), var(--shadow-glow);
  transform: translateY(-3px);
}

.quick-card-icon {
  font-size: 2.25rem;
  line-height: 1;
  margin-bottom: 0.25rem;
}

.quick-card-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
}

.quick-card-desc {
  font-size: 0.9rem;
  color: var(--text-secondary);
  line-height: 1.6;
}
</style>
