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
const dipendenti = ref([])   // solo quelli con turno attivo
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
      tuttiTask.value = [...tuttiTask.value] // Forza aggiornamento visivo
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

// NUOVA COMPUTED: Filtra i non completati E applica la ricerca
const taskAttiviLista = computed(() => {
  // 1. Prima prendiamo solo i task attivi
  let attivi = tuttiTask.value.filter(t => t.statoTask !== 'COMPLETATO');

  // 2. Se la barra di ricerca è vuota, li ritorniamo tutti
  if (!searchQuery.value) {
    return attivi;
  }

  // 3. Altrimenti filtriamo in base al testo (ID, Descrizione, Operatore, Stato)
  const q = searchQuery.value.toLowerCase();

  return attivi.filter(task => {
    const idMatch = task.id ? task.id.toString().includes(q) : false;
    const descMatch = task.descrizione ? task.descrizione.toLowerCase().includes(q) : false;
    const operatoreMatch = task.nomeDipendente ? task.nomeDipendente.toLowerCase().includes(q) : false;
    const statoMatch = task.statoTask ? task.statoTask.replace('_', ' ').toLowerCase().includes(q) : false;

    return idMatch || descMatch || operatoreMatch || statoMatch;
  });
})

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
  <div class="dashboard-layout">
    <AdminSidebar />

    <main class="main-content">
      <header class="topbar">
        <div class="topbar-left">
          <span class="greeting">Gestione Operativa</span>
          <h1>Assegnazione Task</h1>
        </div>
        <div class="topbar-right">
          <div class="user-profile">
            <div class="avatar">{{ nomeUtente.charAt(0) }}</div>
            <span>{{ nomeUtente }}</span>
          </div>
          <button @click="apriModale" class="btn-primary">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
            </svg>
            Nuovo Task
          </button>
        </div>
      </header>

      <div class="content-area">

        <div class="stats-row">
          <div class="stat-card">
            <span class="stat-title">Da Fare</span>
            <h2 class="stat-value" style="color:#64748b">{{ taskDaFare }}</h2>
          </div>
          <div class="stat-card">
            <span class="stat-title">In Carico</span>
            <h2 class="stat-value" style="color:#2563eb">{{ taskInCarico }}</h2>
          </div>
          <div class="stat-card">
            <span class="stat-title">Completati</span>
            <h2 class="stat-value" style="color:#10b981">{{ taskCompletati }}</h2>
          </div>
          <div class="stat-card">
            <span class="stat-title">Totale Task</span>
            <h2 class="stat-value">{{ tuttiTask.length }}</h2>
          </div>
        </div>

        <div class="card table-card">
          <div class="card-header" style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #e2e8f0; padding: 20px 24px;">
            <div style="display: flex; align-items: center; gap: 16px;">
              <h3>Task Attivi</h3>
              <span class="badge">{{ taskAttiviLista.length }} risultati</span>
            </div>

            <div class="search-container" style="position: relative; width: 280px;">
              <svg style="position: absolute; left: 12px; top: 50%; transform: translateY(-50%); width: 18px; height: 18px; color: #94a3b8;" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
              </svg>
              <input
                  type="text"
                  v-model="searchQuery"
                  placeholder="Cerca per ID, Operatore, Stato..."
                  style="width: 100%; box-sizing: border-box; padding: 10px 12px 10px 40px; border-radius: 8px; border: 1px solid #cbd5e1; font-size: 14px; outline: none; transition: all 0.2s; box-shadow: inset 0 1px 2px rgba(0,0,0,0.02);"
                  onfocus="this.style.borderColor='#3b82f6'; this.style.boxShadow='0 0 0 3px rgba(59, 130, 246, 0.1)';"
                  onblur="this.style.borderColor='#cbd5e1'; this.style.boxShadow='inset 0 1px 2px rgba(0,0,0,0.02)';"
              />
            </div>
          </div>

          <div class="table-responsive" v-if="taskAttiviLista.length > 0">
            <table class="modern-table">
              <thead>
              <tr>
                <th>ID</th><th>Descrizione</th><th>Tipo</th><th>Quantità</th><th>Assegnato a</th><th>Stato</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="task in taskAttiviLista" :key="task.id">
                <td class="id-cell">#TSK-{{ task.id }}</td>
                <td class="desc-cell">{{ task.descrizione }}</td>
                <td>
                    <span class="task-type" :class="task.tipoTask === 'PRELIEVO' ? 'pickup' : task.tipoTask === 'DEPOSITO' ? 'dropoff' : 'move'">
                      {{ task.tipoTask }}
                    </span>
                </td>
                <td><strong>{{ task.quantita }}</strong></td>

                <td style="font-weight: 500; color: #475569;">
                  <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" style="width: 14px; display: inline; margin-right: 4px; opacity: 0.7;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>
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

          <div v-else class="empty-state-modern">
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
    <div class="modal-content">
      <div class="modal-header">
        <h2>Crea e Assegna Task</h2>
        <button @click="chiudiModale" class="btn-close">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
          </svg>
        </button>
      </div>

      <div class="form-body">
        <div class="form-group">
          <label>Descrizione del Task</label>
          <input v-model="form.descrizione" type="text" placeholder="Es: Prelievo pallet zona A3..." />
        </div>
        <div class="form-row">
          <div class="form-group half">
            <label>Tipo Operazione</label>
            <select v-model="form.tipoTask">
              <option value="PRELIEVO">PRELIEVO</option>
              <option value="DEPOSITO">DEPOSITO</option>
              <option value="SPOSTAMENTO">SPOSTAMENTO</option>
            </select>
          </div>
          <div class="form-group half">
            <label>Quantità (unità)</label>
            <input v-model.number="form.quantita" type="number" min="1" />
          </div>
        </div>

        <div class="form-group">
          <label>Assegna a Dipendente</label>
          <select v-model="form.emailDipendente">
            <option value="" disabled>Seleziona un dipendente in turno...</option>
            <option v-for="dip in dipendenti" :key="dip.id" :value="dip.email">
              {{ dip.nome }} {{ dip.cognome }} — {{ dip.email }}
            </option>
          </select>
          <p v-if="dipendenti.length === 0" class="hint-warning">
            ⚠️ Nessun dipendente ha un turno attivo al momento.
          </p>
        </div>

        <div v-if="errorMessage" class="error-banner">
          {{ errorMessage }}
        </div>
        <div v-if="successMessage" class="success-banner">
          {{ successMessage }}
        </div>
      </div>

      <div class="modal-actions">
        <button @click="chiudiModale" class="btn-secondary">Annulla</button>
        <button @click="creaTask" class="btn-success" :disabled="dipendenti.length === 0">
          Crea e Assegna
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
.dashboard-layout { display: flex; height: 100vh; width: 100vw; background-color: #f8fafc; font-family: 'Inter', sans-serif; color: #334155; overflow: hidden; }
.main-content { flex-grow: 1; display: flex; flex-direction: column; overflow-y: auto; height: 100vh; }
.topbar { background: #fff; padding: 24px 40px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #e2e8f0; }
.topbar-right { display: flex; align-items: center; gap: 16px; }
.greeting { font-size: 14px; color: #64748b; font-weight: 500; }
.topbar h1 { margin: 4px 0 0 0; font-size: 24px; font-weight: 700; color: #0f172a; }
.user-profile { display: flex; align-items: center; gap: 12px; font-weight: 600; color: #0f172a; }
.avatar { width: 40px; height: 40px; background: linear-gradient(135deg, #6366f1, #a855f7); color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 16px; font-weight: 700; }
.btn-primary { background: #6366f1; color: white; border: none; padding: 10px 20px; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 14px; display: flex; align-items: center; gap: 8px; transition: background 0.2s; }
.btn-primary svg { width: 18px; height: 18px; }
.btn-primary:hover { background: #4f46e5; }
.content-area { padding: 40px; max-width: 1200px; margin: 0 auto; width: 100%; box-sizing: border-box; }
.stats-row { display: flex; gap: 24px; margin-bottom: 32px; }
.stat-card { flex: 1; background: #fff; padding: 24px; border-radius: 16px; border: 1px solid #e2e8f0; }
.stat-title { font-size: 13px; font-weight: 600; color: #64748b; text-transform: uppercase; letter-spacing: 0.5px; }
.stat-value { margin: 12px 0 0 0; font-size: 32px; font-weight: 700; color: #0f172a; }
.card { background: #fff; border-radius: 16px; border: 1px solid #e2e8f0; overflow: hidden; }
.card-header { padding: 24px; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; background: #f8fafc; }
.card-header h3 { margin: 0; font-size: 18px; color: #0f172a; }
.badge { background: #e0e7ff; color: #4f46e5; padding: 4px 12px; border-radius: 12px; font-size: 13px; font-weight: 600; }
.table-responsive { width: 100%; overflow-x: auto; }
.modern-table { width: 100%; border-collapse: collapse; text-align: left; }
.modern-table th { padding: 16px 24px; font-size: 13px; font-weight: 600; color: #64748b; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1px solid #e2e8f0; background: #fff; }
.modern-table td { padding: 16px 24px; border-bottom: 1px solid #f1f5f9; color: #334155; font-size: 14px; vertical-align: middle; }
.modern-table tbody tr:hover { background: #f8fafc; }
.modern-table tbody tr:last-child td { border-bottom: none; }
.id-cell { color: #94a3b8; font-family: monospace; font-weight: 600; }
.desc-cell { color: #0f172a; font-weight: 500; max-width: 300px; }
.task-type { font-size: 12px; font-weight: 700; text-transform: uppercase; padding: 4px 10px; border-radius: 6px; }
.task-type.pickup { background: #fef2f2; color: #dc2626; }
.task-type.dropoff { background: #eff6ff; color: #2563eb; }
.task-type.move { background: #fef9c3; color: #92400e; }
.stato-badge { font-size: 12px; font-weight: 600; padding: 5px 12px; border-radius: 20px; display: inline-block; }
.stato-da-fare { background: #f1f5f9; color: #475569; }
.stato-in-carico { background: #dbeafe; color: #1d4ed8; }
.stato-completato { background: #d1fae5; color: #065f46; }
.empty-state-modern { padding: 60px 0; color: #94a3b8; text-align: center; display: flex; flex-direction: column; align-items: center; }
.empty-state-modern svg { width: 48px; height: 48px; margin-bottom: 16px; opacity: 0.5; }
.empty-state-modern p { margin: 0; font-size: 15px; }
.modal-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(15,23,42,0.4); backdrop-filter: blur(4px); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-content { background: white; border-radius: 16px; width: 100%; max-width: 480px; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1); display: flex; flex-direction: column; max-height: 90vh; }
.modal-header { padding: 24px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #e2e8f0; }
.modal-header h2 { margin: 0; font-size: 18px; font-weight: 700; color: #0f172a; }
.btn-close { background: transparent; border: none; color: #94a3b8; cursor: pointer; padding: 4px; border-radius: 6px; }
.btn-close svg { width: 24px; height: 24px; }
.btn-close:hover { background: #f1f5f9; color: #0f172a; }
.form-body { padding: 24px; overflow-y: auto; }
.form-row { display: flex; gap: 16px; }
.form-group { margin-bottom: 20px; width: 100%; }
.form-group.half { width: 50%; }
.form-group label { display: block; font-weight: 600; margin-bottom: 8px; color: #334155; font-size: 13px; }
.form-group input, .form-group select { width: 100%; padding: 12px 16px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 14px; font-family: 'Inter', sans-serif; color: #0f172a; box-sizing: border-box; background: #fff; transition: all 0.2s; }
.form-group input:focus, .form-group select:focus { outline: none; border-color: #6366f1; box-shadow: 0 0 0 3px rgba(99,102,241,0.15); }
.hint-warning { margin: 6px 0 0 0; font-size: 13px; color: #b45309; font-weight: 500; }
.error-banner { margin-top: 4px; padding: 12px 16px; background: #fef2f2; border-left: 4px solid #ef4444; border-radius: 6px; display: flex; align-items: center; gap: 12px; color: #b91c1c; font-size: 13px; font-weight: 500; }
.error-banner svg { width: 20px; height: 20px; flex-shrink: 0; }
.success-banner { margin-top: 4px; padding: 12px 16px; background: #f0fdf4; border-left: 4px solid #10b981; border-radius: 6px; display: flex; align-items: center; gap: 12px; color: #065f46; font-size: 13px; font-weight: 500; }
.success-banner svg { width: 20px; height: 20px; flex-shrink: 0; }
.modal-actions { padding: 20px 24px; border-top: 1px solid #e2e8f0; background: #f8fafc; display: flex; justify-content: flex-end; gap: 12px; border-radius: 0 0 16px 16px; }
.btn-secondary { background: white; color: #475569; border: 1px solid #cbd5e1; padding: 10px 20px; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 14px; }
.btn-secondary:hover { background: #f8fafc; }
.btn-success { background: #10b981; color: white; border: none; padding: 10px 24px; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 14px; transition: background 0.2s; }
.btn-success:hover:not(:disabled) { background: #059669; }
.btn-success:disabled { opacity: 0.5; cursor: not-allowed; }
</style>