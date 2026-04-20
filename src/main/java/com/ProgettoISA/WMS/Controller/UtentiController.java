package com.ProgettoISA.WMS.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ProgettoISA.WMS.Repository.TurniDipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ProgettoISA.WMS.DTO.UtentiDTO;
import com.ProgettoISA.WMS.Model.Utenti;
import com.ProgettoISA.WMS.Service.UtentiService;

@RestController //trasferiamo i dati in JSON
@RequestMapping("/api/auth") //URL base per tutti gli endpoint di questo controller
@CrossOrigin(origins = "*") // FONDAMENTALE: Permette a Vue.js (porta 5173) di comunicare con Spring Boot (porta 8080) senza essere bloccato!
public class UtentiController {

    @Autowired
    private UtentiService utentiService;

    //L'URL localhost:8080 VIENE GESTITO DAL FILE api.js

    // ==========================================
    // 1. ENDPOINT: REGISTRAZIONE (RESTful pulito)
    // URL: POST http://localhost:8080/api/auth/registrati //prima passavo il nomeRuolo nell'URL--> così è più pulito
    // ==========================================
    @PostMapping("/registrati")
    public ResponseEntity<?> registraUtente(@RequestBody Map<String, Object> payload) {
        try {

            // 2. Costruiamo l'utente con i dati restanti del JSON
            Utenti nuovoUtente = new Utenti();
            nuovoUtente.setNome((String) payload.get("nome"));
            nuovoUtente.setCognome((String) payload.get("cognome"));
            nuovoUtente.setEmail((String) payload.get("email"));
            nuovoUtente.setPassword((String) payload.get("password"));

            //parsing data di nascita
            String dataStringa = (String) payload.get("data_nascita");
            if (dataStringa != null && !dataStringa.isEmpty()) {
                nuovoUtente.setData_nascita(java.sql.Date.valueOf(dataStringa));
            }

            String nomeRuolo = (String) payload.get("ruolo"); //passata come stringa perchè non abbiamo un vero e proprio campo ruolo nel DB

            // 3. Passiamo tutto al Service
            Utenti utenteSalvato = utentiService.registraUtente(nuovoUtente, nomeRuolo);
            return ResponseEntity.ok(utenteSalvato);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Errore nel formato dei dati inviati.");
        }
    }

    // 2. LOGIN
    // URL: POST http://localhost:8080/api/auth/login

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenziali) {
        try {
            // Estraiamo email e password dal JSON inviato da Vue.js
            String email = credenziali.get("email");
            String password = credenziali.get("password");

            // Chiamiamo il Service
            Utenti utenteLoggato = utentiService.effettuaLogin(email, password);

            UtentiDTO utenteDTO = new UtentiDTO(
                    utenteLoggato.getId(),
                    utenteLoggato.getNome(),
                    utenteLoggato.getCognome(),
                    utenteLoggato.getEmail(),
                    utenteLoggato.getRuolo().getNomeRuolo() //assumendo che Ruoli abbia un campo nomeRuolo
            );
            // Se tutto va bene, restituiamo i dati dell'utente in JSON
            return ResponseEntity.ok(utenteDTO);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(401).body(e.getMessage()); //il 401 è un errore di autenticazione
        }
    }


    // 3. ENDPOINT: OTTIENI TUTTI GLI UTENTI
    // URL: GET http://localhost:8080/api/auth/utenti
    @GetMapping("/utenti")
    public ResponseEntity<List<Utenti>> getTuttiUtenti() {
        return ResponseEntity.ok(utentiService.ottieniTuttiUtenti());
    }

    @Autowired
    private TurniDipRepository turniDipRepository;

    @GetMapping("/utenti/attivi")
    public ResponseEntity<List<UtentiDTO>> getDipendentiAttivi() {
        // 1. Peschiamo i dipendenti "online" tramite la nostra query
        List<Utenti> dipendentiAttivi = turniDipRepository.findDipendentiAttualmenteInTurno();

        // 2. Li convertiamo in DTO per non esporre dati sensibili (password, ecc.)
        List<UtentiDTO> response = dipendentiAttivi.stream()
                .map(utente -> new UtentiDTO(utente.getId(), utente.getNome(), utente.getCognome(), utente.getEmail(), utente.getRuolo().getNomeRuolo()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }


    // 4. ENDPOINT: MODIFICA UTENTE
    // URL: PUT http://localhost:8080/api/auth/modifica/{id} (la chiamata PUT modifica)
    @PutMapping("/modifica/{id}")
    public ResponseEntity<?> modificaUtente(@PathVariable Long id, @RequestBody Utenti datiAggiornati) {
        try {
            Utenti utenteModificato = utentiService.modificaUtente(id, datiAggiornati);
            return ResponseEntity.ok(utenteModificato);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // 5. ENDPOINT: ELIMINA UTENTE
    // URL: DELETE http://localhost:8080/api/auth/elimina/{id} (la chiamata DELETE elimina)
    @DeleteMapping("/elimina/{id}")
    public ResponseEntity<?> eliminaUtente(@PathVariable Long id) {
        try {
            utentiService.eliminaUtente(id);
            return ResponseEntity.ok("Utente eliminato con successo.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}