package com.ProgettoISA.WMS.Controller;

import com.ProgettoISA.WMS.DTO.AdminStoricoTaskDTO;
import com.ProgettoISA.WMS.DTO.CreaTaskDTO;
import com.ProgettoISA.WMS.DTO.TaskDTO;
import com.ProgettoISA.WMS.Model.TaskDip;
import com.ProgettoISA.WMS.Repository.TaskDipRepository;
import com.ProgettoISA.WMS.Service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskDipRepository taskDipRepository;

    // ==========================================
    // 1. CREA UN TASK E ASSEGNALO A UN DIPENDENTE
    // POST /api/tasks/crea-e-assegna
    // ==========================================
    @PostMapping("/crea-e-assegna")
    public ResponseEntity<?> creaEAssegna(@RequestBody CreaTaskDTO dto) {
        try {
            TaskDTO taskCreato = taskService.creaEAssegna(dto);
            return ResponseEntity.ok(taskCreato);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Errore nella creazione del task.");
        }
    }

    // ==========================================
    // 2. TASK DI UN DIPENDENTE (per homepage dipendente)
    // GET /api/tasks/miei-task?email=...
    // ==========================================
    @GetMapping("/miei-task")
    public ResponseEntity<List<TaskDTO>> getMieiTask(@RequestParam String email) {
        try {
            return ResponseEntity.ok(taskService.getTaskPerDipendente(email));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==========================================
    // 3. AGGIORNA STATO TASK (per il dipendente)
    // PATCH /api/tasks/{id}/stato?nuovoStato=...
    // ==========================================
    @PatchMapping("/{id}/stato")
    public ResponseEntity<?> aggiornaStatoTask(@PathVariable Long id, @RequestParam String nuovoStato) {
        try {
            TaskDTO aggiornato = taskService.aggiornaStato(id, nuovoStato);
            return ResponseEntity.ok(aggiornato);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==========================================
    // 4. TUTTI I TASK (per la dashboard admin)
    // GET /api/tasks/tutti
    // ==========================================
    @GetMapping("/tutti")
    public ResponseEntity<List<TaskDTO>> getTuttiTaskAdmin() {
        try {
            return ResponseEntity.ok(taskService.getTuttiTask());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==========================================
    // 5. STORICO TASK COMPLETATI (per l'admin)
    // GET /api/tasks/storico-admin
    // ==========================================
    @GetMapping("/storico-admin")
    public ResponseEntity<List<AdminStoricoTaskDTO>> getStoricoAdmin() {
        try {
            List<TaskDip> assegnazioniCompletate = taskDipRepository.findStoricoCompletati();
            List<AdminStoricoTaskDTO> storico = new ArrayList<>();

            for (TaskDip td : assegnazioniCompletate) {
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
}