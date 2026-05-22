package com.ProgettoISA.WMS.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.ProgettoISA.WMS.Model.Mappa;
import com.ProgettoISA.WMS.Model.Scaffali;
import com.ProgettoISA.WMS.Repository.MappaRepository;
import com.ProgettoISA.WMS.Repository.ScaffaliRepository;
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
    private final ScaffaliRepository scaffaliRepository;
    private final MappaRepository mappaRepository;

    public RepartiService(RepartiRepository repartiRepository, MappaRepository mappaRepository, ScaffaliRepository scaffaliRepository)
    {
        this.repartiRepository = repartiRepository;
        this.scaffaliRepository = scaffaliRepository;
        this.mappaRepository = mappaRepository;
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

    @Transactional(rollbackFor = Exception.class)
    public Reparti creaRepartoConPlanimetria(RepartiDTO dto) {
        // 1. Creazione e salvataggio del record Reparto base
        Reparti nuovoReparto = new Reparti();
        nuovoReparto.setNome(dto.getNome());
        nuovoReparto.setMaxX(dto.getMaxX());
        nuovoReparto.setMaxY(dto.getMaxY());
        nuovoReparto.setTemperatura(dto.getTemperatura());

        nuovoReparto = repartiRepository.save(nuovoReparto);

        // 2. Calcolo Planimetria "IKEA-Style"
        // Corridoio: 2 se X è pari, 3 se X è dispari
        int corridoioCentrale = (nuovoReparto.getMaxX() % 2 == 0) ? 2 : 3;
        int larghezzaScaffale = (int) (nuovoReparto.getMaxX() - corridoioCentrale) / 2;

        // Sicurezza: se il reparto è troppo stretto, evitiamo di generare scaffali negativi o a dimensione 0
        if (larghezzaScaffale > 0) {
            // Creiamo il template dello scaffale (Altezza fissa 3 piani, max 3000kg)
            Scaffali templateScaffale = new Scaffali(larghezzaScaffale, 1, 3, 3000);
            templateScaffale = scaffaliRepository.save(templateScaffale);

            List<Mappa> mappeDaSalvare = new ArrayList<>();

            // Ciclo sulla Y (Lunghezza del reparto) lasciando 1 blocco di spazio tra gli scaffali (step = 2)
            for (int y = 0; y <= (nuovoReparto.getMaxY() - 1); y += 2) {
                // Scaffale di SINISTRA
                mappeDaSalvare.add(new Mappa(nuovoReparto, templateScaffale, 0, y, "ORIZZONTALE"));

                // Scaffale di DESTRA
                int coordinataXDestra = larghezzaScaffale + corridoioCentrale;
                mappeDaSalvare.add(new Mappa(nuovoReparto, templateScaffale, coordinataXDestra, y, "ORIZZONTALE"));
            }

            // Scrittura massiva della mappa a DB
            mappaRepository.saveAll(mappeDaSalvare);
        }

        return nuovoReparto;
    }
}
