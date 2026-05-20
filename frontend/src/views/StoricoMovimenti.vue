<script setup>
import { ref, onMounted, watch , computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'
import AdminSidebar from '../components/AdminSidebar.vue'
import { useWmsWebSocket } from '../composables/useWmsWebSocket.js'

const router = useRouter()
const nomeUtente = ref(sessionStorage.getItem('nomeUtente') || 'Admin')
const storico = ref([])

const { ultimoTaskRicevuto } = useWmsWebSocket()

const fetchStorico = async () => {
  try {
    const response = await api.get('/api/tasks/storico-admin')
    storico.value = response.data.reverse()
  } catch (error) {
    console.error("Errore nel recupero dello storico:", error)
  }
}

// OSSERVIAMO IL WEBSOCKET
watch(ultimoTaskRicevuto, (nuovoTask) => {
  if (nuovoTask && nuovoTask.statoTask === 'COMPLETATO') {
    const idDaCercare = nuovoTask.id || nuovoTask.idTask;
    const esisteGia = storico.value.some(t => t.idTask === idDaCercare || t.id === idDaCercare)

    if (!esisteGia) {
      const taskStorico = {
        idTask: nuovoTask.id,
        nomeDipendente: nuovoTask.nomeDipendente || 'Operatore',
        tipoTask: nuovoTask.tipoTask,
        descrizione: nuovoTask.descrizione,
        quantita: nuovoTask.quantita || nuovoTask.qtaSpostata
      }
      storico.value = [taskStorico, ...storico.value]
    }
  }
})

// BARRA DI RICERCA
const searchQuery = ref('')
const filtroTipo = ref('TUTTI')

const storicoFiltrato = computed(() => {
  const q = searchQuery.value.toLowerCase()

  return storico.value.filter(record => {
    // 1. Logica Categoria
    const matchesTipo = filtroTipo.value === 'TUTTI' || record.tipoTask === filtroTipo.value

    // 2. Logica Ricerca Testuale
    const idValido = (record.idTask || record.id || '').toString().toLowerCase();
    const descValida = (record.descrizione || '').toLowerCase();
    const nomeValido = (record.nomeDipendente || '').toLowerCase();

    const matchesQuery = idValido.includes(q) || descValida.includes(q) || nomeValido.includes(q)

    return matchesTipo && matchesQuery
  })
})

onMounted(() => {
  fetchStorico()
})

// Helper: label leggibile di uno slot
const labelScaffale = (idScaffale, y, x, z) => {
  if (!idScaffale) return null
  return `S${idScaffale} · R${y + 1} C${x + 1} P${z + 1}`
}

const getPercorso = (record) => {
  const inizio = labelScaffale(record.idScaffaleInizio, record.vecchiaY, record.vecchiaX, record.vecchiaZ)
  const fine   = labelScaffale(record.idScaffaleFine,   record.nuovaY,  record.nuovaX,  record.nuovaZ)
  if (record.tipoTask === 'SPOSTAMENTO') return { da: inizio || '—', a: fine || '—', tipo: 'sposta' }
  if (record.tipoTask === 'PRELIEVO')    return { da: inizio || '—', a: 'In attesa', tipo: 'preleva' }
  if (record.tipoTask === 'DEPOSITO')    return { da: 'In attesa', a: fine || '—',   tipo: 'deposita' }
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
          <span class="greeting">Registro di Audit</span>
          <h1>Storico Movimenti</h1>
        </div>
        <div class="topbar-right">
          <div class="user-profile">
            <div class="avatar">{{ nomeUtente.charAt(0) }}</div>
            <span>{{ nomeUtente }}</span>
          </div>
        </div>
      </header>

      <div class="content-area">

        <div class="glass-card table-card">
          <div class="card-header">
            <div class="header-section left">
              <h3>Task Completati</h3>
              <span class="glass-badge badge-success">{{ storicoFiltrato.length }} Registri</span>
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
                       placeholder="Cerca task, operatore..." />
              </div>
            </div>
          </div>

          <div class="table-responsive" v-if="storicoFiltrato.length > 0">
            <table class="glass-table">
              <thead>
              <tr>
                <th>ID Task</th>
                <th>Operatore</th>
                <th>Tipo</th>
                <th>Percorso</th>
                <th>Reparto</th>
                <th>Qta</th>
                <th>Stato</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="record in storicoFiltrato" :key="record.idTask || record.id">
                <td class="id-cell">#TSK-{{ record.idTask || record.id }}</td>
                <td class="user-cell">
                  <div class="user-avatar-small">{{ record.nomeDipendente ? record.nomeDipendente.charAt(0) : 'O' }}</div>
                  <strong>{{ record.nomeDipendente || 'Operatore' }}</strong>
                </td>
                <td>
                    <span class="task-type" :class="record.tipoTask === 'PRELIEVO' ? 'pickup' : record.tipoTask === 'DEPOSITO' ? 'dropoff' : 'move'">
                      {{ record.tipoTask }}
                    </span>
                </td>
                <!-- COLONNA PERCORSO -->
                <td class="route-cell">
                  <div class="route-wrap" :class="'route-' + getPercorso(record).tipo">
                    <span class="route-slot">{{ getPercorso(record).da }}</span>
                    <svg class="route-arrow" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 8l4 4m0 0l-4 4m4-4H3"/></svg>
                    <span class="route-slot" :class="{'route-attesa': getPercorso(record).a === 'In attesa' || getPercorso(record).da === 'In attesa'}">{{ getPercorso(record).a }}</span>
                  </div>
                </td>
                <!-- COLONNA REPARTO -->
                <td>
                  <span v-if="record.nomeReparto" class="reparto-badge">{{ record.nomeReparto }}</span>
                  <span v-else class="text-muted">&mdash;</span>
                </td>
                <td><strong>{{ record.quantita || record.qtaSpostata }}</strong></td>
                <td><span class="glass-badge badge-success">Completato</span></td>
              </tr>
              </tbody>
            </table>
          </div>

          <div v-else class="empty-state-glass">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path></svg>
            <p v-if="searchQuery">Nessun risultato trovato per "<strong>{{ searchQuery }}</strong>"</p>
            <p v-else>Il registro storico è vuoto. Nessun task completato al momento.</p>
          </div>
        </div>

      </div>
    </main>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');

/* ------- LAYOUT & BACKGROUND CHIARO (Uniformato) ------- */
.glass-dashboard-layout { display: flex; height: 100vh; width: 100vw; background-color: #e2e8f0; font-family: 'Inter', sans-serif; margin: 0; color: #1e293b; position: relative; overflow: hidden; }
.glass-bg-blob { position: absolute; border-radius: 50%; filter: blur(80px); z-index: 0; opacity: 0.6; }
.blob-1 { top: -10%; left: -10%; width: 500px; height: 500px; background: #93c5fd; }
.blob-2 { bottom: -20%; right: -10%; width: 600px; height: 600px; background: #c4b5fd; }
.blob-3 { top: 40%; left: 40%; width: 400px; height: 400px; background: #86efac; opacity: 0.4; }

.main-content { flex-grow: 1; display: flex; flex-direction: column; overflow-y: auto; z-index: 10; }

/* ------- TOPBAR CHIARA ------- */
.glass-topbar { background: rgba(255, 255, 255, 0.4); backdrop-filter: blur(10px); -webkit-backdrop-filter: blur(10px); padding: 20px 40px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid rgba(255, 255, 255, 0.3); }
.topbar-right { display: flex; align-items: center; gap: 24px; }
.greeting { font-size: 14px; color: #64748b; font-weight: 500; }
.topbar-left h1 { margin: 4px 0 0 0; font-size: 24px; font-weight: 700; color: #0f172a; letter-spacing: -0.5px; }

.user-profile { display: flex; align-items: center; gap: 12px; font-weight: 600; color: #0f172a; }
.avatar { width: 44px; height: 44px; background: linear-gradient(135deg, #6366f1, #a855f7); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-weight: 600; font-size: 18px; box-shadow: 0 4px 10px rgba(99, 102, 241, 0.3); }

.content-area { padding: 40px; max-width: 1200px; margin: 0 auto; width: 100%; box-sizing: border-box;}

/* ------- IL VETRO CHIARO (CARD DELLA TABELLA) ------- */
.glass-card { background: rgba(255, 255, 255, 0.65); backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px); border: 1px solid rgba(255, 255, 255, 0.5); box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.1); border-radius: 20px; }
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

/* Filtri Glass */
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
.desc-cell { color: #0f172a; font-weight: 600; }

.user-cell { display: flex; align-items: center; gap: 12px; }
.user-avatar-small { width: 28px; height: 28px; background-color: rgba(99, 102, 241, 0.1); color: #4f46e5; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; border: 1px solid rgba(99, 102, 241, 0.2); }

.task-type { font-size: 11px; font-weight: 800; text-transform: uppercase; padding: 4px 10px; border-radius: 6px; }
.task-type.pickup { background: rgba(239, 68, 68, 0.1); color: #dc2626; border: 1px solid rgba(239, 68, 68, 0.2);}
.task-type.dropoff { background: rgba(59, 130, 246, 0.1); color: #2563eb; border: 1px solid rgba(59, 130, 246, 0.2);}
.task-type.move { background: rgba(245, 158, 11, 0.1); color: #d97706; border: 1px solid rgba(245, 158, 11, 0.2);}

/* Badges */
.glass-badge { padding: 6px 12px; border-radius: 12px; font-size: 12px; font-weight: 600; border: 1px solid rgba(255,255,255,0.5); backdrop-filter: blur(4px); display: inline-block; }
.badge-success { background-color: rgba(16, 185, 129, 0.15); color: #059669; border-color: rgba(16, 185, 129, 0.3); }

.empty-state-glass { padding: 60px 0; color: #64748b; text-align: center; display: flex; flex-direction: column; align-items: center; }
.empty-state-glass svg { width: 48px; height: 48px; margin-bottom: 16px; opacity: 0.5; }
.empty-state-glass p { margin: 0; font-size: 15px; }

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
</style>