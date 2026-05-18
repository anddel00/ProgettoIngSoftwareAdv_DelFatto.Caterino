<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

import AdminSidebar from '../components/AdminSidebar.vue'
import { useWmsWebSocket } from '../composables/useWmsWebSocket.js'

const router = useRouter()
const nomeUtente = ref(sessionStorage.getItem('nomeUtente') || 'Amministratore')

const tuttiTask = ref([])
const dipendentiInTurnoCount = ref(0)
const isSimulando = ref(false)

// NUOVA VARIABILE: Controlla quanti lotti generare (default 5)
const numeroDaSimulare = ref(5)

// 1. ESTRAIAMO IL WEBSOCKET
const { ultimoTaskRicevuto } = useWmsWebSocket()

// 2. RECUPERO DATI INIZIALI
const fetchDashboardData = async () => {
  try {
    const response = await api.get('/api/tasks/tutti')
    tuttiTask.value = response.data.reverse()
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

// FUNZIONE AGGIORNATA: Usa il valore dell'input numerico
const simulaNuoviArrivi = async () => {
  if (numeroDaSimulare.value < 1) {
    alert("Inserisci un numero valido maggiore di 0")
    return
  }
  
  isSimulando.value = true
  try {
    // Invia il valore dinamico al backend
    await api.post('/api/batch-prodotti/simulaArrivi', numeroDaSimulare.value, {
      headers: { 'Content-Type': 'application/json' }
    })
    alert(`📦 Simulazione completata! ${numeroDaSimulare.value} nuovi lotti generati.`)
  } catch (error) {
    console.error("Errore nella simulazione:", error)
    alert("❌ Errore durante la simulazione degli arrivi.")
  } finally {
    isSimulando.value = false
  }
}

// 4. COMPUTED PROPERTIES PER LE CARD E STATISTICHE
const conteggioTaskAttivi = computed(() => {
  return tuttiTask.value.filter(t => t.statoTask !== 'COMPLETATO').length
})

const efficienzaOdierna = computed(() => {
  if (tuttiTask.value.length === 0) return 0;
  const completati = tuttiTask.value.filter(t => t.statoTask === 'COMPLETATO').length
  return Math.round((completati / tuttiTask.value.length) * 100)
})

const ultimiCompletati = computed(() => {
  return tuttiTask.value
      .filter(t => t.statoTask === 'COMPLETATO')
      .slice(0, 5)
})

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
  <div class="glass-dashboard-layout">
    <div class="glass-bg-blob blob-1"></div>
    <div class="glass-bg-blob blob-2"></div>
    <div class="glass-bg-blob blob-3"></div>

    <AdminSidebar class="glass-sidebar" />

    <main class="main-content">
      <header class="glass-topbar">
        <div class="topbar-left">
          <span class="greeting">Bentornato,</span>
          <h1>{{ nomeUtente }}</h1>
        </div>
        
        <div class="user-profile" style="display: flex; gap: 20px; align-items: center;">
          
          <div class="simulation-controls">
            <input 
              type="number" 
              v-model.number="numeroDaSimulare" 
              min="1"
              max="100"
              class="glass-input-number"
              title="Numero lotti da generare"
            />
            <button 
              @click="simulaNuoviArrivi" 
              class="btn-glass-primary" 
              :disabled="isSimulando"
            >
              {{ isSimulando ? 'Generazione...' : '+ Simula Arrivi' }}
            </button>
          </div>

          <div class="avatar">{{ nomeUtente.charAt(0) }}</div>
        </div>
      </header>

      <div class="content-area">

        <div class="stats-row">
          <div class="glass-card stat-card">
            <div class="stat-icon-wrapper blue-glow">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"></path></svg>
            </div>
            <div class="stat-info">
              <span class="stat-title">Task Attivi</span>
              <h2 class="stat-value">{{ conteggioTaskAttivi }}</h2>
            </div>
          </div>

          <div class="glass-card stat-card">
            <div class="stat-icon-wrapper purple-glow">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>
            </div>
            <div class="stat-info">
              <span class="stat-title">Dipendenti in Turno</span>
              <h2 class="stat-value">{{ dipendentiInTurnoCount }}</h2>
            </div>
          </div>

          <div class="glass-card stat-card">
            <div class="stat-icon-wrapper" :class="efficienzaOdierna > 50 ? 'green-glow' : 'orange-glow'">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path></svg>
            </div>
            <div class="stat-info">
              <span class="stat-title">Efficienza</span>
              <h2 class="stat-value">{{ efficienzaOdierna }}%</h2>
            </div>
          </div>
        </div>

        <div class="dashboard-cards">

          <div class="glass-card list-card">
            <div class="card-header">
              <h3>Ultimi Completati</h3>
              <button @click="vaiAStorico" class="btn-glass-ghost">Storico</button>
            </div>

            <div v-if="ultimiCompletati.length > 0" class="task-list">
              <div v-for="task in ultimiCompletati" :key="task.id" class="task-item glass-item">
                <div class="task-info">
                  <div class="task-header">
                    <span class="task-id">#TSK-{{ task.id }}</span>
                    <span class="glass-badge badge-success">COMPLETATO</span>
                  </div>
                  <p class="item-desc">{{ task.descrizione }}</p>
                  <span class="operator-name">Da: <strong>{{ task.nomeDipendente || 'Operatore' }}</strong></span>
                </div>
              </div>
            </div>

            <div v-else class="empty-state-glass">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path></svg>
              <p>Nessun task completato.</p>
            </div>
          </div>

          <div class="glass-card list-card">
            <div class="card-header">
              <h3>In Lavorazione Ora</h3>
              <button @click="vaiAGestioneTask" class="btn-glass-ghost">Gestisci</button>
            </div>

            <div v-if="attivitaInCorso.length > 0" class="task-list">
              <div v-for="task in attivitaInCorso" :key="task.id" class="task-item glass-item">
                <div class="task-info">
                  <div class="task-header">
                    <span class="task-id">#TSK-{{ task.id }}</span>
                    <span class="glass-badge badge-active">IN CARICO</span>
                  </div>
                  <p class="item-desc">{{ task.descrizione }}</p>
                </div>
                <div class="task-meta">
                  <div class="operator-chip">
                    <div class="mini-avatar">{{ task.nomeDipendente ? task.nomeDipendente.charAt(0) : 'O' }}</div>
                    <span>{{ task.nomeDipendente || 'Operatore' }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div v-else class="empty-state-glass">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
              <p>Nessuna attività in corso.</p>
            </div>
          </div>

        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');

/* ------- LAYOUT & BACKGROUND ANIMATO ------- */
.glass-dashboard-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  background-color: #e2e8f0;
  font-family: 'Inter', sans-serif;
  margin: 0;
  color: #1e293b;
  position: relative;
  overflow: hidden;
}

.glass-bg-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  z-index: 0;
  opacity: 0.6;
}

.blob-1 {
  top: -10%; left: -10%;
  width: 500px; height: 500px;
  background: #93c5fd; 
}

.blob-2 {
  bottom: -20%; right: -10%;
  width: 600px; height: 600px;
  background: #c4b5fd; 
}

.blob-3 {
  top: 40%; left: 40%;
  width: 400px; height: 400px;
  background: #86efac; 
  opacity: 0.4;
}

.main-content, .glass-sidebar {
  position: relative;
  z-index: 10;
}

/* ------- IL VETRO (MIXIN) ------- */
.glass-card {
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.1);
  border-radius: 20px;
}

/* ------- TOPBAR ------- */
.main-content {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.glass-topbar {
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  padding: 20px 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
}

.greeting { font-size: 14px; color: #475569; font-weight: 500; }
.topbar-left h1 { margin: 4px 0 0 0; font-size: 24px; font-weight: 700; color: #0f172a; letter-spacing: -0.5px; }

.avatar {
  width: 44px; height: 44px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.8), rgba(168, 85, 247, 0.8));
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255,255,255,0.5);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: white; font-weight: 600; font-size: 18px;
  box-shadow: 0 4px 10px rgba(99, 102, 241, 0.3);
}

/* ------- CONTROLLI SIMULAZIONE (NUOVI) ------- */
.simulation-controls {
  display: flex;
  gap: 8px;
  align-items: center;
  background: rgba(255, 255, 255, 0.3);
  padding: 6px;
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.glass-input-number {
  width: 50px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.5);
  color: #0f172a;
  font-family: 'Inter', sans-serif;
  font-weight: 600;
  font-size: 14px;
  padding: 8px 6px;
  border-radius: 10px;
  outline: none;
  text-align: center;
  transition: all 0.2s ease;
}

.glass-input-number:focus {
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(99, 102, 241, 0.6);
  box-shadow: 0 0 10px rgba(99, 102, 241, 0.2);
}

.btn-glass-primary {
  background: rgba(99, 102, 241, 0.8);
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  color: white;
  font-family: 'Inter', sans-serif;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 10px;
  transition: all 0.2s ease;
  box-shadow: 0 4px 10px rgba(99, 102, 241, 0.2);
}

.btn-glass-primary:hover:not(:disabled) {
  background: rgba(99, 102, 241, 1);
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(99, 102, 241, 0.4);
}

.btn-glass-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.content-area { padding: 40px; max-width: 1200px; margin: 0 auto; width: 100%; box-sizing: border-box;}

/* ------- STATISTICHE ------- */
.stats-row { display: flex; gap: 24px; margin-bottom: 32px; }

.stat-card {
  flex: 1; padding: 24px;
  display: flex; align-items: center; gap: 20px;
  transition: transform 0.2s;
}
.stat-card:hover { transform: translateY(-5px); }

.stat-icon-wrapper {
  width: 56px; height: 56px;
  border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255,255,255,0.5);
}
.stat-icon-wrapper svg { width: 28px; height: 28px; }

.blue-glow { color: #3b82f6; box-shadow: 0 0 20px rgba(59, 130, 246, 0.2); }
.purple-glow { color: #a855f7; box-shadow: 0 0 20px rgba(168, 85, 247, 0.2); }
.green-glow { color: #10b981; box-shadow: 0 0 20px rgba(16, 185, 129, 0.2); }
.orange-glow { color: #f59e0b; box-shadow: 0 0 20px rgba(245, 158, 11, 0.2); }

.stat-info { display: flex; flex-direction: column; }
.stat-title { font-size: 13px; font-weight: 600; color: #475569; text-transform: uppercase; letter-spacing: 0.5px; }
.stat-value { margin: 4px 0 0 0; font-size: 32px; font-weight: 800; color: #0f172a; }

/* ------- CARD LISTE ------- */
.dashboard-cards { display: flex; gap: 24px; }
.list-card { flex: 1; display: flex; flex-direction: column; overflow: hidden; padding: 0;}

.card-header {
  padding: 24px;
  display: flex; justify-content: space-between; align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.4);
}
.card-header h3 { margin: 0; font-size: 18px; font-weight: 700; color: #0f172a; }

.btn-glass-ghost {
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.6);
  color: #475569; font-weight: 600; font-size: 13px;
  cursor: pointer; padding: 6px 14px; border-radius: 8px;
  transition: all 0.2s;
}
.btn-glass-ghost:hover { background: rgba(255, 255, 255, 0.8); color: #0f172a; transform: translateY(-1px);}

.task-list { display: flex; flex-direction: column; }
.glass-item {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  display: flex; justify-content: space-between; align-items: center;
  transition: background 0.2s;
}
.glass-item:hover { background: rgba(255, 255, 255, 0.3); }
.glass-item:last-child { border-bottom: none; }

.task-info { display: flex; flex-direction: column; gap: 6px; }
.task-header { display: flex; align-items: center; gap: 10px; }
.task-id { font-size: 13px; font-weight: 700; color: #64748b; font-family: monospace; }
.item-desc { margin: 0; font-size: 15px; font-weight: 600; color: #1e293b; }
.operator-name { font-size: 12px; color: #64748b; }

.glass-badge {
  font-size: 10px; font-weight: 800; padding: 4px 8px; border-radius: 6px;
  backdrop-filter: blur(4px); border: 1px solid rgba(255,255,255,0.4);
}
.badge-success { background: rgba(16, 185, 129, 0.2); color: #065f46; }
.badge-active { background: rgba(59, 130, 246, 0.2); color: #1e40af; }

.operator-chip {
  display: flex; align-items: center; gap: 8px;
  background: rgba(255, 255, 255, 0.5); padding: 4px 12px 4px 4px;
  border-radius: 20px; border: 1px solid rgba(255,255,255,0.4);
}
.mini-avatar {
  width: 24px; height: 24px; background: #cbd5e1; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: bold; color: #334155;
}
.operator-chip span { font-size: 13px; font-weight: 600; color: #334155; }

.empty-state-glass {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 60px 0; color: #64748b;
}
.empty-state-glass svg { width: 48px; height: 48px; margin-bottom: 16px; opacity: 0.4; }
.empty-state-glass p { margin: 0; font-size: 15px; font-weight: 500; }
</style>