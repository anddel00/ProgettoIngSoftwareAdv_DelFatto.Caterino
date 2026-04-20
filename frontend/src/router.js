import { createRouter, createWebHistory } from 'vue-router'

// 1. Import delle views
import Login from './views/Login.vue'
import AdminDashboard from './views/AdminDashboard.vue'
import GestioneDipendenti from "./views/GestioneDipendenti.vue";
import GestioneAdmin from "./views/GestioneAdmin.vue";
import DipendenteHome from "./views/DipendenteHome.vue";
import DipendenteTask from "./views/DipendenteTask.vue";
import StoricoMovimenti from "./views/StoricoMovimenti.vue";
import GestioneTask from "./views/GestioneTask.vue";


// 2. Rotte
const routes = [
    {
        path: '/',          // Quando l'utente va sull'indirizzo base (localhost:5173/)
        name: 'Login',
        component: Login    // ...mostra il componente Login
    },
    {
        path: '/dashboard', // Quando l'utente va su localhost:5173/dashboard
        name: 'Dashboard',
        component: AdminDashboard // ...mostra la Dashboard
    },
    {
        path: '/GestioneDipendenti',
        name: 'GestioneDipendenti',
        component: GestioneDipendenti
    },
    {
        path: '/GestioneAdmin',
        name: 'GestioneAdmin',
        component: GestioneAdmin
    },
    {
        path: '/DipendenteHome',
        name: 'DipendenteHome',
        component: DipendenteHome
    },
    {
        path: '/DipendenteTask',
        name: 'DipendenteTask',
        component: DipendenteTask
    },
    {
        path: '/StoricoMovimenti',
        name: 'StoricoMovimenti',
        component: StoricoMovimenti
    },
    {
        path: '/GestioneTask',
        name: 'GestioneTask',
        component: GestioneTask
    },
    {
        path: '/GestioneTurni',
        name: 'GestioneTurni',
        component: () => import('./views/GestioneTurni.vue')
    }
]

// 3. Creiamo il router vero e proprio
const router = createRouter({
    history: createWebHistory(), // Usa la cronologia standard del browser (permette di usare i tasti "Indietro" e "Avanti")
    routes: routes
})

export default router