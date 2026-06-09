package com.smartdoc.api.dto.req;

import java.util.List;

public record BatchDeleteSessionReq(
        List<String> sessionIds
) {}
