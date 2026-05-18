package com.ProgettoISA.WMS.Service;

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

import java.util.ArrayList;
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
    void testBilanciamentoAutomaticoTasks() {
        // PREPARAZIONE: 5 Dipendenti in turno
        List<Utenti> dipendenti = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Utenti u = new Utenti();
            u.setEmail("dipendente" + i + "@mail.com");
            dipendenti.add(u);
        }
        when(turniDipRepository.findDipendentiAttualmenteInTurno()).thenReturn(dipendenti);

        // All'inizio nessuno ha task attivi (tutti 0)
        when(taskRepository.countTaskAttiviPerDipendente(anyString())).thenReturn(0L);
        when(taskDipRepository.findMaxCompletedTaskIdByDipendenteEmail(anyString())).thenReturn(0L);

        // PREPARAZIONE: 20 task da assegnare
        List<Task> nuoviTask = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            Task t = new Task();
            t.setId((long) i);
            nuoviTask.add(t);
        }

        // ESECUZIONE
        taskAssignmentService.assegnaTasksAutomaticamente(nuoviTask);

        // VERIFICA: Catturiamo tutti i salvataggi
        ArgumentCaptor<TaskDip> captor = ArgumentCaptor.forClass(TaskDip.class);
        verify(taskDipRepository, times(20)).save(captor.capture());
        
        List<TaskDip> assegnazioni = captor.getAllValues();
        assertEquals(20, assegnazioni.size());

        // Contiamo quanti task sono stati assegnati a ciascun dipendente
        int[] conteggio = new int[6];
        for (TaskDip assegnazione : assegnazioni) {
            String email = assegnazione.getDipendente().getEmail();
            int id = Integer.parseInt(email.replace("dipendente", "").replace("@mail.com", ""));
            conteggio[id]++;
        }

        // Il bilanciamento perfetto per 20 task e 5 dipendenti è 4 task a testa!
        for (int i = 1; i <= 5; i++) {
            System.out.println("Dipendente " + i + " ha ricevuto " + conteggio[i] + " task.");
            assertEquals(4, conteggio[i], "Il bilanciamento non è corretto!");
        }
    }
}
