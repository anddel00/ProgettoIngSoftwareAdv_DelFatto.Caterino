package com.ProgettoISA.WMS.Controller;

import com.ProgettoISA.WMS.Model.Utenti;
import com.ProgettoISA.WMS.Service.UtentiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController //trasferiamo i dati in JSON
@RequestMapping("/api/auth") //URL base per tutti gli endpoint di questo controller
@CrossOrigin(origins = "*") // FONDAMENTALE: Permette a Vue.js (porta 5173) di comunicare con Spring Boot (porta 8080) senza essere bloccato!
public class UtentiController {

    @Autowired
    private UtentiService utentiService;

    // ==========================================
    // 1. ENDPOINT: REGISTRAZIONE
    // URL: POST http://localhost:8080/api/auth/registrati?nome_ruolo=Admin
    // ==========================================
    @PostMapping("/registrati")
    public ResponseEntity<?> registraUtente(@RequestBody Utenti nuovoUtente, @RequestParam String nome_ruolo) {
        try {
            Utenti utenteSalvato = utentiService.registraUtente(nuovoUtente, nome_ruolo);
            return ResponseEntity.ok(utenteSalvato);
        } catch (IllegalArgumentException e) {
            // Se l'email esiste già o il ruolo è sbagliato, restituiamo un errore 400 (Bad Request)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==========================================
    // 2. ENDPOINT: LOGIN
    // URL: POST http://localhost:8080/api/auth/login
    // ==========================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenziali) {
        try {
            // Estraiamo email e password dal JSON inviato da Vue.js
            String email = credenziali.get("email");
            String password = credenziali.get("password");

            // Chiamiamo il Service
            Utenti utenteLoggato = utentiService.effettuaLogin(email, password);

            // Se tutto va bene, restituiamo i dati dell'utente in JSON
            return ResponseEntity.ok(utenteLoggato);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(401).body(e.getMessage()); //il 401 è un errore di autenticazione
        }
    }
}