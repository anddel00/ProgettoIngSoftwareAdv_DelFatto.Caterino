<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()
const email = ref('')
const password = ref('')
const errorMessage = ref('')
const isLoggingIn = ref(false)

const faiLogin = async () => {
  if (!email.value || !password.value) {
    errorMessage.value = 'Inserisci email e password.';
    return;
  }

  errorMessage.value = ''
  isLoggingIn.value = true

  // 1. PULIZIA TOTALE: Dimentichiamo i vecchi utenti prima di accedere
  localStorage.clear()

  try {
    const response = await api.post('/api/auth/login', {
      email: email.value,
      password: password.value
    });

    const token = response.data.token;
    const utenteLoggato = response.data.utente;
    const ruoloUtente = utenteLoggato.ruolo;

    sessionStorage.clear();
    sessionStorage.setItem('token', token);
    sessionStorage.setItem('nomeUtente', utenteLoggato.nome + ' ' + utenteLoggato.cognome);
    sessionStorage.setItem('emailUtente', utenteLoggato.email);
    sessionStorage.setItem('ruolo', ruoloUtente);

    // 2. CONTROLLO RUOLO PIÙ ROBUSTO
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
  } finally {
    isLoggingIn.value = false;
  }
}
</script>

<template>
  <div class="glass-login-layout">
    <div class="glass-bg-blob blob-1"></div>
    <div class="glass-bg-blob blob-2"></div>
    <div class="glass-bg-blob blob-3"></div>

    <div class="login-wrapper">
      <div class="glass-login-box">

        <div class="login-header">
          <div class="logo-wrapper">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"></path>
            </svg>
          </div>
          <h1>WMS System</h1>
          <p>Accesso Piattaforma Logistica</p>
        </div>

        <div class="login-form">
          <div class="input-group">
            <label>Indirizzo Email</label>
            <div class="input-with-icon">
              <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>
              </svg>
              <input
                  v-model="email"
                  type="email"
                  placeholder="es. admin@wms.it"
                  class="glass-input"
                  @keyup.enter="faiLogin"
              />
            </div>
          </div>

          <div class="input-group">
            <label>Password</label>
            <div class="input-with-icon">
              <svg class="input-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
              </svg>
              <input
                  v-model="password"
                  type="password"
                  placeholder="••••••••"
                  class="glass-input"
                  @keyup.enter="faiLogin"
              />
            </div>
          </div>

          <div v-if="errorMessage" class="error-banner">
            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
            </svg>
            {{ errorMessage }}
          </div>

          <button @click="faiLogin" class="btn-primary-glass" :disabled="isLoggingIn">
            <span v-if="!isLoggingIn">Entra nel Sistema</span>
            <span v-else class="loading-state">
              <svg class="spinner" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
              </svg>
              Autenticazione...
            </span>
          </button>
        </div>

      </div>

      <div class="footer-text">
        <p>&copy; 2026 Progetto ISA - WMS Enterprise. Tutti i diritti riservati.</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');

/* ------- LAYOUT & BACKGROUND ANIMATO ------- */
.glass-login-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  background-color: #e2e8f0;
  font-family: 'Inter', sans-serif;
  margin: 0;
  color: #1e293b;
  position: relative;
  overflow: hidden;
  align-items: center;
  justify-content: center;
}

/* BLOB FLUTTUANTI ANIMATI */
.glass-bg-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  z-index: 0;
  opacity: 0.6;
  animation: float 10s infinite ease-in-out alternate;
}

.blob-1 { top: -10%; left: -10%; width: 50vw; height: 50vw; background: #93c5fd; animation-delay: 0s; }
.blob-2 { bottom: -20%; right: -10%; width: 60vw; height: 60vw; background: #c4b5fd; animation-delay: -3s; }
.blob-3 { top: 30%; left: 40%; width: 40vw; height: 40vw; background: #86efac; opacity: 0.4; animation-delay: -6s; }

@keyframes float {
  0% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(5%, 5%) scale(1.05); }
  100% { transform: translate(-2%, -2%) scale(0.95); }
}

/* ------- CONTENITORE LOGIN ------- */
.login-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 440px;
  padding: 20px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.glass-login-box {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.15);
  border-radius: 24px;
  padding: 40px;
  width: 100%;
  box-sizing: border-box;
}

/* ------- HEADER LOGIN ------- */
.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-wrapper {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px auto;
  box-shadow: 0 10px 25px rgba(99, 102, 241, 0.4);
  color: white;
}

.logo-wrapper svg {
  width: 32px;
  height: 32px;
}

.login-header h1 {
  margin: 0 0 8px 0;
  font-size: 26px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.5px;
}

.login-header p {
  margin: 0;
  color: #64748b;
  font-size: 15px;
  font-weight: 500;
}

/* ------- FORM ------- */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-group label {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  margin-left: 4px;
}

.input-with-icon {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 14px;
  width: 20px;
  height: 20px;
  color: #94a3b8;
  pointer-events: none;
  transition: color 0.2s;
}

.glass-input {
  width: 100%;
  padding: 14px 14px 14px 44px; /* Padding a sinistra per far spazio all'icona */
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.6);
  color: #0f172a;
  font-size: 15px;
  font-family: 'Inter', sans-serif;
  outline: none;
  box-sizing: border-box;
  transition: all 0.3s ease;
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.02);
}

.glass-input::placeholder { color: #94a3b8; }

.glass-input:focus {
  border-color: #6366f1;
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.15);
}

.glass-input:focus + .input-icon,
.input-with-icon:focus-within .input-icon {
  color: #6366f1; /* L'icona diventa blu quando l'input è a fuoco */
}

/* ------- MESSAGGI ED ERRORI ------- */
.error-banner {
  padding: 12px 16px;
  background: rgba(239, 68, 68, 0.1);
  border-left: 4px solid #ef4444;
  border-radius: 8px;
  color: #b91c1c;
  font-size: 13px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 10px;
  animation: slideIn 0.3s ease-out forwards;
}

.error-banner svg { width: 20px; height: 20px; flex-shrink: 0; }

@keyframes slideIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ------- BOTTONE LOGIN ------- */
.btn-primary-glass {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  border: none;
  padding: 14px 20px;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 600;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 10px 20px -5px rgba(99, 102, 241, 0.5);
  margin-top: 10px;
}

.btn-primary-glass:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 15px 25px -5px rgba(99, 102, 241, 0.6);
}

.btn-primary-glass:active:not(:disabled) {
  transform: translateY(1px);
  box-shadow: 0 5px 10px -5px rgba(99, 102, 241, 0.5);
}

.btn-primary-glass:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-state {
  display: flex;
  align-items: center;
  gap: 8px;
}

.spinner {
  animation: spin 1s linear infinite;
  width: 20px;
  height: 20px;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* ------- FOOTER ------- */
.footer-text {
  margin-top: 30px;
  text-align: center;
  color: #64748b;
  font-size: 12px;
  font-weight: 500;
}
</style>