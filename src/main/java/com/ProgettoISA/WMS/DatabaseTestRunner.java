package com.ProgettoISA.WMS;

import com.ProgettoISA.WMS.Model.Ruoli;
import com.ProgettoISA.WMS.Model.Utenti;
import com.ProgettoISA.WMS.Repository.RuoliRepository;
import com.ProgettoISA.WMS.Service.UtentiService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DatabaseTestRunner implements CommandLineRunner {

    private final RuoliRepository ruoliRepository;
    private final UtentiService utentiService;

    public DatabaseTestRunner(RuoliRepository ruoliRepository, UtentiService utentiService) {
        this.ruoliRepository = ruoliRepository;
        this.utentiService = utentiService;
    }

    @Override
    public void run(String... args) {
        System.out.println("⏳ Preparazione dati nel database H2...");

        // 1. Creiamo i ruoli se non esistono
        if (ruoliRepository.findByNomeRuolo("Admin").isEmpty()) {
            ruoliRepository.save(new Ruoli("Admin"));
            ruoliRepository.save(new Ruoli("Dipendente"));
            System.out.println("✅ Ruoli 'Admin' e 'Dipendente' creati!");
        }

        // 2. Creiamo l'utente Admin per il test
        try {
            Utenti adminTest = new Utenti("Mario", "Rossi", new Date(), "admin@wms.it", "admin123", null);
            utentiService.registraUtente(adminTest, "Admin");
            System.out.println("✅ Utente Admin creato! Usa queste credenziali:");
            System.out.println("   Email: admin@wms.it");
            System.out.println("   Pass:  admin123");
        } catch (IllegalArgumentException e) {
            System.out.println("ℹ️ Utente di test già presente.");
        }
    }
}