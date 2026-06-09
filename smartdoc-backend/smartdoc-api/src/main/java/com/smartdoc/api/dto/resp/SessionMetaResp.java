package com.smartdoc.api.dto.resp;

public record SessionMetaResp(
        String sessionId,
        String title,
        long createdAt,
        long lastActiveAt,
        int messageCount,
        String status
) {}
