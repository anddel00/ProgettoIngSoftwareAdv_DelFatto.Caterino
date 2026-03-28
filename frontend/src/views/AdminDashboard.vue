<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

import AdminSidebar from '../components/AdminSidebar.vue'

const router = useRouter()
const nomeUtente = ref('')

onMounted(() => {
  // Recuperiamo il nome dell'utente loggato salvato durante il login
  nomeUtente.value = localStorage.getItem('nomeUtente') || 'Amministratore'
})

const logout = () => {
  // Puliamo i dati e torniamo al login
  localStorage.clear()
  router.push('/')
}
</script>

<template>
  <div class="dashboard-layout">

    <AdminSidebar /> <!--SIDEBAR CONDIVISA TRA LE PAGINE-->

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
            <h2 class="stat-value">0</h2>
          </div>
          <div class="stat-card">
            <span class="stat-title">Dipendenti in Turno</span>
            <h2 class="stat-value">0</h2>
          </div>
          <div class="stat-card">
            <span class="stat-title">Efficienza Odierna</span>
            <h2 class="stat-value">--%</h2>
          </div>
        </div>

        <div class="dashboard-cards">
          <div class="card">
            <div class="card-header">
              <h3>Ultimi Eventi</h3>
              <button class="btn-ghost">Vedi tutti</button>
            </div>
            <div class="empty-state-modern">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path></svg>
              <p>Nessun task recente al momento.</p>
            </div>
          </div>

          <div class="card">
            <div class="card-header">
              <h3>Arrivi Previsti</h3>
              <button class="btn-ghost">Gestisci</button>
            </div>
            <div class="empty-state-modern">
              <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M8 7v8a2 2 0 002 2h6M8 7V5a2 2 0 012-2h4.586a1 1 0 01.707.293l4.414 4.414a1 1 0 01.293.707V15a2 2 0 01-2 2h-2M8 7H6a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2v-2"></path></svg>
              <p>Nessun nuovo arrivo registrato.</p>
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
</style>