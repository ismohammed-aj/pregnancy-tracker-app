package com.pfe.mamacareplus.dto;

public class OptionResponseDTO {

    private Integer optionId;
    private Integer questionId;
    private String label;
    private Integer scoreValue;
    private Integer displayOrder;

    public OptionResponseDTO() {}

    public OptionResponseDTO(
            Integer optionId,
            Integer questionId,
            String label,
            Integer scoreValue,
            Integer displayOrder
    ) {
        this.optionId = optionId;
        this.questionId = questionId;
        this.label = label;
        this.scoreValue = scoreValue;
        this.displayOrder = displayOrder;
    }

    public Integer getOptionId() { return optionId; }
    public Integer getQuestionId() { return questionId; }
    public String getLabel() { return label; }
    public Integer getScoreValue() { return scoreValue; }
    public Integer getDisplayOrder() { return displayOrder; }
}
