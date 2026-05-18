<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import api from '../api'
import AdminSidebar from '../components/AdminSidebar.vue'

const nomeUtente = ref(sessionStorage.getItem('nomeUtente') || 'Admin')
const dipendenti = ref([])
let intervalId = null

const fetchDipendenti = async () => {
  try {
    const [tuttiR, attiviR] = await Promise.all([
      api.get('/api/auth/utenti'),
      api.get('/api/turni/attivi')
    ])
    const emailInTurno = new Set(attiviR.data.map(d => d.email))
    dipendenti.value = tuttiR.data
        .filter(u => u.ruolo === 'Dipendente')
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
  <div class="glass-dashboard-layout">
    <div class="glass-bg-blob blob-1"></div>
    <div class="glass-bg-blob blob-2"></div>
    <div class="glass-bg-blob blob-3"></div>

    <AdminSidebar />

    <main class="main-content">
      <header class="glass-topbar">
        <div class="topbar-left">
          <span class="greeting">Monitoraggio in tempo reale</span>
          <h1>Turni Dipendenti</h1>
        </div>
        <div class="topbar-right">
          <div class="user-profile">
            <div class="avatar">{{ nomeUtente.charAt(0) }}</div>
            <span>{{ nomeUtente }}</span>
          </div>
          <button @click="fetchDipendenti" class="btn-glass-ghost">
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
          <div class="glass-card stat-card interactive-card">
            <div class="stat-icon-wrapper green-glow">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            </div>
            <div class="stat-info">
              <span class="stat-title">In Turno</span>
              <h2 class="stat-value text-green">{{ inTurno }}</h2>
            </div>
          </div>

          <div class="glass-card stat-card interactive-card">
            <div class="stat-icon-wrapper slate-glow">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            </div>
            <div class="stat-info">
              <span class="stat-title">Fuori Turno</span>
              <h2 class="stat-value text-slate">{{ fuoriTurno }}</h2>
            </div>
          </div>

          <div class="glass-card stat-card interactive-card">
            <div class="stat-icon-wrapper blue-glow">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
            </div>
            <div class="stat-info">
              <span class="stat-title">Totale Dipendenti</span>
              <h2 class="stat-value text-dark">{{ dipendenti.length }}</h2>
            </div>
          </div>
        </div>

        <div class="section-header">
          <h3>Stato Operatori</h3>
          <span class="refresh-hint">Aggiornamento automatico ogni 10s</span>
        </div>

        <div class="dipendenti-grid" v-if="dipendenti.length > 0">
          <div v-for="dip in dipendenti" :key="dip.id"
               class="glass-card dip-card" :class="dip.inTurno ? 'card-attivo' : 'card-inattivo'">
            <div class="dip-header">
              <div class="dip-avatar">{{ inizialiAvatar(dip.nome, dip.cognome) }}</div>
              <div class="dip-info">
                <h4>{{ dip.nome }} {{ dip.cognome }}</h4>
                <p class="dip-email">{{ dip.email }}</p>
              </div>
            </div>
            <div class="dip-footer">
              <span class="glass-badge" :class="dip.inTurno ? 'badge-attivo' : 'badge-inattivo'">
                <span class="dot" :class="dip.inTurno ? 'dot-verde' : 'dot-grigio'"></span>
                {{ dip.inTurno ? 'In Turno' : 'Fuori Turno' }}
              </span>
            </div>
          </div>
        </div>

        <div v-else class="empty-state-glass">
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

.btn-glass-ghost { background: rgba(255, 255, 255, 0.5); border: 1px solid rgba(255, 255, 255, 0.6); color: #475569; font-weight: 600; font-size: 13px; cursor: pointer; padding: 10px 16px; border-radius: 12px; transition: all 0.2s; display: flex; align-items: center; gap: 8px;}
.btn-glass-ghost svg { width: 16px; height: 16px;}
.btn-glass-ghost:hover { background: rgba(255, 255, 255, 0.8); color: #0f172a; transform: translateY(-1px);}

.content-area { padding: 40px; max-width: 1200px; margin: 0 auto; width: 100%; box-sizing: border-box;}

/* ------- IL VETRO CHIARO ------- */
.glass-card { background: rgba(255, 255, 255, 0.65); backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px); border: 1px solid rgba(255, 255, 255, 0.5); box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.1); border-radius: 20px; }
.interactive-card { transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); }
.interactive-card:hover { transform: translateY(-5px); background: rgba(255, 255, 255, 0.85); box-shadow: 0 20px 40px -12px rgba(0, 0, 0, 0.15); }

/* ------- STATISTICHE ------- */
.stats-row { display: flex; gap: 24px; margin-bottom: 32px; }
.stat-card { flex: 1; padding: 24px; display: flex; align-items: center; gap: 20px; }

.stat-icon-wrapper { width: 56px; height: 56px; border-radius: 16px; display: flex; align-items: center; justify-content: center; background: rgba(255, 255, 255, 0.8); border: 1px solid rgba(255,255,255,0.5); }
.stat-icon-wrapper svg { width: 28px; height: 28px; }

.blue-glow { color: #3b82f6; box-shadow: 0 0 20px rgba(59, 130, 246, 0.2); }
.green-glow { color: #10b981; box-shadow: 0 0 20px rgba(16, 185, 129, 0.2); }
.slate-glow { color: #64748b; box-shadow: 0 0 20px rgba(100, 116, 139, 0.2); }

.stat-info { display: flex; flex-direction: column; }
.stat-title { font-size: 13px; font-weight: 600; color: #64748b; text-transform: uppercase; letter-spacing: 0.5px; }
.stat-value { margin: 4px 0 0 0; font-size: 32px; font-weight: 800; }
.text-slate { color: #64748b; }
.text-green { color: #10b981; }
.text-dark { color: #0f172a; }

/* ------- GRIGLIA DIPENDENTI ------- */
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.section-header h3 { margin: 0; font-size: 18px; color: #0f172a; font-weight: 700; }
.refresh-hint { font-size: 12px; color: #64748b; font-weight: 500;}

.dipendenti-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 24px; }
.dip-card { padding: 24px; transition: all 0.3s; display: flex; flex-direction: column; gap: 16px;}
.dip-card:hover { transform: translateY(-4px); box-shadow: 0 15px 30px -10px rgba(0,0,0,0.1); }

/* Card attiva: bordo verde acqua traslucido */
.card-attivo { border-color: rgba(16, 185, 129, 0.4); background: rgba(255, 255, 255, 0.85);}
.card-inattivo { opacity: 0.85; filter: grayscale(20%); }

.dip-header { display: flex; align-items: center; gap: 14px; }
.dip-avatar { width: 48px; height: 48px; border-radius: 50%; background: linear-gradient(135deg, #6366f1, #a855f7); color: white; display: flex; align-items: center; justify-content: center; font-size: 18px; font-weight: 700; flex-shrink: 0; box-shadow: 0 4px 10px rgba(99, 102, 241, 0.2);}
.dip-info h4 { margin: 0 0 4px 0; font-size: 16px; font-weight: 700; color: #0f172a; letter-spacing: -0.3px;}
.dip-email { margin: 0; font-size: 12px; color: #64748b; font-weight: 500;}

.dip-footer { display: flex; justify-content: flex-end; margin-top: auto;}

/* Badges */
.glass-badge { display: inline-flex; align-items: center; gap: 6px; padding: 6px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; border: 1px solid rgba(255,255,255,0.5); backdrop-filter: blur(4px); }
.badge-attivo { background-color: rgba(16, 185, 129, 0.15); color: #059669; border-color: rgba(16, 185, 129, 0.3); }
.badge-inattivo { background-color: rgba(255, 255, 255, 0.5); color: #64748b; border-color: rgba(0,0,0,0.1); }

.dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.dot-verde { background: #10b981; box-shadow: 0 0 0 2px rgba(16,185,129,0.25); }
.dot-grigio { background: #94a3b8; }

.empty-state-glass { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 60px 0; color: #64748b; }
.empty-state-glass svg { width: 56px; height: 56px; margin-bottom: 16px; opacity: 0.4; }
.empty-state-glass p { margin: 0; font-size: 15px; font-weight: 500; }
</style>