import { ref } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const isConnected = ref(false)
const notificaGlobale = ref('')
const ultimoTaskRicevuto = ref(null)

// --- NUOVO: VARIABILI PER I BADGE DELLA SIDEBAR ---
const nuoviTaskAttiviBadge = ref(0) // Da usare su "Gestione Task"
const nuoviTaskCompletatiBadge = ref(0) // Da usare su "Storico Movimenti"

let stompClient = null

export function useWmsWebSocket() {

    const connectWebSocket = (email, ruolo) => {
        if (isConnected.value || !email) return
        const emailPulita = email.trim().toLowerCase()

        stompClient = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws-wms'),
            onConnect: () => {
                isConnected.value = true
                const topic = (ruolo.toUpperCase() === 'ADMIN') ? '/topic/tasks' : `/queue/tasks/${emailPulita}`;

                stompClient.subscribe(topic, (message) => {
                    const task = JSON.parse(message.body)
                    ultimoTaskRicevuto.value = task

                    if (ruolo.toUpperCase() === 'ADMIN') {
                        // --- LOGICA ADMIN: BADGE SILENZIOSI INVECE DI BANNER ---

                        // Se il task è passato in lavorazione (IN_CARICO o DA_FARE)
                        if (task.statoTask !== 'COMPLETATO') {
                            nuoviTaskAttiviBadge.value += 1
                        }
                        // Se il task è stato finito
                        else if (task.statoTask === 'COMPLETATO') {
                            nuoviTaskCompletatiBadge.value += 1
                        }
                        // (Il banner fastidioso è stato rimosso per l'Admin!)

                    } else {
                        // --- LOGICA DIPENDENTE: BANNER MANTENUTO ---
                        if (task.statoTask === 'DA_FARE') {
                            notificaGlobale.value = `🔔 NUOVO ORDINE: ${task.descrizione}`
                            setTimeout(() => { notificaGlobale.value = '' }, 6000)
                        }
                    }
                })
            }
        })
        stompClient.activate()
    }

    // Aggiungiamo anche due funzioni per "resettare" i badge quando l'Admin visita le pagine
    const azzeraBadgeAttivi = () => { nuoviTaskAttiviBadge.value = 0 }
    const azzeraBadgeCompletati = () => { nuoviTaskCompletatiBadge.value = 0 }

    const disconnectWebSocket = () => {
        if (stompClient) {
            stompClient.deactivate()
            isConnected.value = false
        }
    }

    return {
        connectWebSocket,
        disconnectWebSocket,
        notificaGlobale,
        ultimoTaskRicevuto,
        isConnected,
        nuoviTaskAttiviBadge, // Esponiamo i contatori
        nuoviTaskCompletatiBadge,
        azzeraBadgeAttivi, // Esponiamo le funzioni di reset
        azzeraBadgeCompletati
    }
}