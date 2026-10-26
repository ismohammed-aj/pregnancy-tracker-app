package com.pfe.mamacareplus.repository;

import com.pfe.mamacareplus.entity.RiskAssessment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Integer> {

    Optional<RiskAssessment> findFirstByFemmeFemmeIdOrderByEvaluatedAtDesc(Integer femmeId);

    Optional<RiskAssessment> findByQuestionnaireResponseResponseId(Integer responseId);

    List<RiskAssessment> findByFemmeFemmeIdOrderByEvaluatedAtDesc(Integer femmeId);
}
