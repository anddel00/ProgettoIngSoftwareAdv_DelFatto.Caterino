package com.ProgettoISA.WMS.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ProgettoISA.WMS.Model.BatchScaffale;

public interface BatchScaffaleRepository extends JpaRepository<BatchScaffale, Long> {

    // 1. Cerca se in quello specifico slot c'è già quel prodotto
    @Query("SELECT b FROM BatchScaffale b WHERE b.mappa.id = :idMappa AND b.batch_prodotti.id = :idBatch AND b.riga = :riga AND b.colonna = :colonna AND b.altezza = :altezza")
    Optional<BatchScaffale> trovaEsistente(
        @Param("idMappa") Long idMappa, 
        @Param("idBatch") Long idBatch, 
        @Param("riga") Integer riga, 
        @Param("colonna") Integer colonna, 
        @Param("altezza") Integer altezza
    );

    // 2. Calcola la somma di tutti i pezzi di un batch sparsi per il magazzino
    @Query("SELECT SUM(b.qta) FROM BatchScaffale b WHERE b.batch_prodotti.id = :idBatch")
    Integer sumQtaByIdBatch(@Param("idBatch") Long idBatch);
}