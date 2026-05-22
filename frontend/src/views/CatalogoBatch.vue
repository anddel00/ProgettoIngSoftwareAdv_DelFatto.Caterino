<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import api from '../api'
import AdminSidebar from '../components/AdminSidebar.vue'
import GlassSelect from '../components/GlassSelect.vue'

const lotti = ref([])
const categorie = ref([])
const page = ref(0)
const size = ref(50)
const search = ref('')
const idCategoria = ref('')
const statoSistemazione = ref('')
const sortByScadenza = ref('')

const totalPages = ref(0)
const totalElements = ref(0)
const isFetching = ref(false)

let debounceTimeout = null

const statoOptions = [
  { value: '', label: 'Tutti gli Stati' },
  { value: 'SISTEMATI', label: 'Sistemati' },
  { value: 'ATTESA', label: 'In Attesa' }
]

const ordinamentoOptions = [
  { value: '', label: 'Ordina per...' },
  { value: 'ASC', label: 'Meno recente (ASC)' },
  { value: 'DESC', label: 'Più recente (DESC)' }
]

const categoriaOptions = computed(() => {
  const base = [{ value: '', label: 'Tutte le Categorie' }];
  const cats = categorie.value.map(c => ({ value: c.id, label: c.nome }));
  return [...base, ...cats];
});

// Carica le etichette per il filtro categorie
const fetchCategorie = async () => {
  try {
    const res = await api.get('/api/etichette/carica')
    categorie.value = res.data
  } catch (err) {
    console.error("Errore recupero categorie:", err)
  }
}

const fetchCatalogo = async (resetPage = false) => {
  if (resetPage) {
    page.value = 0
  }
  isFetching.value = true
  try {
    const params = {
      page: page.value,
      size: size.value,
      search: search.value,
      sortByScadenza: sortByScadenza.value
    }
    // Aggiungiamo idCategoria solo se non è vuoto
    if (idCategoria.value !== '') {
      params.idCategoria = idCategoria.value
    }
    // Aggiungiamo statoSistemazione solo se non è vuoto
    if (statoSistemazione.value !== '') {
      params.statoSistemazione = statoSistemazione.value
    }

    const res = await api.get('/api/batch-prodotti/catalogo', { params })
    lotti.value = res.data.content
    totalPages.value = res.data.totalPages
    totalElements.value = res.data.totalElements
  } catch (err) {
    console.error("Errore recupero catalogo:", err)
  } finally {
    isFetching.value = false
  }
}

// Debounce nativo per la ricerca testuale
const onSearchInput = () => {
  if (debounceTimeout) clearTimeout(debounceTimeout)
  debounceTimeout = setTimeout(() => {
    fetchCatalogo(true)
  }, 400)
}

// Ricarica automaticamente se cambiano le select
watch([idCategoria, statoSistemazione, sortByScadenza], () => {
  fetchCatalogo(true)
})

const prevPage = () => {
  if (page.value > 0) {
    page.value--
    fetchCatalogo()
  }
}

const nextPage = () => {
  if (page.value < totalPages.value - 1) {
    page.value++
    fetchCatalogo()
  }
}

onMounted(() => {
  fetchCategorie()
  fetchCatalogo()
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
          <h1>Catalogo Lotti Avanzato</h1>
        </div>
      </header>

      <div class="content-area">
        <div class="glass-card list-card">
          <div class="card-header">
            <h3>Archivio Lotti ({{ totalElements }} totali)</h3>
            
            <div class="filters-container">
              <!-- Filtro Ricerca Testuale -->
              <div class="search-box">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" class="search-icon">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
                </svg>
                <input type="text" v-model="search" @input="onSearchInput" placeholder="Cerca per prodotto..." class="glass-input" />
              </div>

              <!-- Filtro Stato Sistemazione -->
              <GlassSelect 
                v-model="statoSistemazione" 
                :options="statoOptions" 
                placeholder="Tutti gli Stati" 
              />

              <!-- Filtro Categoria -->
              <GlassSelect 
                v-model="idCategoria" 
                :options="categoriaOptions" 
                placeholder="Tutte le Categorie" 
              />

              <!-- Filtro Ordinamento Data -->
              <GlassSelect 
                v-model="sortByScadenza" 
                :options="ordinamentoOptions" 
                placeholder="Ordina per..." 
              />
            </div>
          </div>

          <div v-if="lotti.length > 0" class="table-container">
            <table class="glass-table">
              <thead>
                <tr>
                  <th>ID Lotto</th>
                  <th>Prodotto</th>
                  <th>Posizione Fisica</th>
                  <th>Quantità</th>
                  <th>Scadenza</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="lotto in lotti" :key="lotto.idLotto">
                  <td>#L-{{ lotto.idLotto }}</td>
                  <td><strong>{{ lotto.nomeProdotto }}</strong></td>
                  
                  <!-- POSIZIONE -->
                  <td class="td-posizione">
                    <template v-if="lotto.posizioni && lotto.posizioni.length > 0">
                      <div class="posizioni-list">
                        <span v-for="(pos, idx) in lotto.posizioni" :key="idx" class="posizione-badge">
                          {{ pos }}
                        </span>
                      </div>
                    </template>
                    <template v-else>
                      <span class="badge-in-attesa">
                        In attesa di sistemazione
                      </span>
                    </template>
                  </td>

                  <td>{{ lotto.quantitaDisponibile }}</td>
                  <td>{{ lotto.dataScadenza }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-else-if="!isFetching" class="empty-state-glass">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path>
            </svg>
            <p>Nessun lotto trovato.</p>
          </div>

          <div v-else class="empty-state-glass">
            <p>Caricamento in corso...</p>
          </div>

          <!-- Pagination Footer -->
          <div class="pagination-footer" v-if="totalPages > 0">
            <button @click="prevPage" :disabled="page === 0" class="btn-glass-ghost">Precedente</button>
            <span class="page-info">Pagina {{ page + 1 }} di {{ totalPages }}</span>
            <button @click="nextPage" :disabled="page >= totalPages - 1" class="btn-glass-ghost">Successivo</button>
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

.filters-container {
  display: flex; gap: 12px; align-items: center; flex-wrap: wrap;
}

.search-box { position: relative; display: flex; align-items: center; }
.search-icon { width: 18px; height: 18px; position: absolute; left: 12px; color: #64748b; }

.glass-input {
  padding: 8px 12px; border-radius: 8px; border: 1px solid rgba(255, 255, 255, 0.6);
  background: rgba(255, 255, 255, 0.5); font-family: 'Inter', sans-serif; font-size: 14px;
  outline: none; transition: all 0.2s; color: #1e293b;
}
.glass-input { padding-left: 36px; width: 200px; }
.glass-input:focus { 
  background: rgba(255, 255, 255, 0.8); border-color: rgba(99, 102, 241, 0.5); 
  box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2); 
}

.table-container { overflow-x: auto; }
.glass-table { width: 100%; border-collapse: collapse; }
.glass-table th, .glass-table td { padding: 16px 24px; text-align: left; border-bottom: 1px solid rgba(255, 255, 255, 0.3); font-size: 14px; }
.glass-table th { font-weight: 600; color: #475569; text-transform: uppercase; font-size: 12px; letter-spacing: 0.5px; }
.glass-table tbody tr { transition: background 0.2s; }
.glass-table tbody tr:hover { background: rgba(255, 255, 255, 0.3); }

/* Stili per le posizioni */
.td-posizione { min-width: 250px; }
.posizioni-list { display: flex; flex-direction: column; gap: 6px; }
.posizione-badge {
  background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.2);
  color: #065f46; font-size: 12px; font-weight: 500; padding: 4px 8px; border-radius: 6px;
  width: fit-content;
}
.badge-in-attesa {
  background: rgba(245, 158, 11, 0.1); border: 1px solid rgba(245, 158, 11, 0.2);
  color: #92400e; font-size: 12px; font-weight: 600; padding: 4px 10px; border-radius: 12px;
  display: inline-block;
}

.empty-state-glass {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 60px 0; color: #64748b;
}
.empty-state-glass svg { width: 48px; height: 48px; margin-bottom: 16px; opacity: 0.4; }
.empty-state-glass p { margin: 0; font-size: 15px; font-weight: 500; }

.pagination-footer {
  padding: 20px 24px; display: flex; justify-content: space-between; align-items: center;
  border-top: 1px solid rgba(255, 255, 255, 0.3); background: rgba(255, 255, 255, 0.2);
}
.page-info { font-size: 14px; font-weight: 500; color: #475569; }

.btn-glass-ghost {
  background: rgba(255, 255, 255, 0.5); border: 1px solid rgba(255, 255, 255, 0.6);
  color: #475569; font-weight: 600; font-size: 13px; cursor: pointer; padding: 8px 16px; border-radius: 8px;
  transition: all 0.2s;
}
.btn-glass-ghost:hover:not(:disabled) { background: rgba(255, 255, 255, 0.8); color: #0f172a; transform: translateY(-1px); }
.btn-glass-ghost:disabled { opacity: 0.4; cursor: not-allowed; }
</style>
