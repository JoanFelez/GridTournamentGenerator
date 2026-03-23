package com.gridpadel.domain.model.vo;

import java.time.LocalDateTime;

public record MatchDateTime(LocalDateTime value) {

    public MatchDateTime {
        if (value == null) {
            throw new IllegalArgumentException("Match date-time cannot be null");
        }
    }

    public static MatchDateTime of(LocalDateTime value) {
        return new MatchDateTime(value);
    }

    @Override
    public String toString() {
        return "MatchDateTime{" + value + "}";
    }
}
