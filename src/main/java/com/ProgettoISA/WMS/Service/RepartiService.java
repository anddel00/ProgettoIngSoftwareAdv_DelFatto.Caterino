package com.ProgettoISA.WMS.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
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
        
        // Filtriamo il reparto Inbound e trasformiamo ogni oggetto Reparto in un RepartiDTO
        return entitaReparti.stream()
            .filter(r -> r.getNome() == null || !r.getNome().toLowerCase().contains("inbound"))
            .map(r -> new RepartiDTO(
            r.getId(),
            r.getMaxX(),
            r.getMaxY(),
            r.getTemperatura(),
            r.getNome()
        )).collect(Collectors.toList());
    }
}