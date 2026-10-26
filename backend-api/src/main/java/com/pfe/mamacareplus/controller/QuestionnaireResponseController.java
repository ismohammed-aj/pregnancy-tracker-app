package com.pfe.mamacareplus.controller;

import com.pfe.mamacareplus.dto.QuestionnaireResultResponseDTO;
import com.pfe.mamacareplus.dto.QuestionnaireSubmitRequestDTO;
import com.pfe.mamacareplus.service.QuestionnaireResponseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/questionnaire-responses")
@CrossOrigin("*")
public class QuestionnaireResponseController {

    private final QuestionnaireResponseService questionnaireResponseService;

    public QuestionnaireResponseController(QuestionnaireResponseService questionnaireResponseService) {
        this.questionnaireResponseService = questionnaireResponseService;
    }

    @PostMapping("/submit")
    public ResponseEntity<QuestionnaireResultResponseDTO> submitQuestionnaire(
            @Valid @RequestBody QuestionnaireSubmitRequestDTO request
    ) {
        return ResponseEntity.ok(questionnaireResponseService.submitQuestionnaire(request));
    }

    @GetMapping
    public ResponseEntity<List<QuestionnaireResultResponseDTO>> getAllResponses() {
        return ResponseEntity.ok(questionnaireResponseService.getAllResponses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionnaireResultResponseDTO> getResponseById(@PathVariable Integer id) {
        return ResponseEntity.ok(questionnaireResponseService.getResponseById(id));
    }

    @GetMapping("/femme/{femmeId}")
    public ResponseEntity<List<QuestionnaireResultResponseDTO>> getResponsesByFemme(@PathVariable Integer femmeId) {
        return ResponseEntity.ok(questionnaireResponseService.getResponsesByFemme(femmeId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResponse(@PathVariable Integer id) {
        questionnaireResponseService.deleteResponse(id);
        return ResponseEntity.noContent().build();
    }
}