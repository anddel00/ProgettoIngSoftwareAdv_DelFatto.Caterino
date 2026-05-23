package com.ProgettoISA.WMS.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ProgettoISA.WMS.Model.Ordine;

public interface OrdineRepository extends JpaRepository<Ordine, Long> {
}
