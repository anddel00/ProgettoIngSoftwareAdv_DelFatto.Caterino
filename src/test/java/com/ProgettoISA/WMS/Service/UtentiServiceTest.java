package com.ProgettoISA.WMS.Service;

import com.ProgettoISA.WMS.Model.Ruoli;
import com.ProgettoISA.WMS.Model.Utenti;
import com.ProgettoISA.WMS.Repository.RuoliRepository;
import com.ProgettoISA.WMS.Repository.UtentiRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
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

    @Mock
    private RuoliRepository ruoliRepository;

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

    // ==========================================
    // TEST CRUD: CREATE (Registrazione)
    // ==========================================
    @Test
    void testRegistraUtente_Successo() {
        Utenti nuovoUtente = new Utenti();
        nuovoUtente.setEmail("nuovo@test.it");
        nuovoUtente.setPassword("pass123");

        Ruoli ruolo = new Ruoli();
        ruolo.setNomeRuolo("Dipendente");

        when(utentiRepository.existsByEmail("nuovo@test.it")).thenReturn(false);
        when(ruoliRepository.findByNomeRuolo("Dipendente")).thenReturn(Optional.of(ruolo));
        when(passwordEncoder.encode("pass123")).thenReturn("hashed_pass");
        when(utentiRepository.save(any(Utenti.class))).thenReturn(nuovoUtente);

        Utenti salvato = utentiService.registraUtente(nuovoUtente, "Dipendente");

        assertNotNull(salvato);
        assertEquals("hashed_pass", salvato.getPassword());
        verify(utentiRepository, times(1)).save(nuovoUtente);
    }

    @Test
    void testRegistraUtente_EmailGiaEsistente() {
        Utenti utente = new Utenti();
        utente.setEmail("esistente@test.it");

        when(utentiRepository.existsByEmail("esistente@test.it")).thenReturn(true);

        IllegalArgumentException eccezione = assertThrows(IllegalArgumentException.class, () -> {
            utentiService.registraUtente(utente, "Dipendente");
        });
        assertEquals("Errore: L'email inserita è già in uso!", eccezione.getMessage());
    }

    // ==========================================
    // TEST CRUD: READ (Ottieni Tutti)
    // ==========================================
    @Test
    void testOttieniTuttiUtenti() {
        List<Utenti> fintiUtenti = List.of(new Utenti(), new Utenti());
        when(utentiRepository.findAll()).thenReturn(fintiUtenti);

        List<Utenti> risultato = utentiService.ottieniTuttiUtenti();

        assertEquals(2, risultato.size());
        verify(utentiRepository, times(1)).findAll();
    }

    // ==========================================
    // TEST CRUD: UPDATE (Modifica)
    // ==========================================
    @Test
    void testModificaUtente_Successo() {
        Long id = 1L;
        Utenti utenteEsistente = new Utenti();
        utenteEsistente.setNome("VecchioNome");

        Utenti datiAggiornati = new Utenti();
        datiAggiornati.setNome("NuovoNome");
        datiAggiornati.setPassword("nuovaPass");

        when(utentiRepository.findById(id)).thenReturn(Optional.of(utenteEsistente));
        when(passwordEncoder.encode("nuovaPass")).thenReturn("nuova_hashed");
        when(utentiRepository.save(any(Utenti.class))).thenReturn(utenteEsistente);

        Utenti modificato = utentiService.modificaUtente(id, datiAggiornati);

        assertEquals("NuovoNome", modificato.getNome());
        assertEquals("nuova_hashed", modificato.getPassword());
    }

    // ==========================================
    // TEST CRUD: DELETE (Elimina)
    // ==========================================
    @Test
    void testEliminaUtente_Successo() {
        Long id = 1L;
        when(utentiRepository.existsById(id)).thenReturn(true);

        utentiService.eliminaUtente(id);

        // Verifichiamo che il metodo deleteById sia stato chiamato esattamente 1 volta
        verify(utentiRepository, times(1)).deleteById(id);
    }
}