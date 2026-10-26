package com.pfe.mamacareplus.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class WeightRecordRequestDTO {
    @NotNull(message = "Pregnancy profile ID is required")
    private Integer pregnancyProfileId;
    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.1", message = "Weight must be positive")
    private BigDecimal weightKg;
    private BigDecimal bmi;
    private LocalDate recordDate;
    private String notes;

    public WeightRecordRequestDTO() {}

    public Integer getPregnancyProfileId() { return pregnancyProfileId; }
    public void setPregnancyProfileId(Integer pregnancyProfileId) { this.pregnancyProfileId = pregnancyProfileId; }

    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }

    public BigDecimal getBmi() { return bmi; }
    public void setBmi(BigDecimal bmi) { this.bmi = bmi; }

    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}