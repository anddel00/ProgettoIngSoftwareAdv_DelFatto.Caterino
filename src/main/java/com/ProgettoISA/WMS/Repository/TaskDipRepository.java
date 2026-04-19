package com.ProgettoISA.WMS.Repository;

import com.ProgettoISA.WMS.Model.TaskDip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskDipRepository extends JpaRepository<TaskDip, Long> {

    // Per la pagina GESTIONE: Solo quelli che devono ancora finire
    @Query("SELECT td FROM TaskDip td WHERE td.task.stato_task != 'COMPLETATO'")
    List<TaskDip> findTaskAttivi();

    // Trova tutte le assegnazioni dove lo stato del Task è 'COMPLETATO'
    @Query("SELECT td FROM TaskDip td WHERE td.task.stato_task = 'COMPLETATO'")
    List<TaskDip> findStoricoCompletati();


}