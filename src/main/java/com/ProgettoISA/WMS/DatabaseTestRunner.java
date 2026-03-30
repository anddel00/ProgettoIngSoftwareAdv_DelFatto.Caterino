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

        if (ruoliRepository.findByNomeRuolo("Admin").isEmpty()) {
            ruoliRepository.save(new Ruoli("Admin"));
            ruoliRepository.save(new Ruoli("Dipendente"));
            System.out.println("✅ Ruoli creati!");
        }

        try {
            Utenti adminTest = new Utenti("Mario", "Rossi", new Date(), "admin@wms.it", "admin123", null);
            utentiService.registraUtente(adminTest, "Admin");
            System.out.println("✅ Admin creato! Email: admin@wms.it");
            //paswword hash
            System.out.println("   🔒 Hash salvato: " + adminTest.getPassword());
        } catch (IllegalArgumentException e) {
            System.out.println("ℹ️ Admin già presente.");
        }

        try {
            Utenti dipendenteTest = new Utenti("Luigi", "Verdi", new Date(), "dipendente@wms.it", "user123", null);
            utentiService.registraUtente(dipendenteTest, "Dipendente");
            System.out.println("✅ Dipendente creato! Email: dipendente@wms.it");
       //password hash
            System.out.println("   🔒 Hash salvato: " + dipendenteTest.getPassword());
        } catch (IllegalArgumentException e) {
            System.out.println("ℹ️ Dipendente già presente.");
        }

        System.out.println("🚀 Database pronto!");
    }
}