package com.pfe.mamacareplus.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class QuestionnaireSubmitRequestDTO {

    @NotNull(message = "Femme ID is required")
    private Integer femmeId;
    @NotNull(message = "Questionnaire ID is required")
    private Integer questionnaireId;
    @NotEmpty(message = "Answers list cannot be empty")
    @Valid
    private List<AnswerSubmitRequestDTO> answers;

    public QuestionnaireSubmitRequestDTO() {}

    public Integer getFemmeId() { return femmeId; }
    public void setFemmeId(Integer femmeId) { this.femmeId = femmeId; }

    public Integer getQuestionnaireId() { return questionnaireId; }
    public void setQuestionnaireId(Integer questionnaireId) { this.questionnaireId = questionnaireId; }

    public List<AnswerSubmitRequestDTO> getAnswers() { return answers; }
    public void setAnswers(List<AnswerSubmitRequestDTO> answers) { this.answers = answers; }
}