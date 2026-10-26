package com.pfe.mamacareplus.repository;

import com.pfe.mamacareplus.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Integer> {

    List<Question> findByQuestionnaireQuestionnaireIdOrderByDisplayOrderAscQuestionIdAsc(Integer questionnaireId);

    Optional<Question> findByQuestionnaireQuestionnaireIdAndFieldKey(Integer questionnaireId, String fieldKey);
}
