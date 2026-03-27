package com.ProgettoISA.WMS.Repository;

import com.ProgettoISA.WMS.Model.Scaffali;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScaffaliRepository extends JpaRepository<Scaffali, Long> {
}