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
            throw new IllegalArgumentException("Nessun dipendente attualmente in turno per assegnare i task.");
        }

        // Recupera il carico iniziale di lavoro (1 singola query per ogni dipendente, invece di N)
        Map<String, Long> caricoDiLavoro = new HashMap<>(); //quanti task "DA_FARE" ha attualmente il dipendente?
        Map<String, Long> ultimiTaskCompletati = new HashMap<>(); //qual è l'id dell'ultimo task che ha completato?
        
        for (Utenti u : dipendentiAttivi) {
            caricoDiLavoro.put(u.getEmail(), taskRepository.countTaskAttiviPerDipendente(u.getEmail()));
            ultimiTaskCompletati.put(u.getEmail(), getLastCompletedTaskId(u.getEmail()));
        }

        for (Task task : nuoviTask) { //scorre la lista dei nuovi task da smistare e per ogni singolo task:
            // Per ogni dipendente, cerca chi ha meno task (usando i dati in memoria)
            Utenti dipendenteScelto = dipendentiAttivi.stream()
                .min(Comparator.comparingLong((Utenti u) -> caricoDiLavoro.get(u.getEmail())) //scorre tutti i dipendenti attivi alla ricerca di quello con il punteggio minore (quello più "scarico")
                                                                                             //criterio 1: guardando l'hashmap in ram, chi ha il numero più basso di task vince
                        .thenComparingLong(u -> ultimiTaskCompletati.get(u.getEmail())))//criterio 2: se c'è un pareggio, va a vedere chi ha il task id completato più grande. In questo modo l'algoritmo fa riposare chi ha lavorato per ultimo
                .orElse(dipendentiAttivi.get(0)); //

            // crea l'assegnazione
            TaskDip nuovaAssegnazione = new TaskDip(dipendenteScelto, task);//una volta trovato il dipendente giusto, si crea il legame tra questo e il task (tramite TaskDip) e lo salva nel DB.
            taskDipRepository.save(nuovaAssegnazione);
            
            // aggiorna il carico di lavoro IN MEMORIA per il bilanciamento del prossimo ciclo
            caricoDiLavoro.put(dipendenteScelto.getEmail(), caricoDiLavoro.get(dipendenteScelto.getEmail()) + 1);
        }
    }

    private long getLastCompletedTaskId(String email) {
        // Ritorna l'ID più grande tra i task completati. Se non ne ha, ritorna 0 (priorità massima per averne).
        Long lastId = taskDipRepository.findMaxCompletedTaskIdByDipendenteEmail(email);
        return lastId != null ? lastId : 0L;
    }
}
