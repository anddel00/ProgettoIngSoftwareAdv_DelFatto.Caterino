package com.ProgettoISA.WMS.Repository;

import com.ProgettoISA.WMS.Model.TaskDip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskDipRepository extends JpaRepository<TaskDip, Long> {
}