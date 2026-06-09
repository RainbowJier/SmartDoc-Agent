package com.smartdoc.chat;

import com.smartdoc.agent.tools.KnowledgeSearchTool;
import com.smartdoc.agent.tools.TaskStatusTool;
import com.smartdoc.rag.RetrievalService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Chat configuration — AiService assembly core.
 *
 * @see AiServices
 * @see GeneralAssistant
 */
@Configuration
public class ChatConfig {

    @Bean
    public GeneralAssistant smartDocAssistant(
            ChatModel chatModel,
            StreamingChatModel streamingChatModel,
            RetrievalService retrievalService,
            ChatSessionManager sessionManager,
            KnowledgeSearchTool knowledgeSearchTool,
            TaskStatusTool taskStatusTool) {

        return AiServices.builder(GeneralAssistant.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .contentRetriever(retrievalService.getContentRetriever())
                .chatMemoryProvider(memoryId -> sessionManager.getOrCreate(memoryId.toString()))
                .tools(knowledgeSearchTool, taskStatusTool)
                .build();
    }
}
