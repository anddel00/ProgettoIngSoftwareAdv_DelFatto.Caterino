package com.ProgettoISA.WMS.Controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
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

import com.ProgettoISA.WMS.DTO.LoginRequestDTO;
import com.ProgettoISA.WMS.DTO.RegistrazioneRequestDTO;
import com.ProgettoISA.WMS.DTO.UtentiDTO;
import com.ProgettoISA.WMS.Model.Utenti;
import com.ProgettoISA.WMS.Security.JwtService;
import com.ProgettoISA.WMS.Service.UtentiService;
import jakarta.validation.Valid;

@RestController //trasferiamo i dati in JSON
@RequestMapping("/api/auth") //URL base per tutti gli endpoint di questo controller
@CrossOrigin(origins = "*") // FONDAMENTALE: Permette a Vue.js (porta 5173) di comunicare con Spring Boot (porta 8080) senza essere bloccato!
public class UtentiController {

    @Autowired
    private UtentiService utentiService;

    @Autowired
    private JwtService jwtService;

    //L'URL localhost:8080 VIENE GESTITO DAL FILE api.js

    // ==========================================
    // 1. ENDPOINT: REGISTRAZIONE (RESTful pulito)
    // URL: POST http://localhost:8080/api/auth/registrati //prima passavo il nomeRuolo nell'URL--> così è più pulito
    // ==========================================
    @PostMapping("/registrati")
    public ResponseEntity<?> registraUtente(@Valid @RequestBody RegistrazioneRequestDTO payload) {
        // La validazione fallirà in automatico grazie a @Valid se mancano campi obbligatori.
        
        Utenti nuovoUtente = new Utenti();
        nuovoUtente.setNome(payload.getNome());
        nuovoUtente.setCognome(payload.getCognome());
        nuovoUtente.setEmail(payload.getEmail());
        nuovoUtente.setPassword(payload.getPassword());
        nuovoUtente.setUsername(payload.getUsername());

        if (payload.getData_nascita() != null && !payload.getData_nascita().isEmpty()) {
            nuovoUtente.setData_nascita(java.sql.Date.valueOf(payload.getData_nascita()));
        }

        Utenti utenteSalvato = utentiService.registraUtente(nuovoUtente, payload.getRuolo());
        return ResponseEntity.ok(utenteSalvato);
    }

    // ==========================================
    // 2. LOGIN (restituisce JWT)
    // URL: POST http://localhost:8080/api/auth/login
    // ==========================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO credenziali) {
        try {
            Utenti utenteLoggato = utentiService.effettuaLogin(credenziali.getEmail(), credenziali.getPassword());

            UtentiDTO utenteDTO = new UtentiDTO(
                    utenteLoggato.getId(),
                    utenteLoggato.getUsername(),
                    utenteLoggato.getNome(),
                    utenteLoggato.getCognome(),
                    utenteLoggato.getEmail(),
                    utenteLoggato.getRuolo() != null ? utenteLoggato.getRuolo().getNomeRuolo() : "N/A",
                    utenteLoggato.getData_nascita()
            );

            // Generiamo il token JWT
            String token = jwtService.generateToken(utenteLoggato.getEmail(), utenteDTO.getRuolo());

            // Restituiamo il token insieme ai dati dell'utente
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("utente", utenteDTO);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(401).body(errorResponse);
        }
    }


    // 3. ENDPOINT: OTTIENI TUTTI GLI UTENTI
    // URL: GET http://localhost:8080/api/auth/utenti
    @GetMapping("/utenti")
    public ResponseEntity<List<UtentiDTO>> getTuttiUtenti() {
        List<Utenti> tutti = utentiService.ottieniTuttiUtenti();

        List<UtentiDTO> response = tutti.stream()
                .map(utente -> new UtentiDTO(
                        utente.getId(),
                        utente.getUsername(),
                        utente.getNome(),
                        utente.getCognome(),
                        utente.getEmail(),
                        utente.getRuolo() != null ? utente.getRuolo().getNomeRuolo() : "N/A",
                        utente.getData_nascita()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Autowired
    private TurniDipRepository turniDipRepository;

    @GetMapping("/utenti/attivi")
    public ResponseEntity<List<UtentiDTO>> getDipendentiAttivi() {
        // 1. Peschiamo i dipendenti "online" tramite la nostra query
        List<Utenti> dipendentiAttivi = turniDipRepository.findDipendentiAttualmenteInTurno();

        // 2. Li convertiamo in DTO per non esporre dati sensibili (password, ecc.)
        List<UtentiDTO> response = dipendentiAttivi.stream()
                .map(utente -> new UtentiDTO(utente.getId(),utente.getUsername(), utente.getNome(), utente.getCognome(), utente.getEmail(), utente.getRuolo().getNomeRuolo(), utente.getData_nascita()))
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