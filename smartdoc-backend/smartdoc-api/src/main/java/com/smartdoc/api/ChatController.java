package com.smartdoc.api;

import com.smartdoc.api.dto.req.BatchDeleteSessionReq;
import com.smartdoc.api.dto.req.ChatReq;
import com.smartdoc.api.dto.req.RenameSessionReq;
import com.smartdoc.api.dto.resp.ChatHistoryItemResp;
import com.smartdoc.api.dto.resp.SessionMetaResp;
import com.smartdoc.chat.ChatSessionManager;
import com.smartdoc.chat.GeneralAssistant;
import com.smartdoc.chat.TitleGenerator;
import com.smartdoc.common.entity.AjaxResult;
import dev.langchain4j.data.message.*;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.PreDestroy;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ConcurrentHashMap<String, AtomicBoolean> busySessions = new ConcurrentHashMap<>();

    private final ExecutorService executor = new ThreadPoolExecutor(
            4, 20, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(200),
            r -> { Thread t = new Thread(r, "sse-chat"); t.setDaemon(true); return t; },
            new ThreadPoolExecutor.CallerRunsPolicy());

    private final GeneralAssistant assistant;

    private final ChatSessionManager sessionManager;

    private final TitleGenerator titleGenerator;

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@Valid @RequestBody ChatReq request) {
        String sessionId = request.sessionId() != null ? request.sessionId() : "default";

        AtomicBoolean sessionLock = busySessions.putIfAbsent(sessionId, new AtomicBoolean(true));
        if (sessionLock != null) {
            if (sessionLock.get()) {
                SseEmitter errorEmitter = new SseEmitter(0L);
                try {
                    errorEmitter.send(SseEmitter.event().data("{\"error\":\"Current session is busy, please try again later\"}"));
                } catch (IOException ignored) {
                }
                errorEmitter.complete();
                log.warn("Concurrent request rejected for session: {}", sessionId);
                return errorEmitter;
            }
            busySessions.put(sessionId, new AtomicBoolean(true));
        }

        SseEmitter emitter = new SseEmitter(120_000L);
        AtomicBoolean cancelled = new AtomicBoolean(false);

        Future<?> future = executor.submit(() -> {
            try {
                TokenStream tokenStream = assistant.chat(request.message(), sessionId);

                tokenStream
                        .onPartialResponse(token -> {
                            System.out.print(token);
                            System.out.flush();
                            if (cancelled.get()) return;
                            try {
                                emitter.send(SseEmitter.event().data(token));
                            } catch (IOException e) {
                                cancelled.set(true);
                                sendErrorEvent(emitter, "SSE connection lost");
                            }
                        })
                        .onCompleteResponse(response -> {
                            System.out.println();
                            if (cancelled.get()) return;
                            try {
                                if (sessionManager.needsTitle(sessionId)) {
                                    String msg = request.message();
                                    CompletableFuture.runAsync(() -> {
                                        try {
                                            String title = titleGenerator.generate(msg);
                                            sessionManager.renameSession(sessionId, title);
                                        } catch (Exception e) {
                                            log.warn("Failed to generate session title", e);
                                        }
                                    });
                                }
                                sessionManager.updateLastAccess(sessionId);
                                emitter.send(SseEmitter.event().data("[DONE]"));
                            } catch (IOException e) {
                                log.warn("Failed to send [DONE] event", e);
                            } finally {
                                emitter.complete();
                            }
                        })
                        .onError(error -> {
                            if (cancelled.get()) return;
                            log.error("Chat streaming error", error);
                            sendErrorEvent(emitter, "LLM service is temporarily unavailable");
                        })
                        .start();
            } catch (Exception e) {
                log.error("Failed to start chat stream", e);
                sendErrorEvent(emitter, "Failed to start chat stream");
            }
        });

        emitter.onTimeout(() -> {
            cancelled.set(true);
            future.cancel(true);
            busySessions.remove(sessionId);
            log.warn("SSE connection timed out for session: {}", sessionId);
        });

        emitter.onCompletion(() -> {
            cancelled.set(true);
            future.cancel(true);
            busySessions.remove(sessionId);
        });

        return emitter;
    }

    private void sendErrorEvent(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event().data("{\"error\":\"" + message + "\"}"));
        } catch (IOException e) {
            log.warn("Failed to send error event", e);
        } finally {
            emitter.complete();
        }
    }

    private static String messageText(ChatMessage msg) {
        if (msg instanceof AiMessage ai) return ai.text() != null ? ai.text() : "";
        if (msg instanceof UserMessage user) {
            String text = user.singleText();
            int idx = text.indexOf("\n\nAnswer using the following information:");
            return idx != -1 ? text.substring(0, idx) : text;
        }
        if (msg instanceof SystemMessage sys) return sys.text();
        return "";
    }

    @GetMapping("/history/{sessionId}")
    public AjaxResult<List<ChatHistoryItemResp>> getHistory(@PathVariable String sessionId) {
        List<ChatHistoryItemResp> history = sessionManager.getSessionHistory(sessionId).stream()
                .filter(msg -> msg.type() != ChatMessageType.TOOL_EXECUTION_RESULT)
                .map(msg -> new ChatHistoryItemResp(
                        switch (msg.type()) {
                            case USER -> "user";
                            case AI -> "assistant";
                            default -> "system";
                        },
                        messageText(msg)
                ))
                .toList();
        return AjaxResult.success(history);
    }

    @GetMapping("/session/clear/{sessionId}")
    public AjaxResult<Void> clearSession(@PathVariable String sessionId) {
        busySessions.remove(sessionId);
        sessionManager.clearSession(sessionId);
        return AjaxResult.success();
    }

    @GetMapping("/sessions")
    public AjaxResult<List<SessionMetaResp>> getSessions() {
        List<SessionMetaResp> result = sessionManager.getSessionList().stream()
                .map(meta -> new SessionMetaResp(
                        meta.getSessionId(),
                        meta.getTitle(),
                        meta.getCreatedAt(),
                        meta.getLastActiveAt(),
                        meta.getMessageCount(),
                        "active"
                ))
                .toList();
        return AjaxResult.success(result);
    }

    @PutMapping("/session/title")
    public AjaxResult<Void> renameSession(@Valid @RequestBody RenameSessionReq request) {
        if (request.sessionId() == null || request.sessionId().isBlank()) {
            return AjaxResult.badRequest("sessionId 不能为空");
        }
        if (request.title() == null || request.title().isBlank()) {
            return AjaxResult.badRequest("标题不能为空");
        }
        if (request.title().length() > 100) {
            return AjaxResult.badRequest("标题不能超过 100 字符");
        }
        sessionManager.renameSession(request.sessionId(), request.title());
        return AjaxResult.success();
    }

    @PostMapping("/session/title/regenerate")
    public AjaxResult<String> regenerateTitle(@Valid @RequestBody RenameSessionReq request) {
        if (request.sessionId() == null || request.sessionId().isBlank()) {
            return AjaxResult.badRequest("sessionId 不能为空");
        }
        List<ChatMessage> history = sessionManager.getSessionHistory(request.sessionId());
        if (history.isEmpty()) {
            return AjaxResult.badRequest("无法生成标题：会话无历史消息");
        }
        String firstUserMessage = history.stream()
                .filter(m -> m instanceof UserMessage)
                .map(m -> ((UserMessage) m).singleText())
                .findFirst()
                .orElse(null);
        if (firstUserMessage == null) {
            return AjaxResult.badRequest("无法生成标题：会话无用户消息");
        }
        String title = titleGenerator.generate(firstUserMessage);
        sessionManager.renameSession(request.sessionId(), title);
        return AjaxResult.success(title);
    }

    @PostMapping("/sessions/batch")
    public AjaxResult<Void> batchDeleteSessions(@Valid @RequestBody BatchDeleteSessionReq request) {
        if (request.sessionIds() == null || request.sessionIds().isEmpty()) {
            return AjaxResult.badRequest("sessionIds 不能为空");
        }
        if (request.sessionIds().size() > 50) {
            return AjaxResult.badRequest("一次最多删除 50 个会话");
        }
        sessionManager.batchDeleteSessions(request.sessionIds());
        return AjaxResult.success();
    }

    @PostMapping("/sessions/clear")
    public AjaxResult<Void> clearAllSessions() {
        sessionManager.clearAllSessions();
        busySessions.clear();
        return AjaxResult.success();
    }
}
