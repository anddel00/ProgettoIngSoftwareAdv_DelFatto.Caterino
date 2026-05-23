package com.ProgettoISA.WMS.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ProgettoISA.WMS.DTO.CreaTaskDTO;
import com.ProgettoISA.WMS.Model.BatchProdotti;
import com.ProgettoISA.WMS.Model.BatchScaffale;
import com.ProgettoISA.WMS.Model.Ordine;
import com.ProgettoISA.WMS.Model.RigaOrdine;
import com.ProgettoISA.WMS.Repository.BatchProdottiRepository;
import com.ProgettoISA.WMS.Repository.BatchScaffaleRepository;
import com.ProgettoISA.WMS.Repository.OrdineRepository;

@Service
public class OrdineService {

    @Autowired
    private OrdineRepository ordineRepository;

    @Autowired
    private com.ProgettoISA.WMS.Repository.TaskRepository taskRepository;

    @Autowired
    private BatchProdottiRepository batchProdottiRepository;

    @Autowired
    private BatchScaffaleRepository batchScaffaleRepository;

    @Autowired
    private TaskService taskService;

    @Autowired
    private com.ProgettoISA.WMS.Repository.ProdottiRepository prodottiRepository;

    public List<com.ProgettoISA.WMS.DTO.OrdineDTO> getTuttiOrdini() {
        List<Ordine> ordini = ordineRepository.findAll();
        List<com.ProgettoISA.WMS.DTO.OrdineDTO> dtos = new ArrayList<>();
        for (Ordine o : ordini) {
            List<com.ProgettoISA.WMS.DTO.RigaOrdineDTO> righe = new ArrayList<>();
            for (RigaOrdine r : o.getRighe()) {
                righe.add(new com.ProgettoISA.WMS.DTO.RigaOrdineDTO(r.getId(), r.getProdotto().getId(), r.getProdotto().getNome(), r.getQuantitaRichiesta()));
            }
            dtos.add(new com.ProgettoISA.WMS.DTO.OrdineDTO(o.getId(), o.getDataCreazione().toString(), o.getStatoOrdine(), righe));
        }
        return dtos;
    }

    @Transactional
    public com.ProgettoISA.WMS.DTO.OrdineDTO generaOrdineCasuale() {
        List<com.ProgettoISA.WMS.Model.Prodotti> prodotti = prodottiRepository.findAll();
        if (prodotti.isEmpty()) throw new RuntimeException("Nessun prodotto nel DB");

        Ordine ordine = new Ordine();
        ordine.setStatoOrdine("DA_ALLOCARE");
        ordine.setRighe(new ArrayList<>());

        int numProdotti = java.util.concurrent.ThreadLocalRandom.current().nextInt(1, 4);
        for (int i = 0; i < numProdotti; i++) {
            com.ProgettoISA.WMS.Model.Prodotti p = prodotti.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(prodotti.size()));
            RigaOrdine r = new RigaOrdine();
            r.setOrdine(ordine);
            r.setProdotto(p);
            r.setQuantitaRichiesta(java.util.concurrent.ThreadLocalRandom.current().nextInt(3, 10));
            ordine.getRighe().add(r);
        }
        
        ordine = ordineRepository.save(ordine);
        
        List<com.ProgettoISA.WMS.DTO.RigaOrdineDTO> righe = new ArrayList<>();
        for (RigaOrdine r : ordine.getRighe()) {
            righe.add(new com.ProgettoISA.WMS.DTO.RigaOrdineDTO(r.getId(), r.getProdotto().getId(), r.getProdotto().getNome(), r.getQuantitaRichiesta()));
        }
        return new com.ProgettoISA.WMS.DTO.OrdineDTO(ordine.getId(), ordine.getDataCreazione().toString(), ordine.getStatoOrdine(), righe);
    }

    @Transactional(rollbackFor = Exception.class)
    public void allocaOrdine(Long idOrdine) throws Exception {
        Ordine ordine = ordineRepository.findById(idOrdine)
                .orElseThrow(() -> new IllegalArgumentException("Ordine non trovato: " + idOrdine));

        if (!"DA_ALLOCARE".equals(ordine.getStatoOrdine())) {
            throw new IllegalArgumentException("L'ordine non è nello stato DA_ALLOCARE");
        }

        List<CreaTaskDTO> tasksToCreate = new ArrayList<>();
        double pesoMissioneCorrente = 0;
        char suffissoMissione = 'A';

        for (RigaOrdine riga : ordine.getRighe()) {
            int qtaMancante = riga.getQuantitaRichiesta();
            
            // Recupero lotti disponibili in logica FEFO
            List<BatchProdotti> lottiDisponibili = batchProdottiRepository.findDisponibiliFEFO(riga.getProdotto(), "IN_MAGAZZINO");

            for (BatchProdotti lotto : lottiDisponibili) {
                if (qtaMancante <= 0) break;

                // Un lotto può trovarsi su più scaffali, iteriamo sugli scaffali
                for (BatchScaffale scaffale : lotto.getBatch_scaffali()) {
                    if (qtaMancante <= 0) break;

                    int qtaDisponibileSuScaffale = scaffale.getQta();
                    if (qtaDisponibileSuScaffale <= 0) continue;

                    int qtaDaPrelevare = Math.min(qtaMancante, qtaDisponibileSuScaffale);

                    // --- LOGICA DI FRAZIONAMENTO (SPLITTING) ---
                    // 1. Creazione del lotto "ombra" per il prelievo
                    BatchProdotti lottoPrelievo = new BatchProdotti();
                    lottoPrelievo.setProdotto(lotto.getProdotto());
                    lottoPrelievo.setScadenza(lotto.getScadenza());
                    lottoPrelievo.setStatoLotto("IN_MAGAZZINO"); // Lo manteniamo in magazzino finché non è effettivamente prelevato
                    lottoPrelievo.setQta(qtaDaPrelevare);
                    lottoPrelievo.setIdOrdineVendita(ordine.getId());
                    lottoPrelievo.setIdLottoOrigine(lotto.getId());
                    lottoPrelievo = batchProdottiRepository.save(lottoPrelievo);

                    // 2. Creazione dello scaffale "ombra" per il nuovo lotto
                    BatchScaffale scaffalePrelievo = new BatchScaffale(
                            scaffale.getMappa(),
                            lottoPrelievo,
                            scaffale.getColonna(),
                            scaffale.getRiga(),
                            scaffale.getAltezza(),
                            qtaDaPrelevare
                    );
                    batchScaffaleRepository.save(scaffalePrelievo);

                    // 3. Riduzione delle quantità dal lotto e scaffale originali
                    lotto.setQta(lotto.getQta() - qtaDaPrelevare);
                    batchProdottiRepository.save(lotto);

                    scaffale.setQta(scaffale.getQta() - qtaDaPrelevare);
                    if (scaffale.getQta() == 0) {
                        batchScaffaleRepository.delete(scaffale);
                    } else {
                        batchScaffaleRepository.save(scaffale);
                    }

                    // 4. Generazione Task di Prelievo
                    CreaTaskDTO dto = new CreaTaskDTO();
                    dto.setTipoTask("USCITA");
                    dto.setDescrizione("Prelievo per Ordine #" + ordine.getId() + " (" + riga.getProdotto().getNome() + ")");
                    dto.setQuantita(qtaDaPrelevare);
                    dto.setIdBatch(lottoPrelievo.getId());
                    dto.setIdScaffaleInizio(scaffale.getMappa().getId());
                    dto.setVecchiaX(scaffale.getColonna());
                    dto.setVecchiaY(scaffale.getRiga());
                    dto.setVecchiaZ(scaffale.getAltezza());

                    float pesoTask = qtaDaPrelevare * lotto.getProdotto().getPesoUnitario();
                    
                    if (pesoMissioneCorrente + pesoTask > 500 && !tasksToCreate.isEmpty()) {
                        // Spediamo la missione corrente
                        String idMissione = "MISS-ORD-" + ordine.getId() + "-" + suffissoMissione;
                        for (CreaTaskDTO t : tasksToCreate) {
                            t.setIdMissione(idMissione);
                        }
                        taskService.creaEAssegnaMultipli(tasksToCreate);
                        
                        // Prepariamo la prossima missione
                        tasksToCreate.clear();
                        pesoMissioneCorrente = 0;
                        suffissoMissione++;
                    }

                    tasksToCreate.add(dto);
                    pesoMissioneCorrente += pesoTask;
                    qtaMancante -= qtaDaPrelevare;
                }
            }

            if (qtaMancante > 0) {
                // Questo rollbackerà l'intera transazione, evitando lotti a metà
                throw new Exception("Stock insufficiente per il prodotto " + riga.getProdotto().getNome());
            }
        }

        // Se arriviamo qui, abbiamo coperto tutte le righe dell'ordine. Generiamo i task rimasti.
        if (!tasksToCreate.isEmpty()) {
            String idMissione = "MISS-ORD-" + ordine.getId() + "-" + suffissoMissione;
            for (CreaTaskDTO t : tasksToCreate) {
                t.setIdMissione(idMissione);
            }
            taskService.creaEAssegnaMultipli(tasksToCreate);
        }

        ordine.setStatoOrdine("IN_LAVORAZIONE");
        ordineRepository.save(ordine);
    }

    @Transactional
    public void eliminaOrdine(Long idOrdine) {
        Ordine ordine = ordineRepository.findById(idOrdine)
                .orElseThrow(() -> new IllegalArgumentException("Ordine non trovato: " + idOrdine));
        if (!"DA_ALLOCARE".equals(ordine.getStatoOrdine())) {
            throw new IllegalArgumentException("L'ordine non è nello stato DA_ALLOCARE");
        }
        ordineRepository.delete(ordine);
    }

    @Transactional(rollbackFor = Exception.class)
    public void annullaOrdineAllocato(Long idOrdine) throws Exception {
        Ordine ordine = ordineRepository.findById(idOrdine)
                .orElseThrow(() -> new IllegalArgumentException("Ordine non trovato: " + idOrdine));

        if (!"IN_LAVORAZIONE".equals(ordine.getStatoOrdine())) {
            throw new IllegalArgumentException("L'ordine non è IN_LAVORAZIONE");
        }

        String missionePrefix = "MISS-ORD-" + idOrdine + "-";
        List<com.ProgettoISA.WMS.Model.Task> tasks = taskRepository.findByIdMissioneStartingWith(missionePrefix);

        // Controllo che nessuno sia COMPLETATO
        for (com.ProgettoISA.WMS.Model.Task t : tasks) {
            if ("COMPLETATO".equals(t.getStato_task())) {
                throw new IllegalStateException("Impossibile annullare l'ordine: alcuni task della missione sono già stati completati");
            }
        }

        // Annulla tutti i task legati all'ordine
        for (com.ProgettoISA.WMS.Model.Task t : tasks) {
            taskService.annullaTask(t.getId());
        }

        // Trova e ripristina i batch ombra creati per l'ordine
        List<BatchProdotti> lottiOmbra = batchProdottiRepository.findByIdOrdineVendita(idOrdine);
        for (BatchProdotti lottoOmbra : lottiOmbra) {
            if (lottoOmbra.getIdLottoOrigine() != null) {
                BatchProdotti lottoOrigine = batchProdottiRepository.findById(lottoOmbra.getIdLottoOrigine())
                        .orElseThrow(() -> new IllegalStateException("Lotto originale non trovato"));

                // 1. Ripristiniamo la QTA nel lotto originale
                lottoOrigine.setQta(lottoOrigine.getQta() + lottoOmbra.getQta());
                batchProdottiRepository.save(lottoOrigine);

                // 2. Ripristiniamo la QTA nello scaffale originale
                for (BatchScaffale scaffaleOmbra : lottoOmbra.getBatch_scaffali()) {
                    BatchScaffale scaffaleOrigine = batchScaffaleRepository.trovaEsistente(
                            scaffaleOmbra.getMappa().getId(),
                            lottoOrigine.getId(),
                            scaffaleOmbra.getRiga(),
                            scaffaleOmbra.getColonna(),
                            scaffaleOmbra.getAltezza()
                    ).orElse(null);

                    if (scaffaleOrigine != null) {
                        scaffaleOrigine.setQta(scaffaleOrigine.getQta() + scaffaleOmbra.getQta());
                        batchScaffaleRepository.save(scaffaleOrigine);
                    } else {
                        BatchScaffale nuovoScaffaleOrigine = new BatchScaffale(
                                scaffaleOmbra.getMappa(),
                                lottoOrigine,
                                scaffaleOmbra.getColonna(),
                                scaffaleOmbra.getRiga(),
                                scaffaleOmbra.getAltezza(),
                                scaffaleOmbra.getQta()
                        );
                        batchScaffaleRepository.save(nuovoScaffaleOrigine);
                    }
                    batchScaffaleRepository.delete(scaffaleOmbra);
                }

                // 3. Eliminiamo il lotto ombra
                batchProdottiRepository.delete(lottoOmbra);
            }
        }

        // Ripristina lo stato dell'ordine
        ordine.setStatoOrdine("DA_ALLOCARE");
        ordineRepository.save(ordine);
    }
}
