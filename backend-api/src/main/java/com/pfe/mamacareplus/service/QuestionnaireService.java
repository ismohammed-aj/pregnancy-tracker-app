package com.pfe.mamacareplus.service;

import com.pfe.mamacareplus.dto.*;
import com.pfe.mamacareplus.entity.Option;
import com.pfe.mamacareplus.entity.Question;
import com.pfe.mamacareplus.entity.Questionnaire;
import com.pfe.mamacareplus.repository.OptionRepository;
import com.pfe.mamacareplus.repository.QuestionRepository;
import com.pfe.mamacareplus.repository.QuestionnaireRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class QuestionnaireService {

    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;

    public QuestionnaireService(
            QuestionnaireRepository questionnaireRepository,
            QuestionRepository questionRepository,
            OptionRepository optionRepository
    ) {
        this.questionnaireRepository = questionnaireRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
    }

    public List<QuestionnaireResponseDTO> getAllQuestionnaires() {
        return questionnaireRepository.findAll()
                .stream()
                .map(this::mapQuestionnaireToResponseWithQuestions)
                .toList();
    }

    public QuestionnaireResponseDTO getQuestionnaireById(Integer questionnaireId) {
        Questionnaire questionnaire = findQuestionnaireById(questionnaireId);
        return mapQuestionnaireToResponseWithQuestions(questionnaire);
    }

    public List<QuestionnaireResponseDTO> getQuestionnairesByType(String type) {
        if (type == null || type.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type is required");
        }

        return questionnaireRepository.findByTypeIgnoreCase(type)
                .stream()
                .map(this::mapQuestionnaireToResponseWithQuestions)
                .toList();
    }

    public List<QuestionnaireResponseDTO> getQuestionnairesByLevel(String level) {
        if (level == null || level.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Level is required");
        }

        return questionnaireRepository.findByLevelIgnoreCase(level)
                .stream()
                .map(this::mapQuestionnaireToResponseWithQuestions)
                .toList();
    }

    public QuestionnaireResponseDTO createQuestionnaire(QuestionnaireRequestDTO request) {
        validateQuestionnaireRequest(request);

        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setTitle(request.getTitle());
        questionnaire.setDescription(request.getDescription());
        questionnaire.setType(request.getType());
        questionnaire.setLevel(request.getLevel());

        return mapQuestionnaireToResponseWithQuestions(questionnaireRepository.save(questionnaire));
    }

    public QuestionnaireResponseDTO updateQuestionnaire(Integer questionnaireId, QuestionnaireRequestDTO request) {
        Questionnaire questionnaire = findQuestionnaireById(questionnaireId);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            questionnaire.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            questionnaire.setDescription(request.getDescription());
        }

        if (request.getType() != null) {
            questionnaire.setType(request.getType());
        }

        if (request.getLevel() != null) {
            questionnaire.setLevel(request.getLevel());
        }

        return mapQuestionnaireToResponseWithQuestions(questionnaireRepository.save(questionnaire));
    }

    public void deleteQuestionnaire(Integer questionnaireId) {
        Questionnaire questionnaire = findQuestionnaireById(questionnaireId);
        questionnaireRepository.delete(questionnaire);
    }

    public List<QuestionResponseDTO> getQuestionsByQuestionnaireId(Integer questionnaireId) {
        findQuestionnaireById(questionnaireId);

        return questionRepository.findByQuestionnaireQuestionnaireIdOrderByDisplayOrderAscQuestionIdAsc(questionnaireId)
                .stream()
                .map(this::mapQuestionToResponseWithOptions)
                .toList();
    }

    public QuestionResponseDTO getQuestionById(Integer questionId) {
        return mapQuestionToResponseWithOptions(findQuestionById(questionId));
    }

    public QuestionResponseDTO createQuestion(QuestionRequestDTO request) {
        validateQuestionRequest(request);

        Questionnaire questionnaire = findQuestionnaireById(request.getQuestionnaireId());

        Question question = new Question();
        question.setQuestionnaire(questionnaire);
        question.setText(request.getText());
        question.setType(normalizeQuestionType(request.getType()));
        question.setFieldKey(clean(request.getFieldKey()));
        question.setCategory(clean(request.getCategory()));
        question.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        question.setActive(request.getActive() != null ? request.getActive() : true);

        return mapQuestionToResponseWithOptions(questionRepository.save(question));
    }

    public QuestionResponseDTO updateQuestion(Integer questionId, QuestionRequestDTO request) {
        Question question = findQuestionById(questionId);

        if (request.getText() != null && !request.getText().isBlank()) {
            question.setText(request.getText());
        }

        if (request.getType() != null && !request.getType().isBlank()) {
            question.setType(normalizeQuestionType(request.getType()));
        }

        if (request.getFieldKey() != null) {
            question.setFieldKey(clean(request.getFieldKey()));
        }

        if (request.getCategory() != null) {
            question.setCategory(clean(request.getCategory()));
        }

        if (request.getDisplayOrder() != null) {
            question.setDisplayOrder(request.getDisplayOrder());
        }

        if (request.getActive() != null) {
            question.setActive(request.getActive());
        }

        return mapQuestionToResponseWithOptions(questionRepository.save(question));
    }

    public void deleteQuestion(Integer questionId) {
        Question question = findQuestionById(questionId);
        questionRepository.delete(question);
    }

    public List<OptionResponseDTO> getOptionsByQuestionId(Integer questionId) {
        findQuestionById(questionId);

        return optionRepository.findByQuestionQuestionIdOrderByDisplayOrderAscOptionIdAsc(questionId)
                .stream()
                .map(this::mapOptionToResponse)
                .toList();
    }

    public OptionResponseDTO getOptionById(Integer optionId) {
        return mapOptionToResponse(findOptionById(optionId));
    }

    public OptionResponseDTO createOption(OptionRequestDTO request) {
        validateOptionRequest(request);

        Question question = findQuestionById(request.getQuestionId());

        Option option = new Option();
        option.setQuestion(question);
        option.setLabel(request.getLabel());
        option.setScoreValue(request.getScoreValue() != null ? request.getScoreValue() : 0);
        option.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);

        return mapOptionToResponse(optionRepository.save(option));
    }

    public OptionResponseDTO updateOption(Integer optionId, OptionRequestDTO request) {
        Option option = findOptionById(optionId);

        if (request.getLabel() != null && !request.getLabel().isBlank()) {
            option.setLabel(request.getLabel());
        }

        if (request.getScoreValue() != null) {
            option.setScoreValue(request.getScoreValue());
        }

        if (request.getDisplayOrder() != null) {
            option.setDisplayOrder(request.getDisplayOrder());
        }

        return mapOptionToResponse(optionRepository.save(option));
    }

    public void deleteOption(Integer optionId) {
        Option option = findOptionById(optionId);
        optionRepository.delete(option);
    }

    private Questionnaire findQuestionnaireById(Integer questionnaireId) {
        return questionnaireRepository.findById(questionnaireId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Questionnaire not found"));
    }

    private Question findQuestionById(Integer questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
    }

    private Option findOptionById(Integer optionId) {
        return optionRepository.findById(optionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Option not found"));
    }

    private void validateQuestionnaireRequest(QuestionnaireRequestDTO request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Questionnaire title is required");
        }
    }

    private void validateQuestionRequest(QuestionRequestDTO request) {
        if (request.getQuestionnaireId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Questionnaire ID is required");
        }

        if (request.getText() == null || request.getText().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question text is required");
        }

        if (request.getType() == null || request.getType().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question type is required");
        }

        normalizeQuestionType(request.getType());
    }

    private void validateOptionRequest(OptionRequestDTO request) {
        if (request.getQuestionId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question ID is required");
        }

        if (request.getLabel() == null || request.getLabel().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Option label is required");
        }
    }

    private String normalizeQuestionType(String type) {
        String normalized = type.trim().toLowerCase();

        if (!normalized.equals("text")
                && !normalized.equals("date")
                && !normalized.equals("single_choice")
                && !normalized.equals("multiple_choice")
                && !normalized.equals("number")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Question type must be text, date, number, single_choice, or multiple_choice"
            );
        }

        return normalized;
    }

    private QuestionnaireResponseDTO mapQuestionnaireToResponseWithQuestions(Questionnaire questionnaire) {
        List<QuestionResponseDTO> questions = questionRepository
                .findByQuestionnaireQuestionnaireIdOrderByDisplayOrderAscQuestionIdAsc(questionnaire.getQuestionnaireId())
                .stream()
                .map(this::mapQuestionToResponseWithOptions)
                .toList();

        return new QuestionnaireResponseDTO(
                questionnaire.getQuestionnaireId(),
                questionnaire.getTitle(),
                questionnaire.getDescription(),
                questionnaire.getType(),
                questionnaire.getLevel(),
                questionnaire.getCreatedAt(),
                questions
        );
    }

    private QuestionResponseDTO mapQuestionToResponseWithOptions(Question question) {
        List<OptionResponseDTO> options = optionRepository
                .findByQuestionQuestionIdOrderByDisplayOrderAscOptionIdAsc(question.getQuestionId())
                .stream()
                .map(this::mapOptionToResponse)
                .toList();

        return new QuestionResponseDTO(
                question.getQuestionId(),
                question.getQuestionnaire().getQuestionnaireId(),
                question.getText(),
                question.getType(),
                question.getFieldKey(),
                question.getCategory(),
                question.getDisplayOrder(),
                question.getActive(),
                question.getCreatedAt(),
                options
        );
    }

    private OptionResponseDTO mapOptionToResponse(Option option) {
        return new OptionResponseDTO(
                option.getOptionId(),
                option.getQuestion().getQuestionId(),
                option.getLabel(),
                option.getScoreValue(),
                option.getDisplayOrder()
        );
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }
}
