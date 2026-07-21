<script setup lang="ts">
export type ModalType = 'success' | 'error' | 'warning' | 'confirm'

const props = withDefaults(defineProps<{
  show: boolean
  type?: ModalType
  title?: string
  message: string
  confirmLabel?: string
  cancelLabel?: string
}>(), {
  type: 'success',
  title: '',
  confirmLabel: 'OK',
  cancelLabel: 'Cancelar',
})

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'confirm'): void
}>()

const icons: Record<ModalType, string> = {
  success: '✅',
  error: '❌',
  warning: '⚠️',
  confirm: '🗑️',
}

const defaultTitles: Record<ModalType, string> = {
  success: 'Sucesso!',
  error: 'Ocorreu um erro',
  warning: 'Atenção',
  confirm: 'Confirmar ação',
}

function onOverlayClick() {
  if (props.type !== 'confirm') {
    emit('close')
  }
}
</script>

<template>
  <Teleport to="body">
    <Transition name="modal-fade">
      <div
        v-if="show"
        class="app-modal-overlay"
        @click.self="onOverlayClick"
        role="dialog"
        aria-modal="true"
      >
        <Transition name="modal-pop">
          <div
            v-if="show"
            class="app-modal-box"
            :class="`app-modal-${type}`"
          >
            <!-- Icon -->
            <div class="app-modal-icon-wrap">
              <span class="app-modal-icon">{{ icons[type] }}</span>
            </div>

            <!-- Content -->
            <h2 class="app-modal-title">{{ title || defaultTitles[type] }}</h2>
            <p class="app-modal-message">{{ message }}</p>

            <!-- Actions -->
            <div class="app-modal-actions" :class="{ 'two-btns': type === 'confirm' }">
              <button
                v-if="type === 'confirm'"
                class="btn btn-secondary"
                @click="emit('close')"
              >
                {{ cancelLabel }}
              </button>
              <button
                class="btn"
                :class="type === 'error' || type === 'warning' ? 'btn-danger' : type === 'confirm' ? 'btn-danger' : 'btn-primary'"
                @click="type === 'confirm' ? emit('confirm') : emit('close')"
              >
                {{ type === 'confirm' ? confirmLabel : (type === 'success' ? 'OK' : 'Fechar') }}
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.app-modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(4px);
}

.app-modal-box {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  padding: 2.5rem 2rem 2rem;
  width: 100%;
  max-width: 420px;
  text-align: center;
  box-shadow: var(--shadow-lg), 0 0 60px rgba(0,0,0,0.4);
  position: relative;
  overflow: hidden;
}

/* Faixa colorida no topo */
.app-modal-box::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
}

.app-modal-success::before { background: var(--color-success); }
.app-modal-error::before   { background: var(--color-danger); }
.app-modal-warning::before { background: var(--color-warning); }
.app-modal-confirm::before { background: var(--color-danger); }

/* Icon */
.app-modal-icon-wrap {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.25rem;
}

.app-modal-success .app-modal-icon-wrap { background: rgba(16, 185, 129, 0.12); }
.app-modal-error   .app-modal-icon-wrap { background: rgba(239, 68, 68, 0.12); }
.app-modal-warning .app-modal-icon-wrap { background: rgba(245, 158, 11, 0.12); }
.app-modal-confirm .app-modal-icon-wrap { background: rgba(239, 68, 68, 0.12); }

.app-modal-icon {
  font-size: 2.25rem;
  line-height: 1;
}

/* Title */
.app-modal-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.625rem;
}

/* Message */
.app-modal-message {
  font-size: 0.9375rem;
  color: var(--text-secondary);
  line-height: 1.55;
  margin-bottom: 1.75rem;
  word-break: break-word;
}

/* Actions */
.app-modal-actions {
  display: flex;
  justify-content: center;
}

.app-modal-actions.two-btns {
  gap: 0.75rem;
}

/* Transitions */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.2s ease;
}
.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-pop-enter-active {
  transition: opacity 0.22s ease, transform 0.22s cubic-bezier(0.16, 1, 0.3, 1);
}
.modal-pop-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.modal-pop-enter-from {
  opacity: 0;
  transform: scale(0.88) translateY(12px);
}
.modal-pop-leave-to {
  opacity: 0;
  transform: scale(0.94);
}
</style>
