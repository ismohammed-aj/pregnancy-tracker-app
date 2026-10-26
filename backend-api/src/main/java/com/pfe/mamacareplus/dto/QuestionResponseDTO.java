package com.pfe.mamacareplus.dto;

import java.time.LocalDateTime;
import java.util.List;

public class QuestionResponseDTO {

    private Integer questionId;
    private Integer questionnaireId;
    private String text;
    private String type;
    private String fieldKey;
    private String category;
    private Integer displayOrder;
    private Boolean active;
    private LocalDateTime createdAt;
    private List<OptionResponseDTO> options;

    public QuestionResponseDTO() {}

    public QuestionResponseDTO(
            Integer questionId,
            Integer questionnaireId,
            String text,
            String type,
            String fieldKey,
            String category,
            Integer displayOrder,
            Boolean active,
            LocalDateTime createdAt,
            List<OptionResponseDTO> options
    ) {
        this.questionId = questionId;
        this.questionnaireId = questionnaireId;
        this.text = text;
        this.type = type;
        this.fieldKey = fieldKey;
        this.category = category;
        this.displayOrder = displayOrder;
        this.active = active;
        this.createdAt = createdAt;
        this.options = options;
    }

    public Integer getQuestionId() { return questionId; }
    public Integer getQuestionnaireId() { return questionnaireId; }
    public String getText() { return text; }
    public String getType() { return type; }
    public String getFieldKey() { return fieldKey; }
    public String getCategory() { return category; }
    public Integer getDisplayOrder() { return displayOrder; }
    public Boolean getActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OptionResponseDTO> getOptions() { return options; }
}
