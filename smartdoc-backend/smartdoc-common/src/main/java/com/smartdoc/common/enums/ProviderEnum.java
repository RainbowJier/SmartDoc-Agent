package com.smartdoc.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProviderEnum {
    DEEPSEEK("deepseek"),
    ZHIPU("zhipu");

    private final String code;
}
