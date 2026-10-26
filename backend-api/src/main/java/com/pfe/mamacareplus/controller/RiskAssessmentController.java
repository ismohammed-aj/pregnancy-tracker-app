package com.pfe.mamacareplus.controller;

import com.pfe.mamacareplus.dto.RiskAssessmentResponseDTO;
import com.pfe.mamacareplus.service.RiskAssessmentService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/risk-assessments")
@CrossOrigin(origins = "*")
public class RiskAssessmentController {

    private final RiskAssessmentService riskAssessmentService;

    public RiskAssessmentController(RiskAssessmentService riskAssessmentService) {
        this.riskAssessmentService = riskAssessmentService;
    }

    @GetMapping("/femme/{femmeId}/latest")
    public RiskAssessmentResponseDTO getLatestByFemmeId(@PathVariable Integer femmeId) {
        return riskAssessmentService.getLatestByFemmeId(femmeId);
    }

    @GetMapping("/femme/{femmeId}")
    public List<RiskAssessmentResponseDTO> getByFemmeId(@PathVariable Integer femmeId) {
        return riskAssessmentService.getByFemmeId(femmeId);
    }
}
