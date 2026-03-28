package com.ProgettoISA.WMS.Repository;

import com.ProgettoISA.WMS.Model.TurniDip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TurniDipRepository extends JpaRepository<TurniDip, Long> {
}