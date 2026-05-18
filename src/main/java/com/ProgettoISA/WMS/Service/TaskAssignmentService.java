package com.ProgettoISA.WMS.Service;

import com.ProgettoISA.WMS.Model.Task;
import com.ProgettoISA.WMS.Model.TaskDip;
import com.ProgettoISA.WMS.Model.Utenti;
import com.ProgettoISA.WMS.Repository.TaskDipRepository;
import com.ProgettoISA.WMS.Repository.TaskRepository;
import com.ProgettoISA.WMS.Repository.TurniDipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskAssignmentService {

    @Autowired
    private TurniDipRepository turniDipRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskDipRepository taskDipRepository;

    public void assegnaTasksAutomaticamente(List<Task> nuoviTask) {
        // Trova tutti i dipendenti attivi
        List<Utenti> dipendentiAttivi = turniDipRepository.findDipendentiAttualmenteInTurno();
        
        if (dipendentiAttivi.isEmpty()) {
            System.err.println("Nessun dipendente attualmente in turno per assegnare i task.");
            return;
        }

        // Recupera il carico iniziale di lavoro (1 singola query per ogni dipendente, invece di N)
        Map<String, Long> caricoDiLavoro = new HashMap<>();
        Map<String, Long> ultimiTaskCompletati = new HashMap<>();
        
        for (Utenti u : dipendentiAttivi) {
            caricoDiLavoro.put(u.getEmail(), taskRepository.countTaskAttiviPerDipendente(u.getEmail()));
            ultimiTaskCompletati.put(u.getEmail(), getLastCompletedTaskId(u.getEmail()));
        }

        for (Task task : nuoviTask) {
            // Per ogni dipendente, cerca chi ha meno task (usando i dati in memoria)
            Utenti dipendenteScelto = dipendentiAttivi.stream()
                .min(Comparator.comparingLong((Utenti u) -> caricoDiLavoro.get(u.getEmail()))
                        .thenComparingLong(u -> ultimiTaskCompletati.get(u.getEmail())))
                .orElse(dipendentiAttivi.get(0));

            // Crea l'assegnazione
            TaskDip nuovaAssegnazione = new TaskDip(dipendenteScelto, task);
            taskDipRepository.save(nuovaAssegnazione);
            
            // Aggiorna il carico di lavoro IN MEMORIA per il bilanciamento del prossimo ciclo!
            caricoDiLavoro.put(dipendenteScelto.getEmail(), caricoDiLavoro.get(dipendenteScelto.getEmail()) + 1);
        }
    }

    private long getLastCompletedTaskId(String email) {
        // Ritorna l'ID più grande tra i task completati. Se non ne ha, ritorna 0 (priorità massima per averne).
        Long lastId = taskDipRepository.findMaxCompletedTaskIdByDipendenteEmail(email);
        return lastId != null ? lastId : 0L;
    }
}
