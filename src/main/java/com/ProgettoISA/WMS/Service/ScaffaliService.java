package com.ProgettoISA.WMS.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ProgettoISA.WMS.DTO.ScaffaliDTO;
import com.ProgettoISA.WMS.Model.Scaffali;
import com.ProgettoISA.WMS.Repository.ScaffaliRepository;

@Service
public class ScaffaliService {
    
    private final ScaffaliRepository scaffaliRepository;

    public ScaffaliService(ScaffaliRepository scaffaliRepository) {
        this.scaffaliRepository = scaffaliRepository;
    }

    public List<ScaffaliDTO> getTuttiGliScaffali() {
        List<Scaffali> entitaScaffali = scaffaliRepository.findAll();
        
        // Trasformiamo ogni oggetto Scaffale in un ScaffaliDTO
        return entitaScaffali.stream().map(s -> new ScaffaliDTO(
            s.getId(),
            s.getMax_righe(),
            s.getMax_colonne(),
            s.getMax_altezza(),
            s.getMax_peso()
        )).collect(Collectors.toList());
    }
}
