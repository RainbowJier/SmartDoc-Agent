package com.smartdoc.chat;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatSessionManager {

    private final int maxMessages;
    private final long sessionTimeoutMs;
    private final Map<String, ChatMemory> sessions = new ConcurrentHashMap<>();
    private final Map<String, Long> lastAccessTime = new ConcurrentHashMap<>();
    private final Map<String, SessionMeta> sessionMetadata = new ConcurrentHashMap<>();

    public ChatSessionManager(
            @Value("${chat.max-memory-messages:20}") int maxMessages,
            @Value("${chat.session-timeout-minutes:30}") int sessionTimeoutMinutes) {
        this.maxMessages = maxMessages;
        this.sessionTimeoutMs = sessionTimeoutMinutes * 60_000L;
    }

    public ChatMemory getOrCreate(String sessionId) {
        lastAccessTime.put(sessionId, System.currentTimeMillis());
        SessionMeta meta = sessionMetadata.get(sessionId);
        if (meta == null) {
            long now = System.currentTimeMillis();
            meta = new SessionMeta(sessionId, null, now, now, 0);
            sessionMetadata.put(sessionId, meta);
        }
        return sessions.computeIfAbsent(sessionId,
                id -> MessageWindowChatMemory.withMaxMessages(maxMessages));
    }

    public List<ChatMessage> getSessionHistory(String sessionId) {
        ChatMemory memory = sessions.get(sessionId);
        if (memory == null) {
            return List.of();
        }
        return memory.messages().stream()
                .filter(m -> m.type() != ChatMessageType.SYSTEM)
                .toList();
    }

    public void clearSession(String sessionId) {
        sessions.remove(sessionId);
        lastAccessTime.remove(sessionId);
        sessionMetadata.remove(sessionId);
    }

    public void updateLastAccess(String sessionId) {
        lastAccessTime.put(sessionId, System.currentTimeMillis());
        SessionMeta meta = sessionMetadata.get(sessionId);
        if (meta != null) {
            meta.setLastActiveAt(System.currentTimeMillis());
            ChatMemory memory = sessions.get(sessionId);
            if (memory != null) {
                meta.setMessageCount((int) memory.messages().stream()
                        .filter(m -> m.type() != ChatMessageType.SYSTEM)
                        .count());
            }
        }
    }

    public boolean needsTitle(String sessionId) {
        SessionMeta meta = sessionMetadata.get(sessionId);
        return meta != null && meta.getTitle() == null;
    }

    public void renameSession(String sessionId, String title) {
        SessionMeta meta = sessionMetadata.get(sessionId);
        if (meta != null) {
            meta.setTitle(title);
        }
    }

    public List<SessionMeta> getSessionList() {
        List<SessionMeta> list = new ArrayList<>(sessionMetadata.values());
        list.sort(Comparator.comparingLong(SessionMeta::getCreatedAt).reversed());
        return list;
    }

    public void batchDeleteSessions(List<String> sessionIds) {
        for (String id : sessionIds) {
            sessions.remove(id);
            lastAccessTime.remove(id);
            sessionMetadata.remove(id);
        }
    }

    public void clearAllSessions() {
        sessions.clear();
        lastAccessTime.clear();
        sessionMetadata.clear();
    }

    @Scheduled(fixedRateString = "${chat.eviction-interval-ms:300000}")
    public void cleanExpiredSessions() {
        long now = System.currentTimeMillis();
        log.debug("Starting chat session eviction check. Active sessions: {}", sessions.size());
        lastAccessTime.forEach((sessionId, lastAccess) -> {
            if (now - lastAccess > sessionTimeoutMs) {
                sessions.remove(sessionId);
                lastAccessTime.remove(sessionId);
                sessionMetadata.remove(sessionId);
                log.info("Evicted expired/inactive chat session: {}", sessionId);
            }
        });
    }

    public Map<String, ChatMemory> getAllSessions() {
        return sessions;
    }
}
