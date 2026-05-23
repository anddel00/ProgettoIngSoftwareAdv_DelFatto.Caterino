package com.ProgettoISA.WMS.Service;

import com.ProgettoISA.WMS.DTO.CreaTaskDTO;
import com.ProgettoISA.WMS.DTO.TaskDTO;
import com.ProgettoISA.WMS.Model.BatchProdotti;
import com.ProgettoISA.WMS.Model.Prodotti;
import com.ProgettoISA.WMS.Model.Task;
import com.ProgettoISA.WMS.Model.Utenti;
import com.ProgettoISA.WMS.Repository.BatchProdottiRepository;
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

import java.util.List;
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

    @Mock
    private BatchProdottiRepository batchProdottiRepository;

    @Mock
    private TaskAssignmentService taskAssignmentService;

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @InjectMocks
    private TaskService taskService;

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
        mockUser.setEmail("test@wms.it");
        mockUser.setNome("Mario");
        mockUser.setCognome("Rossi");

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

    // ==========================================
    // PROPRIETÀ 3: Fuzzing dei Pesi (Splitting Limitato a 500kg)
    // ==========================================
    @Property
    void splittingNonSuperaMaiLimitePeso(
            @ForAll @IntRange(min = 1, max = 100000) int quantitaCasuale,
            @ForAll @FloatRange(min = 0.1f, max = 500.0f) float pesoUnitario
    ) {
        // Arrange
        CreaTaskDTO dto = new CreaTaskDTO();
        dto.setDescrizione("Task Fuzzing");
        dto.setTipoTask("SPOSTAMENTO");
        dto.setQuantita(quantitaCasuale);
        dto.setEmailDipendente("test@wms.it");
        dto.setIdBatch(1L);

        BatchProdotti batch = new BatchProdotti();
        Prodotti prodotto = new Prodotti();
        prodotto.setPesoUnitario(pesoUnitario);
        batch.setProdotto(prodotto);

        when(batchProdottiRepository.findById(1L)).thenReturn(Optional.of(batch));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            saved.setId((long)(Math.random() * 10000));
            return saved;
        });

        // Simula la creazione dell'assegnazione nel TaskDipRepository per evitare NullPointerException nel DTO
        Utenti mockUser = new Utenti();
        mockUser.setEmail("test@wms.it");
        mockUser.setNome("Mario");
        mockUser.setCognome("Rossi");
        
        when(taskDipRepository.findByTask_Id(any())).thenAnswer(invocation -> {
            com.ProgettoISA.WMS.Model.TaskDip td = new com.ProgettoISA.WMS.Model.TaskDip();
            td.setTask(new Task());
            td.getTask().setId(invocation.getArgument(0));
            td.setDipendente(mockUser);
            return Optional.of(td);
        });

        // Act
        List<TaskDTO> result = taskService.creaEAssegnaMultipli(List.of(dto));

        // Assert
        assertNotNull(result);
        for (TaskDTO task : result) {
            float pesoTask = task.getQuantita() * pesoUnitario;
            assertTrue(pesoTask <= 500.0f, "Il peso del task non deve mai superare i 500kg. Trovato: " + pesoTask);
        }
    }

    // Fornitore di dati per le scelte a tendina
    @Provide
    Arbitrary<String> tipiTask() {
        return Arbitraries.of("PRELIEVO", "DEPOSITO", "SPOSTAMENTO");
    }
}