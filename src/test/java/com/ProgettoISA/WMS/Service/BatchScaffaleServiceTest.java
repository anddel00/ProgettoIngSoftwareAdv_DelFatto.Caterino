package com.ProgettoISA.WMS.Service;

import com.ProgettoISA.WMS.DTO.BatchScaffaleDTO;
import com.ProgettoISA.WMS.Model.BatchProdotti;
import com.ProgettoISA.WMS.Model.BatchScaffale;
import com.ProgettoISA.WMS.Model.Mappa;
import com.ProgettoISA.WMS.Model.Reparti;
import com.ProgettoISA.WMS.Repository.BatchProdottiRepository;
import com.ProgettoISA.WMS.Repository.BatchScaffaleRepository;
import com.ProgettoISA.WMS.Repository.MappaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class BatchScaffaleServiceTest {

    @Mock
    private BatchScaffaleRepository batchScaffaleRepository;

    @Mock
    private MappaRepository mappaRepository;

    @Mock
    private BatchProdottiRepository batchProdottiRepository;

    @InjectMocks
    private BatchScaffaleService batchScaffaleService;

    // Oggetti di test riutilizzabili
    private Mappa mappaTest;
    private BatchProdotti batchTest;
    private Reparti repartoTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        repartoTest = new Reparti();
        repartoTest.setId(1L);
        repartoTest.setNome("Reparto Secco");

        mappaTest = new Mappa();
        mappaTest.setId(10L);
        mappaTest.setReparto(repartoTest);

        batchTest = new BatchProdotti();
        batchTest.setId(100L);
        batchTest.setQta(50);
        batchTest.setScadenza(LocalDate.of(2026, 12, 31));
    }

    // =========================================================================
    // Helper per creare BatchScaffale di test
    // =========================================================================
    private BatchScaffale creaBatchScaffale(Integer id, Mappa mappa, BatchProdotti batch,
                                            int colonna, int riga, int altezza, int qta) {
        BatchScaffale bs = new BatchScaffale(mappa, batch, colonna, riga, altezza, qta);
        bs.setId(id);
        return bs;
    }

    // =========================================================================
    // TEST: getTuttiIBatchScaffali()
    // =========================================================================
    @Nested
    @DisplayName("getTuttiIBatchScaffali")
    class GetTuttiIBatchScaffaliTests {

        @Test
        @DisplayName("Deve restituire lista vuota quando non ci sono BatchScaffale")
        void testListaVuota() {
            when(batchScaffaleRepository.findAll()).thenReturn(Collections.emptyList());

            List<BatchScaffaleDTO> result = batchScaffaleService.getTuttiIBatchScaffali();

            assertTrue(result.isEmpty());
            verify(batchScaffaleRepository).findAll();
        }

        @Test
        @DisplayName("Deve mappare correttamente i campi da Entity a DTO")
        void testMappaturaEntityToDTO() {
            BatchScaffale bs = creaBatchScaffale(1, mappaTest, batchTest, 2, 3, 1, 25);
            when(batchScaffaleRepository.findAll()).thenReturn(List.of(bs));

            List<BatchScaffaleDTO> result = batchScaffaleService.getTuttiIBatchScaffali();

            assertEquals(1, result.size());
            BatchScaffaleDTO dto = result.get(0);
            assertEquals(1L, dto.getId());
            assertEquals(10L, dto.getIdMappa());
            assertEquals(100L, dto.getIdBatchProdotti());
            assertEquals(2, dto.getColonna());
            assertEquals(3, dto.getRiga());
            assertEquals(1, dto.getAltezza());
            assertEquals(25, dto.getQta());
        }

        @Test
        @DisplayName("Deve restituire più BatchScaffale correttamente")
        void testMultipliBatchScaffale() {
            Mappa mappa2 = new Mappa();
            mappa2.setId(20L);
            mappa2.setReparto(repartoTest);
            BatchProdotti batch2 = new BatchProdotti();
            batch2.setId(200L);
            batch2.setQta(30);

            BatchScaffale bs1 = creaBatchScaffale(1, mappaTest, batchTest, 0, 0, 0, 10);
            BatchScaffale bs2 = creaBatchScaffale(2, mappa2, batch2, 1, 1, 1, 20);

            when(batchScaffaleRepository.findAll()).thenReturn(List.of(bs1, bs2));

            List<BatchScaffaleDTO> result = batchScaffaleService.getTuttiIBatchScaffali();

            assertEquals(2, result.size());
            assertEquals(10L, result.get(0).getIdMappa());
            assertEquals(20L, result.get(1).getIdMappa());
        }
    }

    // =========================================================================
    // TEST: getBatchScaffaliPerReparto()
    // =========================================================================
    @Nested
    @DisplayName("getBatchScaffaliPerReparto")
    class GetBatchScaffaliPerRepartoTests {

        @Test
        @DisplayName("Deve filtrare per reparto e restituire solo i relativi")
        void testFiltroPerReparto() {
            BatchScaffale bs = creaBatchScaffale(1, mappaTest, batchTest, 0, 0, 0, 15);
            when(batchScaffaleRepository.findByMappaRepartoId(1L)).thenReturn(List.of(bs));

            List<BatchScaffaleDTO> result = batchScaffaleService.getBatchScaffaliPerReparto(1L);

            assertEquals(1, result.size());
            assertEquals(15, result.get(0).getQta());
            verify(batchScaffaleRepository).findByMappaRepartoId(1L);
        }

        @Test
        @DisplayName("Deve restituire lista vuota per reparto senza scaffali")
        void testRepartoVuoto() {
            when(batchScaffaleRepository.findByMappaRepartoId(999L)).thenReturn(Collections.emptyList());

            List<BatchScaffaleDTO> result = batchScaffaleService.getBatchScaffaliPerReparto(999L);

            assertTrue(result.isEmpty());
        }
    }

    // =========================================================================
    // TEST: sincronizzaBatch() — CASO A: INSERIMENTO (id == null)
    // =========================================================================
    @Nested
    @DisplayName("sincronizzaBatch - Inserimento (id == null)")
    class SincronizzaBatchInserimentoTests {

        @Test
        @DisplayName("Crea nuovo record quando non esiste nello slot")
        void testInserimentoNuovoRecord() throws Exception {
            BatchScaffaleDTO dto = new BatchScaffaleDTO(null, 10L, 100L, 0, 0, 0, 5);

            when(batchScaffaleRepository.trovaEsistente(10L, 100L, 0, 0, 0))
                .thenReturn(Optional.empty());
            when(mappaRepository.findById(10L)).thenReturn(Optional.of(mappaTest));
            when(batchProdottiRepository.findById(100L)).thenReturn(Optional.of(batchTest));
            when(batchScaffaleRepository.sumQtaByIdBatch(100L)).thenReturn(5);

            batchScaffaleService.sincronizzaBatch(List.of(dto));

            ArgumentCaptor<BatchScaffale> captor = ArgumentCaptor.forClass(BatchScaffale.class);
            verify(batchScaffaleRepository).save(captor.capture());
            BatchScaffale salvato = captor.getValue();
            assertEquals(5, salvato.getQta());
            assertEquals(0, salvato.getColonna());
        }

        @Test
        @DisplayName("Somma quantità se già esiste nello slot (merge)")
        void testMergeQuantitaEsistente() throws Exception {
            BatchScaffale esistente = creaBatchScaffale(1, mappaTest, batchTest, 0, 0, 0, 10);
            BatchScaffaleDTO dto = new BatchScaffaleDTO(null, 10L, 100L, 0, 0, 0, 5);

            when(batchScaffaleRepository.trovaEsistente(10L, 100L, 0, 0, 0))
                .thenReturn(Optional.of(esistente));
            when(batchProdottiRepository.findById(100L)).thenReturn(Optional.of(batchTest));
            when(batchScaffaleRepository.sumQtaByIdBatch(100L)).thenReturn(15);

            batchScaffaleService.sincronizzaBatch(List.of(dto));

            assertEquals(15, esistente.getQta());
            verify(batchScaffaleRepository).save(esistente);
        }

        @Test
        @DisplayName("Elimina record esistente se la somma risulta <= 0")
        void testEliminaSeQuantitaZeroONegativa() throws Exception {
            BatchScaffale esistente = creaBatchScaffale(1, mappaTest, batchTest, 0, 0, 0, 3);
            BatchScaffaleDTO dto = new BatchScaffaleDTO(null, 10L, 100L, 0, 0, 0, -3);

            when(batchScaffaleRepository.trovaEsistente(10L, 100L, 0, 0, 0))
                .thenReturn(Optional.of(esistente));
            when(batchProdottiRepository.findById(100L)).thenReturn(Optional.of(batchTest));
            when(batchScaffaleRepository.sumQtaByIdBatch(100L)).thenReturn(0);

            batchScaffaleService.sincronizzaBatch(List.of(dto));

            verify(batchScaffaleRepository).delete(esistente);
            verify(batchScaffaleRepository, never()).save(esistente);
        }

        @Test
        @DisplayName("Non crea record se quantità <= 0 e non esiste precedente")
        void testIgnoraQuantitaZeroSenzaEsistente() throws Exception {
            BatchScaffaleDTO dto = new BatchScaffaleDTO(null, 10L, 100L, 0, 0, 0, 0);

            when(batchScaffaleRepository.trovaEsistente(10L, 100L, 0, 0, 0))
                .thenReturn(Optional.empty());
            when(batchProdottiRepository.findById(100L)).thenReturn(Optional.of(batchTest));
            when(batchScaffaleRepository.sumQtaByIdBatch(100L)).thenReturn(0);

            batchScaffaleService.sincronizzaBatch(List.of(dto));

            verify(batchScaffaleRepository, never()).save(any(BatchScaffale.class));
        }

        @Test
        @DisplayName("Lancia eccezione se Mappa non trovata")
        void testMappaNotFound() {
            BatchScaffaleDTO dto = new BatchScaffaleDTO(null, 999L, 100L, 0, 0, 0, 5);

            when(batchScaffaleRepository.trovaEsistente(999L, 100L, 0, 0, 0))
                .thenReturn(Optional.empty());
            when(mappaRepository.findById(999L)).thenReturn(Optional.empty());

            Exception ex = assertThrows(Exception.class, () ->
                batchScaffaleService.sincronizzaBatch(List.of(dto))
            );
            assertTrue(ex.getMessage().contains("Mappa non trovata"));
        }

        @Test
        @DisplayName("Lancia eccezione se BatchProdotti non trovato")
        void testBatchNotFound() {
            BatchScaffaleDTO dto = new BatchScaffaleDTO(null, 10L, 999L, 0, 0, 0, 5);

            when(batchScaffaleRepository.trovaEsistente(10L, 999L, 0, 0, 0))
                .thenReturn(Optional.empty());
            when(mappaRepository.findById(10L)).thenReturn(Optional.of(mappaTest));
            when(batchProdottiRepository.findById(999L)).thenReturn(Optional.empty());

            Exception ex = assertThrows(Exception.class, () ->
                batchScaffaleService.sincronizzaBatch(List.of(dto))
            );
            assertTrue(ex.getMessage().contains("Batch non trovato"));
        }
    }

    // =========================================================================
    // TEST: sincronizzaBatch() — CASO B: AGGIORNAMENTO/RIMOZIONE (id != null)
    // =========================================================================
    @Nested
    @DisplayName("sincronizzaBatch - Aggiornamento (id != null)")
    class SincronizzaBatchAggiornamentoTests {

        @Test
        @DisplayName("Aggiorna quantità di un record esistente")
        void testAggiornamentoQuantita() throws Exception {
            BatchScaffale esistente = creaBatchScaffale(1, mappaTest, batchTest, 0, 0, 0, 10);
            BatchScaffaleDTO dto = new BatchScaffaleDTO(1L, 10L, 100L, 0, 0, 0, 20);

            when(batchScaffaleRepository.findById(1L)).thenReturn(Optional.of(esistente));
            when(batchProdottiRepository.findById(100L)).thenReturn(Optional.of(batchTest));
            when(batchScaffaleRepository.sumQtaByIdBatch(100L)).thenReturn(20);

            batchScaffaleService.sincronizzaBatch(List.of(dto));

            assertEquals(20, esistente.getQta());
            verify(batchScaffaleRepository).save(esistente);
        }

        @Test
        @DisplayName("Elimina record se quantità aggiornata <= 0")
        void testRimozioneQuantitaZero() throws Exception {
            BatchScaffale esistente = creaBatchScaffale(1, mappaTest, batchTest, 0, 0, 0, 10);
            BatchScaffaleDTO dto = new BatchScaffaleDTO(1L, 10L, 100L, 0, 0, 0, 0);

            when(batchScaffaleRepository.findById(1L)).thenReturn(Optional.of(esistente));
            when(batchProdottiRepository.findById(100L)).thenReturn(Optional.of(batchTest));
            when(batchScaffaleRepository.sumQtaByIdBatch(100L)).thenReturn(0);

            batchScaffaleService.sincronizzaBatch(List.of(dto));

            verify(batchScaffaleRepository).delete(esistente);
        }

        @Test
        @DisplayName("Elimina record se quantità negativa")
        void testRimozioneQuantitaNegativa() throws Exception {
            BatchScaffale esistente = creaBatchScaffale(1, mappaTest, batchTest, 0, 0, 0, 10);
            BatchScaffaleDTO dto = new BatchScaffaleDTO(1L, 10L, 100L, 0, 0, 0, -5);

            when(batchScaffaleRepository.findById(1L)).thenReturn(Optional.of(esistente));
            when(batchProdottiRepository.findById(100L)).thenReturn(Optional.of(batchTest));
            when(batchScaffaleRepository.sumQtaByIdBatch(100L)).thenReturn(0);

            batchScaffaleService.sincronizzaBatch(List.of(dto));

            verify(batchScaffaleRepository).delete(esistente);
        }

        @Test
        @DisplayName("Lancia eccezione se record non trovato per ID")
        void testRecordNonTrovato() {
            BatchScaffaleDTO dto = new BatchScaffaleDTO(999L, 10L, 100L, 0, 0, 0, 5);

            when(batchScaffaleRepository.findById(999L)).thenReturn(Optional.empty());

            Exception ex = assertThrows(Exception.class, () ->
                batchScaffaleService.sincronizzaBatch(List.of(dto))
            );
            assertTrue(ex.getMessage().contains("Record non trovato"));
        }
    }

    // =========================================================================
    // TEST: sincronizzaBatch() — CONTROLLO TETTO MASSIMO (validazione quantità)
    // Replica il vincolo frontend: qtaSuScaffali <= lottoMaster.getQta()
    // =========================================================================
    @Nested
    @DisplayName("sincronizzaBatch - Controllo tetto massimo quantità")
    class SincronizzaBatchTettoMassimoTests {

        @Test
        @DisplayName("Lancia eccezione quando la quantità sugli scaffali supera il tetto del lotto")
        void testSuperamentoTettoMassimo() {
            batchTest.setQta(50); // Il lotto ha max 50 pezzi
            BatchScaffaleDTO dto = new BatchScaffaleDTO(null, 10L, 100L, 0, 0, 0, 10);

            when(batchScaffaleRepository.trovaEsistente(10L, 100L, 0, 0, 0))
                .thenReturn(Optional.empty());
            when(mappaRepository.findById(10L)).thenReturn(Optional.of(mappaTest));
            when(batchProdottiRepository.findById(100L)).thenReturn(Optional.of(batchTest));
            // Dopo il flush, la somma sugli scaffali risulta 60 > 50
            when(batchScaffaleRepository.sumQtaByIdBatch(100L)).thenReturn(60);

            Exception ex = assertThrows(Exception.class, () ->
                batchScaffaleService.sincronizzaBatch(List.of(dto))
            );
            assertTrue(ex.getMessage().contains("Quantità massima superata"));
            assertTrue(ex.getMessage().contains("100"));
        }

        @Test
        @DisplayName("Passa correttamente quando la quantità è esattamente al limite")
        void testQuantitaAlLimite() throws Exception {
            batchTest.setQta(50);
            BatchScaffaleDTO dto = new BatchScaffaleDTO(null, 10L, 100L, 0, 0, 0, 10);

            when(batchScaffaleRepository.trovaEsistente(10L, 100L, 0, 0, 0))
                .thenReturn(Optional.empty());
            when(mappaRepository.findById(10L)).thenReturn(Optional.of(mappaTest));
            when(batchProdottiRepository.findById(100L)).thenReturn(Optional.of(batchTest));
            when(batchScaffaleRepository.sumQtaByIdBatch(100L)).thenReturn(50); // Esattamente al limite

            assertDoesNotThrow(() -> batchScaffaleService.sincronizzaBatch(List.of(dto)));
        }

        @Test
        @DisplayName("Gestisce sumQtaByIdBatch che ritorna null (nessun record)")
        void testSumNullTrattataComezero() throws Exception {
            batchTest.setQta(50);
            BatchScaffaleDTO dto = new BatchScaffaleDTO(null, 10L, 100L, 0, 0, 0, 5);

            when(batchScaffaleRepository.trovaEsistente(10L, 100L, 0, 0, 0))
                .thenReturn(Optional.empty());
            when(mappaRepository.findById(10L)).thenReturn(Optional.of(mappaTest));
            when(batchProdottiRepository.findById(100L)).thenReturn(Optional.of(batchTest));
            when(batchScaffaleRepository.sumQtaByIdBatch(100L)).thenReturn(null);

            assertDoesNotThrow(() -> batchScaffaleService.sincronizzaBatch(List.of(dto)));
        }

        @Test
        @DisplayName("Lancia eccezione se il lotto originale non è trovato durante il controllo")
        void testLottoOriginaleNonTrovato() {
            BatchScaffaleDTO dto = new BatchScaffaleDTO(null, 10L, 100L, 0, 0, 0, 5);

            when(batchScaffaleRepository.trovaEsistente(10L, 100L, 0, 0, 0))
                .thenReturn(Optional.empty());
            when(mappaRepository.findById(10L)).thenReturn(Optional.of(mappaTest));
            // findById per il salvataggio OK
            when(batchProdottiRepository.findById(100L))
                .thenReturn(Optional.of(batchTest))      // prima chiamata: salvataggio
                .thenReturn(Optional.empty());            // seconda chiamata: controllo tetto

            Exception ex = assertThrows(Exception.class, () ->
                batchScaffaleService.sincronizzaBatch(List.of(dto))
            );
            assertTrue(ex.getMessage().contains("Lotto originale non trovato"));
        }
    }

    // =========================================================================
    // TEST: sincronizzaBatch() — Operazioni multiple nella stessa chiamata
    // =========================================================================
    @Nested
    @DisplayName("sincronizzaBatch - Operazioni multiple")
    class SincronizzaBatchMultipleTests {

        @Test
        @DisplayName("Gestisce correttamente un payload misto (inserimento + aggiornamento)")
        void testPayloadMisto() throws Exception {
            // DTO 1: Inserimento nuovo (id == null)
            BatchScaffaleDTO dtoNuovo = new BatchScaffaleDTO(null, 10L, 100L, 1, 1, 0, 5);
            // DTO 2: Aggiornamento esistente (id != null)
            BatchScaffale esistente = creaBatchScaffale(2, mappaTest, batchTest, 0, 0, 0, 10);
            BatchScaffaleDTO dtoUpdate = new BatchScaffaleDTO(2L, 10L, 100L, 0, 0, 0, 15);

            when(batchScaffaleRepository.trovaEsistente(10L, 100L, 1, 1, 0))
                .thenReturn(Optional.empty());
            when(mappaRepository.findById(10L)).thenReturn(Optional.of(mappaTest));
            when(batchProdottiRepository.findById(100L)).thenReturn(Optional.of(batchTest));
            when(batchScaffaleRepository.findById(2L)).thenReturn(Optional.of(esistente));
            when(batchScaffaleRepository.sumQtaByIdBatch(100L)).thenReturn(20); // 5 + 15 = 20 <= 50

            batchScaffaleService.sincronizzaBatch(List.of(dtoNuovo, dtoUpdate));

            // Verifica che save sia stato chiamato per entrambi
            verify(batchScaffaleRepository, times(2)).save(any(BatchScaffale.class));
            assertEquals(15, esistente.getQta());
        }

        @Test
        @DisplayName("Esegue il flush prima del controllo tetto")
        void testFlushPrimaDelControllo() throws Exception {
            BatchScaffaleDTO dto = new BatchScaffaleDTO(null, 10L, 100L, 0, 0, 0, 5);

            when(batchScaffaleRepository.trovaEsistente(10L, 100L, 0, 0, 0))
                .thenReturn(Optional.empty());
            when(mappaRepository.findById(10L)).thenReturn(Optional.of(mappaTest));
            when(batchProdottiRepository.findById(100L)).thenReturn(Optional.of(batchTest));
            when(batchScaffaleRepository.sumQtaByIdBatch(100L)).thenReturn(5);

            batchScaffaleService.sincronizzaBatch(List.of(dto));

            verify(batchScaffaleRepository).flush();
        }

        @Test
        @DisplayName("Lista vuota non genera operazioni né errori")
        void testPayloadVuoto() throws Exception {
            batchScaffaleService.sincronizzaBatch(Collections.emptyList());

            verify(batchScaffaleRepository, never()).save(any());
            verify(batchScaffaleRepository, never()).delete(any());
            verify(batchScaffaleRepository).flush();
        }
    }

    // =========================================================================
    // TEST: Vincoli replicati dal frontend (MappaMagazzino.vue)
    // Frontend controlla: quantità > 0, non supera disponibilità lotto
    // Backend applica lo stesso vincolo nel controllo tetto massimo
    // =========================================================================
    @Nested
    @DisplayName("Vincoli frontend replicati lato backend")
    class VincoliFrontendTests {

        @Test
        @DisplayName("Quantità negativa nel DTO: nuovo record con qta <= 0 non viene creato")
        void testQuantitaNegativaNuovoNonCreata() throws Exception {
            BatchScaffaleDTO dto = new BatchScaffaleDTO(null, 10L, 100L, 0, 0, 0, -5);

            when(batchScaffaleRepository.trovaEsistente(10L, 100L, 0, 0, 0))
                .thenReturn(Optional.empty());
            when(batchProdottiRepository.findById(100L)).thenReturn(Optional.of(batchTest));
            when(batchScaffaleRepository.sumQtaByIdBatch(100L)).thenReturn(0);

            batchScaffaleService.sincronizzaBatch(List.of(dto));

            verify(batchScaffaleRepository, never()).save(any(BatchScaffale.class));
            verify(mappaRepository, never()).findById(anyLong());
        }

        @Test
        @DisplayName("Distribuzione su più scaffali: la somma totale non deve superare il lotto")
        void testDistribuzioneSuPiuScaffali() {
            batchTest.setQta(20);

            // Inseriamo 15 su uno scaffale e 10 su un altro = 25 > 20
            BatchScaffaleDTO dto1 = new BatchScaffaleDTO(null, 10L, 100L, 0, 0, 0, 15);
            BatchScaffaleDTO dto2 = new BatchScaffaleDTO(null, 10L, 100L, 1, 0, 0, 10);

            when(batchScaffaleRepository.trovaEsistente(anyLong(), eq(100L), anyInt(), anyInt(), anyInt()))
                .thenReturn(Optional.empty());
            when(mappaRepository.findById(10L)).thenReturn(Optional.of(mappaTest));
            when(batchProdottiRepository.findById(100L)).thenReturn(Optional.of(batchTest));
            // Dopo il flush, la somma risulta 25 > 20
            when(batchScaffaleRepository.sumQtaByIdBatch(100L)).thenReturn(25);

            Exception ex = assertThrows(Exception.class, () ->
                batchScaffaleService.sincronizzaBatch(List.of(dto1, dto2))
            );
            assertTrue(ex.getMessage().contains("Quantità massima superata"));
        }
    }
}
