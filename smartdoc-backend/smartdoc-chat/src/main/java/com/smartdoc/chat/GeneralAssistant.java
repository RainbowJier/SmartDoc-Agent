package com.smartdoc.chat;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface GeneralAssistant {

    @SystemMessage(fromResource = "prompts/smartdoc-assistant.md")
    TokenStream chat(@UserMessage String message, @MemoryId String sessionId);
}
