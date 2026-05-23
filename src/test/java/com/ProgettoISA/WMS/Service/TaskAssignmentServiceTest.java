package com.ProgettoISA.WMS.Service;

import com.ProgettoISA.WMS.Model.BatchProdotti;
import com.ProgettoISA.WMS.Model.Prodotti;
import com.ProgettoISA.WMS.Model.Task;
import com.ProgettoISA.WMS.Model.TaskDip;
import com.ProgettoISA.WMS.Model.Utenti;
import com.ProgettoISA.WMS.Repository.TaskDipRepository;
import com.ProgettoISA.WMS.Repository.TaskRepository;
import com.ProgettoISA.WMS.Repository.TurniDipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class TaskAssignmentServiceTest {

    @Mock
    private TurniDipRepository turniDipRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskDipRepository taskDipRepository;

    @InjectMocks
    private TaskAssignmentService taskAssignmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testBilanciamentoAutomaticoTasksInKg() {
        // Arrange
        Utenti u1 = new Utenti(); u1.setEmail("u1@test.com");
        Utenti u2 = new Utenti(); u2.setEmail("u2@test.com");
        Utenti u3 = new Utenti(); u3.setEmail("u3@test.com");

        when(turniDipRepository.findDipendentiAttualmenteInTurno()).thenReturn(Arrays.asList(u1, u2, u3));
        
        when(taskRepository.sumCaricoAttivoInKgPerDipendente("u1@test.com")).thenReturn(0.0);
        when(taskRepository.sumCaricoAttivoInKgPerDipendente("u2@test.com")).thenReturn(300.0);
        when(taskRepository.sumCaricoAttivoInKgPerDipendente("u3@test.com")).thenReturn(600.0);

        when(taskRepository.countTaskAttiviPerDipendente("u1@test.com")).thenReturn(0L);
        when(taskRepository.countTaskAttiviPerDipendente("u2@test.com")).thenReturn(1L);
        when(taskRepository.countTaskAttiviPerDipendente("u3@test.com")).thenReturn(2L);

        BatchProdotti batch = new BatchProdotti();
        Prodotti prodotto = new Prodotti();
        prodotto.setPesoUnitario(10.0f); // 10 kg a pezzo
        batch.setProdotto(prodotto);

        Task t1 = new Task(); t1.setQta_spostata(10); t1.setBatch_prodotti(batch); // 100 kg
        Task t2 = new Task(); t2.setQta_spostata(10); t2.setBatch_prodotti(batch); // 100 kg
        Task t3 = new Task(); t3.setQta_spostata(10); t3.setBatch_prodotti(batch); // 100 kg
        Task t4 = new Task(); t4.setQta_spostata(10); t4.setBatch_prodotti(batch); // 100 kg

        // Act
        taskAssignmentService.assegnaTasksAutomaticamente(Arrays.asList(t1, t2, t3, t4));

        // Assert
        ArgumentCaptor<TaskDip> captor = ArgumentCaptor.forClass(TaskDip.class);
        verify(taskDipRepository, times(4)).save(captor.capture());
        
        List<TaskDip> assegnazioni = captor.getAllValues();
        
        // Ordine di assegnazione atteso:
        // Task 1: a u1 (ora u1 ha 100kg, 1 task)
        assertEquals("u1@test.com", assegnazioni.get(0).getDipendente().getEmail());
        // Task 2: a u1 (ora u1 ha 200kg, 2 tasks)
        assertEquals("u1@test.com", assegnazioni.get(1).getDipendente().getEmail());
        // Task 3: a u1 (ora u1 ha 300kg, 3 tasks). Ora è in parità di Kg con u2.
        assertEquals("u1@test.com", assegnazioni.get(2).getDipendente().getEmail());
        // Task 4: u1 ha 300kg e 3 tasks, u2 ha 300kg e 1 task.
        // Tie-break sul numero di task: vince chi ne ha meno (u2).
        assertEquals("u2@test.com", assegnazioni.get(3).getDipendente().getEmail());
    }
}
