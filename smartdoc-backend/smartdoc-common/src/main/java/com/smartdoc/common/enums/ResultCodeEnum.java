package com.smartdoc.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ResultCodeEnum {
    SUCCESS(200, "Operation successful"),
    FAILED(500, "Operation failed"),
    VALIDATE_FAILED(417, "Parameter validation failed"),
    UNAUTHORIZED(401, "Not logged in or token expired"),
    FORBIDDEN(403, "Access denied"),
    BAD_REQUEST(400, "Bad request"),
    NOT_FOUND(404, "Resource not found"),
    CONFLICT(409, "Resource conflict"),
    TOO_MANY_REQUESTS(429, "Too many requests"),
    SERVICE_UNAVAILABLE(503, "Service unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway timeout");

    private final int code;
    private final String message;

    public static ResultCodeEnum of(String name) {
        return Arrays.stream(values()).filter((s) -> s.equalsTo(name)).findFirst().orElse(FAILED);
    }

    public static ResultCodeEnum of(Integer code) {
        return Arrays.stream(values()).filter((s) -> s.getCode() == (long) code).findFirst().orElse(FAILED);
    }

    private boolean equalsTo(String name) {
        return this.name().equalsIgnoreCase(name);
    }

}