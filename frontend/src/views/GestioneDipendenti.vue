<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

// Stato della pagina
const dipendenti = ref([])
const mostraModale = ref(false)
const isModifica = ref(false) // Ci dice se stiamo creando o modificando
const utenteSelezionatoId = ref(null)
const errorMessage = ref('')

// Dati del form
const form = ref({
  nome: '',
  cognome: '',
  email: '',
  password: '',
  ruolo: 'Dipendente'
})

// ==========================================
// 1. CARICA LA LISTA (READ)
// ==========================================
const caricaDipendenti = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/auth/utenti') //chiamata per leggere tutti gli utenti
    dipendenti.value = response.data
  } catch (error) {
    console.error("Errore nel caricamento dei dipendenti:", error)
  }
}

// Eseguiamo il caricamento appena la pagina si apre
onMounted(() => { //con onMounted carichiamo la lista dei dipendenti al caricamento della pagina
  caricaDipendenti()
})

// ==========================================
// 2. GESTIONE DEL MODAL (Form)
// ==========================================
const apriModaleAggiungi = () => {
  isModifica.value = false
  errorMessage.value = ''
  form.value = { nome: '', cognome: '', email: '', password: '', ruolo: 'Dipendente' }
  mostraModale.value = true
}

const apriModaleModifica = (dipendente) => {
  isModifica.value = true
  utenteSelezionatoId.value = dipendente.id
  errorMessage.value = ''

  // Riempiamo il form con i dati attuali
  form.value = {
    nome: dipendente.nome,
    cognome: dipendente.cognome,
    email: dipendente.email,
    password: '', // La lasciamo vuota per sicurezza
    ruolo: dipendente.ruolo ? dipendente.ruolo.nomeRuolo : 'Dipendente'
  }
  mostraModale.value = true
}

const chiudiModale = () => {
  mostraModale.value = false
}

// ==========================================
// 3. SALVA O MODIFICA (CREATE / UPDATE)
// ==========================================
const salvaDipendente = async () => {
  errorMessage.value = ''

  try {
    if (isModifica.value) {
      // CHIAMATA PUT PER MODIFICARE
      await axios.put(`http://localhost:8080/api/auth/modifica/${utenteSelezionatoId.value}`, { //con questa chiamata vado a modificare l'utente selezionato
        nome: form.value.nome,
        cognome: form.value.cognome,
        email: form.value.email,
        password: form.value.password // Se è vuota, il backend manterrà quella vecchia
      })
    } else {
      // CHIAMATA POST PER CREARE
      await axios.post(`http://localhost:8080/api/auth/registrati?nomeRuolo=${form.value.ruolo}`, {
        nome: form.value.nome,
        cognome: form.value.cognome,
        email: form.value.email,
        password: form.value.password
      })
    }

    chiudiModale()
    caricaDipendenti() // Aggiorniamo la tabella istantaneamente!

  } catch (error) {
    if (error.response && error.response.data) {
      errorMessage.value = error.response.data
    } else {
      errorMessage.value = "Errore di connessione al server."
    }
  }
}

// ==========================================
// 4. ELIMINA (DELETE)
// ==========================================
const eliminaDipendente = async (id) => {
  // Chiediamo conferma prima di cancellare
  if (confirm("Sei sicuro di voler eliminare questo dipendente? L'azione è irreversibile.")) {
    try {
      await axios.delete(`http://localhost:8080/api/auth/elimina/${id}`)
      caricaDipendenti() // Aggiorniamo la tabella
    } catch (error) {
      alert("Errore durante l'eliminazione.")
    }
  }
}

const tornaAllaHome = () => {
  router.push('/dashboard')
}
</script>

<template>
  <div class="page-layout">
    <header class="topbar">
      <div class="header-left">
        <button @click="tornaAllaHome" class="btn-back">⬅️ Torna alla Home</button>
        <h1>👥 Gestione Dipendenti</h1>
      </div>
      <button @click="apriModaleAggiungi" class="btn-primary">+ Nuovo Dipendente</button>
    </header>

    <main class="content-area">
      <div class="card">
        <table class="data-table">
          <thead>
          <tr>
            <th>ID</th>
            <th>Nome</th>
            <th>Cognome</th>
            <th>Email</th>
            <th>Ruolo</th>
            <th>Azioni</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="dipendente in dipendenti" :key="dipendente.id">
            <td>{{ dipendente.id }}</td>
            <td>{{ dipendente.nome }}</td>
            <td>{{ dipendente.cognome }}</td>
            <td>{{ dipendente.email }}</td>
            <td><span class="badge">{{ dipendente.ruolo ? dipendente.ruolo.nomeRuolo : 'Nessuno' }}</span></td>
            <td>
              <button @click="apriModaleModifica(dipendente)" class="btn-icon edit" title="Modifica">✏️</button>
              <button @click="eliminaDipendente(dipendente.id)" class="btn-icon delete" title="Elimina">🗑️</button>
            </td>
          </tr>
          <tr v-if="dipendenti.length === 0">
            <td colspan="6" class="empty-state">Nessun utente nel sistema.</td>
          </tr>
          </tbody>
        </table>
      </div>
    </main>

    <div v-if="mostraModale" class="modal-overlay">
      <div class="modal-content">
        <h2>{{ isModifica ? 'Modifica Dipendente' : 'Nuovo Dipendente' }}</h2>

        <div class="form-group">
          <label>Nome</label>
          <input v-model="form.nome" type="text" placeholder="Es. Mario" />
        </div>

        <div class="form-group">
          <label>Cognome</label>
          <input v-model="form.cognome" type="text" placeholder="Es. Rossi" />
        </div>

        <div class="form-group">
          <label>Email</label>
          <input v-model="form.email" type="email" placeholder="mario.rossi@wms.it" />
        </div>

        <div class="form-group">
          <label>Password {{ isModifica ? '(Lascia vuota per non cambiarla)' : '' }}</label>
          <input v-model="form.password" type="password" placeholder="***" />
        </div>

        <div class="form-group" v-if="!isModifica">
          <label>Ruolo</label>
          <select v-model="form.ruolo">
            <option value="Dipendente">Dipendente</option>
            <option value="Admin">Admin</option>
          </select>
        </div>

        <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>

        <div class="modal-actions">
          <button @click="chiudiModale" class="btn-secondary">Annulla</button>
          <button @click="salvaDipendente" class="btn-success">Salva</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ------- LAYOUT (come prima) ------- */
.page-layout { height: 100vh; background-color: #f4f6f9; display: flex; flex-direction: column; margin: 0; font-family: sans-serif; }
.topbar { background: white; padding: 20px 30px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }
.header-left { display: flex; align-items: center; gap: 20px; }
.topbar h1 { margin: 0; font-size: 24px; color: #2c3e50; }
.content-area { padding: 30px; flex-grow: 1; overflow-y: auto; }
.card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }

/* ------- BOTTONI ------- */
.btn-back { background: none; border: none; font-size: 16px; cursor: pointer; color: #7f8c8d; }
.btn-back:hover { color: #2c3e50; }
.btn-primary { background-color: #3498db; color: white; border: none; padding: 10px 20px; border-radius: 4px; cursor: pointer; font-weight: bold; }
.btn-primary:hover { background-color: #2980b9; }
.btn-icon { background: none; border: none; cursor: pointer; font-size: 18px; margin: 0 5px; transition: transform 0.2s; }
.btn-icon:hover { transform: scale(1.2); }
.btn-icon.delete:hover { color: red; }

/* ------- TABELLA ------- */
.data-table { width: 100%; border-collapse: collapse; margin-top: 10px; }
.data-table th, .data-table td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #ddd; }
.data-table th { background-color: #f8f9fa; color: #2c3e50; font-weight: bold; }
.data-table tr:hover { background-color: #f1f2f6; }
.badge { background-color: #34495e; padding: 5px 10px; border-radius: 12px; font-size: 12px; font-weight: bold; color: white; }
.empty-state { text-align: center; color: #7f8c8d; font-style: italic; padding: 20px !important; }

/* ------- MODAL PER REGISTRAZIONE/MODIFICA DIPENDENTE ------- */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex; justify-content: center; align-items: center;
  z-index: 1000;
}
.modal-content {
  background: white; padding: 30px; border-radius: 8px; width: 400px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.3);
}
.modal-content h2 { margin-top: 0; color: #2c3e50; border-bottom: 2px solid #ecf0f1; padding-bottom: 10px; }
.form-group { margin-bottom: 15px; text-align: left; }
.form-group label { display: block; font-weight: bold; margin-bottom: 5px; color: #34495e; font-size: 14px; }
.form-group input, .form-group select {
  width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box;
}
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; }
.btn-secondary { background: #95a5a6; color: white; border: none; padding: 10px 15px; border-radius: 4px; cursor: pointer; }
.btn-secondary:hover { background: #7f8c8d; }
.btn-success { background: #27ae60; color: white; border: none; padding: 10px 15px; border-radius: 4px; cursor: pointer; font-weight: bold; }
.btn-success:hover { background: #2ecc71; }
.error-text { color: #e74c3c; font-size: 14px; font-weight: bold; margin-top: 10px; }
</style>