package com.ProgettoISA.WMS.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ProgettoISA.WMS.Model.EtProd;
import com.ProgettoISA.WMS.Model.Etichette;
import com.ProgettoISA.WMS.Model.Mappa;
import com.ProgettoISA.WMS.Model.Prodotti;
import com.ProgettoISA.WMS.Model.Reparti;
import com.ProgettoISA.WMS.Model.Ruoli;
import com.ProgettoISA.WMS.Model.Scaffali;
import com.ProgettoISA.WMS.Model.Utenti;
import com.ProgettoISA.WMS.Repository.EtProdRepository;
import com.ProgettoISA.WMS.Repository.EtichetteRepository;
import com.ProgettoISA.WMS.Repository.MappaRepository;
import com.ProgettoISA.WMS.Repository.ProdottiRepository;
import com.ProgettoISA.WMS.Repository.RepartiRepository;
import com.ProgettoISA.WMS.Repository.RuoliRepository;
import com.ProgettoISA.WMS.Repository.ScaffaliRepository;
import com.ProgettoISA.WMS.Service.UtentiService;


@Component
public class DatabaseTestRunner implements CommandLineRunner {

    private final RuoliRepository ruoliRepository;
    private final UtentiService utentiService;
    private final ProdottiRepository prodottiRepository;
    private final RepartiRepository repartiRepository;
    private final ScaffaliRepository scaffaliRepository;
    private final MappaRepository mappaRepository;
    private final EtichetteRepository etichetteRepository;
    private final EtProdRepository etProdRepository;
    public DatabaseTestRunner(
        RuoliRepository ruoliRepository, 
        UtentiService utentiService, 
        ProdottiRepository prodottiRepository, 
        RepartiRepository repartiRepository,
        ScaffaliRepository scaffaliRepository,
        MappaRepository mappaRepository,
        EtichetteRepository etichetteRepository,
        EtProdRepository etProdRepository
    ) 
    {
        this.ruoliRepository = ruoliRepository;
        this.utentiService = utentiService;
        this.prodottiRepository = prodottiRepository;
        this.repartiRepository = repartiRepository;
        this.scaffaliRepository = scaffaliRepository;
        this.mappaRepository = mappaRepository;
        this.etichetteRepository = etichetteRepository;
        this.etProdRepository = etProdRepository;
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


    // 2-4. Gestione Unificata: Etichette, Prodotti e Relazioni (Approccio "Annidato")
    if (prodottiRepository.findAll().isEmpty() && etichetteRepository.findAll().isEmpty()) {
        
        System.out.println("⏳ Creazione Prodotti, Etichette e Relazioni...");

        // creazione e salvataggio etichette (per avere ID da usare nelle relazioni)
        Etichette surgelato = etichetteRepository.save(new Etichette("Surgelato"));
        Etichette fresco = etichetteRepository.save(new Etichette("Fresco"));
        Etichette vetro = etichetteRepository.save(new Etichette("Vetro"));
        Etichette fragile = etichetteRepository.save(new Etichette("Fragile"));
        Etichette alimentare = etichetteRepository.save(new Etichette("Alimentare"));

        List<Prodotti> listaProdotti = new ArrayList<>();
        List<EtProd> listaRelazioni = new ArrayList<>();
        
        // Variabile d'appoggio che ricicliamo per ogni prodotto
        Prodotti p;

        // --- SURGELATI ---
        p = new Prodotti("Gelato al Cioccolato", 2L, 0.5f, 4.50f, -25L, -15L);
        listaProdotti.add(p);
        listaRelazioni.add(new EtProd(p, surgelato));
        listaRelazioni.add(new EtProd(p, alimentare));

        p = new Prodotti("Gelato alla Vaniglia", 2L, 0.5f, 4.00f, -25L, -15L);
        listaProdotti.add(p);
        listaRelazioni.add(new EtProd(p, surgelato));
        listaRelazioni.add(new EtProd(p, alimentare));

        p = new Prodotti("Gelato alla Fragola", 2L, 0.5f, 4.20f, -25L, -15L);
        listaProdotti.add(p);
        listaRelazioni.add(new EtProd(p, surgelato));
        listaRelazioni.add(new EtProd(p, alimentare));

        p = new Prodotti("Gelato al Pistacchio", 2L, 0.5f, 4.80f, -25L, -15L);
        listaProdotti.add(p);
        listaRelazioni.add(new EtProd(p, surgelato));
        listaRelazioni.add(new EtProd(p, alimentare));

        // --- FRESCHI ---
        p = new Prodotti("Latte Intero", 1L, 1.0f, 1.80f, 2L, 6L); // 1 litro/kg
        listaProdotti.add(p);
        listaRelazioni.add(new EtProd(p, fresco));
        listaRelazioni.add(new EtProd(p, alimentare));

        p = new Prodotti("Yogurt alla Frutta", 1L, 0.15f, 0.90f, 2L, 6L); // vasetto da 150g
        listaProdotti.add(p);
        listaRelazioni.add(new EtProd(p, fresco));
        listaRelazioni.add(new EtProd(p, alimentare));
        listaRelazioni.add(new EtProd(p, vetro));
        listaRelazioni.add(new EtProd(p, fragile));

        p = new Prodotti("Formaggio", 1L, 0.4f, 3.50f, 2L, 6L); // trancio da 400g
        listaProdotti.add(p);
        listaRelazioni.add(new EtProd(p, fresco));
        listaRelazioni.add(new EtProd(p, alimentare));

        p = new Prodotti("Prosciutto Cotto", 1L, 0.12f, 4.00f, 2L, 6L); // vaschetta da 120g
        listaProdotti.add(p);
        listaRelazioni.add(new EtProd(p, fresco));
        listaRelazioni.add(new EtProd(p, alimentare));

        // --- SECCHI ---
        p = new Prodotti("Spaghetti n.5", 1L, 0.5f, 1.20f, 10L, 30L); // pacco da 500g
        listaProdotti.add(p);
        listaRelazioni.add(new EtProd(p, alimentare));

        p = new Prodotti("Riso Arborio", 1L, 1.0f, 2.50f, 10L, 30L); // pacco da 1kg
        listaProdotti.add(p);
        listaRelazioni.add(new EtProd(p, alimentare));

        p = new Prodotti("Passata di Pomodoro", 1L, 0.7f, 0.80f, 10L, 30L); // bottiglia da 700g
        listaProdotti.add(p);
        listaRelazioni.add(new EtProd(p, alimentare));
        listaRelazioni.add(new EtProd(p, vetro));
        listaRelazioni.add(new EtProd(p, fragile));

        p = new Prodotti("Olio Extra Vergine", 1L, 1.0f, 5.00f, 10L, 30L); // bottiglia da 1 litro/kg
        listaProdotti.add(p);
        listaRelazioni.add(new EtProd(p, alimentare));
        listaRelazioni.add(new EtProd(p, vetro));
        listaRelazioni.add(new EtProd(p, fragile));

        // salvataggio in batch (prima prodotti per avere ID, poi relazioni)
        prodottiRepository.saveAll(listaProdotti);
        etProdRepository.saveAll(listaRelazioni);

        System.out.println("✅ Prodotti, Etichette e Relazioni inizializzati con successo!");
    }


    // 5. Gestione Reparti con le dimensioni originali
    if(repartiRepository.findAll().isEmpty()) {
        List<Reparti> repartiIniziali = List.of(
            new Reparti(15L, 15L, -20L, "Surgelati"), // Dispari -> Corridoio 3
            new Reparti(20L, 20L, 3L, "Fresco"),      // Pari -> Corridoio 2
            new Reparti(30L, 30L, 15L, "Secco")       // Pari -> Corridoio 2
        );

        repartiRepository.saveAll(repartiIniziali);
        System.out.println("✅ Reparti inizializzati con successo!");
        
        // 6 & 7. Generazione Automatica Scaffali e Mappa (Griglia Simmetrica Dinamica)
        if(scaffaliRepository.findAll().isEmpty() && mappaRepository.findAll().isEmpty()) {
            
            List<Mappa> mappeDaSalvare = new ArrayList<>();
            int spazioVerticale = 1;
            int altezzaScaffaleY = 1; // Occupazione sulla Y

            for (Reparti reparto : repartiIniziali) {
                
                // IL TUO ALGORITMO: Calcolo dinamico del corridoio
                int corridoioCentrale = (reparto.getMaxX() % 2 == 0) ? 2 : 3;
                
                // Calcolo larghezza simmetrica esatta
                int larghezzaScaffale = (int) (reparto.getMaxX() - corridoioCentrale) / 2;
                
                // Creiamo un tipo di scaffale specifico per le dimensioni calcolate
                Scaffali templateScaffale = scaffaliRepository.save(
                    new Scaffali(larghezzaScaffale, 1, 3, 3000) 
                );

                System.out.println("🧱 Generazione griglia per " + reparto.getNome() + 
                                   " | MaxX: " + reparto.getMaxX() + 
                                   " | Corridoio: " + corridoioCentrale + 
                                   " | Larghezza scaffali: " + larghezzaScaffale);

                // Ciclo sulla coordinata Y
                for (int y = 0; y <= (reparto.getMaxY() - 1); y += 2) {
                    
                    // 1. Scaffale a SINISTRA (parte da X=0)
                    mappeDaSalvare.add(new Mappa(
                        reparto, 
                        templateScaffale, 
                        0, 
                        y, 
                        "ORIZZONTALE"
                    ));

                    // 2. Scaffale a DESTRA (parte dopo il corridoio)
                    int coordinataXDestra = larghezzaScaffale + corridoioCentrale;
                    mappeDaSalvare.add(new Mappa(
                        reparto, 
                        templateScaffale, 
                        coordinataXDestra, 
                        y, 
                        "ORIZZONTALE"
                    ));
                }
            }
            mappaRepository.saveAll(mappeDaSalvare);
            System.out.println("✅ Mappa e Scaffali generati con corridoio dinamico!");
        }
    }
    
        // 8. Creazione Utente Admin tramite Service (che gestisce BCrypt)
        try {
            Utenti admin = new Utenti();
            admin.setNome("Mario");
            admin.setUsername("Admin");
            admin.setCognome("Rossi");
            admin.setData_nascita(new Date());
            admin.setEmail("admin@wms.it");
            admin.setPassword("admin123");
            
            // Passiamo l'oggetto admin e la password in chiaro. Il service farà l'hash.
            utentiService.registraUtente(admin, adminRole.getNomeRuolo());
            
            System.out.println("✅ Database pronto. Admin: admin@wms.it / admin123");
        } catch (Exception e) {
            // Se l'email esiste già, il service lancerà un'eccezione che catturiamo qui
            System.out.println("ℹ️ Database già popolato o utente presente.");
        }
    }
}