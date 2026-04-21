package com.ProgettoISA.WMS.Integration;

import com.ProgettoISA.WMS.Model.Ruoli;
import com.ProgettoISA.WMS.Repository.RuoliRepository;
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

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // FONDAMENTALE: Attiva Testcontainers e ignora AWS!
public class UtentiControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UtentiRepository utentiRepository;

    @Autowired
    private RuoliRepository ruoliRepository;

    @BeforeEach
    void setup() {
        // Puliamo il database prima di ogni test
        utentiRepository.deleteAll();

        // Assicuriamoci che esista il ruolo nel DB testcontainers
        if (ruoliRepository.findByNomeRuolo("Dipendente").isEmpty()) {
            ruoliRepository.save(new Ruoli("Dipendente"));
        }
    }

    @Test
    void testCreazioneUtente_FlussoCompleto() throws Exception {
        // 1. Prepariamo il payload JSON identico a quello che manda Vue.js
        Map<String, Object> payload = new HashMap<>();
        payload.put("nome", "Giulia");
        payload.put("cognome", "Bianchi");
        payload.put("username", "gbianchi");
        payload.put("email", "giulia@wms.it");
        payload.put("password", "segreto123");
        payload.put("data_nascita", "1998-05-20");
        payload.put("ruolo", "Dipendente");

        // 2. Eseguiamo la richiesta POST e verifichiamo la risposta
        mockMvc.perform(post("/api/auth/registrati")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk()) // Ci aspettiamo 200 OK
                .andExpect(jsonPath("$.username").value("gbianchi"))
                .andExpect(jsonPath("$.email").value("giulia@wms.it"));

        // 3. Verifica finale: il dato è davvero nel Database?
        assertTrue(utentiRepository.existsByEmail("giulia@wms.it"));
    }
}