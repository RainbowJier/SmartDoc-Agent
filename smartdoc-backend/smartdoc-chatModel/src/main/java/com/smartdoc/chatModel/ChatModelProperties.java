package com.smartdoc.chatModel;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "llm")
public class ChatModelProperties {

    private String provider = "deepseek";

    private Map<String, ProviderConfig> providers = new HashMap<>();

    public ProviderConfig getActiveConfig() {
        ProviderConfig config = providers.get(provider.toLowerCase());
        if (config == null) {
            throw new IllegalArgumentException("Unknown LLM provider: '" + provider + "'. Supported: " + providers.keySet());
        }
        
        // Handle LLM_API_KEY environment variable override
        String envKey = System.getenv("LLM_API_KEY");
        if (envKey != null && !envKey.isBlank()) {
            config.setApiKey(envKey);
        }

        String apiKey = config.getApiKey();
        if (apiKey == null || apiKey.isBlank() || apiKey.contains("your-api-key-here")) {
            throw new IllegalStateException(
                    "API Key not configured for provider '" + provider + "'. " +
                    "Set environment variable LLM_API_KEY, " + provider.toUpperCase() + "_API_KEY or " +
                    "configure llm.providers." + provider + ".api-key in application.yml");
        }
        return config;
    }

    @Getter
    @Setter
    public static class ProviderConfig {
        private String baseUrl;
        private String apiKey;
        private String modelName;
        private Integer maxTokens = 4096;
    }
}
