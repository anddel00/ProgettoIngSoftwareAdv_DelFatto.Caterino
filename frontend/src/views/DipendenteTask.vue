<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'
import { useWmsWebSocket } from '../composables/useWmsWebSocket.js' // aggiunto .js per sicurezza

const router = useRouter()
const nomeUtente = ref(sessionStorage.getItem('nomeUtente') || 'Operatore')
const emailUtente = ref(sessionStorage.getItem('emailUtente') || 'dipendente@wms.it')
const ruoloUtente = ref(sessionStorage.getItem('ruolo') || 'Dipendente')

const { connectWebSocket, ultimoTaskRicevuto } = useWmsWebSocket()

const tuttiTask = ref([])

const logout = () => {
  sessionStorage.clear()
  router.push('/')
}

const fetchTasks = async () => {
  try {
    const response = await api.get(`/api/tasks/miei-task?email=${emailUtente.value}`)
    tuttiTask.value = response.data
  } catch (error) {
    console.error("Errore nel recupero dei task:", error)
  }
}

watch(ultimoTaskRicevuto, (nuovoTask) => {
  if (nuovoTask) {
    const index = tuttiTask.value.findIndex(t => t.id === nuovoTask.id)
    if (index !== -1) {
      tuttiTask.value[index] = nuovoTask
      tuttiTask.value = [...tuttiTask.value]
    } else {
      tuttiTask.value = [nuovoTask, ...tuttiTask.value]
    }
  }
})

// --- RICERCA LATO CLIENT ---
const searchQuery = ref('')

// Filtra i task ATTIVI in base alla ricerca
const taskAttiviFiltrati = computed(() => {
  let attivi = tuttiTask.value.filter(t => t.statoTask !== 'COMPLETATO')

  if (!searchQuery.value) return attivi;

  const q = searchQuery.value.toLowerCase();
  return attivi.filter(task => {
    const idMatch = task.id?.toString().includes(q);
    const descMatch = task.descrizione?.toLowerCase().includes(q);
    const tipoMatch = task.tipoTask?.toLowerCase().includes(q);
    return idMatch || descMatch || tipoMatch;
  });
})

// Filtra i task COMPLETATI in base alla ricerca
const taskCompletatiFiltrati = computed(() => {
  let completati = tuttiTask.value.filter(t => t.statoTask === 'COMPLETATO')

  if (!searchQuery.value) return completati;

  const q = searchQuery.value.toLowerCase();
  return completati.filter(task => {
    const idMatch = task.id?.toString().includes(q);
    const descMatch = task.descrizione?.toLowerCase().includes(q);
    const tipoMatch = task.tipoTask?.toLowerCase().includes(q);
    return idMatch || descMatch || tipoMatch;
  });
})

onMounted(() => {
  fetchTasks()
  connectWebSocket(emailUtente.value, ruoloUtente.value)
})
</script>

<template>
  <div class="dashboard-layout">

    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo-icon">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"></path></svg>
        </div>
        <h2>WMS Worker</h2>
      </div>

      <nav class="sidebar-nav">
        <div class="nav-item" @click="router.push('/DipendenteHome')">
          <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"></path></svg>
          Il Mio Turno
        </div>
        <div class="nav-item active">
          <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"></path></svg>
          I Miei Task
        </div>
        <div class="nav-item">
          <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7"></path></svg>
          Mappa
        </div>
      </nav>

      <div class="sidebar-footer">
        <button @click="logout" class="btn-logout">
          <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path></svg>
          Disconnetti
        </button>
      </div>
    </aside>

    <main class="main-content">
      <header class="topbar">
        <div class="topbar-left">
          <span class="greeting">Riepilogo Operativo</span>
          <h1>Gestione Task</h1>
        </div>

        <div class="topbar-right">
          <div class="search-container" style="position: relative; width: 300px;">
            <svg style="position: absolute; left: 12px; top: 50%; transform: translateY(-50%); width: 18px; height: 18px; color: #94a3b8;" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
            </svg>
            <input
                type="text"
                v-model="searchQuery"
                placeholder="Cerca nei tuoi task..."
                style="width: 100%; box-sizing: border-box; padding: 10px 12px 10px 40px; border-radius: 8px; border: 1px solid #cbd5e1; font-size: 14px; outline: none; transition: all 0.2s; box-shadow: inset 0 1px 2px rgba(0,0,0,0.02);"
                onfocus="this.style.borderColor='#3b82f6'; this.style.boxShadow='0 0 0 3px rgba(59, 130, 246, 0.1)';"
                onblur="this.style.borderColor='#cbd5e1'; this.style.boxShadow='inset 0 1px 2px rgba(0,0,0,0.02)';"
            />
          </div>
        </div>
      </header>

      <div class="content-area">

        <div class="section-header">
          <h3>Task Attivi</h3>
          <span class="task-count">{{ taskAttiviFiltrati.length }} risultati</span>
        </div>

        <div class="task-grid mb-8" v-if="taskAttiviFiltrati.length > 0">
          <div v-for="task in taskAttiviFiltrati" :key="task.id" class="task-card" :class="task.statoTask === 'IN_CARICO' ? 'priority-high' : 'priority-normal'">
            <div class="task-header">
              <span class="task-type" :class="task.tipoTask === 'PRELIEVO' ? 'pickup' : 'dropoff'">{{ task.tipoTask }}</span>
              <span class="task-id">#TSK-{{ task.id }}</span>
            </div>
            <h3 class="item-name">{{ task.descrizione }}</h3>
            <div class="location-box">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
              <span>Posizione da definire</span>
            </div>
            <div class="qty-box">Quantità: <strong>{{ task.quantita }} unità</strong></div>
            <div class="status-indicator mt-4" :class="task.statoTask === 'IN_CARICO' ? 'text-blue' : 'text-gray'">
              Stato: <strong>{{ task.statoTask.replace('_', ' ') }}</strong>
            </div>
          </div>
        </div>

        <div v-else class="empty-state-modern mb-8">
          <p v-if="searchQuery">Nessun task attivo trovato per "<strong>{{ searchQuery }}</strong>"</p>
          <p v-else>Nessun task attivo al momento.</p>
        </div>

        <hr class="divider" />

        <div class="section-header">
          <h3>Storico Completati</h3>
          <span class="task-count completed-count">{{ taskCompletatiFiltrati.length }} risultati</span>
        </div>

        <div class="task-grid" v-if="taskCompletatiFiltrati.length > 0">
          <div v-for="task in taskCompletatiFiltrati" :key="task.id" class="task-card completed-card">
            <div class="task-header">
              <span class="badge-success">COMPLETATO</span>
              <span class="task-id">#TSK-{{ task.id }}</span>
            </div>
            <h3 class="item-name">{{ task.descrizione }}</h3>
            <div class="qty-box">Quantità spostata: <strong>{{ task.quantita }} unità</strong></div>
          </div>
        </div>

        <div v-else class="empty-state-modern">
          <p v-if="searchQuery">Nessun task completato trovato per "<strong>{{ searchQuery }}</strong>"</p>
          <p v-else>Il tuo storico è vuoto.</p>
        </div>

      </div>
    </main>
  </div>
</template>
<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

/* Manteniamo gli stili base della DipendenteHome */
.dashboard-layout { display: flex; flex-direction: row; height: 100vh; width: 100vw; background-color: #f8fafc; margin: 0; font-family: 'Inter', sans-serif; color: #334155; overflow: hidden; }
.main-content { flex-grow: 1; display: flex; flex-direction: column; overflow-y: auto; height: 100vh; }
.sidebar { width: 260px; background-color: #0f172a; color: #94a3b8; display: flex; flex-direction: column; border-right: 1px solid #1e293b; }
.sidebar-header { padding: 30px 24px; display: flex; align-items: center; gap: 12px; }
.logo-icon svg { width: 28px; height: 28px; color: #10b981; }
.sidebar-header h2 { margin: 0; font-size: 20px; font-weight: 700; color: #f8fafc; letter-spacing: -0.5px; }
.sidebar-nav { flex-grow: 1; padding: 0 16px; display: flex; flex-direction: column; gap: 8px; }
.nav-item { padding: 12px 16px; border-radius: 12px; cursor: pointer; font-size: 14px; font-weight: 500; display: flex; align-items: center; gap: 12px; transition: all 0.2s ease; }
.nav-icon { width: 20px; height: 20px; opacity: 0.7; }
.nav-item:hover { background-color: #1e293b; color: #f8fafc; }
.nav-item.active { background-color: rgba(16, 185, 129, 0.15); color: #10b981; font-weight: 600; }
.nav-item.active .nav-icon { opacity: 1; }
.sidebar-footer { padding: 24px 16px; }
.btn-logout { width: 100%; padding: 12px; background-color: transparent; color: #94a3b8; border: 1px solid #1e293b; border-radius: 12px; cursor: pointer; font-weight: 500; display: flex; align-items: center; justify-content: center; gap: 8px; transition: all 0.2s; }
.btn-logout:hover { background-color: #ef4444; color: white; border-color: #ef4444; }

.topbar { background-color: #ffffff; padding: 24px 40px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #e2e8f0; }
.greeting { font-size: 14px; color: #64748b; font-weight: 500; }
.topbar h1 { margin: 4px 0 0 0; font-size: 24px; font-weight: 700; color: #0f172a; letter-spacing: -0.5px; }
.content-area { padding: 40px; max-width: 1200px; margin: 0 auto; width: 100%; box-sizing: border-box; }

.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.section-header h3 { margin: 0; font-size: 18px; color: #0f172a; }
.task-count { background: #e0e7ff; color: #4f46e5; padding: 4px 12px; border-radius: 12px; font-size: 13px; font-weight: 600; }
.completed-count { background: #f1f5f9; color: #64748b; }

.task-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 24px; }
.task-card { background: white; border: 1px solid #e2e8f0; border-radius: 16px; padding: 24px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02); position: relative; overflow: hidden; }
.task-card::before { content: ''; position: absolute; top: 0; left: 0; width: 4px; height: 100%; }
.priority-high::before { background-color: #3b82f6; }
.priority-normal::before { background-color: #e2e8f0; }

.task-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.task-type { font-size: 12px; font-weight: 700; text-transform: uppercase; padding: 4px 10px; border-radius: 6px; }
.task-type.pickup { background: #fef2f2; color: #dc2626; }
.task-type.dropoff { background: #eff6ff; color: #2563eb; }
.task-id { color: #94a3b8; font-size: 13px; font-weight: 600; font-family: monospace; }

.item-name { margin: 0 0 16px 0; font-size: 18px; color: #0f172a; font-weight: 700; }
.location-box { display: flex; align-items: center; gap: 8px; background: #f8fafc; padding: 12px; border-radius: 8px; color: #334155; font-size: 14px; margin-bottom: 12px; border: 1px solid #e2e8f0; }
.location-box svg { width: 20px; height: 20px; color: #64748b; }
.qty-box { font-size: 15px; color: #475569; }

.text-blue { color: #2563eb; }
.text-gray { color: #64748b; }
.mt-4 { margin-top: 16px; }
.mb-8 { margin-bottom: 32px; }

/* Stili specifici per i completati */
.completed-card { background-color: #f8fafc; border-color: #e2e8f0; }
.completed-card::before { background-color: #10b981; }
.completed-card .item-name { color: #64748b; text-decoration: line-through; }
.badge-success { background: #d1fae5; color: #065f46; font-size: 12px; font-weight: 700; padding: 4px 10px; border-radius: 6px; }

.divider { border: 0; height: 1px; background: #e2e8f0; margin: 32px 0; }

.empty-state-modern { padding: 40px 0; color: #94a3b8; text-align: center; font-size: 15px; }
</style>