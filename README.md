PROGETTO INGEGNERIA DEL SOFTWARE AVANZATA
Smart Warehouse Management System
1. Descrizione e Obiettivo del Progetto 
Il progetto consiste nello sviluppo di un WMS (Warehouse Management System) web-based, focalizzato sull'ottimizzazione spaziale e logistica di merci soggette a vincoli stringenti (es. catena del freddo, merci pericolose, gestione rigorosa delle scadenze).
2. Stack Tecnologico e Architettura 
Il sistema sarà implementato con un approccio che favorisce una moderna architettura disaccoppiata:
Backend (REST API): Sviluppato in Java utilizzando il framework Spring Boot. Il backend esporrà esclusivamente endpoint REST, occupandosi di tutta la complessa logica di business e di dominio.
Database e Persistenza: Utilizzo di PostgreSQL come database relazionale, con interazione ai dati garantita tramite JDBC.
Frontend: Interfaccia reattiva e client-side sviluppata in Vue.js, HTML e CSS. Il frontend comunicherà con il backend consumando le API in formato JSON, garantendo un'esperienza utente dinamica (es. drag&drop per gli spostamenti in mappa e aggiornamento in tempo reale dei task).
3. Complessità del Sistema e Logica di Dominio 
La complessità dell'applicativo non risiede nelle semplici operazioni CRUD, ma nell'implementazione di un motore logistico avanzato:
Allocazione Intelligente (Inbound): Algoritmo per suggerire all'operatore la collocazione ottimale della merce, calcolando l'occupazione volumetrica degli scaffali ("riempimento dei buchi") e rispettando i vincoli di zona (es. compatibilità delle etichette prodotto con le caratteristiche del reparto).
Prelievo Ottimizzato (Outbound - FEFO): Generazione automatica di percorsi e task di prelievo basati sulla logica First Expired, First Out, minimizzando gli sprechi della merce in scadenza.
Gestione della Concorrenza: Implementazione di lock transazionali sul database per gestire in totale sicurezza prelievi o allocazioni simultanee da parte di più operatori sugli stessi slot fisici (evitando condizioni di race e inconsistenze).
4. Testing e Qualità del Software 
La robustezza della logica di business sarà garantita da una suite di test rigorosa:
Unit Testing e Property-Based Testing (PBT): Utilizzati per validare gli invarianti di dominio (es. garantendo matematicamente che "nessuna merce deperibile possa essere allocata in reparti con temperatura > 0°C").
Integration Testing: Per verificare la corretta interazione con il database e il funzionamento dei lock transazionali (tramite testcontainers o DB in memoria).
5. CI/CD e Deployment 
Il ciclo di vita del software sarà gestito tramite repository Git, collegato a una pipeline di Continuous Integration e Continuous Deployment (CI/CD). La pipeline sarà configurata per:
Eseguire automaticamente la suite di test ad ogni push.
Pacchettizzare l'applicazione (generazione del file eseguibile .jar  tramite Maven/Spring Boot).
Preparare il deployment finale all'interno di un container Docker isolato





Task, funzionalità e feature: (per il file readme)
Gestione Accessi e Utenze
Login differenziato: accesso al sistema basato sul ruolo (Admin e Dipendente)
Gestione utenze: creazione, modifica e disabilitazione degli account dei dipendenti da parte degli admin. 

Funzionalità Admin: 
Gestione topografica: creazione e configurazione fisica del magazzino (corsie, scaffali, celle) con vincoli di peso e volume.
Dashboard inventario: visualizzazione completa delle merci stoccate, filtrabile per categoria e ricercabili (barra di ricerca). 
Inserimento intelligente: allocazione automatica della merce in ingresso calcolata in base a tag (es. “Surgelato”) e capacità residua per ciascun reparto. 
Gestione anagrafica dei batch: modifica dei dettagli o cancellazione manuale/logica delle merci a sistema (CRUD). 
Generazione prelievi: creazioni di liste di prelievo automatiche per far uscire prima la merce con scadenza più imminente. 
Assegnazione task: creazione e smistamento manuale o automatico delle missioni di magazzino ai dipendenti. 
Monitoraggio turni: pannello per vedere in tempo reale quali dipendenti sono presenti, attivi o in pausa. 
Storico movimenti: registro mutabile di chi ha fatto cosa e quanto (es. completamento task, spostamenti merce).

Funzionalità Dipendente: 
Dashboard operativa: visualizzazione della lista dei task giornalieri, ordinata automaticamente per priorità o vicinanza. 
Avanzamento task: possibilità di aggiornare lo stato di una missione (es. “iniziato”, “completato”, “anomalia/merce guasta”). 
Gestione stato lavorativo: aggiornamento in tempo reale della propria disponibilità (“attivo”, “in pausa”, “fine turno”). 

Funzionalità di sistema: 
Lock transazionale: gestione della concorrenza sul database per impedire che due operatori prelevino la stessa quantità limitata di merce
Notifiche di scadenza: sistema automatico che avvisa l’admin quanto un lotto di avvicina alla data di scadenza (basandosi sulla data odierna). 
Validazione invarianti fisici: blocco rigoroso di qualsiasi operazione che superi la portata massima (peso/volume) di uno scaffale o violi le incompatibilità di zona. 










