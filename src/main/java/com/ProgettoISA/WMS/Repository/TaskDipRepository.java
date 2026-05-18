package com.ProgettoISA.WMS.Repository;

import com.ProgettoISA.WMS.Model.TaskDip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskDipRepository extends JpaRepository<TaskDip, Long> {

    // Per la pagina GESTIONE: Solo quelli che devono ancora finire
    @Query("SELECT td FROM TaskDip td WHERE td.task.stato_task != 'COMPLETATO'")
    List<TaskDip> findTaskAttivi();

    // Trova tutte le assegnazioni dove lo stato del Task è 'COMPLETATO'
    @Query("SELECT td FROM TaskDip td WHERE td.task.stato_task = 'COMPLETATO'")
    List<TaskDip> findStoricoCompletati();

    // Trova l'assegnazione conoscendo solo l'ID del task
    Optional<TaskDip> findByTask_Id(Long idTask);

    @Query("SELECT MAX(td.task.id) FROM TaskDip td WHERE td.dipendente.email = :email AND td.task.stato_task = 'COMPLETATO'")
    Long findMaxCompletedTaskIdByDipendenteEmail(@Param("email") String email);


}