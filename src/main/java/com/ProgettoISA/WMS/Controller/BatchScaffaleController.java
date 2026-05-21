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

import com.ProgettoISA.WMS.DTO.BatchScaffaleDTO;
import com.ProgettoISA.WMS.Service.BatchScaffaleService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/batch-scaffale")
public class BatchScaffaleController {
    
    private final BatchScaffaleService batchScaffaleService;

    public BatchScaffaleController(BatchScaffaleService batchScaffaleService) {
        this.batchScaffaleService = batchScaffaleService;
    }

    @GetMapping("/carica")
    public ResponseEntity<List<BatchScaffaleDTO>> getBatchScaffali() {
        try {
            List<BatchScaffaleDTO> batchScaffali = batchScaffaleService.getTuttiIBatchScaffali();
            return ResponseEntity.ok(batchScaffali);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // LAZY LOADING: Restituisce solo i BatchScaffale del reparto specificato
    @GetMapping("/reparto/{id}")
    public ResponseEntity<List<BatchScaffaleDTO>> getBatchScaffaliPerReparto(@PathVariable Long id) {
        try {
            List<BatchScaffaleDTO> batchScaffali = batchScaffaleService.getBatchScaffaliPerReparto(id);
            return ResponseEntity.ok(batchScaffali);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/salva")
    public ResponseEntity<Void> salvaBatchScaffali(@RequestBody List<BatchScaffaleDTO> batchDTOs) {
        try {
            batchScaffaleService.sincronizzaBatch(batchDTOs);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
