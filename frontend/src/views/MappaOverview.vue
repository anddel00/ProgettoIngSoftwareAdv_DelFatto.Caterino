<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '../api';
import AdminSidebar from '../components/AdminSidebar.vue';

const reparti = ref([]);
const loading = ref(true);
const router = useRouter();
const nomeUtente = ref(sessionStorage.getItem('nomeUtente') || 'Admin');

const vaiAlReparto = (id) => {
  router.push(`/Mappa/reparto/${id}`);
};

const getStileReparto = (reparto) => {
  const fattoreDiScala = 3.5;
  // Per mantenere l'architettura vettoriale e occupare tutto lo spazio
  let spanX = Math.max(3, Math.round(reparto.maxX / fattoreDiScala));
  let spanY = Math.max(3, Math.round(reparto.maxY / fattoreDiScala));

  // Forza il reparto 2 (Secco) a essere disegnato verticalmente
  // (Allineato a quanto fatto in MappaMagazzino.vue)
  if (reparto.id === 2 && spanX > spanY) {
    const temp = spanX;
    spanX = spanY;
    spanY = temp;
  }

  // Colori in stile Glassmorphism per i blocchi
  let backgroundColor = 'rgba(255, 255, 255, 0.7)';
  let borderColor = 'rgba(148, 163, 184, 0.6)';

  if (reparto.temperatura < 0) {
    backgroundColor = 'rgba(219, 234, 254, 0.7)'; borderColor = '#3b82f6';
  } else if (reparto.temperatura >= 0 && reparto.temperatura <= 8) {
    backgroundColor = 'rgba(224, 242, 254, 0.7)'; borderColor = '#0ea5e9';
  } else if (reparto.id === 2) {
    backgroundColor = 'rgba(254, 243, 199, 0.7)'; borderColor = '#f59e0b';
  } else if (reparto.id === 1) {
    backgroundColor = 'rgba(243, 232, 255, 0.7)'; borderColor = '#8b5cf6';
  }

  return {
    gridColumn: `span ${spanX}`,
    gridRow: `span ${spanY}`,
    backgroundColor,
    borderColor
  };
};

onMounted(async () => {
  try {
    const res = await api.get('/api/reparti/carica');
    reparti.value = res.data;
  } catch (e) {
    console.error("Errore nel caricamento dei reparti:", e);
  } finally {
    loading.value = false;
  }
});
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
          <span class="greeting">Architettura WMS</span>
          <h1>Planimetria Magazzino</h1>
        </div>
        <div class="topbar-right">
          <div class="user-profile">
            <div class="avatar">{{ nomeUtente.charAt(0) }}</div>
            <span>{{ nomeUtente }}</span>
          </div>
        </div>
      </header>

      <div class="content-area">
        <div class="glass-info-banner">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
          <p>Seleziona un reparto per gestire la disposizione interna degli scaffali.</p>
        </div>

        <div v-if="loading" class="loading-overlay">
          <div class="spinner"></div> Caricamento mappa vettoriale...
        </div>

        <div v-else class="glass-card warehouse-floor-wrapper">
          <div class="warehouse-floor">
            <div
                v-for="reparto in reparti"
                :key="reparto.id"
                class="reparto-block glass-reparto"
                :style="getStileReparto(reparto)"
                @click="vaiAlReparto(reparto.id)"
            >
              <span class="glass-badge id-badge">ID #{{ reparto.id }}</span>

              <div class="block-content">
                <h3>{{ reparto.nome }}</h3>
                <div class="reparto-stats">
                  <span class="glass-stat-badge temp" :class="{ 'cold': reparto.temperatura <= 8, 'freezing': reparto.temperatura < 0 }">
                    {{ reparto.temperatura }}°C
                  </span>
                </div>
              </div>
            </div>
          </div>
          <div class="gate-glass">ZONA CARICO / SCARICO MERCI</div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');

/* ------- LAYOUT & BACKGROUND CHIARO ------- */
.glass-dashboard-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  background-color: #e2e8f0;
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

/* ------- TOPBAR CHIARA (Uguale a GestioneTask) ------- */
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

.content-area {
  flex: 1;
  padding: 30px 40px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

/* --- BANNER INFORMATIVO GLASS --- */
.glass-info-banner {
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  color: #1e40af;
  padding: 12px 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  font-size: 0.9rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
}
.glass-info-banner svg { width: 20px; height: 20px; color: #3b82f6;}
.glass-info-banner p { margin: 0; }

/* --- CONTENITORE PLANIMETRIA --- */
.glass-card {
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 15px 35px -5px rgba(0, 0, 0, 0.1);
  border-radius: 20px;
}

.warehouse-floor-wrapper {
  flex: 1;
  position: relative;
  padding: 30px;
  display: flex;
  flex-direction: column;
  min-height: 0;
  /* Il bordo simula le pareti esterne della struttura */
  border: 4px solid rgba(255, 255, 255, 0.9);
}

.warehouse-floor {
  display: grid;
  grid-template-columns: repeat(10, 1fr);
  grid-template-rows: repeat(8, 1fr);
  grid-auto-flow: dense;
  gap: 15px;
  height: 100%;
  width: 100%;
}

/* --- I REPARTI (BLOCCHI) --- */
.glass-reparto {
  position: relative;
  border-style: solid;
  border-width: 1px;
  border-left-width: 8px; /* Mantiene l'idea della planimetria strutturale */
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  /* Sfondo gestito in JS */
  backdrop-filter: blur(8px);
  box-shadow: 0 4px 10px rgba(0,0,0,0.03);
}

.glass-reparto:hover {
  transform: translateY(-4px) scale(1.01);
  box-shadow: 0 15px 30px -10px rgba(0, 0, 0, 0.15);
  border-color: rgba(255,255,255,0.9) !important; /* Brilla al passaggio */
  z-index: 10;
}

.id-badge {
  position: absolute;
  top: 12px; right: 12px;
}

.glass-badge {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(4px);
  color: #0f172a;
  border: 1px solid rgba(255,255,255,0.5);
  font-size: 0.65rem;
  font-weight: 800;
  padding: 4px 8px;
  border-radius: 6px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
}

.block-content h3 {
  margin: 0 0 8px 0;
  font-size: 1.25rem;
  text-transform: uppercase;
  color: #0f172a;
  font-weight: 800;
  letter-spacing: -0.5px;
}

.reparto-stats {
  display: flex;
  justify-content: center;
}

.glass-stat-badge {
  font-size: 0.85rem;
  font-weight: 700;
  padding: 4px 12px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255,255,255,0.5);
  box-shadow: inset 0 2px 4px rgba(255,255,255,0.5), 0 2px 5px rgba(0,0,0,0.02);
}

.glass-stat-badge.temp.cold { color: #0284c7; }
.glass-stat-badge.temp.freezing { color: #2563eb; }
.glass-stat-badge.temp { color: #b45309; } /* Per il secco */

/* --- INGRESSO --- */
.gate-glass {
  position: absolute;
  bottom: -4px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  padding: 4px 30px;
  font-weight: 800;
  color: #475569;
  font-size: 0.75rem;
  letter-spacing: 3px;
  border-left: 2px dashed #94a3b8;
  border-right: 2px dashed #94a3b8;
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
}

.loading-overlay {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #475569;
  font-weight: 600;
}
</style>