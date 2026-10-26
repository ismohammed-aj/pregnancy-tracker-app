package com.pfe.mamacareplus.repository;

import com.pfe.mamacareplus.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    List<Task> findByFemmeFemmeIdOrderByDueDateAsc(Integer femmeId);

    List<Task> findByFemmeFemmeIdAndIsCompletedOrderByDueDateAsc(Integer femmeId, Boolean isCompleted);
}