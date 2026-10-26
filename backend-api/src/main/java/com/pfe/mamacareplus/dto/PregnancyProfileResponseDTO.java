package com.pfe.mamacareplus.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PregnancyProfileResponseDTO {

    private Integer pregnancyProfileId;
    private Integer femmeId;
    private String username;
    private LocalDate startDate;
    private LocalDate dueDate;
    private Integer currentWeek;
    private Integer currentMonth;
    private Integer remainingDays;
    private Double progressPercentage;
    private String healthStatus;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PregnancyProfileResponseDTO() {}

    public PregnancyProfileResponseDTO(
            Integer pregnancyProfileId,
            Integer femmeId,
            String username,
            LocalDate startDate,
            LocalDate dueDate,
            Integer currentWeek,
            Integer currentMonth,
            Integer remainingDays,
            Double progressPercentage,
            String healthStatus,
            String notes,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.pregnancyProfileId = pregnancyProfileId;
        this.femmeId = femmeId;
        this.username = username;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.currentWeek = currentWeek;
        this.currentMonth = currentMonth;
        this.remainingDays = remainingDays;
        this.progressPercentage = progressPercentage;
        this.healthStatus = healthStatus;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Integer getPregnancyProfileId() { return pregnancyProfileId; }
    public Integer getFemmeId() { return femmeId; }
    public String getUsername() { return username; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getDueDate() { return dueDate; }
    public Integer getCurrentWeek() { return currentWeek; }
    public Integer getCurrentMonth() { return currentMonth; }
    public Integer getRemainingDays() { return remainingDays; }
    public Double getProgressPercentage() { return progressPercentage; }
    public String getHealthStatus() { return healthStatus; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}