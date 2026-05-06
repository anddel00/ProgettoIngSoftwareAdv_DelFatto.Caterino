package com.ProgettoISA.WMS.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ProgettoISA.WMS.DTO.BatchScaffaleDTO;
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

    public void salvaTuttiIBatch(List<BatchScaffaleDTO> batchDTOs) {
        List<BatchScaffale> batchDaSalvare = new ArrayList<>();
        for(int i=0; i<batchDTOs.size(); i++) {
            BatchScaffale batch = new BatchScaffale(
                mappaRepository.findById(batchDTOs.get(i).getIdMappa()).orElse(null),
                batchProdottiRepository.findById(batchDTOs.get(i).getIdBatchProdotti()).orElse(null),
                batchDTOs.get(i).getColonna(),
                batchDTOs.get(i).getRiga(),
                batchDTOs.get(i).getAltezza(),
                batchDTOs.get(i).getQta()
            );
            batchDaSalvare.add(batch);
        }
        batchScaffaleRepository.saveAll(batchDaSalvare);
    }
    
}
