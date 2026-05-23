<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import AdminSidebar from '../components/AdminSidebar.vue'

const ordini = ref([])
const isFetching = ref(false)
const isAllocating = ref({}) // Tiene traccia di quali ordini stanno venendo allocati

const fetchOrdini = async () => {
  isFetching.value = true
  try {
    const res = await api.get('/api/ordini/tutti')
    // Ordiniamo per data decrescente (i più recenti in alto)
    ordini.value = res.data.sort((a, b) => new Date(b.dataCreazione) - new Date(a.dataCreazione))
  } catch (err) {
    console.error("Errore recupero ordini:", err)
  } finally {
    isFetching.value = false
  }
}

const generaOrdineCasuale = async () => {
  try {
    const res = await api.post('/api/ordini/genera')
    // Aggiungi in cima alla lista
    ordini.value.unshift(res.data)
  } catch (err) {
    console.error("Errore generazione ordine:", err)
    alert("Errore durante la generazione dell'ordine")
  }
}

const allocaOrdine = async (id) => {
  isAllocating.value[id] = true
  try {
    await api.post(`/api/ordini/${id}/alloca`)
    alert(`Ordine #${id} allocato con successo! I task di prelievo (FEFO) sono stati generati.`)
    await fetchOrdini() // Ricarica per aggiornare gli stati
  } catch (err) {
    console.error("Errore allocazione:", err)
    alert(err.response?.data || "Errore durante l'allocazione. Stock insufficiente?")
  } finally {
    isAllocating.value[id] = false
  }
}

const eliminaOrdine = async (id) => {
  if (!confirm(`Sei sicuro di voler eliminare l'ordine #${id}?`)) return
  try {
    await api.delete(`/api/ordini/${id}`)
    await fetchOrdini()
  } catch (err) {
    console.error("Errore eliminazione:", err)
    alert(err.response?.data || "Impossibile eliminare l'ordine.")
  }
}

const annullaOrdine = async (id) => {
  if (!confirm(`Sei sicuro di voler ANNULLARE l'ordine #${id}? Tutti i task verranno annullati e le scorte ripristinate.`)) return
  isAllocating.value[id] = true
  try {
    await api.post(`/api/ordini/${id}/annulla`)
    alert(`Ordine #${id} annullato con successo. Scorte ripristinate.`)
    await fetchOrdini()
  } catch (err) {
    console.error("Errore annullamento:", err)
    alert(err.response?.data || "Impossibile annullare l'ordine. (Forse alcuni task sono già stati completati?)")
  } finally {
    isAllocating.value[id] = false
  }
}

const getStatoClass = (stato) => {
  if (stato === 'DA_ALLOCARE') return 'stato-da-allocare'
  if (stato === 'IN_LAVORAZIONE') return 'stato-in-lavorazione'
  if (stato === 'PRONTO') return 'stato-pronto'
  if (stato === 'SPEDITO') return 'stato-spedito'
  return ''
}

const formatData = (dataStr) => {
  if (!dataStr) return ''
  const d = new Date(dataStr)
  return d.toLocaleString('it-IT', { 
    day: '2-digit', month: '2-digit', year: 'numeric', 
    hour: '2-digit', minute: '2-digit' 
  })
}

onMounted(() => {
  fetchOrdini()
})
</script>

<template>
  <div class="glass-dashboard-layout">
    <div class="glass-bg-blob blob-1"></div>
    <div class="glass-bg-blob blob-2"></div>
    <div class="glass-bg-blob blob-3"></div>

    <AdminSidebar class="glass-sidebar" />

    <main class="main-content">
      <header class="glass-topbar">
        <div class="topbar-left">
          <h1>Gestione Ordini in Uscita</h1>
        </div>
        <div class="topbar-right">
          <button @click="generaOrdineCasuale" class="btn-primary-glass">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
            </svg>
            Genera Ordine Casuale
          </button>
        </div>
      </header>

      <div class="content-area">
        <div class="glass-card list-card">
          <div class="card-header">
            <h3>Archivio Ordini ({{ ordini.length }} totali)</h3>
            <span class="badge glass-badge">Ordini di Spedizione</span>
          </div>

          <div v-if="ordini.length > 0" class="table-container">
            <table class="glass-table">
              <thead>
                <tr>
                  <th>ID Ordine</th>
                  <th>Data Creazione</th>
                  <th>Prodotti Richiesti</th>
                  <th>Stato</th>
                  <th class="text-right">Azioni</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="ordine in ordini" :key="ordine.id">
                  <td class="id-cell">#ORD-{{ ordine.id }}</td>
                  <td>{{ formatData(ordine.dataCreazione) }}</td>
                  
                  <!-- RIGHE ORDINE -->
                  <td class="td-righe">
                    <div class="righe-list">
                      <div v-for="riga in ordine.righe" :key="riga.id" class="riga-badge">
                        <span class="riga-nome">{{ riga.nomeProdotto }}</span>
                        <span class="riga-qta">{{ riga.quantitaRichiesta }} pz</span>
                      </div>
                    </div>
                  </td>

                  <td>
                    <span class="stato-badge" :class="getStatoClass(ordine.statoOrdine)">
                      {{ ordine.statoOrdine?.replace('_', ' ') }}
                    </span>
                  </td>

                  <td class="action-cell">
                    <div style="display: flex; gap: 8px; justify-content: flex-end;">
                      <template v-if="ordine.statoOrdine === 'DA_ALLOCARE'">
                        <button @click="allocaOrdine(ordine.id)"
                                :disabled="isAllocating[ordine.id]"
                                class="btn-success-glass">
                          {{ isAllocating[ordine.id] ? 'Avvio...' : 'Avvia Ordine' }}
                        </button>
                        <button @click="eliminaOrdine(ordine.id)"
                                class="btn-danger-glass">
                          Elimina
                        </button>
                      </template>

                      <template v-else-if="ordine.statoOrdine === 'IN_LAVORAZIONE'">
                        <button @click="annullaOrdine(ordine.id)"
                                :disabled="isAllocating[ordine.id]"
                                class="btn-warning-glass">
                          {{ isAllocating[ordine.id] ? 'Annullamento...' : 'Annulla Ordine' }}
                        </button>
                      </template>

                      <span v-else class="text-muted">Gestito dai Task</span>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-else-if="!isFetching" class="empty-state-glass">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"></path>
            </svg>
            <p>Nessun ordine presente. Genera un ordine casuale per iniziare.</p>
          </div>

          <div v-else class="empty-state-glass">
            <p>Caricamento in corso...</p>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');

.glass-dashboard-layout {
  display: flex; height: 100vh; width: 100vw;
  background-color: #e2e8f0; font-family: 'Inter', sans-serif;
  margin: 0; color: #1e293b; position: relative; overflow: hidden;
}

.glass-bg-blob { position: absolute; border-radius: 50%; filter: blur(80px); z-index: 0; opacity: 0.6; }
.blob-1 { top: -10%; left: -10%; width: 500px; height: 500px; background: #93c5fd; }
.blob-2 { bottom: -20%; right: -10%; width: 600px; height: 600px; background: #c4b5fd; }
.blob-3 { top: 40%; left: 40%; width: 400px; height: 400px; background: #86efac; opacity: 0.4; }

.main-content, .glass-sidebar { position: relative; z-index: 10; }
.main-content { flex-grow: 1; display: flex; flex-direction: column; overflow-y: auto; }

.glass-topbar {
  background: rgba(255, 255, 255, 0.4); backdrop-filter: blur(10px);
  padding: 20px 40px; display: flex; justify-content: space-between; align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
}

.topbar-left h1 { margin: 0; font-size: 24px; font-weight: 700; color: #0f172a; letter-spacing: -0.5px; }
.content-area { padding: 40px; max-width: 1200px; margin: 0 auto; width: 100%; box-sizing: border-box; }

.btn-primary-glass {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white; border: none; padding: 10px 20px; border-radius: 12px;
  cursor: pointer; font-weight: 600; font-size: 14px;
  display: flex; align-items: center; gap: 8px;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(99, 102, 241, 0.3);
}
.btn-primary-glass svg { width: 18px; height: 18px; }
.btn-primary-glass:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(99, 102, 241, 0.4); }

.glass-card {
  background: rgba(255, 255, 255, 0.65); backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.5); box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.1);
  border-radius: 20px;
}
.list-card { display: flex; flex-direction: column; overflow: hidden; }

.card-header {
  padding: 24px; display: flex; justify-content: space-between; align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.3); background: rgba(255, 255, 255, 0.4);
  flex-wrap: wrap; gap: 16px;
}
.card-header h3 { margin: 0; font-size: 18px; font-weight: 700; color: #0f172a; }

.badge.glass-badge {
  background: rgba(99, 102, 241, 0.1);
  color: #4f46e5; padding: 4px 12px; border-radius: 12px; font-size: 13px; font-weight: 600;
  border: 1px solid rgba(99, 102, 241, 0.2);
}

.table-container { overflow-x: auto; }
.glass-table { width: 100%; border-collapse: collapse; }
.glass-table th, .glass-table td { padding: 16px 24px; text-align: left; border-bottom: 1px solid rgba(255, 255, 255, 0.3); font-size: 14px; }
.glass-table th { font-weight: 600; color: #475569; text-transform: uppercase; font-size: 12px; letter-spacing: 0.5px; }
.glass-table tbody tr { transition: background 0.2s; }
.glass-table tbody tr:hover { background: rgba(255, 255, 255, 0.3); }
.text-right { text-align: right; }

.id-cell { color: #64748b; font-family: monospace; font-weight: 600; }

.td-righe { min-width: 250px; }
.righe-list { display: flex; flex-direction: column; gap: 6px; }
.riga-badge {
  background: rgba(255, 255, 255, 0.7); border: 1px solid rgba(0, 0, 0, 0.05);
  display: flex; justify-content: space-between; align-items: center;
  padding: 6px 12px; border-radius: 8px; font-size: 13px;
}
.riga-nome { font-weight: 600; color: #334155; }
.riga-qta { font-weight: 700; color: #6366f1; background: rgba(99, 102, 241, 0.1); padding: 2px 8px; border-radius: 10px; }

.stato-badge { font-size: 11px; font-weight: 700; padding: 5px 12px; border-radius: 20px; display: inline-block; }
.stato-da-allocare { background: #fee2e2; color: #b91c1c; border: 1px solid #fecaca;}
.stato-in-lavorazione { background: #dbeafe; color: #1d4ed8; border: 1px solid #bfdbfe;}
.stato-pronto { background: #fef08a; color: #854d0e; border: 1px solid #fde047;}
.stato-spedito { background: #d1fae5; color: #065f46; border: 1px solid #a7f3d0;}

.action-cell { text-align: right; }
.btn-success-glass { 
  background: linear-gradient(135deg, #10b981, #059669); color: white; border: none; 
  padding: 8px 16px; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 13px; 
  transition: 0.3s; box-shadow: 0 4px 10px rgba(16,185,129,0.3);
}
.btn-success-glass:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 6px 15px rgba(16,185,129,0.4); }
.btn-success-glass:disabled { opacity: 0.5; cursor: not-allowed; box-shadow: none; }

.btn-danger-glass { 
  background: linear-gradient(135deg, #ef4444, #dc2626); color: white; border: none; 
  padding: 8px 16px; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 13px; 
  transition: 0.3s; box-shadow: 0 4px 10px rgba(239,68,68,0.3);
}
.btn-danger-glass:hover { transform: translateY(-2px); box-shadow: 0 6px 15px rgba(239,68,68,0.4); }

.btn-warning-glass { 
  background: linear-gradient(135deg, #f59e0b, #d97706); color: white; border: none; 
  padding: 8px 16px; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 13px; 
  transition: 0.3s; box-shadow: 0 4px 10px rgba(245,158,11,0.3);
}
.btn-warning-glass:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 6px 15px rgba(245,158,11,0.4); }
.btn-warning-glass:disabled { opacity: 0.5; cursor: not-allowed; box-shadow: none; }

.text-muted { color: #94a3b8; font-style: italic; font-size: 12px; }

.empty-state-glass {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 60px 0; color: #64748b;
}
.empty-state-glass svg { width: 48px; height: 48px; margin-bottom: 16px; opacity: 0.4; }
.empty-state-glass p { margin: 0; font-size: 15px; font-weight: 500; }
</style>
