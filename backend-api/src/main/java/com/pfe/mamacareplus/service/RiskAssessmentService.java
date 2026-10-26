package com.pfe.mamacareplus.service;

import com.pfe.mamacareplus.dto.RiskAssessmentResponseDTO;
import com.pfe.mamacareplus.entity.QuestionnaireResponse;
import com.pfe.mamacareplus.entity.RiskAssessment;
import com.pfe.mamacareplus.repository.RiskAssessmentRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RiskAssessmentService {

    private final RiskAssessmentRepository riskAssessmentRepository;
    private final CurrentUserService currentUserService;

    public RiskAssessmentService(
            RiskAssessmentRepository riskAssessmentRepository,
            CurrentUserService currentUserService
    ) {
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.currentUserService = currentUserService;
    }

    public RiskAssessment saveAssessment(
            QuestionnaireResponse response,
            Integer totalScore,
            String riskLevel,
            String description,
            String recommendation
    ) {
        RiskAssessment assessment = new RiskAssessment();
        assessment.setFemme(response.getFemme());
        assessment.setQuestionnaireResponse(response);
        assessment.setTotalScore(totalScore != null ? totalScore : 0);
        assessment.setRiskLevel(riskLevel);
        assessment.setDescription(description);
        assessment.setRecommendation(recommendation);
        return riskAssessmentRepository.save(assessment);
    }

    public RiskAssessmentResponseDTO getLatestByFemmeId(Integer femmeId) {
        currentUserService.checkFemmeAccess(femmeId);
        return riskAssessmentRepository.findFirstByFemmeFemmeIdOrderByEvaluatedAtDesc(femmeId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Risk assessment not found"));
    }

    public List<RiskAssessmentResponseDTO> getByFemmeId(Integer femmeId) {
        currentUserService.checkFemmeAccess(femmeId);
        return riskAssessmentRepository.findByFemmeFemmeIdOrderByEvaluatedAtDesc(femmeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public RiskAssessmentResponseDTO mapToResponse(RiskAssessment assessment) {
        Integer responseId = assessment.getQuestionnaireResponse() == null
                ? null
                : assessment.getQuestionnaireResponse().getResponseId();

        return new RiskAssessmentResponseDTO(
                assessment.getRiskAssessmentId(),
                assessment.getFemme().getFemmeId(),
                responseId,
                assessment.getTotalScore(),
                assessment.getRiskLevel(),
                assessment.getDescription(),
                assessment.getRecommendation(),
                assessment.getEvaluatedAt()
        );
    }
}
