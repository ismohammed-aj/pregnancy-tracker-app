package com.pfe.mamacareplus.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class MedicationReminderResponseDTO {

    private Integer id;
    private Integer femmeId;
    private String username;
    private String medicationName;
    private String dosage;
    private String frequency;
    private String daysOfWeek;
    private LocalTime reminderTime;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;
    private Boolean active;
    private Boolean done;
    private LocalDateTime lastCompletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MedicationReminderResponseDTO() {}

    public MedicationReminderResponseDTO(
            Integer id,
            Integer femmeId,
            String username,
            String medicationName,
            String dosage,
            String frequency,
            String daysOfWeek,
            LocalTime reminderTime,
            LocalDate startDate,
            LocalDate endDate,
            String notes,
            Boolean active,
            Boolean done,
            LocalDateTime lastCompletedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.femmeId = femmeId;
        this.username = username;
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.daysOfWeek = daysOfWeek;
        this.reminderTime = reminderTime;
        this.startDate = startDate;
        this.endDate = endDate;
        this.notes = notes;
        this.active = active;
        this.done = done;
        this.lastCompletedAt = lastCompletedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getId() { return id; }
    public Integer getFemmeId() { return femmeId; }
    public String getUsername() { return username; }
    public String getMedicationName() { return medicationName; }
    public String getDosage() { return dosage; }
    public String getFrequency() { return frequency; }
    public String getDaysOfWeek() { return daysOfWeek; }
    public LocalTime getReminderTime() { return reminderTime; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getNotes() { return notes; }
    public Boolean getActive() { return active; }
    public Boolean getDone() { return done; }
    public LocalDateTime getLastCompletedAt() { return lastCompletedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
