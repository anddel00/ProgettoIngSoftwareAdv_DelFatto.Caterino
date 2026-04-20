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

const logout = () => {
  sessionStorage.clear()
  router.push('/')
}

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
    // Nota: il WebSocket manda un TaskDTO standard (con .id invece di .idTask)
    // Adattiamo la logica per assicurarci che combaci con lo storico
    const idDaCercare = nuovoTask.id || nuovoTask.idTask;
    const esisteGia = storico.value.some(t => t.idTask === idDaCercare || t.id === idDaCercare)

    if (!esisteGia) {
      // Formattiamo il nuovo task come se fosse arrivato dall'API dello storico
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

// logica per bottoni filtro
const filtroTipo = ref('TUTTI')

const storicoFiltrato = computed(() => {
  const q = searchQuery.value.toLowerCase()

  return storico.value.filter(record => {
    // 1. Logica Categoria (Se è 'TUTTI' passa tutto, altrimenti filtra)
    const matchesTipo = filtroTipo.value === 'TUTTI' || record.tipoTask === filtroTipo.value

    // 2. Logica Ricerca Testuale per barra di ricerca
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
</script>

<template>
  <div class="dashboard-layout">
    <AdminSidebar />

    <main class="main-content">
      <header class="topbar">
        <div class="topbar-left">
          <span class="greeting">Registro di Audit</span>
          <h1>Storico Movimenti</h1>
        </div>
        <div class="user-profile">
          <div class="avatar">{{ nomeUtente.charAt(0) }}</div>
          <span>{{ nomeUtente }}</span>
        </div>
      </header>

      <div class="content-area">

        <div class="card table-card">
          <div class="card-header">
            <div class="header-section left">
              <h3>Task Completati</h3>
              <span class="badge">{{ storicoFiltrato.length }} Registri</span>
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
                       class="search-input"
                       placeholder="Cerca task..." />
              </div>
            </div>
          </div>

          <div class="table-responsive" v-if="storicoFiltrato.length > 0">
            <table class="modern-table">
              <thead>
              <tr>
                <th>ID Task</th>
                <th>Operatore</th>
                <th>Tipo Operazione</th>
                <th>Articolo / Descrizione</th>
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
                    <span class="task-type" :class="record.tipoTask === 'PRELIEVO' ? 'pickup' : 'dropoff'">
                      {{ record.tipoTask }}
                    </span>
                </td>
                <td class="desc-cell">{{ record.descrizione }}</td>
                <td><strong>{{ record.quantita || record.qtaSpostata }}</strong></td>
                <td><span class="status-badge-success">Completato</span></td>
              </tr>
              </tbody>
            </table>
          </div>

          <div v-else class="empty-state-modern">
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
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

/* Layout Base Admin */
.dashboard-layout { display: flex; flex-direction: row; height: 100vh; width: 100vw; background-color: #f8fafc; margin: 0; font-family: 'Inter', sans-serif; color: #334155; overflow: hidden; }
.main-content { flex-grow: 1; display: flex; flex-direction: column; overflow-y: auto; height: 100vh; }
.sidebar { width: 260px; background-color: #0f172a; color: #94a3b8; display: flex; flex-direction: column; border-right: 1px solid #1e293b; }
.sidebar-header { padding: 30px 24px; display: flex; align-items: center; gap: 12px; }
.admin-icon svg { width: 28px; height: 28px; color: #3b82f6; }
.sidebar-header h2 { margin: 0; font-size: 20px; font-weight: 700; color: #f8fafc; letter-spacing: -0.5px; }
.sidebar-nav { flex-grow: 1; padding: 0 16px; display: flex; flex-direction: column; gap: 8px; }
.nav-item { padding: 12px 16px; border-radius: 12px; cursor: pointer; font-size: 14px; font-weight: 500; display: flex; align-items: center; gap: 12px; transition: all 0.2s ease; }
.nav-icon { width: 20px; height: 20px; opacity: 0.7; }
.nav-item:hover { background-color: #1e293b; color: #f8fafc; }
.nav-item.active { background-color: rgba(59, 130, 246, 0.15); color: #3b82f6; font-weight: 600; }
.nav-item.active .nav-icon { opacity: 1; }
.sidebar-footer { padding: 24px 16px; }
.btn-logout { width: 100%; padding: 12px; background-color: transparent; color: #94a3b8; border: 1px solid #1e293b; border-radius: 12px; cursor: pointer; font-weight: 500; display: flex; align-items: center; justify-content: center; gap: 8px; transition: all 0.2s; }
.btn-logout:hover { background-color: #ef4444; color: white; border-color: #ef4444; }

/* Topbar */
.topbar { background-color: #ffffff; padding: 24px 40px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #e2e8f0; }
.greeting { font-size: 14px; color: #64748b; font-weight: 500; }
.topbar h1 { margin: 4px 0 0 0; font-size: 24px; font-weight: 700; color: #0f172a; letter-spacing: -0.5px; }
.user-profile { display: flex; align-items: center; gap: 12px; font-weight: 600; color: #0f172a; }
.avatar { width: 40px; height: 40px; background-color: #3b82f6; color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 16px; font-weight: 700; }

.content-area { padding: 40px; max-width: 1200px; margin: 0 auto; width: 100%; box-sizing: border-box; }

/* Stili Tabella */
.card { background: #ffffff; border-radius: 16px; border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02); overflow: hidden; }
/* Intestazione della Card */
/* Intestazione della Card */
/* Intestazione della Card - Layout a 3 Zone */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
  gap: 16px;
  flex-wrap: wrap; /* Va a capo solo se lo schermo è davvero stretto */
}

.header-section {
  display: flex;
  align-items: center;
  flex: 1; /* Dividiamo lo spazio in 3 parti uguali */
}

.left { justify-content: flex-start; }
.center { justify-content: center; }
.right { justify-content: flex-end; }

/* Barra Ricerca */
.search-wrapper {
  position: relative;
  width: 100%;
  max-width: 250px; /* Limita la larghezza per non tagliare */
}

.search-input {
  width: 100%;
  padding: 10px 12px 10px 40px;
  border-radius: 8px;
  border: 1px solid #cbd5e1;
  font-size: 13px;
  outline: none;
  box-sizing: border-box; /* Fondamentale: impedisce al padding di rompere la larghezza */
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  color: #94a3b8;
}

/* Filtri */
.filter-group {
  display: flex;
  gap: 8px;
}

.filter-btn {
  padding: 6px 14px;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

/* L'hover scatta solo se non è attivo */
.filter-btn:not(.active):hover {
  background: #f1f5f9;
}

.filter-btn.active {
  background: #6366f1;
  color: white;
  border-color: #6366f1;
}

.filter-btn:disabled {
  cursor: default;
  opacity: 1;
}
.card-header h3 { margin: 0; font-size: 18px; color: #0f172a; }
.badge { background: #e0e7ff; color: #4f46e5; padding: 4px 12px; border-radius: 12px; font-size: 13px; font-weight: 600; }

.table-responsive { width: 100%; overflow-x: auto; }
.modern-table { width: 100%; border-collapse: collapse; text-align: left; }
.modern-table th { padding: 16px 24px; font-size: 13px; font-weight: 600; color: #64748b; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1px solid #e2e8f0; background-color: #ffffff; }
.modern-table td { padding: 16px 24px; border-bottom: 1px solid #f1f5f9; color: #334155; font-size: 14px; vertical-align: middle; }
.modern-table tbody tr:hover { background-color: #f8fafc; }
.modern-table tbody tr:last-child td { border-bottom: none; }

.id-cell { color: #94a3b8; font-family: monospace; font-weight: 600; }
.desc-cell { color: #0f172a; font-weight: 500; }

/* Utente nella riga */
.user-cell { display: flex; align-items: center; gap: 12px; }
.user-avatar-small { width: 28px; height: 28px; background-color: #e2e8f0; color: #475569; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; }

/* Badges */
.task-type { font-size: 12px; font-weight: 700; text-transform: uppercase; padding: 4px 10px; border-radius: 6px; }
.task-type.pickup { background: #fef2f2; color: #dc2626; }
.task-type.dropoff { background: #eff6ff; color: #2563eb; }

.status-badge-success { background: #d1fae5; color: #065f46; font-size: 12px; font-weight: 600; padding: 6px 12px; border-radius: 20px; display: inline-block; }

.empty-state-modern { padding: 60px 0; color: #94a3b8; text-align: center; font-size: 15px; display: flex; flex-direction: column; align-items: center; }
.empty-state-modern svg { width: 48px; height: 48px; margin-bottom: 16px; opacity: 0.5; }

</style>