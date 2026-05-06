package com.ProgettoISA.WMS.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ProgettoISA.WMS.DTO.ProdottiDTO;
import com.ProgettoISA.WMS.Service.ProdottiService;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/prodotti")
public class ProdottiController {

    private final ProdottiService prodottiService;
    public ProdottiController(ProdottiService prodottiService) {
        this.prodottiService = prodottiService;
    }

    @GetMapping("/carica")
    public ResponseEntity<List<ProdottiDTO>> getProdotti() {
        try {
            List<ProdottiDTO> prodotti = prodottiService.getTuttiIProdotti();
            return ResponseEntity.ok(prodotti);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
