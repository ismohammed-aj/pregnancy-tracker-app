package com.pfe.mamacareplus.service;

import com.pfe.mamacareplus.dto.MessageResponseDTO;
import com.pfe.mamacareplus.entity.PasswordResetCodeEntity;
import com.pfe.mamacareplus.entity.User;
import com.pfe.mamacareplus.repository.PasswordResetCodeRepository;
import com.pfe.mamacareplus.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetCodeRepository passwordResetCodeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    private PasswordResetService passwordResetService;
    private User user;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetService(
                userRepository,
                passwordResetCodeRepository,
                passwordEncoder,
                emailService
        );

        user = new User();
        user.setUserId(1);
        user.setEmail("User@Example.com");
        user.setPassword("old-hash");
        user.setRole("FEMME");
    }

    @Test
    void sendCodeInvalidatesOldCodesSavesSixDigitCodeAndDoesNotReturnIt() {
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(passwordResetCodeRepository.save(any(PasswordResetCodeEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MessageResponseDTO response = passwordResetService.sendCode(" user@example.com ");

        ArgumentCaptor<PasswordResetCodeEntity> codeCaptor = ArgumentCaptor.forClass(PasswordResetCodeEntity.class);
        verify(passwordResetCodeRepository).invalidateUnusedCodesByEmail("user@example.com");
        verify(passwordResetCodeRepository).save(codeCaptor.capture());

        PasswordResetCodeEntity savedCode = codeCaptor.getValue();
        assertEquals("user@example.com", savedCode.getEmail());
        assertTrue(savedCode.getCode().matches("\\d{6}"));
        assertFalse(savedCode.getUsed());
        assertTrue(savedCode.getExpiresAt().isAfter(LocalDateTime.now().plusMinutes(2)));
        assertTrue(savedCode.getExpiresAt().isBefore(LocalDateTime.now().plusMinutes(4)));
        assertFalse(response.getMessage().contains(savedCode.getCode()));

        verify(emailService).sendPasswordResetCode("User@Example.com", savedCode.getCode());
    }

    @Test
    void verifyCodeSucceedsWhenCodeIsUnusedAndNotExpired() {
        PasswordResetCodeEntity resetCode = resetCode(LocalDateTime.now().plusMinutes(1), false);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(passwordResetCodeRepository.findFirstByEmailAndCodeOrderByCreatedAtDesc("user@example.com", "123456"))
                .thenReturn(Optional.of(resetCode));

        MessageResponseDTO response = passwordResetService.verifyCode("user@example.com", "123456");

        assertEquals("Code vérifié.", response.getMessage());
        assertFalse(resetCode.getUsed());
        verify(passwordResetCodeRepository, never()).save(resetCode);
    }

    @Test
    void verifyCodeRejectsIncorrectCode() {
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(passwordResetCodeRepository.findFirstByEmailAndCodeOrderByCreatedAtDesc("user@example.com", "000000"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> passwordResetService.verifyCode("user@example.com", "000000")
        );

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void verifyCodeMarksExpiredCodeAsUsed() {
        PasswordResetCodeEntity resetCode = resetCode(LocalDateTime.now().minusSeconds(1), false);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(passwordResetCodeRepository.findFirstByEmailAndCodeOrderByCreatedAtDesc("user@example.com", "123456"))
                .thenReturn(Optional.of(resetCode));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> passwordResetService.verifyCode("user@example.com", "123456")
        );

        assertEquals(400, exception.getStatusCode().value());
        assertTrue(resetCode.getUsed());
        verify(passwordResetCodeRepository).save(resetCode);
    }

    @Test
    void resetPasswordHashesPasswordAndMarksCodeAsUsed() {
        PasswordResetCodeEntity resetCode = resetCode(LocalDateTime.now().plusMinutes(1), false);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(passwordResetCodeRepository.findFirstByEmailAndCodeOrderByCreatedAtDesc("user@example.com", "123456"))
                .thenReturn(Optional.of(resetCode));
        when(passwordEncoder.encode("newPassword")).thenReturn("hashed-password");

        MessageResponseDTO response = passwordResetService.resetPassword("user@example.com", "123456", "newPassword");

        assertEquals("Mot de passe modifié avec succès", response.getMessage());
        assertEquals("hashed-password", user.getPassword());
        assertTrue(resetCode.getUsed());
        verify(userRepository).save(user);
        verify(passwordResetCodeRepository).save(resetCode);
    }

    @Test
    void sendCodeRejectsUnknownEmail() {
        when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> passwordResetService.sendCode("missing@example.com")
        );

        assertEquals(404, exception.getStatusCode().value());
        verifyNoInteractions(emailService);
    }

    private PasswordResetCodeEntity resetCode(LocalDateTime expiresAt, boolean used) {
        PasswordResetCodeEntity resetCode = new PasswordResetCodeEntity();
        resetCode.setEmail("user@example.com");
        resetCode.setCode("123456");
        resetCode.setExpiresAt(expiresAt);
        resetCode.setUsed(used);
        resetCode.setCreatedAt(LocalDateTime.now());
        return resetCode;
    }
}
