<script setup>
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute() // Ci serve per capire in che pagina siamo!

const logout = () => {
  localStorage.clear()
  router.push('/')
}
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <div class="logo-icon">
        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"></path></svg>
      </div>
      <h2>WMS Admin</h2>
    </div>

    <nav class="sidebar-nav">
      <div class="nav-item" :class="{ active: route.path === '/dashboard' }" @click="router.push('/dashboard')">
        <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"></path></svg>
        Dashboard
      </div>

      <div class="nav-item">
        <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7"></path></svg>
        Mappa
      </div>

      <div class="nav-item" :class="{ active: route.path === '/GestioneAdmin' }" @click="router.push('/GestioneAdmin')">
        <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"></path></svg>
        Amministratori
      </div>

      <div class="nav-item" :class="{ active: route.path === '/GestioneDipendenti' || route.path === '/GestioneDipendenti' }" @click="router.push('/GestioneDipendenti')">
        <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>
        Dipendenti
      </div>

        <div class="nav-item" :class="{ active: route.path === '/StoricoMovimenti' || route.path === '/StoricoMovimenti' }" @click="router.push('/StoricoMovimenti')">
          <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>
          Storico Movimenti
        </div>

      <div class="nav-item">
        <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
        Turni
      </div>
    </nav>

    <div class="sidebar-footer">
      <button @click="logout" class="btn-logout">
        <svg class="nav-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path></svg>
        Disconnetti
      </button>
    </div>
  </aside>
</template>

<style scoped>

.sidebar { width: 260px; background-color: #0f172a; color: #94a3b8; display: flex; flex-direction: column; border-right: 1px solid #1e293b; }
.sidebar-header { padding: 30px 24px; display: flex; align-items: center; gap: 12px; }
.logo-icon svg { width: 28px; height: 28px; color: #6366f1; }
.sidebar-header h2 { margin: 0; font-size: 20px; font-weight: 700; color: #f8fafc; letter-spacing: -0.5px; }
.sidebar-nav { flex-grow: 1; padding: 0 16px; display: flex; flex-direction: column; gap: 8px; }
.nav-item { padding: 12px 16px; border-radius: 12px; cursor: pointer; font-size: 14px; font-weight: 500; display: flex; align-items: center; gap: 12px; transition: all 0.2s ease; }
.nav-icon { width: 20px; height: 20px; opacity: 0.7; }
.nav-item:hover { background-color: #1e293b; color: #f8fafc; }
.nav-item.active { background-color: rgba(99, 102, 241, 0.15); color: #6366f1; font-weight: 600; }
.nav-item.active .nav-icon { opacity: 1; }
.sidebar-footer { padding: 24px 16px; }
.btn-logout { width: 100%; padding: 12px; background-color: transparent; color: #94a3b8; border: 1px solid #1e293b; border-radius: 12px; cursor: pointer; font-weight: 500; display: flex; align-items: center; justify-content: center; gap: 8px; transition: all 0.2s; }
.btn-logout:hover { background-color: #ef4444; color: white; border-color: #ef4444; }
</style>