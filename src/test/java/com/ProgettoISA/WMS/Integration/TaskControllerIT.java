package com.ProgettoISA.WMS.Integration;

import com.ProgettoISA.WMS.DTO.CreaTaskDTO;
import com.ProgettoISA.WMS.Model.Utenti;
import com.ProgettoISA.WMS.Repository.TaskDipRepository;
import com.ProgettoISA.WMS.Repository.TaskRepository;
import com.ProgettoISA.WMS.Repository.UtentiRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "Admin")
@Transactional // Fondamentale: fa il "rollback" (cancella) del DB alla fine di ogni test per non sporcarlo
@ActiveProfiles("test") // IMPORTANTE PER TUTTI I TEST DI INTEGRAZIONE!!!!!!
public class TaskControllerIT {

    @Autowired
    private MockMvc mockMvc; // Il finto "Postman" che fa le chiamate HTTP

    @Autowired
    private ObjectMapper objectMapper; // Converte gli oggetti Java in JSON (e viceversa)

    @Autowired
    private UtentiRepository utentiRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskDipRepository taskDipRepository;

    @BeforeEach
    void setUp() {
        // 1. Inseriamo un utente "reale" nel database di test
        Utenti dipendente = new Utenti();
        dipendente.setEmail("integrazione@wms.it");
        dipendente.setNome("Integrazione");
        dipendente.setCognome("Test");
        dipendente.setPassword("passwordSegreta");
        utentiRepository.save(dipendente);
    }

    // ==========================================
    // TEST 1: Il giro completo ("Happy Path")
    // ==========================================
    @Test
    void creaEAssegna_EndToEnd_Successo() throws Exception {
        long taskCountPrima = taskRepository.count();
        long taskDipCountPrima = taskDipRepository.count();
        
        // Arrange: prepariamo il JSON da inviare
        CreaTaskDTO dto = new CreaTaskDTO();
        dto.setDescrizione("Task di Integrazione");
        dto.setTipoTask("PRELIEVO");
        dto.setQuantita(15);
        dto.setEmailDipendente("integrazione@wms.it");

        // Act & Assert: Facciamo la chiamata POST simulata e controlliamo la risposta
        mockMvc.perform(post("/api/tasks/crea-e-assegna")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk()) // Ci aspettiamo uno status HTTP 200
                .andExpect(jsonPath("$.descrizione").value("Task di Integrazione"));

        // Verifiche DB: Andiamo a vedere se Hibernate ha scritto le tabelle!
        long taskCountDopo = taskRepository.count();
        long taskDipCountDopo = taskDipRepository.count();
        assertEquals(taskCountPrima + 1, taskCountDopo, "Il task doveva essere salvato nel DB!");
        assertEquals(taskDipCountPrima + 1, taskDipCountDopo, "Il collegamento TaskDip doveva essere salvato!");
    }

    // ==========================================
    // TEST 2: La Difesa del Backend (Test Sicurezza)
    // ==========================================
    @Test
    void creaEAssegna_EndToEnd_BloccaQuantitaNegative() throws Exception {
        long taskCountPrima = taskRepository.count();
        long taskDipCountPrima = taskDipRepository.count();
        
        // Arrange: proviamo a ingannare il server
        CreaTaskDTO dto = new CreaTaskDTO();
        dto.setDescrizione("Task Malevolo");
        dto.setTipoTask("DEPOSITO");
        dto.setQuantita(-500); // INVALIIDO!
        dto.setEmailDipendente("integrazione@wms.it");

        // Act & Assert: Il controller deve intercettare l'eccezione e restituire 400
        mockMvc.perform(post("/api/tasks/crea-e-assegna")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest()) // Ci aspettiamo HTTP 400
                .andExpect(content().string("Errore di sicurezza: La quantità deve essere maggiore di zero."));

        // Verifiche DB: Il database NON deve essere stato toccato
        long taskCountDopo = taskRepository.count();
        long taskDipCountDopo = taskDipRepository.count();
        assertEquals(taskCountPrima, taskCountDopo, "Il DB non doveva essere modificato!");
        assertEquals(taskDipCountPrima, taskDipCountDopo);
    }
}