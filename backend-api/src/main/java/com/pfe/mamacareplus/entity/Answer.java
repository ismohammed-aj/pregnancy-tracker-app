package com.pfe.mamacareplus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Integer answerId;

    @ManyToOne
    @JoinColumn(name = "response_id", nullable = false)
    private QuestionnaireResponse questionnaireResponse;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne
    @JoinColumn(name = "option_id")
    private Option selectedOption;

    @Column(name = "text_answer")
    private String textAnswer;

    @Column(name = "number_answer")
    private Double numberAnswer;

    @Column(name = "score")
    private Integer score = 0;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    public Answer() {}

    @PrePersist
    protected void onCreate() {
        this.answeredAt = LocalDateTime.now();
    }

    public Integer getAnswerId() { return answerId; }

    public QuestionnaireResponse getQuestionnaireResponse() { return questionnaireResponse; }
    public void setQuestionnaireResponse(QuestionnaireResponse questionnaireResponse) {
        this.questionnaireResponse = questionnaireResponse;
    }

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }

    public Option getSelectedOption() { return selectedOption; }
    public void setSelectedOption(Option selectedOption) { this.selectedOption = selectedOption; }

    public String getTextAnswer() { return textAnswer; }
    public void setTextAnswer(String textAnswer) { this.textAnswer = textAnswer; }

    public Double getNumberAnswer() { return numberAnswer; }
    public void setNumberAnswer(Double numberAnswer) { this.numberAnswer = numberAnswer; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public LocalDateTime getAnsweredAt() { return answeredAt; }
}
