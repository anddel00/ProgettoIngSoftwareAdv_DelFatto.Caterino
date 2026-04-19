<script setup>
import { useWmsWebSocket } from './composables/useWmsWebSocket'

// Inizializziamo il composable.
// Grazie alla logica delle variabili definite fuori dalla funzione,
// questo stato sarà condiviso con tutti gli altri componenti.
const { notificaGlobale } = useWmsWebSocket()
</script>

<template>
  <router-view></router-view>

  <Transition name="slide"> <!--QUESSTO QUI è IL BANNER PER LE NOTIFICHE PUSH, CHE APPARE SU OGNI PAGINA, DOVE SERVE-->
    <div v-if="notificaGlobale" class="global-notification">
      <div class="notification-content">
        <div class="icon-bell">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
          </svg>
        </div>
        <div class="text-content">
          <span class="notif-title">Nuovo Messaggio WMS</span>
          <p class="notif-text">{{ notificaGlobale }}</p>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style>
/* Reset e stili base */
body {
  margin: 0;
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
  background-color: #f8fafc;
  color: #1e293b;
}

/* Stile Notifica Globale */
.global-notification {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
  background: white;
  border-left: 4px solid #10b981;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  padding: 16px;
  min-width: 300px;
  max-width: 400px;
}

.notification-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-bell {
  background: #ecfdf5;
  color: #10b981;
  padding: 8px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-bell svg {
  width: 20px;
  height: 20px;
}

.text-content {
  display: flex;
  flex-direction: column;
}

.notif-title {
  font-weight: 700;
  font-size: 0.875rem;
  color: #064e3b;
}

.notif-text {
  margin: 0;
  font-size: 0.875rem;
  color: #374151;
}

/* Animazione Slide In/Out */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.slide-enter-from {
  transform: translateX(100%) translateY(20px);
  opacity: 0;
}

.slide-leave-to {
  transform: translateX(100%);
  opacity: 0;
}
</style>