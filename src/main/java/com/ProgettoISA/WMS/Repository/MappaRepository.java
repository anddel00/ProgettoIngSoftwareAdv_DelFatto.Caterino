package com.ProgettoISA.WMS.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ProgettoISA.WMS.Model.Mappa;

public interface MappaRepository extends JpaRepository<Mappa, Long> {
    
    List<Mappa> findByRepartoId(Long repartoId);
}
