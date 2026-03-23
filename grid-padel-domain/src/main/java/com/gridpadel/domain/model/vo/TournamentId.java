package com.gridpadel.domain.model.vo;

import java.util.UUID;

public record TournamentId(String value) {

    public TournamentId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("TournamentId cannot be null or blank");
        }
    }

    public static TournamentId generate() {
        return new TournamentId(UUID.randomUUID().toString());
    }

    public static TournamentId of(String value) {
        return new TournamentId(value);
    }

    @Override
    public String toString() {
        return "TournamentId{" + value + "}";
    }
}
