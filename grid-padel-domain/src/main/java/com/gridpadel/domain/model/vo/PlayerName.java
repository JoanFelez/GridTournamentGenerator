package com.gridpadel.domain.model.vo;

import com.gridpadel.domain.exception.ValidationException;

public record PlayerName(String value) {

    private static final int MAX_LENGTH = 100;

    public PlayerName {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Player name cannot be blank", "playerName");
        }
        value = value.trim();
        if (value.length() > MAX_LENGTH) {
            throw new ValidationException("Player name cannot exceed " + MAX_LENGTH + " characters", "playerName");
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
