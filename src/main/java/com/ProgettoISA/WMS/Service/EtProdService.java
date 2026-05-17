package com.ProgettoISA.WMS.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ProgettoISA.WMS.DTO.EtProdDTO;
import com.ProgettoISA.WMS.Repository.EtProdRepository;

@Service
public class EtProdService {
    private final EtProdRepository etProdRepository;

    public EtProdService(EtProdRepository etProdRepository) {
        this.etProdRepository = etProdRepository;
    }

    public List<EtProdDTO> getallEtProd() {
        return etProdRepository.findAll().stream()
                .map(e -> new EtProdDTO(e.getId(), e.getProdotto().getId(), e.getEtichetta().getId()))
                .toList();
    }
}
