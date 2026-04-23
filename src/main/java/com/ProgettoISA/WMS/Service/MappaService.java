package com.ProgettoISA.WMS.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ProgettoISA.WMS.DTO.MappaDTO;
import com.ProgettoISA.WMS.Model.Mappa;
import com.ProgettoISA.WMS.Model.Reparti;
import com.ProgettoISA.WMS.Model.Scaffali;
import com.ProgettoISA.WMS.Repository.MappaRepository;
import com.ProgettoISA.WMS.Repository.RepartiRepository;
import com.ProgettoISA.WMS.Repository.ScaffaliRepository;

@Service
public class MappaService {

    private final MappaRepository mappaRepository;
    private final RepartiRepository repartiRepository;
    private final ScaffaliRepository scaffaliRepository;

    // Iniettiamo tutte le repository necessarie
    public MappaService(MappaRepository mappaRepository, RepartiRepository repartiRepository, ScaffaliRepository scaffaliRepository) {
        this.mappaRepository = mappaRepository;
        this.repartiRepository = repartiRepository;
        this.scaffaliRepository = scaffaliRepository;
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

    // @Transactional: se si rompe qualcosa a metà, annulla tutto
    @Transactional 
    public void salvaPosizioni(List<MappaDTO> mappaDTOs) {
        
        List<Mappa> entitiesDaAggiornare = mappaDTOs.stream().map(dto -> {
            // 1. TRTROVO OVA IL RECORD ESISTENTE TRAMITE IL SUO ID
            Mappa mappaEsistente = mappaRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Errore: Mappa non trovata con ID " + dto.getId()));

            // 2. RECUPERO LE RELAZIONI (Serve per il ManyToOne)
            // Seleziona il reparto, sia che sia rimasto lo stesso, sia che lo scaffale sia stato spostato
            Reparti reparto = repartiRepository.findById(dto.getIdReparto())
                .orElseThrow(() -> new RuntimeException("Errore: Reparto non trovato"));
                
            Scaffali scaffale = scaffaliRepository.findById(dto.getIdScaffale())
                .orElseThrow(() -> new RuntimeException("Errore: Scaffale non trovato"));

            // 3. AGGIORNO I CAMPI DEL RECORD ESISTENTE
            mappaEsistente.setReparto(reparto);
            mappaEsistente.setScaffale(scaffale);
            mappaEsistente.setX(dto.getCoordinataX());
            mappaEsistente.setY(dto.getCoordinataY());
            mappaEsistente.setOrientamentoScaffale(dto.getOrientamentoScaffale());

            // Ritorna l'oggetto modificato
            return mappaEsistente;
        }).collect(Collectors.toList());

        // 4. SALVA TUTTO. Essendo oggetti con un ID esistente, JPA farà delle UPDATE.
        mappaRepository.saveAll(entitiesDaAggiornare);
    }
}