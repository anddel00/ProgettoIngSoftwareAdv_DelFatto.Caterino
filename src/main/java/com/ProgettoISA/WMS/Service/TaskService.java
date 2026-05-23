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
import com.ProgettoISA.WMS.DTO.RiprogrammaTaskDTO;

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
    // 1. CREA UN TASK E ASSEGNALO A UN DIPENDENTE // questo qui è quello manuale, lo lascio perché è ancora possibile assegnare un task manualmente (il frontend non è stato toccato)
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

        // Crea fisicamente i task gestendo lo splitting
        for (CreaTaskDTO dto : dtos) {
            if (dto.getQuantita() <= 0) {
                throw new IllegalArgumentException("Errore di sicurezza: La quantità deve essere maggiore di zero.");
            }

            com.ProgettoISA.WMS.Model.BatchProdotti batch = null;
            if (dto.getIdBatch() != null) {
                batch = batchProdottiRepository.findById(dto.getIdBatch()).orElse(null);
            }

            float pesoUnitario = (batch != null && batch.getProdotto() != null) ? batch.getProdotto().getPesoUnitario() : 0f;
            int qtaDaSpostare = dto.getQuantita();

            // FASE 1: Generazione e Spezzamento dei Task
            while (qtaDaSpostare > 0) {
                int qta_max = qtaDaSpostare;
                if (pesoUnitario > 0) {
                    // Calcolo pezzi massimi per 500 kg
                    int pezziMax = (int) Math.floor(500.0 / pesoUnitario);
                    if (pezziMax > 0 && pezziMax < qtaDaSpostare) {
                        qta_max = pezziMax;
                    }
                }

                Task nuovoTask = new Task();
                nuovoTask.setDescrizione(dto.getDescrizione());
                nuovoTask.setTipo_task(dto.getTipoTask());
                nuovoTask.setQta_spostata(qta_max);
                nuovoTask.setStato_task("DA_FARE");
                nuovoTask.setVecchia_x(dto.getVecchiaX());
                nuovoTask.setVecchia_y(dto.getVecchiaY());
                nuovoTask.setVecchia_z(dto.getVecchiaZ());
                nuovoTask.setNuova_x(dto.getNuovaX());
                nuovoTask.setNuova_y(dto.getNuovaY());
                nuovoTask.setNuova_z(dto.getNuovaZ());

                // Collega le entità se fornite
                nuovoTask.setBatch_prodotti(batch);
                if (dto.getIdScaffaleInizio() != null) {
                    nuovoTask.setScaffale_inizio(mappaRepository.findById(dto.getIdScaffaleInizio()).orElse(null));
                }
                if (dto.getIdScaffaleFine() != null) {
                    nuovoTask.setScaffale_fine(mappaRepository.findById(dto.getIdScaffaleFine()).orElse(null));
                }
                
                nuovoTask.setIdMissione(dto.getIdMissione());

                nuoviTask.add(taskRepository.save(nuovoTask));

                qtaDaSpostare -= qta_max;
            }
        }

        // Assegna automaticamente: raggruppa per missione
        java.util.Map<String, List<Task>> tasksByMission = nuoviTask.stream()
            .collect(Collectors.groupingBy(t -> t.getIdMissione() != null ? t.getIdMissione() : "SINGOLI"));

        for (java.util.Map.Entry<String, List<Task>> entry : tasksByMission.entrySet()) {
            if ("SINGOLI".equals(entry.getKey())) {
                taskAssignmentService.assegnaTasksAutomaticamente(entry.getValue());
            } else {
                taskAssignmentService.assegnaMissioneInBlocco(entry.getValue());
            }
        }


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
        dto.setIdMissione(t.getIdMissione());
        
        if (t.getBatch_prodotti() != null) {
            dto.setIdBatch(t.getBatch_prodotti().getId());
            dto.setIdLottoOrigine(t.getBatch_prodotti().getIdLottoOrigine());
        }
        if (t.getScaffale_inizio() != null) dto.setIdScaffaleInizio(t.getScaffale_inizio().getId());
        if (t.getScaffale_fine() != null) dto.setIdScaffaleFine(t.getScaffale_fine().getId());

        // Reparto: preferisce scaffale_inizio, fallback su scaffale_fine
        if (t.getScaffale_inizio() != null && t.getScaffale_inizio().getReparto() != null) {
            dto.setNomeReparto(t.getScaffale_inizio().getReparto().getNome());
        } else if (t.getScaffale_fine() != null && t.getScaffale_fine().getReparto() != null) {
            dto.setNomeReparto(t.getScaffale_fine().getReparto().getNome());
        }

        dto.setNuovaX(t.getNuova_x());
        dto.setNuovaY(t.getNuova_y());
        dto.setNuovaZ(t.getNuova_z());
        dto.setVecchiaX(t.getVecchia_x());
        dto.setVecchiaY(t.getVecchia_y());
        dto.setVecchiaZ(t.getVecchia_z());
        
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
    // 2A-BIS. OTTIENI TASK ATTIVI FILTRATI PER REPARTO (Per MappaMagazzino lazy loading)
    // ==========================================
    public List<TaskDTO> getTaskAttiviPerReparto(Long idReparto) {
        return taskDipRepository.findTaskAttiviPerReparto(idReparto).stream()
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
    @Transactional // è transactional perché se la collocazione fisica dovesse fallire (es. c'è meno merce di quella che si vuole sottrarre), il database applica il rollback e fa tornare il task in stato "da fare".
    public TaskDTO aggiornaStato(Long id, String nuovoStato) {
        // 1. Aggiorniamo il task fisico
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task non trovato con id: " + id));

        task.setStato_task(nuovoStato);
        Task taskAggiornato = taskRepository.save(task);
        
        // --- NOVITÀ: Se il task è completato, aggiorno lo scaffale fisico! ---
        if ("COMPLETATO".equals(nuovoStato) && taskAggiornato.getBatch_prodotti() != null) {
            
            // --- NOVITÀ FEFO e VENDITE ---
            if ("PRELIEVO".equals(taskAggiornato.getTipo_task())) {
                taskAggiornato.getBatch_prodotti().setStatoLotto("IN_ATTESA");
                batchProdottiRepository.save(taskAggiornato.getBatch_prodotti());
            } else if ("USCITA".equals(taskAggiornato.getTipo_task())) {
                taskAggiornato.getBatch_prodotti().setStatoLotto("VENDUTO");
                batchProdottiRepository.save(taskAggiornato.getBatch_prodotti());
            } else if ("DEPOSITO".equals(taskAggiornato.getTipo_task())) {
                taskAggiornato.getBatch_prodotti().setStatoLotto("IN_MAGAZZINO");
                batchProdottiRepository.save(taskAggiornato.getBatch_prodotti());
            }

            try {
                // 1. Se era uno spostamento o prelievo da scaffale, rimuovo dalla vecchia cella
                if (taskAggiornato.getScaffale_inizio() != null && taskAggiornato.getQta_spostata() > 0) {
                    List<com.ProgettoISA.WMS.DTO.BatchScaffaleDTO> syncRemove = new java.util.ArrayList<>();
                    //se c'era uno "scaffale_inizio", significa che stiamo togliendo merce. Il sistema crea un BatchScaffale DTO con quantità negativa
                    //creo quindi un BatchScaffaleDTO con quantità spostata negativa. Se questo viene passato al BatchScaffaleService, questo andrà a trovare i pacchi e sottrarrà la quantità (se arriva a zero, si svuota)
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
                //se invece c'è uno scaffale_fine, crea un DTO con quantità positiva -> lo invia al batchScaffaleService, il quale piazzerà fisicamente il lotto nello scaffale (mappa) corrispondente.
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
            if (taskAggiornato.getBatch_prodotti() != null) {
                responseDto.setIdBatch(taskAggiornato.getBatch_prodotti().getId());
                responseDto.setIdLottoOrigine(taskAggiornato.getBatch_prodotti().getIdLottoOrigine());
            }
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

    // ==========================================
    // 5. ANNULLA TASK (CANCELLAZIONE FISICA)
    // ==========================================
    @Transactional
    public void annullaTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task non trovato."));

        if ("COMPLETATO".equals(task.getStato_task())) {
            throw new IllegalArgumentException("Impossibile annullare un task già completato.");
        }

        // Cerca assegnazione per notificare ed eliminare
        TaskDip assegnazione = taskDipRepository.findByTask_Id(taskId).orElse(null);
        String dipendenteEmail = null;
        if (assegnazione != null) {
            if (assegnazione.getDipendente() != null) {
                dipendenteEmail = assegnazione.getDipendente().getEmail();
            }
            taskDipRepository.delete(assegnazione);
        }

        // Elimina fisicamente il task
        taskRepository.delete(task);

        // Notifica WebSocket per il frontend e il palmarino
        TaskDTO taskAnnullatoDto = new TaskDTO(taskId, "Task Annullato", "ANNULLATO", "ANNULLATO", 0);
        
        if (dipendenteEmail != null) {
            String canalePrivato = "/queue/tasks/" + dipendenteEmail.trim().toLowerCase();
            messagingTemplate.convertAndSend(canalePrivato, taskAnnullatoDto);
        }
        messagingTemplate.convertAndSend("/topic/tasks", taskAnnullatoDto);
    }

    // ==========================================
    // 6. RIPROGRAMMAZIONE VISUALE DEL TASK
    // ==========================================
    @Transactional(rollbackFor = Exception.class)
    public List<TaskDTO> riprogrammaTask(RiprogrammaTaskDTO dto) {
        // 1. Recuperare il task originale dal DB
        Task taskOriginale = taskRepository.findById(dto.getIdTaskOriginale())
                .orElseThrow(() -> new IllegalArgumentException("Task originale non trovato con id: " + dto.getIdTaskOriginale()));

        if ("COMPLETATO".equals(taskOriginale.getStato_task()) || "ANNULLATO".equals(taskOriginale.getStato_task())) {
            throw new IllegalArgumentException("Impossibile riprogrammare un task completato o già annullato.");
        }

        // 2. Costruire il DTO di creazione con i dati originali
        CreaTaskDTO nuovoCreaTaskDTO = new CreaTaskDTO();
        nuovoCreaTaskDTO.setDescrizione(taskOriginale.getDescrizione() + " (Riprogrammato)");
        nuovoCreaTaskDTO.setTipoTask(taskOriginale.getTipo_task());
        nuovoCreaTaskDTO.setQuantita(taskOriginale.getQta_spostata());
        
        if (taskOriginale.getBatch_prodotti() != null) {
            nuovoCreaTaskDTO.setIdBatch(taskOriginale.getBatch_prodotti().getId());
        }
        
        if (taskOriginale.getScaffale_inizio() != null) {
            nuovoCreaTaskDTO.setIdScaffaleInizio(taskOriginale.getScaffale_inizio().getId());
            nuovoCreaTaskDTO.setVecchiaX(taskOriginale.getVecchia_x());
            nuovoCreaTaskDTO.setVecchiaY(taskOriginale.getVecchia_y());
            nuovoCreaTaskDTO.setVecchiaZ(taskOriginale.getVecchia_z());
        }

        // 3. Impostare la nuova destinazione fornita dal dto
        nuovoCreaTaskDTO.setIdScaffaleFine(dto.getIdNuovoScaffale());
        nuovoCreaTaskDTO.setNuovaX(dto.getNuovaX());
        nuovoCreaTaskDTO.setNuovaY(dto.getNuovaY());
        nuovoCreaTaskDTO.setNuovaZ(dto.getNuovaZ());

        // 4. Annullare fisicamente/logicamente il task originale
        annullaTask(taskOriginale.getId());

        // 5. Creare il nuovo task passando dalla logica di splitting e assegnamento
        return creaEAssegnaMultipli(List.of(nuovoCreaTaskDTO));
    }
}