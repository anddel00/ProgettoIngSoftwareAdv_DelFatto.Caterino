<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 1. Variabili "reattive" che si collegheranno in automatico alle caselle di testo
const email = ref('')
const password = ref('')
const errorMessage = ref('')

const faiLogin = () => {
  // Resettiamo l'errore a ogni tentativo
  errorMessage.value = ''

  // 2. IL MOCK DEL BACKEND (Qui in futuro faremo: axios.post('/api/login', { email, password }))
  if (email.value === 'admin@wms.it' && password.value === 'admin123') {

    // Simuliamo che il backend ci abbia risposto con il ruolo dell'utente
    // Salviamo questo dato nel browser così non lo perdiamo se ricarichiamo la pagina!
    localStorage.setItem('ruolo', 'Admin')
    localStorage.setItem('nomeUtente', 'Mario Rossi')

    // Spediamo l'Admin alla sua dashboard
    router.push('/dashboard')

  } else if (email.value === 'dipendente@wms.it' && password.value === 'user123') {

    localStorage.setItem('ruolo', 'Dipendente')
    localStorage.setItem('nomeUtente', 'Luigi Verdi')

    // Spediamo il dipendente alla sua vista dedicata (es. la lista dei task)
    // NOTA: Dovrete creare questa pagina e questa rotta in router.js!
    router.push('/task-operativi')

  } else {
    // 3. SE LE CREDENZIALI SONO SBAGLIATE
    errorMessage.value = 'Email o password errati. Riprova.'
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <h1>🔐 Accesso WMS</h1>
      <p>Inserisci le tue credenziali</p>

      <input v-model="email" type="text" placeholder="Email (es: admin@wms.it)" class="input-field" />
      <input v-model="password" type="password" placeholder="Password (es: admin123)" class="input-field" />

      <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>

      <button @click="faiLogin" class="btn-login">Entra nel Sistema</button>
    </div>
  </div>
</template>

<style scoped>
/* Mantieni il CSS di prima e aggiungi solo lo stile per l'errore: */
.login-container { display: flex; justify-content: center; align-items: center; height: 100vh; background-color: #2c3e50; }
.login-box { background: white; padding: 40px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.2); text-align: center; width: 300px; }
.input-field { width: 100%; padding: 10px; margin: 10px 0; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; }
.btn-login { width: 100%; padding: 10px; background-color: #27ae60; color: white; border: none; border-radius: 4px; font-size: 16px; cursor: pointer; margin-top: 15px; }
.btn-login:hover { background-color: #2ecc71; }

.error-text {
  color: #e74c3c;
  font-size: 14px;
  font-weight: bold;
  margin: 10px 0;
}
</style>