package com.gridpadel.domain.model.vo;

import com.gridpadel.domain.exception.ValidationException;

import java.time.LocalDateTime;

public record MatchDateTime(LocalDateTime value) {

    public MatchDateTime {
        if (value == null) {
            throw new ValidationException("Match date-time cannot be null", "dateTime");
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
