package com.pfe.mamacareplus.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BabyDevelopmentResponseDTO {

    private Integer babyDevelopmentId;
    private Integer weekNumber;
    private String babySize;
    private BigDecimal babyWeightGrams;
    private BigDecimal babyLengthCm;
    private String imageUrl;
    private String comparisonType;
    private String comparisonName;
    private String description;
    private String advice;
    private LocalDateTime createdAt;

    public BabyDevelopmentResponseDTO() {}

    public BabyDevelopmentResponseDTO(
            Integer babyDevelopmentId,
            Integer weekNumber,
            String babySize,
            BigDecimal babyWeightGrams,
            BigDecimal babyLengthCm,
            String imageUrl,
            String comparisonType,
            String comparisonName,
            String description,
            String advice,
            LocalDateTime createdAt
    ) {
        this.babyDevelopmentId = babyDevelopmentId;
        this.weekNumber = weekNumber;
        this.babySize = babySize;
        this.babyWeightGrams = babyWeightGrams;
        this.babyLengthCm = babyLengthCm;
        this.imageUrl = imageUrl;
        this.comparisonType = comparisonType;
        this.comparisonName = comparisonName;
        this.description = description;
        this.advice = advice;
        this.createdAt = createdAt;
    }

    public Integer getBabyDevelopmentId() { return babyDevelopmentId; }
    public Integer getWeekNumber() { return weekNumber; }
    public String getBabySize() { return babySize; }
    public BigDecimal getBabyWeightGrams() { return babyWeightGrams; }
    public BigDecimal getBabyLengthCm() { return babyLengthCm; }
    public String getImageUrl() { return imageUrl; }
    public String getComparisonType() { return comparisonType; }
    public String getComparisonName() { return comparisonName; }
    public String getDescription() { return description; }
    public String getAdvice() { return advice; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
