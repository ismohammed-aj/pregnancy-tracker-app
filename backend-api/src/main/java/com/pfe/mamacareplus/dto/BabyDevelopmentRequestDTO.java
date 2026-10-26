package com.pfe.mamacareplus.dto;

import java.math.BigDecimal;

public class BabyDevelopmentRequestDTO {

    private Integer weekNumber;
    private String babySize;
    private BigDecimal babyWeightGrams;
    private BigDecimal babyLengthCm;
    private String imageUrl;
    private String comparisonType;
    private String comparisonName;
    private String description;
    private String advice;

    public BabyDevelopmentRequestDTO() {}

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
}
