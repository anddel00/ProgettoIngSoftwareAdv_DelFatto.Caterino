package com.ProgettoISA.WMS.Repository;

import com.ProgettoISA.WMS.Model.Turni;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TurniRepository extends JpaRepository<Turni, Long> {
}