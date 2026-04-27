<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import api from '../api';
import AdminSidebar from '../components/AdminSidebar.vue';

const mappaDati = ref([]);
const repartiDati = ref([]);
const scaffaliDati = ref([]);
const loading = ref(true);
const route = useRoute();
const router = useRouter();

// --- RIPARAZIONE PROPORZIONI REPARTO SECCO ---
const repartoVisuale = computed(() => {
  const idRepartoSelezionato = parseInt(route.params.id);
  const r = repartiDati.value.find(rep => rep.id === idRepartoSelezionato);

  if (!r) return null;
  const repCopy = { ...r };

  if (repCopy.id === 2 && repCopy.maxX > repCopy.maxY) {
    const temp = repCopy.maxX;
    repCopy.maxX = repCopy.maxY;
    repCopy.maxY = temp;
  }
  return repCopy;
});

const isEditing = ref(false);
const isSaving = ref(false);
const scaffaleSelezionato = ref(null);
let mappaOriginale = [];

// --- GESTIONE PIANI SINGOLI ---
const pianiScaffali = ref({});

// --- GESTIONE MODALE INFO ---
const scaffaleInfoAttivo = ref(null);

const apriInfoScaffale = (cella) => {
  scaffaleInfoAttivo.value = cella;
};

const chiudiInfoScaffale = () => {
  scaffaleInfoAttivo.value = null;
};

const getScaffaliPerReparto = (idReparto) => mappaDati.value.filter(cella => cella.idReparto === idReparto);
const getDatiTecniciScaffale = (idScaffale) => scaffaliDati.value.find(s => s.id === idScaffale) || null;

const getPiano = (idScaffale) => pianiScaffali.value[idScaffale] || 1;

const cambiaPiano = (idScaffale, delta) => {
  const info = getDatiTecniciScaffale(idScaffale);
  const max = info ? info.max_altezza : 1;
  const current = getPiano(idScaffale);
  if (current + delta >= 1 && current + delta <= max) {
    pianiScaffali.value[idScaffale] = current + delta;
  }
};

const haPiano = (cella) => {
  const info = getDatiTecniciScaffale(cella.idScaffale);
  if (!info) return true;
  return info.max_altezza >= getPiano(cella.idScaffale);
};

// --- LOGICA ANTEPRIMA SPOSTAMENTO (GHOST PREVIEW) ---
const hoverX = ref(null);
const hoverY = ref(null);
const isPosizioneValida = ref(false);

const impostaHover = (x, y) => {
  if (!scaffaleSelezionato.value) return;
  hoverX.value = x;
  hoverY.value = y;
  isPosizioneValida.value = controllaValidita(x, y);
};

const azzeraHover = () => {
  hoverX.value = null;
  hoverY.value = null;
};

const controllaValidita = (xNuovo, yNuovo) => {
  if (!scaffaleSelezionato.value || !repartoVisuale.value) return false;

  const info = getDatiTecniciScaffale(scaffaleSelezionato.value.idScaffale);
  const isOrizzontale = scaffaleSelezionato.value.orientamentoScaffale === 'ORIZZONTALE';
  const spanX = isOrizzontale ? info.max_righe : info.max_colonne;
  const spanY = isOrizzontale ? info.max_colonne : info.max_righe;

  if (xNuovo + spanX > repartoVisuale.value.maxX || yNuovo + spanY > repartoVisuale.value.maxY) {
    return false;
  }

  const altriScaffali = mappaDati.value.filter(
      c => c.idReparto === repartoVisuale.value.id && c.id !== scaffaleSelezionato.value.id
  );

  const isSovrapposto = altriScaffali.some(altro => {
    const altroInfo = getDatiTecniciScaffale(altro.idScaffale);
    if (!altroInfo) return false;

    const altroIsOrizzontale = altro.orientamentoScaffale === 'ORIZZONTALE';
    const altroSpanX = altroIsOrizzontale ? altroInfo.max_righe : altroInfo.max_colonne;
    const altroSpanY = altroIsOrizzontale ? altroInfo.max_colonne : altroInfo.max_righe;

    const buffer = 1;
    const altroSinistra = altro.coordinataX - buffer;
    const altroDestra = altro.coordinataX + altroSpanX + buffer;
    const altroSopra = altro.coordinataY - buffer;
    const altroSotto = altro.coordinataY + altroSpanY + buffer;

    const nuovoSinistra = xNuovo;
    const nuovoDestra = xNuovo + spanX;
    const nuovoSopra = yNuovo;
    const nuovoSotto = yNuovo + spanY;

    return !(nuovoSinistra >= altroDestra || nuovoDestra <= altroSinistra || nuovoSopra >= altroSotto || nuovoSotto <= altroSopra);
  });

  return !isSovrapposto;
};

const calcolaStilePreview = () => {
  if (!scaffaleSelezionato.value || hoverX.value === null) return { display: 'none' };

  const info = getDatiTecniciScaffale(scaffaleSelezionato.value.idScaffale);
  const isOrizzontale = scaffaleSelezionato.value.orientamentoScaffale === 'ORIZZONTALE';
  const spanX = isOrizzontale ? info.max_righe : info.max_colonne;
  const spanY = isOrizzontale ? info.max_colonne : info.max_righe;

  return {
    gridColumn: `${hoverX.value + 1} / span ${spanX}`,
    gridRow: `${hoverY.value + 1} / span ${spanY}`,
    width: `${(spanX * 50) - 4}px`,
    height: `${(spanY * 50) - 4}px`,
    margin: '2px'
  };
};

// --- LOGICA BASE DRAWING ---
const calcolaStileBuffer = (cella, reparto) => {
  const info = getDatiTecniciScaffale(cella.idScaffale);
  if (!info) return { display: 'none' };
  const isOrizzontale = cella.orientamentoScaffale === 'ORIZZONTALE';
  const spanX = isOrizzontale ? info.max_righe : info.max_colonne;
  const spanY = isOrizzontale ? info.max_colonne : info.max_righe;
  const startX = Math.max(1, cella.coordinataX);
  const expectedEndX = cella.coordinataX + spanX + 2;
  const actualEndX = Math.min(reparto.maxX + 1, expectedEndX);
  const startY = Math.max(1, cella.coordinataY);
  const expectedEndY = cella.coordinataY + spanY + 2;
  const actualEndY = Math.min(reparto.maxY + 1, expectedEndY);
  return {
    gridColumn: `${startX} / span ${actualEndX - startX}`,
    gridRow: `${startY} / span ${actualEndY - startY}`
  };
};

const calcolaStileScaffale = (cella) => {
  const info = getDatiTecniciScaffale(cella.idScaffale);
  if (!info) return { gridColumn: cella.coordinataX + 1, gridRow: cella.coordinataY + 1, width: '46px', height: '46px', margin: '2px' };
  const isOrizzontale = cella.orientamentoScaffale === 'ORIZZONTALE';
  const spanX = isOrizzontale ? info.max_righe : info.max_colonne;
  const spanY = isOrizzontale ? info.max_colonne : info.max_righe;
  return {
    gridColumn: `${cella.coordinataX + 1} / span ${spanX}`,
    gridRow: `${cella.coordinataY + 1} / span ${spanY}`,
    width: `${(spanX * 50) - 4}px`,
    height: `${(spanY * 50) - 4}px`,
    margin: '2px'
  };
};

const calcolaStileInnerGrid = (cella) => {
  const info = getDatiTecniciScaffale(cella.idScaffale);
  if (!info) return {};
  const isOrizzontale = cella.orientamentoScaffale === 'ORIZZONTALE';
  return {
    display: 'grid',
    gridTemplateColumns: `repeat(${isOrizzontale ? info.max_righe : info.max_colonne}, 1fr)`,
    gridTemplateRows: `repeat(${isOrizzontale ? info.max_colonne : info.max_righe}, 1fr)`,
    width: '100%', height: '100%', gap: '2px', padding: '2px'
  };
};

const calcolaNumeroSlot = (cella) => {
  const info = getDatiTecniciScaffale(cella.idScaffale);
  return info ? info.max_colonne * info.max_righe : 1;
};

// --- LOGICA MODIFICA ---
const attivaModifica = () => {
  mappaOriginale = JSON.parse(JSON.stringify(mappaDati.value));
  isEditing.value = true;
};

const annullaModifiche = () => {
  mappaDati.value = mappaOriginale;
  scaffaleSelezionato.value = null;
  azzeraHover();
  isEditing.value = false;
};

const selezionaScaffale = (cella) => {
  scaffaleSelezionato.value = scaffaleSelezionato.value?.id === cella.id ? null : cella;
  azzeraHover();
};

const ruotaScaffaleSelezionato = () => {
  if (!scaffaleSelezionato.value) return;
  scaffaleSelezionato.value.orientamentoScaffale =
      scaffaleSelezionato.value.orientamentoScaffale === 'ORIZZONTALE' ? 'VERTICALE' : 'ORIZZONTALE';
  if(hoverX.value !== null) isPosizioneValida.value = controllaValidita(hoverX.value, hoverY.value);
};

const spostaScaffaleQui = (x, y) => {
  if (!scaffaleSelezionato.value || !isPosizioneValida.value) return;
  scaffaleSelezionato.value.coordinataX = x;
  scaffaleSelezionato.value.coordinataY = y;
  scaffaleSelezionato.value = null;
  azzeraHover();
};

const salvaMappa = async () => {
  isSaving.value = true;
  try {
    await api.post('/api/mappa/salva-posizioni', mappaDati.value);
    isEditing.value = false;
    scaffaleSelezionato.value = null;
  } catch (error) {
    alert("❌ Errore durante il salvataggio.");
  } finally {
    isSaving.value = false;
  }
};

onMounted(async () => {
  const ruolo = sessionStorage.getItem('ruolo');
  if (!ruolo) {
    router.push('/'); return;
  }
  try {
    const [resMappa, resReparti, resScaffali] = await Promise.all([
      api.get('/api/mappa/carica'),
      api.get('/api/reparti/carica'),
      api.get('/api/scaffali/carica')
    ]);
    mappaDati.value = resMappa.data;
    repartiDati.value = resReparti.data;
    scaffaliDati.value = resScaffali.data;
  } catch (e) {
    console.error("Errore nel caricamento:", e);
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
          <h1>Dettaglio Reparto</h1>
        </div>

        <button @click="router.push('/MappaOverview')" class="btn-back">
          <svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path></svg>
          Torna alla Planimetria
        </button>
      </header>

      <main class="content-area">
        <div v-if="loading" class="loading-overlay">
          <div class="spinner"></div> Caricamento layout...
        </div>

        <div v-else-if="repartoVisuale" class="reparto-workspace">

          <div class="control-panel">
            <div class="reparto-info">
              <span class="id-badge">ID #{{ repartoVisuale.id }}</span>
              <h2>{{ repartoVisuale.nome }}</h2>
              <span class="temp-badge" :class="{ 'cold': repartoVisuale.temperatura <= 8 }">
                {{ repartoVisuale.temperatura }}°C
              </span>
            </div>

            <div class="tools">
              <div v-if="!isEditing" class="view-controls">
                <button @click="attivaModifica" class="btn btn-primary">
                  <svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
                  Modifica Disposizione
                </button>
              </div>

              <div v-else class="edit-controls">
                <span class="edit-status">
                  <svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                  {{ scaffaleSelezionato ? `Scaffale S${scaffaleSelezionato.idScaffale} selezionato. Clicca per piazzarlo.` : 'Clicca su uno scaffale per muoverlo.' }}
                </span>
                <button @click="salvaMappa" class="btn btn-success" :disabled="isSaving">
                  <svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path></svg>
                  {{ isSaving ? 'Salvataggio...' : 'Conferma' }}
                </button>
                <button @click="annullaModifiche" class="btn btn-danger" :disabled="isSaving">
                  <svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
                  Annulla
                </button>
              </div>
            </div>
          </div>

          <div class="grid-container-wrapper">
            <div class="grid-scroll-area">
              <div class="ingresso-reparto">PORTA REPARTO</div>

              <div
                  class="griglia-dinamica"
                  :class="{ 'edit-mode': isEditing }"
                  :style="{
                  gridTemplateColumns: `repeat(${repartoVisuale.maxX}, 50px)`,
                  gridTemplateRows: `repeat(${repartoVisuale.maxY}, 50px)`
                }"
                  @mouseleave="azzeraHover"
              >
                <template v-if="isEditing && scaffaleSelezionato">
                  <template v-for="y in repartoVisuale.maxY" :key="'row-'+y">
                    <div v-for="x in repartoVisuale.maxX" :key="'col-'+x+'-row-'+y"
                         class="cella-fantasma"
                         :style="{ gridColumn: x, gridRow: y }"
                         @mouseover="impostaHover(x - 1, y - 1)"
                         @click="spostaScaffaleQui(x - 1, y - 1)">
                    </div>
                  </template>
                </template>

                <div v-if="isEditing && scaffaleSelezionato && hoverX !== null"
                     class="scaffale-preview"
                     :class="isPosizioneValida ? 'valido' : 'invalido'"
                     :style="calcolaStilePreview()">
                </div>

                <div
                    v-for="cella in getScaffaliPerReparto(repartoVisuale.id)"
                    :key="'buffer-'+cella.id"
                    class="scaffale-buffer"
                    :class="{ 'is-editing': isEditing }"
                    :style="calcolaStileBuffer(cella, repartoVisuale)"
                ></div>

                <div
                    v-for="cella in getScaffaliPerReparto(repartoVisuale.id)"
                    :key="cella.id"
                    class="scaffale-item"
                    :class="{
                    'is-editing': isEditing,
                    'is-selected': scaffaleSelezionato?.id === cella.id,
                    'piano-inattivo': !haPiano(cella)
                  }"
                    :style="calcolaStileScaffale(cella)"
                    @click="isEditing ? selezionaScaffale(cella) : null"
                >
                  <div class="scaffale-header-fluttuante">
                    <div class="header-main" :class="{'editing-header': isEditing}">
                      <span class="badge-id">S{{ cella.idScaffale }}</span>

                      <button v-if="!isEditing" @click.stop="apriInfoScaffale(cella)" class="btn-info-mini" title="Info Scaffale">
                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                      </button>

                      <div class="piano-controls" v-if="!isEditing">
                        <button @click.stop="cambiaPiano(cella.idScaffale, -1)" :disabled="getPiano(cella.idScaffale) <= 1" class="btn-piano-mini">
                          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 14l-7 7m0 0l-7-7m7 7V3"></path></svg>
                        </button>
                        <span class="piano-text">P{{ getPiano(cella.idScaffale) }}</span>
                        <button @click.stop="cambiaPiano(cella.idScaffale, 1)" :disabled="getPiano(cella.idScaffale) >= (getDatiTecniciScaffale(cella.idScaffale)?.max_altezza || 1)" class="btn-piano-mini">
                          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 10l7-7m0 0l7 7m-7-7v18"></path></svg>
                        </button>
                      </div>
                    </div>

                    <button
                        v-if="isEditing && scaffaleSelezionato?.id === cella.id"
                        @click.stop="ruotaScaffaleSelezionato"
                        class="btn-inline-rotate"
                        title="Ruota Scaffale"
                    >
                      <svg class="icon-sm" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path></svg>
                    </button>
                  </div>

                  <div class="scaffale-inner-grid" :style="calcolaStileInnerGrid(cella)">
                    <div v-for="slotIndex in calcolaNumeroSlot(cella)" :key="slotIndex" class="slot-1x1">
                      <div v-if="haPiano(cella)" class="batch-placeholder"></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="error-state">
          <h2>Reparto non trovato</h2>
        </div>
      </main>
    </div>

    <div v-if="scaffaleInfoAttivo" class="modal-overlay" @click="chiudiInfoScaffale">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Dettagli Scaffale <span class="text-blue">S{{ scaffaleInfoAttivo.idScaffale }}</span></h3>
          <button @click="chiudiInfoScaffale" class="btn-close-modal">✖</button>
        </div>
        <div class="modal-body">
          <div class="info-row">
            <span class="info-label">📍 Posizione Rilevata:</span>
            <span class="info-value">X: {{ scaffaleInfoAttivo.coordinataX }}, Y: {{ scaffaleInfoAttivo.coordinataY }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">🔄 Orientamento:</span>
            <span class="info-value">{{ scaffaleInfoAttivo.orientamentoScaffale }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">📏 Dimensioni (Col x Righe):</span>
            <span class="info-value">{{ getDatiTecniciScaffale(scaffaleInfoAttivo.idScaffale)?.max_colonne }} x {{ getDatiTecniciScaffale(scaffaleInfoAttivo.idScaffale)?.max_righe }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">🏢 Piani Disponibili:</span>
            <span class="info-value">{{ getDatiTecniciScaffale(scaffaleInfoAttivo.idScaffale)?.max_altezza }} piani max</span>
          </div>
          <div class="info-row highlight-row">
            <span class="info-label">⚖️ Peso Massimo (Per Piano):</span>
            <span class="info-value">{{ getDatiTecniciScaffale(scaffaleInfoAttivo.idScaffale)?.peso_max_piano || 'N/A' }} Kg</span>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
/* --- LAYOUT DI BASE --- */
.dashboard-layout { display: flex; height: 100vh; width: 100vw; background-color: #f8fafc; overflow: hidden; }
.main-content { flex: 1; display: flex; flex-direction: column; height: 100vh; min-width: 0; }
.topbar { flex-shrink: 0; display: flex; align-items: center; justify-content: space-between; padding: 15px 30px; background-color: white; border-bottom: 1px solid #e2e8f0; height: 70px; box-sizing: border-box; }
.header-left { display: flex; align-items: center; }
.divider { width: 4px; height: 20px; background-color: #3b82f6; margin-right: 15px; border-radius: 2px; }
.header-left h1 { margin: 0; font-size: 1.3rem; color: #1e293b; font-weight: 700; }
.content-area { flex: 1; padding: 20px 30px; display: flex; flex-direction: column; overflow: hidden; }

/* Icone */
.icon { width: 18px; height: 18px; }
.icon-sm { width: 14px; height: 14px; }

/* Bottoni Moderni */
.btn-back { display: flex; align-items: center; gap: 8px; background: #f1f5f9; color: #475569; border: 1px solid #e2e8f0; padding: 8px 16px; border-radius: 6px; font-weight: 600; cursor: pointer; transition: all 0.2s; font-size: 0.9rem;}
.btn-back:hover { background: #e2e8f0; color: #0f172a; }
.btn { display: flex; align-items: center; gap: 8px; padding: 8px 16px; font-weight: 600; border-radius: 6px; cursor: pointer; border: none; transition: 0.2s; font-size: 0.9rem;}
.btn:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-primary { background-color: #3b82f6; color: white; }
.btn-primary:hover { background-color: #2563eb; }
.btn-success { background-color: #10b981; color: white; }
.btn-success:hover { background-color: #059669; }
.btn-danger { background-color: #ef4444; color: white; }
.btn-danger:hover { background-color: #dc2626; }

/* --- PANNELLO DI CONTROLLO INTEGRATO --- */
.reparto-workspace { display: flex; flex-direction: column; height: 100%; background: white; border-radius: 12px; border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05); overflow: hidden; }
.control-panel { display: flex; justify-content: space-between; align-items: center; padding: 15px 25px; background: #f8fafc; border-bottom: 1px solid #e2e8f0; }
.reparto-info { display: flex; align-items: center; gap: 15px; }
.reparto-info h2 { margin: 0; font-size: 1.4rem; color: #0f172a; text-transform: uppercase; font-weight: 800; }
.id-badge { background: #334155; color: white; padding: 4px 10px; border-radius: 6px; font-size: 0.75rem; font-weight: bold; }
.temp-badge { background: #fee2e2; color: #ef4444; padding: 4px 10px; border-radius: 6px; font-size: 0.85rem; font-weight: bold; border: 1px solid #fca5a5; }
.temp-badge.cold { background: #e0f2fe; color: #0284c7; border-color: #bae6fd; }

.tools { display: flex; align-items: center; }
.view-controls, .edit-controls { display: flex; align-items: center; gap: 15px; }
.edit-status { display: flex; align-items: center; gap: 8px; font-size: 0.9rem; color: #1e40af; font-weight: 600; background: #eff6ff; padding: 8px 16px; border-radius: 6px; border: 1px solid #bfdbfe;}

/* --- MODALE INFORMAZIONI SCAFFALE --- */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(15, 23, 42, 0.6); backdrop-filter: blur(4px); z-index: 9999; display: flex; align-items: center; justify-content: center; }
.modal-content { background: white; border-radius: 12px; width: 420px; max-width: 90%; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1); overflow: hidden; border: 1px solid #cbd5e1;}
.modal-header { background: #f8fafc; padding: 15px 20px; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; color: #0f172a; font-size: 1.2rem; font-weight: 800;}
.text-blue { color: #3b82f6; }
.btn-close-modal { background: transparent; border: none; font-size: 1.2rem; color: #64748b; cursor: pointer; transition: 0.2s;}
.btn-close-modal:hover { color: #ef4444; transform: scale(1.1);}
.modal-body { padding: 20px; display: flex; flex-direction: column; gap: 12px; }
.info-row { display: flex; justify-content: space-between; align-items: center; padding-bottom: 8px; border-bottom: 1px dashed #e2e8f0; }
.info-row:last-child { border-bottom: none; padding-bottom: 0; }
.highlight-row { background: #f1f5f9; padding: 10px; border-radius: 6px; border: none;}
.info-label { font-weight: 600; color: #475569; font-size: 0.9rem; }
.info-value { font-weight: 800; color: #0f172a; font-size: 0.95rem; }

/* --- AREA GRIGLIA E SCROLLING --- */
.grid-container-wrapper { flex: 1; background-color: #e2e8f0; position: relative; overflow: hidden; }
.grid-scroll-area { width: 100%; height: 100%; overflow: auto; padding: 40px; box-sizing: border-box; display: flex; flex-direction: column; align-items: center; }

/* Ingresso */
.ingresso-reparto { background: #cbd5e1; padding: 5px 60px; font-weight: 800; color: #334155; font-size: 0.8rem; letter-spacing: 4px; border-top-left-radius: 8px; border-top-right-radius: 8px; margin-bottom: 0; border: 3px solid #94a3b8; border-bottom: none; }

.griglia-dinamica {
  display: grid; gap: 0;
  background-image: linear-gradient(#f1f5f9 1px, transparent 1px), linear-gradient(90deg, #f1f5f9 1px, transparent 1px);
  background-size: 50px 50px; background-color: #ffffff;
  border: 4px solid #94a3b8; box-shadow: 0 10px 25px rgba(0,0,0,0.1);
  width: max-content; position: relative;
}
.griglia-dinamica.edit-mode { border-color: #3b82f6; }

/* --- ANTEPRIMA SPOSTAMENTO (GHOST PREVIEW) --- */
.scaffale-preview {
  position: relative; z-index: 15;
  pointer-events: none;
  transition: transform 0.1s;
}
.scaffale-preview.valido { background: rgba(16, 185, 129, 0.2); border: 3px dashed #10b981; }
.scaffale-preview.invalido { background: rgba(239, 68, 68, 0.2); border: 3px dashed #ef4444; }

/* Celle Hitbox Fantasma */
.cella-fantasma { width: 50px; height: 50px; z-index: 20; border: 1px dashed transparent; cursor: crosshair; }

/* --- SCAFFALI --- */
.scaffale-buffer {
  background: rgba(203, 213, 225, 0.15); border: 1px dashed rgba(148, 163, 184, 0.4);
  border-radius: 4px; z-index: 1; pointer-events: none; transition: all 0.2s;
}
.scaffale-buffer.is-editing { background: rgba(239, 68, 68, 0.05); border-color: rgba(239, 68, 68, 0.2); }

.scaffale-item {
  display: block; box-sizing: border-box; position: relative; z-index: 5;
  border: 2px solid #334155; border-radius: 4px; background-color: #f8fafc;
  transition: all 0.2s ease; box-shadow: 2px 2px 5px rgba(0,0,0,0.1);
}

.scaffale-inner-grid { box-sizing: border-box; width: 100%; height: 100%; }
.slot-1x1 { background-color: #e2e8f0; border: 1px solid #cbd5e1; display: flex; align-items: center; justify-content: center; position: relative;}
.batch-placeholder { width: 75%; height: 75%; background-color: #6366f1; border-radius: 2px; }

/* HEADER FLUTTUANTE E CONTROLLO PIANI */
.scaffale-header-fluttuante { position: absolute; top: -14px; left: -10px; display: flex; align-items: center; gap: 6px; z-index: 30; }

.header-main {
  display: flex; align-items: center;
  background: #ffffff; border: 1px solid #cbd5e1;
  border-radius: 6px; box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  overflow: hidden; pointer-events: auto;
}
.header-main.editing-header { background: transparent; border: none; box-shadow: none; overflow: visible;}

.badge-id {
  background: #3b82f6; color: white;
  font-size: 0.65rem; font-weight: bold;
  padding: 4px 8px;
}
.header-main.editing-header .badge-id { background: #0f172a; border-radius: 6px; box-shadow: 0 2px 4px rgba(0,0,0,0.2); }

/* Tasto info */
.btn-info-mini { background: transparent; color: #3b82f6; border: none; padding: 4px; margin-left: 2px; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: 0.2s;}
.btn-info-mini svg { width: 14px; height: 14px; }
.btn-info-mini:hover { color: #2563eb; background: #eff6ff; border-radius: 4px;}

.piano-controls {
  display: flex; align-items: center;
  background: #ffffff; padding: 0 4px; border-left: 1px solid #e2e8f0;
}
.btn-piano-mini { background: transparent; color: #64748b; border: none; padding: 4px; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: 0.2s;}
.btn-piano-mini svg { width: 12px; height: 12px; }
.btn-piano-mini:hover:not(:disabled) { color: #0f172a; background: #f1f5f9; border-radius: 4px;}
.btn-piano-mini:disabled { opacity: 0.3; cursor: not-allowed; }
.piano-text { color: #0f172a; font-size: 0.7rem; font-weight: 800; margin: 0 6px; }

.btn-inline-rotate { pointer-events: auto; background-color: #f59e0b; color: white; border: none; border-radius: 6px; width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; cursor: pointer; box-shadow: 0 2px 4px rgba(0,0,0,0.2); transition: 0.2s;}
.btn-inline-rotate:hover { background-color: #d97706; }

/* MODALITÀ MODIFICA */
.scaffale-item.is-editing { cursor: pointer; border-style: dashed; z-index: 25;}
.scaffale-item.is-editing:hover { border-color: #3b82f6; background-color: #eff6ff;}
.scaffale-item.is-selected { border: 3px solid #10b981; box-shadow: 0 0 15px rgba(16, 185, 129, 0.4); z-index: 30; opacity: 0.8;}
.error-state { text-align: center; margin-top: 50px; }
</style>