package com.ProgettoISA.WMS.Util;

import java.util.Date;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ProgettoISA.WMS.Model.Ruoli;
import com.ProgettoISA.WMS.Model.Utenti;
import com.ProgettoISA.WMS.Repository.RuoliRepository;
import com.ProgettoISA.WMS.Service.UtentiService;

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
        System.out.println("⏳ Inizializzazione dati database...");

        // 1. Gestione Ruoli con salvataggio sicuro
        Ruoli adminRole = ruoliRepository.findByNomeRuolo("Admin")
                .orElseGet(() -> ruoliRepository.save(new Ruoli("Admin")));
        
        if (ruoliRepository.findByNomeRuolo("Dipendente").isEmpty()) {
            ruoliRepository.save(new Ruoli("Dipendente"));
        }

        // 2. Creazione Utente Admin tramite Service (che gestisce BCrypt)
        try {
            Utenti admin = new Utenti();
            admin.setNome("Mario");
            admin.setUsername("Admin");
            admin.setCognome("Rossi");
            admin.setData_nascita(new Date());
            admin.setEmail("admin@wms.it");
            
            // Passiamo l'oggetto admin e la password in chiaro. Il service farà l'hash.
            utentiService.registraUtente(admin, adminRole.getNomeRuolo());
            
            System.out.println("✅ Database pronto. Admin: admin@wms.it / admin123");
        } catch (Exception e) {
            // Se l'email esiste già, il service lancerà un'eccezione che catturiamo qui
            System.out.println("ℹ️ Database già popolato o utente presente.");
        }
    }
}