package com.github.happylynx.prick.lib.model;

import java.util.stream.Stream;

public enum FsEntryType {
    FILE("f"),
    DIRECTORY("D"),
    LINK("l"),
    ;
    private final String code;

    FsEntryType(String code) {
        this.code = code;
    }

    public static FsEntryType parse(String code) {
        return Stream.of(FsEntryType.values())
                .filter(type -> type.code.equals(code))
                .findAny()
                .orElseThrow();
    }

    public String getCode() {
        return code;
    }
}
