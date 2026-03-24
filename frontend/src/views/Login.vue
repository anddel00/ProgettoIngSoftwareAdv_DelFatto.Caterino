<script setup>
import { useRouter } from 'vue-router'

const router = useRouter() //Consente di navigare tra le viste

const faiLogin = () => {
  // Per ora non facciamo controlli su email e password.
  // Diciamo solo al router: "Portami alla dashboard!"
  router.push('/dashboard')
}
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <h1>🔐 Accesso WMS</h1>
      <p>Inserisci le tue credenziali</p>

      <input type="text" placeholder="Email o Matricola" class="input-field" />
      <input type="password" placeholder="Password" class="input-field" />

      <button @click="faiLogin" class="btn-login">Entra nel Sistema</button>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #2c3e50;
}

.login-box {
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.2);
  text-align: center;
  width: 300px;
}

.input-field {
  width: 100%;
  padding: 10px;
  margin: 10px 0;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
}

.btn-login {
  width: 100%;
  padding: 10px;
  background-color: #27ae60;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  margin-top: 15px;
}

.btn-login:hover {
  background-color: #2ecc71;
}
</style>