package com.ProgettoISA.WMS.Service;

import com.ProgettoISA.WMS.DTO.CreaTaskDTO;
import com.ProgettoISA.WMS.DTO.TaskDTO;
import com.ProgettoISA.WMS.Model.Task;
import com.ProgettoISA.WMS.Model.Utenti;
import com.ProgettoISA.WMS.Repository.TaskDipRepository;
import com.ProgettoISA.WMS.Repository.TaskRepository;
import com.ProgettoISA.WMS.Repository.UtentiRepository;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.BeforeTry;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TaskServicePbtTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskDipRepository taskDipRepository;

    @Mock
    private UtentiRepository utentiRepository;

    @InjectMocks
    private TaskService taskService;

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @BeforeTry
    void setUp() {
        // Inizializza i finti repository di Mockito prima di ogni generazione
        MockitoAnnotations.openMocks(this);
    }

    // ==========================================
    // PROPRIETÀ 1: Resilienza sui dati corretti
    // Genera 1000 combinazioni di descrizioni e quantità
    // ==========================================
    @Property
    void taskValidoMantieneSempreIDatiIntatti(
            @ForAll @AlphaChars @StringLength(min = 1, max = 100) String descrizioneCasuale,
            @ForAll("tipiTask") String tipoCasuale,
            @ForAll @IntRange(min = 1, max = 10000) int quantitaCasuale
    ) {
        // Arrange
        CreaTaskDTO dto = new CreaTaskDTO();
        dto.setDescrizione(descrizioneCasuale);
        dto.setTipoTask(tipoCasuale);
        dto.setQuantita(quantitaCasuale);
        dto.setEmailDipendente("test@wms.it");

        Utenti mockUser = new Utenti();
        Task mockTaskSalvato = new Task();
        mockTaskSalvato.setId(99L);
        mockTaskSalvato.setDescrizione(descrizioneCasuale);
        mockTaskSalvato.setTipo_task(tipoCasuale);
        mockTaskSalvato.setQta_spostata(quantitaCasuale);
        mockTaskSalvato.setStato_task("DA_FARE");

        when(utentiRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(taskRepository.save(any(Task.class))).thenReturn(mockTaskSalvato);

        // Act
        TaskDTO result = taskService.creaEAssegna(dto);

        // Assert: Il DTO restituito DEVE avere gli stessi dati generati da Jqwik
        assertNotNull(result);
        assertEquals(descrizioneCasuale, result.getDescrizione());
        assertEquals(quantitaCasuale, result.getQuantita());
        assertEquals("DA_FARE", result.getStatoTask(), "I nuovi task DEVONO nascere 'DA_FARE'");
    }

    // ==========================================
    // PROPRIETÀ 2: Blocco totale sulle quantità negative o zero
    // Genera 1000 numeri casuali da -2.147.483.648 a 0
    // ==========================================
    @Property
    void rifiutaSempreQuantitaNegativeOZero(
            @ForAll @IntRange(min = Integer.MIN_VALUE, max = 0) int quantitaInvalida
    ) {
        // Arrange
        CreaTaskDTO dto = new CreaTaskDTO();
        dto.setDescrizione("Task hacker");
        dto.setTipoTask("PRELIEVO");
        dto.setQuantita(quantitaInvalida); // Jqwik inietta il numero negativo
        dto.setEmailDipendente("test@wms.it");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.creaEAssegna(dto);
        });

        assertTrue(exception.getMessage().contains("maggiore di zero"));

        // Assicuriamoci che il DB non venga MAI chiamato per salvare
        verify(taskRepository, never()).save(any());
    }

    // Fornitore di dati per le scelte a tendina
    @Provide
    Arbitrary<String> tipiTask() {
        return Arbitraries.of("PRELIEVO", "DEPOSITO", "SPOSTAMENTO");
    }
}