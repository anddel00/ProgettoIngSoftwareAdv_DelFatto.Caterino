package com.ProgettoISA.WMS.Integration;

import com.ProgettoISA.WMS.Model.BatchProdotti;
import com.ProgettoISA.WMS.Model.Prodotti;
import com.ProgettoISA.WMS.Repository.BatchProdottiRepository;
import com.ProgettoISA.WMS.Repository.ProdottiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import org.springframework.test.context.ActiveProfiles;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Disabilitiamo Spring Security per i test
@ActiveProfiles("test")
@Transactional
public class BatchProdottiControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BatchProdottiRepository batchProdottiRepository;

    @Autowired
    private ProdottiRepository prodottiRepository;

    @BeforeEach
    void setUp() {
        Prodotti p1 = new Prodotti();
        p1.setNome("ProdottoUnicoIntegrazione 1");
        prodottiRepository.save(p1);

        Prodotti p2 = new Prodotti();
        p2.setNome("AltroProdotto");
        prodottiRepository.save(p2);

        BatchProdotti b1 = new BatchProdotti();
        b1.setProdotto(p1);
        b1.setQta(100);
        b1.setScadenza(LocalDate.now());
        batchProdottiRepository.save(b1);

        BatchProdotti b2 = new BatchProdotti();
        b2.setProdotto(p2);
        b2.setQta(50);
        b2.setScadenza(LocalDate.now());
        batchProdottiRepository.save(b2);
    }

    @Test
    void testGetCatalogoConPaginazioneERicerca() throws Exception {
        mockMvc.perform(get("/api/batch-prodotti/catalogo")
                .param("page", "0")
                .param("size", "5")
                .param("search", "ProdottoUnicoIntegrazione"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nomeProdotto", is("ProdottoUnicoIntegrazione 1")))
                .andExpect(jsonPath("$.size", is(5)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }
}
