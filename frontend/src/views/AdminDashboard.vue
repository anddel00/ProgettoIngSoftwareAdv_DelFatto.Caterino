<script setup>
import { ref, onMounted, computed, watch } from 'vue' // Aggiunto watch
import { useRouter } from 'vue-router'
import api from '../api'

import AdminSidebar from '../components/AdminSidebar.vue'
import { useWmsWebSocket } from '../composables/useWmsWebSocket.js' // Il nostro cervello real-time

const router = useRouter()
const nomeUtente = ref(sessionStorage.getItem('nomeUtente') || 'Amministratore')

const tuttiTask = ref([])
const dipendentiInTurnoCount = ref(0) // Nuova variabile per il conteggio reale dei turni

// 1. ESTRAIAMO IL WEBSOCKET
const { ultimoTaskRicevuto } = useWmsWebSocket()

// 2. RECUPERO DATI INIZIALI
const fetchDashboardData = async () => {
  try {
    const response = await api.get('/api/tasks/tutti')
    tuttiTask.value = response.data.reverse() // Reverse per avere i più recenti in cima
  } catch (error) {
    console.error("Errore nel recupero dati dashboard:", error)
  }
}

const fetchTurniAttivi = async () => {
  try {
    const response = await api.get('/api/turni/attivi')
    dipendentiInTurnoCount.value = response.data.length
  } catch (error) {
    console.error("Errore nel recupero turni:", error)
  }
}

// 3. LA MAGIA DEL REAL-TIME
// Quando qualcuno sposta un pacco, la dashboard intercetta e aggiorna la lista.
// Le "computed" sottostanti ricalcoleranno tutto automaticamente!
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

// 4. COMPUTED PROPERTIES PER LE CARD E STATISTICHE
const conteggioTaskAttivi = computed(() => {
  return tuttiTask.value.filter(t => t.statoTask !== 'COMPLETATO').length
})

// Efficienza = (Task Completati / Totale Task) * 100
const efficienzaOdierna = computed(() => {
  if (tuttiTask.value.length === 0) return 0;
  const completati = tuttiTask.value.filter(t => t.statoTask === 'COMPLETATO').length
  return Math.round((completati / tuttiTask.value.length) * 100)
})

// Card 1: Ultimi Eventi (Gli ultimi 5 task COMPLETATI)
const ultimiCompletati = computed(() => {
  return tuttiTask.value
      .filter(t => t.statoTask === 'COMPLETATO')
      .slice(0, 5) // Prende solo i primi 5
})

// Card 2: Attività in Corso (Gli ultimi 5 task attualmente IN_CARICO)
const attivitaInCorso = computed(() => {
  return tuttiTask.value
      .filter(t => t.statoTask === 'IN_CARICO')
      .slice(0, 5)
})

onMounted(() => {
  fetchDashboardData()
  fetchTurniAttivi()
})

const vaiAGestioneTask = () => { router.push('/GestioneTask') }
const vaiAStorico = () => { router.push('/StoricoMovimenti') }
</script>

<template>
  <div class="dashboard-layout">
    <AdminSidebar />

    <main class="main-content">
      <header class="topbar">
        <div class="topbar-left">
          <span class="greeting">Bentornato,</span>
          <h1>{{ nomeUtente }}</h1>
        </div>
        <div class="user-profile">
          <div class="avatar">{{ nomeUtente.charAt(0) }}</div>
        </div>
      </header>

      <div class="content-area">

        <div class="stats-row">
          <div class="stat-card">
            <span class="stat-title">Task Attivi</span>
            <h2 class="stat-value">{{ conteggioTaskAttivi }}</h2>
          </div>
          <div class="stat-card">
            <span class="stat-title">Dipendenti in Turno</span>
            <h2 class="stat-value" style="color: #3b82f6">{{ dipendentiInTurnoCount }}</h2>
          </div>
          <div class="stat-card">
            <span class="stat-title">Efficienza Complessiva</span>
            <h2 class="stat-value" :style="{ color: efficienzaOdierna > 50 ? '#10b981' : '#f59e0b' }">
              {{ efficienzaOdierna }}%
            </h2>
          </div>
        </div>

        <div class="dashboard-cards">

          <div class="card" style="padding: 0; overflow: hidden;">
            <div class="card-header" style="padding: 24px; margin: 0; border-bottom: 1px solid #e2e8f0; background: #f8fafc;">
              <h3>Ultimi Eventi Completati</h3>
              <button @click="vaiAStorico" class="btn-ghost">Vedi storico</button>
            </div>

            <div v-if="ultimiCompletati.length > 0" class="inbound-list">
              <div v-for="task in ultimiCompletati" :key="task.id" class="inbound-item">
                <div class="inbound-info">
                  <div style="display: flex; align-items: center; gap: 8px;">
                    <span class="task-id">#TSK-{{ task.id }}</span>
                    <span class="badge" style="background: #d1fae5; color: #065f46; font-size: 10px; padding: 2px 6px;">COMPLETATO</span>
                  </div>
                  <p class="item-desc">{{ task.descrizione }}</p>
                  <span style="font-size: 12px; color: #64748b;">da: <strong>{{ task.nomeDipendente || 'Operatore' }}</strong></span>
                </div>
              </div>
            </div>

            <div v-else class="empty-state-modern">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path></svg>
              <p>Nessun task completato di recente.</p>
            </div>
          </div>

          <div class="card" style="padding: 0; overflow: hidden;">
            <div class="card-header" style="padding: 24px; margin: 0; border-bottom: 1px solid #e2e8f0; background: #f8fafc;">
              <h3>In Lavorazione Ora</h3>
              <button @click="vaiAGestioneTask" class="btn-ghost">Gestisci task</button>
            </div>

            <div v-if="attivitaInCorso.length > 0" class="inbound-list">
              <div v-for="task in attivitaInCorso" :key="task.id" class="inbound-item">
                <div class="inbound-info">
                  <div style="display: flex; align-items: center; gap: 8px;">
                    <span class="task-id">#TSK-{{ task.id }}</span>
                    <span class="badge" style="background: #dbeafe; color: #1e40af; font-size: 10px; padding: 2px 6px;">IN CARICO</span>
                  </div>
                  <p class="item-desc">{{ task.descrizione }}</p>
                </div>
                <div class="inbound-meta" style="flex-direction: column; align-items: flex-end; gap: 4px;">
                  <div style="display: flex; align-items: center; gap: 6px;">
                    <div style="width: 20px; height: 20px; background: #e2e8f0; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 10px; font-weight: bold;">
                      {{ task.nomeDipendente ? task.nomeDipendente.charAt(0) : 'O' }}
                    </div>
                    <span style="font-size: 12px; font-weight: 600;">{{ task.nomeDipendente || 'Operatore' }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div v-else class="empty-state-modern">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
              <p>Nessuno sta lavorando a un task in questo momento.</p>
            </div>
          </div>

        </div>
      </div>
    </main>
  </div>
</template>
<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');

/* ------- RESET E LAYOUT PRINCIPALE ------- */
.dashboard-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  background-color: #f8fafc; /* Grigio chiarissimo, quasi azzurro */
  font-family: 'Inter', sans-serif;
  margin: 0;
  color: #334155;
}

/* ------- SIDEBAR PREMIUM ------- */
.sidebar {
  width: 260px;
  background-color: #0f172a; /* Slate 900 - Molto più elegante del blu scuro base */
  color: #94a3b8;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #1e293b;
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
  color: #6366f1; /* Indigo moderno */
}

.sidebar-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #f8fafc;
  letter-spacing: -0.5px;
}

.sidebar-nav {
  flex-grow: 1;
  padding: 0 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-item {
  padding: 12px 16px;
  border-radius: 12px; /* Forma a pillola moderna */
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.2s ease;
}

.nav-icon {
  width: 20px;
  height: 20px;
  opacity: 0.7;
}

.nav-item:hover {
  background-color: #1e293b;
  color: #f8fafc;
}

.nav-item.active {
  background-color: rgba(99, 102, 241, 0.15); /* Sfondo indigo traslucido */
  color: #6366f1; /* Testo indigo */
  font-weight: 600;
}

.nav-item.active .nav-icon {
  opacity: 1;
}

.sidebar-footer {
  padding: 24px 16px;
}

.btn-logout {
  width: 100%;
  padding: 12px;
  background-color: transparent;
  color: #94a3b8;
  border: 1px solid #1e293b;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s;
}

.btn-logout:hover {
  background-color: #ef4444;
  color: white;
  border-color: #ef4444;
}

/* ------- CONTENUTO PRINCIPALE ------- */
.main-content {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.topbar {
  background-color: #ffffff;
  padding: 24px 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e2e8f0;
}

.greeting {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.topbar h1 {
  margin: 4px 0 0 0;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.5px;
}

.user-profile {
  display: flex;
  align-items: center;
}

.avatar {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #6366f1, #a855f7);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 18px;
  box-shadow: 0 4px 6px -1px rgba(99, 102, 241, 0.3);
}

.content-area {
  padding: 40px;
  max-width: 1200px;
}

/* ------- STATISTICHE RAPIDE ------- */
.stats-row {
  display: flex;
  gap: 24px;
  margin-bottom: 32px;
}

.stat-card {
  flex: 1;
  background: #ffffff;
  padding: 24px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02), 0 2px 4px -1px rgba(0, 0, 0, 0.02);
}

.stat-title {
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-value {
  margin: 12px 0 0 0;
  font-size: 32px;
  font-weight: 700;
  color: #0f172a;
}

/* ------- CARD DELLA DASHBOARD ------- */
.dashboard-cards {
  display: flex;
  gap: 24px;
}

.card {
  background: #ffffff;
  flex: 1;
  padding: 28px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02), 0 2px 4px -1px rgba(0, 0, 0, 0.02);
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.card h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
}

.btn-ghost {
  background: transparent;
  border: none;
  color: #6366f1;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 6px;
  transition: background 0.2s;
}

.btn-ghost:hover {
  background: rgba(99, 102, 241, 0.1);
}

.empty-state-modern {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-grow: 1;
  padding: 40px 0;
  color: #94a3b8;
}

.empty-state-modern svg {
  width: 48px;
  height: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state-modern p {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
}

.inbound-list { display: flex; flex-direction: column; }
.inbound-item { padding: 16px 24px; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; transition: background-color 0.2s; }
.inbound-item:hover { background-color: #f8fafc; }
.inbound-item:last-child { border-bottom: none; }
.inbound-info { display: flex; flex-direction: column; gap: 4px; }
.task-id { font-size: 12px; font-weight: 600; color: #94a3b8; font-family: monospace; }
.item-desc { margin: 0; font-size: 14px; font-weight: 600; color: #0f172a; }
.inbound-meta { display: flex; align-items: center; gap: 16px; }
.qty-badge { background: #f1f5f9; color: #475569; padding: 4px 8px; border-radius: 6px; font-size: 12px; font-weight: 700; }
.status-text { font-size: 12px; font-weight: 700; }
.text-gray { color: #64748b; }
.text-blue { color: #2563eb; }
</style>