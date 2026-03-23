package com.gridpadel.domain.model.vo;

import com.gridpadel.domain.exception.ValidationException;

import java.util.UUID;

public record MatchId(String value) {

    public MatchId {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Match ID cannot be blank", "matchId");
        }
    }

    public static MatchId generate() {
        return new MatchId(UUID.randomUUID().toString());
    }

    public static MatchId of(String value) {
        return new MatchId(value);
    }

    @Override
    public String toString() {
        return "MatchId{" + value + "}";
    }
}
