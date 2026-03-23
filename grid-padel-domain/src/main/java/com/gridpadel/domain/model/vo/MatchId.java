package com.gridpadel.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

public final class MatchId {

    private final String value;

    private MatchId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("MatchId cannot be null or blank");
        }
        this.value = value;
    }

    public static MatchId generate() {
        return new MatchId(UUID.randomUUID().toString());
    }

    public static MatchId of(String value) {
        return new MatchId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatchId other)) return false;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "MatchId{" + value + "}";
    }
}
