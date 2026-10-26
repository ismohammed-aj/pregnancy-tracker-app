package com.pfe.mamacareplus.dto;

import java.time.LocalDateTime;
import java.util.List;

public class QuestionnaireResultResponseDTO {

    private Integer ResponseId;
    private Integer femmeId;
    private Integer questionnaireId;
    private Integer totalScore;
    private String riskLevel;
    private String riskDescription;
    private String recommendation;
    private LocalDateTime completedAt;
    private List<AnswerResponseDTO> answers;

    public QuestionnaireResultResponseDTO() {}

    public QuestionnaireResultResponseDTO(
            Integer ResponseId,
            Integer femmeId,
            Integer questionnaireId,
            Integer totalScore,
            String riskLevel,
            String riskDescription,
            String recommendation,
            LocalDateTime completedAt,
            List<AnswerResponseDTO> answers
    ) {
        this.ResponseId = ResponseId;
        this.femmeId = femmeId;
        this.questionnaireId = questionnaireId;
        this.totalScore = totalScore;
        this.riskLevel = riskLevel;
        this.riskDescription = riskDescription;
        this.recommendation = recommendation;
        this.completedAt = completedAt;
        this.answers = answers;
    }

    public Integer getResponseId() { return ResponseId; }
    public Integer getFemmeId() { return femmeId; }
    public Integer getQuestionnaireId() { return questionnaireId; }
    public Integer getTotalScore() { return totalScore; }
    public String getRiskLevel() { return riskLevel; }
    public String getRiskDescription() { return riskDescription; }
    public String getRecommendation() { return recommendation; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public LocalDateTime getSubmittedAt() { return completedAt; }
    public List<AnswerResponseDTO> getAnswers() { return answers; }
}
