package com.pfe.mamacareplus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OptionRequestDTO {
    
    @NotNull(message = "Question ID is required")
    private Integer questionId;
    @NotBlank(message = "Option label is required")
    private String label;
    private Integer scoreValue;
    private Integer displayOrder;

    public OptionRequestDTO() {}

    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Integer getScoreValue() { return scoreValue; }
    public void setScoreValue(Integer scoreValue) { this.scoreValue = scoreValue; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}
