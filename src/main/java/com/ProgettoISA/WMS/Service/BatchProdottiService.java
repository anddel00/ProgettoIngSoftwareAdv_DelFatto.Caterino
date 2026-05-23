package com.ProgettoISA.WMS.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ProgettoISA.WMS.DTO.BatchProdottiDTO;
import com.ProgettoISA.WMS.Model.BatchProdotti;
import com.ProgettoISA.WMS.Model.Prodotti; // IMPORTANTE: L'Entità!
import com.ProgettoISA.WMS.Repository.BatchProdottiRepository;      // IMPORTANTE: L'Entità!
import com.ProgettoISA.WMS.Repository.ProdottiRepository;

@Service
public class BatchProdottiService {

    private final BatchProdottiRepository batchProdottiRepository;
    private final ProdottiRepository prodottiRepository;

    public BatchProdottiService(BatchProdottiRepository batchProdottiRepository, ProdottiRepository prodottiRepository) {
        this.batchProdottiRepository = batchProdottiRepository;
        this.prodottiRepository = prodottiRepository;
    }

    public List<BatchProdottiDTO> generaBatchProdotti(int n) {
        // 1. Inizializziamo DUE liste: una per il Database, una per la Risposta
        List<BatchProdotti> batchListEntity = new ArrayList<>();
        List<BatchProdottiDTO> batchListDTO = new ArrayList<>();

        // 2. Prendiamo i VERI prodotti dal DB per evitare ID inesistenti
        List<Prodotti> tuttiIProdotti = prodottiRepository.findAll();
        if (tuttiIProdotti.isEmpty()) {
            throw new RuntimeException("Impossibile generare batch: nessun prodotto nel DB!");
        }

        LocalDate oggi = LocalDate.now();
        LocalDate dataInizio = oggi.plusMonths(1);
        LocalDate dataFine = oggi.plusYears(1);
        long giorniTra = ChronoUnit.DAYS.between(dataInizio, dataFine);

        for (int i = 0; i < n; i++) {

            long giorniCasuali = ThreadLocalRandom.current().nextLong(giorniTra + 1);
            int quantitaCasuale = ThreadLocalRandom.current().nextInt(1, 6);
            LocalDate scadenzaCasuale = dataInizio.plusDays(giorniCasuali);

            // Peschiamo un prodotto casuale ma REALE dalla lista
            Prodotti prodottoScelto = tuttiIProdotti.get(ThreadLocalRandom.current().nextInt(tuttiIProdotti.size()));

            // --- A. CREIAMO L'ENTITÀ (Quella che piace al DB) ---
            BatchProdotti batchEntity = new BatchProdotti();
            batchEntity.setProdotto(prodottoScelto);
            batchEntity.setQta(quantitaCasuale);
            batchEntity.setScadenza(scadenzaCasuale);
            batchListEntity.add(batchEntity);

            // --- B. CREIAMO IL DTO (Quello che piace al Frontend) ---
            BatchProdottiDTO batchDTO = new BatchProdottiDTO();
            batchDTO.setIdProdotto(prodottoScelto.getId());
            batchDTO.setQuantita(quantitaCasuale);
            batchDTO.setScadenza(scadenzaCasuale.toString());
            batchListDTO.add(batchDTO);
        }

        // 3. IL FIX: Salviamo la lista delle ENTITÀ!
        batchProdottiRepository.saveAll(batchListEntity);

        // 4. Restituiamo i DTO
        return batchListDTO;
    }

    public List<BatchProdottiDTO> getTuttiIBatchProdotti() {
        List<BatchProdotti> batchProdotti = batchProdottiRepository.findAll();  
        
        return batchProdotti.stream().map(bp -> new BatchProdottiDTO(
            bp.getId(),
            bp.getProdotto().getId(),
            bp.getQta(),
            bp.getScadenza().toString(),
            bp.getIdLottoOrigine()
        )).collect(Collectors.toList());    
    }

    /**
     * LAZY LOADING: Restituisce i batch rilevanti per un reparto.
     * Esegue DUE query semplici (nessuna subquery correlata) e le unisce in-memory:
     *  1) Batch con almeno uno slot nel reparto (JOIN diretto su BatchScaffale)
     *  2) Batch completamente sospesi (mai assegnati a nessuno scaffale)
     */
    public List<BatchProdottiDTO> getBatchProdottiPerReparto(Long idReparto) {
        // Query 1: batch fisicamente nel reparto
        List<BatchProdotti> nelReparto = batchProdottiRepository.findByReparto(idReparto);
        // Query 2: batch mai assegnati a nessun scaffale (sospesi globali)
        List<BatchProdotti> sospesi = batchProdottiRepository.findCompletamenteSospesi();

        // Unione senza duplicati
        java.util.LinkedHashSet<BatchProdotti> risultato = new java.util.LinkedHashSet<>(nelReparto);
        risultato.addAll(sospesi);

        return risultato.stream().map(bp -> new BatchProdottiDTO(
            bp.getId(),
            bp.getProdotto().getId(),
            bp.getQta(),
            bp.getScadenza().toString(),
            bp.getIdLottoOrigine()
        )).collect(Collectors.toList());
    }

    public org.springframework.data.domain.Page<com.ProgettoISA.WMS.DTO.CatalogoBatchDTO> getCatalogoLottiAvanzato(
            String search, Long idCategoria, String statoSistemazione, org.springframework.data.domain.Pageable pageable, 
            com.ProgettoISA.WMS.Repository.BatchScaffaleRepository batchScaffaleRepository) {
        
        // 1. Recupera la pagina di lotti filtrata
        org.springframework.data.domain.Page<BatchProdotti> pageBatch = 
            batchProdottiRepository.findByFiltriAvanzati(search, idCategoria, statoSistemazione, pageable);
        
        // 2. Estrai gli ID dei lotti nella pagina corrente
        List<Long> batchIds = pageBatch.getContent().stream()
            .map(BatchProdotti::getId)
            .collect(Collectors.toList());
            
        // 3. Esegui UNA SOLA query per recuperare tutte le posizioni fisiche
        List<com.ProgettoISA.WMS.Model.BatchScaffale> posizioniFisiche = new ArrayList<>();
        if (!batchIds.isEmpty()) {
            posizioniFisiche = batchScaffaleRepository.findByBatchIdsWithMappaAndReparto(batchIds);
        }
        
        // 4. Mappa i risultati (Group By Batch ID in memoria per evitare N+1)
        java.util.Map<Long, List<String>> mapPosizioni = new java.util.HashMap<>();
        for (com.ProgettoISA.WMS.Model.BatchScaffale bs : posizioniFisiche) {
            String posStr = "Reparto " + bs.getMappa().getReparto().getNome() + 
                            " - Sc. " + bs.getMappa().getId() + 
                            " (R:" + (bs.getRiga() + 1) + ", C:" + (bs.getColonna() + 1) + ") - " + 
                            bs.getQta() + "pz";
            
            mapPosizioni.computeIfAbsent(bs.getBatch_prodotti().getId(), k -> new ArrayList<>()).add(posStr);
        }
        
        // 5. Costruisci i DTO finali
        return pageBatch.map(b -> new com.ProgettoISA.WMS.DTO.CatalogoBatchDTO(
            b.getId(),
            b.getProdotto() != null ? b.getProdotto().getNome() : "N/A",
            b.getQta(),
            b.getScadenza(),
            mapPosizioni.getOrDefault(b.getId(), new ArrayList<>()),
            b.getIdLottoOrigine(),
            b.getIdOrdineVendita()
        ));
    }
}