package com.ProgettoISA.WMS;

import com.ProgettoISA.WMS.Model.Ruoli;
import com.ProgettoISA.WMS.Model.Task;
import com.ProgettoISA.WMS.Model.TaskDip;
import com.ProgettoISA.WMS.Model.Utenti;
import com.ProgettoISA.WMS.Repository.RuoliRepository;
import com.ProgettoISA.WMS.Repository.TaskDipRepository;
import com.ProgettoISA.WMS.Repository.TaskRepository;
import com.ProgettoISA.WMS.Service.UtentiService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DatabaseTestRunner implements CommandLineRunner {

    private final RuoliRepository ruoliRepository;
    private final UtentiService utentiService;
    private final TaskRepository taskRepository;
    private final TaskDipRepository taskDipRepository;

    // Costruttore con tutti i repository iniettati
    public DatabaseTestRunner(RuoliRepository ruoliRepository, UtentiService utentiService,
                              TaskRepository taskRepository, TaskDipRepository taskDipRepository) {
        this.ruoliRepository = ruoliRepository;
        this.utentiService = utentiService;
        this.taskRepository = taskRepository;
        this.taskDipRepository = taskDipRepository;
    }

    @Override
    public void run(String... args) {
        System.out.println("⏳ Preparazione dati nel database...");

        // 1. Ruoli
        if (ruoliRepository.findByNomeRuolo("Admin").isEmpty()) {
            ruoliRepository.save(new Ruoli("Admin"));
            ruoliRepository.save(new Ruoli("Dipendente"));
        }

        // 2. Utenti
        try {
            Utenti adminTest = new Utenti("Mario", "Rossi", new Date(), "admin@wms.it", "admin123", null);
            utentiService.registraUtente(adminTest, "Admin");
        } catch (IllegalArgumentException e) { }

        Utenti dipendenteTest = null;
        try {
            dipendenteTest = new Utenti("Luigi", "Verdi", new Date(), "dipendente@wms.it", "user123", null);
            utentiService.registraUtente(dipendenteTest, "Dipendente");
            System.out.println("✅ Dipendente Luigi Verdi pronto all'azione!");
        } catch (IllegalArgumentException e) {
            System.out.println("ℹ️ Dipendente già presente.");
        }

        // 3. Creazione di un Task di Prova
        if (taskRepository.count() == 0 && dipendenteTest != null) {
            System.out.println("📦 Creazione Task di test...");

            // Usiamo il costruttore vuoto e i setter per semplicità (mettiamo null agli scaffali per ora)
            Task task1 = new Task();
            task1.setDescrizione("Prelievo iPhone 15 Pro Max");
            task1.setTipo_task("INBOUND");
            task1.setStato_task("DA_FARE");
            task1.setQta_spostata(2);
            // Salviamo il Task nella tabella TASK
            taskRepository.save(task1);

            // Creiamo il collegamento: Assegniamo il task1 a Luigi Verdi
            TaskDip assegnazione1 = new TaskDip(dipendenteTest, task1);
            taskDipRepository.save(assegnazione1);

            // Usiamo il costruttore vuoto e i setter per semplicità (mettiamo null agli scaffali per ora)
            Task task2 = new Task();
            task2.setDescrizione("Spostamento confezione surgelati");
            task2.setTipo_task("SPOSTAMENTO");
            task2.setStato_task("DA_FARE");
            task2.setQta_spostata(2);
            // Salviamo il Task nella tabella TASK
            taskRepository.save(task2);

            // Creiamo il collegamento: Assegniamo il task1 a Luigi Verdi
            TaskDip assegnazione2 = new TaskDip(dipendenteTest, task2);
            taskDipRepository.save(assegnazione2);

            System.out.println("🎯 Task assegnati con successo a Luigi Verdi!");
        }

        System.out.println("🚀 Database pronto e operativo!");
    }
}