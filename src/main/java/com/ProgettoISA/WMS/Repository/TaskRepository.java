package com.ProgettoISA.WMS.Repository;

import com.ProgettoISA.WMS.Model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
}