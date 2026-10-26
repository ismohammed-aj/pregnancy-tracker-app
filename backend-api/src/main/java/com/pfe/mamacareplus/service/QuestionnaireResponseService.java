package com.pfe.mamacareplus.service;

import com.pfe.mamacareplus.dto.AnswerResponseDTO;
import com.pfe.mamacareplus.dto.AnswerSubmitRequestDTO;
import com.pfe.mamacareplus.dto.QuestionnaireResultResponseDTO;
import com.pfe.mamacareplus.dto.QuestionnaireSubmitRequestDTO;
import com.pfe.mamacareplus.entity.Answer;
import com.pfe.mamacareplus.entity.Femme;
import com.pfe.mamacareplus.entity.Option;
import com.pfe.mamacareplus.entity.Question;
import com.pfe.mamacareplus.entity.Questionnaire;
import com.pfe.mamacareplus.entity.QuestionnaireResponse;
import com.pfe.mamacareplus.entity.RiskAssessment;
import com.pfe.mamacareplus.repository.FemmeRepository;
import com.pfe.mamacareplus.repository.OptionRepository;
import com.pfe.mamacareplus.repository.QuestionRepository;
import com.pfe.mamacareplus.repository.QuestionnaireRepository;
import com.pfe.mamacareplus.repository.QuestionnaireResponseRepository;
import com.pfe.mamacareplus.repository.RiskAssessmentRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class QuestionnaireResponseService {

    private final QuestionnaireResponseRepository questionnaireResponseRepository;
    private final FemmeRepository femmeRepository;
    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final CurrentUserService currentUserService;

    public QuestionnaireResponseService(
            QuestionnaireResponseRepository questionnaireResponseRepository,
            FemmeRepository femmeRepository,
            QuestionnaireRepository questionnaireRepository,
            QuestionRepository questionRepository,
            OptionRepository optionRepository,
            RiskAssessmentRepository riskAssessmentRepository,
            CurrentUserService currentUserService
    ) {
        this.questionnaireResponseRepository = questionnaireResponseRepository;
        this.femmeRepository = femmeRepository;
        this.questionnaireRepository = questionnaireRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.currentUserService = currentUserService;
    }

    public QuestionnaireResultResponseDTO submitQuestionnaire(QuestionnaireSubmitRequestDTO request) {
        Femme femme = femmeRepository.findById(request.getFemmeId())
                .orElseThrow(() -> new RuntimeException("Femme not found with id: " + request.getFemmeId()));

        currentUserService.checkFemmeAccess(femme.getFemmeId());

        Questionnaire questionnaire = questionnaireRepository.findById(request.getQuestionnaireId())
                .orElseThrow(() -> new RuntimeException("Questionnaire not found with id: " + request.getQuestionnaireId()));

        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            throw new RuntimeException("Answers list cannot be empty");
        }

        QuestionnaireResponse response = new QuestionnaireResponse();
        response.setFemme(femme);
        response.setQuestionnaire(questionnaire);

        int totalScore = 0;

        for (AnswerSubmitRequestDTO answerRequest : request.getAnswers()) {
            Question question = questionRepository.findById(answerRequest.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question not found with id: " + answerRequest.getQuestionId()));

            if (!question.getQuestionnaire().getQuestionnaireId().equals(questionnaire.getQuestionnaireId())) {
                throw new RuntimeException("Question does not belong to this questionnaire");
            }

            Answer answer = buildAnswer(question, answerRequest);
            totalScore += answer.getScore() != null ? answer.getScore() : 0;
            response.addAnswer(answer);
        }

        QuestionnaireResponse savedResponse = questionnaireResponseRepository.save(response);
        RiskAssessment assessment = saveRiskAssessment(savedResponse, totalScore);

        return mapToDTO(savedResponse, assessment);
    }

    public List<QuestionnaireResultResponseDTO> getAllResponses() {
        if (!currentUserService.isAdmin()) {
            Integer currentFemmeId = currentUserService.getCurrentFemmeId();

            return questionnaireResponseRepository.findByFemme_FemmeId(currentFemmeId)
                    .stream()
                    .map(this::mapToDTO)
                    .toList();
        }

        return questionnaireResponseRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public QuestionnaireResultResponseDTO getResponseById(Integer responseId) {
        QuestionnaireResponse response = questionnaireResponseRepository.findById(responseId)
                .orElseThrow(() -> new RuntimeException("Questionnaire response not found with id: " + responseId));

        currentUserService.checkFemmeAccess(response.getFemme().getFemmeId());

        return mapToDTO(response);
    }

    public List<QuestionnaireResultResponseDTO> getResponsesByFemme(Integer femmeId) {
        currentUserService.checkFemmeAccess(femmeId);

        return questionnaireResponseRepository.findByFemme_FemmeId(femmeId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public void deleteResponse(Integer responseId) {
        QuestionnaireResponse response = questionnaireResponseRepository.findById(responseId)
                .orElseThrow(() -> new RuntimeException("Questionnaire response not found with id: " + responseId));

        currentUserService.checkFemmeAccess(response.getFemme().getFemmeId());

        questionnaireResponseRepository.delete(response);
    }

    private Answer buildAnswer(Question question, AnswerSubmitRequestDTO answerRequest) {
        Answer answer = new Answer();
        answer.setQuestion(question);

        if (requiresNumberAnswer(question)) {
            if (answerRequest.getNumberAnswer() == null) {
                throw new RuntimeException("Number answer is required for question: " + question.getQuestionId());
            }
            answer.setNumberAnswer(answerRequest.getNumberAnswer());
            answer.setScore(0);
            return answer;
        }

        if (requiresTextAnswer(question)) {
            String textAnswer = answerRequest.getTextAnswer();
            if (textAnswer == null || textAnswer.trim().isEmpty()) {
                throw new RuntimeException("Text answer is required for question: " + question.getQuestionId());
            }
            answer.setTextAnswer(textAnswer.trim());
            answer.setScore(0);
            return answer;
        }

        if (answerRequest.getOptionId() == null) {
            throw new RuntimeException("Option ID is required for question: " + question.getQuestionId());
        }

        Option option = optionRepository.findById(answerRequest.getOptionId())
                .orElseThrow(() -> new RuntimeException("Option not found with id: " + answerRequest.getOptionId()));

        if (!option.getQuestion().getQuestionId().equals(question.getQuestionId())) {
            throw new RuntimeException("Option does not belong to this question");
        }

        answer.setSelectedOption(option);
        answer.setScore(option.getScoreValue() != null ? option.getScoreValue() : 0);
        return answer;
    }

    private RiskAssessment saveRiskAssessment(QuestionnaireResponse response, int totalScore) {
        RiskAssessment assessment = new RiskAssessment();
        assessment.setFemme(response.getFemme());
        assessment.setQuestionnaireResponse(response);
        assessment.setTotalScore(totalScore);
        assessment.setRiskLevel(calculateRiskLevel(totalScore));
        assessment.setDescription(calculateClassificationType(totalScore));
        assessment.setRecommendation(calculateRecommendation(totalScore));
        return riskAssessmentRepository.save(assessment);
    }

    private String calculateRiskLevel(int totalScore) {
        if (totalScore <= 20) {
            return "NORMAL";
        }
        if (totalScore <= 40) {
            return "ATTENTION";
        }
        return "RISQUE";
    }

    private String calculateClassificationType(int totalScore) {
        if (totalScore <= 20) {
            return "Bon etat";
        }
        if (totalScore <= 40) {
            return "A surveiller";
        }
        return "Risque eleve";
    }

    private String calculateRecommendation(int totalScore) {
        if (totalScore <= 20) {
            return "Continuez le suivi habituel et gardez vos informations a jour.";
        }
        if (totalScore <= 40) {
            return "Surveillez les signes importants et discutez-en avec un professionnel de sante.";
        }
        return "Prenez contact rapidement avec un professionnel de sante pour une evaluation.";
    }

    private boolean requiresNumberAnswer(Question question) {
        String type = question.getType();
        return type != null && type.equalsIgnoreCase("number");
    }

    private boolean requiresTextAnswer(Question question) {
        String type = question.getType();
        return type != null && (
                type.equalsIgnoreCase("text")
                        || type.equalsIgnoreCase("date")
        );
    }

    private QuestionnaireResultResponseDTO mapToDTO(QuestionnaireResponse response) {
        RiskAssessment assessment = riskAssessmentRepository
                .findByQuestionnaireResponseResponseId(response.getResponseId())
                .orElseGet(() -> buildTransientAssessment(response));
        return mapToDTO(response, assessment);
    }

    private QuestionnaireResultResponseDTO mapToDTO(QuestionnaireResponse response, RiskAssessment assessment) {
        List<AnswerResponseDTO> answerDTOs = response.getAnswers()
                .stream()
                .map(answer -> {
                    Option selectedOption = answer.getSelectedOption();
                    Integer optionId = selectedOption != null ? selectedOption.getOptionId() : null;
                    String optionLabel = selectedOption != null ? selectedOption.getLabel() : null;

                    return new AnswerResponseDTO(
                            answer.getAnswerId(),
                            answer.getQuestion().getQuestionId(),
                            answer.getQuestion().getText(),
                            optionId,
                            optionLabel,
                            answer.getTextAnswer(),
                            answer.getNumberAnswer(),
                            answer.getScore() != null ? answer.getScore() : 0
                    );
                })
                .toList();

        return new QuestionnaireResultResponseDTO(
                response.getResponseId(),
                response.getFemme().getFemmeId(),
                response.getQuestionnaire().getQuestionnaireId(),
                assessment.getTotalScore(),
                assessment.getRiskLevel(),
                assessment.getDescription(),
                assessment.getRecommendation(),
                response.getCompletedAt(),
                answerDTOs
        );
    }

    private RiskAssessment buildTransientAssessment(QuestionnaireResponse response) {
        int totalScore = response.getAnswers()
                .stream()
                .mapToInt(answer -> answer.getScore() != null ? answer.getScore() : 0)
                .sum();

        RiskAssessment assessment = new RiskAssessment();
        assessment.setFemme(response.getFemme());
        assessment.setQuestionnaireResponse(response);
        assessment.setTotalScore(totalScore);
        assessment.setRiskLevel(calculateRiskLevel(totalScore));
        assessment.setDescription(calculateClassificationType(totalScore));
        assessment.setRecommendation(calculateRecommendation(totalScore));
        assessment.setEvaluatedAt(response.getCompletedAt());
        return assessment;
    }
}
