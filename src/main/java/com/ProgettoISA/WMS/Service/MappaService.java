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

    @Transactional 
    public void salvaPosizioni(List<MappaDTO> mappaDTOs) {
        
        List<Mappa> entitiesDaAggiornare = mappaDTOs.stream().map(dto -> {
            
            // 1. Recupero lo stato attuale dal database
            Mappa mappaEsistente = mappaRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Mappa non trovata con ID " + dto.getId()));

            Reparti reparto = repartiRepository.findById(dto.getIdReparto())
                .orElseThrow(() -> new RuntimeException("Reparto non trovato"));
                
            Scaffali scaffaleNuovo = scaffaliRepository.findById(dto.getIdScaffale())
                .orElseThrow(() -> new RuntimeException("Scaffale non trovato"));

            // ---------------------------------------------------------
            // 2a. VALIDAZIONE LIMITI REPARTO (Bordi mappa)
            // ---------------------------------------------------------
            
            boolean nuovoIsOrizzontale = dto.getOrientamentoScaffale().equals("ORIZZONTALE");
            int spanX = nuovoIsOrizzontale ? scaffaleNuovo.getMax_righe() : scaffaleNuovo.getMax_colonne();
            int spanY = nuovoIsOrizzontale ? scaffaleNuovo.getMax_colonne() : scaffaleNuovo.getMax_righe();

            if (dto.getCoordinataX() < 0 || dto.getCoordinataY() < 0) {
                throw new IllegalArgumentException("Posizione non valida: lo scaffale non può avere coordinate negative.");
            }

            if (dto.getCoordinataX() + spanX > reparto.getMaxX() || 
                dto.getCoordinataY() + spanY > reparto.getMaxY()) {
                throw new IllegalArgumentException("Spazio insufficiente: lo scaffale esce dai bordi del reparto.");
            }

            // ---------------------------------------------------------
            // 2b. VALIDAZIONE COLLISIONI E AREA DI RISPETTO (AABB)
            // ---------------------------------------------------------
            
            List<Mappa> scaffaliNelReparto = mappaRepository.findByRepartoId(reparto.getId());
            
            int nuovoSinistra = dto.getCoordinataX();
            int nuovoDestra = dto.getCoordinataX() + spanX;
            int nuovoSopra = dto.getCoordinataY();
            int nuovoSotto = dto.getCoordinataY() + spanY;

            for (Mappa altro : scaffaliNelReparto) {
                
                if (altro.getId() == dto.getId()) {
                    continue;
                }

                boolean altroIsOrizzontale = altro.getOrientamentoScaffale().equals("ORIZZONTALE");
                int altroSpanX = altroIsOrizzontale ? altro.getScaffale().getMax_righe() : altro.getScaffale().getMax_colonne();
                int altroSpanY = altroIsOrizzontale ? altro.getScaffale().getMax_colonne() : altro.getScaffale().getMax_righe();

                int buffer = 1; 

                int altroSinistra = altro.getX() - buffer;
                int altroDestra = altro.getX() + altroSpanX + buffer;
                int altroSopra = altro.getY() - buffer;
                int altroSotto = altro.getY() + altroSpanY + buffer;

                boolean nonSiToccano = (
                    nuovoSinistra >= altroDestra || 
                    nuovoDestra <= altroSinistra || 
                    nuovoSopra >= altroSotto ||     
                    nuovoSotto <= altroSopra        
                );

                if (!nonSiToccano) {
                    // Usa IllegalArgumentException qui!
                    throw new IllegalArgumentException("Spazio insufficiente! Lo scaffale in posizione X:" 
                        + dto.getCoordinataX() + " Y:" + dto.getCoordinataY() 
                        + " si sovrappone a un altro scaffale o alla sua area di manovra.");
                }
            }

            // ---------------------------------------------------------
            // 3. APPLICAZIONE MODIFICHE
            // ---------------------------------------------------------
            
            mappaEsistente.setReparto(reparto);
            mappaEsistente.setScaffale(scaffaleNuovo);
            mappaEsistente.setX(dto.getCoordinataX());
            mappaEsistente.setY(dto.getCoordinataY());
            mappaEsistente.setOrientamentoScaffale(dto.getOrientamentoScaffale());

            return mappaEsistente;
            
        }).collect(Collectors.toList());

        mappaRepository.saveAll(entitiesDaAggiornare);
    }
}