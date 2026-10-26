package com.pfe.mamacareplus.dto;

import java.time.LocalDateTime;

public class RiskAssessmentResponseDTO {

    private Integer riskAssessmentId;
    private Integer femmeId;
    private Integer responseId;
    private Integer totalScore;
    private String riskLevel;
    private String description;
    private String recommendation;
    private LocalDateTime evaluatedAt;

    public RiskAssessmentResponseDTO() {}

    public RiskAssessmentResponseDTO(
            Integer riskAssessmentId,
            Integer femmeId,
            Integer responseId,
            Integer totalScore,
            String riskLevel,
            String description,
            String recommendation,
            LocalDateTime evaluatedAt
    ) {
        this.riskAssessmentId = riskAssessmentId;
        this.femmeId = femmeId;
        this.responseId = responseId;
        this.totalScore = totalScore;
        this.riskLevel = riskLevel;
        this.description = description;
        this.recommendation = recommendation;
        this.evaluatedAt = evaluatedAt;
    }

    public Integer getRiskAssessmentId() { return riskAssessmentId; }
    public Integer getFemmeId() { return femmeId; }
    public Integer getResponseId() { return responseId; }
    public Integer getTotalScore() { return totalScore; }
    public String getRiskLevel() { return riskLevel; }
    public String getDescription() { return description; }
    public String getRecommendation() { return recommendation; }
    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
}
