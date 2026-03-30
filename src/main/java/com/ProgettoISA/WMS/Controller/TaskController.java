package com.ProgettoISA.WMS.Controller;

import com.ProgettoISA.WMS.DTO.TaskDTO;
import com.ProgettoISA.WMS.Model.Task;
import com.ProgettoISA.WMS.Repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "http://localhost:5173") // Permette a Vue di comunicare con Spring
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @GetMapping("/miei-task")
    public ResponseEntity<List<TaskDTO>> getMieiTask(@RequestParam String email) {
        try {
            // 1. Trova i task originali tramite l'email del dipendente
            List<Task> taskAssegnati = taskRepository.findTasksByDipendenteEmail(email);

            // 2. Convertili nel formato DTO sicuro per il frontend
            List<TaskDTO> taskDTOs = new ArrayList<>();
            for (Task t : taskAssegnati) {
                taskDTOs.add(new TaskDTO(
                        t.getId(),
                        t.getDescrizione(),
                        t.getTipo_task(),
                        t.getStato_task(),
                        t.getQta_spostata()
                ));
            }

            return ResponseEntity.ok(taskDTOs);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}