package com.ProgettoISA.WMS.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ProgettoISA.WMS.Model.Prodotti;

public interface ProdottiRepository extends JpaRepository<Prodotti, Long> {
    
}
