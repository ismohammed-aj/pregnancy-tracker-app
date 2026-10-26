package com.pfe.mamacareplus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class QuestionRequestDTO {

    @NotNull(message = "Questionnaire ID is required")
    private Integer questionnaireId;
    @NotBlank(message = "Question text is required")
    private String text;
    @NotBlank(message = "Question type is required")
    private String type;
    private String fieldKey;
    private String category;
    private Integer displayOrder;
    private Boolean active;

    public QuestionRequestDTO() {}

    public Integer getQuestionnaireId() { return questionnaireId; }
    public void setQuestionnaireId(Integer questionnaireId) { this.questionnaireId = questionnaireId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
