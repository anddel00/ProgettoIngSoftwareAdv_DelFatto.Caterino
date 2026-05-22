package com.ProgettoISA.WMS.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ProgettoISA.WMS.Model.BatchProdotti;


public interface BatchProdottiRepository extends JpaRepository<BatchProdotti, Long> {

    /**
     * LAZY LOADING (Query 1): Batch che hanno almeno uno slot occupato nel reparto.
     * Query semplice: JOIN BatchScaffale → Mappa → Reparto, con DISTINCT.
     */
    @Query("SELECT DISTINCT bs.batch_prodotti FROM BatchScaffale bs WHERE bs.mappa.reparto.id = :idReparto")
    List<BatchProdotti> findByReparto(@Param("idReparto") Long idReparto);

    @Query("SELECT bp FROM BatchProdotti bp WHERE bp.id NOT IN (SELECT DISTINCT bs.batch_prodotti.id FROM BatchScaffale bs)")
    List<BatchProdotti> findCompletamenteSospesi();

    // Server-Side Pagination per il Catalogo Lotti (Ricerca Avanzata)
    @Query("SELECT DISTINCT bp FROM BatchProdotti bp " +
           "LEFT JOIN bp.prodotto p " +
           "LEFT JOIN EtProd ep ON ep.prodotto = p " +
           "WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', :nomeProdotto, '%')) " +
           "AND (:idCategoria IS NULL OR ep.etichetta.id = :idCategoria) " +
           "AND (:statoSistemazione IS NULL " +
           "     OR (:statoSistemazione = 'SISTEMATI' AND EXISTS (SELECT 1 FROM BatchScaffale bs WHERE bs.batch_prodotti = bp)) " +
           "     OR (:statoSistemazione = 'ATTESA' AND NOT EXISTS (SELECT 1 FROM BatchScaffale bs WHERE bs.batch_prodotti = bp)))")
    org.springframework.data.domain.Page<BatchProdotti> findByFiltriAvanzati(
        @org.springframework.data.repository.query.Param("nomeProdotto") String nomeProdotto, 
        @org.springframework.data.repository.query.Param("idCategoria") Long idCategoria, 
        @org.springframework.data.repository.query.Param("statoSistemazione") String statoSistemazione, 
        org.springframework.data.domain.Pageable pageable
    );
}
