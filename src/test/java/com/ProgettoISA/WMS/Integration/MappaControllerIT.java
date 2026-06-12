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
import org.springframework.security.test.context.support.WithMockUser;

import org.junit.jupiter.api.BeforeEach;
import com.ProgettoISA.WMS.Model.Mappa;
import com.ProgettoISA.WMS.Model.Reparti;
import com.ProgettoISA.WMS.Model.Scaffali;
import com.ProgettoISA.WMS.Repository.MappaRepository;
import com.ProgettoISA.WMS.Repository.RepartiRepository;
import com.ProgettoISA.WMS.Repository.ScaffaliRepository;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "Admin")
@ActiveProfiles("test") // Usa il profilo con il database Docker
@Transactional 
public class MappaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepartiRepository repartiRepository;

    @Autowired
    private ScaffaliRepository scaffaliRepository;

    @Autowired
    private MappaRepository mappaRepository;

    @BeforeEach
    void setup() {
        if (mappaRepository.findAll().isEmpty()) {
            Reparti reparto = new Reparti();
            reparto.setNome("Reparto Test");
            reparto.setMaxX(10L);
            reparto.setMaxY(10L);
            reparto = repartiRepository.save(reparto);

            Scaffali scaffale = new Scaffali(2, 2, 2, 500);
            scaffale = scaffaliRepository.save(scaffale);

            Mappa mappa = new Mappa();
            mappa.setReparto(reparto);
            mappa.setScaffale(scaffale);
            mappa.setX(0);
            mappa.setY(0);
            mappa.setOrientamentoScaffale("ORIZZONTALE");
            mappaRepository.save(mappa);
        }
    }

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