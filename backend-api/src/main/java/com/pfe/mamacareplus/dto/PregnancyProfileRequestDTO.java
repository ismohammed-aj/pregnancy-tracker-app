package com.pfe.mamacareplus.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;

public class PregnancyProfileRequestDTO {
    @NotNull(message = "Femme ID is required")
    private Integer femmeId;
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    @NotNull(message = "Due date is required")
    private LocalDate dueDate;
    private String healthStatus;
    private String notes;

    public PregnancyProfileRequestDTO() {}

    public Integer getFemmeId() { return femmeId; }
    public void setFemmeId(Integer femmeId) { this.femmeId = femmeId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}