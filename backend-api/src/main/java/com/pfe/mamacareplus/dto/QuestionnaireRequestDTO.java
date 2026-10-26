package com.pfe.mamacareplus.dto;

import jakarta.validation.constraints.NotBlank;

public class QuestionnaireRequestDTO {
    @NotBlank(message = "Questionnaire title is required")
    private String title;
    private String description;
    private String type;
    private String level;

    public QuestionnaireRequestDTO() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}