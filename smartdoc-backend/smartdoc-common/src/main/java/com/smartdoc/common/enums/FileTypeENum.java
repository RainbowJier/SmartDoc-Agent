package com.smartdoc.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileTypeENum {
    PDF(".pdf"),
    DOCX(".docx"),
    DOC(".doc"),
    TXT(".txt");

    private final String str;


    public static FileTypeENum fromCode(String str) {
        for (FileTypeENum value : FileTypeENum.values()) {
            if (value.str.equals(str)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unsupported file format: " + str);
    }

}
