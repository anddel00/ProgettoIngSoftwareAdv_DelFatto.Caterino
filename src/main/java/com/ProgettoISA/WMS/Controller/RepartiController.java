package com.ProgettoISA.WMS.Controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}