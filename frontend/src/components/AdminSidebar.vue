<script setup>
import { useRouter, useRoute } from 'vue-router'
import { ref, watch, onMounted } from "vue";
import { useWmsWebSocket } from '../composables/useWmsWebSocket.js'
import api from '../api.js'

const router = useRouter()
const route = useRoute()

// Estraiamo ANCHE connectWebSocket
const {
  nuoviTaskAttiviBadge,
  nuoviTaskCompletatiBadge,
  azzeraBadgeAttivi,
  azzeraBadgeCompletati,
  connectWebSocket
} = useWmsWebSocket()

// --- STATO DROPDOWN MAPPA ---
const mappaAperta = ref(false)
const reparti = ref([])
const repartiLoading = ref(false)

const toggleMappa = async () => {
  mappaAperta.value = !mappaAperta.value
  if (mappaAperta.value && reparti.value.length === 0) {
    repartiLoading.value = true
    try {
      const res = await api.get('/api/reparti/carica')
      reparti.value = res.data
    } catch (e) {
      console.error('Errore caricamento reparti:', e)
    } finally {
      repartiLoading.value = false
    }
  }
}

const vaiAlReparto = (id) => {
  router.push(`/Mappa/reparto/${id}`)
  mappaAperta.value = false
}

// Badge temperatura → classe colore
const getTempClass = (temp) => {
  if (temp < 0) return 'temp-freezing'
  if (temp <= 8) return 'temp-cold'
  return 'temp-warm'
}

const logout = () => {
  sessionStorage.clear()
  router.push('/')
}

// Quando la sidebar viene caricata, accendiamo la ricezione globale per l'Admin
onMounted(() => {
  const emailUtente = sessionStorage.getItem('emailUtente') || 'admin@wms.it'
  const ruoloUtente = sessionStorage.getItem('ruolo') || 'ADMIN'
  connectWebSocket(emailUtente, ruoloUtente)
})

// LOGICA INTELLIGENTE: Azzeriamo i badge quando l'Admin entra nelle rispettive pagine
watch(() => route.path, (nuovoPath) => {
  if (nuovoPath === '/GestioneTask' || nuovoPath === '/AdminHome' || nuovoPath === '/dashboard') {
    azzeraBadgeAttivi()
  } else if (nuovoPath === '/StoricoMovimenti') {
    azzeraBadgeCompletati()
  }
  // Chiudi dropdown mappa al cambio pagina
  mappaAperta.value = false
}, { immediate: true })
</script>

<template>
  <aside class="glass-sidebar">
    <div class="sidebar-header">
      <div class="logo-icon">
        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"></path></svg>
      </div>
      <h2>WMS Admin</h2>
    </div>

    <nav class="sidebar-nav">
      <div class="nav-item" :class="{ active: route.path === '/dashboard' }" @click="router.push('/dashboard')">
        <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"></path></svg>
        Dashboard
      </div>

      <div class="nav-item" :class="{ active: route.path === '/GestioneTask' }" @click="router.push('/GestioneTask')">
        <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"></path>
        </svg>
        Gestione Task
        <span v-if="nuoviTaskAttiviBadge > 0 && route.path !== '/GestioneTask'" class="badge-notifica badge-blu">
          {{ nuoviTaskAttiviBadge }}
        </span>
      </div>

      <!-- VOCE MAPPA CON DROPDOWN -->
      <div>
        <div
          class="nav-item"
          :class="{ active: route.path.startsWith('/Mappa') || mappaAperta }"
          @click="toggleMappa"
        >
          <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7"></path></svg>
          Mappa
          <svg
            class="chevron-icon"
            :class="{ 'chevron-open': mappaAperta }"
            fill="none" stroke="currentColor" viewBox="0 0 24 24"
          >
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
          </svg>
        </div>

        <!-- Dropdown reparti -->
        <div class="reparto-dropdown" :class="{ 'dropdown-open': mappaAperta }">
          <div v-if="repartiLoading" class="reparto-loading">
            <span>Caricamento...</span>
          </div>
          <div
            v-for="reparto in reparti"
            :key="reparto.id"
            class="reparto-item"
            :class="{ 'active-reparto': route.path === `/Mappa/reparto/${reparto.id}` }"
            @click="vaiAlReparto(reparto.id)"
          >
            <span class="reparto-temp-dot" :class="getTempClass(reparto.temperatura)"></span>
            <span class="reparto-nome">{{ reparto.nome }}</span>
            <span class="reparto-temp-label">{{ reparto.temperatura }}°C</span>
          </div>
        </div>
      </div>

      <div class="nav-item" :class="{ active: route.path === '/GestioneAdmin' }" @click="router.push('/GestioneAdmin')">
        <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"></path></svg>
        Amministratori
      </div>

      <div class="nav-item" :class="{ active: route.path === '/GestioneDipendenti' }" @click="router.push('/GestioneDipendenti')">
        <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>
        Dipendenti
      </div>

      <div class="nav-item" :class="{ active: route.path === '/StoricoMovimenti' }" @click="router.push('/StoricoMovimenti')">
        <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>
        Storico Movimenti
        <span v-if="nuoviTaskCompletatiBadge > 0 && route.path !== '/StoricoMovimenti'" class="badge-notifica badge-verde">
          {{ nuoviTaskCompletatiBadge }}
        </span>
      </div>

      <div class="nav-item" :class="{ active: route.path === '/GestioneTurni' }" @click="router.push('/GestioneTurni')">
        <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
        Turni
      </div>
    </nav>

    <div class="sidebar-footer">
      <button @click="logout" class="btn-logout">
        <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path></svg>
        Disconnetti
      </button>
    </div>
  </aside>
</template>

<style scoped>
/* ------- EFFETTO GLASS SCURO PER LA SIDEBAR ------- */
.glass-sidebar {
  width: 260px;
  /* Sfondo molto più scuro e meno trasparente per bloccare l'alone luminoso */
  background-color: rgba(11, 15, 25, 0.85);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  color: #cbd5e1; /* Testo di default più chiaro per massimo contrasto */
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(255, 255, 255, 0.05);
  z-index: 20;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.3); /* Stacca la sidebar dal contenuto */
}

.sidebar-header {
  padding: 30px 24px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon svg {
  width: 28px;
  height: 28px;
  color: #60a5fa;
  filter: drop-shadow(0 0 6px rgba(96, 165, 250, 0.4));
}

.sidebar-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #ffffff; /* Bianco puro per il titolo */
  letter-spacing: -0.5px;
}

.sidebar-nav {
  flex-grow: 1;
  padding: 0 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow-y: auto;
}

/* --- TASTI DI NAVIGAZIONE --- */
.nav-item {
  padding: 12px 16px;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.nav-icon {
  width: 20px;
  height: 20px;
  opacity: 0.7;
  transition: opacity 0.2s ease;
  flex-shrink: 0;
}

/* Hover: Sfondo leggermente più chiaro ma senza bagliori confusi */
.nav-item:hover {
  background-color: rgba(255, 255, 255, 0.08);
  color: #ffffff;
}

.nav-item:hover .nav-icon {
  opacity: 1;
}

/* STATO ATTIVO: Sfondo azzurro traslucido, molto pulito */
.nav-item.active {
  background-color: rgba(59, 130, 246, 0.15);
  color: #60a5fa;
  font-weight: 600;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.nav-item.active .nav-icon {
  opacity: 1;
}

/* --- CHEVRON --- */
.chevron-icon {
  width: 14px;
  height: 14px;
  margin-left: auto;
  opacity: 0.5;
  transition: transform 0.25s ease, opacity 0.2s ease;
  flex-shrink: 0;
}
.chevron-open {
  transform: rotate(90deg);
  opacity: 1;
}

/* --- DROPDOWN REPARTI --- */
.reparto-dropdown {
  max-height: 0;
  overflow: hidden;
  transition: max-height 0.3s cubic-bezier(0.4, 0, 0.2, 1),
              opacity 0.25s ease;
  opacity: 0;
  margin: 0 8px;
}
.dropdown-open {
  max-height: 300px;
  opacity: 1;
}

.reparto-loading {
  padding: 10px 16px;
  font-size: 0.8rem;
  color: rgba(148, 163, 184, 0.7);
  font-style: italic;
}

.reparto-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 16px 9px 22px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.82rem;
  font-weight: 500;
  color: #94a3b8;
  transition: all 0.18s ease;
  border: 1px solid transparent;
  margin-bottom: 2px;
}

.reparto-item:hover {
  background-color: rgba(255, 255, 255, 0.07);
  color: #e2e8f0;
}

.reparto-item.active-reparto {
  background-color: rgba(59, 130, 246, 0.12);
  color: #60a5fa;
  border-color: rgba(59, 130, 246, 0.15);
}

.reparto-temp-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.temp-freezing { background: #60a5fa; box-shadow: 0 0 4px rgba(96, 165, 250, 0.6); }
.temp-cold     { background: #38bdf8; box-shadow: 0 0 4px rgba(56, 189, 248, 0.5); }
.temp-warm     { background: #fbbf24; box-shadow: 0 0 4px rgba(251, 191, 36, 0.5); }

.reparto-nome {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.reparto-temp-label {
  font-size: 0.7rem;
  font-weight: 700;
  color: rgba(148, 163, 184, 0.6);
  flex-shrink: 0;
}

/* --- FOOTER / DISCONNETTI --- */
.sidebar-footer {
  padding: 24px 16px;
}

.btn-logout {
  width: 100%;
  padding: 12px;
  background-color: rgba(255, 255, 255, 0.03);
  color: #94a3b8;
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  cursor: pointer;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s ease;
}

.btn-logout:hover {
  background-color: rgba(239, 68, 68, 0.15);
  color: #f87171;
  border-color: rgba(239, 68, 68, 0.3);
}

/* --- BADGE --- */
.badge-notifica {
  margin-left: auto;
  font-size: 0.7rem;
  font-weight: 800;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  min-width: 12px;
  text-align: center;
  line-height: 1.2;
}

.badge-blu { background: #3b82f6; }
.badge-verde { background: #10b981; }
</style>