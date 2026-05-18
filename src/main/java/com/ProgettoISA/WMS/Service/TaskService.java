package com.ProgettoISA.WMS.Service;

import com.ProgettoISA.WMS.DTO.CreaTaskDTO;
import com.ProgettoISA.WMS.DTO.TaskDTO;
import com.ProgettoISA.WMS.Model.Task;
import com.ProgettoISA.WMS.Model.TaskDip;
import com.ProgettoISA.WMS.Model.Utenti;
import com.ProgettoISA.WMS.Repository.TaskDipRepository;
import com.ProgettoISA.WMS.Repository.TaskRepository;
import com.ProgettoISA.WMS.Repository.UtentiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskAssignmentService taskAssignmentService;

    @Autowired
    private com.ProgettoISA.WMS.Repository.TaskDipRepository taskDipRepository;

    @Autowired
    private com.ProgettoISA.WMS.Repository.UtentiRepository utentiRepository;

    @Autowired
    private com.ProgettoISA.WMS.Repository.BatchProdottiRepository batchProdottiRepository;

    @Autowired
    private com.ProgettoISA.WMS.Repository.MappaRepository mappaRepository;

    @Autowired
    private BatchScaffaleService batchScaffaleService;

    // IL NOSTRO "POSTINO" WEBSOCKET
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    // ==========================================
    // 1. CREA UN TASK E ASSEGNALO A UN DIPENDENTE
    // ==========================================
    @Transactional
    public TaskDTO creaEAssegna(CreaTaskDTO dto) {
        if (dto.getQuantita() <= 0) {
            throw new IllegalArgumentException("Errore di sicurezza: La quantità deve essere maggiore di zero.");
        }

        Utenti dipendente = utentiRepository.findByEmail(dto.getEmailDipendente())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Nessun dipendente trovato con email: " + dto.getEmailDipendente()));

        Task nuovoTask = new Task();
        nuovoTask.setDescrizione(dto.getDescrizione());
        nuovoTask.setTipo_task(dto.getTipoTask());
        nuovoTask.setQta_spostata(dto.getQuantita());
        nuovoTask.setStato_task("DA_FARE");
        nuovoTask.setVecchia_x(dto.getVecchiaX());
        nuovoTask.setVecchia_y(dto.getVecchiaY());
        nuovoTask.setNuova_x(dto.getNuovaX());
        nuovoTask.setNuova_y(dto.getNuovaY());

        Task taskSalvato = taskRepository.save(nuovoTask);

        TaskDip assegnazione = new TaskDip(dipendente, taskSalvato);
        taskDipRepository.save(assegnazione);

        // Usiamo l'helper in modo che il DTO abbia anche il nome del dipendente!
        TaskDTO responseDto = convertiInDTO(assegnazione);

        // 🚀 WEBSOCKET: Invia notifica privata al palmarino
        if (dipendente.getEmail() != null) {
            String canalePrivato = "/queue/tasks/" + dipendente.getEmail().trim().toLowerCase();
            messagingTemplate.convertAndSend(canalePrivato, responseDto);
        }

        // 🚀 WEBSOCKET: Invia notifica pubblica
        messagingTemplate.convertAndSend("/topic/tasks", responseDto);

        return responseDto;
    }

    // ==========================================
    // 1B. CREA MULTIPLI TASK E ASSEGNALI AUTOMATICAMENTE
    // ==========================================
    @Transactional
    public List<TaskDTO> creaEAssegnaMultipli(List<CreaTaskDTO> dtos) {
        List<Task> nuoviTask = new java.util.ArrayList<>();

        // Crea fisicamente i task
        for (CreaTaskDTO dto : dtos) {
            if (dto.getQuantita() <= 0) {
                throw new IllegalArgumentException("Errore di sicurezza: La quantità deve essere maggiore di zero.");
            }
            Task nuovoTask = new Task();
            nuovoTask.setDescrizione(dto.getDescrizione());
            nuovoTask.setTipo_task(dto.getTipoTask());
            nuovoTask.setQta_spostata(dto.getQuantita());
            nuovoTask.setStato_task("DA_FARE");
            nuovoTask.setVecchia_x(dto.getVecchiaX());
            nuovoTask.setVecchia_y(dto.getVecchiaY());
            nuovoTask.setVecchia_z(dto.getVecchiaZ());
            nuovoTask.setNuova_x(dto.getNuovaX());
            nuovoTask.setNuova_y(dto.getNuovaY());
            nuovoTask.setNuova_z(dto.getNuovaZ());
            
            // Collega le entità se fornite
            if (dto.getIdBatch() != null) {
                nuovoTask.setBatch_prodotti(batchProdottiRepository.findById(dto.getIdBatch()).orElse(null));
            }
            if (dto.getIdScaffaleInizio() != null) {
                nuovoTask.setScaffale_inizio(mappaRepository.findById(dto.getIdScaffaleInizio()).orElse(null));
            }
            if (dto.getIdScaffaleFine() != null) {
                nuovoTask.setScaffale_fine(mappaRepository.findById(dto.getIdScaffaleFine()).orElse(null));
            }

            nuoviTask.add(taskRepository.save(nuovoTask));
        }

        // Assegna automaticamente
        taskAssignmentService.assegnaTasksAutomaticamente(nuoviTask);

        // Prepariamo i DTO di risposta recuperando le assegnazioni appena fatte
        List<TaskDTO> responses = new java.util.ArrayList<>();
        for (Task task : nuoviTask) {
            TaskDip assegnazione = taskDipRepository.findByTask_Id(task.getId()).orElse(null);
            if (assegnazione != null) {
                TaskDTO dto = convertiInDTO(assegnazione);
                responses.add(dto);

                // 🚀 WEBSOCKET: Invia notifica privata al dipendente scelto
                if (assegnazione.getDipendente().getEmail() != null) {
                    String canalePrivato = "/queue/tasks/" + assegnazione.getDipendente().getEmail().trim().toLowerCase();
                    messagingTemplate.convertAndSend(canalePrivato, dto);
                }
                // 🚀 WEBSOCKET: Invia notifica pubblica
                messagingTemplate.convertAndSend("/topic/tasks", dto);
            }
        }
        return responses;
    }

    // ==========================================
    // --- HELPER PRIVATO PER MAPPARE I DTO ---
    // ==========================================
    private TaskDTO convertiInDTO(TaskDip td) {
        Task t = td.getTask();
        Utenti dipendente = td.getDipendente();
        String nomeCompleto = dipendente.getNome() + " " + dipendente.getCognome();

        TaskDTO dto = new TaskDTO(
                t.getId(),
                t.getDescrizione(),
                t.getTipo_task(),
                t.getStato_task(),
                t.getQta_spostata(),
                nomeCompleto
        );
        
        if (t.getBatch_prodotti() != null) dto.setIdBatch(t.getBatch_prodotti().getId());
        if (t.getScaffale_inizio() != null) dto.setIdScaffaleInizio(t.getScaffale_inizio().getId());
        if (t.getScaffale_fine() != null) dto.setIdScaffaleFine(t.getScaffale_fine().getId());
        dto.setNuovaX(t.getNuova_x());
        dto.setNuovaY(t.getNuova_y());
        dto.setNuovaZ(t.getNuova_z());
        
        return dto;
    }

    // ==========================================
    // 2A. OTTIENI SOLO I TASK ATTIVI (Per GestioneTask.vue)
    // ==========================================
    public List<TaskDTO> getTaskAttiviAdmin() {
        return taskDipRepository.findTaskAttivi().stream()
                .map(this::convertiInDTO)
                .collect(Collectors.toList());
    }

    // ==========================================
    // 2B. OTTIENI TUTTI I TASK (Per StoricoAdmin.vue)
    // ==========================================
    public List<TaskDTO> getTuttiTask() {
        return taskDipRepository.findAll().stream()
                .map(this::convertiInDTO)
                .collect(Collectors.toList());
    }

    // ==========================================
    // 3. OTTIENI I TASK DI UN DIPENDENTE (per la homepage dipendente)
    // ==========================================
    public List<TaskDTO> getTaskPerDipendente(String email) {
        return taskDipRepository.findAll().stream()
                .filter(td -> td.getDipendente().getEmail().equals(email))
                .map(this::convertiInDTO)
                .collect(Collectors.toList());
    }

    // ==========================================
    // 4. AGGIORNA LO STATO DI UN TASK
    // ==========================================
    @Transactional
    public TaskDTO aggiornaStato(Long id, String nuovoStato) {
        // 1. Aggiorniamo il task fisico
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task non trovato con id: " + id));

        task.setStato_task(nuovoStato);
        Task taskAggiornato = taskRepository.save(task);
        
        // --- NOVITÀ: Se il task è completato, aggiorno lo scaffale fisico! ---
        if ("COMPLETATO".equals(nuovoStato) && taskAggiornato.getBatch_prodotti() != null) {
            try {
                // 1. Se era uno spostamento o prelievo da scaffale, rimuovo dalla vecchia cella
                if (taskAggiornato.getScaffale_inizio() != null && taskAggiornato.getQta_spostata() > 0) {
                    List<com.ProgettoISA.WMS.DTO.BatchScaffaleDTO> syncRemove = new java.util.ArrayList<>();
                    // Troviamo l'ID del record BatchScaffale (che possiamo dedurre o lasciare al BatchScaffaleService)
                    // Purtroppo non abbiamo id del BatchScaffale vecchio.
                    // Fortunatamente, BatchScaffaleService.sincronizzaBatch con ID null cerca il record se passiamo -qta!
                    // Ma BatchScaffaleService ha bisogno di QTA POSITIVA e ID.
                    // Aspetta, BatchScaffaleService: CASO A INSERIMENTO, se trova esistente fa +qta. 
                    // Se passiamo quantità NEGATIVA e Id nullo?
                    // "esistente.setQta(esistente.getQta() + dto.getQta());"
                    // Funzionerebbe! Proviamo ad aggiungere l'operazione di decremento.
                    syncRemove.add(new com.ProgettoISA.WMS.DTO.BatchScaffaleDTO(
                            null, 
                            taskAggiornato.getScaffale_inizio().getId(),
                            taskAggiornato.getBatch_prodotti().getId(),
                            taskAggiornato.getVecchia_x(),
                            taskAggiornato.getVecchia_y(),
                            taskAggiornato.getVecchia_z(),
                            -taskAggiornato.getQta_spostata() // Quantità NEGATIVA per rimuovere
                    ));
                    batchScaffaleService.sincronizzaBatch(syncRemove);
                }
                
                // 2. Se è un deposito o spostamento, aggiungo alla nuova cella
                if (taskAggiornato.getScaffale_fine() != null && taskAggiornato.getQta_spostata() > 0) {
                    List<com.ProgettoISA.WMS.DTO.BatchScaffaleDTO> syncAdd = new java.util.ArrayList<>();
                    syncAdd.add(new com.ProgettoISA.WMS.DTO.BatchScaffaleDTO(
                            null, 
                            taskAggiornato.getScaffale_fine().getId(),
                            taskAggiornato.getBatch_prodotti().getId(),
                            taskAggiornato.getNuova_x(),
                            taskAggiornato.getNuova_y(),
                            taskAggiornato.getNuova_z(),
                            taskAggiornato.getQta_spostata()
                    ));
                    batchScaffaleService.sincronizzaBatch(syncAdd);
                }
            } catch (Exception e) {
                System.err.println("Errore aggiornamento fisico dello scaffale: " + e.getMessage());
            }
        }

        // 2. Cerchiamo l'assegnazione in modo efficiente usando il database!
        TaskDip assegnazione = taskDipRepository.findByTask_Id(id).orElse(null);

        // 3. Prepariamo il DTO da spedire
        TaskDTO responseDto;
        if (assegnazione != null) {
            responseDto = convertiInDTO(assegnazione);
        } else {
            // Se per qualche motivo il task non ha assegnazione, usiamo un fallback sicuro
            responseDto = new TaskDTO(
                    taskAggiornato.getId(),
                    taskAggiornato.getDescrizione(),
                    taskAggiornato.getTipo_task(),
                    taskAggiornato.getStato_task(),
                    taskAggiornato.getQta_spostata(),
                    "Non Assegnato"
            );
            if (taskAggiornato.getBatch_prodotti() != null) responseDto.setIdBatch(taskAggiornato.getBatch_prodotti().getId());
            if (taskAggiornato.getScaffale_inizio() != null) responseDto.setIdScaffaleInizio(taskAggiornato.getScaffale_inizio().getId());
            if (taskAggiornato.getScaffale_fine() != null) responseDto.setIdScaffaleFine(taskAggiornato.getScaffale_fine().getId());
            responseDto.setNuovaX(taskAggiornato.getNuova_x());
            responseDto.setNuovaY(taskAggiornato.getNuova_y());
            responseDto.setNuovaZ(taskAggiornato.getNuova_z());
        }

        // 🚀 WEBSOCKET: Invia aggiornamento globale
        messagingTemplate.convertAndSend("/topic/tasks", responseDto);

        // 🚀 WEBSOCKET: Invia sul canale privato (se assegnato e se ha una email)
        if (assegnazione != null && assegnazione.getDipendente().getEmail() != null) {
            String canalePrivato = "/queue/tasks/" + assegnazione.getDipendente().getEmail().trim().toLowerCase();
            messagingTemplate.convertAndSend(canalePrivato, responseDto);
        }

        return responseDto;
    }
}