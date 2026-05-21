package com.ProgettoISA.WMS.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ProgettoISA.WMS.DTO.BatchScaffaleDTO;
import com.ProgettoISA.WMS.Model.BatchProdotti;
import com.ProgettoISA.WMS.Model.BatchScaffale;
import com.ProgettoISA.WMS.Repository.BatchProdottiRepository;
import com.ProgettoISA.WMS.Repository.BatchScaffaleRepository;
import com.ProgettoISA.WMS.Repository.MappaRepository;

@Service
public class BatchScaffaleService {
    private final BatchScaffaleRepository batchScaffaleRepository;
    private final MappaRepository mappaRepository;
    private final BatchProdottiRepository batchProdottiRepository;
    
    public BatchScaffaleService(BatchScaffaleRepository batchScaffaleRepository, MappaRepository mappaRepository, BatchProdottiRepository batchProdottiRepository) {
        this.batchScaffaleRepository = batchScaffaleRepository;
        this.mappaRepository = mappaRepository;
        this.batchProdottiRepository = batchProdottiRepository;
    }

    @Transactional(readOnly = true)
    public List<BatchScaffaleDTO> getTuttiIBatchScaffali() {
        List<BatchScaffale> batchScaffali = batchScaffaleRepository.findAll();
        return batchScaffali.stream().map(bs -> new BatchScaffaleDTO(
            bs.getId().longValue(),
            bs.getMappa().getId(), 
            bs.getBatch_prodotti().getId(), 
            bs.getColonna(),
            bs.getRiga(),
            bs.getAltezza(),
            bs.getQta()
        )).collect(Collectors.toList());
    }

    // LAZY LOADING: Restituisce solo i BatchScaffale del reparto richiesto
    @Transactional(readOnly = true)
    public List<BatchScaffaleDTO> getBatchScaffaliPerReparto(Long idReparto) {
        List<BatchScaffale> batchScaffali = batchScaffaleRepository.findByMappaRepartoId(idReparto);
        return batchScaffali.stream().map(bs -> new BatchScaffaleDTO(
            bs.getId().longValue(),
            bs.getMappa().getId(),
            bs.getBatch_prodotti().getId(),
            bs.getColonna(),
            bs.getRiga(),
            bs.getAltezza(),
            bs.getQta()
        )).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void sincronizzaBatch(List<BatchScaffaleDTO> payload) throws Exception {
        Set<Long> lottiCoinvolti = new HashSet<>();

        // Cicliamo l'unica lista inviata dal frontend
        for (BatchScaffaleDTO dto : payload) {
            
            if (dto.getId() == null) {
                // ==========================================
                // CASO A: INSERIMENTO (Non ha un ID assegnato)
                // ==========================================
                BatchScaffale esistente = batchScaffaleRepository.trovaEsistente(
                    dto.getIdMappa(), dto.getIdBatchProdotti(), dto.getRiga(), dto.getColonna(), dto.getAltezza()
                ).orElse(null);

                if (esistente != null) {
                    esistente.setQta(esistente.getQta() + dto.getQta());
                    batchScaffaleRepository.save(esistente);
                } else {
                    BatchScaffale nuovo = new BatchScaffale(
                        mappaRepository.findById(dto.getIdMappa()).orElseThrow(() -> new Exception("Mappa non trovata")),
                        batchProdottiRepository.findById(dto.getIdBatchProdotti()).orElseThrow(() -> new Exception("Batch non trovato")),
                        dto.getColonna(),
                        dto.getRiga(),
                        dto.getAltezza(),
                        dto.getQta()
                    );
                    batchScaffaleRepository.save(nuovo);
                }
                lottiCoinvolti.add(dto.getIdBatchProdotti());

            } else {
                // ==========================================
                // CASO B: AGGIORNAMENTO O RIMOZIONE (L'ID c'è)
                // ==========================================
                BatchScaffale esistente = batchScaffaleRepository.findById(dto.getId())
                    .orElseThrow(() -> new Exception("Record non trovato"));

                if (dto.getQta() <= 0) {
                    batchScaffaleRepository.delete(esistente);
                } else {
                    esistente.setQta(dto.getQta());
                    batchScaffaleRepository.save(esistente);
                }
                lottiCoinvolti.add(esistente.getBatch_prodotti().getId());
            }
        }

        // Forziamo il DB ad aggiornare i dati per fare le somme matematiche correttamente
        batchScaffaleRepository.flush();

        // ==========================================
        // 3. CONTROLLO TETTO MASSIMO DI SICUREZZA
        // ==========================================
        for (Long idBatch : lottiCoinvolti) {
            BatchProdotti lottoMaster = batchProdottiRepository.findById(idBatch)
                .orElseThrow(() -> new Exception("Lotto originale non trovato"));
            
            Integer qtaSuScaffali = batchScaffaleRepository.sumQtaByIdBatch(idBatch);
            if (qtaSuScaffali == null) qtaSuScaffali = 0;

            if (qtaSuScaffali > lottoMaster.getQta()) {
                throw new Exception("Errore: Quantità massima superata per il lotto " + idBatch + 
                                    ". Spazio max: " + lottoMaster.getQta() + " Pz.");
            }
        }
    }
}