package com.ProgettoISA.WMS.Repository;

import com.ProgettoISA.WMS.Model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT td.task FROM TaskDip td WHERE td.dipendente.email = :email")
    List<Task> findTasksByDipendenteEmail(@Param("email") String email);

    // ADDED: Count tasks that are NOT completed for a specific employee
    @Query("SELECT COUNT(td) FROM TaskDip td WHERE td.dipendente.email = :email AND td.task.stato_task != 'COMPLETATO'")
    long countTaskAttiviPerDipendente(@Param("email") String email);

    @Query("SELECT COALESCE(SUM(td.task.qta_spostata * p.PesoUnitario), 0.0) " +
           "FROM TaskDip td " +
           "JOIN td.task t " +
           "JOIN t.batch_prodotti b " +
           "JOIN b.prodotto p " +
           "WHERE td.dipendente.email = :email " +
           "AND t.stato_task IN ('DA_FARE', 'IN_CARICO')")
    Double sumCaricoAttivoInKgPerDipendente(@Param("email") String email);
}