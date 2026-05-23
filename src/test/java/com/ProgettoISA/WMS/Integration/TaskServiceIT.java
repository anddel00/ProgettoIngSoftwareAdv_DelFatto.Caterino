package com.ProgettoISA.WMS.Integration;

import com.ProgettoISA.WMS.DTO.RiprogrammaTaskDTO;
import com.ProgettoISA.WMS.Model.Task;
import com.ProgettoISA.WMS.Repository.TaskRepository;
import com.ProgettoISA.WMS.Service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("test")
public class TaskServiceIT {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void testRiprogrammaTask_RollbackTransazionale() {
        // Arrange
        Task taskOriginale = new Task();
        taskOriginale.setDescrizione("Task Originale");
        taskOriginale.setTipo_task("SPOSTAMENTO");
        taskOriginale.setQta_spostata(0); // Forza un'eccezione nella fase successiva
        taskOriginale.setStato_task("DA_FARE");
        taskOriginale = taskRepository.save(taskOriginale);

        RiprogrammaTaskDTO dto = new RiprogrammaTaskDTO();
        dto.setIdTaskOriginale(taskOriginale.getId());
        dto.setIdNuovoScaffale(9999L); 

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            taskService.riprogrammaTask(dto);
        });

        // Verify that original task was NOT deleted due to @Transactional rollback
        assertTrue(taskRepository.findById(taskOriginale.getId()).isPresent(), "Il task originale non deve essere stato cancellato dal database a causa del rollback transazionale.");
    }
}
