package com.pfe.mamacareplus.service;

import com.pfe.mamacareplus.dto.MessageResponseDTO;
import com.pfe.mamacareplus.entity.PasswordResetCodeEntity;
import com.pfe.mamacareplus.entity.User;
import com.pfe.mamacareplus.repository.PasswordResetCodeRepository;
import com.pfe.mamacareplus.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class PasswordResetService {

    private static final int CODE_EXPIRATION_MINUTES = 3;

    private final UserRepository userRepository;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetCodeRepository passwordResetCodeRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordResetCodeRepository = passwordResetCodeRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public MessageResponseDTO sendCode(String email) {
        User user = findUserByEmail(email);
        String normalizedEmail = normalizeEmail(user.getEmail());
        String code = generateCode();

        passwordResetCodeRepository.invalidateUnusedCodesByEmail(normalizedEmail);

        PasswordResetCodeEntity resetCode = new PasswordResetCodeEntity();
        resetCode.setEmail(normalizedEmail);
        resetCode.setCode(code);
        resetCode.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES));
        resetCode.setUsed(false);

        passwordResetCodeRepository.save(resetCode);
        try {
            emailService.sendPasswordResetCode(user.getEmail(), code);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), ex);
        }

        return new MessageResponseDTO("Code envoyé par email.");
    }

    @Transactional
    public MessageResponseDTO verifyCode(String email, String code) {
        User user = findUserByEmail(email);
        findValidCode(user.getEmail(), code);
        return new MessageResponseDTO("Code vérifié.");
    }

    @Transactional
    public MessageResponseDTO resetPassword(String email, String code, String newPassword) {
        User user = findUserByEmail(email);
        PasswordResetCodeEntity resetCode = findValidCode(user.getEmail(), code);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetCode.setUsed(true);
        passwordResetCodeRepository.save(resetCode);

        return new MessageResponseDTO("Mot de passe modifié avec succès");
    }

    private PasswordResetCodeEntity findValidCode(String email, String code) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedCode = code == null ? "" : code.trim();

        PasswordResetCodeEntity resetCode = passwordResetCodeRepository
                .findFirstByEmailAndCodeOrderByCreatedAtDesc(normalizedEmail, normalizedCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code incorrect."));

        if (Boolean.TRUE.equals(resetCode.getUsed())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ce code a déjà été utilisé.");
        }

        if (LocalDateTime.now().isAfter(resetCode.getExpiresAt())) {
            resetCode.setUsed(true);
            passwordResetCodeRepository.save(resetCode);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code expiré.");
        }

        return resetCode;
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucun compte n'existe avec cet email."));
    }

    private String generateCode() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }
}
