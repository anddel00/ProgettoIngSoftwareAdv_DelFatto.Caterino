package com.ProgettoISA.WMS.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ProgettoISA.WMS.DTO.RepartiDTO;
import com.ProgettoISA.WMS.Model.Reparti;
import com.ProgettoISA.WMS.Repository.RepartiRepository;

@Service
public class RepartiService {

    private final RepartiRepository repartiRepository;

    public RepartiService(RepartiRepository repartiRepository) {
        this.repartiRepository = repartiRepository;
    }

    public List<RepartiDTO> getTuttiIReparti() {
        List<Reparti> entitaReparti = repartiRepository.findAll();
        
        // Trasformiamo ogni oggetto Reparto in un RepartiDTO
        return entitaReparti.stream().map(r -> new RepartiDTO(
            r.getId(),
            r.getMaxX(),
            r.getMaxY(),
            r.getTemperatura(),
            r.getNome()
        )).collect(Collectors.toList());
    }
}