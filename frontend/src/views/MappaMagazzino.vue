<template>
  <div class="mappa-container">
    <div class="header-mappa">
      <h1>Mappa Magazzino Interattiva</h1>
      <p>Visualizzazione e Gestione Layout</p>
    </div>

    <div class="floating-bottom-bar">
      
      <div v-if="!isEditing" class="view-controls">
        <button @click="attivaModifica" class="btn btn-edit">
          ✏️ Modifica Disposizione
        </button>
        
        <div class="divider"></div>
        
        <div class="piano-selector">
          <button @click="cambiaPiano(-1)" :disabled="pianoAttuale <= 1" class="btn-piano">⬇️</button>
          <span class="piano-label">Piano Visualizzato: <strong>{{ pianoAttuale }}</strong></span>
          <button @click="cambiaPiano(1)" class="btn-piano">⬆️</button>
        </div>
      </div>

      <div v-else class="edit-controls">
        <span class="edit-status">
          {{ scaffaleSelezionato ? `📍 S${scaffaleSelezionato.idScaffale} selezionato: clicca una cella vuota` : '👆 Clicca su uno scaffale' }}
        </span>
        <button @click="salvaMappa" class="btn btn-save" :disabled="isSaving">
          {{ isSaving ? '⏳ Salv...' : '💾 Conferma' }}
        </button>
        <button @click="annullaModifiche" class="btn btn-cancel" :disabled="isSaving">
          ❌ Annulla
        </button>
      </div>
    </div>

    <div v-if="loading" class="loading-overlay">
      <div class="spinner"></div> Caricamento in corso...
    </div>

    <div v-else class="container-reparti">
      <div v-for="reparto in repartiDati" :key="reparto.id" class="reparto-card">
        <div class="reparto-info">
          <h3>{{ reparto.nome }} <small>(ID #{{ reparto.id }})</small></h3>
          <span class="temp-badge">{{ reparto.temperatura }}°C</span>
        </div>
        
        <div 
          class="griglia-dinamica"
          :class="{ 'edit-mode': isEditing }"
          :style="{ 
            gridTemplateColumns: `repeat(${reparto.maxX}, 50px)`, 
            gridTemplateRows: `repeat(${reparto.maxY}, 50px)` 
          }"
        >
          
          <template v-if="isEditing">
            <template v-for="y in reparto.maxY" :key="'row-'+y">
              <div v-for="x in reparto.maxX" :key="'col-'+x+'-row-'+y"
                   class="cella-fantasma"
                   :class="{ 'target-attivo': scaffaleSelezionato }"
                   :style="{ gridColumn: x, gridRow: y }"
                   @click="spostaScaffaleQui(reparto.id, x - 1, y - 1)">
              </div>
            </template>
          </template>

          <div 
            v-for="cella in getScaffaliPerReparto(reparto.id)" 
            :key="cella.id"
            class="scaffale-item"
            :class="{ 
              'is-editing': isEditing, 
              'is-selected': scaffaleSelezionato?.id === cella.id,
              'piano-inattivo': !haPiano(cella) /* NUOVA LOGICA GREY-OUT */
            }"
            :style="calcolaStileScaffale(cella)"
            @click="isEditing && haPiano(cella) ? selezionaScaffale(cella) : null"
          >
            <div class="scaffale-header-fluttuante">
              <span class="badge-id">S{{ cella.idScaffale }}</span>
              <button 
                v-if="isEditing && scaffaleSelezionato?.id === cella.id" 
                @click.stop="ruotaScaffaleSelezionato" 
                class="btn-inline-rotate"
                title="Ruota Scaffale"
              >
                🔄
              </button>
            </div>
            
            <div class="scaffale-inner-grid" :style="calcolaStileInnerGrid(cella)">
              <div v-for="slotIndex in calcolaNumeroSlot(cella)" :key="slotIndex" class="slot-1x1">
                <div v-if="haPiano(cella)" class="batch-placeholder"></div>
              </div>
            </div>

            <div class="tooltip" v-if="!isEditing">
              <strong>Scaffale S{{ cella.idScaffale }}</strong><br>
              Posizione: ({{ cella.coordinataX }}, {{ cella.coordinataY }})<br>
              Orientamento: {{ cella.orientamentoScaffale }}<br>
              Altezza Massima: {{ getDatiTecniciScaffale(cella.idScaffale)?.max_altezza }} piani
            </div>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '../api'; 

const mappaDati = ref([]);
const repartiDati = ref([]);
const scaffaliDati = ref([]);
const loading = ref(true);
const router = useRouter();

const isEditing = ref(false);
const isSaving = ref(false);
const scaffaleSelezionato = ref(null);
let mappaOriginale = []; 

// NUOVA VARIABILE STATO: Piano attualmente visualizzato
const pianoAttuale = ref(1);

const getScaffaliPerReparto = (idReparto) => mappaDati.value.filter(cella => cella.idReparto === idReparto);
const getDatiTecniciScaffale = (idScaffale) => scaffaliDati.value.find(s => s.id === idScaffale) || null;

// FUNZIONI DI VISUALIZZAZIONE
const cambiaPiano = (delta) => {
  if (pianoAttuale.value + delta >= 1) {
    pianoAttuale.value += delta;
  }
};

// Verifica se lo scaffale ha un'altezza sufficiente per essere visibile in questo piano
const haPiano = (cella) => {
  const info = getDatiTecniciScaffale(cella.idScaffale);
  if (!info) return true; // Se non abbiamo dati, lo mostriamo per sicurezza
  return info.max_altezza >= pianoAttuale.value;
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

// FUNZIONI DI MODIFICA
const attivaModifica = () => {
  // Disattiviamo la visualizzazione dei piani durante la modifica per evitare errori di spostamento
  pianoAttuale.value = 1; 
  mappaOriginale = JSON.parse(JSON.stringify(mappaDati.value));
  isEditing.value = true;
};

const annullaModifiche = () => {
  mappaDati.value = mappaOriginale;
  scaffaleSelezionato.value = null;
  isEditing.value = false;
};

const selezionaScaffale = (cella) => {
  scaffaleSelezionato.value = scaffaleSelezionato.value?.id === cella.id ? null : cella;
};

const ruotaScaffaleSelezionato = () => {
  if (!scaffaleSelezionato.value) return;
  scaffaleSelezionato.value.orientamentoScaffale = 
    scaffaleSelezionato.value.orientamentoScaffale === 'ORIZZONTALE' ? 'VERTICALE' : 'ORIZZONTALE';
};

const spostaScaffaleQui = (idRepartoNuovo, xNuovo, yNuovo) => {
  if (!scaffaleSelezionato.value) return;

  const info = getDatiTecniciScaffale(scaffaleSelezionato.value.idScaffale);
  const repartoTarget = repartiDati.value.find(r => r.id === idRepartoNuovo);
  
  const isOrizzontale = scaffaleSelezionato.value.orientamentoScaffale === 'ORIZZONTALE';
  const spanX = isOrizzontale ? info.max_righe : info.max_colonne;
  const spanY = isOrizzontale ? info.max_colonne : info.max_righe;

  if (xNuovo + spanX > repartoTarget.maxX || yNuovo + spanY > repartoTarget.maxY) {
    alert("⚠️ Spostamento annullato: lo scaffale esce dai bordi del reparto.");
    return;
  }

  const altriScaffali = mappaDati.value.filter(
    c => c.idReparto === idRepartoNuovo && c.id !== scaffaleSelezionato.value.id
  );

  const isSovrapposto = altriScaffali.some(altro => {
    const altroInfo = getDatiTecniciScaffale(altro.idScaffale);
    if(!altroInfo) return false;

    const altroIsOrizzontale = altro.orientamentoScaffale === 'ORIZZONTALE';
    const altroSpanX = altroIsOrizzontale ? altroInfo.max_righe : altroInfo.max_colonne;
    const altroSpanY = altroIsOrizzontale ? altroInfo.max_colonne : altroInfo.max_righe;

    const nonSiToccano = (
      xNuovo >= altro.coordinataX + altroSpanX || 
      xNuovo + spanX <= altro.coordinataX ||      
      yNuovo >= altro.coordinataY + altroSpanY || 
      yNuovo + spanY <= altro.coordinataY         
    );

    return !nonSiToccano; 
  });

  if (isSovrapposto) {
    alert("⚠️ Spostamento annullato: l'area è già occupata da un altro scaffale.");
    return;
  }

  scaffaleSelezionato.value.idReparto = idRepartoNuovo;
  scaffaleSelezionato.value.coordinataX = xNuovo;
  scaffaleSelezionato.value.coordinataY = yNuovo;
  scaffaleSelezionato.value = null; 
};

const salvaMappa = async () => {
  isSaving.value = true;
  try {
    await api.post('/api/mappa/salva-posizioni', mappaDati.value);
    alert("✅ Modifiche salvate con successo nel database!");
    isEditing.value = false;
  } catch (error) {
    if (error.response && error.response.status === 400) {
      alert("❌ Salvataggio fallito: Posizioni non valide.");
    } else {
      alert("❌ Errore di connessione col server.");
    }
  } finally {
    isSaving.value = false;
  }
};

onMounted(async () => {
  const ruolo = sessionStorage.getItem('ruolo');
  if (!ruolo) {
    router.push('/');
    return;
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

<style scoped>
.mappa-container { padding: 20px; padding-bottom: 100px; background-color: #f4f7f6; min-height: 100vh; }
.header-mappa { margin-bottom: 30px; text-align: center; display: flex; flex-direction: column; align-items: center;}
.container-reparti { display: flex; flex-wrap: wrap; gap: 40px; justify-content: center; overflow-x: auto; padding-bottom: 20px;}
.reparto-card { background: white; padding: 20px; border-radius: 12px; box-shadow: 0 8px 16px rgba(0,0,0,0.1); flex-shrink: 0; min-width: max-content;}
.reparto-info { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;}
.temp-badge { background: #e1f5fe; color: #0288d1; padding: 4px 8px; border-radius: 20px; font-size: 0.8rem; font-weight: bold;}
.griglia-dinamica { display: grid; gap: 0; background-image: linear-gradient(#e0e0e0 1px, transparent 1px), linear-gradient(90deg, #e0e0e0 1px, transparent 1px); background-size: 50px 50px; background-origin: content-box; background-color: #ffffff; border: 2px dashed #cfd8dc; padding: 10px; border-radius: 8px; width: max-content; margin: 0 auto; position: relative;}

/* STILI SCAFFALE NORMALE */
.scaffale-item { display: block; box-sizing: border-box; position: relative; z-index: 2; border: 1px solid rgba(44, 62, 80, 0.3); border-radius: 4px; background-color: rgba(44, 62, 80, 0.05); transition: all 0.3s ease;}
.scaffale-item:not(.is-editing):not(.piano-inattivo):hover { z-index: 10; box-shadow: 0 4px 12px rgba(44, 62, 80, 0.2); border-color: rgba(44, 62, 80, 0.8);}

/* STILE SCAFFALE GREYED OUT (Piano non disponibile) */
.piano-inattivo {
  opacity: 0.3;
  filter: grayscale(100%);
  pointer-events: none; /* Impedisce interazioni e tooltip */
}

.scaffale-inner-grid { box-sizing: border-box; width: 100%; height: 100%; }
.slot-1x1 { background-color: #2c3e50; border-radius: 2px; display: flex; align-items: center; justify-content: center; position: relative;}
.batch-placeholder { width: 70%; height: 70%; background-color: #a8e6cf; border: 1px solid #3b8d6e; border-radius: 2px; cursor: pointer; transition: transform 0.1s;}
.batch-placeholder:hover { transform: scale(1.1); background-color: #dcedc1;}

.scaffale-header-fluttuante { position: absolute; top: -12px; left: -12px; display: flex; align-items: center; gap: 5px; z-index: 30; pointer-events: none; }
.badge-id { pointer-events: auto; background-color: #e74c3c; color: white; font-size: 0.65rem; font-weight: bold; padding: 3px 6px; border-radius: 12px; box-shadow: 0 2px 4px rgba(0,0,0,0.2);}
.btn-inline-rotate { pointer-events: auto; background-color: #f39c12; color: white; border: none; border-radius: 50%; width: 26px; height: 26px; display: flex; align-items: center; justify-content: center; cursor: pointer; font-size: 0.8rem; box-shadow: 0 2px 6px rgba(0,0,0,0.3); transition: transform 0.2s ease, background-color 0.2s ease;}
.btn-inline-rotate:hover { background-color: #d68910; transform: scale(1.15) rotate(90deg); }

.tooltip { visibility: hidden; position: absolute; bottom: 110%; left: 50%; transform: translateX(-50%); background: #2c3e50; color: #fff; padding: 8px; border-radius: 6px; font-size: 0.75rem; z-index: 100; white-space: nowrap; box-shadow: 0 4px 6px rgba(0,0,0,0.2);}
.scaffale-item:not(.is-editing):not(.piano-inattivo):hover .tooltip { visibility: visible; }

/* --- BARRA FLUTTUANTE UNIFICATA --- */
.floating-bottom-bar {
  position: fixed;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
  padding: 12px 25px;
  border-radius: 50px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  border: 1px solid #e0e0e0;
}

.view-controls, .edit-controls { display: flex; align-items: center; gap: 15px; }

.divider { width: 1px; height: 30px; background-color: #ddd; margin: 0 10px; }

/* Stili selettore piano */
.piano-selector { display: flex; align-items: center; gap: 10px; }
.btn-piano { background: #ecf0f1; border: none; border-radius: 50%; width: 35px; height: 35px; cursor: pointer; font-size: 1.2rem; display: flex; align-items: center; justify-content: center; transition: 0.2s;}
.btn-piano:hover:not(:disabled) { background: #bdc3c7; }
.btn-piano:disabled { opacity: 0.3; cursor: not-allowed; }
.piano-label { color: #2c3e50; font-size: 0.9rem; }

.edit-status { font-size: 0.9rem; color: #2c3e50; font-weight: bold; background: #f1f2f6; padding: 8px 15px; border-radius: 20px;}
.btn { padding: 10px 20px; font-weight: bold; border-radius: 6px; cursor: pointer; border: none; transition: 0.2s; margin: 0; }
.btn:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-edit { background-color: #3498db; color: white; border-radius: 20px;}
.btn-edit:hover { background-color: #2980b9; }
.btn-save { background-color: #2ecc71; color: white; border-radius: 20px; }
.btn-save:hover { background-color: #27ae60; }
.btn-cancel { background-color: #e74c3c; color: white; border-radius: 20px; }
.btn-cancel:hover { background-color: #c0392b; }

.scaffale-item.is-editing { cursor: pointer; }
.scaffale-item.is-editing:not(.piano-inattivo):hover { border-color: #f1c40f; box-shadow: 0 0 10px rgba(241, 196, 15, 0.5); }
.scaffale-item.is-selected { border: 3px solid #f1c40f; box-shadow: 0 0 20px rgba(241, 196, 15, 0.8); pointer-events: none; opacity: 0.8; z-index: 20; }
.cella-fantasma { width: 50px; height: 50px; z-index: 1; border: 1px dashed transparent; transition: all 0.2s; }
.cella-fantasma.target-attivo { cursor: crosshair; }
.cella-fantasma.target-attivo:hover { background-color: rgba(46, 204, 113, 0.3); border: 2px dashed #2ecc71; border-radius: 4px; }
</style>