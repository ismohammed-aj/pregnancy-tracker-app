package com.pfe.mamacareplus.repository;

import com.pfe.mamacareplus.entity.MedicationReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MedicationReminderRepository extends JpaRepository<MedicationReminder, Integer> {

    List<MedicationReminder> findByFemmeFemmeIdOrderByReminderTimeAsc(Integer femmeId);

    List<MedicationReminder> findByFemmeUserUserIdOrderByReminderTimeAsc(Integer userId);

    @Query("""
            SELECT reminder FROM MedicationReminder reminder
            WHERE reminder.femme.femmeId = :femmeId
              AND reminder.active = true
              AND reminder.startDate <= :date
              AND (reminder.endDate IS NULL OR reminder.endDate >= :date)
            ORDER BY reminder.reminderTime ASC
            """)
    List<MedicationReminder> findActiveForDate(
            @Param("femmeId") Integer femmeId,
            @Param("date") LocalDate date
    );
}
