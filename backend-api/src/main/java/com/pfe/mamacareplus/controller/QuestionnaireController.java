package com.pfe.mamacareplus.controller;

import com.pfe.mamacareplus.dto.*;
import com.pfe.mamacareplus.service.QuestionnaireService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/questionnaires")
@CrossOrigin(origins = "*")
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;

    public QuestionnaireController(QuestionnaireService questionnaireService) {
        this.questionnaireService = questionnaireService;
    }

    @GetMapping
    public List<QuestionnaireResponseDTO> getAllQuestionnaires() {
        return questionnaireService.getAllQuestionnaires();
    }

    @GetMapping("/{questionnaireId}")
    public QuestionnaireResponseDTO getQuestionnaireById(@PathVariable Integer questionnaireId) {
        return questionnaireService.getQuestionnaireById(questionnaireId);
    }

    @GetMapping("/type/{type}")
    public List<QuestionnaireResponseDTO> getQuestionnairesByType(@PathVariable String type) {
        return questionnaireService.getQuestionnairesByType(type);
    }

    @GetMapping("/level/{level}")
    public List<QuestionnaireResponseDTO> getQuestionnairesByLevel(@PathVariable String level) {
        return questionnaireService.getQuestionnairesByLevel(level);
    }

    @PostMapping
    public QuestionnaireResponseDTO createQuestionnaire(@Valid @RequestBody QuestionnaireRequestDTO request) {
        return questionnaireService.createQuestionnaire(request);
    }

    @PutMapping("/{questionnaireId}")
    public QuestionnaireResponseDTO updateQuestionnaire(
            @PathVariable Integer questionnaireId,
            @Valid @RequestBody QuestionnaireRequestDTO request
    ) {
        return questionnaireService.updateQuestionnaire(questionnaireId, request);
    }

    @DeleteMapping("/{questionnaireId}")
    public ResponseEntity<Void> deleteQuestionnaire(@PathVariable Integer questionnaireId) {
        questionnaireService.deleteQuestionnaire(questionnaireId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{questionnaireId}/questions")
    public List<QuestionResponseDTO> getQuestionsByQuestionnaireId(@PathVariable Integer questionnaireId) {
        return questionnaireService.getQuestionsByQuestionnaireId(questionnaireId);
    }

    @GetMapping("/questions/{questionId}")
    public QuestionResponseDTO getQuestionById(@PathVariable Integer questionId) {
        return questionnaireService.getQuestionById(questionId);
    }

    @PostMapping("/questions")
    public QuestionResponseDTO createQuestion(@RequestBody QuestionRequestDTO request) {
        return questionnaireService.createQuestion(request);
    }

    @PutMapping("/questions/{questionId}")
    public QuestionResponseDTO updateQuestion(
            @PathVariable Integer questionId,
            @RequestBody QuestionRequestDTO request
    ) {
        return questionnaireService.updateQuestion(questionId, request);
    }

    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Integer questionId) {
        questionnaireService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/questions/{questionId}/options")
    public List<OptionResponseDTO> getOptionsByQuestionId(@PathVariable Integer questionId) {
        return questionnaireService.getOptionsByQuestionId(questionId);
    }

    @GetMapping("/options/{optionId}")
    public OptionResponseDTO getOptionById(@PathVariable Integer optionId) {
        return questionnaireService.getOptionById(optionId);
    }

    @PostMapping("/options")
    public OptionResponseDTO createOption(@RequestBody OptionRequestDTO request) {
        return questionnaireService.createOption(request);
    }

    @PutMapping("/options/{optionId}")
    public OptionResponseDTO updateOption(
            @PathVariable Integer optionId,
            @RequestBody OptionRequestDTO request
    ) {
        return questionnaireService.updateOption(optionId, request);
    }

    @DeleteMapping("/options/{optionId}")
    public ResponseEntity<Void> deleteOption(@PathVariable Integer optionId) {
        questionnaireService.deleteOption(optionId);
        return ResponseEntity.noContent().build();
    }
}