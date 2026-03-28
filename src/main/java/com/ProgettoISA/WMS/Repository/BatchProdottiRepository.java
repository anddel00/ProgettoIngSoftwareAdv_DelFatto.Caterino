package com.ProgettoISA.WMS.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ProgettoISA.WMS.Model.BatchProdotti;


interface BatchProdottiRepository extends JpaRepository<BatchProdotti, Long> {
    
}
