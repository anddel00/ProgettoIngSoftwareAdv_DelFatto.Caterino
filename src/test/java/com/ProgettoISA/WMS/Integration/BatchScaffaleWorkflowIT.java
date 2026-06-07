package com.ProgettoISA.WMS.Integration;

import com.ProgettoISA.WMS.DTO.BatchScaffaleDTO;
import com.ProgettoISA.WMS.Model.*;
import com.ProgettoISA.WMS.Repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "Admin")
@Transactional
@ActiveProfiles("test")
public class BatchScaffaleWorkflowIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ProdottiRepository prodottiRepository;
    @Autowired private BatchProdottiRepository batchProdottiRepository;
    @Autowired private ScaffaliRepository scaffaliRepository;
    @Autowired private RepartiRepository repartiRepository;
    @Autowired private MappaRepository mappaRepository;
    @Autowired private BatchScaffaleRepository batchScaffaleRepository;

    private Mappa mappaTest;
    private Mappa mappaTest2;
    private BatchProdotti batchTest;
    private Reparti repartoTest;

    @BeforeEach
    void setUp() {
        // Prodotto
        Prodotti prodotto = new Prodotti();
        prodotto.setNome("Pasta Barilla");
        prodotto.setPesoUnitario(0.5f);
        prodotto.setSpazioUnitario(1L);
        prodotto = prodottiRepository.save(prodotto);

        // Lotto da 100 pezzi
        batchTest = new BatchProdotti();
        batchTest.setProdotto(prodotto);
        batchTest.setQta(100);
        batchTest.setScadenza(LocalDate.of(2027, 6, 30));
        batchTest = batchProdottiRepository.save(batchTest);

        // Reparto
        repartoTest = new Reparti();
        repartoTest.setNome("Reparto Secco");
        repartoTest.setMaxX(10L);
        repartoTest.setMaxY(10L);
        repartoTest = repartiRepository.save(repartoTest);

        // Scaffale fisico (template)
        Scaffali scaffale = new Scaffali(3, 3, 3, 1000);
        scaffale = scaffaliRepository.save(scaffale);

        // Mappa 1 (scaffale posizionato nel reparto)
        mappaTest = new Mappa();
        mappaTest.setReparto(repartoTest);
        mappaTest.setScaffale(scaffale);
        mappaTest.setX(0);
        mappaTest.setY(0);
        mappaTest.setOrientamentoScaffale("ORIZZONTALE");
        mappaTest = mappaRepository.save(mappaTest);

        // Mappa 2 (secondo scaffale)
        mappaTest2 = new Mappa();
        mappaTest2.setReparto(repartoTest);
        mappaTest2.setScaffale(scaffale);
        mappaTest2.setX(5);
        mappaTest2.setY(5);
        mappaTest2.setOrientamentoScaffale("VERTICALE");
        mappaTest2 = mappaRepository.save(mappaTest2);
    }

    // =========================================================================
    // GET /api/batch-scaffale/carica
    // =========================================================================
    @Test
    @DisplayName("GET /carica: restituisce lista vuota quando non ci sono batch sugli scaffali")
    void testCaricaListaVuota() throws Exception {
        mockMvc.perform(get("/api/batch-scaffale/carica"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /carica: restituisce i batch salvati con tutti i campi")
    void testCaricaConDati() throws Exception {
        // Prepariamo dei dati nel DB
        BatchScaffale bs = new BatchScaffale(mappaTest, batchTest, 1, 2, 0, 30);
        batchScaffaleRepository.save(bs);

        mockMvc.perform(get("/api/batch-scaffale/carica"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].idMappa").value(mappaTest.getId()))
            .andExpect(jsonPath("$[0].idBatchProdotti").value(batchTest.getId()))
            .andExpect(jsonPath("$[0].colonna").value(1))
            .andExpect(jsonPath("$[0].riga").value(2))
            .andExpect(jsonPath("$[0].altezza").value(0))
            .andExpect(jsonPath("$[0].qta").value(30));
    }

    // =========================================================================
    // GET /api/batch-scaffale/reparto/{id}
    // =========================================================================
    @Test
    @DisplayName("GET /reparto/{id}: filtra batch per reparto corretto")
    void testCaricaPerReparto() throws Exception {
        // Creiamo un secondo reparto con i suoi dati
        Reparti altroReparto = new Reparti();
        altroReparto.setNome("Surgelati");
        altroReparto.setMaxX(5L);
        altroReparto.setMaxY(5L);
        altroReparto = repartiRepository.save(altroReparto);

        Scaffali scaffale2 = new Scaffali(2, 2, 2, 500);
        scaffale2 = scaffaliRepository.save(scaffale2);

        Mappa mappaAltro = new Mappa();
        mappaAltro.setReparto(altroReparto);
        mappaAltro.setScaffale(scaffale2);
        mappaAltro.setX(0);
        mappaAltro.setY(0);
        mappaAltro.setOrientamentoScaffale("ORIZZONTALE");
        mappaAltro = mappaRepository.save(mappaAltro);

        // Batch sul reparto 1
        batchScaffaleRepository.save(new BatchScaffale(mappaTest, batchTest, 0, 0, 0, 10));
        // Batch sul reparto 2
        batchScaffaleRepository.save(new BatchScaffale(mappaAltro, batchTest, 0, 0, 0, 20));

        // Richiesta per il primo reparto: deve restituire solo 1 record
        mockMvc.perform(get("/api/batch-scaffale/reparto/" + repartoTest.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].qta").value(10));

        // Richiesta per il secondo reparto: deve restituire solo 1 record
        mockMvc.perform(get("/api/batch-scaffale/reparto/" + altroReparto.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].qta").value(20));
    }

    @Test
    @DisplayName("GET /reparto/{id}: restituisce lista vuota per reparto senza scaffali")
    void testRepartoVuotoLazyLoading() throws Exception {
        Reparti repartoVuoto = new Reparti();
        repartoVuoto.setNome("Vuoto");
        repartoVuoto.setMaxX(5L);
        repartoVuoto.setMaxY(5L);
        repartoVuoto = repartiRepository.save(repartoVuoto);

        mockMvc.perform(get("/api/batch-scaffale/reparto/" + repartoVuoto.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    // =========================================================================
    // POST /api/batch-scaffale/salva — Inserimento nuovo
    // =========================================================================
    @Test
    @DisplayName("POST /salva: inserimento di un nuovo batch su uno slot vuoto")
    void testInserimentoNuovoBatch() throws Exception {
        BatchScaffaleDTO dto = new BatchScaffaleDTO(null, mappaTest.getId(), batchTest.getId(), 0, 0, 0, 25);

        mockMvc.perform(post("/api/batch-scaffale/salva")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(dto))))
            .andExpect(status().isOk());

        List<BatchScaffale> risultato = batchScaffaleRepository.findAll();
        assertEquals(1, risultato.size());
        assertEquals(25, risultato.get(0).getQta());
        assertEquals(0, risultato.get(0).getColonna());
    }

    // =========================================================================
    // POST /api/batch-scaffale/salva — Merge su slot già occupato
    // =========================================================================
    @Test
    @DisplayName("POST /salva: merge quantità su slot già occupato dallo stesso batch")
    void testMergeSuSlotEsistente() throws Exception {
        // Pre-inseriamo 30 pezzi nello slot (0,0,0)
        batchScaffaleRepository.save(new BatchScaffale(mappaTest, batchTest, 0, 0, 0, 30));

        // Ora proviamo ad aggiungere 20 pezzi allo stesso slot (senza ID = inserimento)
        BatchScaffaleDTO dto = new BatchScaffaleDTO(null, mappaTest.getId(), batchTest.getId(), 0, 0, 0, 20);

        mockMvc.perform(post("/api/batch-scaffale/salva")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(dto))))
            .andExpect(status().isOk());

        List<BatchScaffale> risultato = batchScaffaleRepository.findAll();
        assertEquals(1, risultato.size());
        assertEquals(50, risultato.get(0).getQta()); // 30 + 20
    }

    // =========================================================================
    // POST /api/batch-scaffale/salva — Aggiornamento per ID
    // =========================================================================
    @Test
    @DisplayName("POST /salva: aggiornamento quantità di un record esistente tramite ID")
    void testAggiornamentoPerID() throws Exception {
        BatchScaffale salvato = batchScaffaleRepository.save(
            new BatchScaffale(mappaTest, batchTest, 0, 0, 0, 40)
        );

        BatchScaffaleDTO dto = new BatchScaffaleDTO(
            salvato.getId().longValue(), mappaTest.getId(), batchTest.getId(), 0, 0, 0, 60
        );

        mockMvc.perform(post("/api/batch-scaffale/salva")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(dto))))
            .andExpect(status().isOk());

        BatchScaffale aggiornato = batchScaffaleRepository.findById(salvato.getId().longValue()).orElseThrow();
        assertEquals(60, aggiornato.getQta());
    }

    // =========================================================================
    // POST /api/batch-scaffale/salva — Rimozione (qta <= 0)
    // =========================================================================
    @Test
    @DisplayName("POST /salva: rimozione record quando quantità aggiornata a 0")
    void testRimozioneQuantitaZero() throws Exception {
        BatchScaffale salvato = batchScaffaleRepository.save(
            new BatchScaffale(mappaTest, batchTest, 0, 0, 0, 10)
        );

        BatchScaffaleDTO dto = new BatchScaffaleDTO(
            salvato.getId().longValue(), mappaTest.getId(), batchTest.getId(), 0, 0, 0, 0
        );

        mockMvc.perform(post("/api/batch-scaffale/salva")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(dto))))
            .andExpect(status().isOk());

        assertTrue(batchScaffaleRepository.findAll().isEmpty());
    }

    // =========================================================================
    // POST /api/batch-scaffale/salva — Superamento tetto massimo (vincolo backend)
    // Replica del vincolo presente nel frontend MappaMagazzino.vue:
    //   quantitaSelezionata <= lottoDaAssegnare.quantita
    // =========================================================================
    @Test
    @DisplayName("POST /salva: errore 500 quando la quantità totale supera il tetto del lotto")
    void testErroreSuperamentoTettoMassimo() throws Exception {
        batchTest.setQta(50);
        batchTest = batchProdottiRepository.save(batchTest);

        // Inseriamo 30 già esistenti
        batchScaffaleRepository.save(new BatchScaffale(mappaTest, batchTest, 0, 0, 0, 30));

        // Proviamo ad aggiungerne 25 → totale 55 > 50 → errore
        BatchScaffaleDTO dto = new BatchScaffaleDTO(null, mappaTest.getId(), batchTest.getId(), 1, 0, 0, 25);

        // Il controller cattura l'eccezione del service e ritorna 500
        mockMvc.perform(post("/api/batch-scaffale/salva")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(dto))))
            .andExpect(status().isInternalServerError());
    }

    // =========================================================================
    // Workflow completo: distribuzione su più scaffali
    // =========================================================================
    @Test
    @DisplayName("Workflow: distribuzione batch su più scaffali entro il limite")
    void testDistribuzioneSuPiuScaffali() throws Exception {
        batchTest.setQta(80);
        batchTest = batchProdottiRepository.save(batchTest);

        // Piazza 30 sullo scaffale 1 e 50 sullo scaffale 2
        BatchScaffaleDTO dto1 = new BatchScaffaleDTO(null, mappaTest.getId(), batchTest.getId(), 0, 0, 0, 30);
        BatchScaffaleDTO dto2 = new BatchScaffaleDTO(null, mappaTest2.getId(), batchTest.getId(), 0, 0, 0, 50);

        mockMvc.perform(post("/api/batch-scaffale/salva")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(dto1, dto2))))
            .andExpect(status().isOk());

        List<BatchScaffale> tutti = batchScaffaleRepository.findAll();
        assertEquals(2, tutti.size());

        Integer somma = batchScaffaleRepository.sumQtaByIdBatch(batchTest.getId());
        assertEquals(80, somma); // 30 + 50 = 80 <= 80
    }

    @Test
    @DisplayName("Workflow: distribuzione su più scaffali che supera il limite → errore 500")
    void testDistribuzioneOltreLimiteRollback() throws Exception {
        batchTest.setQta(40);
        batchTest = batchProdottiRepository.save(batchTest);

        BatchScaffaleDTO dto1 = new BatchScaffaleDTO(null, mappaTest.getId(), batchTest.getId(), 0, 0, 0, 25);
        BatchScaffaleDTO dto2 = new BatchScaffaleDTO(null, mappaTest2.getId(), batchTest.getId(), 0, 0, 0, 20);
        // 25 + 20 = 45 > 40

        // Il service lancia l'eccezione che il controller trasforma in HTTP 500
        mockMvc.perform(post("/api/batch-scaffale/salva")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(dto1, dto2))))
            .andExpect(status().isInternalServerError());
    }

    // =========================================================================
    // Workflow: operazioni miste (inserimento + modifica + rimozione)
    // =========================================================================
    @Test
    @DisplayName("Workflow: payload misto con inserimento, modifica e rimozione in un'unica chiamata")
    void testPayloadMisto() throws Exception {
        // Pre-inserisci 2 record
        BatchScaffale slot1 = batchScaffaleRepository.save(
            new BatchScaffale(mappaTest, batchTest, 0, 0, 0, 20)
        );
        BatchScaffale slot2 = batchScaffaleRepository.save(
            new BatchScaffale(mappaTest, batchTest, 1, 0, 0, 15)
        );

        // Payload:
        // 1. Aggiorna slot1 da 20 → 30
        // 2. Rimuove slot2 (qta = 0)
        // 3. Inserisce un nuovo slot (2,0,0) con 10
        // Totale atteso: 30 + 0 + 10 = 40 <= 100 → OK
        BatchScaffaleDTO update = new BatchScaffaleDTO(slot1.getId().longValue(), mappaTest.getId(), batchTest.getId(), 0, 0, 0, 30);
        BatchScaffaleDTO remove = new BatchScaffaleDTO(slot2.getId().longValue(), mappaTest.getId(), batchTest.getId(), 1, 0, 0, 0);
        BatchScaffaleDTO insert = new BatchScaffaleDTO(null, mappaTest.getId(), batchTest.getId(), 2, 0, 0, 10);

        mockMvc.perform(post("/api/batch-scaffale/salva")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(update, remove, insert))))
            .andExpect(status().isOk());

        List<BatchScaffale> risultato = batchScaffaleRepository.findAll();
        assertEquals(2, risultato.size()); // slot1 aggiornato + nuovo, slot2 rimosso

        Integer somma = batchScaffaleRepository.sumQtaByIdBatch(batchTest.getId());
        assertEquals(40, somma);
    }

    // =========================================================================
    // POST /salva: payload vuoto
    // =========================================================================
    @Test
    @DisplayName("POST /salva: payload vuoto non genera errori")
    void testPayloadVuoto() throws Exception {
        mockMvc.perform(post("/api/batch-scaffale/salva")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.emptyList())))
            .andExpect(status().isOk());
    }
}
