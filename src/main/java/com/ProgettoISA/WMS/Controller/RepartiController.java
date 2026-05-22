package com.ProgettoISA.WMS.Controller;

import java.util.List;

import com.ProgettoISA.WMS.Model.Reparti;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ProgettoISA.WMS.DTO.RepartiDTO;
import com.ProgettoISA.WMS.Service.RepartiService;

@RestController
@RequestMapping("/api/reparti")
@CrossOrigin(origins = "*") 
public class RepartiController {
    private final RepartiService repartiService;

    public RepartiController(RepartiService repartiService) {
        this.repartiService = repartiService;
    }

@GetMapping("/carica")
public ResponseEntity<List<RepartiDTO>> getReparti() {
    try {
        List<RepartiDTO> reparti = repartiService.getTuttiIReparti();
        return ResponseEntity.ok(reparti);
    } catch (Exception e) {
        return ResponseEntity.internalServerError().build();
    }
}

    @PostMapping("/crea")
    public ResponseEntity<?> creaReparto(@RequestBody RepartiDTO dto) {
        try {
            // Check base di validazione
            if (dto.getMaxX() < 4 || dto.getMaxY() < 2) {
                return ResponseEntity.badRequest().body("Le dimensioni del reparto sono troppo piccole per generare scaffali.");
            }

            Reparti creato = repartiService.creaRepartoConPlanimetria(dto);
            return ResponseEntity.ok(creato);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Errore durante la creazione del reparto: " + e.getMessage());
        }
    }
}