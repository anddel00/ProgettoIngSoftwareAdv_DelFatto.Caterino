package com.ProgettoISA.WMS.Controller;

import com.ProgettoISA.WMS.DTO.AdminStoricoTaskDTO;
import com.ProgettoISA.WMS.DTO.CreaTaskDTO;
import com.ProgettoISA.WMS.DTO.RiprogrammaTaskDTO;
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
    // 1B. CREA MULTIPLI TASK E ASSEGNALI AUTOMATICAMENTE
    // POST /api/tasks/crea-e-assegna-multipli
    // ==========================================
    @PostMapping("/crea-e-assegna-multipli")
    public ResponseEntity<?> creaEAssegnaMultipli(@RequestBody List<CreaTaskDTO> dtos) {
        try {
            List<TaskDTO> tasksCreati = taskService.creaEAssegnaMultipli(dtos);
            return ResponseEntity.ok(tasksCreati);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Errore nella creazione e assegnazione dei task.");
        }
    }

    // ==========================================
    // 1C. RIPROGRAMMAZIONE VISUALE TASK
    // POST /api/tasks/riprogramma
    // ==========================================
    @PostMapping("/riprogramma")
    public ResponseEntity<?> riprogrammaTask(@RequestBody RiprogrammaTaskDTO dto) {
        try {
            List<TaskDTO> tasksCreati = taskService.riprogrammaTask(dto);
            return ResponseEntity.ok(tasksCreati);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Errore nella riprogrammazione del task.");
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
                AdminStoricoTaskDTO dto = new AdminStoricoTaskDTO(
                        td.getTask().getId(),
                        td.getTask().getDescrizione(),
                        td.getTask().getTipo_task(),
                        td.getTask().getQta_spostata(),
                        nomeCompleto
                );

                // Popolamento campi percorso (scaffale partenza + destinazione + coordinate)
                if (td.getTask().getScaffale_inizio() != null) {
                    dto.setIdScaffaleInizio(td.getTask().getScaffale_inizio().getId());
                }
                if (td.getTask().getScaffale_fine() != null) {
                    dto.setIdScaffaleFine(td.getTask().getScaffale_fine().getId());
                }
                dto.setVecchiaX(td.getTask().getVecchia_x());
                dto.setVecchiaY(td.getTask().getVecchia_y());
                dto.setVecchiaZ(td.getTask().getVecchia_z());
                dto.setNuovaX(td.getTask().getNuova_x());
                dto.setNuovaY(td.getTask().getNuova_y());
                dto.setNuovaZ(td.getTask().getNuova_z());

                // Reparto: scaffale_inizio ha priorità, fallback su scaffale_fine
                if (td.getTask().getScaffale_inizio() != null && td.getTask().getScaffale_inizio().getReparto() != null) {
                    dto.setNomeReparto(td.getTask().getScaffale_inizio().getReparto().getNome());
                } else if (td.getTask().getScaffale_fine() != null && td.getTask().getScaffale_fine().getReparto() != null) {
                    dto.setNomeReparto(td.getTask().getScaffale_fine().getReparto().getNome());
                }

                storico.add(dto);

            }

            return ResponseEntity.ok(storico);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==========================================
    // 6. GET /api/tasks/attivi
    // Ottiene solo i task NON completati (Per GestioneTask.vue)
    // ==========================================
    @GetMapping("/attivi")
    public ResponseEntity<List<TaskDTO>> getTaskAttiviAdmin() {
        try {
            return ResponseEntity.ok(taskService.getTaskAttiviAdmin());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==========================================
    // 6-BIS. GET /api/tasks/attivi/reparto/{id}
    // LAZY LOADING: Task attivi che coinvolgono il reparto specificato
    // ==========================================
    @GetMapping("/attivi/reparto/{id}")
    public ResponseEntity<List<TaskDTO>> getTaskAttiviPerReparto(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(taskService.getTaskAttiviPerReparto(id));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    // ==========================================
    // 7. ANNULLA UN TASK ATTIVO (CANCELLAZIONE)
    // DELETE /api/tasks/{id}/annulla
    // ==========================================
    @DeleteMapping("/{id}/annulla")
    public ResponseEntity<?> annullaTask(@PathVariable Long id) {
        try {
            taskService.annullaTask(id);
            return ResponseEntity.ok("Task annullato e rimosso con successo.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Errore nell'annullamento del task.");
        }
    }

}