package com.ProgettoISA.WMS.Integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Usa il profilo con il database Docker
@Transactional 
public class MappaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void caricaMappa_ritornaSuccessoEListaScaffali() throws Exception {
        
        // Eseguiamo la chiamata GET per caricare la mappa
        mockMvc.perform(get("/api/mappa/carica")
                .contentType(MediaType.APPLICATION_JSON))
                
                // 1. Verifichiamo che il server risponda 200 OK
                .andExpect(status().isOk())
                
                // 2. Verifichiamo che la risposta sia in formato JSON
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                
                // 3. Verifichiamo che la risposta sia un Array JSON ("$")
                .andExpect(jsonPath("$").isArray())
                
                // 4. Verifichiamo che la griglia sia stata generata 
                // (la lunghezza dell'array deve essere maggiore di 0)
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThan(0)));
        
        System.out.println("✅ INTEGRATION TEST: Endpoint /carica risponde correttamente e restituisce la griglia.");
    }
}