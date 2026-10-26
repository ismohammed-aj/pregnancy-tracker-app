package com.pfe.mamacareplus.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class GeminiConfig {

    private final String apiKey;

    public GeminiConfig() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        String environmentApiKey = System.getenv("GEMINI_API_KEY");
        this.apiKey = StringUtils.hasText(environmentApiKey)
                ? environmentApiKey
                : dotenv.get("GEMINI_API_KEY");
    }

    public String getApiKey() {
        return apiKey;
    }

    public boolean isApiKeyConfigured() {
        return StringUtils.hasText(apiKey);
    }
}
