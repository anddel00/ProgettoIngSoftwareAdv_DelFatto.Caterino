package com.ProgettoISA.WMS.Integration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals; // Necessario per pulire la cache
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.ProgettoISA.WMS.DTO.MappaDTO;
import com.ProgettoISA.WMS.Model.Mappa;
import com.ProgettoISA.WMS.Repository.MappaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Usa il profilo con il database Docker (Testcontainers)
@Transactional // Garantisce il rollback alla fine di ogni test
public class MappaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MappaRepository mappaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager; // Il "Grimaldello" per bypassare la cache L1

    /**
     * Test Negativo: Verifica che un errore di bordi venga intercettato 
     * e restituito come 400 Bad Request.
     */
    @Test
    void quandoInvioPosizioniErrate_ritornaBadRequest() throws Exception {
        // Arrange: ID 1 esiste grazie al DatabaseTestRunner, ma X=5000 è fuori limiti
        MappaDTO dtoSballato = new MappaDTO(1L, 5000, 0, 1L, 1L, "ORIZZONTALE");
        List<MappaDTO> payload = List.of(dtoSballato);

        // Act & Assert
        mockMvc.perform(post("/api/mappa/salva-posizioni")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest()); 
        
        System.out.println("✅ INTEGRATION TEST: Errore bordi gestito correttamente con Status 400.");
    }

    /**
     * Test di Atomicità (Rollback): Verifica che se un elemento della lista fallisce,
     * l'intera operazione venga annullata e nessun dato venga sporcato nel DB.
     */
    @Test
    void quandoUnoScaffaleErratoInLista_nessunoVieneSalvato() throws Exception {
        // 1. Prepariamo due scaffali: 
        // Il primo è valido (lo spostiamo a 2,2), il secondo è una bomba (X=5000)
        MappaDTO buono = new MappaDTO(1L, 2, 2, 1L, 1L, "ORIZZONTALE");
        MappaDTO bomba = new MappaDTO(2L, 5000, 0, 2L, 1L, "ORIZZONTALE");
        
        List<MappaDTO> payload = List.of(buono, bomba);

        // 2. Invia la richiesta: deve fallire a causa della "bomba"
        mockMvc.perform(post("/api/mappa/salva-posizioni")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());

        // 3. IL SEGRETO: Puliamo il Persistence Context (L1 Cache)
        // Senza questo, findById leggerebbe l'oggetto "sporco" in RAM con X=2
        entityManager.clear(); 

        // 4. Verifica finale sul DB reale
        Mappa mappaDb = mappaRepository.findById(1L).get();
        
        // Verifichiamo che X sia ancora 0 (valore iniziale del DatabaseTestRunner)
        assertEquals(0, mappaDb.getX(), 
            "ERRORE CRITICO: Il rollback non ha funzionato! Lo scaffale buono è stato salvato.");
            
        System.out.println("✅ INTEGRATION TEST: Rollback verificato con successo. Database integro.");
    }
}