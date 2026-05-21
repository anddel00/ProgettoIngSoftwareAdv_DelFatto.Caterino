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

    // LAZY LOADING: Task attivi che coinvolgono scaffali del reparto specificato
    // JOIN espliciti su task e scaffali per evitare N+1 lazy loading
    @Query("""
        SELECT td FROM TaskDip td
        JOIN td.task t
        LEFT JOIN t.scaffale_inizio si
        LEFT JOIN si.reparto r_inizio
        LEFT JOIN t.scaffale_fine sf
        LEFT JOIN sf.reparto r_fine
        WHERE t.stato_task != 'COMPLETATO'
        AND (
            r_inizio.id = :idReparto
            OR
            r_fine.id = :idReparto
        )
    """)
    List<TaskDip> findTaskAttiviPerReparto(@Param("idReparto") Long idReparto);

}