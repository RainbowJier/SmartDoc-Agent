package com.smartdoc.api.dto.req;

public record RenameSessionReq(
        String sessionId,
        String title
) {}
