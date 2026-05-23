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

        // FASE 2: Bilanciamento Dinamico
        Map<String, Double> caricoAttivoKg = new HashMap<>(); 
        Map<String, Long> numeroTaskAttivi = new HashMap<>(); 
        
        for (Utenti u : dipendentiAttivi) {
            Double kgAttivi = taskRepository.sumCaricoAttivoInKgPerDipendente(u.getEmail());
            caricoAttivoKg.put(u.getEmail(), kgAttivi != null ? kgAttivi : 0.0);
            numeroTaskAttivi.put(u.getEmail(), taskRepository.countTaskAttiviPerDipendente(u.getEmail()));
        }

        for (Task task : nuoviTask) { 
            float pesoUnitario = (task.getBatch_prodotti() != null && task.getBatch_prodotti().getProdotto() != null) 
                                 ? task.getBatch_prodotti().getProdotto().getPesoUnitario() : 0f;
            double pesoTask = task.getQta_spostata() * pesoUnitario;

            Utenti dipendenteScelto = dipendentiAttivi.stream()
                .min(Comparator.comparingDouble((Utenti u) -> caricoAttivoKg.get(u.getEmail())) // priorità: Kg
                        .thenComparingLong(u -> numeroTaskAttivi.get(u.getEmail()))) // tie-break: Num Task
                .orElse(dipendentiAttivi.get(0));

            // crea l'assegnazione
            TaskDip nuovaAssegnazione = new TaskDip(dipendenteScelto, task);
            taskDipRepository.save(nuovaAssegnazione);
            
            // Aggiornamento In Memoria
            caricoAttivoKg.put(dipendenteScelto.getEmail(), caricoAttivoKg.get(dipendenteScelto.getEmail()) + pesoTask);
            numeroTaskAttivi.put(dipendenteScelto.getEmail(), numeroTaskAttivi.get(dipendenteScelto.getEmail()) + 1L);
        }
    }

    public void assegnaMissioneInBlocco(List<Task> taskDellaMissione) {
        if (taskDellaMissione == null || taskDellaMissione.isEmpty()) {
            return;
        }

        List<Utenti> dipendentiAttivi = turniDipRepository.findDipendentiAttualmenteInTurno();
        
        if (dipendentiAttivi.isEmpty()) {
            throw new IllegalArgumentException("Nessun dipendente attualmente in turno per assegnare i task.");
        }

        // Calcola il peso totale della missione
        double pesoTotaleMissione = 0;
        for (Task task : taskDellaMissione) {
            float pesoUnitario = (task.getBatch_prodotti() != null && task.getBatch_prodotti().getProdotto() != null) 
                                 ? task.getBatch_prodotti().getProdotto().getPesoUnitario() : 0f;
            pesoTotaleMissione += (task.getQta_spostata() * pesoUnitario);
        }

        Map<String, Double> caricoAttivoKg = new HashMap<>(); 
        Map<String, Long> numeroTaskAttivi = new HashMap<>(); 
        
        for (Utenti u : dipendentiAttivi) {
            Double kgAttivi = taskRepository.sumCaricoAttivoInKgPerDipendente(u.getEmail());
            caricoAttivoKg.put(u.getEmail(), kgAttivi != null ? kgAttivi : 0.0);
            numeroTaskAttivi.put(u.getEmail(), taskRepository.countTaskAttiviPerDipendente(u.getEmail()));
        }

        // Trova il dipendente con il carico minore
        Utenti dipendenteScelto = dipendentiAttivi.stream()
            .min(Comparator.comparingDouble((Utenti u) -> caricoAttivoKg.get(u.getEmail()))
                    .thenComparingLong(u -> numeroTaskAttivi.get(u.getEmail())))
            .orElse(dipendentiAttivi.get(0));

        // Assegna tutti i task al dipendente scelto
        for (Task task : taskDellaMissione) {
            TaskDip nuovaAssegnazione = new TaskDip(dipendenteScelto, task);
            taskDipRepository.save(nuovaAssegnazione);
        }
        
        // Aggiornamento In Memoria (utile se il servizio mantenesse lo stato)
        caricoAttivoKg.put(dipendenteScelto.getEmail(), caricoAttivoKg.get(dipendenteScelto.getEmail()) + pesoTotaleMissione);
        numeroTaskAttivi.put(dipendenteScelto.getEmail(), numeroTaskAttivi.get(dipendenteScelto.getEmail()) + taskDellaMissione.size());
    }
}
