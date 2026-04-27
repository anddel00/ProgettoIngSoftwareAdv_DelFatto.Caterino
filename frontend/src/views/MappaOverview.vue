<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '../api';
import AdminSidebar from '../components/AdminSidebar.vue';

const reparti = ref([]);
const loading = ref(true);
const router = useRouter();

const vaiAlReparto = (id) => {
  router.push(`/Mappa/reparto/${id}`);
};

const getStileReparto = (reparto) => {
  // ABBIAMO RIDOTTO IL FATTORE DI SCALA (da 5 a 3.5)
  // Questo rende i reparti significativamente più grandi sulla griglia
  const fattoreDiScala = 3.5;
  const spanX = Math.max(3, Math.round(reparto.maxX / fattoreDiScala));
  const spanY = Math.max(3, Math.round(reparto.maxY / fattoreDiScala));

  let backgroundColor = '#f8fafc';
  let borderColor = '#94a3b8';

  if (reparto.temperatura < 0) {
    backgroundColor = '#eff6ff'; borderColor = '#2563eb';
  } else if (reparto.temperatura >= 0 && reparto.temperatura <= 8) {
    backgroundColor = '#ecfeff'; borderColor = '#0ea5e9';
  } else if (reparto.id === 2) {
    backgroundColor = '#fffbeb'; borderColor = '#f59e0b';
  } else if (reparto.id === 1) {
    backgroundColor = '#f5f3ff'; borderColor = '#8b5cf6';
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
  <div class="dashboard-layout">
    <AdminSidebar />

    <div class="main-content">
      <header class="topbar">
        <div class="header-left">
          <div class="divider"></div>
          <h1>Planimetria Magazzino</h1>
        </div>
      </header>

      <main class="content-area">
        <div class="info-banner">
          <p>👇 Seleziona un reparto per gestire la disposizione interna degli scaffali.</p>
        </div>

        <div v-if="loading" class="loading-overlay">
          <div class="spinner"></div> Caricamento...
        </div>

        <div v-else class="warehouse-floor-wrapper">
          <div class="warehouse-floor">
            <div
                v-for="reparto in reparti"
                :key="reparto.id"
                class="reparto-block"
                :style="getStileReparto(reparto)"
                @click="vaiAlReparto(reparto.id)"
            >
              <span class="id-badge">ID #{{ reparto.id }}</span>

              <div class="block-content">
                <h3>{{ reparto.nome }}</h3>
                <div class="reparto-stats">
                  <span class="stat-badge temp" :class="{ 'cold': reparto.temperatura <= 8, 'freezing': reparto.temperatura < 0 }">
                    {{ reparto.temperatura }}°C
                  </span>
                </div>
              </div>
            </div>
          </div>
          <div class="gate">ZONA CARICO / SCARICO MERCI</div>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* --- LAYOUT STRUTTURALE --- */
.dashboard-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  background-color: #f8fafc;
  overflow: hidden;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100vh;
  min-width: 0;
}

.topbar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  padding: 15px 30px;
  background-color: white;
  border-bottom: 1px solid #e2e8f0;
  height: 70px;
  box-sizing: border-box;
}

.content-area {
  flex: 1;
  padding: 15px 20px; /* Ridotto il padding per dare più spazio alla mappa */
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.divider {
  width: 4px; height: 20px;
  background-color: #3b82f6;
  margin-right: 15px;
  border-radius: 2px;
}

.header-left h1 { margin: 0; font-size: 1.3rem; color: #1e293b; font-weight: 700; }

.info-banner {
  background: #eff6ff;
  color: #1e40af;
  padding: 8px 15px;
  border-radius: 8px;
  margin-bottom: 10px;
  font-size: 0.85rem;
  border-left: 4px solid #3b82f6;
}

/* --- PLANIMETRIA --- */
.warehouse-floor-wrapper {
  flex: 1;
  position: relative;
  background-color: #f1f5f9;
  padding: 20px; /* Ridotto il padding interno */
  border-radius: 12px;
  border: 4px solid #cbd5e1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.warehouse-floor {
  display: grid;
  /* RIDOTTE LE COLONNE (da 12 a 10) per far apparire i reparti più grandi */
  grid-template-columns: repeat(10, 1fr);
  grid-template-rows: repeat(8, 1fr);
  grid-auto-flow: dense;
  gap: 15px;
  height: 100%;
  width: 100%;
}

/* --- REPARTO --- */
.reparto-block {
  position: relative;
  border-style: solid;
  border-width: 2px;
  border-left-width: 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 2px 2px 5px rgba(0,0,0,0.03);
}

.reparto-block:hover {
  transform: translateY(-2px);
  filter: brightness(0.98);
  box-shadow: 0 8px 15px -5px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.id-badge {
  position: absolute;
  top: 8px; right: 8px;
  background: rgba(15, 23, 42, 0.8);
  color: white;
  font-size: 0.65rem;
  padding: 2px 6px;
  border-radius: 4px;
}

.block-content h3 {
  margin: 0 0 5px 0;
  font-size: 1.1rem; /* Leggermente più grande */
  text-transform: uppercase;
  color: #0f172a;
  font-weight: 800;
}

.stat-badge {
  font-size: 0.8rem;
  font-weight: 700;
  padding: 4px 10px;
  background: white;
  border-radius: 5px;
  border: 1px solid rgba(0,0,0,0.05);
}

.gate {
  position: absolute;
  bottom: -4px;
  left: 50%;
  transform: translateX(-50%);
  background: #f1f5f9;
  padding: 0 25px;
  font-weight: 700;
  color: #94a3b8;
  font-size: 0.75rem;
  letter-spacing: 2px;
  border-left: 2px dashed #94a3b8;
  border-right: 2px dashed #94a3b8;
}
</style>