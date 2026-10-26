package com.pfe.mamacareplus.repository;

import com.pfe.mamacareplus.entity.BabyDevelopment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BabyDevelopmentRepository extends JpaRepository<BabyDevelopment, Integer> {

    Optional<BabyDevelopment> findByWeekNumber(Integer weekNumber);

    boolean existsByWeekNumber(Integer weekNumber);
}