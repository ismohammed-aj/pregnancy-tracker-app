package com.pfe.mamacareplus.repository;

import com.pfe.mamacareplus.entity.Questionnaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Integer> {

    List<Questionnaire> findByTypeIgnoreCase(String type);

    List<Questionnaire> findByLevelIgnoreCase(String level);

    Optional<Questionnaire> findFirstByTypeIgnoreCaseOrderByQuestionnaireIdAsc(String type);
}
