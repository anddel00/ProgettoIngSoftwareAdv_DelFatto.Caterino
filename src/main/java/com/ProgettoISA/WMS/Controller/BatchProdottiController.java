package com.ProgettoISA.WMS.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ProgettoISA.WMS.DTO.BatchProdottiDTO;
import com.ProgettoISA.WMS.Service.BatchProdottiService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/batch-prodotti")
public class BatchProdottiController {
    
    private final BatchProdottiService batchProdottiService;

    public BatchProdottiController(BatchProdottiService batchProdottiService) {
        this.batchProdottiService = batchProdottiService;
    }

    @PostMapping("/simulaArrivi")
public ResponseEntity<List<BatchProdottiDTO>> simulaArrivi(@RequestBody int numeroBatch) {
    try {
        // Log di controllo per vedere se la chiamata arriva
        System.out.println("🚀 Richiesta di simulazione ricevuta per " + numeroBatch + " lotti.");
        List<BatchProdottiDTO> batchList = batchProdottiService.generaBatchProdotti(numeroBatch);
        return ResponseEntity.ok(batchList);
        
    } catch (Exception e) {
        // Questo scriverà l'errore nella console di Spring Boot
        System.err.println("❌ ERRORE DURANTE LA SIMULAZIONE:");
        e.printStackTrace(); 
        
        return ResponseEntity.internalServerError().build();
    }
}

@GetMapping("/carica")
public ResponseEntity<List<BatchProdottiDTO>> caricaBatchProdotti() {
    try {
        List<BatchProdottiDTO> batchList = batchProdottiService.getTuttiIBatchProdotti();
        return ResponseEntity.ok(batchList);
    } catch (Exception e) {
        return ResponseEntity.internalServerError().build();
    }
}
}