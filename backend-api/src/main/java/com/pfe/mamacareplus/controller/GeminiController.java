package com.pfe.mamacareplus.controller;

import com.pfe.mamacareplus.config.GeminiConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gemini")
@CrossOrigin("*")
public class GeminiController {

    private final GeminiConfig geminiConfig;

    public GeminiController(GeminiConfig geminiConfig) {
        this.geminiConfig = geminiConfig;
    }

    @GetMapping("/test")
    public ResponseEntity<String> testGeminiApiKey() {
        if (geminiConfig.isApiKeyConfigured()) {
            return ResponseEntity.ok("Gemini API key is configured");
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Gemini API key is not configured");
    }
}
