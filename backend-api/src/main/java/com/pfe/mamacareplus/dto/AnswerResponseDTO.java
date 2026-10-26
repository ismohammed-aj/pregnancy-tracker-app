package com.pfe.mamacareplus.dto;

public class AnswerResponseDTO {

    private Integer answerId;
    private Integer questionId;
    private String questionText;
    private Integer optionId;
    private String optionLabel;
    private String textAnswer;
    private Double numberAnswer;
    private Integer score;

    public AnswerResponseDTO() {}

    public AnswerResponseDTO(Integer answerId, Integer questionId, String questionText,
                             Integer optionId, String optionLabel, String textAnswer,
                             Double numberAnswer, Integer score) {
        this.answerId = answerId;
        this.questionId = questionId;
        this.questionText = questionText;
        this.optionId = optionId;
        this.optionLabel = optionLabel;
        this.textAnswer = textAnswer;
        this.numberAnswer = numberAnswer;
        this.score = score;
    }

    public Integer getId() { return answerId; }
    public Integer getAnswerId() { return answerId; }
    public Integer getQuestionId() { return questionId; }
    public String getQuestionText() { return questionText; }
    public Integer getOptionId() { return optionId; }
    public String getOptionLabel() { return optionLabel; }
    public String getTextAnswer() { return textAnswer; }
    public Double getNumberAnswer() { return numberAnswer; }
    public Integer getScore() { return score; }
}
