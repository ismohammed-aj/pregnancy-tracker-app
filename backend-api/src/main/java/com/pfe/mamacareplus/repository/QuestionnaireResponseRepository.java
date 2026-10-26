package com.pfe.mamacareplus.repository;

import com.pfe.mamacareplus.entity.QuestionnaireResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionnaireResponseRepository extends JpaRepository<QuestionnaireResponse, Integer> {
    List<QuestionnaireResponse> findByFemme_FemmeId(Integer femmeId);
    List<QuestionnaireResponse> findByQuestionnaire_QuestionnaireId(Integer questionnaireId);
    Optional<QuestionnaireResponse> findFirstByFemme_FemmeIdAndQuestionnaire_TypeIgnoreCaseOrderByCompletedAtDesc(
            Integer femmeId,
            String questionnaireType
    );
}
