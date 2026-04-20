package com.ProgettoISA.WMS.Controller;

import com.ProgettoISA.WMS.DTO.UtentiDTO;
import com.ProgettoISA.WMS.Model.Utenti;
import com.ProgettoISA.WMS.Repository.TurniDipRepository;
import com.ProgettoISA.WMS.Service.TurniService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/turni")
@CrossOrigin(origins = "*")
public class TurniController {

    @Autowired
    private TurniService turniService;

    @Autowired
    private TurniDipRepository turniDipRepository;

    // ==========================================
    // POST /api/turni/inizia
    // Body: { "email": "..." }
    // ==========================================
    @PostMapping("/inizia")
    public ResponseEntity<?> iniziaTurno(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email mancante"));
        }
        try {
            turniService.iniziaTurno(email);
            return ResponseEntity.ok(Map.of("message", "Turno iniziato con successo!"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Errore del server: " + e.getMessage()));
        }
    }

    // ==========================================
    // POST /api/turni/termina
    // Body: { "email": "..." }
    // ==========================================
    @PostMapping("/termina")
    public ResponseEntity<?> terminaTurno(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email mancante"));
        }
        try {
            turniService.terminaTurno(email);
            return ResponseEntity.ok(Map.of("message", "Turno terminato con successo!"));
        } catch (IllegalStateException e) {
            // 409 Conflict: task attivi bloccano la fine turno
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Errore del server: " + e.getMessage()));
        }
    }

    // ==========================================
    // GET /api/turni/attivi
    // Dipendenti con turno aperto — usato dal dropdown task e dal monitoraggio
    // ==========================================
    @GetMapping("/attivi")
    public ResponseEntity<List<UtentiDTO>> getDipendentiInTurno() {
        List<Utenti> attivi = turniDipRepository.findDipendentiAttualmenteInTurno();
        List<UtentiDTO> result = attivi.stream()
                .map(u -> new UtentiDTO(
                        u.getId(),
                        u.getNome(),
                        u.getCognome(),
                        u.getEmail(),
                        u.getRuolo().getNomeRuolo()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}