<script setup>
import { ref, onMounted, computed } from 'vue' // Aggiunto 'computed'
import { useRouter } from 'vue-router'
import api from '../api'

//COMPONENTI COMUNI
import AdminSidebar from '../components/AdminSidebar.vue'

const router = useRouter()
const dipendenti = ref([])
const mostraModale = ref(false)
const isModifica = ref(false)
const utenteSelezionatoId = ref(null)
const errorMessage = ref('')

// --- VARIABILI PER LA SICUREZZA ENTERPRISE ---
const staVerificandoAdmin = ref(false)
const isPasswordUnlocked = ref(false)
const adminPassword = ref('')

const form = ref({
  nome: '',
  cognome: '',
  data_nascita: '',
  email: '',
  password: '',
  confermaPassword: '',
  ruolo: 'Dipendente'
})

// ==========================================
// 1. CARICA LA LISTA
// ==========================================
const caricaDipendenti = async () => {
  try {
    const response = await api.get('/api/auth/utenti')
    dipendenti.value = response.data.filter(utente => utente.ruolo && utente.ruolo.nomeRuolo === 'Dipendente')
  } catch (error) {
    console.error("Errore di caricamento:", error)
  }
}

onMounted(() => caricaDipendenti())

// --- RICERCA LATO CLIENT ---
const searchQuery = ref('')

const dipendentiFiltrati = computed(() => {
  if (!searchQuery.value) {
    return dipendenti.value
  }

  const q = searchQuery.value.toLowerCase()

  return dipendenti.value.filter(dip => {
    const nomeMatch = dip.nome ? dip.nome.toLowerCase().includes(q) : false;
    const cognomeMatch = dip.cognome ? dip.cognome.toLowerCase().includes(q) : false;
    const emailMatch = dip.email ? dip.email.toLowerCase().includes(q) : false;
    const idMatch = dip.id ? dip.id.toString().includes(q) : false;

    // Permette di cercare "Mario Rossi" interamente
    const nomeCompletoMatch = dip.nome && dip.cognome
        ? `${dip.nome} ${dip.cognome}`.toLowerCase().includes(q)
        : false;

    return idMatch || nomeMatch || cognomeMatch || emailMatch || nomeCompletoMatch;
  })
})

// ==========================================
// 2. GESTIONE MODALI E SALVATAGGIO (Invariato)
// ==========================================
const apriModaleAggiungi = () => {
  isModifica.value = false
  errorMessage.value = ''
  staVerificandoAdmin.value = false
  isPasswordUnlocked.value = false
  form.value = { nome: '', cognome: '', data_nascita: '', email: '', password: '', confermaPassword: '', ruolo: 'Dipendente' }
  mostraModale.value = true
}

const apriModaleModifica = (dipendente) => {
  isModifica.value = true
  utenteSelezionatoId.value = dipendente.id
  errorMessage.value = ''
  staVerificandoAdmin.value = false
  isPasswordUnlocked.value = false
  adminPassword.value = ''

  let dataFormattata = '';
  if (dipendente.data_nascita) {
    dataFormattata = dipendente.data_nascita.split('T')[0];
  }

  form.value = {
    nome: dipendente.nome,
    cognome: dipendente.cognome,
    data_nascita: dataFormattata,
    email: dipendente.email,
    password: '',
    confermaPassword: '',
    ruolo: dipendente.ruolo ? dipendente.ruolo.nomeRuolo : 'Dipendente'
  }
  mostraModale.value = true
}

const chiudiModale = () => { mostraModale.value = false }

const sbloccaCambioPassword = async () => {
  errorMessage.value = '';
  const emailAdmin = sessionStorage.getItem('emailUtente');

  if (!emailAdmin) {
    errorMessage.value = "Errore di sistema: Email Admin non trovata.";
    return;
  }

  try {
    await api.post('/api/auth/login', { email: emailAdmin, password: adminPassword.value });
    isPasswordUnlocked.value = true;
    staVerificandoAdmin.value = false;
    adminPassword.value = '';
  } catch (error) {
    errorMessage.value = "Password Amministratore non valida.";
  }
}

const salvaDipendente = async () => {
  errorMessage.value = ''

  if (!form.value.data_nascita) {
    errorMessage.value = "La data di nascita è obbligatoria.";
    return;
  }

  if (!isModifica.value && form.value.password !== form.value.confermaPassword) {
    errorMessage.value = "Le password non coincidono!";
    return;
  }

  try {
    if (isModifica.value) {
      await api.put(`/api/auth/modifica/${utenteSelezionatoId.value}`, {
        nome: form.value.nome,
        cognome: form.value.cognome,
        data_nascita: form.value.data_nascita,
        email: form.value.email,
        password: form.value.password
      })
    } else {
      await api.post('/api/auth/registrati', {
        nome: form.value.nome,
        cognome: form.value.cognome,
        data_nascita: form.value.data_nascita,
        email: form.value.email,
        password: form.value.password,
        ruolo: form.value.ruolo
      })
    }
    chiudiModale()
    caricaDipendenti()
  } catch (error) {
    errorMessage.value = error.response?.data || "Errore di connessione al server."
  }
}

const eliminaDipendente = async (id) => {
  if (confirm("Sei sicuro di voler eliminare questo dipendente?")) {
    try {
      await api.delete(`/api/auth/elimina/${id}`)
      caricaDipendenti()
    } catch (error) {
      alert("Errore durante l'eliminazione.")
    }
  }
}
</script>

<template>
  <div class="dashboard-layout">
    <AdminSidebar />

    <div class="main-content">

      <header class="topbar">
        <div class="header-left">
          <div class="divider"></div>
          <h1>Gestione Dipendenti</h1>
        </div>
        <button @click="apriModaleAggiungi" class="btn-primary">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"></path></svg>
          Nuovo Dipendente
        </button>
      </header>

      <main class="content-area">
        <div class="card">

          <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #e2e8f0; padding: 20px 24px;">
            <div style="display: flex; align-items: center; gap: 16px;">
              <h3 style="margin: 0; font-size: 18px; color: #0f172a;">Anagrafica Personale</h3>
              <span class="badge" style="background: #e0e7ff; color: #4f46e5; padding: 4px 12px; border-radius: 12px; font-size: 13px; font-weight: 600;">
                {{ dipendentiFiltrati.length }} Dipendenti
              </span>
            </div>

            <div class="search-container" style="position: relative; width: 280px;">
              <svg style="position: absolute; left: 12px; top: 50%; transform: translateY(-50%); width: 18px; height: 18px; color: #94a3b8;" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
              </svg>
              <input
                  type="text"
                  v-model="searchQuery"
                  placeholder="Cerca nome, cognome, email..."
                  style="width: 100%; box-sizing: border-box; padding: 10px 12px 10px 40px; border-radius: 8px; border: 1px solid #cbd5e1; font-size: 14px; outline: none; transition: all 0.2s; box-shadow: inset 0 1px 2px rgba(0,0,0,0.02);"
                  onfocus="this.style.borderColor='#3b82f6'; this.style.boxShadow='0 0 0 3px rgba(59, 130, 246, 0.1)';"
                  onblur="this.style.borderColor='#cbd5e1'; this.style.boxShadow='inset 0 1px 2px rgba(0,0,0,0.02)';"
              />
            </div>
          </div>

          <div class="table-container">
            <table class="data-table">
              <thead>
              <tr>
                <th>ID</th>
                <th>Nome e Cognome</th>
                <th>Data di Nascita</th>
                <th>Email</th>
                <th>Ruolo</th>
                <th class="actions-col">Azioni</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="dipendente in dipendentiFiltrati" :key="dipendente.id">
                <td class="text-muted">#{{ dipendente.id }}</td>
                <td class="font-medium">{{ dipendente.nome }} {{ dipendente.cognome }}</td>
                <td>{{ dipendente.data_nascita ? new Date(dipendente.data_nascita).toLocaleDateString('it-IT') : '-' }}</td>
                <td class="text-muted">{{ dipendente.email }}</td>
                <td>
                  <span class="badge" :class="dipendente.ruolo?.nomeRuolo === 'Admin' ? 'badge-admin' : 'badge-user'" style="background: #f1f5f9; color: #475569; padding: 4px 12px; border-radius: 12px; font-size: 13px; font-weight: 600;">
                    {{ dipendente.ruolo ? dipendente.ruolo.nomeRuolo : 'Nessuno' }}
                  </span>
                </td>
                <td class="actions-col">
                  <button @click="apriModaleModifica(dipendente)" class="btn-icon edit" title="Modifica">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"></path></svg>
                  </button>
                  <button @click="eliminaDipendente(dipendente.id)" class="btn-icon delete" title="Elimina">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                  </button>
                </td>
              </tr>

              <tr v-if="dipendentiFiltrati.length === 0">
                <td colspan="6" class="empty-state" style="padding: 60px 0; color: #94a3b8; text-align: center;">
                  <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" style="width: 48px; height: 48px; margin: 0 auto 16px auto; opacity: 0.5;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>
                  <p v-if="searchQuery">Nessun dipendente trovato per "<strong>{{ searchQuery }}</strong>"</p>
                  <p v-else>Nessun utente nel sistema.</p>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </main>

    </div>

    <div v-if="mostraModale" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ isModifica ? 'Modifica Anagrafica' : 'Registra Dipendente' }}</h2>
          <button @click="chiudiModale" class="btn-close">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
          </button>
        </div>

        <div class="form-body">
          <div class="form-row">
            <div class="form-group half">
              <label>Nome</label>
              <input v-model="form.nome" type="text" placeholder="Mario" />
            </div>
            <div class="form-group half">
              <label>Cognome</label>
              <input v-model="form.cognome" type="text" placeholder="Rossi" />
            </div>
          </div>

          <div class="form-group">
            <label>Data di Nascita</label>
            <input v-model="form.data_nascita" type="date" />
          </div>

          <div class="form-group">
            <label>Email aziendale</label>
            <input v-model="form.email" type="email" placeholder="mario.rossi@wms.it" />
          </div>

          <template v-if="!isModifica">
            <div class="form-row">
              <div class="form-group half">
                <label>Password</label>
                <input v-model="form.password" type="password" placeholder="••••••••" />
              </div>
              <div class="form-group half">
                <label>Conferma Password</label>
                <input v-model="form.confermaPassword" type="password" placeholder="••••••••" />
              </div>
            </div>
            <div class="form-group">
              <label>Ruolo di sistema assegnato</label>
              <div class="role-badge fixed-role-user">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>
                <span>Dipendente Operativo</span>
              </div>
            </div>
          </template>

          <template v-if="isModifica">
            <div class="security-box" :class="{ 'is-unlocked': isPasswordUnlocked, 'is-verifying': staVerificandoAdmin }">

              <div v-if="!isPasswordUnlocked && !staVerificandoAdmin" class="locked-state">
                <div class="security-info">
                  <svg class="icon-lock" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path></svg>
                  <div>
                    <h4>Sicurezza Account</h4>
                    <p>La modifica della password è protetta.</p>
                  </div>
                </div>
                <button @click="staVerificandoAdmin = true" class="btn-unlock">Sblocca</button>
              </div>

              <div v-if="staVerificandoAdmin" class="verify-state">
                <label>Conferma la tua identità Admin</label>
                <input v-model="adminPassword" type="password" placeholder="Inserisci la tua password..." />
                <div class="verify-actions">
                  <button @click="staVerificandoAdmin = false" class="btn-ghost small">Annulla</button>
                  <button @click="sbloccaCambioPassword" class="btn-primary small">Verifica</button>
                </div>
              </div>

              <div v-if="isPasswordUnlocked" class="unlocked-state">
                <div class="security-info success">
                  <svg class="icon-unlock" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 11V7a4 4 0 118 0m-4 8v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2z"></path></svg>
                  <label>Imposta nuova password</label>
                </div>
                <input v-model="form.password" type="password" placeholder="Lascia vuoto per non cambiare" />
              </div>
            </div>
          </template>

          <div v-if="errorMessage" class="error-banner">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            {{ errorMessage }}
          </div>
        </div>

        <div class="modal-actions">
          <button @click="chiudiModale" class="btn-secondary">Annulla</button>
          <button @click="salvaDipendente" class="btn-success">Salva Dipendente</button>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

/* ------- LAYOUT BASE ------- */
.page-layout { height: 100vh; background-color: #f8fafc; display: flex; flex-direction: column; margin: 0; font-family: 'Inter', sans-serif; color: #334155; }

/* ------- TOPBAR ------- */
.topbar { background: white; padding: 20px 40px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #e2e8f0; }
.header-left { display: flex; align-items: center; gap: 16px; }
.divider { width: 1px; height: 24px; background-color: #e2e8f0; }
.topbar h1 { margin: 0; font-size: 20px; font-weight: 700; color: #0f172a; letter-spacing: -0.5px; }

/*------sidebar------*/
.dashboard-layout {
  display: flex;
  flex-direction: row; /* Mette Sidebar e Main Content uno di fianco all'altro */
  height: 100vh;
  width: 100vw;
  background-color: #f8fafc;
  margin: 0;
  font-family: 'Inter', sans-serif;
  color: #334155;
  overflow: hidden; /* Blocca lo scroll dell'intera pagina... */
}

.main-content {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto; /* ...e fa scorrere solo la parte destra! */
  height: 100vh;
}

.btn-back { background: transparent; border: none; font-size: 14px; font-weight: 500; cursor: pointer; color: #64748b; display: flex; align-items: center; gap: 6px; padding: 8px; border-radius: 8px; transition: all 0.2s; }
.btn-back svg { width: 18px; height: 18px; }
.btn-back:hover { background-color: #f1f5f9; color: #0f172a; }

/* ------- BOTTONI ------- */
.btn-primary { background-color: #6366f1; color: white; border: none; padding: 10px 20px; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 14px; display: flex; align-items: center; gap: 8px; transition: background 0.2s, box-shadow 0.2s; box-shadow: 0 4px 6px -1px rgba(99, 102, 241, 0.2); }
.btn-primary:hover { background-color: #4f46e5; box-shadow: 0 6px 8px -1px rgba(99, 102, 241, 0.3); }
.btn-primary.small { padding: 8px 16px; font-size: 13px; }

.btn-secondary { background: white; color: #475569; border: 1px solid #cbd5e1; padding: 10px 20px; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 14px; transition: all 0.2s; }
.btn-secondary:hover { background: #f8fafc; border-color: #94a3b8; color: #0f172a; }

.btn-success { background-color: #10b981; color: white; border: none; padding: 10px 24px; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 14px; transition: background 0.2s, box-shadow 0.2s; box-shadow: 0 4px 6px -1px rgba(16, 185, 129, 0.2); }
.btn-success:hover { background-color: #059669; }

.btn-ghost { background: transparent; border: none; color: #64748b; font-weight: 500; font-size: 13px; cursor: pointer; padding: 8px 16px; border-radius: 6px; transition: all 0.2s; }
.btn-ghost:hover { background: #f1f5f9; color: #0f172a; }

/* ------- TABELLA ------- */
.content-area { padding: 40px; flex-grow: 1; overflow-y: auto; display: flex; flex-direction: column; align-items: center; }
.card { background: white; width: 100%; max-width: 1200px; border-radius: 12px; border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02); overflow: hidden; }
.table-container { width: 100%; overflow-x: auto; }
.data-table { width: 100%; border-collapse: collapse; text-align: left; }
.data-table th { background-color: #f8fafc; color: #64748b; font-weight: 600; font-size: 12px; text-transform: uppercase; letter-spacing: 0.5px; padding: 16px 24px; border-bottom: 1px solid #e2e8f0; }
.data-table td { padding: 16px 24px; border-bottom: 1px solid #f1f5f9; font-size: 14px; vertical-align: middle; }
.data-table tr:hover td { background-color: #f8fafc; }
.data-table tr:last-child td { border-bottom: none; }

.font-medium { font-weight: 600; color: #0f172a; }
.text-muted { color: #64748b; }
.actions-col { text-align: right; }

.btn-icon { background: white; border: 1px solid #e2e8f0; color: #64748b; cursor: pointer; width: 32px; height: 32px; border-radius: 6px; display: inline-flex; align-items: center; justify-content: center; margin-left: 8px; transition: all 0.2s; }
.btn-icon svg { width: 16px; height: 16px; }
.btn-icon:hover { border-color: #cbd5e1; color: #0f172a; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
.btn-icon.delete:hover { color: #ef4444; border-color: #fca5a5; background-color: #fef2f2; }

/* Badges */
.badge { padding: 6px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.badge-admin { background-color: #f0fdf4; color: #166534; border: 1px solid #bbf7d0; }
.badge-user { background-color: #f1f5f9; color: #475569; border: 1px solid #e2e8f0; }

.empty-state { text-align: center; color: #94a3b8; padding: 60px 20px !important; }
.empty-state svg { width: 48px; height: 48px; margin-bottom: 16px; opacity: 0.5; }
.empty-state p { margin: 0; font-weight: 500; }

/* ------- MODALE  ------- */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(15, 23, 42, 0.4); backdrop-filter: blur(4px); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-content { background: white; border-radius: 16px; width: 100%; max-width: 500px; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04); display: flex; flex-direction: column; max-height: 90vh; }
.modal-header { padding: 24px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #e2e8f0; }
.modal-header h2 { margin: 0; font-size: 18px; font-weight: 700; color: #0f172a; }
.btn-close { background: transparent; border: none; color: #94a3b8; cursor: pointer; padding: 4px; border-radius: 6px; transition: all 0.2s; }
.btn-close svg { width: 24px; height: 24px; }
.btn-close:hover { background: #f1f5f9; color: #0f172a; }

.form-body { padding: 24px; overflow-y: auto; }
.form-row { display: flex; gap: 16px; }
.form-group { margin-bottom: 20px; width: 100%; }
.form-group.half { width: 50%; }
.form-group label { display: block; font-weight: 600; margin-bottom: 8px; color: #334155; font-size: 13px; }

/* Inputs Stripe-style */
.form-group input, .form-group select { width: 100%; padding: 12px 16px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 14px; font-family: 'Inter', sans-serif; transition: all 0.2s; color: #0f172a; box-sizing: border-box; background-color: #fff; }
.form-group input::placeholder { color: #94a3b8; }
.form-group input:focus, .form-group select:focus { outline: none; border-color: #6366f1; box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15); }

/* ------- CONTROLLO SICUREZZA ------- */
.security-box { background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px; margin-top: 10px; transition: all 0.3s ease; }
.security-box.is-unlocked { background-color: #f0fdf4; border-color: #bbf7d0; }
.security-box.is-verifying { background-color: #fffbeb; border-color: #fde68a; }

.locked-state { display: flex; justify-content: space-between; align-items: center; }
.security-info { display: flex; align-items: center; gap: 12px; }
.icon-lock { width: 24px; height: 24px; color: #64748b; }
.security-info h4 { margin: 0 0 4px 0; font-size: 14px; color: #0f172a; }
.security-info p { margin: 0; font-size: 12px; color: #64748b; }

.btn-unlock { background-color: white; color: #0f172a; border: 1px solid #cbd5e1; padding: 6px 12px; border-radius: 6px; cursor: pointer; font-size: 13px; font-weight: 600; transition: all 0.2s; }
.btn-unlock:hover { border-color: #94a3b8; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }

.verify-state label { display: block; font-size: 13px; font-weight: 600; color: #b45309; margin-bottom: 12px; }
.verify-state input { width: 100%; padding: 10px 16px; border: 1px solid #fcd34d; border-radius: 8px; margin-bottom: 16px; outline: none; transition: box-shadow 0.2s; box-sizing: border-box;}
.verify-state input:focus { box-shadow: 0 0 0 3px rgba(251, 191, 36, 0.2); }
.verify-actions { display: flex; gap: 12px; justify-content: flex-end; }

.unlocked-state { display: flex; flex-direction: column; gap: 12px; }
.security-info.success .icon-unlock { width: 20px; height: 20px; color: #059669; }
.unlocked-state label { font-size: 13px; font-weight: 600; color: #059669; }
.unlocked-state input { border-color: #6ee7b7; box-sizing: border-box;}
.unlocked-state input:focus { box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.15); border-color: #10b981; }

.error-banner { margin-top: 16px; padding: 12px 16px; background-color: #fef2f2; border-left: 4px solid #ef4444; border-radius: 6px; display: flex; align-items: center; gap: 12px; color: #b91c1c; font-size: 13px; font-weight: 500; }
.error-banner svg { width: 20px; height: 20px; }

.modal-actions { padding: 20px 24px; border-top: 1px solid #e2e8f0; background: #f8fafc; display: flex; justify-content: flex-end; gap: 12px; border-radius: 0 0 16px 16px; }

/* ------- BADGE RUOLO FISSO NEL FORM ------- */
.role-badge { display: flex; align-items: center; gap: 10px; padding: 12px 16px; border-radius: 8px; font-weight: 600; font-size: 14px; }
.role-badge svg { width: 20px; height: 20px; }
.fixed-role-user { background-color: #f1f5f9; color: #475569; border: 1px solid #e2e8f0; }
.fixed-role-admin { background-color: #f0fdf4; color: #166534; border: 1px solid #bbf7d0; }
</style>