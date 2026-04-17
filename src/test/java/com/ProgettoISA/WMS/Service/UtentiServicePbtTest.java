package com.ProgettoISA.WMS.Service;

import com.ProgettoISA.WMS.Repository.UtentiRepository;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;
import net.jqwik.api.lifecycle.BeforeTry;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

// 1. VIA L'ANNOTAZIONE @ExtendWith di JUnit!
class UtentiServicePbtTest {

    @Mock
    private UtentiRepository utentiRepository;

    @Mock
    private PasswordEncoder passwordEncoder; // Inseriamo anche questo per sicurezza

    @InjectMocks
    private UtentiService utentiService;

    // 2. Diciamo a Jqwik di attivare i Mock manualmente prima di ogni generazione casuale
    @BeforeTry
    void setupMocks() {
        MockitoAnnotations.openMocks(this);
    }

    @Property
    void qualsiasiEmailInesistenteDeveLanciareEccezione(
            @ForAll @AlphaChars @StringLength(min = 1, max = 50) String emailCasuale
    ) {
        // PREPARAZIONE
        when(utentiRepository.findByEmail(emailCasuale)).thenReturn(Optional.empty());

        // AZIONE
        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () -> {
            utentiService.effettuaLogin(emailCasuale, "unaPasswordQualsiasi");
        });

        // VERIFICA
        assertEquals("Errore: Nessun utente trovato con questa email.", eccezione.getMessage());
    }
}