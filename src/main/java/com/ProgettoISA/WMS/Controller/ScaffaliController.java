package com.ProgettoISA.WMS.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ProgettoISA.WMS.DTO.ScaffaliDTO;
import com.ProgettoISA.WMS.Service.ScaffaliService;

@RestController
@RequestMapping("/api/scaffali")
@CrossOrigin(origins = "*")
public class ScaffaliController {
    
    private final ScaffaliService scaffaliService;

    public ScaffaliController(ScaffaliService scaffaliService) {
        this.scaffaliService = scaffaliService;
    }

@GetMapping("/carica")
    public ResponseEntity<List<ScaffaliDTO>> caricaScaffali() {
        try {
            List<ScaffaliDTO> scaffali = scaffaliService.getTuttiGliScaffali();
            return ResponseEntity.ok(scaffali);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
        
    }
}
