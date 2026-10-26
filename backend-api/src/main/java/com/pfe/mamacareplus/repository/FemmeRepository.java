package com.pfe.mamacareplus.repository;

import com.pfe.mamacareplus.entity.Femme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FemmeRepository extends JpaRepository<Femme, Integer> {

    Optional<Femme> findByUserUserId(Integer userId);

    boolean existsByUserUserId(Integer userId);
}