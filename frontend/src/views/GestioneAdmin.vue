<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

//COMPONENTI COMUNI
import AdminSidebar from '../components/AdminSidebar.vue'

const router = useRouter()
const nomeUtente = ref(sessionStorage.getItem('nomeUtente') || 'Admin')

const utenti = ref([])
const mostraModale = ref(false)
const isModifica = ref(false)
const utenteSelezionatoId = ref(null)
const errorMessage = ref('')

// --- VARIABILI PER LA SICUREZZA ENTERPRISE ---
// Rimossi check manuali, la sicurezza è gestita dal JWT.

const form = ref({
  nome: '',
  cognome: '',
  data_nascita: '',
  email: '',
  password: '',
  confermaPassword: '',
  ruolo: 'Admin'
})

// ==========================================
// 1. CARICA LA LISTA
// ==========================================
const caricaDipendenti = async () => {
  try {
    const response = await api.get('/api/auth/utenti')
    // RISOLTO: utente.ruolo è già la stringa 'Admin' grazie al DTO in Java!
    utenti.value = response.data.filter(utente => utente.ruolo === 'Admin')
  } catch (error) {
    console.error("Errore di caricamento:", error)
  }
}

onMounted(() => caricaDipendenti())

// ==========================================
// 2. GESTIONE MODALI
// ==========================================
const apriModaleAggiungi = () => {
  isModifica.value = false
  errorMessage.value = ''
  form.value = { nome: '', cognome: '', data_nascita: '', email: '', password: '', confermaPassword: '', ruolo: 'Admin' }
  mostraModale.value = true
}

const apriModaleModifica = (dipendente) => {
  isModifica.value = true
  utenteSelezionatoId.value = dipendente.id
  errorMessage.value = ''

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
    // RISOLTO: usa direttamente dipendente.ruolo
    ruolo: dipendente.ruolo || 'Admin'
  }
  mostraModale.value = true
}

const chiudiModale = () => {
  mostraModale.value = false
}

// ==========================================
// 4. SALVATAGGIO NEL DATABASE
// ==========================================
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
    if (error.response && error.response.data) {
      if (error.response.data.error) {
        errorMessage.value = error.response.data.error; // Es: "Errore: L'email inserita è già in uso!"
      } else {
        errorMessage.value = Object.values(error.response.data).join(', ');
      }
    } else {
      errorMessage.value = "Errore di connessione al server."
    }
  }
}

// ==========================================
// 5. ELIMINAZIONE
// ==========================================
const eliminaDipendente = async (id) => {
  if (confirm("Sei sicuro di voler eliminare questo amministratore?")) {
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
  <div class="glass-dashboard-layout">
    <div class="glass-bg-blob blob-1"></div>
    <div class="glass-bg-blob blob-2"></div>
    <div class="glass-bg-blob blob-3"></div>

    <AdminSidebar />

    <main class="main-content">
      <header class="glass-topbar">
        <div class="topbar-left">
          <span class="greeting">Gestione Operativa</span>
          <h1>Amministratori</h1>
        </div>
        <div class="topbar-right">
          <div class="user-profile">
            <div class="avatar">{{ nomeUtente.charAt(0) }}</div>
            <span>{{ nomeUtente }}</span>
          </div>
          <button @click="apriModaleAggiungi" class="btn-primary-glass">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"></path></svg>
            Nuovo Admin
          </button>
        </div>
      </header>

      <div class="content-area">
        <div class="glass-card table-card">
          <div class="table-responsive">
            <table class="glass-table">
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
              <tr v-for="dipendente in utenti" :key="dipendente.id">
                <td class="id-cell">#{{ dipendente.id }}</td>
                <td class="desc-cell">{{ dipendente.nome }} {{ dipendente.cognome }}</td>
                <td>{{ dipendente.data_nascita ? new Date(dipendente.data_nascita).toLocaleDateString('it-IT') : '-' }}</td>
                <td style="color:#64748b">{{ dipendente.email }}</td>
                <td>
                  <span class="glass-badge badge-admin">
                    {{ dipendente.ruolo ? dipendente.ruolo : 'Nessuno' }}
                  </span>
                </td>
                <td class="actions-col">
                  <button @click="apriModaleModifica(dipendente)" class="btn-icon-glass edit" title="Modifica">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"></path></svg>
                  </button>
                  <button @click="eliminaDipendente(dipendente.id)" class="btn-icon-glass delete" title="Elimina">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                  </button>
                </td>
              </tr>
              <tr v-if="utenti.length === 0">
                <td colspan="6">
                  <div class="empty-state-glass">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>
                    <p>Nessun amministratore nel sistema.</p>
                  </div>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </main>

    <div v-if="mostraModale" class="modal-overlay">
      <div class="glass-modal">
        <div class="modal-header">
          <h2>{{ isModifica ? 'Modifica Anagrafica' : 'Registra Admin' }}</h2>
          <button @click="chiudiModale" class="btn-close-glass">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
          </button>
        </div>

        <div class="form-body">
          <div class="form-row">
            <div class="form-group half">
              <label>Nome</label>
              <input v-model="form.nome" type="text" class="glass-input" placeholder="Mario" />
            </div>
            <div class="form-group half">
              <label>Cognome</label>
              <input v-model="form.cognome" type="text" class="glass-input" placeholder="Rossi" />
            </div>
          </div>

          <div class="form-group">
            <label>Data di Nascita</label>
            <input v-model="form.data_nascita" type="date" class="glass-input" />
          </div>

          <div class="form-group">
            <label>Email aziendale</label>
            <input v-model="form.email" type="email" class="glass-input" placeholder="admin@wms.it" />
          </div>

          <template v-if="!isModifica">
            <div class="form-row">
              <div class="form-group half">
                <label>Password</label>
                <input v-model="form.password" type="password" class="glass-input" placeholder="••••••••" />
              </div>
              <div class="form-group half">
                <label>Conferma Password</label>
                <input v-model="form.confermaPassword" type="password" class="glass-input" placeholder="••••••••" />
              </div>
            </div>
            <div class="form-group">
              <label>Ruolo di sistema assegnato</label>
              <div class="role-badge fixed-role-admin">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"></path></svg>
                <span>Amministratore (Admin)</span>
              </div>
            </div>
          </template>

          <template v-if="isModifica">
            <div class="security-box is-unlocked">
              <div class="unlocked-state">
                <div class="security-info success">
                  <svg class="icon-unlock" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 11V7a4 4 0 118 0m-4 8v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2z"></path></svg>
                  <label>Imposta nuova password (Opzionale)</label>
                </div>
                <input v-model="form.password" type="password" class="glass-input-success" placeholder="Lascia vuoto per non cambiare" />
              </div>
            </div>
          </template>

          <div v-if="errorMessage" class="error-banner">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
            {{ errorMessage }}
          </div>
        </div>

        <div class="modal-actions">
          <button @click="chiudiModale" class="btn-ghost">Annulla</button>
          <button @click="salvaDipendente" class="btn-success-glass">Salva Admin</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');

/* ------- LAYOUT & BACKGROUND CHIARO (Uniformato) ------- */
.glass-dashboard-layout { display: flex; height: 100vh; width: 100vw; background-color: #e2e8f0; font-family: 'Inter', sans-serif; margin: 0; color: #1e293b; position: relative; overflow: hidden; }
.glass-bg-blob { position: absolute; border-radius: 50%; filter: blur(80px); z-index: 0; opacity: 0.6; }
.blob-1 { top: -10%; left: -10%; width: 500px; height: 500px; background: #93c5fd; }
.blob-2 { bottom: -20%; right: -10%; width: 600px; height: 600px; background: #c4b5fd; }
.blob-3 { top: 40%; left: 40%; width: 400px; height: 400px; background: #86efac; opacity: 0.4; }

.main-content { flex-grow: 1; display: flex; flex-direction: column; overflow-y: auto; z-index: 10; }

/* ------- TOPBAR CHIARA ------- */
.glass-topbar { background: rgba(255, 255, 255, 0.4); backdrop-filter: blur(10px); -webkit-backdrop-filter: blur(10px); padding: 20px 40px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid rgba(255, 255, 255, 0.3); }
.topbar-right { display: flex; align-items: center; gap: 24px; }
.greeting { font-size: 14px; color: #64748b; font-weight: 500; }
.topbar-left h1 { margin: 4px 0 0 0; font-size: 24px; font-weight: 700; color: #0f172a; letter-spacing: -0.5px; }

.user-profile { display: flex; align-items: center; gap: 12px; font-weight: 600; color: #0f172a; }
.avatar { width: 44px; height: 44px; background: linear-gradient(135deg, #6366f1, #a855f7); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-weight: 600; font-size: 18px; box-shadow: 0 4px 10px rgba(99, 102, 241, 0.3); }

.btn-primary-glass { background: linear-gradient(135deg, #6366f1, #8b5cf6); color: white; border: none; padding: 10px 20px; border-radius: 12px; cursor: pointer; font-weight: 600; font-size: 14px; display: flex; align-items: center; gap: 8px; transition: all 0.3s; box-shadow: 0 4px 15px rgba(99, 102, 241, 0.3); }
.btn-primary-glass svg { width: 18px; height: 18px; }
.btn-primary-glass:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(99, 102, 241, 0.4); }

.content-area { padding: 40px; max-width: 1200px; margin: 0 auto; width: 100%; box-sizing: border-box;}

/* ------- IL VETRO CHIARO (CARD DELLA TABELLA) ------- */
.glass-card { background: rgba(255, 255, 255, 0.65); backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px); border: 1px solid rgba(255, 255, 255, 0.5); box-shadow: 0 10px 30px -10px rgba(0, 0, 0, 0.1); border-radius: 20px; }
.table-card { overflow: hidden; padding: 0; }

/* Tabella Glass */
.table-responsive { width: 100%; overflow-x: auto; }
.glass-table { width: 100%; border-collapse: collapse; text-align: left; }
.glass-table th { padding: 16px 24px; font-size: 13px; font-weight: 600; color: #64748b; text-transform: uppercase; letter-spacing: 0.5px; border-bottom: 1px solid rgba(0,0,0,0.05); }
.glass-table td { padding: 16px 24px; border-bottom: 1px solid rgba(0,0,0,0.03); color: #334155; font-size: 14px; vertical-align: middle; }
.glass-table tbody tr:hover { background: rgba(255,255,255,0.4); }
.glass-table tbody tr:last-child td { border-bottom: none; }

.id-cell { color: #64748b; font-family: monospace; font-weight: 600; }
.desc-cell { color: #0f172a; font-weight: 600; }

.actions-col { text-align: right; }
.btn-icon-glass { background: rgba(255,255,255,0.5); border: 1px solid rgba(255,255,255,0.6); color: #64748b; cursor: pointer; width: 32px; height: 32px; border-radius: 8px; display: inline-flex; align-items: center; justify-content: center; margin-left: 8px; transition: all 0.2s; }
.btn-icon-glass svg { width: 16px; height: 16px; }
.btn-icon-glass:hover { background: rgba(255,255,255,0.9); color: #0f172a; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }
.btn-icon-glass.delete:hover { color: #ef4444; border-color: rgba(239, 68, 68, 0.3); background-color: rgba(239, 68, 68, 0.1); }

/* Badges */
.glass-badge { padding: 6px 12px; border-radius: 12px; font-size: 12px; font-weight: 600; border: 1px solid rgba(255,255,255,0.5); backdrop-filter: blur(4px); }
.badge-admin { background-color: rgba(16, 185, 129, 0.15); color: #059669; border-color: rgba(16, 185, 129, 0.3); }

.empty-state-glass { padding: 60px 0; color: #64748b; text-align: center; display: flex; flex-direction: column; align-items: center; }
.empty-state-glass svg { width: 48px; height: 48px; margin-bottom: 16px; opacity: 0.5; }
.empty-state-glass p { margin: 0; font-size: 15px; }

/* ------- MODALE GLASS CHIARA ------- */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(15,23,42,0.4); backdrop-filter: blur(8px); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.glass-modal { background: rgba(255, 255, 255, 0.85); backdrop-filter: blur(24px); border: 1px solid rgba(255,255,255,0.8); border-radius: 20px; width: 100%; max-width: 480px; box-shadow: 0 25px 50px -12px rgba(0,0,0,0.2); display: flex; flex-direction: column; max-height: 90vh; }
.modal-header { padding: 24px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid rgba(0,0,0,0.05); }
.modal-header h2 { margin: 0; font-size: 18px; font-weight: 700; color: #0f172a; }
.btn-close-glass { background: transparent; border: none; color: #64748b; cursor: pointer; padding: 6px; border-radius: 8px; transition: 0.2s; }
.btn-close-glass:hover { background: rgba(0,0,0,0.05); color: #ef4444; }

.form-body { padding: 24px; overflow-y: auto; }
.form-row { display: flex; gap: 16px; }
.form-group { margin-bottom: 20px; width: 100%; }
.form-group.half { width: 50%; }
.form-group label { display: block; font-weight: 600; margin-bottom: 8px; color: #475569; font-size: 13px; }

/* Modificatori Input per Sicurezza */
.glass-input { width: 100%; padding: 12px 16px; border-radius: 12px; background: rgba(255, 255, 255, 0.6); border: 1px solid rgba(255,255,255,0.4); color: #0f172a; font-size: 13px; outline: none; box-sizing: border-box; transition: all 0.2s; }
.glass-input::placeholder { color: #94a3b8; }
.glass-input:focus { border-color: #6366f1; background: rgba(255, 255, 255, 0.9); box-shadow: 0 0 0 3px rgba(99,102,241,0.15); }

.glass-input-warning { width: 100%; padding: 12px 16px; border-radius: 12px; background: rgba(254, 243, 199, 0.5); border: 1px solid rgba(245, 158, 11, 0.5); color: #0f172a; font-size: 13px; outline: none; box-sizing: border-box; margin-bottom: 12px;}
.glass-input-warning:focus { background: white; border-color: #f59e0b; box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.15); }
.glass-input-success { width: 100%; padding: 12px 16px; border-radius: 12px; background: rgba(209, 250, 229, 0.5); border: 1px solid rgba(16, 185, 129, 0.5); color: #0f172a; font-size: 13px; outline: none; box-sizing: border-box; }
.glass-input-success:focus { background: white; border-color: #10b981; box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.15); }

/* Box Sicurezza */
.security-box { background: rgba(255,255,255,0.4); border: 1px solid rgba(0,0,0,0.05); border-radius: 16px; padding: 20px; margin-top: 10px; transition: all 0.3s ease; }
.security-box.is-unlocked { background: rgba(16, 185, 129, 0.05); border-color: rgba(16, 185, 129, 0.2); }
.security-box.is-verifying { background: rgba(245, 158, 11, 0.05); border-color: rgba(245, 158, 11, 0.2); }

.locked-state { display: flex; justify-content: space-between; align-items: center; }
.security-info { display: flex; align-items: center; gap: 12px; }
.icon-lock { width: 24px; height: 24px; color: #64748b; }
.security-info h4 { margin: 0 0 4px 0; font-size: 14px; color: #0f172a; }
.security-info p { margin: 0; font-size: 12px; color: #64748b; }

.btn-unlock-glass { background: rgba(255,255,255,0.6); color: #0f172a; border: 1px solid rgba(0,0,0,0.1); padding: 8px 16px; border-radius: 8px; cursor: pointer; font-size: 13px; font-weight: 600; transition: all 0.2s; }
.btn-unlock-glass:hover { background: white; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }

.verify-state label { display: block; font-size: 13px; font-weight: 600; color: #b45309; margin-bottom: 12px; }
.verify-actions { display: flex; gap: 12px; justify-content: flex-end; }
.btn-warning-glass { background: linear-gradient(135deg, #f59e0b, #d97706); color: white; border: none; padding: 8px 16px; border-radius: 8px; font-weight: 600; font-size: 13px; cursor: pointer; box-shadow: 0 2px 8px rgba(245, 158, 11, 0.3);}

.unlocked-state { display: flex; flex-direction: column; gap: 12px; }
.security-info.success .icon-unlock { width: 20px; height: 20px; color: #059669; }
.unlocked-state label { font-size: 13px; font-weight: 600; color: #059669; }

.error-banner { margin-top: 16px; padding: 12px 16px; background: rgba(239,68,68,0.1); border-left: 4px solid #ef4444; border-radius: 6px; color: #b91c1c; font-size: 13px; font-weight: 500; display: flex; align-items: center; gap: 8px;}
.error-banner svg { width: 20px; height: 20px; flex-shrink: 0;}

.modal-actions { padding: 20px 24px; border-top: 1px solid rgba(0,0,0,0.05); background: rgba(248, 250, 252, 0.6); display: flex; justify-content: flex-end; gap: 12px; border-radius: 0 0 20px 20px; }
.btn-ghost { background: transparent; color: #64748b; border: 1px solid rgba(0,0,0,0.1); padding: 10px 20px; border-radius: 12px; cursor: pointer; font-weight: 600; font-size: 14px; transition: 0.2s;}
.btn-ghost:hover { background: rgba(0,0,0,0.05); color: #0f172a;}
.btn-success-glass { background: linear-gradient(135deg, #10b981, #059669); color: white; border: none; padding: 10px 24px; border-radius: 12px; cursor: pointer; font-weight: 600; font-size: 14px; transition: 0.3s; box-shadow: 0 4px 15px rgba(16,185,129,0.3);}
.btn-success-glass:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(16,185,129,0.4); }
.btn-success-glass:disabled { opacity: 0.5; cursor: not-allowed; box-shadow: none;}

.role-badge { display: flex; align-items: center; gap: 10px; padding: 12px 16px; border-radius: 12px; font-weight: 600; font-size: 14px; }
.role-badge svg { width: 20px; height: 20px; }
.fixed-role-admin { background-color: rgba(16, 185, 129, 0.1); color: #059669; border: 1px solid rgba(16, 185, 129, 0.2); }
</style>