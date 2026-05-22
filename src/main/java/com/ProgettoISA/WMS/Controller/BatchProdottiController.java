package com.ProgettoISA.WMS.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final com.ProgettoISA.WMS.Repository.BatchScaffaleRepository batchScaffaleRepository;

    public BatchProdottiController(BatchProdottiService batchProdottiService, 
                                   com.ProgettoISA.WMS.Repository.BatchScaffaleRepository batchScaffaleRepository) {
        this.batchProdottiService = batchProdottiService;
        this.batchScaffaleRepository = batchScaffaleRepository;
    }

    @PostMapping("/simulaArrivi")
    public ResponseEntity<List<BatchProdottiDTO>> simulaArrivi(@RequestBody int numeroBatch) {
        try {
            System.out.println("🚀 Richiesta di simulazione ricevuta per " + numeroBatch + " lotti.");
            List<BatchProdottiDTO> batchList = batchProdottiService.generaBatchProdotti(numeroBatch);
            return ResponseEntity.ok(batchList);
        } catch (Exception e) {
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

    // LAZY LOADING: Restituisce solo i batch del reparto (assegnati + sospesi compatibili)
    @GetMapping("/reparto/{id}")
    public ResponseEntity<List<BatchProdottiDTO>> caricaBatchProdottiPerReparto(@PathVariable Long id) {
        try {
            List<BatchProdottiDTO> batchList = batchProdottiService.getBatchProdottiPerReparto(id);
            return ResponseEntity.ok(batchList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/catalogo")
    public ResponseEntity<org.springframework.data.domain.Page<com.ProgettoISA.WMS.DTO.CatalogoBatchDTO>> getCatalogoLotti(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "50") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "") String search,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long idCategoria,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String statoSistemazione,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "") String sortByScadenza) {
        
        try {
            org.springframework.data.domain.Pageable pageable;
            
            if ("ASC".equalsIgnoreCase(sortByScadenza)) {
                pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("scadenza").ascending());
            } else if ("DESC".equalsIgnoreCase(sortByScadenza)) {
                pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("scadenza").descending());
            } else {
                pageable = org.springframework.data.domain.PageRequest.of(page, size);
            }
            
            // Convert empty string to null for standardizing SQL behavior
            if (statoSistemazione != null && statoSistemazione.trim().isEmpty()) {
                statoSistemazione = null;
            }

            org.springframework.data.domain.Page<com.ProgettoISA.WMS.DTO.CatalogoBatchDTO> res = 
                batchProdottiService.getCatalogoLottiAvanzato(search, idCategoria, statoSistemazione, pageable, batchScaffaleRepository);
            
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}