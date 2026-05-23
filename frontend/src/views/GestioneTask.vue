<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'
import AdminSidebar from '../components/AdminSidebar.vue'
import { useWmsWebSocket } from '../composables/useWmsWebSocket.js'

const { ultimoTaskRicevuto } = useWmsWebSocket()
const router = useRouter()
const nomeUtente = ref(sessionStorage.getItem('nomeUtente') || 'Admin')

const tuttiTask  = ref([])
const dipendenti = ref([])
const mostraModale   = ref(false)
const errorMessage   = ref('')
const successMessage = ref('')

const form = ref({
  descrizione:     '',
  tipoTask:        'PRELIEVO',
  quantita:        1,
  emailDipendente: ''
})

// ==========================================
// CARICA DATI
// ==========================================
const fetchTuttiTask = async () => {
  try {
    const r = await api.get('/api/tasks/tutti')
    tuttiTask.value = r.data
  } catch (e) { console.error(e) }
}

const fetchDipendenti = async () => {
  try {
    const response = await api.get('/api/turni/attivi')
    dipendenti.value = response.data.filter(u => u.ruolo === 'Dipendente' || u.ruolo === 'DIPENDENTE')
  } catch (error) {
    console.error("Errore nel recupero dei dipendenti:", error)
  }
}

watch(ultimoTaskRicevuto, (nuovoTask) => {
  if (nuovoTask) {
    const index = tuttiTask.value.findIndex(t => t.id === nuovoTask.id)
    if (index !== -1) {
      tuttiTask.value[index] = nuovoTask
      tuttiTask.value = [...tuttiTask.value]
    } else {
      tuttiTask.value.unshift(nuovoTask)
    }
  }
})

onMounted(() => {
  fetchTuttiTask()
  fetchDipendenti()
})

// ==========================================
// STATISTICHE E FILTRI
// ==========================================
const taskDaFare    = computed(() => tuttiTask.value.filter(t => t.statoTask === 'DA_FARE').length)
const taskInCarico  = computed(() => tuttiTask.value.filter(t => t.statoTask === 'IN_CARICO').length)
const taskCompletati = computed(() => tuttiTask.value.filter(t => t.statoTask === 'COMPLETATO').length)

// --- RICERCA LATO CLIENT ---
const searchQuery = ref('')
const filtroTipo = ref('TUTTI')

const taskAttiviLista = computed(() => tuttiTask.value.filter(t => t.statoTask !== 'COMPLETATO'))

const taskFiltrati = computed(() => {
  const q = searchQuery.value.toLowerCase();
  return taskAttiviLista.value.filter(task => {
    let matchesTipo = false;
    if (filtroTipo.value === 'TUTTI') matchesTipo = true;
    else if (filtroTipo.value === 'USCITA/ORDINI') matchesTipo = (task.idMissione != null || task.tipoTask === 'USCITA');
    else matchesTipo = (task.tipoTask === filtroTipo.value);

    const matchesQuery = task.id.toString().includes(q) ||
        task.descrizione.toLowerCase().includes(q) ||
        task.statoTask.toLowerCase().includes(q) ||
        (task.nomeDipendente && task.nomeDipendente.toLowerCase().includes(q));

    return matchesTipo && matchesQuery;
  });
});

const taskFiltratiRaggruppati = computed(() => {
  const result = {};
  taskFiltrati.value.forEach(task => {
    const key = task.idMissione || 'Task Singoli';
    if (!result[key]) result[key] = [];
    result[key].push(task);
  });
  return result;
});

// ==========================================
// MODALE E AZIONI
// ==========================================
const apriModale = async () => {
  await fetchDipendenti()
  form.value = { descrizione: '', tipoTask: 'PRELIEVO', quantita: 1, emailDipendente: '' }
  errorMessage.value = ''
  successMessage.value = ''
  mostraModale.value = true
}
const chiudiModale = () => { mostraModale.value = false }

const creaTask = async () => {
  errorMessage.value   = ''
  successMessage.value = ''

  if (!form.value.descrizione || !form.value.emailDipendente) {
    errorMessage.value = 'Descrizione e dipendente sono obbligatori.'
    return
  }

  try {
    await api.post('/api/tasks/crea-e-assegna', {
      descrizione:     form.value.descrizione,
      tipoTask:        form.value.tipoTask,
      quantita:        form.value.quantita,
      emailDipendente: form.value.emailDipendente,
      vecchiaX: 0, vecchiaY: 0, nuovaX: 0, nuovaY: 0
    })
    successMessage.value = 'Task creato e assegnato con successo!'
    await fetchTuttiTask()
    setTimeout(chiudiModale, 1200)
  } catch (e) {
    errorMessage.value = e.response?.data || 'Errore durante la creazione del task.'
  }
}

const annullaTask = async (taskId) => {
  if (confirm(`Sei sicuro di voler annullare il task #${taskId}? Questa operazione non può essere annullata.`)) {
    try {
      await api.delete(`/api/tasks/${taskId}/annulla`)
      // Rimuovi localmente per evitare un ricaricamento completo o aspetta il websocket
      tuttiTask.value = tuttiTask.value.filter(t => t.id !== taskId)
      // Per sicurezza ricarichiamo (in caso il WS non sia istantaneo)
      await fetchTuttiTask()
    } catch (e) {
      alert(e.response?.data || "Errore durante l'annullamento del task.")
    }
  }
}

const getStatoClass = (stato) => {
  if (stato === 'DA_FARE')   return 'stato-da-fare'
  if (stato === 'IN_CARICO') return 'stato-in-carico'
  if (stato === 'COMPLETATO') return 'stato-completato'
  return ''
}

// Helper: costruisce il label leggibile di uno slot da idScaffale + coordinate
const labelScaffale = (idScaffale, y, x, z) => {
  if (!idScaffale) return null
  return `S${idScaffale} · R${y + 1} C${x + 1} P${z + 1}`
}

// Percorso contestuale per tipo di task
const getPercorso = (task) => {
  const inizio = labelScaffale(task.idScaffaleInizio, task.vecchiaY, task.vecchiaX, task.vecchiaZ)
  const fine   = labelScaffale(task.idScaffaleFine,   task.nuovaY,  task.nuovaX,  task.nuovaZ)
  if (task.tipoTask === 'SPOSTAMENTO') return { da: inizio || '—', a: fine || '—', tipo: 'sposta' }
  if (task.tipoTask === 'PRELIEVO')    return { da: inizio || '—', a: 'In attesa', tipo: 'preleva' }
  if (task.tipoTask === 'USCITA')      return { da: inizio || '—', a: 'In uscita', tipo: 'preleva' }
  if (task.tipoTask === 'DEPOSITO')    return { da: 'In attesa', a: fine || '—',   tipo: 'deposita' }
  return { da: '—', a: '—', tipo: '' }
}
</script>

<template>
  <div class="glass-dashboard-layout">
    <div class="glass-bg-blob blob-1"></div>
    <div class="glass-bg-blob blob-2"></div>
    <div class="glass-bg-blob blob-3"></div>

    <AdminSidebar />

    <main class="main-content">
      <header class="glass-topbar">
        <div class="topbar-left">
          <span class="greeting">Gestione Operativa</span>
          <h1>Assegnazione Task</h1>
        </div>
        <div class="topbar-right">
          <div class="user-profile">
            <div class="avatar">{{ nomeUtente.charAt(0) }}</div>
            <span>{{ nomeUtente }}</span>
          </div>
          <button @click="apriModale" class="btn-primary-glass">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
            </svg>
            Nuovo Task
          </button>
        </div>
      </header>

      <div class="content-area">

        <div class="stats-row">
          <div class="glass-card stat-card interactive-card">
            <span class="stat-title">Da Fare</span>
            <h2 class="stat-value text-slate">{{ taskDaFare }}</h2>
          </div>
          <div class="glass-card stat-card interactive-card">
            <span class="stat-title">In Carico</span>
            <h2 class="stat-value text-blue">{{ taskInCarico }}</h2>
          </div>
          <div class="glass-card stat-card interactive-card">
            <span class="stat-title">Completati</span>
            <h2 class="stat-value text-green">{{ taskCompletati }}</h2>
          </div>
          <div class="glass-card stat-card interactive-card">
            <span class="stat-title">Totale Task</span>
            <h2 class="stat-value text-dark">{{ tuttiTask.length }}</h2>
          </div>
        </div>

        <div class="glass-card table-card">
          <div class="card-header">
            <div class="header-section left">
              <h3>Task Attivi</h3>
              <span class="badge glass-badge">{{ taskFiltrati.length }} risultati</span>
            </div>

            <div class="header-section center">
              <div class="filter-group">
                <button v-for="tipo in ['TUTTI', 'USCITA/ORDINI', 'PRELIEVO', 'SPOSTAMENTO', 'DEPOSITO']"
                        :key="tipo"
                        @click="filtroTipo = tipo"
                        :disabled="filtroTipo === tipo"
                        :class="['filter-btn', { 'active': filtroTipo === tipo }]">
                  {{ tipo }}
                </button>
              </div>
            </div>

            <div class="header-section right">
              <div class="search-wrapper">
                <svg class="search-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
                </svg>
                <input type="text"
                       v-model="searchQuery"
                       class="search-input glass-input"
                       placeholder="Cerca task..." />
              </div>
            </div>
          </div>

          <div class="table-responsive" v-if="taskFiltrati.length > 0">
            <table class="glass-table">
              <thead>
              <tr>
                <th>ID</th><th>Tipo</th><th>Qta</th><th>Percorso</th><th>Reparto</th><th>Assegnato a</th><th>Stato</th><th class="text-right">Azioni</th>
              </tr>
              </thead>
              <tbody v-for="(tasks, idMissione) in taskFiltratiRaggruppati" :key="idMissione">
                <!-- Intestazione Missione se non sono task singoli -->
                <tr v-if="idMissione !== 'Task Singoli'" class="mission-group-header">
                  <td colspan="8">
                    <div class="mission-header-content">
                      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path></svg>
                      <strong>Missione: {{ idMissione }}</strong>
                      <span class="mission-operator"> — Assegnata a: <strong>{{ tasks[0].nomeDipendente || 'Operatore' }}</strong></span>
                    </div>
                  </td>
                </tr>
                <!-- Task effettivi -->
                <tr v-for="task in tasks" :key="task.id" :class="{'nested-row': idMissione !== 'Task Singoli'}">
                  <td class="id-cell">#TSK-{{ task.id }}</td>
                  <td>
                      <span class="task-type" :class="['PRELIEVO', 'USCITA'].includes(task.tipoTask) ? 'pickup' : task.tipoTask === 'DEPOSITO' ? 'dropoff' : 'move'">
                        {{ task.tipoTask }}
                      </span>
                  </td>
                  <td><strong>{{ task.quantita }}</strong></td>

                  <!-- COLONNA PERCORSO -->
                  <td class="route-cell">
                    <div class="route-wrap" :class="'route-' + getPercorso(task).tipo">
                      <span class="route-slot route-from">{{ getPercorso(task).da }}</span>
                      <svg class="route-arrow" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 8l4 4m0 0l-4 4m4-4H3"/></svg>
                      <span class="route-slot route-to" :class="{'route-attesa': getPercorso(task).a === 'In attesa' || getPercorso(task).da === 'In attesa'}">{{ getPercorso(task).a }}</span>
                    </div>
                  </td>

                  <!-- COLONNA REPARTO -->
                  <td>
                    <span v-if="task.nomeReparto" class="reparto-badge">{{ task.nomeReparto }}</span>
                    <span v-else class="text-muted">&mdash;</span>
                  </td>

                  <td class="operator-cell">
                    <span v-if="idMissione !== 'Task Singoli'" class="text-muted" style="opacity: 0.5;">↳ Gruppo</span>
                    <span v-else>
                      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>
                      {{ task.nomeDipendente }}
                    </span>
                  </td>
                  <td>
                      <span class="stato-badge" :class="getStatoClass(task.statoTask)">
                        {{ task.statoTask?.replace('_', ' ') }}
                      </span>
                  </td>
                  <td class="action-cell">
                    <button v-if="task.statoTask !== 'COMPLETATO' && task.statoTask !== 'ANNULLATO'" 
                            @click="annullaTask(task.id)" 
                            class="btn-delete-glass" title="Annulla Task">
                      <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-else class="empty-state-glass">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
            </svg>
            <p v-if="searchQuery">Nessun task corrisponde alla ricerca "<strong>{{ searchQuery }}</strong>"</p>
            <p v-else>Nessun task attivo presente. Creane uno con il pulsante "Nuovo Task".</p>
          </div>
        </div>

      </div>
    </main>
  </div>

  <div v-if="mostraModale" class="modal-overlay">
    <div class="glass-modal">
      <div class="modal-header">
        <h2>Crea e Assegna Task</h2>
        <button @click="chiudiModale" class="btn-close-glass">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
          </svg>
        </button>
      </div>

      <div class="form-body">
        <div class="form-group">
          <label>Descrizione del Task</label>
          <input v-model="form.descrizione" type="text" class="glass-input" placeholder="Es: Prelievo pallet zona A3..." />
        </div>
        <div class="form-row">
          <div class="form-group half">
            <label>Tipo Operazione</label>
            <select v-model="form.tipoTask" class="glass-input">
              <option value="PRELIEVO">PRELIEVO</option>
              <option value="DEPOSITO">DEPOSITO</option>
              <option value="SPOSTAMENTO">SPOSTAMENTO</option>
            </select>
          </div>
          <div class="form-group half">
            <label>Quantità (unità)</label>
            <input v-model.number="form.quantita" type="number" min="1" class="glass-input" />
          </div>
        </div>

        <div class="form-group">
          <label>Assegna a Dipendente</label>
          <select v-model="form.emailDipendente" class="glass-input">
            <option value="" disabled>Seleziona un dipendente in turno...</option>
            <option v-for="dip in dipendenti" :key="dip.id" :value="dip.email">
              {{ dip.nome }} {{ dip.cognome }} — {{ dip.email }}
            </option>
          </select>
          <p v-if="dipendenti.length === 0" class="hint-warning">
            Nessun dipendente ha un turno attivo al momento.
          </p>
        </div>

        <div v-if="errorMessage" class="error-banner">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
          {{ errorMessage }}
        </div>
        <div v-if="successMessage" class="success-banner">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
          {{ successMessage }}
        </div>
      </div>

      <div class="modal-actions">
        <button @click="chiudiModale" class="btn-ghost">Annulla</button>
        <button @click="creaTask" class="btn-success-glass" :disabled="dipendenti.length === 0">
          Crea e Assegna
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');

/* ------- LAYOUT & BACKGROUND CHIARO (Uniformato) ------- */
.glass-dashboard-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  background-color: #e2e8f0; /* Il grigio/azzurro chiaro base */
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

/* Colori dei blob ripresi dalla AdminDashboard chiara */
.blob-1 { top: -10%; left: -10%; width: 500px; height: 500px; background: #93c5fd; }
.blob-2 { bottom: -20%; right: -10%; width: 600px; height: 600px; background: #c4b5fd; }
.blob-3 { top: 40%; left: 40%; width: 400px; height: 400px; background: #86efac; opacity: 0.4; }

.main-content {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  z-index: 10;
}

/* ------- IL VETRO CHIARO ------- */
.glass-card {
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.1);
  border-radius: 20px;
}

.interactive-card { transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); }
.interactive-card:hover {
  transform: translateY(-5px);
  background: rgba(255, 255, 255, 0.85);
  box-shadow: 0 20px 40px -12px rgba(0, 0, 0, 0.15);
}

/* ------- TOPBAR CHIARA ------- */
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

.topbar-right { display: flex; align-items: center; gap: 24px; }
.greeting { font-size: 14px; color: #64748b; font-weight: 500; }
.topbar-left h1 { margin: 4px 0 0 0; font-size: 24px; font-weight: 700; color: #0f172a; letter-spacing: -0.5px; }

.user-profile { display: flex; align-items: center; gap: 12px; font-weight: 600; color: #0f172a; }
.avatar {
  width: 44px; height: 44px;
  background: linear-gradient(135deg, #6366f1, #a855f7);
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  color: white; font-weight: 600; font-size: 18px;
  box-shadow: 0 4px 10px rgba(99, 102, 241, 0.3);
}

.btn-primary-glass {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white; border: none; padding: 10px 20px; border-radius: 12px;
  cursor: pointer; font-weight: 600; font-size: 14px;
  display: flex; align-items: center; gap: 8px;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(99, 102, 241, 0.3);
}
.btn-primary-glass svg { width: 18px; height: 18px; }
.btn-primary-glass:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(99, 102, 241, 0.4); }

.content-area { padding: 40px; max-width: 1200px; margin: 0 auto; width: 100%; box-sizing: border-box;}

/* ------- STATISTICHE ------- */
.stats-row { display: flex; gap: 24px; margin-bottom: 32px; }
.stat-card { flex: 1; padding: 24px; display: flex; flex-direction: column; justify-content: center; }
.stat-title { font-size: 13px; font-weight: 600; color: #64748b; text-transform: uppercase; letter-spacing: 0.5px; }
.stat-value { margin: 12px 0 0 0; font-size: 32px; font-weight: 800; }

.text-slate { color: #64748b; }
.text-blue { color: #3b82f6; }
.text-green { color: #10b981; }
.text-dark { color: #0f172a; }

/* ------- TABELLA E CONTROLLI ------- */
.table-card { overflow: hidden; padding: 0; }

.card-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 24px; border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.4);
  flex-wrap: wrap; gap: 16px;
}
.card-header h3 { margin: 0; font-size: 18px; font-weight: 700; color: #0f172a; }
.header-section { display: flex; align-items: center; flex: 1; }
.left { justify-content: flex-start; gap: 12px; }
.center { justify-content: center; }
.right { justify-content: flex-end; }

.badge.glass-badge {
  background: rgba(99, 102, 241, 0.1);
  color: #4f46e5; padding: 4px 12px; border-radius: 12px; font-size: 13px; font-weight: 600;
  border: 1px solid rgba(99, 102, 241, 0.2);
}

/* Filtri */
.filter-group { display: flex; gap: 8px; background: rgba(255,255,255,0.5); padding: 4px; border-radius: 24px; border: 1px solid rgba(255,255,255,0.4);}
.filter-btn {
  padding: 6px 16px; border-radius: 20px; border: none;
  background: transparent; color: #64748b;
  font-size: 12px; font-weight: 600; cursor: pointer; transition: all 0.2s; white-space: nowrap;
}
.filter-btn:not(.active):hover { color: #0f172a; background: rgba(255,255,255,0.6); }
.filter-btn.active { background: #6366f1; color: white; box-shadow: 0 2px 10px rgba(99, 102, 241, 0.3); }

/* Search Glass */
.search-wrapper { position: relative; width: 100%; max-width: 250px; }
.glass-input {
  width: 100%; padding: 10px 12px 10px 40px; border-radius: 12px;
  background: rgba(255, 255, 255, 0.6); border: 1px solid rgba(255,255,255,0.4);
  color: #0f172a; font-size: 13px; outline: none; box-sizing: border-box;
  transition: all 0.2s;
}
.glass-input::placeholder { color: #94a3b8; }
.glass-input:focus { border-color: #6366f1; background: rgba(255, 255, 255, 0.9); box-shadow: 0 0 0 3px rgba(99,102,241,0.15); }
.search-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); width: 16px; height: 16px; color: #94a3b8; }

/* Tabella Glass */
.table-responsive { width: 100%; overflow-x: auto; }
.glass-table { width: 100%; border-collapse: collapse; text-align: left; }
.glass-table th { padding: 16px 24px; font-size: 13px; font-weight: 600; color: #64748b; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1px solid rgba(0,0,0,0.05); }
.glass-table td { padding: 16px 24px; border-bottom: 1px solid rgba(0,0,0,0.03); color: #334155; font-size: 14px; vertical-align: middle; }
.glass-table tbody tr:hover { background: rgba(255,255,255,0.4); }
.glass-table tbody tr:last-child td { border-bottom: none; }

.mission-group-header { background: rgba(99, 102, 241, 0.05); border-top: 2px solid rgba(99, 102, 241, 0.2) !important; }
.mission-group-header td { padding: 12px 24px !important; color: #4f46e5; border-bottom: 1px solid rgba(99, 102, 241, 0.1) !important; }
.mission-header-content { display: flex; align-items: center; gap: 8px; font-size: 14px; }
.mission-header-content svg { width: 18px; height: 18px; }
.mission-operator { color: #64748b; font-weight: 500; margin-left: auto; }
.mission-operator strong { color: #334155; }

.nested-row td:first-child { padding-left: 40px; position: relative; }
.nested-row td:first-child::before { content: ''; position: absolute; left: 24px; top: 50%; width: 10px; height: 1px; background: #cbd5e1; }
.nested-row td:first-child::after { content: ''; position: absolute; left: 24px; top: 0; width: 1px; height: 100%; background: #cbd5e1; }
.nested-row:last-child td:first-child::after { height: 50%; }

.id-cell { color: #64748b; font-family: monospace; font-weight: 600; }
.desc-cell { color: #0f172a; font-weight: 600; max-width: 300px; }

.task-type { font-size: 11px; font-weight: 800; text-transform: uppercase; padding: 4px 10px; border-radius: 6px; }
.task-type.pickup { background: rgba(239, 68, 68, 0.1); color: #dc2626; border: 1px solid rgba(239, 68, 68, 0.2);}
.task-type.dropoff { background: rgba(59, 130, 246, 0.1); color: #2563eb; border: 1px solid rgba(59, 130, 246, 0.2);}
.task-type.move { background: rgba(245, 158, 11, 0.1); color: #d97706; border: 1px solid rgba(245, 158, 11, 0.2);}

.operator-cell { font-weight: 600; color: #475569; }
.operator-cell svg { width: 14px; display: inline; margin-right: 4px; opacity: 0.7; }

.stato-badge { font-size: 11px; font-weight: 700; padding: 5px 12px; border-radius: 20px; display: inline-block; }
.stato-da-fare { background: #f1f5f9; color: #475569; border: 1px solid #e2e8f0;}
.stato-in-carico { background: #dbeafe; color: #1d4ed8; border: 1px solid #bfdbfe;}
.stato-completato { background: #d1fae5; color: #065f46; border: 1px solid #a7f3d0;}

.action-cell { text-align: right; }
.btn-delete-glass { background: rgba(239, 68, 68, 0.1); color: #ef4444; border: 1px solid rgba(239, 68, 68, 0.2); padding: 8px; border-radius: 8px; cursor: pointer; transition: all 0.2s; display: inline-flex; align-items: center; justify-content: center; }
.btn-delete-glass:hover { background: #ef4444; color: white; transform: translateY(-2px); box-shadow: 0 4px 10px rgba(239, 68, 68, 0.3); }
.btn-delete-glass svg { width: 16px; height: 16px; }

/* --- COLONNA PERCORSO --- */
.route-cell { min-width: 220px; }
.route-wrap { display: flex; align-items: center; gap: 6px; font-size: 12px; font-weight: 600; padding: 5px 8px; border-radius: 8px; width: fit-content; }
.route-sposta  { background: rgba(139,92,246,0.08); border: 1px solid rgba(139,92,246,0.15); }
.route-preleva { background: rgba(239,68,68,0.07);  border: 1px solid rgba(239,68,68,0.15); }
.route-deposita{ background: rgba(16,185,129,0.07); border: 1px solid rgba(16,185,129,0.15); }
.route-slot { font-family: 'Courier New', monospace; font-size: 11px; color: #334155; padding: 2px 6px; background: rgba(255,255,255,0.7); border-radius: 4px; white-space: nowrap; }
.route-attesa { color: #94a3b8 !important; background: #f8fafc !important; font-style: italic; }
.route-arrow { width: 12px; height: 12px; color: #94a3b8; flex-shrink: 0; }

/* --- BADGE REPARTO --- */
.reparto-badge { display: inline-block; padding: 3px 10px; border-radius: 20px; font-size: 11px; font-weight: 700; background: rgba(99,102,241,0.1); color: #4338ca; border: 1px solid rgba(99,102,241,0.2); white-space: nowrap; }
.text-muted { color: #94a3b8; }

.empty-state-glass { padding: 60px 0; color: #64748b; text-align: center; display: flex; flex-direction: column; align-items: center; }
.empty-state-glass svg { width: 48px; height: 48px; margin-bottom: 16px; opacity: 0.5; }
.empty-state-glass p { margin: 0; font-size: 15px; }

/* ------- MODALE GLASS CHIARA ------- */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(15,23,42,0.4); backdrop-filter: blur(8px); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.glass-modal { background: rgba(255, 255, 255, 0.85); backdrop-filter: blur(24px); border: 1px solid rgba(255,255,255,0.8); border-radius: 20px; width: 100%; max-width: 480px; box-shadow: 0 25px 50px -12px rgba(0,0,0,0.2); display: flex; flex-direction: column; max-height: 90vh; }
.modal-header { padding: 24px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid rgba(0,0,0,0.05); }
.modal-header h2 { margin: 0; font-size: 18px; font-weight: 700; color: #0f172a; }
.btn-close-glass { background: transparent; border: none; color: #64748b; cursor: pointer; padding: 6px; border-radius: 8px; transition: 0.2s; }
.btn-close-glass:hover { background: rgba(0,0,0,0.05); color: #ef4444; }

.form-body { padding: 24px; overflow-y: auto; }
.form-row { display: flex; gap: 16px; }
.form-group { margin-bottom: 20px; width: 100%; }
.form-group.half { width: 50%; }
.form-group label { display: block; font-weight: 600; margin-bottom: 8px; color: #475569; font-size: 13px; }

.hint-warning { margin: 6px 0 0 0; font-size: 13px; color: #d97706; font-weight: 500; }
.error-banner { margin-top: 4px; padding: 12px 16px; background: rgba(239,68,68,0.1); border-left: 4px solid #ef4444; border-radius: 6px; color: #b91c1c; font-size: 13px; font-weight: 500; display: flex; align-items: center; gap: 8px;}
.success-banner { margin-top: 4px; padding: 12px 16px; background: rgba(16,185,129,0.1); border-left: 4px solid #10b981; border-radius: 6px; color: #047857; font-size: 13px; font-weight: 500; display: flex; align-items: center; gap: 8px;}

.modal-actions { padding: 20px 24px; border-top: 1px solid rgba(0,0,0,0.05); background: rgba(248, 250, 252, 0.6); display: flex; justify-content: flex-end; gap: 12px; border-radius: 0 0 20px 20px; }
.btn-ghost { background: transparent; color: #64748b; border: 1px solid rgba(0,0,0,0.1); padding: 10px 20px; border-radius: 12px; cursor: pointer; font-weight: 600; font-size: 14px; transition: 0.2s;}
.btn-ghost:hover { background: rgba(0,0,0,0.05); color: #0f172a;}
.btn-success-glass { background: linear-gradient(135deg, #10b981, #059669); color: white; border: none; padding: 10px 24px; border-radius: 12px; cursor: pointer; font-weight: 600; font-size: 14px; transition: 0.3s; box-shadow: 0 4px 15px rgba(16,185,129,0.3);}
.btn-success-glass:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(16,185,129,0.4); }
.btn-success-glass:disabled { opacity: 0.5; cursor: not-allowed; box-shadow: none;}
</style>