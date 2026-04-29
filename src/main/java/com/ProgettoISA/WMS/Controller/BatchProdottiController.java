package com.ProgettoISA.WMS.Controller;

import com.ProgettoISA.WMS.DTO.BatchProdottiDTO;
import com.ProgettoISA.WMS.Service.BatchProdottiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/batch-prodotti")
public class BatchProdottiController {

    // Iniezione del Service
    private final BatchProdottiService batchProdottiService;

    public BatchProdottiController(BatchProdottiService batchProdottiService) {
        this.batchProdottiService = batchProdottiService;
    }

    // Usiamo @RequestParam per prendere il parametro dall'URL
    @PostMapping("/simulaArrivi")
    public ResponseEntity<List<BatchProdottiDTO>> simulaArrivi(@RequestParam(defaultValue = "1") int numeroBatch) {
        try {
            System.out.println("Richiesta di simulazione ricevuta per " + numeroBatch + " lotti.");

            // Usiamo l'istanza iniettata (minuscola), non la classe!
            List<BatchProdottiDTO> batchList = batchProdottiService.generaBatchProdotti(numeroBatch);

            return ResponseEntity.ok(batchList);

        } catch (Exception e) {
            System.err.println("ERRORE DURANTE LA SIMULAZIONE:");
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}