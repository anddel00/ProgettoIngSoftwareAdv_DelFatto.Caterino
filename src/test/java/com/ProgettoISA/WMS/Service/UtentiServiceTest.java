package com.ProgettoISA.WMS.Service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Fondamentale: usa H2 e non AWS!
public class UtentiServiceTest {

    @Autowired
    private UtentiService utentiService;

    @Test
    void contextLoads() {
        // Verifica che il sistema si avvii correttamente
    }

    @Test
    void testLoginPasswordErrataLanciaEccezione() {
        // Verifichiamo che il service blocchi tentativi falsi
        assertThrows(IllegalArgumentException.class, () -> {
            utentiService.effettuaLogin("email@non-esiste.it", "password_falsa");
        });
    }
}