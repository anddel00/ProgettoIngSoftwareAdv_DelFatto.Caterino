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
            
            // 1. Recupero lo stato attuale dal database
            Mappa mappaEsistente = mappaRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Mappa non trovata con ID " + dto.getId()));

            Reparti reparto = repartiRepository.findById(dto.getIdReparto())
                .orElseThrow(() -> new RuntimeException("Reparto non trovato"));
                
            Scaffali scaffaleNuovo = scaffaliRepository.findById(dto.getIdScaffale())
                .orElseThrow(() -> new RuntimeException("Scaffale non trovato"));

            // ---------------------------------------------------------
            // 2. VALIDAZIONE COLLISIONI E AREA DI RISPETTO (AABB)
            // ---------------------------------------------------------
            
            // Recupero tutti gli altri scaffali presenti in questo reparto per fare il confronto
            // (Assicurati di avere questo metodo nel tuo MappaRepository)
            List<Mappa> scaffaliNelReparto = mappaRepository.findByRepartoId(reparto.getId());
            
            // Calcolo l'ingombro del MIO scaffale (quello che sto cercando di salvare)
            boolean nuovoIsOrizzontale = dto.getOrientamentoScaffale().equals("ORIZZONTALE");
            int spanX = nuovoIsOrizzontale ? scaffaleNuovo.getMax_righe() : scaffaleNuovo.getMax_colonne();
            int spanY = nuovoIsOrizzontale ? scaffaleNuovo.getMax_colonne() : scaffaleNuovo.getMax_righe();

            int nuovoSinistra = dto.getCoordinataX();
            int nuovoDestra = dto.getCoordinataX() + spanX;
            int nuovoSopra = dto.getCoordinataY();
            int nuovoSotto = dto.getCoordinataY() + spanY;

            // Controllo il mio scaffale contro ogni singolo scaffale già piazzato
            for (Mappa altro : scaffaliNelReparto) {
                
                // Se sto confrontando lo scaffale con se stesso (stesso ID), salto il giro
                if (altro.getId() == dto.getId()) {
                    continue;
                }

                // Calcolo l'ingombro dello scaffale "nemico"
                boolean altroIsOrizzontale = altro.getOrientamentoScaffale().equals("ORIZZONTALE");
                int altroSpanX = altroIsOrizzontale ? altro.getScaffale().getMax_righe() : altro.getScaffale().getMax_colonne();
                int altroSpanY = altroIsOrizzontale ? altro.getScaffale().getMax_colonne() : altro.getScaffale().getMax_righe();

                // Aggiungo 1 blocco di buffer per lo spazio vitale dei muletti
                int buffer = 1; 

                int altroSinistra = altro.getX() - buffer;
                int altroDestra = altro.getX() + altroSpanX + buffer;
                int altroSopra = altro.getY() - buffer;
                int altroSotto = altro.getY() + altroSpanY + buffer;

                // Logica AABB: cerco di dimostrare che NON si toccano
                boolean nonSiToccano = (
                    nuovoSinistra >= altroDestra || // Sono tutto a destra
                    nuovoDestra <= altroSinistra || // Sono tutto a sinistra
                    nuovoSopra >= altroSotto ||     // Sono tutto sotto
                    nuovoSotto <= altroSopra        // Sono tutto sopra
                );

                // Se non è vero che "non si toccano", allora c'è un incidente!
                if (!nonSiToccano) {
                    throw new RuntimeException("Spazio insufficiente! Lo scaffale in posizione X:" 
                        + dto.getCoordinataX() + " Y:" + dto.getCoordinataY() 
                        + " si sovrappone a un altro scaffale o alla sua area di manovra.");
                }
            }

            // ---------------------------------------------------------
            // 3. APPLICAZIONE MODIFICHE
            // ---------------------------------------------------------
            
            // Se siamo arrivati qui, la validazione è passata. Aggiorno l'Entity.
            mappaEsistente.setReparto(reparto);
            mappaEsistente.setScaffale(scaffaleNuovo);
            mappaEsistente.setX(dto.getCoordinataX());
            mappaEsistente.setY(dto.getCoordinataY());
            mappaEsistente.setOrientamentoScaffale(dto.getOrientamentoScaffale());

            return mappaEsistente;
            
        }).collect(Collectors.toList());

        // 4. Salvataggio massivo (JPA eseguirà le query di UPDATE)
        mappaRepository.saveAll(entitiesDaAggiornare);
    }
}