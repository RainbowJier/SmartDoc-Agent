package com.smartdoc.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionMeta {
    private String sessionId;
    private String title;
    private long createdAt;
    private long lastActiveAt;
    private int messageCount;
}
