package com.ProgettoISA.WMS.Repository;

import com.ProgettoISA.WMS.Model.Ruoli;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RuoliRepository extends JpaRepository<Ruoli, Long> {
    Optional<Ruoli> findByNomeRuolo(String nomeRuolo);
    boolean existsByNomeRuolo(String nomeRuolo);
}
