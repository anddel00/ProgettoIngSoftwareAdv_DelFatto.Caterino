package com.ProgettoISA.WMS.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ProgettoISA.WMS.DTO.MappaDTO;
import com.ProgettoISA.WMS.Service.MappaService;

@RestController
@RequestMapping("/api/mappa")
@CrossOrigin(origins = "*") 
public class MappaController {
    
    private final MappaService mappaService;

    public MappaController(MappaService mappaService) {
        this.mappaService = mappaService;
    }

    @GetMapping("/carica")
    public ResponseEntity<List<MappaDTO>> getMappa() {
        try {
            List<MappaDTO> mappa = mappaService.getTuttaLaMappa();
            return ResponseEntity.ok(mappa);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}