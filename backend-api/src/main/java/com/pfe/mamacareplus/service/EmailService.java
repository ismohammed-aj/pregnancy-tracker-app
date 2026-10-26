package com.pfe.mamacareplus.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String FROM_EMAIL = "simosimoff56@gmail.com";

    private final JavaMailSender mailSender;
    private final Environment environment;

    public EmailService(JavaMailSender mailSender, Environment environment) {
        this.mailSender = mailSender;
        this.environment = environment;
    }

    public void sendPasswordResetCode(String to, String code) {
        try {
            ensureGmailAppPasswordConfigured();
            logger.info("Tentative d'envoi du code vers : {}", to);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(to);
            message.setSubject("Code de récupération du mot de passe - MamaCarePlus");
            message.setText(
                    "Bonjour,\n\n" +
                    "Votre code de récupération est : " + code + "\n\n" +
                    "Ce code expire dans 3 minutes.\n\n" +
                    "Si vous n'avez pas demandé cette opération, ignorez cet email.\n\n" +
                    "MamaCarePlus"
            );

            mailSender.send(message);
            logger.info("Code de récupération envoyé vers : {}", to);

        } catch (Exception e) {
            logger.error("Erreur envoi email : {}", e.getMessage(), e);
            throw new RuntimeException(buildMailErrorMessage("l'email de récupération", e), e);
        }
    }

    public void sendTestEmail(String to) {
        try {
            ensureGmailAppPasswordConfigured();
            logger.info("Tentative d'envoi d'un email de test vers : {}", to);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(to);
            message.setSubject("Test email - MamaCarePlus");
            message.setText("Email de test MamaCarePlus. Si vous recevez ce message, SMTP Gmail fonctionne.");

            mailSender.send(message);
            logger.info("Email de test envoyé vers : {}", to);

        } catch (Exception e) {
            logger.error("Erreur envoi email test : {}", e.getMessage(), e);
            throw new RuntimeException(buildMailErrorMessage("l'email de test", e), e);
        }
    }

    private void ensureGmailAppPasswordConfigured() {
        String envPassword = environment.getProperty("GMAIL_APP_PASSWORD");
        String configuredPassword = readSpringMailPassword();

        boolean missingEnvPassword = envPassword == null || envPassword.isBlank();
        boolean unresolvedSpringPassword = configuredPassword == null
                || configuredPassword.isBlank()
                || configuredPassword.contains("${GMAIL_APP_PASSWORD}");

        if (missingEnvPassword && unresolvedSpringPassword) {
            throw new IllegalStateException(
                    "Variable d'environnement GMAIL_APP_PASSWORD manquante. " +
                    "Créez un App Password Gmail, exécutez setx GMAIL_APP_PASSWORD \"votre_app_password_gmail\", " +
                    "puis fermez et rouvrez le terminal ou l'IDE."
            );
        }
    }

    private String readSpringMailPassword() {
        try {
            return environment.getProperty("spring.mail.password");
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String buildMailErrorMessage(String action, Exception exception) {
        String details = rootMessage(exception);
        String lowerDetails = details.toLowerCase();
        String hint = "";

        if (details.contains("535") || lowerDetails.contains("username and password not accepted")) {
            hint = " Vérifiez que GMAIL_APP_PASSWORD contient un App Password Gmail valide, pas le mot de passe normal.";
        } else if (details.contains("534") || lowerDetails.contains("application-specific password")) {
            hint = " Gmail demande un App Password : activez la validation en 2 étapes puis créez un mot de passe d'application.";
        } else if (lowerDetails.contains("timeout") || lowerDetails.contains("timed out")) {
            hint = " Vérifiez la connexion réseau, le pare-feu et l'accès sortant vers smtp.gmail.com:587.";
        } else if (lowerDetails.contains("mail server connection failed") || lowerDetails.contains("could not connect")) {
            hint = " Vérifiez la configuration SMTP Gmail host/port/starttls.";
        }

        return "Impossible d'envoyer " + action + " : " + details + hint;
    }

    private String rootMessage(Throwable throwable) {
        Throwable cursor = throwable;
        while (cursor.getCause() != null) {
            cursor = cursor.getCause();
        }

        String message = cursor.getMessage();
        if (message == null || message.isBlank()) {
            message = throwable.getMessage();
        }

        return message == null || message.isBlank() ? throwable.getClass().getSimpleName() : message;
    }
}
