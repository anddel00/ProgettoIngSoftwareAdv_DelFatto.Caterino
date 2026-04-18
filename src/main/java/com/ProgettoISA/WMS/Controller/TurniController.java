package com.ProgettoISA.WMS.Controller;

import com.ProgettoISA.WMS.Service.TurniService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/turni") // URL base pulito per i turni
@CrossOrigin(origins = "*")
public class TurniController {

    @Autowired
    private TurniService turniService;

    @PostMapping("/inizia")
    public ResponseEntity<?> iniziaTurno(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email mancante"));
            }

            turniService.iniziaTurno(email);

            return ResponseEntity.ok(Map.of("message", "Turno iniziato con successo!"));
        } catch (IllegalStateException e) {
            // Ritorna 400 Bad Request se ha già un turno attivo
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            // Ritorna 500 per errori generici
            return ResponseEntity.internalServerError().body(Map.of("message", "Errore del server: " + e.getMessage()));
        }
    }
}