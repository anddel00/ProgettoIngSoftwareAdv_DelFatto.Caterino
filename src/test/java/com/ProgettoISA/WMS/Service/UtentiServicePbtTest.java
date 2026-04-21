package com.ProgettoISA.WMS.Service;

import com.ProgettoISA.WMS.Model.Ruoli;
import com.ProgettoISA.WMS.Model.Utenti;
import com.ProgettoISA.WMS.Repository.RuoliRepository;
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

    @Mock
    private RuoliRepository ruoliRepository;

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

    @Property
    void registrazioneDovrebbeGestireQualsiasiStringaValida(
            @ForAll @AlphaChars @StringLength(min = 1, max = 50) String nomeCasuale,
            @ForAll @AlphaChars @StringLength(min = 1, max = 50) String cognomeCasuale,
            @ForAll @StringLength(min = 5, max = 20) String passwordCasuale
    ) {
        // PREPARAZIONE
        String emailCasuale = nomeCasuale + "@test.it";
        Utenti utente = new Utenti();
        utente.setNome(nomeCasuale);
        utente.setCognome(cognomeCasuale);
        utente.setEmail(emailCasuale);
        utente.setPassword(passwordCasuale);

        Ruoli ruoloFinto = new Ruoli();
        ruoloFinto.setNomeRuolo("Dipendente");

        // Istruiamo i mock
        when(utentiRepository.existsByEmail(emailCasuale)).thenReturn(false);
        when(ruoliRepository.findByNomeRuolo("Dipendente")).thenReturn(Optional.of(ruoloFinto));
        when(passwordEncoder.encode(passwordCasuale)).thenReturn("hashed_pass");

        // Diciamo al finto repository di restituire semplicemente l'utente che gli passiamo
        when(utentiRepository.save(org.mockito.ArgumentMatchers.any(Utenti.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // AZIONE
        Utenti salvato = utentiService.registraUtente(utente, "Dipendente");

        // VERIFICA: Nessun crash e i dati generati a caso rimangono integri
        assertEquals(nomeCasuale, salvato.getNome());
        assertEquals(cognomeCasuale, salvato.getCognome());
        assertEquals("hashed_pass", salvato.getPassword());
    }
}