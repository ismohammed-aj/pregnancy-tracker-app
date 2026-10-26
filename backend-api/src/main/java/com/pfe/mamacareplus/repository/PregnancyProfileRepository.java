package com.pfe.mamacareplus.repository;

import com.pfe.mamacareplus.entity.PregnancyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PregnancyProfileRepository extends JpaRepository<PregnancyProfile, Integer> {

    Optional<PregnancyProfile> findByFemmeFemmeId(Integer femmeId);

    boolean existsByFemmeFemmeId(Integer femmeId);
}