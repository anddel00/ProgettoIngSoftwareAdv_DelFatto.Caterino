package com.ProgettoISA.WMS.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ProgettoISA.WMS.Model.BatchProdotti;


public interface BatchProdottiRepository extends JpaRepository<BatchProdotti, Long> {

    /**
     * Query FEFO: Recupera lotti disponibili per un prodotto con uno specifico stato,
     * assicurandosi che non siano attualmente impegnati in task di tipo DA_FARE o IN_CORSO.
     */
    @Query("SELECT b FROM BatchProdotti b " +
           "WHERE b.prodotto = :prodotto " +
           "AND b.statoLotto = :statoLotto " +
           "AND NOT EXISTS (SELECT t FROM Task t WHERE t.batch_prodotti = b AND t.stato_task IN ('DA_FARE', 'IN_CORSO')) " +
           "ORDER BY b.scadenza ASC")
    List<BatchProdotti> findDisponibiliFEFO(@Param("prodotto") com.ProgettoISA.WMS.Model.Prodotti prodotto, @Param("statoLotto") String statoLotto);

    /**
     * LAZY LOADING (Query 1): Batch che hanno almeno uno slot occupato nel reparto.
     * Query semplice: JOIN BatchScaffale → Mappa → Reparto, con DISTINCT.
     */
    @Query("SELECT DISTINCT bs.batch_prodotti FROM BatchScaffale bs WHERE bs.mappa.reparto.id = :idReparto")
    List<BatchProdotti> findByReparto(@Param("idReparto") Long idReparto);

    @Query("SELECT bp FROM BatchProdotti bp WHERE bp.statoLotto = 'IN_ATTESA' AND bp.id NOT IN (SELECT DISTINCT bs.batch_prodotti.id FROM BatchScaffale bs)")
    List<BatchProdotti> findCompletamenteSospesi();

    // Server-Side Pagination per il Catalogo Lotti (Ricerca Avanzata)
    @Query("SELECT DISTINCT bp FROM BatchProdotti bp " +
           "LEFT JOIN bp.prodotto p " +
           "LEFT JOIN EtProd ep ON ep.prodotto = p " +
           "WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', :nomeProdotto, '%')) " +
           "AND (:idCategoria IS NULL OR ep.etichetta.id = :idCategoria) " +
           "AND (:statoSistemazione IS NULL AND bp.statoLotto != 'VENDUTO' " +
           "     OR (:statoSistemazione = 'SISTEMATI' AND EXISTS (SELECT 1 FROM BatchScaffale bs WHERE bs.batch_prodotti = bp)) " +
           "     OR (:statoSistemazione = 'ATTESA' AND NOT EXISTS (SELECT 1 FROM BatchScaffale bs WHERE bs.batch_prodotti = bp) AND bp.statoLotto != 'VENDUTO') " +
           "     OR (:statoSistemazione = 'VENDUTI' AND bp.statoLotto = 'VENDUTO'))")
    org.springframework.data.domain.Page<BatchProdotti> findByFiltriAvanzati(
        @org.springframework.data.repository.query.Param("nomeProdotto") String nomeProdotto, 
        @org.springframework.data.repository.query.Param("idCategoria") Long idCategoria, 
        @org.springframework.data.repository.query.Param("statoSistemazione") String statoSistemazione, 
        org.springframework.data.domain.Pageable pageable
    );

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("UPDATE BatchProdotti bp SET bp.statoLotto = 'IN_MAGAZZINO' WHERE bp.statoLotto = 'IN_ATTESA' AND bp.id IN (SELECT DISTINCT bs.batch_prodotti.id FROM BatchScaffale bs)")
    void aggiornaStatoLottiStoccati();

    @Query("SELECT bp FROM BatchProdotti bp WHERE bp.idOrdineVendita = :idOrdineVendita")
    List<BatchProdotti> findByIdOrdineVendita(@org.springframework.data.repository.query.Param("idOrdineVendita") Long idOrdineVendita);
}
