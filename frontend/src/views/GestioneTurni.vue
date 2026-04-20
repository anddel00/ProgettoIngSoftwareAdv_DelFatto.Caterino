<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import api from '../api'
import AdminSidebar from '../components/AdminSidebar.vue'

const nomeUtente = ref(sessionStorage.getItem('nomeUtente') || 'Admin')
const dipendenti = ref([])
let intervalId = null

const fetchDipendenti = async () => {
  try {
    // /api/turni/attivi restituisce solo chi ha il turno aperto
    // Per il monitoraggio completo usiamo tutti i dipendenti con il loro stato derivato
    const [tuttiR, attiviR] = await Promise.all([
      api.get('/api/auth/utenti'),
      api.get('/api/turni/attivi')
    ])
    const emailInTurno = new Set(attiviR.data.map(d => d.email))
    dipendenti.value = tuttiR.data
        .filter(u => u.ruolo?.nomeRuolo === 'Dipendente')
        .map(u => ({
          ...u,
          inTurno: emailInTurno.has(u.email)
        }))
  } catch (e) { console.error(e) }
}

onMounted(() => {
  fetchDipendenti()
  intervalId = setInterval(fetchDipendenti, 10000)
})
onUnmounted(() => clearInterval(intervalId))

const inTurno    = computed(() => dipendenti.value.filter(d => d.inTurno).length)
const fuoriTurno = computed(() => dipendenti.value.filter(d => !d.inTurno).length)

const inizialiAvatar = (nome, cognome) =>
    ((nome?.[0] || '') + (cognome?.[0] || '')).toUpperCase()
</script>

<template>
  <div class="dashboard-layout">
    <AdminSidebar />

    <main class="main-content">
      <header class="topbar">
        <div class="topbar-left">
          <span class="greeting">Monitoraggio in tempo reale</span>
          <h1>Turni Dipendenti</h1>
        </div>
        <div class="topbar-right">
          <div class="user-profile">
            <div class="avatar">{{ nomeUtente.charAt(0) }}</div>
            <span>{{ nomeUtente }}</span>
          </div>
          <button @click="fetchDipendenti" class="btn-refresh">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
            </svg>
            Aggiorna
          </button>
        </div>
      </header>

      <div class="content-area">

        <div class="stats-row">
          <div class="stat-card stat-verde">
            <div class="stat-icon">🟢</div>
            <div>
              <span class="stat-title">In Turno</span>
              <h2 class="stat-value">{{ inTurno }}</h2>
            </div>
          </div>
          <div class="stat-card stat-grigio">
            <div class="stat-icon">⚫</div>
            <div>
              <span class="stat-title">Fuori Turno</span>
              <h2 class="stat-value">{{ fuoriTurno }}</h2>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">👥</div>
            <div>
              <span class="stat-title">Totale Dipendenti</span>
              <h2 class="stat-value">{{ dipendenti.length }}</h2>
            </div>
          </div>
        </div>

        <div class="section-header">
          <h3>Stato Operatori</h3>
          <span class="refresh-hint">Aggiornamento automatico ogni 10s</span>
        </div>

        <div class="dipendenti-grid" v-if="dipendenti.length > 0">
          <div v-for="dip in dipendenti" :key="dip.id"
               class="dip-card" :class="dip.inTurno ? 'card-attivo' : 'card-inattivo'">
            <div class="dip-header">
              <div class="dip-avatar">{{ inizialiAvatar(dip.nome, dip.cognome) }}</div>
              <div class="dip-info">
                <h4>{{ dip.nome }} {{ dip.cognome }}</h4>
                <p class="dip-email">{{ dip.email }}</p>
              </div>
            </div>
            <div class="dip-footer">
              <span class="stato-badge" :class="dip.inTurno ? 'badge-attivo' : 'badge-inattivo'">
                <span class="dot" :class="dip.inTurno ? 'dot-verde' : 'dot-grigio'"></span>
                {{ dip.inTurno ? 'In Turno' : 'Fuori Turno' }}
              </span>
            </div>
          </div>
        </div>

        <div v-else class="empty-state-modern">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                  d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/>
          </svg>
          <p>Nessun dipendente registrato nel sistema.</p>
        </div>

      </div>
    </main>
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
.btn-refresh { background: #f1f5f9; color: #475569; border: 1px solid #e2e8f0; padding: 10px 16px; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 14px; display: flex; align-items: center; gap: 8px; transition: all 0.2s; }
.btn-refresh svg { width: 16px; height: 16px; }
.btn-refresh:hover { background: #e2e8f0; color: #0f172a; }
.content-area { padding: 40px; max-width: 1200px; margin: 0 auto; width: 100%; box-sizing: border-box; }
.stats-row { display: flex; gap: 20px; margin-bottom: 32px; }
.stat-card { flex: 1; background: #fff; padding: 20px 24px; border-radius: 16px; border: 1px solid #e2e8f0; display: flex; align-items: center; gap: 16px; }
.stat-verde { border-left: 4px solid #10b981; }
.stat-grigio { border-left: 4px solid #94a3b8; }
.stat-icon { font-size: 24px; }
.stat-title { font-size: 13px; font-weight: 600; color: #64748b; text-transform: uppercase; letter-spacing: 0.5px; }
.stat-value { margin: 4px 0 0 0; font-size: 32px; font-weight: 700; color: #0f172a; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.section-header h3 { margin: 0; font-size: 18px; color: #0f172a; font-weight: 600; }
.refresh-hint { font-size: 12px; color: #94a3b8; }
.dipendenti-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }
.dip-card { background: #fff; border-radius: 16px; padding: 20px; border: 1px solid #e2e8f0; transition: transform 0.2s; }
.dip-card:hover { transform: translateY(-2px); }
.card-attivo { border-color: #6ee7b7; background: linear-gradient(135deg, #fff 60%, #f0fdf4); }
.card-inattivo { opacity: 0.7; }
.dip-header { display: flex; align-items: center; gap: 14px; margin-bottom: 16px; }
.dip-avatar { width: 48px; height: 48px; border-radius: 50%; background: linear-gradient(135deg, #6366f1, #a855f7); color: white; display: flex; align-items: center; justify-content: center; font-size: 18px; font-weight: 700; flex-shrink: 0; }
.dip-info h4 { margin: 0 0 4px 0; font-size: 15px; font-weight: 700; color: #0f172a; }
.dip-email { margin: 0; font-size: 12px; color: #94a3b8; }
.dip-footer { display: flex; justify-content: flex-end; }
.stato-badge { display: inline-flex; align-items: center; gap: 6px; font-size: 12px; font-weight: 600; padding: 5px 12px; border-radius: 20px; }
.badge-attivo { background: #d1fae5; color: #065f46; }
.badge-inattivo { background: #f1f5f9; color: #475569; }
.dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.dot-verde { background: #10b981; box-shadow: 0 0 0 2px rgba(16,185,129,0.25); }
.dot-grigio { background: #94a3b8; }
.empty-state-modern { display: flex; flex-direction: column; align-items: center; padding: 60px 0; color: #94a3b8; }
.empty-state-modern svg { width: 56px; height: 56px; margin-bottom: 16px; opacity: 0.5; }
.empty-state-modern p { margin: 0; font-size: 15px; font-weight: 500; }
</style>