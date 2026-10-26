package com.pfe.mamacareplus.controller;

import com.pfe.mamacareplus.dto.MessageResponseDTO;
import com.pfe.mamacareplus.service.EmailService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
@ConditionalOnProperty(name = "mamacareplus.test-email.enabled", havingValue = "true")
public class TestEmailController {

    private final EmailService emailService;

    public TestEmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/test-email")
    public ResponseEntity<MessageResponseDTO> sendTestEmail(@RequestParam String to) {
        emailService.sendTestEmail(to);
        return ResponseEntity.ok(new MessageResponseDTO("Email de test envoyé."));
    }
}
