package com.smartdoc.chat;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TitleGenerator {

    private static final String SYSTEM_PROMPT = """
            Generate a concise title (max 20 characters, in the same language as the user's question) 
            that summarizes what the user is asking about. 
            Return ONLY the title text, no quotes, no punctuation, no explanation.
            """;

    private final ChatModel chatModel;

    public String generate(String userMessage) {
        try {
            var response = chatModel.chat(List.of(
                    SystemMessage.from(SYSTEM_PROMPT),
                    UserMessage.from(userMessage)
            ));
            String title = response.aiMessage().text().trim();
            if (title.length() > 20) {
                title = title.substring(0, 20).trim();
            }
            return title;
        } catch (Exception e) {
            log.warn("Failed to generate session title for message: {}", userMessage, e);
            return userMessage.length() > 20
                    ? userMessage.substring(0, 17) + "..."
                    : userMessage;
        }
    }
}
