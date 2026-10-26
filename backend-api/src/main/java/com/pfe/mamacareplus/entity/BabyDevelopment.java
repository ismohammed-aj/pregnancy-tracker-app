package com.pfe.mamacareplus.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "baby_developments")
public class BabyDevelopment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "baby_development_id")
    private Integer babyDevelopmentId;

    @Column(name = "week_number", nullable = false, unique = true)
    private Integer weekNumber;

    @Column(name = "baby_size", length = 100)
    private String babySize;

    @Column(name = "baby_weight_grams", precision = 7, scale = 2)
    private BigDecimal babyWeightGrams;

    @Column(name = "baby_length_cm", precision = 5, scale = 2)
    private BigDecimal babyLengthCm;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "comparison_type", length = 40)
    private String comparisonType;

    @Column(name = "comparison_name", length = 120)
    private String comparisonName;

    @Column(name = "description")
    private String description;

    @Column(name = "advice")
    private String advice;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public BabyDevelopment() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Integer getBabyDevelopmentId() { return babyDevelopmentId; }
    public void setBabyDevelopmentId(Integer babyDevelopmentId) { this.babyDevelopmentId = babyDevelopmentId; }

    public Integer getWeekNumber() { return weekNumber; }
    public void setWeekNumber(Integer weekNumber) { this.weekNumber = weekNumber; }

    public String getBabySize() { return babySize; }
    public void setBabySize(String babySize) { this.babySize = babySize; }

    public BigDecimal getBabyWeightGrams() { return babyWeightGrams; }
    public void setBabyWeightGrams(BigDecimal babyWeightGrams) { this.babyWeightGrams = babyWeightGrams; }

    public BigDecimal getBabyLengthCm() { return babyLengthCm; }
    public void setBabyLengthCm(BigDecimal babyLengthCm) { this.babyLengthCm = babyLengthCm; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getComparisonType() { return comparisonType; }
    public void setComparisonType(String comparisonType) { this.comparisonType = comparisonType; }

    public String getComparisonName() { return comparisonName; }
    public void setComparisonName(String comparisonName) { this.comparisonName = comparisonName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAdvice() { return advice; }
    public void setAdvice(String advice) { this.advice = advice; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
