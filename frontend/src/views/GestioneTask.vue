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
    const matchesTipo = filtroTipo.value === 'TUTTI' || task.tipoTask === filtroTipo.value;
    const matchesQuery = task.id.toString().includes(q) ||
        task.descrizione.toLowerCase().includes(q) ||
        task.statoTask.toLowerCase().includes(q) ||
        (task.nomeDipendente && task.nomeDipendente.toLowerCase().includes(q));

    return matchesTipo && matchesQuery;
  });
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

const getStatoClass = (stato) => {
  if (stato === 'DA_FARE')   return 'stato-da-fare'
  if (stato === 'IN_CARICO') return 'stato-in-carico'
  if (stato === 'COMPLETATO') return 'stato-completato'
  return ''
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
                <button v-for="tipo in ['TUTTI', 'PRELIEVO', 'SPOSTAMENTO', 'DEPOSITO']"
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
                <th>ID</th><th>Descrizione</th><th>Tipo</th><th>Quantità</th><th>Assegnato a</th><th>Stato</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="task in taskFiltrati" :key="task.id">
                <td class="id-cell">#TSK-{{ task.id }}</td>
                <td class="desc-cell">{{ task.descrizione }}</td>
                <td>
                    <span class="task-type" :class="task.tipoTask === 'PRELIEVO' ? 'pickup' : task.tipoTask === 'DEPOSITO' ? 'dropoff' : 'move'">
                      {{ task.tipoTask }}
                    </span>
                </td>
                <td><strong>{{ task.quantita }}</strong></td>

                <td class="operator-cell">
                  <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>
                  {{ task.nomeDipendente }}
                </td>
                <td>
                    <span class="stato-badge" :class="getStatoClass(task.statoTask)">
                      {{ task.statoTask?.replace('_', ' ') }}
                    </span>
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