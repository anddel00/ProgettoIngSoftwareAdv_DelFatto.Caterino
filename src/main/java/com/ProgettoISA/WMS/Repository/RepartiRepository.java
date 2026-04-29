package com.ProgettoISA.WMS.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ProgettoISA.WMS.Model.Reparti;

import java.util.Optional;


public interface RepartiRepository extends JpaRepository<Reparti, Long> {

    Optional<Reparti> findByNome(String Nome);
    
}
