package com.pfe.mamacareplus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_assessments")
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "risk_assessment_id")
    private Integer riskAssessmentId;

    @ManyToOne
    @JoinColumn(name = "femme_id", nullable = false)
    private Femme femme;

    @OneToOne
    @JoinColumn(name = "questionnaire_response_id")
    private QuestionnaireResponse questionnaireResponse;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore = 0;

    @Column(name = "risk_level", nullable = false, length = 100)
    private String riskLevel;

    @Column(name = "description")
    private String description;

    @Column(name = "recommendation")
    private String recommendation;

    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt;

    public RiskAssessment() {}

    @PrePersist
    protected void onCreate() {
        if (this.evaluatedAt == null) {
            this.evaluatedAt = LocalDateTime.now();
        }
    }

    public Integer getRiskAssessmentId() { return riskAssessmentId; }
    public void setRiskAssessmentId(Integer riskAssessmentId) { this.riskAssessmentId = riskAssessmentId; }

    public Femme getFemme() { return femme; }
    public void setFemme(Femme femme) { this.femme = femme; }

    public QuestionnaireResponse getQuestionnaireResponse() { return questionnaireResponse; }
    public void setQuestionnaireResponse(QuestionnaireResponse questionnaireResponse) {
        this.questionnaireResponse = questionnaireResponse;
    }

    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
