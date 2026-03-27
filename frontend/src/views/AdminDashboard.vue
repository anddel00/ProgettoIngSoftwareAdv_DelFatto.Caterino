<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

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

    <aside class="sidebar">
      <div class="sidebar-header">
        <h2>📦 WMS Admin</h2>
      </div>

      <nav class="sidebar-nav">
        <div class="nav-item active">🏠 Home</div>
        <div class="nav-item">🗺️ Mappa</div>
        <div class="nav-item" @click="router.push('/GestioneDipendenti')">👥 Dipendenti</div>
        <div class="nav-item">🕒 Turni</div>
      </nav>

      <div class="sidebar-footer">
        <button @click="logout" class="btn-logout">🚪 Esci</button>
      </div>
    </aside>

    <main class="main-content">
      <header class="topbar">
        <h1>Benvenuto, {{ nomeUtente }}! 👋</h1>
      </header>

      <div class="content-area">
        <div class="dashboard-cards">
          <div class="card">
            <h3>📋 Ultimi Eventi</h3>
            <p class="empty-state">Nessun task recente al momento.</p>
          </div>

          <div class="card">
            <h3>🚚 Arrivi Previsti</h3>
            <p class="empty-state">Nessun nuovo arrivo registrato.</p>
          </div>
        </div>
      </div>
    </main>

  </div>
</template>

<style scoped>
/* ------- RESET E LAYOUT PRINCIPALE ------- */
.dashboard-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  background-color: #f4f6f9;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  margin: 0; /* Rimuove margini di default */
}

/* ------- SIDEBAR ------- */
.sidebar {
  width: 250px;
  background-color: #2c3e50;
  color: white;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 5px rgba(0,0,0,0.1);
}

.sidebar-header {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #34495e;
}

.sidebar-nav {
  flex-grow: 1;
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.nav-item {
  padding: 15px 25px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s ease;
  color: #ecf0f1;
}

.nav-item:hover {
  background-color: #34495e;
  padding-left: 30px; /* Piccolo effetto di scorrimento al passaggio del mouse */
}

.nav-item.active {
  background-color: #27ae60;
  font-weight: bold;
  border-left: 5px solid #2ecc71;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid #34495e;
}

.btn-logout {
  width: 100%;
  padding: 10px;
  background-color: #e74c3c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: background 0.3s;
}

.btn-logout:hover {
  background-color: #c0392b;
}

/* ------- CONTENUTO PRINCIPALE ------- */
.main-content {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.topbar {
  background-color: white;
  padding: 20px 30px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
}

.topbar h1 {
  margin: 0;
  font-size: 24px;
  color: #2c3e50;
}

.content-area {
  padding: 30px;
}

/* ------- CARD DELLA DASHBOARD ------- */
.dashboard-cards {
  display: flex;
  gap: 20px;
}

.card {
  background: white;
  flex: 1;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
  border-top: 4px solid #3498db;
}

.card h3 {
  margin-top: 0;
  color: #2c3e50;
}

.empty-state {
  color: #7f8c8d;
  font-style: italic;
}
</style>