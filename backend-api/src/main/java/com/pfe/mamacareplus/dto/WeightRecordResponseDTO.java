package com.pfe.mamacareplus.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class WeightRecordResponseDTO {

    private Integer weightRecordId;
    private Integer pregnancyProfileId;
    private BigDecimal weightKg;
    private BigDecimal bmi;
    private LocalDate recordDate;
    private String notes;
    private LocalDateTime createdAt;

    public WeightRecordResponseDTO() {}

    public WeightRecordResponseDTO(
            Integer weightRecordId,
            Integer pregnancyProfileId,
            BigDecimal weightKg,
            BigDecimal bmi,
            LocalDate recordDate,
            String notes,
            LocalDateTime createdAt
    ) {
        this.weightRecordId = weightRecordId;
        this.pregnancyProfileId = pregnancyProfileId;
        this.weightKg = weightKg;
        this.bmi = bmi;
        this.recordDate = recordDate;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public Integer getWeightRecordId() { return weightRecordId; }
    public Integer getPregnancyProfileId() { return pregnancyProfileId; }
    public BigDecimal getWeightKg() { return weightKg; }
    public BigDecimal getBmi() { return bmi; }
    public LocalDate getRecordDate() { return recordDate; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}