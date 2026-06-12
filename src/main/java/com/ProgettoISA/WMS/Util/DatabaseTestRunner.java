package com.ProgettoISA.WMS.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.time.LocalDate;

import com.ProgettoISA.WMS.Model.*;
import com.ProgettoISA.WMS.Repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ProgettoISA.WMS.Service.UtentiService;

@Component
@Profile("!test")
public class DatabaseTestRunner implements CommandLineRunner {

    private final RuoliRepository ruoliRepository;
    private final UtentiService utentiService;
    private final ProdottiRepository prodottiRepository;
    private final RepartiRepository repartiRepository;
    private final ScaffaliRepository scaffaliRepository;
    private final MappaRepository mappaRepository;
    private final EtichetteRepository etichetteRepository;
    private final EtProdRepository etProdRepository;

    // Iniezioni corrette dei repository necessari per lo stress test
    private final BatchScaffaleRepository batchScaffaleRepository;
    private final BatchProdottiRepository batchProdottiRepository;

    public DatabaseTestRunner(
            RuoliRepository ruoliRepository,
            UtentiService utentiService,
            ProdottiRepository prodottiRepository,
            RepartiRepository repartiRepository,
            ScaffaliRepository scaffaliRepository,
            MappaRepository mappaRepository,
            EtichetteRepository etichetteRepository,
            EtProdRepository etProdRepository,
            BatchScaffaleRepository batchScaffaleRepository,
            BatchProdottiRepository batchProdottiRepository
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
        this.batchScaffaleRepository = batchScaffaleRepository;
        this.batchProdottiRepository = batchProdottiRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        System.out.println("⏳ Inizializzazione dati database...");

        // 1. Gestione Ruoli con salvataggio sicuro
        Ruoli adminRole = ruoliRepository.findByNomeRuolo("Admin")
                .orElseGet(() -> ruoliRepository.save(new Ruoli("Admin")));

        if (ruoliRepository.findByNomeRuolo("Dipendente").isEmpty()) {
            ruoliRepository.save(new Ruoli("Dipendente"));
        }

        // 2-4. Gestione Unificata: Etichette, Prodotti e Relazioni
        if (prodottiRepository.findAll().isEmpty() && etichetteRepository.findAll().isEmpty()) {

            System.out.println("⏳ Creazione Prodotti, Etichette e Relazioni...");

            Etichette surgelato = etichetteRepository.save(new Etichette("Surgelato"));
            Etichette fresco = etichetteRepository.save(new Etichette("Fresco"));
            Etichette vetro = etichetteRepository.save(new Etichette("Vetro"));
            Etichette fragile = etichetteRepository.save(new Etichette("Fragile"));
            Etichette alimentare = etichetteRepository.save(new Etichette("Alimentare"));
            Etichette alcolico = etichetteRepository.save(new Etichette("Alcolico"));

            List<Prodotti> listaProdotti = new ArrayList<>();
            List<EtProd> listaRelazioni = new ArrayList<>();
            Prodotti p;

            // --- SURGELATI ---
            p = new Prodotti("Gelato al Cioccolato", 2L, 0.5f, 4.50f, -25L, -15L); listaProdotti.add(p); listaRelazioni.add(new EtProd(p, surgelato)); listaRelazioni.add(new EtProd(p, alimentare));
            p = new Prodotti("Gelato alla Vaniglia", 2L, 0.5f, 4.00f, -25L, -15L); listaProdotti.add(p); listaRelazioni.add(new EtProd(p, surgelato)); listaRelazioni.add(new EtProd(p, alimentare));
            p = new Prodotti("Gelato alla Fragola", 2L, 0.5f, 4.20f, -25L, -15L); listaProdotti.add(p); listaRelazioni.add(new EtProd(p, surgelato)); listaRelazioni.add(new EtProd(p, alimentare));
            p = new Prodotti("Gelato al Pistacchio", 2L, 0.5f, 4.80f, -25L, -15L); listaProdotti.add(p); listaRelazioni.add(new EtProd(p, surgelato)); listaRelazioni.add(new EtProd(p, alimentare));

            // --- FRESCHI ---
            p = new Prodotti("Latte Intero", 1L, 1.0f, 1.80f, 2L, 6L); listaProdotti.add(p); listaRelazioni.add(new EtProd(p, fresco)); listaRelazioni.add(new EtProd(p, alimentare));
            p = new Prodotti("Yogurt alla Frutta", 1L, 0.15f, 0.90f, 2L, 6L); listaProdotti.add(p); listaRelazioni.add(new EtProd(p, fresco)); listaRelazioni.add(new EtProd(p, alimentare)); listaRelazioni.add(new EtProd(p, vetro)); listaRelazioni.add(new EtProd(p, fragile));
            p = new Prodotti("Formaggio", 1L, 0.4f, 3.50f, 2L, 6L); listaProdotti.add(p); listaRelazioni.add(new EtProd(p, fresco)); listaRelazioni.add(new EtProd(p, alimentare));
            p = new Prodotti("Prosciutto Cotto", 1L, 0.12f, 4.00f, 2L, 6L); listaProdotti.add(p); listaRelazioni.add(new EtProd(p, fresco)); listaRelazioni.add(new EtProd(p, alimentare));

            // --- SECCHI ---
            p = new Prodotti("Spaghetti n.5", 1L, 0.5f, 1.20f, 10L, 30L); listaProdotti.add(p); listaRelazioni.add(new EtProd(p, alimentare));
            p = new Prodotti("Riso Arborio", 1L, 1.0f, 2.50f, 10L, 30L); listaProdotti.add(p); listaRelazioni.add(new EtProd(p, alimentare));
            p = new Prodotti("Passata di Pomodoro", 1L, 0.7f, 0.80f, 10L, 30L); listaProdotti.add(p); listaRelazioni.add(new EtProd(p, alimentare)); listaRelazioni.add(new EtProd(p, vetro)); listaRelazioni.add(new EtProd(p, fragile));
            p = new Prodotti("Olio Extra Vergine", 1L, 1.0f, 5.00f, 10L, 30L); listaProdotti.add(p); listaRelazioni.add(new EtProd(p, alimentare)); listaRelazioni.add(new EtProd(p, vetro)); listaRelazioni.add(new EtProd(p, fragile));

            // --- ALCOLICI ---
            p = new Prodotti("Vino Rosso Chianti", 1L, 1.2f, 8.50f, 10L, 20L); listaProdotti.add(p); listaRelazioni.add(new EtProd(p, alcolico)); listaRelazioni.add(new EtProd(p, alimentare)); listaRelazioni.add(new EtProd(p, vetro)); listaRelazioni.add(new EtProd(p, fragile));
            p = new Prodotti("Birra Artigianale IPA", 1L, 0.5f, 3.50f, 5L, 15L); listaProdotti.add(p); listaRelazioni.add(new EtProd(p, alcolico)); listaRelazioni.add(new EtProd(p, alimentare)); listaRelazioni.add(new EtProd(p, vetro));
            p = new Prodotti("Vodka liscia", 1L, 1.0f, 15.00f, -10L, 30L); listaProdotti.add(p); listaRelazioni.add(new EtProd(p, alcolico)); listaRelazioni.add(new EtProd(p, alimentare)); listaRelazioni.add(new EtProd(p, vetro)); listaRelazioni.add(new EtProd(p, fragile));

            prodottiRepository.saveAll(listaProdotti);
            etProdRepository.saveAll(listaRelazioni);
            System.out.println("✅ Prodotti, Etichette e Relazioni inizializzati con successo!");
        }

        // 5. Gestione Reparti con le dimensioni originali
        if(repartiRepository.findAll().isEmpty()) {
            List<Reparti> repartiIniziali = List.of(
                    new Reparti(15L, 15L, -20L, "Surgelati"),
                    new Reparti(20L, 20L, 3L, "Fresco"),
                    new Reparti(20L, 20L, 15L, "Secco")
            );

            repartiRepository.saveAll(repartiIniziali);
            System.out.println("✅ Reparti inizializzati con successo!");

            // 6 & 7. Generazione Automatica Scaffali e Mappa (Griglia Simmetrica Dinamica)
            if(scaffaliRepository.findAll().isEmpty() && mappaRepository.findAll().isEmpty()) {

                List<Mappa> mappeDaSalvare = new ArrayList<>();

                for (Reparti reparto : repartiIniziali) {
                    int corridoioCentrale = (reparto.getMaxX() % 2 == 0) ? 2 : 3;
                    int larghezzaScaffale = (int) (reparto.getMaxX() - corridoioCentrale) / 2;

                    Scaffali templateScaffale = scaffaliRepository.save(
                            new Scaffali(larghezzaScaffale, 1, 3, 3000)
                    );

                    System.out.println("🧱 Generazione griglia per " + reparto.getNome() +
                            " | MaxX: " + reparto.getMaxX() +
                            " | Corridoio: " + corridoioCentrale +
                            " | Larghezza scaffali: " + larghezzaScaffale);

                    for (int y = 0; y <= (reparto.getMaxY() - 1); y += 2) {
                        // 1. Scaffale a SINISTRA (X=0)
                        mappeDaSalvare.add(new Mappa(reparto, templateScaffale, 0, y, "ORIZZONTALE"));

                        // 2. Scaffale a DESTRA
                        int coordinataXDestra = larghezzaScaffale + corridoioCentrale;
                        mappeDaSalvare.add(new Mappa(reparto, templateScaffale, coordinataXDestra, y, "ORIZZONTALE"));
                    }
                }

                List<Mappa> mappeSalvate = mappaRepository.saveAll(mappeDaSalvare);
                System.out.println("✅ Mappa e Scaffali generati con corridoio dinamico!");

            }
        }

        // =================================================================
        // 🧪 STRESS TEST COMPLETO: GENERAZIONE DEI BATCH E ABBINAMENTO CELLE
        // =================================================================
        if (batchScaffaleRepository.findAll().isEmpty()) {
            System.out.println("🧪 Generazione lotti logici e fisici per lo Stress Test (solo Surgelati)...");
            List<Prodotti> tuttiIProdotti = prodottiRepository.findAll();
            List<Mappa> tutteLeMappe = mappaRepository.findAll();

            if (!tuttiIProdotti.isEmpty() && !tutteLeMappe.isEmpty()) {
                List<BatchProdotti> lottiDaSalvare = new ArrayList<>();
                LocalDate scadenzaFittizia = LocalDate.now().plusMonths(6);
                int prodottoIndex = 0;

                List<Mappa> mappeDaRiempire = new ArrayList<>();
                for (Mappa m : tutteLeMappe) {
                    if (m.getReparto().getNome().equalsIgnoreCase("Surgelati")) {
                        mappeDaRiempire.add(m);
                    }
                }

                // Fase A: Generiamo un'entità BatchProdotti per ogni singola coordinata spaziale (Righe x Colonne x Altezza)
                for (Mappa mappa : mappeDaRiempire) {
                    Scaffali scaffale = mappa.getScaffale();

                    for (int r = 0; r < scaffale.getMax_righe(); r++) {
                        for (int c = 0; c < scaffale.getMax_colonne(); c++) {
                            for (int h = 0; h < scaffale.getMax_altezza(); h++) {
                                Prodotti pScelto = tuttiIProdotti.get(prodottoIndex % tuttiIProdotti.size());
                                prodottoIndex++;

                                BatchProdotti bp = new BatchProdotti(pScelto, 100, scadenzaFittizia);
                                lottiDaSalvare.add(bp);
                            }
                        }
                    }
                }

                // Salviamo massivamente a DB per generare gli ID univoci stabili dei lotti logici
                List<BatchProdotti> lottiSalvati = batchProdottiRepository.saveAll(lottiDaSalvare);

                // Fase B: Abbiniamo i lotti logici salvati alla griglia di giacenza fisica (BatchScaffale)
                List<BatchScaffale> relazioniGiacenza = new ArrayList<>();
                int lottoCounter = 0;

                for (Mappa mappa : mappeDaRiempire) {
                    Scaffali scaffale = mappa.getScaffale();

                    // Controllo dell'orientamento per allinearci alla logica del Frontend
                    boolean isOrizzontale = mappa.getOrientamentoScaffale().equals("ORIZZONTALE");

                    for (int r = 0; r < scaffale.getMax_righe(); r++) {
                        for (int c = 0; c < scaffale.getMax_colonne(); c++) {
                            for (int h = 0; h < scaffale.getMax_altezza(); h++) {
                                BatchProdotti lottoCorrente = lottiSalvati.get(lottoCounter);
                                lottoCounter++;

                                // IL TRUCCO: Invertiamo le coordinate se lo scaffale è orizzontale,
                                // in modo che il frontend trovi i dati dove si aspetta!
                                int colonnaEffettiva = isOrizzontale ? r : c;
                                int rigaEffettiva = isOrizzontale ? c : r;

                                BatchScaffale bs = new BatchScaffale(mappa, lottoCorrente, colonnaEffettiva, rigaEffettiva, h, 100/lottoCorrente.getProdotto().getSpazioUnitario().intValue());
                                relazioniGiacenza.add(bs);
                            }
                        }
                    }
                }

                // Scrittura massiva finale ad alte prestazioni delle giacenze fisiche
                batchScaffaleRepository.saveAll(relazioniGiacenza);
                System.out.println("✅ STRESS TEST PRONTO: " + relazioniGiacenza.size() + " celle riempite al 100% nel reparto Surgelati!");
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

            utentiService.registraUtente(admin, adminRole.getNomeRuolo());
            System.out.println("✅ Database pronto. Admin: admin@wms.it / admin123");
        } catch (Exception e) {
            System.out.println("ℹ️ Admin presente");
        }

        try {
            Utenti dipendente = new Utenti();
            dipendente.setNome("Andrea");
            dipendente.setUsername("andel");
            dipendente.setCognome("Del Fatto");
            dipendente.setData_nascita(new Date());
            dipendente.setEmail("andrea@wms.it");
            dipendente.setPassword("andrea123");
            utentiService.registraUtente(dipendente, "Dipendente");
            System.out.println("✅ Dipendente creato: andrea@wms.it / andrea123");
        } catch (Exception e) {
            System.out.println("ℹ️ Dipendente presente");
        }

        try {
            Utenti dipendente2 = new Utenti();
            dipendente2.setNome("Luigi");
            dipendente2.setUsername("luigi");
            dipendente2.setCognome("Verdi");
            dipendente2.setData_nascita(new Date());
            dipendente2.setEmail("luigi@wms.it");
            dipendente2.setPassword("luigi123");
            utentiService.registraUtente(dipendente2, "Dipendente");
            System.out.println("✅ Dipendente creato: luigi@wms.it / luigi123");
        } catch (Exception e) {
            System.out.println("ℹ️ Dipendente luigi presente");
        }

        try {
            Utenti dipendente3 = new Utenti();
            dipendente3.setNome("Giulia");
            dipendente3.setUsername("giulia");
            dipendente3.setCognome("Bianchi");
            dipendente3.setData_nascita(new Date());
            dipendente3.setEmail("giulia@wms.it");
            dipendente3.setPassword("giulia123");
            utentiService.registraUtente(dipendente3, "Dipendente");
            System.out.println("✅ Dipendente creato: giulia@wms.it / giulia123");
        } catch (Exception e) {
            System.out.println("ℹ️ Dipendente giulia presente");
        }

        try {
            Utenti dipendente4 = new Utenti();
            dipendente4.setNome("Marco");
            dipendente4.setUsername("marco");
            dipendente4.setCognome("Neri");
            dipendente4.setData_nascita(new Date());
            dipendente4.setEmail("marco@wms.it");
            dipendente4.setPassword("marco123");
            utentiService.registraUtente(dipendente4, "Dipendente");
            System.out.println("✅ Dipendente creato: marco@wms.it / marco123");
        } catch (Exception e) {
            System.out.println("ℹ️ Dipendente marco presente");
        }

        try {
            Utenti dipendente5 = new Utenti();
            dipendente5.setNome("Sara");
            dipendente5.setUsername("sara");
            dipendente5.setCognome("Gialli");
            dipendente5.setData_nascita(new Date());
            dipendente5.setEmail("sara@wms.it");
            dipendente5.setPassword("sara123");
            utentiService.registraUtente(dipendente5, "Dipendente");
            System.out.println("✅ Dipendente creato: sara@wms.it / sara123");
        } catch (Exception e) {
            System.out.println("ℹ️ Dipendente sara presente");
        }
    }
}