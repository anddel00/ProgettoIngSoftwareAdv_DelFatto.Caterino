package com.ProgettoISA.WMS.Service;

import com.ProgettoISA.WMS.Model.Ruoli;
import com.ProgettoISA.WMS.Model.Utenti;
import com.ProgettoISA.WMS.Repository.UtentiRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Diciamo a JUnit di usare Mockito (niente SpringBootTest, niente Docker!)
@ExtendWith(MockitoExtension.class)
class UtentiServiceTest {

    // @Mock crea delle "controfigure" finte per i componenti esterni
    @Mock
    private UtentiRepository utentiRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    // @InjectMocks crea il VERO servizio, ma gli inietta dentro le controfigure qui sopra
    @InjectMocks
    private UtentiService utentiService;

    @Test
    void testEffettuaLogin_Successo() {
        // 1. PREPARAZIONE (Arrange)
        String email = "admin@test.it";
        String passwordInChiaro = "password123";

        Ruoli ruoloAdmin = new Ruoli();
        ruoloAdmin.setNomeRuolo("Admin");

        Utenti utenteFinto = new Utenti();
        utenteFinto.setEmail(email);
        utenteFinto.setPassword("password_hashata_dal_db");
        utenteFinto.setRuolo(ruoloAdmin);

        // Istruiamo le controfigure: "Se ti chiedono questa email, restituisci questo utente finto"
        when(utentiRepository.findByEmail(email)).thenReturn(Optional.of(utenteFinto));
        // Istruiamo l'encoder: "Dì che la password corrisponde"
        when(passwordEncoder.matches(passwordInChiaro, "password_hashata_dal_db")).thenReturn(true);

        // 2. AZIONE (Act)
        Utenti risultato = utentiService.effettuaLogin(email, passwordInChiaro);

        // 3. VERIFICA (Assert)
        assertNotNull(risultato, "L'utente non dovrebbe essere nullo");
        assertEquals("admin@test.it", risultato.getEmail(), "L'email deve corrispondere");
        assertEquals("Admin", risultato.getRuolo().getNomeRuolo(), "Il ruolo deve essere Admin");
    }

    @Test
    void testEffettuaLogin_EmailNonTrovata() {
        // 1. PREPARAZIONE
        String emailErrata = "inesistente@test.it";
        // Diciamo al DB finto di rispondere "Vuoto" (Utente non trovato)
        when(utentiRepository.findByEmail(emailErrata)).thenReturn(Optional.empty());

        // 2 & 3. AZIONE E VERIFICA (Ci aspettiamo un'eccezione!)
        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () -> {
            utentiService.effettuaLogin(emailErrata, "qualsiasiPass");
        });

        assertEquals("Errore: Nessun utente trovato con questa email.", eccezione.getMessage());
    }
}