package com.ProgettoISA.WMS.Service;

import com.ProgettoISA.WMS.DTO.CreaTaskDTO;
import com.ProgettoISA.WMS.DTO.TaskDTO;
import com.ProgettoISA.WMS.Model.BatchProdotti;
import com.ProgettoISA.WMS.Model.Prodotti;
import com.ProgettoISA.WMS.Model.Task;
import com.ProgettoISA.WMS.Repository.BatchProdottiRepository;
import com.ProgettoISA.WMS.Repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskAssignmentService taskAssignmentService;

    @Mock
    private com.ProgettoISA.WMS.Repository.TaskDipRepository taskDipRepository;

    @Mock
    private BatchProdottiRepository batchProdottiRepository;

    @Mock
    private org.springframework.messaging.simp.SimpMessageSendingOperations messagingTemplate;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreaEAssegnaMultipli_Splitting() {
        // Arrange
        CreaTaskDTO dto = new CreaTaskDTO();
        dto.setQuantita(600); // 600 pezzi da 2 kg = 1200 kg
        dto.setIdBatch(1L);

        BatchProdotti batch = new BatchProdotti();
        Prodotti prodotto = new Prodotti();
        prodotto.setPesoUnitario(2.0f);
        batch.setProdotto(prodotto);

        when(batchProdottiRepository.findById(1L)).thenReturn(Optional.of(batch));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setId((long) (Math.random() * 1000));
            return t;
        });

        // Act
        taskService.creaEAssegnaMultipli(List.of(dto));

        // Assert
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository, times(3)).save(taskCaptor.capture());
        
        List<Task> savedTasks = taskCaptor.getAllValues();
        assertEquals(3, savedTasks.size());
        assertEquals(250, savedTasks.get(0).getQta_spostata()); // 500 kg
        assertEquals(250, savedTasks.get(1).getQta_spostata()); // 500 kg
        assertEquals(100, savedTasks.get(2).getQta_spostata()); // 200 kg
    }
}
