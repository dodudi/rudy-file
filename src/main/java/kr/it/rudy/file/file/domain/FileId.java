package kr.it.rudy.file.file.domain;

import java.util.UUID;

public record FileId(String value) {

    public static FileId generate() {
        return new FileId(UUID.randomUUID().toString());
    }

    public static FileId of(String value) {
        return new FileId(value);
    }

    public String getValue() {
        return value;
    }
}
