package com.ProgettoISA.WMS.Service;

import com.ProgettoISA.WMS.DTO.CatalogoBatchDTO;
import com.ProgettoISA.WMS.Model.BatchProdotti;
import com.ProgettoISA.WMS.Model.BatchScaffale;
import com.ProgettoISA.WMS.Model.Mappa;
import com.ProgettoISA.WMS.Model.Prodotti;
import com.ProgettoISA.WMS.Model.Reparti;
import com.ProgettoISA.WMS.Repository.BatchProdottiRepository;
import com.ProgettoISA.WMS.Repository.BatchScaffaleRepository;
import com.ProgettoISA.WMS.Repository.ProdottiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class BatchProdottiServiceTest {

    @Mock
    private BatchProdottiRepository batchProdottiRepository;

    @Mock
    private ProdottiRepository prodottiRepository;

    @Mock
    private BatchScaffaleRepository batchScaffaleRepository;

    @InjectMocks
    private BatchProdottiService batchProdottiService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCatalogoLottiAvanzato_MappaturaEFormattazione() {
        // Arrange
        BatchProdotti batch = new BatchProdotti();
        batch.setId(1L);
        Prodotti prodotto = new Prodotti();
        prodotto.setNome("Pizza");
        batch.setProdotto(prodotto);
        batch.setQta(100);

        Page<BatchProdotti> page = new PageImpl<>(Collections.singletonList(batch));
        when(batchProdottiRepository.findByFiltriAvanzati(any(), any(), any(), any())).thenReturn(page);

        Reparti reparto = new Reparti();
        reparto.setNome("Surgelati");
        
        Mappa mappa1 = new Mappa();
        mappa1.setId(5L);
        mappa1.setReparto(reparto);
        
        BatchScaffale bs1 = new BatchScaffale();
        bs1.setBatch_prodotti(batch);
        bs1.setMappa(mappa1);
        bs1.setRiga(0);
        bs1.setColonna(1);
        bs1.setQta(60);

        BatchScaffale bs2 = new BatchScaffale();
        bs2.setBatch_prodotti(batch);
        bs2.setMappa(mappa1); // Stesso scaffale
        bs2.setRiga(1);
        bs2.setColonna(2);
        bs2.setQta(40);

        when(batchScaffaleRepository.findByBatchIdsWithMappaAndReparto(any())).thenReturn(Arrays.asList(bs1, bs2));

        // Act
        Page<CatalogoBatchDTO> result = batchProdottiService.getCatalogoLottiAvanzato(null, null, null, PageRequest.of(0, 10), batchScaffaleRepository);

        // Assert
        assertEquals(1, result.getContent().size());
        CatalogoBatchDTO dto = result.getContent().get(0);
        assertEquals("Pizza", dto.getNomeProdotto());
        assertEquals(100, dto.getQuantitaDisponibile());
        
        List<String> posizioni = dto.getPosizioni();
        assertEquals(2, posizioni.size());
        assertTrue(posizioni.contains("Reparto Surgelati - Sc. 5 (R:1, C:2) - 60pz"));
        assertTrue(posizioni.contains("Reparto Surgelati - Sc. 5 (R:2, C:3) - 40pz"));
    }
}
