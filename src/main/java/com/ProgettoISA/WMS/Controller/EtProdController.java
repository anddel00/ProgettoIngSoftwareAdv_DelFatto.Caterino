package com.ProgettoISA.WMS.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ProgettoISA.WMS.DTO.EtProdDTO;
import com.ProgettoISA.WMS.Service.EtProdService;

@CrossOrigin(origins = "*")
@RequestMapping("/api/etprod")
@RestController
public class EtProdController {
    private final EtProdService etProdService;

    public EtProdController(EtProdService etProdService) {
        this.etProdService = etProdService;
    }

    @GetMapping("/carica")
    public ResponseEntity<List<EtProdDTO>> getallEtProd() {
        try {
            List<EtProdDTO> etProdList = etProdService.getallEtProd();
            return ResponseEntity.ok(etProdList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}