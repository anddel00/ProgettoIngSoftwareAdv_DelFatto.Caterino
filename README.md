#  Smart Warehouse Management System (WMS)
**Progetto di Ingegneria del Software Avanzata**

## 1. Descrizione e Visione Architetturale
Il progetto consiste nello sviluppo di un WMS (Warehouse Management System) web-based di livello Enterprise. Il sistema è stato concepito non come un semplice archivio dati passivo (CRUD), ma come un vero e proprio **motore decisionale attivo**, progettato per mappare l'infrastruttura fisica di un magazzino e orchestrare i flussi logistici interni nel rigoroso rispetto di stringenti vincoli fisici e ambientali (es. limiti di portata strutturale, compatibilità di temperatura, saturazione volumetrica).

L'obiettivo architetturale principale è dimostrare l'implementazione di pattern di sviluppo moderni, garantendo scalabilità, integrità transazionale e un'infrastruttura di Continuous Integration e Continuous Deployment (CI/CD) fully-containerized.

---

## 2. Architettura e Stack Tecnologico
Il sistema adotta una rigorosa architettura disaccoppiata e stateless, strutturata su tre layer principali:

* **Backend (REST API):** Sviluppato in **Java 17+** con **Spring Boot**. Il backend espone API RESTful sicure, incapsulando la complessa logica di dominio (algoritmi di allocazione e bilanciamento). L'integrità dei dati critici è garantita dall'uso pervasivo dell'annotazione `@Transactional` per gestire i rollback in caso di eccezioni a runtime.
* **Frontend (SPA):** Interfaccia utente reattiva sviluppata in **Vue.js**, HTML5 e CSS3. Ottimizzata per la manipolazione del DOM ad alte prestazioni e per il rendering dinamico di planimetrie topografiche complesse.
* **Database e Persistenza Cloud:** Il layer dati è affidato a **PostgreSQL**. Per testare il sistema in condizioni di latenza reale e simulare un ambiente distribuito *production-ready*, l'istanza del database è ospitata in cloud tramite **Amazon Web Services (AWS)**.
* **Sicurezza Stateless:** L'autenticazione è gestita tramite **JSON Web Token (JWT)**, garantendo un'architettura priva di sessioni server-side. Le password sono protette a riposo tramite hashing crittografico (**BCrypt**), mentre il routing lato client è sigillato tramite *Route Guards*.

---

## 3. Core Business Logic e Motore Algoritmico
Il vero valore aggiunto del software risiede nella traduzione dei vincoli fisici del mondo reale in strutture dati e algoritmi:

* **Scaffolding Dinamico (Geometria in RAM):** Generazione automatica della planimetria dei reparti. Il sistema calcola l'ingombro degli scaffali e lo spazio per i corridoi partendo dalle sole dimensioni (X, Y), validando la mappa in memoria prima di eseguire la persistenza massiva a database.
* **Smart Putaway (Inbound):** Algoritmo di allocazione che suggerisce la collocazione ottimale della merce in ingresso, calcolando la capacità residua in chilogrammi delle celle e rispettando i vincoli di zona (es. catena del freddo).
* **Orchestrazione Logistica e Task Splitting:** Algoritmo progettato per aggirare i limiti fisici della strumentazione (es. limite di portata di 500 kg di un transpallet). Se un lotto supera tale soglia, il sistema frammenta matematicamente la richiesta in sotto-task operativi eseguibili.
* **Load Balancing Dinamico:** I task generati non sono assegnati casualmente, ma smistati in tempo reale dall'algoritmo di bilanciamento interno che interroga la mappa in memoria dei dipendenti, assegnando la missione all'operatore con il minor "carico attivo" (in kg).

---

## 4. Ottimizzazioni e Sfide Architetturali
Per garantire la stabilità del sistema all'aumentare dei volumi di carico (il problema del "magazzino saturo"), sono state implementate specifiche ottimizzazioni architetturali:
* **Prevenzione Latenza / Memory Leak:** Passaggio dal rendering lineare di array ($O(n)$) all'utilizzo di **HashMap** ($O(1)$) per il caricamento posizionale istantaneo delle celle della mappa, abbattendo drasticamente il payload di rete e prevenendo timeout sulle connessioni AWS.
* **Server-Side Pagination & Filtri SQL:** Il catalogo lotti sfrutta una rigida paginazione server-side, garantendo un impiego di memoria costante indipendentemente dalle dimensioni dell'inventario.
* **Risoluzione Query N+1:** Ottimizzazione delle interrogazioni spaziali sulle allocazioni tramite elaborazione nel layer Service e incapsulamento in Data Transfer Object (DTO) mirati.

---

## 5. Testing, Code Quality e CI/CD
La robustezza del dominio è garantita da una piramide di test strutturata e integrata nella pipeline di automazione:

* **Property-Based Testing (Jqwik):** Stress test sugli invarianti fisici (es. testando il superamento matematico della portata massima degli scaffali con migliaia di input casuali estremi).
* **Unit & Integration Testing (JUnit/Mockito):** Isolamento della logica di business e verifica del corretto funzionamento dei lock transazionali a livello di database.
* **Analisi di Copertura (JaCoCo):** Monitoraggio della *Code Coverage*, con focus assoluto sul layer dei Service.
* **Pipeline CI/CD & Containerizzazione:** Repository ospitato su GitHub, accoppiato a **GitHub Actions** per la Continuous Integration (esecuzione automatica di build e test ad ogni push). L'intero ecosistema applicativo è containerizzato tramite **Docker**, garantendo l'isolamento degli ambienti e la consistenza delle dipendenze.

---

## 6. Funzionalità a Sistema (Use Cases)

### ️ Gestione Accessi e Utenze
* **Login Differenziato:** Accesso stateless al sistema basato sul ruolo (Admin / Dipendente).
* **Gestione Anagrafica Utenti:** Creazione, modifica e disabilitazione account dipendenti gestita dai pannelli direzionali.

### ️ Funzionalità Amministratore (Admin)
* **Gestione Topografica:** Creazione e configurazione fisica del magazzino (corsie, scaffali, celle) con calcolo metrico dello spazio.
* **Dashboard Inventario e Paginazione:** Visualizzazione completa delle merci stoccate tramite tabelle performanti, con filtri server-side per categoria e ricerca testuale.
* **Inserimento Intelligente (Inbound):** Routing automatizzato della merce in ingresso verso lo scaffale ottimale in base alla capacità di carico residua.
* **Gestione Anagrafica Lotti:** Completo controllo CRUD (modifica dettagli, cancellazione manuale/logica) sulle merci a sistema.
* **Assegnazione Task e Monitoraggio:** Smistamento ottimizzato delle missioni (Splitting & Load Balancing) e pannello di monitoraggio live dello stato (attivo/pausa) dei dipendenti in turno.
* **Storico Transazioni:** Registro immutabile delle operazioni (chi ha eseguito lo spostamento, quantità movimentata).

###  Funzionalità Operatore Logistico (Dipendente)
* **Dashboard Operativa (Palmare):** Interfaccia ottimizzata per visualizzare la coda dei task personali, ordinata per priorità operativa.
* **Avanzamento Task:** Possibilità di aggiornare in tempo reale lo stato delle missioni ("preso in carico", "completato", "segnalazione anomalia").
* **Gestione Stato Lavorativo:** Segnalazione della propria operatività a sistema per consentire al Load Balancer di instradare correttamente le missioni future.