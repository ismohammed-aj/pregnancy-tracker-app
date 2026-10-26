package com.pfe.mamacareplus.controller;

import com.pfe.mamacareplus.dto.OnboardingRequestDTO;
import com.pfe.mamacareplus.dto.OnboardingResponseDTO;
import com.pfe.mamacareplus.service.OnboardingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
@CrossOrigin("*")
public class OnboardingController {

    private static final Logger logger = LoggerFactory.getLogger(OnboardingController.class);

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @PostMapping("/complete")
    public ResponseEntity<OnboardingResponseDTO> complete(@RequestBody OnboardingRequestDTO request) {
        logger.info("POST /api/onboarding/complete received");
        try {
            return ResponseEntity.ok(onboardingService.completeOnboarding(request));
        } catch (Exception ex) {
            logger.error("Erreur pendant POST /api/onboarding/complete", ex);
            throw ex;
        }
    }

    @GetMapping("/me")
    public ResponseEntity<OnboardingResponseDTO> me() {
        return ResponseEntity.ok(onboardingService.getCurrentOnboarding());
    }
}
