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

// --- STATI PER I LOTTI E ASSEGNAMENTI ---
const tuttiIProdotti = ref([]); 
const tuttiIBatch = ref([]); 
const lottiSospesi = ref([]);
const batchScaffaliDati = ref([]);

const nuoviAssegnamenti = ref([]); 
const modifichePendenti = ref([]); 

const lottoDaAssegnare = ref(null);
const quantitaSelezionata = ref(1);

const repartoVisuale = computed(() => {
  const idRepartoSelezionato = parseInt(route.params.id);
  const r = repartiDati.value.find(rep => rep.id === idRepartoSelezionato);
  if (!r) return null;
  const repCopy = JSON.parse(JSON.stringify(r));
  if (repCopy.id === 2 && repCopy.maxX > repCopy.maxY) {
    const temp = repCopy.maxX;
    repCopy.maxX = repCopy.maxY;
    repCopy.maxY = temp;
  }
  return repCopy;
});

const isEditing = ref(false);
const isSaving = ref(false);
const isSavingAssignments = ref(false);
const scaffaleSelezionato = ref(null);
let mappaOriginale = [];

// --- GESTIONE PIANI (BASE 0 IN MEMORIA, BASE 1 IN UI) ---
const pianiMappa = ref({}); 

const getPiano = (idMappa) => {
  return pianiMappa.value[idMappa] !== undefined ? pianiMappa.value[idMappa] : 0;
};

const cambiaPiano = (idMappa, idScaffaleTemplate, delta) => {
  const info = getDatiTecniciScaffale(idScaffaleTemplate);
  const max = info ? info.max_altezza : 1;
  const current = getPiano(idMappa);
  if (current + delta >= 0 && current + delta < max) {
    pianiMappa.value[idMappa] = current + delta;
  }
};

const haPiano = (cella) => {
  const info = getDatiTecniciScaffale(cella.idScaffale);
  if (!info) return true;
  return getPiano(cella.id) < info.max_altezza; 
};

// --- FUNZIONI DI SUPPORTO DATI ---
const getDatiTecniciScaffale = (idScaffale) => scaffaliDati.value.find(s => Number(s.id) === Number(idScaffale)) || null;
const getScaffaliPerReparto = (idReparto) => mappaDati.value.filter(cella => Number(cella.idReparto) === Number(idReparto));

const getProdottoDaBatch = (idBatch) => {
  const batch = tuttiIBatch.value.find(b => Number(b.id) === Number(idBatch));
  if (!batch) return null;
  return tuttiIProdotti.value.find(p => Number(p.id) === Number(batch.idProdotto)) || null;
};

const getNomeProdotto = (idProdotto) => {
  if (!tuttiIProdotti.value || tuttiIProdotti.value.length === 0) return 'Caricamento...';
  const prod = tuttiIProdotti.value.find(p => Number(p.id) === Number(idProdotto));
  return prod ? prod.nome : `Sconosciuto (ID: ${idProdotto})`;
};

const MAX_SPAZIO_CELLA = 100;

const calcolaSpazioOccupatoCella = (idMappa, altezza, riga, colonna) => {
  let spazio = 0;
  const merce = [...batchScaffaliDati.value, ...nuoviAssegnamenti.value].filter(
    bs => Number(bs.idMappa) === Number(idMappa) && 
          Number(bs.altezza) === Number(altezza) && 
          Number(bs.riga) === Number(riga) && 
          Number(bs.colonna) === Number(colonna)
  );
  merce.forEach(item => {
    const prodotto = getProdottoDaBatch(item.idBatchProdotti);
    spazio += (item.qta * (prodotto ? prodotto.spazioUnitario || 1 : 1));
  });
  return Math.round(spazio * 10) / 10;
};

const calcolaPesoOccupatoScaffale = (idMappa) => {
  let peso = 0;
  const merce = [...batchScaffaliDati.value, ...nuoviAssegnamenti.value].filter(bs => Number(bs.idMappa) === Number(idMappa));
  merce.forEach(item => {
    const prodotto = getProdottoDaBatch(item.idBatchProdotti);
    peso += (item.qta * (prodotto ? prodotto.pesoUnitario || 0 : 0));
  });
  return Math.round(peso * 10) / 10;
};

// --- LOGICA DEL MODALE E RIMOZIONE ---
const cellaDettaglioAttiva = ref(null);

const chiudiDettaglioCella = () => { 
  cellaDettaglioAttiva.value = null; 
};

const apriModaleDettaglio = (cella, rigaLocale, colonnaLocale, pianoAttuale) => {
  const merceCellaVecchia = batchScaffaliDati.value.filter(bs => 
    Number(bs.idMappa) === Number(cella.id) && Number(bs.altezza) === Number(pianoAttuale) && 
    Number(bs.riga) === Number(rigaLocale) && Number(bs.colonna) === Number(colonnaLocale)
  );
  const merceCellaNuova = nuoviAssegnamenti.value.filter(bs => 
    Number(bs.idMappa) === Number(cella.id) && Number(bs.altezza) === Number(pianoAttuale) && 
    Number(bs.riga) === Number(rigaLocale) && Number(bs.colonna) === Number(colonnaLocale)
  );
  
  const merceMista = [...merceCellaVecchia, ...merceCellaNuova];

  cellaDettaglioAttiva.value = {
    idMappa: cella.id, scaffaleId: cella.idScaffale, riga: rigaLocale, colonna: colonnaLocale, piano: pianoAttuale,
    spazioOccupato: calcolaSpazioOccupatoCella(cella.id, pianoAttuale, rigaLocale, colonnaLocale),
    elementi: merceMista.map(item => ({
      ...item, 
      refOriginale: item, 
      prodotto: getProdottoDaBatch(item.idBatchProdotti),
      isNuovo: merceCellaNuova.includes(item),
      qtaRimuovere: 1 
    }))
  };
};

const rimuoviDaCella = (elementoModale) => {
  const qtaTogliere = elementoModale.qtaRimuovere;
  if (qtaTogliere <= 0 || qtaTogliere > elementoModale.qta) {
    alert("Quantità da rimuovere non valida."); return;
  }

  let lottoInSidebar = lottiSospesi.value.find(l => Number(l.id) === Number(elementoModale.idBatchProdotti));
  if (!lottoInSidebar) {
    lottoInSidebar = tuttiIBatch.value.find(b => Number(b.id) === Number(elementoModale.idBatchProdotti));
    if (lottoInSidebar) lottiSospesi.value.unshift(lottoInSidebar); // RIMETTE IN CIMA
  }

  if (lottoInSidebar) lottoInSidebar.quantita += qtaTogliere;

  elementoModale.refOriginale.qta -= qtaTogliere;
  elementoModale.qta -= qtaTogliere; 

  if (elementoModale.isNuovo) {
    if (elementoModale.refOriginale.qta <= 0) nuoviAssegnamenti.value = nuoviAssegnamenti.value.filter(na => na !== elementoModale.refOriginale);
  } else {
    // Gestione Tracciamento Modifica (Copia anche l'id per il backend)
    let tracciaModifica = modifichePendenti.value.find(m => Number(m.id) === Number(elementoModale.refOriginale.id));
    if (!tracciaModifica) {
      tracciaModifica = { ...elementoModale.refOriginale };
      modifichePendenti.value.push(tracciaModifica);
    }
    tracciaModifica.qta = elementoModale.refOriginale.qta;
    
    // Aggiornamento puramente visivo della griglia
    if (elementoModale.refOriginale.qta <= 0) {
      batchScaffaliDati.value = batchScaffaliDati.value.filter(bs => Number(bs.id) !== Number(elementoModale.refOriginale.id));
    }
  }

  // Pulizia visiva modale
  if (elementoModale.qta <= 0) cellaDettaglioAttiva.value.elementi = cellaDettaglioAttiva.value.elementi.filter(e => e !== elementoModale);
  cellaDettaglioAttiva.value.spazioOccupato = calcolaSpazioOccupatoCella(cellaDettaglioAttiva.value.idMappa, cellaDettaglioAttiva.value.piano, cellaDettaglioAttiva.value.riga, cellaDettaglioAttiva.value.colonna);
  if (cellaDettaglioAttiva.value.elementi.length === 0) chiudiDettaglioCella();
};

// --- GESTISCI CLICK SULLO SLOT ---
const gestisciClickCella = (cella, slotIndex) => {
  if (isEditing.value) return;

  const pianoAttuale = getPiano(cella.id);
  const info = getDatiTecniciScaffale(cella.idScaffale);
  const isOrizzontale = cella.orientamentoScaffale === 'ORIZZONTALE';
  const rigaLocale = Math.floor((slotIndex - 1) / (isOrizzontale ? info.max_righe : info.max_colonne));
  const colonnaLocale = ((slotIndex - 1) % (isOrizzontale ? info.max_righe : info.max_colonne));

  if (lottoDaAssegnare.value) {
    if (quantitaSelezionata.value <= 0 || quantitaSelezionata.value > lottoDaAssegnare.value.quantita) {
      alert("Quantità non valida!"); return;
    }

    const prodotto = getProdottoDaBatch(lottoDaAssegnare.value.id);
    const spazioRichiesto = quantitaSelezionata.value * (prodotto ? prodotto.spazioUnitario || 1 : 1);
    const pesoRichiesto = quantitaSelezionata.value * (prodotto ? prodotto.pesoUnitario || 0 : 0);

    const spazioAttuale = calcolaSpazioOccupatoCella(cella.id, pianoAttuale, rigaLocale, colonnaLocale);
    if (spazioAttuale + spazioRichiesto > MAX_SPAZIO_CELLA) {
      alert(`Spazio insufficiente! Libero: ${MAX_SPAZIO_CELLA - spazioAttuale}. Richiesto: ${spazioRichiesto}`); return;
    }

    const pesoAttuale = calcolaPesoOccupatoScaffale(cella.id);
    if (pesoAttuale + pesoRichiesto > (info.max_peso || 9999)) {
      alert(`Scaffale sovraccarico! Libero: ${(info.max_peso || 9999) - pesoAttuale} kg.`); return;
    }

    lottoDaAssegnare.value.quantita -= quantitaSelezionata.value;

    const payload = {
      // Niente ID qui: il backend capirà che è un inserimento
      idMappa: cella.id, 
      idBatchProdotti: lottoDaAssegnare.value.id,
      colonna: colonnaLocale, 
      riga: rigaLocale, 
      altezza: pianoAttuale, 
      qta: quantitaSelezionata.value
    };

    const indexEsistenteNuovo = nuoviAssegnamenti.value.findIndex(a => 
      Number(a.idMappa) === Number(cella.id) && Number(a.altezza) === Number(pianoAttuale) && 
      Number(a.riga) === Number(rigaLocale) && Number(a.colonna) === Number(colonnaLocale) && 
      Number(a.idBatchProdotti) === Number(payload.idBatchProdotti)
    );
    
    if (indexEsistenteNuovo >= 0) nuoviAssegnamenti.value[indexEsistenteNuovo].qta += payload.qta;
    else nuoviAssegnamenti.value.push(payload);

    if (lottoDaAssegnare.value.quantita <= 0) {
      lottiSospesi.value = lottiSospesi.value.filter(l => Number(l.id) !== Number(lottoDaAssegnare.value.id));
      lottoDaAssegnare.value = null;
    } else quantitaSelezionata.value = lottoDaAssegnare.value.quantita;

  } else {
    apriModaleDettaglio(cella, rigaLocale, colonnaLocale, pianoAttuale);
  }
};

// --- SALVATAGGIO (UNIFICATO) ---
const salvaAssegnamenti = async () => {
  if (nuoviAssegnamenti.value.length === 0 && modifichePendenti.value.length === 0) return;
  isSavingAssignments.value = true;
  try {
    // Uniamo le due liste in un solo array come avevamo detto
    const payloadSingolo = [...nuoviAssegnamenti.value, ...modifichePendenti.value];
    
    // RIMESSO L'URL ORIGINALE CHE FUNZIONA NEL TUO BACKEND: /salva
    await api.post('/api/batch-scaffale/salva', payloadSingolo);
    
    alert("Dati salvati e sincronizzati con successo!");
    window.location.reload(); 
  } catch (error) { 
    console.error("Errore salvataggio API:", error);
    const errorMsg = error.response?.data?.message || error.response?.data || "Errore durante il salvataggio dei dati.";
    alert(errorMsg);
  } finally { 
    isSavingAssignments.value = false; 
  }
};

const annullaAssegnamenti = () => { window.location.reload(); };

const selezionaLotto = (lotto) => {
  if (lottoDaAssegnare.value && Number(lottoDaAssegnare.value.id) === Number(lotto.id)) {
    lottoDaAssegnare.value = null; 
  } else {
    lottoDaAssegnare.value = lotto;
    quantitaSelezionata.value = 1; 
  }
};

const getSlotStatus = (cella, slotIndex) => {
  const pianoAttuale = getPiano(cella.id);
  const info = getDatiTecniciScaffale(cella.idScaffale);
  const isOrizzontale = cella.orientamentoScaffale === 'ORIZZONTALE';
  const rigaLocale = Math.floor((slotIndex - 1) / (isOrizzontale ? info.max_righe : info.max_colonne));
  const colonnaLocale = ((slotIndex - 1) % (isOrizzontale ? info.max_righe : info.max_colonne));

  const hasNuovi = nuoviAssegnamenti.value.some(bs => 
    Number(bs.idMappa) === Number(cella.id) && Number(bs.altezza) === Number(pianoAttuale) && 
    Number(bs.riga) === Number(rigaLocale) && Number(bs.colonna) === Number(colonnaLocale) && Number(bs.qta) > 0
  );
  const hasSalvati = batchScaffaliDati.value.some(bs => 
    Number(bs.idMappa) === Number(cella.id) && Number(bs.altezza) === Number(pianoAttuale) && 
    Number(bs.riga) === Number(rigaLocale) && Number(bs.colonna) === Number(colonnaLocale) && Number(bs.qta) > 0
  );

  if (hasNuovi) return 'nuovo'; 
  if (hasSalvati) return 'salvato';
  return 'vuoto';
};

const scaffaleInfoAttivo = ref(null);
const apriInfoScaffale = (cella) => { scaffaleInfoAttivo.value = cella; };
const chiudiInfoScaffale = () => { scaffaleInfoAttivo.value = null; };

const hoverX = ref(null);
const hoverY = ref(null);
const isPosizioneValida = ref(false);
const impostaHover = (x, y) => { if (!scaffaleSelezionato.value) return; hoverX.value = x; hoverY.value = y; isPosizioneValida.value = controllaValidita(x, y); };
const azzeraHover = () => { hoverX.value = null; hoverY.value = null; };

const controllaValidita = (xNuovo, yNuovo) => {
  if (!scaffaleSelezionato.value || !repartoVisuale.value) return false;
  const info = getDatiTecniciScaffale(scaffaleSelezionato.value.idScaffale);
  const isOrizzontale = scaffaleSelezionato.value.orientamentoScaffale === 'ORIZZONTALE';
  const spanX = isOrizzontale ? info.max_righe : info.max_colonne;
  const spanY = isOrizzontale ? info.max_colonne : info.max_righe;
  if (xNuovo + spanX > repartoVisuale.value.maxX || yNuovo + spanY > repartoVisuale.value.maxY) return false;
  const altriScaffali = mappaDati.value.filter(c => Number(c.idReparto) === Number(repartoVisuale.value.id) && Number(c.id) !== Number(scaffaleSelezionato.value.id));
  const isSovrapposto = altriScaffali.some(altro => {
    const altroInfo = getDatiTecniciScaffale(altro.idScaffale);
    if (!altroInfo) return false;
    const altroSpanX = altro.orientamentoScaffale === 'ORIZZONTALE' ? altroInfo.max_righe : altroInfo.max_colonne;
    const altroSpanY = altro.orientamentoScaffale === 'ORIZZONTALE' ? altroInfo.max_colonne : altroInfo.max_righe;
    return !(xNuovo >= altro.coordinataX + altroSpanX + 1 || xNuovo + spanX <= altro.coordinataX - 1 || yNuovo >= altro.coordinataY + altroSpanY + 1 || yNuovo + spanY <= altro.coordinataY - 1);
  });
  return !isSovrapposto;
};

const calcolaStilePreview = () => {
  if (!scaffaleSelezionato.value || hoverX.value === null) return { display: 'none' };
  const info = getDatiTecniciScaffale(scaffaleSelezionato.value.idScaffale);
  const spanX = scaffaleSelezionato.value.orientamentoScaffale === 'ORIZZONTALE' ? info.max_righe : info.max_colonne;
  const spanY = scaffaleSelezionato.value.orientamentoScaffale === 'ORIZZONTALE' ? info.max_colonne : info.max_righe;
  return { gridColumn: `${hoverX.value + 1} / span ${spanX}`, gridRow: `${hoverY.value + 1} / span ${spanY}`, width: `${(spanX * 50) - 4}px`, height: `${(spanY * 50) - 4}px`, margin: '2px' };
};

const calcolaStileScaffale = (cella) => {
  const info = getDatiTecniciScaffale(cella.idScaffale);
  if (!info) return {};
  const spanX = cella.orientamentoScaffale === 'ORIZZONTALE' ? info.max_righe : info.max_colonne;
  const spanY = cella.orientamentoScaffale === 'ORIZZONTALE' ? info.max_colonne : info.max_righe;
  return { gridColumn: `${cella.coordinataX + 1} / span ${spanX}`, gridRow: `${cella.coordinataY + 1} / span ${spanY}`, width: `${(spanX * 50) - 4}px`, height: `${(spanY * 50) - 4}px`, margin: '2px' };
};

const calcolaStileInnerGrid = (cella) => {
  const info = getDatiTecniciScaffale(cella.idScaffale);
  if (!info) return {};
  return { display: 'grid', gridTemplateColumns: `repeat(${cella.orientamentoScaffale === 'ORIZZONTALE' ? info.max_righe : info.max_colonne}, 1fr)`, gridTemplateRows: `repeat(${cella.orientamentoScaffale === 'ORIZZONTALE' ? info.max_colonne : info.max_righe}, 1fr)`, width: '100%', height: '100%', gap: '2px', padding: '2px' };
};

const calcolaNumeroSlot = (cella) => { const info = getDatiTecniciScaffale(cella.idScaffale); return info ? info.max_colonne * info.max_righe : 1; };
const attivaModifica = () => { mappaOriginale = JSON.parse(JSON.stringify(mappaDati.value)); isEditing.value = true; };
const annullaModifiche = () => { mappaDati.value = mappaOriginale; scaffaleSelezionato.value = null; azzeraHover(); isEditing.value = false; };
const selezionaScaffale = (cella) => { scaffaleSelezionato.value = scaffaleSelezionato.value?.id === cella.id ? null : cella; azzeraHover(); };
const ruotaScaffaleSelezionato = () => { if (!scaffaleSelezionato.value) return; scaffaleSelezionato.value.orientamentoScaffale = scaffaleSelezionato.value.orientamentoScaffale === 'ORIZZONTALE' ? 'VERTICALE' : 'ORIZZONTALE'; if(hoverX.value !== null) isPosizioneValida.value = controllaValidita(hoverX.value, hoverY.value); };
const spostaScaffaleQui = (x, y) => { if (!scaffaleSelezionato.value || !isPosizioneValida.value) return; scaffaleSelezionato.value.coordinataX = x; scaffaleSelezionato.value.coordinataY = y; scaffaleSelezionato.value = null; azzeraHover(); };
const salvaMappa = async () => { isSaving.value = true; try { await api.post('/api/mappa/salva-posizioni', mappaDati.value); isEditing.value = false; scaffaleSelezionato.value = null; } catch (error) { alert("Errore salvataggio."); } finally { isSaving.value = false; } };

onMounted(async () => {
  const ruolo = sessionStorage.getItem('ruolo');
  if (!ruolo) { router.push('/'); return; }
  try {
    const [resMappa, resReparti, resScaffali] = await Promise.all([
      api.get('/api/mappa/carica'),
      api.get('/api/reparti/carica'),
      api.get('/api/scaffali/carica')
    ]);
    mappaDati.value = resMappa.data;
    repartiDati.value = resReparti.data;
    scaffaliDati.value = resScaffali.data;

    try {
      const resProdotti = await api.get('/api/prodotti/carica'); 
      tuttiIProdotti.value = resProdotti.data;
    } catch(e) { console.error("Errore Prodotti:", e); }

    let lottiBackend = [];
    try {
      const resLotti = await api.get('/api/batch-prodotti/carica');
      lottiBackend = resLotti.data;
    } catch (e) { console.error("Errore Lotti:", e); }

    let scaffaliBackend = [];
    try {
      const resBatchScaffali = await api.get('/api/batch-scaffale/carica');
      scaffaliBackend = resBatchScaffali.data;
      batchScaffaliDati.value = scaffaliBackend;
    } catch (e) { console.error("Errore API Batch Scaffali:", e); }

    if (lottiBackend.length > 0) {
      const lottiCalcolati = lottiBackend.map(lotto => {
        const qtaGiaAssegnata = scaffaliBackend
          .filter(bs => Number(bs.idBatchProdotti) === Number(lotto.id))
          .reduce((sum, item) => sum + item.qta, 0);

        return {
          ...lotto,
          quantitaTotale: lotto.quantita, 
          quantita: lotto.quantita - qtaGiaAssegnata 
        };
      });

      tuttiIBatch.value = lottiCalcolati;
      lottiSospesi.value = lottiCalcolati.filter(l => l.quantita > 0);
    }

  } catch (e) {
    alert("Errore nel caricamento del magazzino.");
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
        <div class="header-left"><div class="divider"></div><h1>Dettaglio Reparto</h1></div>
        <button @click="router.push('/MappaOverview')" class="btn-back"><svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path></svg>Torna alla Planimetria</button>
      </header>

      <main class="content-area">
        <div v-if="loading" class="loading-overlay"><div class="spinner"></div> Caricamento...</div>
        <div v-else-if="repartoVisuale" class="reparto-workspace">
          <div class="control-panel">
            <div class="reparto-info">
              <span class="id-badge">ID #{{ repartoVisuale.id }}</span>
              <h2>{{ repartoVisuale.nome }}</h2>
              <span class="temp-badge" :class="{ 'cold': repartoVisuale.temperatura <= 8 }">{{ repartoVisuale.temperatura }}°C</span>
            </div>
            <div class="tools">
              <div v-if="!isEditing" class="view-controls">
                <button @click="attivaModifica" class="btn btn-primary">Modifica Disposizione</button>
              </div>
              <div v-else class="edit-controls">
                <span class="edit-status">{{ scaffaleSelezionato ? `Scaffale selezionato.` : 'Clicca per muovere.' }}</span>
                <button @click="salvaMappa" class="btn btn-success" :disabled="isSaving">Conferma</button>
                <button @click="annullaModifiche" class="btn btn-danger" :disabled="isSaving">Annulla</button>
              </div>
            </div>
          </div>

          <div class="grid-container-wrapper">
            <div class="grid-scroll-area">
              <div class="ingresso-reparto">PORTA REPARTO</div>

              <div class="griglia-dinamica" :class="{ 'edit-mode': isEditing }" :style="{ gridTemplateColumns: `repeat(${repartoVisuale.maxX}, 50px)`, gridTemplateRows: `repeat(${repartoVisuale.maxY}, 50px)` }" @mouseleave="azzeraHover">
                <template v-if="isEditing && scaffaleSelezionato">
                  <template v-for="y in repartoVisuale.maxY" :key="'row-'+y">
                    <div v-for="x in repartoVisuale.maxX" :key="'col-'+x+'-row-'+y" class="cella-fantasma" :style="{ gridColumn: x, gridRow: y }" @mouseover="impostaHover(x - 1, y - 1)" @click="spostaScaffaleQui(x - 1, y - 1)"></div>
                  </template>
                </template>

                <div v-if="isEditing && scaffaleSelezionato && hoverX !== null" class="scaffale-preview" :class="isPosizioneValida ? 'valido' : 'invalido'" :style="calcolaStilePreview()"></div>

                <div v-for="cella in getScaffaliPerReparto(repartoVisuale.id)" :key="cella.id" class="scaffale-item" :class="{ 'is-editing': isEditing, 'is-selected': scaffaleSelezionato?.id === cella.id, 'piano-inattivo': !haPiano(cella) }" :style="calcolaStileScaffale(cella)" @click="isEditing ? selezionaScaffale(cella) : null">
                  
                  <div class="scaffale-header-fluttuante">
                    <div class="header-main" :class="{'editing-header': isEditing}">
                      <span class="badge-id">S{{ cella.id }}</span>
                      
                      <div class="peso-indicatore" :class="{'peso-critico': calcolaPesoOccupatoScaffale(cella.id) > (getDatiTecniciScaffale(cella.idScaffale)?.max_peso * 0.9)}">
                        {{ calcolaPesoOccupatoScaffale(cella.id) }} / {{ getDatiTecniciScaffale(cella.idScaffale)?.max_peso || '?' }} kg
                      </div>

                      <button v-if="!isEditing" @click.stop="apriInfoScaffale(cella)" class="btn-info-mini" title="Info Scaffale"><svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg></button>
                      <div class="piano-controls" v-if="!isEditing">
                        <button @click.stop="cambiaPiano(cella.id, cella.idScaffale, -1)" :disabled="getPiano(cella.id) <= 0" class="btn-piano-mini"><svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 14l-7 7m0 0l-7-7m7 7V3"></path></svg></button>
                        <span class="piano-text">P{{ getPiano(cella.id) + 1 }}</span>
                        <button @click.stop="cambiaPiano(cella.id, cella.idScaffale, 1)" :disabled="getPiano(cella.id) >= ((getDatiTecniciScaffale(cella.idScaffale)?.max_altezza - 1) || 0)" class="btn-piano-mini"><svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 10l7-7m0 0l7 7m-7-7v18"></path></svg></button>
                      </div>
                    </div>
                    <button v-if="isEditing && scaffaleSelezionato?.id === cella.id" @click.stop="ruotaScaffaleSelezionato" class="btn-inline-rotate"><svg class="icon-sm" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path></svg></button>
                  </div>

                  <div class="scaffale-inner-grid" :style="calcolaStileInnerGrid(cella)">
                    <div 
                      v-for="slotIndex in calcolaNumeroSlot(cella)" 
                      :key="slotIndex" 
                      class="slot-1x1"
                      :class="{'cursor-assegna': lottoDaAssegnare && !isEditing, 'cursor-info': !lottoDaAssegnare && !isEditing && getSlotStatus(cella, slotIndex) !== 'vuoto'}"
                      @click.stop="gestisciClickCella(cella, slotIndex)"
                    >
                      <div v-if="haPiano(cella) && getSlotStatus(cella, slotIndex) !== 'vuoto'" class="batch-placeholder" :class="getSlotStatus(cella, slotIndex)">
                         <span class="spazio-indicatore">{{ calcolaSpazioOccupatoCella(cella.id, getPiano(cella.id), Math.floor((slotIndex-1) / (cella.orientamentoScaffale === 'ORIZZONTALE' ? getDatiTecniciScaffale(cella.idScaffale).max_righe : getDatiTecniciScaffale(cella.idScaffale).max_colonne)), ((slotIndex-1) % (cella.orientamentoScaffale === 'ORIZZONTALE' ? getDatiTecniciScaffale(cella.idScaffale).max_righe : getDatiTecniciScaffale(cella.idScaffale).max_colonne))) }}/{{ MAX_SPAZIO_CELLA }}</span>
                      </div>
                    </div>
                  </div>

                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>

    <div v-if="!isEditing" class="floating-assignment-panel">
      <div class="panel-header">
        <h3>Merce da Assegnare</h3>
        <span class="badge">{{ lottiSospesi.length }}</span>
      </div>
      <div class="batch-list">
        <div v-for="lotto in lottiSospesi" :key="lotto.id" class="batch-card" :class="{'selected': lottoDaAssegnare?.id === lotto.id}" @click="selezionaLotto(lotto)">
          <div class="batch-info">
            <strong>Lotto #{{ lotto.id }}</strong>
            <span class="text-blue font-bold">{{ getNomeProdotto(lotto.idProdotto) }}</span>
            <span class="qta-totale">Disp: {{ lotto.quantita }} pz</span>
          </div>
          <div v-if="lottoDaAssegnare?.id === lotto.id" class="assegnazione-controls" @click.stop>
            <label>Qta da piazzare:</label>
            <input type="number" v-model.number="quantitaSelezionata" min="1" :max="lotto.quantita" />
            <p class="help-text">Clicca su uno slot per inserire.</p>
          </div>
        </div>
        <div v-if="lottiSospesi.length === 0" class="empty-list">Nessun lotto in attesa.</div>
      </div>

      <div class="panel-footer" v-if="nuoviAssegnamenti.length > 0 || modifichePendenti.length > 0">
        <div class="unsaved-count"><span>Modifiche in sospeso!</span></div>
        <div class="action-buttons">
          <button @click="salvaAssegnamenti" class="btn-save-batch" :disabled="isSavingAssignments">{{ isSavingAssignments ? 'Salvataggio...' : 'Conferma Tutto' }}</button>
          <button @click="annullaAssegnamenti" class="btn-cancel-batch" :disabled="isSavingAssignments">Annulla</button>
        </div>
      </div>
    </div>

    <div v-if="cellaDettaglioAttiva" class="modal-overlay" @click="chiudiDettaglioCella">
      <div class="modal-content-large" @click.stop>
        <div class="modal-header">
          <h3>Dettaglio Slot <span class="text-blue">R{{ cellaDettaglioAttiva.riga + 1 }} - C{{ cellaDettaglioAttiva.colonna + 1 }}</span></h3>
          <button @click="chiudiDettaglioCella" class="btn-close-modal">
            <svg class="icon-sm" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="info-row highlight-row">
            <span class="info-label">Spazio Utilizzato:</span>
            <span class="info-value" :class="{'text-red-500': cellaDettaglioAttiva.spazioOccupato > 90}">{{ cellaDettaglioAttiva.spazioOccupato }} / {{ MAX_SPAZIO_CELLA }}</span>
          </div>
          <h4 class="mt-4 mb-2 font-bold text-gray-700">Lotti presenti nello slot:</h4>
          <div class="lotti-lista-modal">
            <div v-for="item in cellaDettaglioAttiva.elementi" :key="item.idBatchProdotti" class="lotto-modal-item" :class="{'border-l-4 border-yellow-500': item.isNuovo, 'border-l-4 border-blue-500': !item.isNuovo}">
              <div class="flex justify-between items-center">
                <span class="font-bold">Lotto #{{ item.idBatchProdotti }}</span>
                <span class="badge" :class="item.isNuovo ? 'bg-yellow-100 text-yellow-800' : 'bg-blue-100 text-blue-800'">
                  {{ item.isNuovo ? 'Nuovo' : 'Salvato' }}
                </span>
              </div>
              <div class="text-sm text-gray-600 mt-1 font-bold">{{ item.prodotto ? item.prodotto.nome : 'Sconosciuto' }}</div>
              <div class="flex justify-between mt-2 text-sm text-gray-500">
                <span>In cella: <strong>{{ item.qta }} pz</strong></span>
                <span>Peso: <strong>{{ Math.round(item.qta * (item.prodotto?.pesoUnitario || 0) * 10) / 10 }} kg</strong></span>
              </div>
              <div class="removal-controls mt-3">
                <label>Rimuovi:</label>
                <div class="flex gap-2">
                  <input type="number" v-model.number="item.qtaRimuovere" min="1" :max="item.qta" class="input-remove" />
                  <button @click="rimuoviDaCella(item)" class="btn-remove-sm">Togli</button>
                </div>
              </div>
            </div>
          </div>
          <div v-if="cellaDettaglioAttiva.elementi.length === 0" class="text-center text-gray-500 py-4">Slot vuoto.</div>
        </div>
      </div>
    </div>

    <div v-if="scaffaleInfoAttivo" class="modal-overlay" @click="chiudiInfoScaffale">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Dettagli Scaffale <span class="text-blue">S{{ scaffaleInfoAttivo.id }}</span></h3>
          <button @click="chiudiInfoScaffale" class="btn-close-modal"><svg class="icon-sm" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg></button>
        </div>
        <div class="modal-body">
          <div class="info-row"><span class="info-label">X: {{ scaffaleInfoAttivo.coordinataX }}, Y: {{ scaffaleInfoAttivo.coordinataY }}</span></div>
          <div class="info-row"><span class="info-label">Piani:</span><span class="info-value">{{ getDatiTecniciScaffale(scaffaleInfoAttivo.idScaffale)?.max_altezza }}</span></div>
          <div class="info-row highlight-row"><span class="info-label">Peso:</span><span class="info-value">{{ calcolaPesoOccupatoScaffale(scaffaleInfoAttivo.id) }} / {{ getDatiTecniciScaffale(scaffaleInfoAttivo.idScaffale)?.max_peso || 'N/A' }} Kg</span></div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-layout { display: flex; height: 100vh; width: 100vw; background-color: #f8fafc; overflow: hidden; position: relative;}
.main-content { flex: 1; display: flex; flex-direction: column; height: 100vh; min-width: 0; }
.topbar { flex-shrink: 0; display: flex; align-items: center; justify-content: space-between; padding: 15px 30px; background-color: white; border-bottom: 1px solid #e2e8f0; height: 70px; box-sizing: border-box; }
.header-left { display: flex; align-items: center; }
.divider { width: 4px; height: 20px; background-color: #3b82f6; margin-right: 15px; border-radius: 2px; }
.header-left h1 { margin: 0; font-size: 1.3rem; color: #1e293b; font-weight: 700; }
.content-area { flex: 1; padding: 20px 30px; display: flex; flex-direction: column; overflow: hidden; }
.icon { width: 18px; height: 18px; }
.icon-sm { width: 14px; height: 14px; }
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
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(15, 23, 42, 0.6); backdrop-filter: blur(4px); z-index: 9999; display: flex; align-items: center; justify-content: center; }
.modal-content { background: white; border-radius: 12px; width: 420px; max-width: 90%; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1); overflow: hidden; border: 1px solid #cbd5e1;}
.modal-content-large { background: white; border-radius: 12px; width: 500px; max-width: 95%; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1); overflow: hidden; border: 1px solid #cbd5e1;}
.modal-header { background: #f8fafc; padding: 15px 20px; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; color: #0f172a; font-size: 1.2rem; font-weight: 800;}
.text-blue { color: #3b82f6; }
.btn-close-modal { background: transparent; border: none; color: #64748b; cursor: pointer; transition: 0.2s; padding: 4px; display: flex; justify-content: center; align-items: center; }
.btn-close-modal:hover { color: #ef4444; background: #fee2e2; border-radius: 4px; }
.modal-body { padding: 20px; display: flex; flex-direction: column; gap: 12px; }
.info-row { display: flex; justify-content: space-between; align-items: center; padding-bottom: 8px; border-bottom: 1px dashed #e2e8f0; }
.info-row:last-child { border-bottom: none; padding-bottom: 0; }
.highlight-row { background: #f1f5f9; padding: 10px; border-radius: 6px; border: none;}
.info-label { font-weight: 600; color: #475569; font-size: 0.9rem; display: flex; align-items: center; gap: 8px; }
.info-value { font-weight: 800; color: #0f172a; font-size: 0.95rem; }
.grid-container-wrapper { flex: 1; background-color: #e2e8f0; position: relative; overflow: hidden; }
.grid-scroll-area { width: 100%; height: 100%; overflow: auto; padding: 40px; box-sizing: border-box; display: flex; flex-direction: column; align-items: center; }
.ingresso-reparto { background: #cbd5e1; padding: 5px 60px; font-weight: 800; color: #334155; font-size: 0.8rem; letter-spacing: 4px; border-top-left-radius: 8px; border-top-right-radius: 8px; margin-bottom: 0; border: 3px solid #94a3b8; border-bottom: none; }
.griglia-dinamica { display: grid; gap: 0; background-image: linear-gradient(#f1f5f9 1px, transparent 1px), linear-gradient(90deg, #f1f5f9 1px, transparent 1px); background-size: 50px 50px; background-color: #ffffff; border: 4px solid #94a3b8; box-shadow: 0 10px 25px rgba(0,0,0,0.1); width: max-content; position: relative; }
.griglia-dinamica.edit-mode { border-color: #3b82f6; }
.scaffale-preview { position: relative; z-index: 15; pointer-events: none; transition: transform 0.1s; }
.scaffale-preview.valido { background: rgba(16, 185, 129, 0.2); border: 3px dashed #10b981; }
.scaffale-preview.invalido { background: rgba(239, 68, 68, 0.2); border: 3px dashed #ef4444; }
.cella-fantasma { width: 50px; height: 50px; z-index: 20; border: 1px dashed transparent; cursor: crosshair; }
.scaffale-buffer { background: rgba(203, 213, 225, 0.15); border: 1px dashed rgba(148, 163, 184, 0.4); border-radius: 4px; z-index: 1; pointer-events: none; transition: all 0.2s; }
.scaffale-buffer.is-editing { background: rgba(239, 68, 68, 0.05); border-color: rgba(239, 68, 68, 0.2); }
.scaffale-item { display: block; box-sizing: border-box; position: relative; z-index: 5; border: 2px solid #334155; border-radius: 4px; background-color: #f8fafc; transition: all 0.2s ease; box-shadow: 2px 2px 5px rgba(0,0,0,0.1); }
.scaffale-inner-grid { box-sizing: border-box; width: 100%; height: 100%; }
.slot-1x1 { background-color: #e2e8f0; border: 1px solid #cbd5e1; display: flex; align-items: center; justify-content: center; position: relative;}
.scaffale-header-fluttuante { position: absolute; top: -14px; left: -10px; display: flex; align-items: center; gap: 6px; z-index: 30; }
.header-main { display: flex; align-items: center; background: #ffffff; border: 1px solid #cbd5e1; border-radius: 6px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); overflow: hidden; pointer-events: auto; }
.header-main.editing-header { background: transparent; border: none; box-shadow: none; overflow: visible;}
.badge-id { background: #3b82f6; color: white; font-size: 0.65rem; font-weight: bold; padding: 4px 8px; }
.header-main.editing-header .badge-id { background: #0f172a; border-radius: 6px; box-shadow: 0 2px 4px rgba(0,0,0,0.2); }
.btn-info-mini { background: transparent; color: #3b82f6; border: none; padding: 4px; margin-left: 2px; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: 0.2s;}
.btn-info-mini svg { width: 14px; height: 14px; }
.btn-info-mini:hover { color: #2563eb; background: #eff6ff; border-radius: 4px;}
.piano-controls { display: flex; align-items: center; background: #ffffff; padding: 0 4px; border-left: 1px solid #e2e8f0; }
.btn-piano-mini { background: transparent; color: #64748b; border: none; padding: 4px; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: 0.2s;}
.btn-piano-mini svg { width: 12px; height: 12px; }
.btn-piano-mini:hover:not(:disabled) { color: #0f172a; background: #f1f5f9; border-radius: 4px;}
.btn-piano-mini:disabled { opacity: 0.3; cursor: not-allowed; }
.piano-text { color: #0f172a; font-size: 0.7rem; font-weight: 800; margin: 0 6px; }
.btn-inline-rotate { pointer-events: auto; background-color: #f59e0b; color: white; border: none; border-radius: 6px; width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; cursor: pointer; box-shadow: 0 2px 4px rgba(0,0,0,0.2); transition: 0.2s;}
.btn-inline-rotate:hover { background-color: #d97706; }
.scaffale-item.is-editing { cursor: pointer; border-style: dashed; z-index: 25;}
.scaffale-item.is-editing:hover { border-color: #3b82f6; background-color: #eff6ff;}
.scaffale-item.is-selected { border: 3px solid #10b981; box-shadow: 0 0 15px rgba(16, 185, 129, 0.4); z-index: 30; opacity: 0.8;}

.peso-indicatore { font-size: 0.7rem; font-weight: 800; color: #475569; padding: 0 8px; border-left: 1px solid #e2e8f0; border-right: 1px solid #e2e8f0; }
.peso-critico { color: #ef4444; }
.batch-placeholder { width: 85%; height: 85%; border-radius: 4px; display: flex; align-items: center; justify-content: center; transition: 0.3s; color: white; text-shadow: 0px 1px 2px rgba(0,0,0,0.5); }
.batch-placeholder.salvato { background-color: #3b82f6; box-shadow: 0 2px 6px rgba(59, 130, 246, 0.4); }
.batch-placeholder.nuovo { background-color: #f59e0b; box-shadow: 0 2px 6px rgba(245, 158, 11, 0.4); animation: pulse 1.5s infinite; }
.spazio-indicatore { font-size: 0.65rem; font-weight: bold; }

@keyframes pulse { 0% { transform: scale(1); } 50% { transform: scale(1.05); } 100% { transform: scale(1); } }

.slot-1x1.cursor-assegna { cursor: crosshair; }
.slot-1x1.cursor-info { cursor: pointer; }
.slot-1x1.cursor-assegna:hover { background-color: rgba(16, 185, 129, 0.2); border-color: #10b981; }
.slot-1x1.cursor-info:hover { background-color: rgba(59, 130, 246, 0.2); }

.floating-assignment-panel { position: absolute; top: 90px; right: 30px; width: 280px; background: rgba(255, 255, 255, 0.95); backdrop-filter: blur(10px); border: 1px solid #cbd5e1; border-radius: 12px; box-shadow: 0 10px 25px rgba(0,0,0,0.15); z-index: 100; display: flex; flex-direction: column; max-height: calc(100vh - 120px); overflow: hidden; }
.panel-header { background: #f8fafc; padding: 12px 16px; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; }
.panel-header h3 { margin: 0; font-size: 1rem; color: #0f172a; }
.panel-header .badge { background: #3b82f6; color: white; padding: 2px 8px; border-radius: 12px; font-size: 0.8rem; font-weight: bold;}
.batch-list { padding: 10px; overflow-y: auto; display: flex; flex-direction: column; gap: 8px; flex: 1;}
.batch-card { border: 1px solid #e2e8f0; border-radius: 8px; padding: 10px; cursor: pointer; transition: 0.2s; background: white; }
.batch-card:hover { border-color: #94a3b8; }
.batch-card.selected { border-color: #10b981; box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.2); background: #f0fdf4; }
.batch-info { display: flex; flex-direction: column; font-size: 0.9rem; }
.qta-totale { color: #64748b; font-size: 0.8rem; margin-top: 4px; font-weight: 600;}
.assegnazione-controls { margin-top: 10px; padding-top: 10px; border-top: 1px dashed #cbd5e1; display: flex; flex-direction: column; gap: 5px; }
.assegnazione-controls label { font-size: 0.8rem; font-weight: bold; color: #334155; }
.assegnazione-controls input { padding: 6px; border: 1px solid #cbd5e1; border-radius: 4px; font-family: inherit;}
.help-text { font-size: 0.75rem; color: #10b981; font-weight: bold; margin: 4px 0 0 0; }
.empty-list { text-align: center; color: #64748b; font-size: 0.9rem; padding: 20px 0; }

.panel-footer { background: #fffbeb; padding: 12px; border-top: 1px solid #fde68a; display: flex; flex-direction: column; gap: 10px; }
.unsaved-count { font-size: 0.8rem; font-weight: bold; color: #d97706; text-align: center; }
.action-buttons { display: flex; gap: 8px; }
.btn-save-batch { flex: 2; background: #10b981; color: white; border: none; padding: 8px; border-radius: 6px; font-weight: bold; cursor: pointer; transition: 0.2s; }
.btn-save-batch:hover:not(:disabled) { background: #059669; }
.btn-cancel-batch { flex: 1; background: #f1f5f9; color: #475569; border: 1px solid #cbd5e1; padding: 8px; border-radius: 6px; font-weight: bold; cursor: pointer; transition: 0.2s; }
.btn-cancel-batch:hover:not(:disabled) { background: #e2e8f0; color: #0f172a; }

.lotti-lista-modal { max-height: 350px; overflow-y: auto; display: flex; flex-direction: column; gap: 10px; padding-right: 5px; }
.lotto-modal-item { background: #f8fafc; padding: 12px; border-radius: 6px; border: 1px solid #e2e8f0; }
.badge { padding: 2px 6px; border-radius: 4px; font-size: 0.75rem; font-weight: bold; }
.bg-yellow-100 { background-color: #fef3c7; } .text-yellow-800 { color: #92400e; }
.bg-blue-100 { background-color: #dbeafe; } .text-blue-800 { color: #1e40af; }
.border-yellow-500 { border-left-color: #eab308; } .border-blue-500 { border-left-color: #3b82f6; }
.text-red-500 { color: #ef4444; }

.removal-controls { background: #fff; padding: 8px; border-radius: 4px; border: 1px dashed #cbd5e1; display: flex; align-items: center; justify-content: space-between;}
.removal-controls label { font-size: 0.8rem; font-weight: bold; color: #475569;}
.input-remove { width: 60px; padding: 4px; border: 1px solid #cbd5e1; border-radius: 4px;}
.btn-remove-sm { background: #ef4444; color: white; border: none; padding: 4px 10px; border-radius: 4px; font-weight: bold; cursor: pointer; transition: 0.2s;}
.btn-remove-sm:hover { background: #dc2626;}

.flex { display: flex; } .justify-between { justify-content: space-between; } .items-center { align-items: center; } .gap-2 { gap: 0.5rem; }
.mt-1 { margin-top: 0.25rem; } .mt-2 { margin-top: 0.5rem; } .mt-3 { margin-top: 0.75rem;} .mt-4 { margin-top: 1rem; } .mb-2 { margin-bottom: 0.5rem; }
.py-4 { padding-top: 1rem; padding-bottom: 1rem; }
.font-bold { font-weight: bold; } .text-sm { font-size: 0.875rem; } .text-gray-500 { color: #6b7280; } .text-gray-600 { color: #4b5563; } .text-gray-700 { color: #374151; } .text-center { text-align: center; }
</style>