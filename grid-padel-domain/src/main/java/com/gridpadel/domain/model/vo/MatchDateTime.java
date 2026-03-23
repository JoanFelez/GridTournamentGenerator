package com.gridpadel.domain.model.vo;

import java.time.LocalDateTime;
import java.util.Objects;

public final class MatchDateTime {

    private final LocalDateTime value;

    private MatchDateTime(LocalDateTime value) {
        if (value == null) {
            throw new IllegalArgumentException("Match date-time cannot be null");
        }
        this.value = value;
    }

    public static MatchDateTime of(LocalDateTime value) {
        return new MatchDateTime(value);
    }

    public LocalDateTime value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatchDateTime other)) return false;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "MatchDateTime{" + value + "}";
    }
}
