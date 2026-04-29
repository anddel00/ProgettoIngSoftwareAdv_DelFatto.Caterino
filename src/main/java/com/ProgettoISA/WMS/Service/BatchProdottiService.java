package com.ProgettoISA.WMS.Service;

import com.ProgettoISA.WMS.DTO.BatchProdottiDTO;
import com.ProgettoISA.WMS.Model.*;
import com.ProgettoISA.WMS.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class BatchProdottiService {

    @Autowired
    private BatchProdottiRepository batchProdottiRepository;

    @Autowired
    private ProdottiRepository prodottiRepository;

    @Autowired
    private RepartiRepository repartiRepository;

    @Autowired
    private MappaRepository mappaRepository;

    @Autowired
    private BatchScaffaleRepository batchScaffaleRepository;

    private final Random random = new Random();

    @Transactional
    public List<BatchProdottiDTO> generaBatchProdotti(int numeroBatch) {
        List<BatchProdottiDTO> batchListDTO = new ArrayList<>();
        List<Prodotti> tuttiIProdotti = prodottiRepository.findAll();

        if (tuttiIProdotti.isEmpty()) {
            throw new RuntimeException("Nessun prodotto trovato nel database per la simulazione.");
        }

        // 1. trova il reparto INBOUND
        Reparti repartoInbound = repartiRepository.findByNome("INBOUND")
                .orElseThrow(() -> new RuntimeException("Errore critico: Reparto INBOUND non trovato."));

        // 2. Trova lo scaffale nel reparto inbound
        // uso id del reparto come era stato definito in MappaRepository
        List<Mappa> associazioniMappa = mappaRepository.findByRepartoId(repartoInbound.getId());

        if (associazioniMappa.isEmpty()) {
            throw new RuntimeException("Errore critico: Nessun collegamento Mappa trovato per l'INBOUND.");
        }

        // Estraggo lo scaffale
        Scaffali scaffaleInbound = associazioniMappa.get(0).getScaffale();

        // 3. Genero i batch casuali
        for (int i = 0; i < numeroBatch; i++) {
            Prodotti prodottoScelto = tuttiIProdotti.get(random.nextInt(tuttiIProdotti.size()));

            int quantita = random.nextInt(91) + 10;
            LocalDate dataScadenza = LocalDate.now().plusDays(random.nextInt(336) + 30);

            BatchProdotti nuovoBatch = new BatchProdotti();
            nuovoBatch.setProdotto(prodottoScelto);
            nuovoBatch.setQta(quantita);
            nuovoBatch.setScadenza(dataScadenza);

            BatchProdotti batchSalvato = batchProdottiRepository.save(nuovoBatch);

            // 4. Li salvo nello scaffale INBOUND (cioè del reparto INBOUND)
            BatchScaffale stoccaggioInbound = new BatchScaffale();
            stoccaggioInbound.setBatch_prodotti(batchSalvato);
            stoccaggioInbound.setScaffale(scaffaleInbound);
            stoccaggioInbound.setQta(quantita);
            stoccaggioInbound.setColonna(0); // Symbolic placement
            stoccaggioInbound.setRiga(0);    // Symbolic placement
            stoccaggioInbound.setAltezza(0); // Symbolic placement

            batchScaffaleRepository.save(stoccaggioInbound);

            // Costruisco il DTO per il frontend
            BatchProdottiDTO dto = new BatchProdottiDTO(
                    batchSalvato.getId(),
                    prodottoScelto.getId(),
                    prodottoScelto.getNome(),
                    quantita,
                    dataScadenza.format(DateTimeFormatter.ISO_LOCAL_DATE)
            );
            batchListDTO.add(dto);
        }

        return batchListDTO;
    }
}