package com.gridpadel.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

public final class TournamentId {

    private final String value;

    private TournamentId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("TournamentId cannot be null or blank");
        }
        this.value = value;
    }

    public static TournamentId generate() {
        return new TournamentId(UUID.randomUUID().toString());
    }

    public static TournamentId of(String value) {
        return new TournamentId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TournamentId other)) return false;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "TournamentId{" + value + "}";
    }
}
