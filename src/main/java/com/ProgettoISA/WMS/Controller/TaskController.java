package com.ProgettoISA.WMS.Controller;

import com.ProgettoISA.WMS.DTO.AdminStoricoTaskDTO;
import com.ProgettoISA.WMS.DTO.TaskDTO;
import com.ProgettoISA.WMS.Model.Task;
import com.ProgettoISA.WMS.Model.TaskDip;
import com.ProgettoISA.WMS.Repository.TaskRepository;
import com.ProgettoISA.WMS.Repository.TaskDipRepository;
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
    @Autowired
    private TaskDipRepository taskDipRepository;

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

    @PatchMapping("/{id}/stato")
    public ResponseEntity<?> aggiornaStatoTask(@PathVariable Long id, @RequestParam String nuovoStato) {
        try {
            // 1. Cerchiamo il task nel database tramite il suo ID
            java.util.Optional<Task> taskOpt = taskRepository.findById(id);

            if (taskOpt.isEmpty()) {
                return ResponseEntity.notFound().build(); // Errore 404 se il task non esiste
            }

            // 2. Estraiamo il task e modifichiamo lo stato
            Task task = taskOpt.get();
            task.setStato_task(nuovoStato);

            // 3. Salviamo la modifica nel database
            taskRepository.save(task);

            // 4. Rispondiamo al frontend con un messaggio di successo in formato JSON
            return ResponseEntity.ok(java.util.Map.of("message", "Stato aggiornato con successo a: " + nuovoStato));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build(); // Errore 500 in caso di problemi
        }
    }

        @GetMapping("/storico-admin")
        public ResponseEntity<List<AdminStoricoTaskDTO>> getStoricoAdmin () {
            try {
                List<TaskDip> assegnazioniCompletate = taskDipRepository.findStoricoCompletati();
                List<AdminStoricoTaskDTO> storico = new ArrayList<>();

                for (TaskDip td : assegnazioniCompletate) {
                    // Uniamo nome e cognome del dipendente
                    String nomeCompleto = td.getDipendente().getNome() + " " + td.getDipendente().getCognome();

                    storico.add(new AdminStoricoTaskDTO(
                            td.getTask().getId(),
                            td.getTask().getDescrizione(),
                            td.getTask().getTipo_task(),
                            td.getTask().getQta_spostata(),
                            nomeCompleto
                    ));
                }

                return ResponseEntity.ok(storico);
            } catch (Exception e) {
                return ResponseEntity.internalServerError().build();
            }
        }

    @GetMapping("/tutti")
    public ResponseEntity<List<TaskDTO>> getTuttiTaskAdmin() {
        try {
            // Recuperiamo tutti i task dal database
            List<Task> tuttiTasks = taskRepository.findAll();

            // Li convertiamo nel nostro DTO leggero
            List<TaskDTO> taskDTOs = new ArrayList<>();
            for (Task t : tuttiTasks) {
                taskDTOs.add(new TaskDTO(
                        t.getId(),
                        t.getDescrizione(),
                        t.getTipo_task(), // INBOUND, OUTBOUND, SPOSTAMENTO
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




