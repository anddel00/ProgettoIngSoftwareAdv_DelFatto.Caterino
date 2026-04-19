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
import org.springframework.messaging.simp.SimpMessagingTemplate; // <-- IMPORT NECESSARIO
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskDipRepository taskDipRepository;

    @Autowired
    private UtentiRepository utentiRepository;

    // IL NOSTRO "POSTINO" WEBSOCKET
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

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

        // 🚀 WEBSOCKET (Dentro creaEAssegna)
        String canalePrivato = "/queue/tasks/" + dipendente.getEmail().trim().toLowerCase();
        messagingTemplate.convertAndSend(canalePrivato, responseDto);

        // 🚀 WEBSOCKET: Invia notifica pubblica (Topic) per far aggiornare le card dell'Admin
        messagingTemplate.convertAndSend("/topic/tasks", responseDto);

        return responseDto;
    }

    // ==========================================
    // --- HELPER PRIVATO PER MAPPARE I DTO ---
    // ==========================================
    private TaskDTO convertiInDTO(TaskDip td) {
        Task t = td.getTask();
        Utenti dipendente = td.getDipendente();
        String nomeCompleto = dipendente.getNome() + " " + dipendente.getCognome();

        return new TaskDTO(
                t.getId(),
                t.getDescrizione(),
                t.getTipo_task(),
                t.getStato_task(),
                t.getQta_spostata(),
                nomeCompleto
        );
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
                    "Non Assegnato" // <-- Il sesto parametro mancante!
            );
        }

        // 🚀 WEBSOCKET: Invia l'aggiornamento sul canale globale
        messagingTemplate.convertAndSend("/topic/tasks", responseDto);

        // 🚀 WEBSOCKET (Dentro aggiornaStato)
        if (assegnazione != null) {
            String canalePrivato = "/queue/tasks/" + assegnazione.getDipendente().getEmail().trim().toLowerCase();
            messagingTemplate.convertAndSend(canalePrivato, responseDto);
        }

        return responseDto;
    }
}