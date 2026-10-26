package com.pfe.mamacareplus.repository;

import com.pfe.mamacareplus.entity.WeightRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WeightRecordRepository extends JpaRepository<WeightRecord, Integer> {

    List<WeightRecord> findByPregnancyProfilePregnancyProfileIdOrderByRecordDateDesc(Integer pregnancyProfileId);

    Optional<WeightRecord> findFirstByPregnancyProfilePregnancyProfileIdAndNotesOrderByRecordDateDesc(
            Integer pregnancyProfileId,
            String notes
    );
}
