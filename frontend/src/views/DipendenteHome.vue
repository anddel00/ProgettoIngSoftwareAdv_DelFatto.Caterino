<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api' // Usando la tua configurazione Axios personalizzata!

const router = useRouter()
const nomeUtente = ref(localStorage.getItem('nomeUtente') || 'Operatore')
const emailUtente = ref(localStorage.getItem('emailUtente') || 'dipendente@wms.it')

const isTurnoAttivo = ref(false)
const taskAssegnati = ref([])

const logout = () => {
  localStorage.clear()
  router.push('/')
}

// Funzione per scaricare i task dal database
const fetchTasks = async () => {
  try {
    const response = await api.get(`/api/tasks/miei-task?email=${emailUtente.value}`)
    taskAssegnati.value = response.data.filter(t => t.statoTask !== 'COMPLETATO')
  } catch (error) {
    console.error("Errore nel recupero dei task:", error)
  }
}

// Quando clicca "Inizia Turno", scarichiamo i task veri
const toggleTurno = () => {
  isTurnoAttivo.value = !isTurnoAttivo.value
  if (isTurnoAttivo.value) {
    fetchTasks()
  }
}

// Funzione per cambiare lo stato del task cliccando i bottoni
const aggiornaStatoTask = async (task, nuovoStato) => {
  try {
    await api.patch(`/api/tasks/${task.id}/stato?nuovoStato=${nuovoStato}`)

    // Aggiorniamo la UI localmente
    task.statoTask = nuovoStato

    // Se lo abbiamo completato, lo togliamo dalla vista della Home
    if (nuovoStato === 'COMPLETATO') {
      taskAssegnati.value = taskAssegnati.value.filter(t => t.id !== task.id)
    }
  } catch (error) {
    console.error("Errore durante l'aggiornamento dello stato:", error)
    alert("Impossibile aggiornare lo stato del task. Riprova.")
  }
}
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
        <div class="nav-item active">
          <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"></path></svg>
          Il Mio Turno
        </div>
        <div class="nav-item" @click="router.push('/DipendenteTask')">
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
          <span class="greeting">Operatore Logistico</span>
          <h1>{{ nomeUtente }}</h1>
        </div>
        <div class="status-badge" :class="isTurnoAttivo ? 'status-active' : 'status-inactive'">
          <span class="status-dot"></span>
          {{ isTurnoAttivo ? 'In Turno' : 'Fuori Turno' }}
        </div>
      </header>

      <div class="content-area">

        <div class="shift-panel card" :class="{'panel-active': isTurnoAttivo}">
          <div class="shift-info">
            <h2>{{ isTurnoAttivo ? 'Stai lavorando' : 'Pronto per iniziare?' }}</h2>
            <p>{{ isTurnoAttivo ? 'Assicurati di completare i task in ordine di priorità.' : 'Timbra il cartellino virtuale per ricevere i task.' }}</p>
          </div>
          <button @click="toggleTurno" class="btn-shift" :class="isTurnoAttivo ? 'btn-stop' : 'btn-start'">
            <svg v-if="!isTurnoAttivo" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            <svg v-else fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 9v6m4-6v6m7-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            {{ isTurnoAttivo ? 'Pausa / Fine Turno' : 'Inizia Turno' }}
          </button>
        </div>

        <div class="section-header" v-if="isTurnoAttivo">
          <h3>Task Attuali</h3>
          <span class="task-count">{{ taskAssegnati.length }} da completare</span>
        </div>

        <div class="task-grid" v-if="isTurnoAttivo">

          <div v-for="task in taskAssegnati" :key="task.id"
               class="task-card"
               :class="task.statoTask === 'IN_CARICO' ? 'priority-high' : 'priority-normal'">

            <div class="task-header">
              <span class="task-type" :class="task.tipoTask === 'PRELIEVO' ? 'pickup' : 'dropoff'">
                {{ task.tipoTask }}
              </span>
              <span class="task-id">#TSK-{{ task.id }}</span>
            </div>

            <h3 class="item-name">{{ task.descrizione }}</h3>

            <div class="location-box">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
              <span>Posizione da definire sulla Mappa</span>
            </div>

            <div class="qty-box">Quantità da spostare: <strong>{{ task.quantita }} unità</strong></div>

            <button v-if="task.statoTask === 'DA_FARE'"
                    @click="aggiornaStatoTask(task, 'IN_CARICO')"
                    class="btn-primary w-full mt-4">
              Prendi in Carico
            </button>

            <button v-if="task.statoTask === 'IN_CARICO'"
                    @click="aggiornaStatoTask(task, 'COMPLETATO')"
                    class="btn-success w-full mt-4">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" style="width: 18px; display: inline; vertical-align: text-bottom;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
              Conferma Completamento
            </button>
          </div>

          <div v-if="taskAssegnati.length === 0" class="empty-state-modern" style="grid-column: 1 / -1;">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            <p>Ottimo lavoro! Non ci sono task attivi al momento.</p>
          </div>

        </div>

        <div class="empty-state-modern" v-if="!isTurnoAttivo">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
          <p>Inizia il turno per visualizzare i tuoi task.</p>
        </div>

      </div>
    </main>
  </div>
</template>
<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

/* ------- LAYOUT BASE (Stesso dell'Admin) ------- */
.dashboard-layout { display: flex; flex-direction: row; height: 100vh; width: 100vw; background-color: #f8fafc; margin: 0; font-family: 'Inter', sans-serif; color: #334155; overflow: hidden; }
.main-content { flex-grow: 1; display: flex; flex-direction: column; overflow-y: auto; height: 100vh; }
.sidebar { width: 260px; background-color: #0f172a; color: #94a3b8; display: flex; flex-direction: column; border-right: 1px solid #1e293b; }
.sidebar-header { padding: 30px 24px; display: flex; align-items: center; gap: 12px; }
.logo-icon svg { width: 28px; height: 28px; color: #10b981; /* Verde per il worker */ }
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

/* ------- TOPBAR ------- */
.topbar { background-color: #ffffff; padding: 24px 40px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #e2e8f0; }
.greeting { font-size: 14px; color: #64748b; font-weight: 500; }
.topbar h1 { margin: 4px 0 0 0; font-size: 24px; font-weight: 700; color: #0f172a; letter-spacing: -0.5px; }
.content-area { padding: 40px; max-width: 1200px; margin: 0 auto; width: 100%; box-sizing: border-box; }

/* Badge di Stato */
.status-badge { display: flex; align-items: center; gap: 8px; padding: 8px 16px; border-radius: 20px; font-size: 14px; font-weight: 600; }
.status-dot { width: 10px; height: 10px; border-radius: 50%; }
.status-active { background-color: #f0fdf4; color: #166534; border: 1px solid #bbf7d0; }
.status-active .status-dot { background-color: #22c55e; box-shadow: 0 0 0 3px rgba(34, 197, 94, 0.2); }
.status-inactive { background-color: #f1f5f9; color: #475569; border: 1px solid #e2e8f0; }
.status-inactive .status-dot { background-color: #94a3b8; }

/* ------- PANNELLO TURNO ------- */
.card { background: #ffffff; padding: 28px; border-radius: 16px; border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02); }
.shift-panel { display: flex; justify-content: space-between; align-items: center; margin-bottom: 32px; transition: all 0.3s ease; }
.shift-panel.panel-active { border-color: #6ee7b7; background: linear-gradient(to right, #ffffff, #f0fdf4); }
.shift-info h2 { margin: 0 0 8px 0; font-size: 20px; color: #0f172a; }
.shift-info p { margin: 0; color: #64748b; font-size: 14px; }

.btn-shift { display: flex; align-items: center; gap: 10px; padding: 14px 28px; border-radius: 12px; font-weight: 600; font-size: 16px; cursor: pointer; border: none; transition: all 0.2s; color: white; }
.btn-shift svg { width: 24px; height: 24px; }
.btn-start { background-color: #10b981; box-shadow: 0 4px 6px -1px rgba(16, 185, 129, 0.3); }
.btn-start:hover { background-color: #059669; transform: translateY(-1px); }
.btn-stop { background-color: #f59e0b; box-shadow: 0 4px 6px -1px rgba(245, 158, 11, 0.3); }
.btn-stop:hover { background-color: #d97706; }

/* ------- TASK CARDS ------- */
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.section-header h3 { margin: 0; font-size: 18px; color: #0f172a; }
.task-count { background: #e0e7ff; color: #4f46e5; padding: 4px 12px; border-radius: 12px; font-size: 13px; font-weight: 600; }

.task-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 24px; }
.task-card { background: white; border: 1px solid #e2e8f0; border-radius: 16px; padding: 24px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02); position: relative; overflow: hidden; }
.task-card::before { content: ''; position: absolute; top: 0; left: 0; width: 4px; height: 100%; }
.priority-high::before { background-color: #ef4444; }
.priority-normal::before { background-color: #3b82f6; }

.task-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.task-type { font-size: 12px; font-weight: 700; text-transform: uppercase; padding: 4px 10px; border-radius: 6px; }
.task-type.pickup { background: #fef2f2; color: #dc2626; }
.task-type.dropoff { background: #eff6ff; color: #2563eb; }
.task-id { color: #94a3b8; font-size: 13px; font-weight: 600; font-family: monospace; }

.item-name { margin: 0 0 16px 0; font-size: 18px; color: #0f172a; font-weight: 700; }
.location-box { display: flex; align-items: center; gap: 8px; background: #f8fafc; padding: 12px; border-radius: 8px; color: #334155; font-size: 14px; margin-bottom: 12px; border: 1px solid #e2e8f0; }
.location-box svg { width: 20px; height: 20px; color: #64748b; }
.qty-box { font-size: 15px; color: #475569; padding-bottom: 8px; }

.btn-primary { background-color: #6366f1; color: white; border: none; padding: 12px; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 14px; transition: all 0.2s; }
.btn-primary:hover { background-color: #4f46e5; }
.w-full { width: 100%; }
.mt-4 { margin-top: 16px; }

.empty-state-modern { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 60px 0; color: #94a3b8; }
.empty-state-modern svg { width: 64px; height: 64px; margin-bottom: 16px; opacity: 0.5; }
.empty-state-modern p { margin: 0; font-size: 16px; font-weight: 500; }

.btn-success { background-color: #10b981; color: white; border: none; padding: 12px; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 14px; transition: all 0.2s; }
.btn-success:hover { background-color: #059669; }
</style>