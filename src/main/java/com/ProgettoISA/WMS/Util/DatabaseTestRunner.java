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

        // 1. Gestione Ruoli
        Ruoli adminRole = ruoliRepository.findByNomeRuolo("Admin")
                .orElseGet(() -> ruoliRepository.save(new Ruoli("Admin")));

        if (ruoliRepository.findByNomeRuolo("Dipendente").isEmpty()) {
            ruoliRepository.save(new Ruoli("Dipendente"));
        }

        // 2-4. Gestione Etichette e Prodotti
        if (prodottiRepository.findAll().isEmpty() && etichetteRepository.findAll().isEmpty()) {
            System.out.println("⏳ Creazione Prodotti, Etichette e Relazioni...");

            Etichette surgelato = etichetteRepository.save(new Etichette("Surgelato"));
            Etichette fresco = etichetteRepository.save(new Etichette("Fresco"));
            Etichette vetro = etichetteRepository.save(new Etichette("Vetro"));
            Etichette fragile = etichetteRepository.save(new Etichette("Fragile"));
            Etichette alimentare = etichetteRepository.save(new Etichette("Alimentare"));

            List<Prodotti> listaProdotti = new ArrayList<>();
            List<EtProd> listaRelazioni = new ArrayList<>();
            Prodotti p;

            // SURGELATI
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

            // FRESCHI
            p = new Prodotti("Latte Intero", 1L, 1.0f, 1.80f, 2L, 6L);
            listaProdotti.add(p);
            listaRelazioni.add(new EtProd(p, fresco));
            listaRelazioni.add(new EtProd(p, alimentare));

            p = new Prodotti("Yogurt alla Frutta", 1L, 0.15f, 0.90f, 2L, 6L);
            listaProdotti.add(p);
            listaRelazioni.add(new EtProd(p, fresco));
            listaRelazioni.add(new EtProd(p, alimentare));
            listaRelazioni.add(new EtProd(p, vetro));
            listaRelazioni.add(new EtProd(p, fragile));

            p = new Prodotti("Formaggio", 1L, 0.4f, 3.50f, 2L, 6L);
            listaProdotti.add(p);
            listaRelazioni.add(new EtProd(p, fresco));
            listaRelazioni.add(new EtProd(p, alimentare));

            p = new Prodotti("Prosciutto Cotto", 1L, 0.12f, 4.00f, 2L, 6L);
            listaProdotti.add(p);
            listaRelazioni.add(new EtProd(p, fresco));
            listaRelazioni.add(new EtProd(p, alimentare));

            // SECCHI
            p = new Prodotti("Spaghetti n.5", 1L, 0.5f, 1.20f, 10L, 30L);
            listaProdotti.add(p);
            listaRelazioni.add(new EtProd(p, alimentare));

            p = new Prodotti("Riso Arborio", 1L, 1.0f, 2.50f, 10L, 30L);
            listaProdotti.add(p);
            listaRelazioni.add(new EtProd(p, alimentare));

            p = new Prodotti("Passata di Pomodoro", 1L, 0.7f, 0.80f, 10L, 30L);
            listaProdotti.add(p);
            listaRelazioni.add(new EtProd(p, alimentare));
            listaRelazioni.add(new EtProd(p, vetro));
            listaRelazioni.add(new EtProd(p, fragile));

            p = new Prodotti("Olio Extra Vergine", 1L, 1.0f, 5.00f, 10L, 30L);
            listaProdotti.add(p);
            listaRelazioni.add(new EtProd(p, alimentare));
            listaRelazioni.add(new EtProd(p, vetro));
            listaRelazioni.add(new EtProd(p, fragile));

            prodottiRepository.saveAll(listaProdotti);
            etProdRepository.saveAll(listaRelazioni);
            System.out.println("✅ Prodotti, Etichette e Relazioni inizializzati con successo!");
        }

        // 5. Gestione Reparti Tradizionali
        if(repartiRepository.findAll().isEmpty()) {
            List<Reparti> repartiIniziali = List.of(
                    new Reparti(15L, 15L, -20L, "Surgelati"),
                    new Reparti(20L, 20L, 3L, "Fresco"),
                    new Reparti(30L, 30L, 15L, "Secco")
            );

            repartiRepository.saveAll(repartiIniziali);
            System.out.println("✅ Reparti standard inizializzati con successo!");

            // Generazione Automatica Scaffali e Mappa per i reparti standard
            if(scaffaliRepository.findAll().isEmpty() && mappaRepository.findAll().isEmpty()) {
                List<Mappa> mappeDaSalvare = new ArrayList<>();
                for (Reparti reparto : repartiIniziali) {
                    int corridoioCentrale = (reparto.getMaxX() % 2 == 0) ? 2 : 3;
                    int larghezzaScaffale = (int) (reparto.getMaxX() - corridoioCentrale) / 2;

                    Scaffali templateScaffale = scaffaliRepository.save(
                            new Scaffali(larghezzaScaffale, 1, 3, 3000)
                    );

                    for (int y = 0; y <= (reparto.getMaxY() - 1); y += 2) {
                        mappeDaSalvare.add(new Mappa(reparto, templateScaffale, 0, y, "ORIZZONTALE"));
                        int coordinataXDestra = larghezzaScaffale + corridoioCentrale;
                        mappeDaSalvare.add(new Mappa(reparto, templateScaffale, coordinataXDestra, y, "ORIZZONTALE"));
                    }
                }
                mappaRepository.saveAll(mappeDaSalvare);
                System.out.println("✅ Mappa e Scaffali generati con corridoio dinamico!");
            }
        }

        // =================================================================
        // 6. AREA INBOUND - CONTROLLO INDIPENDENTE (FUORI DALL'IF PRECEDENTE)
        // =================================================================
        if(repartiRepository.findByNome("INBOUND").isEmpty()) {
            System.out.println("🧱 Generazione Reparto e Scaffale infinito per INBOUND...");

            // 1. Creiamo il reparto logico
            Reparti repartoInbound = new Reparti(100L, 100L, 20L, "INBOUND");
            repartoInbound = repartiRepository.save(repartoInbound);

            // 2. Creiamo lo scaffale fittizio
            Scaffali scaffaleInbound = new Scaffali(999, 999, 999, 999999);
            scaffaleInbound = scaffaliRepository.save(scaffaleInbound);

            // 3. Creiamo la relazione nella tabella Mappa
            Mappa mappaInbound = new Mappa(repartoInbound, scaffaleInbound, -1, -1, "INBOUND");
            mappaRepository.save(mappaInbound);

            System.out.println("✅ Area INBOUND inizializzata con successo!");
        }

        // 7. Creazione Utente Admin
        try {
            Utenti admin = new Utenti();
            admin.setNome("Mario");
            admin.setUsername("Admin");
            admin.setCognome("Rossi");
            admin.setData_nascita(new Date());
            admin.setEmail("admin@wms.it");
            admin.setPassword("admin123");

            utentiService.registraUtente(admin, adminRole.getNomeRuolo());
            System.out.println("✅ Database pronto. Admin: admin@wms.it / admin123");
        } catch (Exception e) {
            System.out.println("ℹ️ Database già popolato o utente presente.");
        }
    }
}