package com.ProgettoISA.WMS.Integration;

import com.ProgettoISA.WMS.DTO.CreaTaskDTO;
import com.ProgettoISA.WMS.Model.*;
import com.ProgettoISA.WMS.Repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "Admin")
@Transactional // Rollback automatico a fine test
@ActiveProfiles("test") // Usa Testcontainers con PostgreSQL
public class TaskWorkflowIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UtentiRepository utentiRepository;

    @Autowired
    private TurniDipRepository turniDipRepository;

    @Autowired
    private ProdottiRepository prodottiRepository;

    @Autowired
    private BatchProdottiRepository batchProdottiRepository;

    @Autowired
    private ScaffaliRepository scaffaliRepository;

    @Autowired
    private RepartiRepository repartiRepository;

    @Autowired
    private MappaRepository mappaRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskDipRepository taskDipRepository;

    @Autowired
    private BatchScaffaleRepository batchScaffaleRepository;

    private Mappa mappaTest;
    private BatchProdotti batchTest;
    private Utenti dipendente1;
    private Utenti dipendente2;

    @BeforeEach
    void setUp() {
        // 1. Setup Dipendenti in turno
        dipendente1 = new Utenti();
        dipendente1.setEmail("mario.rossi@wms.it");
        dipendente1.setNome("Mario");
        dipendente1.setCognome("Rossi");
        dipendente1.setPassword("pwd");
        dipendente1 = utentiRepository.save(dipendente1);

        dipendente2 = new Utenti();
        dipendente2.setEmail("luigi.verdi@wms.it");
        dipendente2.setNome("Luigi");
        dipendente2.setCognome("Verdi");
        dipendente2.setPassword("pwd");
        dipendente2 = utentiRepository.save(dipendente2);

        TurniDip turno1 = new TurniDip();
        turno1.setDipendente(dipendente1);
        turno1.setOraInizioReale(java.time.LocalDateTime.now());
        turniDipRepository.save(turno1);

        TurniDip turno2 = new TurniDip();
        turno2.setDipendente(dipendente2);
        turno2.setOraInizioReale(java.time.LocalDateTime.now());
        turniDipRepository.save(turno2);

        // 2. Setup Prodotto e Lotto (Batch)
        Prodotti prodotto = new Prodotti();
        prodotto.setNome("Scatola di Pasta");
        prodotto.setPesoUnitario(0.5f);
        prodotto.setSpazioUnitario(1L);
        prodotto = prodottiRepository.save(prodotto);

        batchTest = new BatchProdotti();
        batchTest.setProdotto(prodotto);
        batchTest.setQta(100);
        batchTest.setScadenza(java.time.LocalDate.of(2026, 12, 31));
        batchTest = batchProdottiRepository.save(batchTest);

        // 3. Setup Mappa (Magazzino e Scaffale Fisico)
        Reparti reparto = new Reparti();
        reparto.setNome("Reparto Secco");
        reparto.setMaxX(10L);
        reparto.setMaxY(10L);
        reparto = repartiRepository.save(reparto);

        Scaffali catalogoScaffale = new Scaffali(3, 3, 3, 1000);
        catalogoScaffale = scaffaliRepository.save(catalogoScaffale);

        mappaTest = new Mappa();
        mappaTest.setReparto(reparto);
        mappaTest.setScaffale(catalogoScaffale);
        mappaTest.setX(5);
        mappaTest.setY(5);
        mappaTest.setOrientamentoScaffale("ORIZZONTALE");
        mappaTest = mappaRepository.save(mappaTest);
    }

    @Test
    void testEndToEndPushWorkflow() throws Exception {
        // STEP 1: Creazione e Assegnazione Automatica (Bilanciamento)
        // Creiamo 3 task. Il bilanciamento dovrebbe assegnare:
        // Task 1 -> Dipendente 1
        // Task 2 -> Dipendente 2
        // Task 3 -> Dipendente 1 (perché 1 e 2 sono pari a 1 task, ma 1 ha l'ultimo completato a 0 o pari merito)
        CreaTaskDTO t1 = createDTO(10, "Task 1");
        CreaTaskDTO t2 = createDTO(20, "Task 2");
        CreaTaskDTO t3 = createDTO(30, "Task 3");

        mockMvc.perform(post("/api/tasks/crea-e-assegna-multipli")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Arrays.asList(t1, t2, t3))))
                .andExpect(status().isOk());

        // Verifichiamo il bilanciamento nel DB
        List<TaskDip> assegnazioni = taskDipRepository.findAll();
        assertEquals(3, assegnazioni.size(), "Dovrebbero esserci 3 assegnazioni di task in totale");

        long taskDip1 = assegnazioni.stream().filter(a -> a.getDipendente().getEmail().equals(dipendente1.getEmail())).count();
        long taskDip2 = assegnazioni.stream().filter(a -> a.getDipendente().getEmail().equals(dipendente2.getEmail())).count();

        // Dato che ci sono 3 task e 2 dipendenti, uno ne avrà 2 e l'altro 1.
        assertEquals(3, taskDip1 + taskDip2);
        
        // Salviamoci l'assegnazione che vogliamo completare. Diciamo il Task 1 assegnato al Dipendente 1.
        TaskDip assegnazioneDaCompletare = assegnazioni.get(0);
        Task taskCreato = assegnazioneDaCompletare.getTask();
        assertNotNull(taskCreato);
        assertEquals("DA_FARE", taskCreato.getStato_task());

        // STEP 2: Completamento del Task e Aggiornamento Magazzino Fisico
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/tasks/" + taskCreato.getId() + "/stato")
                .param("nuovoStato", "COMPLETATO"))
                .andExpect(status().isOk());

        // Verifichiamo che lo stato del task sia completato
        Task taskAggiornato = taskRepository.findById(taskCreato.getId()).orElseThrow();
        assertEquals("COMPLETATO", taskAggiornato.getStato_task());

        // VERIFICA FINALE CRITICA: Controlliamo che la merce sia fisicamente presente sullo scaffale!
        List<BatchScaffale> merceSulloScaffale = batchScaffaleRepository.findAll();
        assertEquals(1, merceSulloScaffale.size(), "Dovrebbe esserci esattamente un record BatchScaffale!");
        
        BatchScaffale slotFisico = merceSulloScaffale.get(0);
        assertEquals(mappaTest.getId(), slotFisico.getMappa().getId(), "La merce deve trovarsi sulla mappa/scaffale target");
        assertEquals(batchTest.getId(), slotFisico.getBatch_prodotti().getId(), "Il lotto deve corrispondere");
        assertEquals(t1.getQuantita(), slotFisico.getQta(), "La quantità posizionata deve corrispondere alla quantità del task!");
    }

    private CreaTaskDTO createDTO(int quantita, String descrizione) {
        CreaTaskDTO dto = new CreaTaskDTO();
        dto.setDescrizione(descrizione);
        dto.setTipoTask("DEPOSITO");
        dto.setQuantita(quantita);
        dto.setIdBatch(batchTest.getId());
        dto.setIdScaffaleFine(mappaTest.getId());
        dto.setNuovaX(0);
        dto.setNuovaY(0);
        dto.setNuovaZ(0);
        return dto;
    }
}
