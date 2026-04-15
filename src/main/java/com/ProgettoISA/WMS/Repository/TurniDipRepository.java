package com.ProgettoISA.WMS.Repository;

import com.ProgettoISA.WMS.Model.TaskDip;
import com.ProgettoISA.WMS.Model.TurniDip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TurniDipRepository extends JpaRepository<TurniDip, Long> {

}