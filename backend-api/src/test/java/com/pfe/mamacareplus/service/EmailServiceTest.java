package com.pfe.mamacareplus.service;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Test
    void sendPasswordResetCodeReturnsClearErrorWhenGmailAppPasswordIsMissing() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        Environment environment = mock(Environment.class);
        when(environment.getProperty("GMAIL_APP_PASSWORD")).thenReturn(null);
        when(environment.getProperty("spring.mail.password")).thenReturn("${GMAIL_APP_PASSWORD}");

        EmailService emailService = new EmailService(mailSender, environment);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> emailService.sendPasswordResetCode("user@example.com", "123456")
        );

        assertTrue(exception.getMessage().contains("GMAIL_APP_PASSWORD"));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendPasswordResetCodeSendsEmailWhenGmailAppPasswordIsConfigured() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        Environment environment = mock(Environment.class);
        when(environment.getProperty("GMAIL_APP_PASSWORD")).thenReturn("configured-app-password");

        EmailService emailService = new EmailService(mailSender, environment);

        emailService.sendPasswordResetCode("user@example.com", "123456");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
