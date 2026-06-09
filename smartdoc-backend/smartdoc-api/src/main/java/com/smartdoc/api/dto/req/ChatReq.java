package com.smartdoc.api.dto.req;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatReq(

        @NotBlank(message = "Message must not be blank")
        @Size(max = 2000, message = "Message must not exceed 2000 characters")
        String message,

        String sessionId
) {
}
