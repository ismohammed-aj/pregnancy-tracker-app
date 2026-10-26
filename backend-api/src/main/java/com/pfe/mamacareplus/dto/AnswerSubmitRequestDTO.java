package com.pfe.mamacareplus.dto;
import jakarta.validation.constraints.NotNull;

public class AnswerSubmitRequestDTO {

    @NotNull(message = "Question ID is required")
    private Integer questionId;
    private Integer optionId;
    private String textAnswer;
    private Double numberAnswer;

    public AnswerSubmitRequestDTO() {}

    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }

    public Integer getOptionId() { return optionId; }
    public void setOptionId(Integer optionId) { this.optionId = optionId; }

    public String getTextAnswer() { return textAnswer; }
    public void setTextAnswer(String textAnswer) { this.textAnswer = textAnswer; }

    public Double getNumberAnswer() { return numberAnswer; }
    public void setNumberAnswer(Double numberAnswer) { this.numberAnswer = numberAnswer; }
}
