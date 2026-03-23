package com.gridpadel.domain.model.vo;

import java.util.Objects;

public final class PlayerName {

    private static final int MAX_LENGTH = 100;

    private final String value;

    private PlayerName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Player name cannot be null or blank");
        }
        String trimmed = value.trim();
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Player name cannot exceed " + MAX_LENGTH + " characters");
        }
        this.value = trimmed;
    }

    public static PlayerName of(String value) {
        return new PlayerName(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerName other)) return false;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "PlayerName{" + value + "}";
    }
}
