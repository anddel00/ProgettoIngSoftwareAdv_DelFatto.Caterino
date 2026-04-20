<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'
const router = useRouter()
const email = ref('')
const password = ref('')
const errorMessage = ref('')

const faiLogin = async () => {
  errorMessage.value = ''

  // 1. PULIZIA TOTALE: Dimentichiamo i vecchi utenti prima di accedere
  localStorage.clear()

  try {
    const response = await api.post('/api/auth/login', {
      email: email.value,
      password: password.value
    });

    const utenteLoggato = response.data;
    const ruoloUtente = utenteLoggato.ruolo;

    // In Login.vue, dentro faiLogin
    sessionStorage.clear(); // Usa sessionStorage invece di localStorage
    sessionStorage.setItem('nomeUtente', utenteLoggato.nome + ' ' + utenteLoggato.cognome);
    sessionStorage.setItem('emailUtente', utenteLoggato.email);
    sessionStorage.setItem('ruolo', ruoloUtente);

    // 2. CONTROLLO RUOLO PIÙ ROBUSTO (Gestisce sia "Admin" che "Amministratore")
    if (ruoloUtente === 'Admin' || ruoloUtente === 'Amministratore' || ruoloUtente === 'ADMIN') {
      router.push('/dashboard');
    } else {
      router.push('/dipendenteHome');
    }

  } catch (error) {
    if (error.response && error.response.status === 401) {
      errorMessage.value = 'Email o password errati. Riprova.';
    } else {
      errorMessage.value = 'Impossibile connettersi al server.';
    }
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <h1>🔐 Accesso WMS</h1>
      <p>Inserisci le tue credenziali</p>

      <input v-model="email" type="text" placeholder="Email (es: admin@wms.it)" class="input-field" />
      <input v-model="password" type="password" placeholder="Password (es: admin123)" class="input-field" @keyup.enter="faiLogin"/>

      <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>

      <button @click="faiLogin" class="btn-login">Entra nel Sistema</button>
    </div>
  </div>
</template>

<style scoped>

/* -------CSS------ */
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