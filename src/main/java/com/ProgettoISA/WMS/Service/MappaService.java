package com.ProgettoISA.WMS.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ProgettoISA.WMS.DTO.MappaDTO;
import com.ProgettoISA.WMS.Model.Mappa;
import com.ProgettoISA.WMS.Repository.MappaRepository;

@Service
public class MappaService {

    private final MappaRepository mappaRepository;

    // RepartiRepository e ScaffaliRepository rimossi: non servono più!
    public MappaService(MappaRepository mappaRepository) {
        this.mappaRepository = mappaRepository;
    }

    public List<MappaDTO> getTuttaLaMappa() {
        List<Mappa> entitaMappa = mappaRepository.findAll();
        
        return entitaMappa.stream().map(m -> new MappaDTO(
            m.getId(),
            m.getX(),
            m.getY(),
            m.getScaffale().getId(),
            m.getReparto().getId(),
            m.getOrientamentoScaffale()  
        )).collect(Collectors.toList());
    }

    // LAZY LOADING: Restituisce solo le celle-mappa del reparto richiesto
    public List<MappaDTO> getMappaPerReparto(Long idReparto) {
        List<Mappa> entitaMappa = mappaRepository.findByRepartoId(idReparto);
        return entitaMappa.stream().map(m -> new MappaDTO(
            m.getId(),
            m.getX(),
            m.getY(),
            m.getScaffale().getId(),
            m.getReparto().getId(),
            m.getOrientamentoScaffale()
        )).collect(Collectors.toList());
    }
}