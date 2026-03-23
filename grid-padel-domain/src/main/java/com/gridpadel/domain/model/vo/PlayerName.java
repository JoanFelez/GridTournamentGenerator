package com.gridpadel.domain.model.vo;

public record PlayerName(String value) {

    private static final int MAX_LENGTH = 100;

    public PlayerName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Player name cannot be null or blank");
        }
        value = value.trim();
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Player name cannot exceed " + MAX_LENGTH + " characters");
        }
    }

    public static PlayerName of(String value) {
        return new PlayerName(value);
    }

    @Override
    public String toString() {
        return "PlayerName{" + value + "}";
    }
}
