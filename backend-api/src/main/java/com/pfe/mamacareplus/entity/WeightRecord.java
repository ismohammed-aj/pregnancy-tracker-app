package com.pfe.mamacareplus.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "weight_records")
public class WeightRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weight_record_id")
    private Integer weightRecordId;

    @ManyToOne
    @JoinColumn(name = "pregnancy_profile_id", nullable = false)
    private PregnancyProfile pregnancyProfile;

    @Column(name = "weight_kg", nullable = false, precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "bmi", precision = 5, scale = 2)
    private BigDecimal bmi;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public WeightRecord() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.recordDate == null) {
            this.recordDate = LocalDate.now();
        }
    }

    public Integer getWeightRecordId() { return weightRecordId; }
    public void setWeightRecordId(Integer weightRecordId) { this.weightRecordId = weightRecordId; }

    public PregnancyProfile getPregnancyProfile() { return pregnancyProfile; }
    public void setPregnancyProfile(PregnancyProfile pregnancyProfile) { this.pregnancyProfile = pregnancyProfile; }

    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }

    public BigDecimal getBmi() { return bmi; }
    public void setBmi(BigDecimal bmi) { this.bmi = bmi; }

    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}