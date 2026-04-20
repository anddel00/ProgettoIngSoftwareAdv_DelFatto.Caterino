package com.ProgettoISA.WMS.Repository;

import com.ProgettoISA.WMS.Model.TurniDip;
import com.ProgettoISA.WMS.Model.Utenti;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TurniDipRepository extends JpaRepository<TurniDip, Long> {

    // Dipendenti con turno attivo (usato per monitoraggio e per dropdown task)
    @Query("SELECT td.dipendente FROM TurniDip td " +
            "WHERE td.oraInizioReale IS NOT NULL AND td.oraFineReale IS NULL")
    List<Utenti> findDipendentiAttualmenteInTurno();

    // Cambiamo Optional con List per evitare crash se ci sono "turni doppi" causati dai test
    @Query("SELECT td FROM TurniDip td " +
            "WHERE td.dipendente.email = :email " +
            "AND td.oraInizioReale IS NOT NULL " +
            "AND td.oraFineReale IS NULL")
    List<TurniDip> findTurniApertiByEmail(@Param("email") String email);

}