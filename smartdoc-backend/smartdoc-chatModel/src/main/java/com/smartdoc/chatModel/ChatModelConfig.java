package com.smartdoc.chatModel;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ChatModelProperties.class)
public class ChatModelConfig {

    private final ChatModelProperties chatModelProperties;

    @Bean
    public ChatModel chatLanguageModel() {
        ChatModelProperties.ProviderConfig config = chatModelProperties.getActiveConfig();
        String provider = chatModelProperties.getProvider();
        log.info("Initializing ChatModel: provider={}, model={}", provider, config.getModelName());
        return buildChatModel(config);
    }

    @Bean
    public StreamingChatModel streamingChatModel() {
        ChatModelProperties.ProviderConfig config = chatModelProperties.getActiveConfig();
        String provider = chatModelProperties.getProvider();
        log.info("Initializing StreamingChatModel: provider={}, model={}", provider, config.getModelName());
        return buildStreamingChatModel(config);
    }

    private ChatModel buildChatModel(ChatModelProperties.ProviderConfig config) {
        var builder = OpenAiChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .maxTokens(config.getMaxTokens());
        return builder.build();
    }

    private StreamingChatModel buildStreamingChatModel(ChatModelProperties.ProviderConfig config) {
        var builder = OpenAiStreamingChatModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .maxTokens(config.getMaxTokens());
        return builder.build();
    }
}
