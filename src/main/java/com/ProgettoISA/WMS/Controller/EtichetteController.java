package com.ProgettoISA.WMS.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ProgettoISA.WMS.DTO.EtichetteDTO;
import com.ProgettoISA.WMS.Service.EtichetteService;

import jakarta.annotation.Generated;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/etichette")
public class EtichetteController {

    private final EtichetteService etichetteService;

    public EtichetteController(EtichetteService etichetteService) {
        this.etichetteService = etichetteService;
    }

    @GetMapping("/carica")
    public ResponseEntity<List<EtichetteDTO>> getEtichette() {
        try {
            List<EtichetteDTO> etichette = etichetteService.getEtichette();
            return ResponseEntity.ok(etichette);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}