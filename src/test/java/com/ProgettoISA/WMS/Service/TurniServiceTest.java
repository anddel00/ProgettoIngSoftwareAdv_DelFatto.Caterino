package com.ProgettoISA.WMS.Service;

import com.ProgettoISA.WMS.Model.Turni;
import com.ProgettoISA.WMS.Model.TurniDip;
import com.ProgettoISA.WMS.Model.Utenti;
import com.ProgettoISA.WMS.Repository.TaskRepository;
import com.ProgettoISA.WMS.Repository.TurniDipRepository;
import com.ProgettoISA.WMS.Repository.TurniRepository;
import com.ProgettoISA.WMS.Repository.UtentiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TurniServiceTest {

    @Mock
    private TurniDipRepository turniDipRepository;

    @Mock
    private UtentiRepository utentiRepository;

    @Mock
    private TurniRepository turniRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TurniService turniService;

    private Utenti dipendenteMock;

    @BeforeEach
    void setUp() {
        // Prepariamo un dipendente fittizio per i test
        dipendenteMock = new Utenti();
        dipendenteMock.setId(1L);
        dipendenteMock.setEmail("test@wms.it");
        dipendenteMock.setNome("Mario");
    }

    // ==========================================
    // TEST INIZIA TURNO
    // ==========================================
    @Test
    void iniziaTurno_Successo() {
        // Arrange (Prepariamo la situazione)
        when(utentiRepository.findByEmail("test@wms.it")).thenReturn(Optional.of(dipendenteMock));
        when(turniDipRepository.findDipendentiAttualmenteInTurno()).thenReturn(new ArrayList<>()); // Nessuno in turno
        when(turniRepository.findAll()).thenReturn(List.of(new Turni("08:00", "16:00")));

        // Act (Eseguiamo l'azione)
        assertDoesNotThrow(() -> turniService.iniziaTurno("test@wms.it"));

        // Assert (Verifichiamo che il DB sia stato chiamato per salvare)
        verify(turniDipRepository, times(1)).save(any(TurniDip.class));
    }

    @Test
    void iniziaTurno_FallisceSeGiaInTurno() {
        // Arrange
        when(utentiRepository.findByEmail("test@wms.it")).thenReturn(Optional.of(dipendenteMock));
        when(turniDipRepository.findDipendentiAttualmenteInTurno()).thenReturn(List.of(dipendenteMock)); // Il dipendente è già attivo!

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            turniService.iniziaTurno("test@wms.it");
        });

        assertEquals("Hai già un turno in corso!", exception.getMessage());
        verify(turniDipRepository, never()).save(any(TurniDip.class)); // Verifichiamo che NON salvi nulla
    }

    // ==========================================
    // TEST TERMINA TURNO
    // ==========================================
    @Test
    void terminaTurno_FallisceSeHaTaskAttivi() {
        // Arrange
        when(utentiRepository.findByEmail("test@wms.it")).thenReturn(Optional.of(dipendenteMock));
        when(taskRepository.countTaskAttiviPerDipendente("test@wms.it")).thenReturn(2L); // Ha 2 task pendenti!

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            turniService.terminaTurno("test@wms.it");
        });

        assertTrue(exception.getMessage().contains("Impossibile terminare il turno"));
        verify(turniDipRepository, never()).save(any(TurniDip.class));
    }

    @Test
    void terminaTurno_Successo() {
        // Arrange
        when(utentiRepository.findByEmail("test@wms.it")).thenReturn(Optional.of(dipendenteMock));
        when(taskRepository.countTaskAttiviPerDipendente("test@wms.it")).thenReturn(0L); // Nessun task

        TurniDip turnoAperto = new TurniDip(dipendenteMock, new Turni());
        turnoAperto.setOraInizioReale(LocalDateTime.now().minusHours(4)); // Iniziato 4 ore fa
        when(turniDipRepository.findTurniApertiByEmail("test@wms.it")).thenReturn(List.of(turnoAperto));

        // Act
        assertDoesNotThrow(() -> turniService.terminaTurno("test@wms.it"));

        // Assert
        assertNotNull(turnoAperto.getOraFineReale()); // Verifichiamo che l'ora di fine sia stata impostata
        verify(turniDipRepository, times(1)).save(turnoAperto);
    }
}