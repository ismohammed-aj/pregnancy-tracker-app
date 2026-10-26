package com.pfe.mamacareplus.dto;

import java.time.LocalDateTime;
import java.util.List;

public class QuestionnaireResponseDTO {

    private Integer questionnaireId;
    private String title;
    private String description;
    private String type;
    private String level;
    private LocalDateTime createdAt;
    private List<QuestionResponseDTO> questions;

    public QuestionnaireResponseDTO() {}

    public QuestionnaireResponseDTO(
            Integer questionnaireId,
            String title,
            String description,
            String type,
            String level,
            LocalDateTime createdAt,
            List<QuestionResponseDTO> questions
    ) {
        this.questionnaireId = questionnaireId;
        this.title = title;
        this.description = description;
        this.type = type;
        this.level = level;
        this.createdAt = createdAt;
        this.questions = questions;
    }

    public Integer getQuestionnaireId() { return questionnaireId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public String getLevel() { return level; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<QuestionResponseDTO> getQuestions() { return questions; }
}