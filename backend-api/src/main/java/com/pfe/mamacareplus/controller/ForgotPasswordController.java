package com.pfe.mamacareplus.controller;

import com.pfe.mamacareplus.dto.ForgotPasswordEmailRequestDTO;
import com.pfe.mamacareplus.dto.MessageResponseDTO;
import com.pfe.mamacareplus.dto.ResetPasswordRequestDTO;
import com.pfe.mamacareplus.dto.VerifyResetCodeRequestDTO;
import com.pfe.mamacareplus.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/forgot-password")
@CrossOrigin("*")
public class ForgotPasswordController {

    private final PasswordResetService passwordResetService;

    public ForgotPasswordController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/send-code")
    public ResponseEntity<MessageResponseDTO> sendCode(@Valid @RequestBody ForgotPasswordEmailRequestDTO request) {
        return ResponseEntity.ok(passwordResetService.sendCode(request.getEmail()));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<MessageResponseDTO> verifyCode(@Valid @RequestBody VerifyResetCodeRequestDTO request) {
        return ResponseEntity.ok(passwordResetService.verifyCode(request.getEmail(), request.getCode()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponseDTO> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        return ResponseEntity.ok(passwordResetService.resetPassword(
                request.getEmail(),
                request.getCode(),
                request.getNewPassword()
        ));
    }
}
