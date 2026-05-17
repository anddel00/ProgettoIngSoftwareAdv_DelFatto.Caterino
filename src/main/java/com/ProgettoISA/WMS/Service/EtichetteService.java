package com.ProgettoISA.WMS.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ProgettoISA.WMS.DTO.EtichetteDTO;
import com.ProgettoISA.WMS.Repository.EtichetteRepository;
@Service
public class EtichetteService {
    
    private final EtichetteRepository etichetteRepository;

    public EtichetteService(EtichetteRepository etichetteRepository) {
        this.etichetteRepository = etichetteRepository;
    }

    public List<EtichetteDTO> getEtichette() {
        return etichetteRepository.findAll().stream()
                .map(e -> new EtichetteDTO(e.getId(), e.getNome()))
                .toList();  
    }


}
