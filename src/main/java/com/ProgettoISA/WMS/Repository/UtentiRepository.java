package com.ProgettoISA.WMS.Repository;

import com.ProgettoISA.WMS.Model.Utenti;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtentiRepository extends JpaRepository<Utenti, Long> {
    Optional<Utenti> findByEmail(String email);
    boolean existsByEmail(String email);
}
