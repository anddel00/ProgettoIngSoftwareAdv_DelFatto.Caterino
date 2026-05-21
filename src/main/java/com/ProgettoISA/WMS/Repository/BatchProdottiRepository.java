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

    /**
     * LAZY LOADING (Query 2): Batch che NON hanno NESSUN BatchScaffale associato
     * (completamente sospesi, mai assegnati a nessuno scaffale in nessun reparto).
     */
    @Query("SELECT bp FROM BatchProdotti bp WHERE bp.id NOT IN (SELECT DISTINCT bs.batch_prodotti.id FROM BatchScaffale bs)")
    List<BatchProdotti> findCompletamenteSospesi();

}
