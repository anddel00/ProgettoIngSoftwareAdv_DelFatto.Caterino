package com.ProgettoISA.WMS.Repository;

import com.ProgettoISA.WMS.Model.TurniDip;
import com.ProgettoISA.WMS.Model.Utenti;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TurniDipRepository extends JpaRepository<TurniDip, Long> {

    // Seleziona l'entità Utente partendo dai record TurniDip dove il turno è attualmente in corso
    @Query("SELECT td.dipendente FROM TurniDip td WHERE td.oraInizioReale IS NOT NULL AND td.oraFineReale IS NULL")
    List<Utenti> findDipendentiAttualmenteInTurno();

}