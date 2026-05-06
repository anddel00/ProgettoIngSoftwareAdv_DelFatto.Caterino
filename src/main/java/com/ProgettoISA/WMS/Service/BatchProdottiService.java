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

        for(int i = 0; i < n; i++) {
           
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
            bp.getScadenza().toString()
        )).collect(Collectors.toList());    
    }
}