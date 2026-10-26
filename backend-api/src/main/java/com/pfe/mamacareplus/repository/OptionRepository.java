package com.pfe.mamacareplus.repository;

import com.pfe.mamacareplus.entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OptionRepository extends JpaRepository<Option, Integer> {

    List<Option> findByQuestionQuestionIdOrderByDisplayOrderAscOptionIdAsc(Integer questionId);

    Optional<Option> findByQuestionQuestionIdAndLabelIgnoreCase(Integer questionId, String label);
}
