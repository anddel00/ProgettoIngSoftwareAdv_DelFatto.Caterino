package com.ProgettoISA.WMS.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ProgettoISA.WMS.DTO.OrdineDTO;
import com.ProgettoISA.WMS.Service.OrdineService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/ordini")
public class OrdineController {

    @Autowired
    private OrdineService ordineService;

    @GetMapping("/tutti")
    public ResponseEntity<List<OrdineDTO>> getTuttiOrdini() {
        return ResponseEntity.ok(ordineService.getTuttiOrdini());
    }

    @PostMapping("/genera")
    public ResponseEntity<OrdineDTO> generaOrdineCasuale() {
        try {
            OrdineDTO dto = ordineService.generaOrdineCasuale();
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/alloca")
    public ResponseEntity<?> allocaOrdine(@PathVariable Long id) {
        try {
            ordineService.allocaOrdine(id);
            return ResponseEntity.ok(Map.of("message", "Ordine allocato con successo. Task generati."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public ResponseEntity<?> eliminaOrdine(@PathVariable Long id) {
        try {
            ordineService.eliminaOrdine(id);
            return ResponseEntity.ok(Map.of("message", "Ordine eliminato con successo."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/annulla")
    public ResponseEntity<?> annullaOrdineAllocato(@PathVariable Long id) {
        try {
            ordineService.annullaOrdineAllocato(id);
            return ResponseEntity.ok(Map.of("message", "Ordine annullato con successo. Scorte ripristinate."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
